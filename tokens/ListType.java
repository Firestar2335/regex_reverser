package tokens;

public enum ListType {
	/** Token is not a list. */
	NOTLIST,
	/** Token should be interpeted as a fixed length list of tokens */
	FIXED,
	/**
	 * Token should be interpreted as a variable length list of tokens that are primarily single
	 * characters. Appends should create a new item at the end.
	 */
	LIST,
	/**
	 * Token should be interpreted as a variable length list of token lists. Appends should add to
	 * the last entry.
	 */
	ALTERNATION
}
