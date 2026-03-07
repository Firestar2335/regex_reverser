package tokens;

/**
 * An immutable token
 */
public class Token extends TokenBase {
	protected final String token;

	public Token(String token) {
		this.token = token;
	}

	public Token reverse() {
		return this;
	}

	public String compile() {
		return this.token;
	}
}
