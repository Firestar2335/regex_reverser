import tokens.*;

/**
 * A single threaded parser of regex
 */
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
					String n = str.substring(i, i+2);
					if (TokenBase.ANCHORS.contains(n)) {
						tokens.append(new Anchor(n));
					}
					else if ((n.charAt(1)=='b' || n.charAt(1)=='B') && i+6 <= str.length() && "{wb}".equals(str.substring(i+2,i+6))) {
						tokens.append(new Anchor(n + "{wb}"));
						i += 4;
					}
					else if (n.charAt(1)=='k' || n.charAt(1) == 'g') {// \k<1>, \k'1', \g1, \g{1}, \g<1>, \g'1'
						int j;
						switch (str.charAt(i+2)) {
							case '{':
								j = Parser.findMatch(str, i+2, str.length(), '{','}',true);
								break;
							case '<':
								j = Parser.findMatch(str, i+2, str.length(), '<','>',true);
								break;
							case '\'':
								j = Parser.findEnclosing(str, i+2, str.length(), '\'',true);
								break;
							default:
								throw new IllegalArgumentException();
						}
						tokens.append(new Backreference(str.substring(i+3,j),str.substring(i,i+3),str.substring(j,j+1)));
						i=j;
					}
					else {
						tokens.append(Token.create(n));
					}
					i++;
					break;
				case '(':
					int j = Parser.findMatch(str,i,str.length(),'(',')',true);
					String mod = "";
					if (str.charAt(i+1) == '?') {//Special
						if (str.charAt(i+2) == '<' && (str.charAt(i+3) == '=' || str.charAt(i+3) == '!')) {
							mod = str.substring(i+1,i+4);
						}
						else if (str.charAt(i+2) == '<') {
							int k  = Parser.findMatch(str, i+2, j, '<','>', false);
							mod = str.substring(i+1, k+1);
						}
						else if (str.charAt(i+2) == '\'') {
							int k  = Parser.findMatch(str, i+2, j, '\'','\'', false);
							mod = str.substring(i+1, k+1);
						}
						else if (str.substring(i+2,i+4).equals("P=")) {
							tokens.append(new Backreference(str.substring(i+4,j),"(?P=",")"));
							i = j;
							break;
						}
						else if (str.substring(i+2,i+4).equals("P<")) {
							int k = Parser.findMatch(str, i+3,j,'<','>',false);
							mod = str.substring(i+1,k+1);
						}
					}
					else if (str.charAt(i+1) == '*') {
						int k = Parser.findMatch(str, i+1, j, '*', ':', false);
						mod = str.substring(i+1, k+1);
					}
					tokens.append(new TokenGroup(mod,parse(str.substring(i+1+mod.length(),j))));
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
					tokens.append(Token.create(str.substring(i,i+1)));
					break;
			}
			i++;
		}
		return tokens;
	}
}
