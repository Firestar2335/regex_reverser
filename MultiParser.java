import tokens.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * This is a multithreaded regex parser, for when you need to parse gigabytes of regular expressions.
 */
public class MultiParser implements Parser {
	private static final boolean DEBUG = false;

	/** The timeout time for each poll */
	private static final long TIMEOUT_TIME = 10l;
	/** The timeout unit */
	private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

	/** Maximum number of failed attempts before throwing an error */
	private static final int FAILED_ATTEMPTS = 10;

	/** The number of threads used */
	private int n;
	/** Whether child threads should be daemonic */
	private boolean makeDaemonic;

	/**
	 * Instantiates a {@code MultiParser} object with {@code n} threads, which are all not daemons
	 * @param n The number of threads used
	 */
	public MultiParser(int n) {
		this(n, false);
		//threads = new Thread[n];
	}

	/**
	 * Instantiates a {@code MultiParser} object using {@code n} threads.
	 * @param n The number of threads to use
	 * @param makeDeamonic Whether the processes should be daemonic
	*/
	public MultiParser(int n, boolean makeDaemonic) {
		this.n = n;
		this.makeDaemonic = makeDaemonic;
	}

	public TokenBase parseSingle(String str) {
		MultiResult first = multiParse(str, 0, str.length());
		Queue<Group> tasks = new LinkedList<>(first.groups);
		while (tasks.size() > 0) {
			Group task = tasks.poll();
			MultiResult r = multiParse(str, task.start, task.stop);
			for (Group g : r.groups) {
				g.indices.addAll(0, task.indices);
			}
			updateGroup(first.tokens, r.tokens, task);
			tasks.addAll(r.groups);
		}
		return first.tokens;
	}

	public TokenBase parse(String str) {
		MultiResult first = multiParse(str, 0, str.length());
		if (DEBUG) {
			System.out.println(first);
			System.out.println();
		}
		if (first.groups.size() == 0) {
			return first.tokens;
		}

		BlockingQueue<Group> taskQueue = new LinkedBlockingQueue<>(first.groups);
		BlockingQueue<ReceiveResult> recv = new LinkedBlockingQueue<>(10);//SynchronousQueue<>();
		MultiContext con = new MultiContext(str, taskQueue, recv, Thread.currentThread(), makeDaemonic);
		/*Thread[] threads = new Thread[n];
		for (int i = 0; i < n; i++) {
			threads[i] = new Thread(new MultiParserThread(con));
			threads[i].setDaemon(makeDaemonic);
			threads[i].start();
		}*/
		//System.out.println(con.unfinishedTasks);
		for (int i = 0; i < n; i++) {
			Thread t = new Thread(new MultiParserThread(con));
			t.setDaemon(makeDaemonic);
			t.start();
			con.threads.add(t);
		}
		int count = 0;//Failed attempts
		try {
			while (!con.isEmpty()) {
				try {
					ReceiveResult result = recv.poll(TIMEOUT_TIME, TIMEOUT_UNIT);
					if (DEBUG) {
						System.out.println(result);
					}
					if (result == null) {
						count++;
						if (count == FAILED_ATTEMPTS / 2) {
							con.reviveThreads();//reviveThreads(con.threads, con);
						}
						else if (count >= FAILED_ATTEMPTS) {
							throw new TimeoutException();
						}
						continue;
					}
					count = 0;
					updateGroup(first.tokens, result.tokens, result.group);
					if (DEBUG) {
						System.out.println(first.tokens);
						System.out.print("Unfinished tasks: ");
						//System.out.println(con.unfinishedTasks);
						System.out.println();
					}
				} catch (InterruptedException e) {}
				catch (TimeoutException e) {
					//con.close();
					throw new RuntimeException(e);
				}
			}
		}
		finally {
			con.close();
		}
		//Process rest of recv
		for (ReceiveResult r : recv) {
			updateGroup(first.tokens, r.tokens, r.group);
		}
		//System.gc();
		return first.tokens;
	}

	/**
	 * Updates the group in {@code root} specified by {@code info} with the tokens in 
	 * {@code replacement} 
	 * @param root The root tokens to update
	 * @param replacement The tokens to update with
	 * @param info The info specifying the group to update
	 */
	private static void updateGroup(TokenBase root, TokenBase replacement, Group info) {
		for (int index : info.indices) {
			root = root.get(index);
		}
		root.replace(replacement);
	}

	/* *
	 * Interrupts all threads that are still running.
	 * @param threads The threads to interrupt
	 * @return The number of threads interrupted by this method
	 * /
	private int exitThreads(Thread[] threads)  {
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
	 * Replaces any threads in {@code threads} that are no longer alive with fresh threads that are
	 * started.
	 * @param threads The threads to revive
	 * @param context The context object to use for the threads
	 * @return The number of threads that were revived
	 * /
	private int reviveThreads(Thread[] threads, MultiContext context) {
		int count = 0;
		for (int i = 0; i < threads.length; i++) {
			if (!threads[i].isAlive()) {
				threads[i] = new Thread(new MultiParserThread(context));
				threads[i].setDaemon(makeDaemonic);
				threads[i].start();
				count++;
			}
		}
		return count;
	}

	private int exitThreads(List<Thread> threads) {
		int count = 0;
		for (Thread t : threads) {
			if (t.isAlive()) {
				t.interrupt();
				count++;
			}
		}
		return count;
	}

	private int reviveThreads(List<Thread> threads, MultiContext context) {
		int count = 0;
		for (int i = 0; i < threads.size(); i++) {
			if (!threads.get(i).isAlive()) {
				threads.set(i,new Thread(new MultiParserThread(context)));
				threads.get(i).setDaemon(makeDaemonic);
				threads.get(i).start();
				count++;
			}
		}
		return count;
	}*/

	/**
	 * Parses only the first level of {@code str} between the specified indices, replacing all 
	 * groups in the returned result with dummy values.
	 * @param str The string to parse
	 * @param start The index to start parsing from, inclusive
	 * @param stop The index to stop parsing at, exclusive
	 * @return a {@code MultiResult} object containing the result of this pass and the location 
	 * information of the groups that still need to be parsed
	 */
	public static MultiResult multiParse(String str, int start, int stop) {
		TokenBase tokens = new TokenList();
		List<Group> groups = new ArrayList<>();
		int i = start;
		while (i < stop) {
			switch (str.charAt(i)) {
				case '\\':
					tokens.append(new Token(str.substring(i, i+2)));
					i++;
					break;
				case '(':
					int j = Parser.findMatch(str,i,stop,'(',')',true);
					TokenBase k = tokens;
					List<Integer> ind = new ArrayList<>();
					while (k.listType() != 0) {
						if (k.listType() == 1) {
							ind.add(k.size()-1);
						}
						else {
							ind.add(k.size());
						}
						if (k.size() == 0) {
							break;
						}
						k = k.get(k.size()-1);
					}
					groups.add(new Group(i+1,j,ind));
					tokens.append(new TokenGroup());
					i = j;
					break;
				case '|':
					if (tokens instanceof Alternation) {
						tokens.newAlt();
					}
					else {
						tokens = Alternation.fromTokens(tokens);
						for (Group g : groups) {
							g.indices.addFirst(0);
						}
					}
					break;
				case '[':
					j = Parser.findMatch(str, i, str.length(), '[',']', true);
					tokens.append(new CharacterClass(str.substring(i+1,j)));
					i = j;
					break;
				case '{':
					j = Parser.findMatch(str, i, str.length(), '{','}',true);
					tokens.setLastToken(new Quantifier(tokens.getLastToken(), str.substring(i,j+1)));
					i = j;
					break;
				case '?':
				case '*':
				case '+':
					tokens.setLastToken(new Quantifier(tokens.getLastToken(), str.substring(i,i+1)));
					break;
				case '$':
				case '^':
					tokens.append(new Anchor(str.substring(i,i+1)));
					break;
				default:
					tokens.append(new Token(str.substring(i,i+1)));
					break;
			}
			i++;
		}
		return new MultiResult(tokens, groups);
	}

	/**
	 * This class represents the result of the {@code multiParse} method
	 */
	public static class MultiResult {
		/** The tokens of the result, with groups holding dummy results. */
		public final TokenBase tokens;
		/** The groups that need to be parsed within this result. */
		public final List<Group> groups;
		public MultiResult(TokenBase tokens, List<Group> groups) {
			this.tokens = tokens;
			this.groups = groups;
		}

		public String toString() {
			return "MultiResult(tokens = "+tokens.toString()+", groups = "+groups.toString()+")";
		}
	}

	/** 
	 * This class represents information on the location of a group that needs to be parsed in the 
	 * string and in the match resultl
	 */
	public static class Group {
		/** The start index of the group in the string */
		public final int start;
		/** The stop index of the group in the string */
		public final int stop;
		/** Recursive indices of the group into the parse result */
		public List<Integer> indices;
		public Group(int start, int stop, List<Integer> indices) {
			this.start = start;
			this.stop = stop;
			this.indices = indices;
		}

		public String toString() {
			return "Group(start = "+start+", stop = "+stop+", indices = "+indices;
		}
	}

	/** Object used to transfer data between processes */
	public static class ReceiveResult {
		/** The tokens belonging to the group */
		public final TokenBase tokens;
		/** The group object containing info about the group */
		public final Group group;
		public ReceiveResult(TokenBase tokens, Group group) {
			this.tokens = tokens;
			this.group = group;
		}

		public String toString() {
			return "ReceiveResult(tokens = "+tokens.toString()+", group = "+group.toString()+")";
		}
	}

}
