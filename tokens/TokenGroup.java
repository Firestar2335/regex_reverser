package tokens;
public class TokenGroup extends TokenBase {
	private TokenBase token;

	public TokenGroup() {
		token = null;
	}

	public TokenGroup(TokenBase token) {
		this.token = token;
	}

	public TokenGroup reverse() {
		return new TokenGroup(token.reverse());
	}

	public String compile() {
		return "(" + token.compile() + ")";
	}

	public TokenBase get(int index) {
		return token.get(index);
	}

	public void set(int index, TokenBase item) {
		token.set(index, item);
	}

	public int size() {
		return token.size();
	}

	public void replace(TokenBase newTokens) {
		token = newTokens;
	}

	public String toString() {
		if (DEBUG) {
			if (token == null) {
				return "Group()";
			}
			else {
				return "Group("+token.toString()+")";
			}
		}
		return super.toString();
	}
}
