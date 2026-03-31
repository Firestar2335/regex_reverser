package tokens;
public class Quantifier extends TokenBase {
	private TokenBase token;
	private String quant;

	public Quantifier(TokenBase token, String quantifier) {
		this.token = token;
		quant = quantifier;
	}

	public Quantifier reverse() {
		return new Quantifier(token.reverse(), quant);
	}

	public Quantifier reverseMulti() {
		return new Quantifier(token.reverseMulti(), quant);
	}

	public String compile() {
		return token.compile() + quant;
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
		token.replace(newTokens);
	}

	/**
	 * Returns a copy of old with the specified quantifier attached. If old is already a quantifier,
	 * a copy is returned with the extra quantifier appended
	 * @param old The token to add the quantifier to
	 * @param extra The quantifier to attach to the token
	 * @return The token with {@code extra} appended
	 */
	public static Quantifier addQuantifier(TokenBase old, String extra) {
		if (old instanceof Quantifier) {
			Quantifier q = (Quantifier) old;
			return new Quantifier(q.token, q.quant + extra);
		}
		return new Quantifier(old, extra);
	}

	public String toString() {
		if (DEBUG) {
			return "Quantifier(token = "+token.toString()+", quant = "+quant.toString()+")";
		}
		return super.toString();
	}
}
