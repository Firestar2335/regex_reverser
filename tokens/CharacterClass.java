package tokens;

/**
 * A class for Character Classes in regex. These are sequences of characters delimited by "[" and 
 * "]".
 */
public class CharacterClass extends Token {
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
}
