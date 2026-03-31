package tokens;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * This is a task that reverses the provided spliterator
 */
class ReverseWorker extends RecursiveTask<List<TokenBase>> {
	private final Spliterator<TokenBase> split;

	public ReverseWorker(Spliterator<TokenBase> spliterator) {
		split = spliterator;
	}

	public List<TokenBase> compute() {
		int initialSize = longToInt(split.estimateSize());
		List<ReverseWorker> subTasks = new ArrayList<>();
		Spliterator<TokenBase> sub;
		while (split.estimateSize() > TokenBase.BATCH_SIZE && (sub = split.trySplit()) != null) {
			ReverseWorker next = new ReverseWorker(sub);
			next.fork();
			subTasks.add(next);
		}
		List<TokenBase> result = new ArrayList<>(initialSize);
		split.forEachRemaining(new Prepender(result));
		for (ReverseWorker w : subTasks.reversed()) {
			result.addAll(w.join());
		}
		return result;
	}

	private static int longToInt(long num) {
		return Math.clamp(num, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * This is a class that wraps the {@code reverseMulti} method of a {@code TokenBase} 
	 * instance composed into the {@code addFirst} method of a list.
	 */
	private static class Prepender implements Consumer<TokenBase> {
		public final List<? super TokenBase> list;

		/**
		 * Creates a new instance that prepends reversed forms of items to the specified list
		 * @param list The list to prepend to
		 */
		public Prepender(List<? super TokenBase> list) {
			this.list = list;
		}

		public void accept(TokenBase t) {
			list.addFirst(t.reverseMulti());
		}

		/*public List<? super TokenBase> getList() {
			return list;
		}*/
	}
}
