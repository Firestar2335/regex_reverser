package tokens;
import java.util.*;

/**
 * A class for representing sequences of individual tokens.
 */
public class TokenList extends TokenBase {
	private List<TokenBase> tokens;

	/**
	 * Creates an empty {@code TokenList}
	 */
	public TokenList() {
		this.tokens = new ArrayList<>();
	}

	public TokenList(List<TokenBase> tokens) {
		this.tokens = tokens;
	}

	public TokenList reverse() {
		List<TokenBase> reversed = new ArrayList<>();
		for (TokenBase token : tokens.reversed()) {
			reversed.add(token.reverse());
		}
		return new TokenList(reversed);
	}

	public String compile() {
		String r = "";
		for (TokenBase token : tokens) {
			r += token.compile();
		}
		return r;
	}

	public TokenBase get(int index) {
		return tokens.get(index);
	}

	public void set(int index, TokenBase token) {
		tokens.set(index, token);
	}

	public int size() {
		return tokens.size();
	}

	public void append(TokenBase item) {
		tokens.addLast(item);
	}

	public TokenBase getLastToken() {
		TokenBase t = tokens.getLast();
		if (t.listType() == ListType.NOTLIST) {
			return t;
		}
		return t.getLastToken();
	}

	public void setLastToken(TokenBase item) {
		TokenBase t = tokens.getLast();
		if (t.listType() == ListType.NOTLIST) {
			tokens.set(size()-1, item);
		}
		else {
			t.setLastToken(item);
		}
	}

	//public int listType() {
	//	return 2;
	//}

	public ListType listType() {
		return ListType.LIST;
	}

	public String toString() {
		if (DEBUG) {
			return tokens.toString();
		}
		return super.toString();
	}

	public TokenList withFirst(TokenBase newFirst) {
		List<TokenBase> result = new ArrayList<>(tokens);
		result.set(0,newFirst);
		return new TokenList(result);
	}
}
