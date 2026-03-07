package tokens;

/**
 * Base class for tokens.
 */
public abstract class TokenBase {
	private static final String LIST_ERR_MSG = "Token does not support indexing";
	public static final boolean DEBUG = false;

	/**
	 * Returns a version of {@code this} representing the regex reversed.
	 * @return a reversed version of {@code this}
	 */
	public abstract TokenBase reverse();

	/**
	 * Returns a {@code String} of the RegEx represented by this token
	 * @return
	 */
	public abstract String compile();

	public String toString() {
		return this.compile();
	}

	/**
	 * Gets the token or group of tokens at the specified index
	 * @param index the index to get
	 * @return The item at {@code index}
	 * @throws UnsupportedOperationException if the class does not support list operations
	 */
	public TokenBase get(int index) {
		throw new UnsupportedOperationException(LIST_ERR_MSG);
	}

	/**
	 * Sets the token or group of tokens at the specified index to the specified value
	 * @param index The index to set
	 * @param item The item to set at {@code index}
	 * @throws UnsupportedOperationException if the class does not support list operations
	 */
	public void set(int index, TokenBase item) {
		throw new UnsupportedOperationException(LIST_ERR_MSG);
	}

	/**
	 * Computes the number of divisions in {@code this}
	 * @return The length of {@code this}
	 * @throws UnsupportedOperationException if the class does not support list operations
	 */
	public int size() {
		throw new UnsupportedOperationException(LIST_ERR_MSG);
	}

	/**
	 * Appends {@code item} to the end of the list of tokens
	 * @param item The token to append
	 * @throws UnsupportedOperationException if the class does not support list operations
	 */
	public void append(TokenBase item) {
		throw new UnsupportedOperationException(LIST_ERR_MSG);
	}

	/**
	 * Gets the last token in {@code this}, including nested containers
	 * @throws UnsupportedOperationException if the class does not support list operations
	 */
	public TokenBase getLastToken() {
		throw new UnsupportedOperationException(LIST_ERR_MSG);
	}
	
	/**
	 * Sets the last token in {@code this}, including nested containers
	 * @param item The token to set the last token to
	 * @throws UnsupportedOperationException if the class does not support list operations
	 */
	public void setLastToken(TokenBase item) {
		throw new UnsupportedOperationException(LIST_ERR_MSG);
	}

	/**
	 * Creates a new blank alternation group and appends it to {@code this}
	 * @throws UnsupportedOperationException if {@code this} is not an instance of {@code Alternation}
	 */
	public void newAlt() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Replaces the contents of {@code this} with {@code newTokens}
	 * @param newTokens the tokens to replace the contents of {@code this} with
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public void replace(TokenBase newTokens) {
		throw new UnsupportedOperationException();
	}
	

	/**
	 * Returns an indicator of what type of list this token is. The values are
	 *   * 0: Not a list
	 *   * 1: Alternation - elements of the list are meant to be interpreted seperately.
	 *   * 2: TokenList - elements are meant to be interpreted together
	 * @return the specified value.
	 */
	public int listType() {
		return 0;
	}
}
