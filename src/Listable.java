public interface Listable<T> {
	public T get(int index);

	public void set(int index, T item);

	public int len();
}
