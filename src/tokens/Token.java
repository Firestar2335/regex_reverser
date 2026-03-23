package tokens;

//import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * An immutable token
 */
public class Token extends TokenBase {
	private static ConcurrentMap<String,Token> instances = new ConcurrentHashMap<>();
	private static final Remapper remap = new Remapper();

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

	/**
	 * Returns a {@code Token} instance representing the provided token, potentially reusing instances.
	 * @param token The token to get an instance for
	 * @return A {@code Token} object corresponding to the provided token
	 */
	public static Token create(String token) {
		return instances.computeIfAbsent(token, remap);
	}

	private static class Remapper implements Function<String, Token> {
		public Token apply(String key) {
			return new Token(key);
		}
	}
}
