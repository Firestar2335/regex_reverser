package tokens;

/**
 * An anchor for start or end of lines
 */
public class Anchor extends Token {
	public Anchor(String token) {
		super(token);
	}

	@Override
	public Anchor reverse() {
		if (this.token.equals("^")) {
			return new Anchor("$");
		}
		if (this.token.equals("$")) {
			return new Anchor("^");
		}
		return this;
	}

	public String toString() {
		if (DEBUG) {
			return "Anchor(" + super.compile() + ")";
		}
		return super.toString();
	}
}
