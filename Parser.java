import tokens.*;

interface Parser {
	public TokenBase parse(String str);

	/**
	 * Finds the index of the parentheses that matches the parentheses at {@code start}. 
	 * @param str The string to search in
	 * @param start The index containing the opening parentheses.
	 * @param stop The index to stop, exclusive.
	 * @param open The opening delimiter to use
	 * @param close The closing delimiter to use
	 * @param escape Whether to interpret backslashes as escaping a character
	 * @return An index containing {@code close} that matches the delimiter at {@code start}
	 * @throws IllegalArgumentException if {@code str} contains an unmatched delimiter within the 
	 * pair starting at {@code start}
	 */
	public static int findMatch(String str, int start, int stop, char open, char close, boolean escape) {
		int count = 0;
		int i = start;
		while (i < stop) {
			if (escape && str.charAt(i) == '\\') {
				i += 2;
				continue;
			}
			else if (str.charAt(i) == open) {
				count++;
			}
			else if (str.charAt(i) == close) {
				count--;
			}
			if (count < 0) {
				throw new IllegalArgumentException("Unmatched closing delimiter at " + i);
			}
			else if (count == 0) {
				break;
			}
			i++;
		}
		if (count != 0) {
			throw new IllegalArgumentException("Unmatched opening delimiter at " + start);
		}
		return i;
	}
}