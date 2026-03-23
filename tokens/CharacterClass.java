package tokens;
import java.util.concurrent.*;

/**
 * A class for Character Classes in regex. These are sequences of characters delimited by "[" and 
 * "]".
 */
public class CharacterClass extends Token {
	private static ConcurrentMap<String, CharacterClass> instances = new ConcurrentHashMap<>();

	
	public CharacterClass(String characters) {
		super(characters);
	}

	@Override
	public String compile() {
		return "[" + super.compile() + "]";
	}

	public String toString() {
		if (DEBUG) {
			return "Class(" + super.compile() + ")";
		}
		return super.toString();
	}

	/**
	 * Returns an instance corresponding to the provided characters, potentially reusing instances
	 * @param characters The characters to get the class for
	 * @return A {@code CharacterClass} instance corresponding to the specified characters.
	 */
	public static CharacterClass create(String characters) {
		return instances.computeIfAbsent(characters, k -> new CharacterClass(k));
	}
}
