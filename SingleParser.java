import tokens.*;

public class SingleParser implements Parser {
	/**
	 * Parses the regex stored in the string into {@code TokenBase} objects. The result is either a
	 * {@code TokenList} object or a {@code Alternation} object.
	 * @param str The string to parse
	 * @return the tokens in {@code str}
	 */
	public TokenBase parse(String str) {
		TokenBase tokens = new TokenList();
		int i = 0;
		while (i < str.length()) {
			switch (str.charAt(i)) {
				case '\\':
					tokens.append(new Token(str.substring(i, i+2)));
					i++;
					break;
				case '(':
					int j = Parser.findMatch(str,i,str.length(),'(',')',true);
					tokens.append(new TokenGroup(parse(str.substring(i+1,j))));
					i = j;
					break;
				case '|':
					if (tokens instanceof Alternation) {
						tokens.newAlt();
					}
					else {
						tokens = Alternation.fromTokens(tokens);
					}
					break;
				case '[':
					j = Parser.findMatch(str, i, str.length(), '[',']', true);
					tokens.append(new CharacterClass(str.substring(i+1,j)));
					i = j;
					break;
				case '{':
					j = Parser.findMatch(str, i, str.length(), '{','}',true);
					tokens.setLastToken(new Quantifier(tokens.getLastToken(), str.substring(i,j+1)));
					i = j;
					break;
				case '?':
				case '*':
				case '+':
					tokens.setLastToken(new Quantifier(tokens.getLastToken(), str.substring(i,i+1)));
					break;
				case '$':
				case '^':
					tokens.append(new Anchor(str.substring(i,i+1)));
					break;
				default:
					tokens.append(new Token(str.substring(i,i+1)));
					break;
			}
			i++;
		}
		return tokens;
	}
}
