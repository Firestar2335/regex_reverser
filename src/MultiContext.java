import java.util.*;
import java.util.concurrent.*;
import java.time.*;

/** This class represents a synchronized state among threads. */
public class MultiContext {
	private static final long LIVE_CHECK_DELAY = 1l;
	private static final long LIVE_CHECK_PERIOD = 10l;
	private static final TimeUnit LIVE_CHECK_UNIT = TimeUnit.MINUTES;

	private static final long INFO_DELAY = 1l;
	private static final long INFO_PERIOD = 720l;//12 hours
	private static final TimeUnit INFO_UNIT = TimeUnit.MINUTES;

	/** The string that is being parsed */
	public final String str;
	//private TokenBase tokens;
	/** The queue of groups that still need to be parsed */
	private final BlockingQueue<MultiParser.Group> taskQueue;
	/** The number of outstanding tasks in the task queue */
	private volatile int unfinishedTasks;
	/** The queue to the master thread to process results */
	public final BlockingQueue<MultiParser.ReceiveResult> toMaster;
	/** The master thread that instantiated the worker threads. */
	public final Thread master;
	/** List of child worker threads */
	public final List<Thread> threads;

	/** The executor to execute the liveness timer check */
	private final ScheduledExecutorService timerThread;
	/** The task that monitors active threads */
	private ScheduledFuture<?> watchdog;
	/** The task that prints status info to the console */
	private ScheduledFuture<?> status;

	/** Whether instantiated threads are daemonic */
	private final boolean daemonic;
	//lock for tokens
	//private Object tokenLock = new Object();

	/**
	 * Creares a new context object
	 * @param str The string that is being parsed
	 * @param taskQueue The queue of tasks
	 * @param recv The recieving queue for processing of results
	 * @param master The master process 
	 * @param daemonic Whether child threads should be daemonic
	 */
	public MultiContext(String str, BlockingQueue<MultiParser.Group> taskQueue, BlockingQueue<MultiParser.ReceiveResult> recv, Thread master, boolean daemonic) {
		this.str = str;
		this.taskQueue = taskQueue;
		unfinishedTasks = taskQueue.size();
		toMaster = recv;
		this.master = master;
		this.threads = new CopyOnWriteArrayList<>();
		this.daemonic = daemonic;
		timerThread = Executors.newSingleThreadScheduledExecutor();
		watchdog = timerThread.scheduleAtFixedRate(new ThreadChecker(), LIVE_CHECK_DELAY, LIVE_CHECK_PERIOD, LIVE_CHECK_UNIT);
		status = timerThread.scheduleAtFixedRate(new TaskInfo(), INFO_DELAY, INFO_PERIOD, INFO_UNIT);
	}

	/**
	 * Takes a task from the task queue, blocking if necessary
	 * @return The first task on the queue
	 * @throws InterruptedException If the thread is interrupted while waiting.
	 */
	public MultiParser.Group take() throws InterruptedException {
		return taskQueue.take();
	}

	/**
	 * Polls a task from the task queue, waiting the specified amount before timing out.
	 * @param timeout The duration to wait
	 * @param unit The unit {@code timeout} is specified in
	 * @return The first task on the queue
	 * @throws InterruptedException If the thread is interrupted while waiting/
	 * @throws TimeoutException If the request times out
	 */
	public MultiParser.Group poll(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		MultiParser.Group res = taskQueue.poll(timeout, unit);
		if (res == null) {
			throw new TimeoutException();
		}
		return res;
	}

	/**
	 * Indicates to the queue that a task has been successfully finished.
	 */
	public synchronized void taskDone() {
		unfinishedTasks--;
		//if (unfinishedTasks == 0) {
		//	System.out.println("No unfinished tasks, interrupting master");
		//	master.interrupt();
		//}
	}

	/**
	 * Puts the task onto the task queue, blocking if necessary.
	 * @param item The item to put onto the queue
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public synchronized void put(MultiParser.Group item) throws InterruptedException {
		taskQueue.put(item);
		unfinishedTasks++;
	}

	/**
	 * Puts all of the items in the collection into the queue.
	 * @param items The tasks to put onto the queue.
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public synchronized void putAll(Collection<? extends MultiParser.Group> items) throws InterruptedException {
		/*for (MultiParser.Group item : items) {
			put(item);
		}*/
		int s = items.size();
		for (MultiParser.Group item : items) {
			taskQueue.put(item);
		}
		unfinishedTasks += s;
	}

	/**
	 * Checks if all tasks in the queue have been completed
	 * @return {@code true} if there are no outstanding tasks in the task queue, {@code false} 
	 * otherwise.
	 */
	public boolean isEmpty() {
		return unfinishedTasks == 0;
	}

	/**
	 * Cleanly exits the threads in this context
	 */
	public void close() {
		watchdog.cancel(true);
		status.cancel(false);
		timerThread.close();
		try {
			timerThread.awaitTermination(20l, TimeUnit.MINUTES);
		} catch (InterruptedException e) {}
		exitThreads();
	}

	/**
	 * Interrupts threads that are still running
	 * @return The number of threads that were interrupted
	 */
	public int exitThreads() {
		int count = 0;
		for (Thread t : threads) {
			if (t.isAlive()) {
				t.interrupt();
				count++;
			}
		}
		return count;
	}

	/**
	 * Replaces threads that are no longer alive with new threads
	 * @return The number of threads that were revived
	 */
	public int reviveThreads() {
		int count = 0;
		for (int i = 0; i < threads.size(); i++) {
			if (!threads.get(i).isAlive()) {
				threads.set(i,new Thread(new MultiParserThread(this)));
				threads.get(i).setDaemon(daemonic);
				threads.get(i).start();
				count++;
			}
		}
		return count;
	}

	/**
	 * This class wraps the {@code reviveThreads} method in a runnable.
	 */
	private class ThreadChecker implements Runnable {
		public void run() {
			if (!isEmpty()){
				reviveThreads();
			}
		}
	}

	/**
	 * This class prints out the date and the number of items in both queues
	 */
	private class TaskInfo implements Runnable {
		public void run() {
			LocalDateTime now = LocalDateTime.now();
			System.out.printf("%s - %d unfinished tasks in queue, %d results waiting to be processed%n", now.format(Client.fmt), unfinishedTasks, toMaster.size());
		}
	}
}