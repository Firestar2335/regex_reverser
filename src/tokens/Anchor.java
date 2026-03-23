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
		switch (this.token) {
			case "^": return new Anchor("$");
			case "$": return new Anchor("^");
			case "\\A": return new Anchor("\\Z");
			case "\\Z": return new Anchor("\\A");
			case "\\`": return new Anchor("\\'");
			case "\\'": return new Anchor("\\`");
			case "\\m": return new Anchor("\\M");
			case "\\M": return new Anchor("\\m");
			case "\\<": return new Anchor("\\>");
			case "\\>": return new Anchor("\\<");
			case "[[:<:]]": return new Anchor("[[:>:]]");
			case "[[:>:]]": return new Anchor("[[:<:]]");
			default: return this;
		}
	}

	public String toString() {
		if (DEBUG) {
			return "Anchor(" + super.compile() + ")";
		}
		return super.toString();
	}
}
