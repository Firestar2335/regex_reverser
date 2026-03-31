package tokens;
import java.util.*;

/** A lookahead conditional, of the form (?(~~~~)then|else) */
public class Conditional extends TokenBase {//Probably could find a way to subclass TokenGroup
	private static final String DEBUG_FORMAT = "Conditional(condition=%s, then=%s, else=%s)";

	//Can be:
	//     - lookahead: (?(?=regex)then|else)
	//     - implicit lookahead: (?(regex)then|else) if regex is not the name of a capturing group
	//     - named:
	//         - (?(name)then|else)
	//         - (?(<name>)then|else)
	//         - (?('name')then|else)
	//         - (?(1)then|else)
	//     - relative:
	//         - (?(-1)then|else)
	//         - (?(+1)then|else)
	/** The condition for the conditional */
	private TokenGroup condition;
	/** The 'then' case */
	private TokenBase thenToken;
	/** The 'else' case */
	private TokenBase elseToken;

	public Conditional() {
		this(null,null,null);
	}

	/* *
	 * Constructs the 
	 * @param contents
	 */
	//public Conditional(TokenBase contents) {
	//	
	//}

	public Conditional(TokenGroup condition, TokenBase thenToken, TokenBase elseToken) {
		this.condition = condition;
		this.thenToken = thenToken;
		this.elseToken = elseToken;
	}

	public String compile() {
		return "(?" + condition.compile() + thenToken.compile() + "|" + elseToken.compile() + ")";
	}

	public Conditional reverse() {
		TokenGroup newCond;
		switch (condition.groupType()) {
			case POSITIVE_LOOKAHEAD:
			case POSITIVE_LOOKBEHIND:
			case NEGATIVE_LOOKAHEAD:
			case NEGATIVE_LOOKBEHIND:
				newCond = condition.reverse();
				break;
			default:
				newCond = negateNumber(condition);
		}
		return new Conditional(newCond, thenToken.reverse(), elseToken.reverse());
	}

	public Conditional reverseMulti() {
		TokenGroup newCond;
		switch (condition.groupType()) {
			case POSITIVE_LOOKAHEAD:
			case POSITIVE_LOOKBEHIND:
			case NEGATIVE_LOOKAHEAD:
			case NEGATIVE_LOOKBEHIND:
				newCond = condition.reverse();
				break;
			default:
				newCond = negateNumber(condition);
		}
		return new Conditional(newCond, thenToken.reverseMulti(), elseToken.reverseMulti());
	}

	private TokenGroup negateNumber(TokenGroup g) {
		/*String first = g.get(0).compile();
		if (first.charAt(0) == '-') {
			return g.withFirst(new Token("+"));
		}
		else if (first.charAt(0) == '+') {
			return g.withFirst(new Token("-"));
		}
		else {
			return g;
		}*/
		String group = g.compile();
		String number = group.substring(1, group.length()-1);
		switch (number.charAt(0)) {
			case '+':
				return new TokenGroup(Token.create("-" + number.substring(1)));
			case '-':
				return new TokenGroup(Token.create("+" + number.substring(1)));
			default:
				return g;
		}
	}

	public TokenBase get(int index) {
		if (index < 0 || index >= 3) {
			throw new IndexOutOfBoundsException(index);
		}
		if (index == 0) {
			return condition;
		}
		else if (index == 1) {
			return thenToken;
		}
		else {// index == 2
			return elseToken;
		}
	}

	public void set(int index, TokenBase item) {
		if (index < 0 || index >= 3) {
			throw new IndexOutOfBoundsException(index);
		}
		if (index == 0) {
			if (item instanceof TokenGroup) {
				condition = (TokenGroup) item;
			}
			else {
				condition = new TokenGroup(item);
			}
		}
		else if (index == 1) {
			thenToken = item;
		}
		else {// index == 2
			elseToken = item;
		}
	}

	public int size() {
		return 3;
	}

	public void replaceIter(TokenBase newTokens, List<Integer> indices) {
		if (indices.size() == 0) {
			throw new UnsupportedOperationException();//replace(newTokens);
		}
		int i = indices.get(0);
		if (i < 0 || i >= 3) {
			throw new IndexOutOfBoundsException(i);
		}
		if (indices.size() > 1) {
			this.get(i).replaceIter(newTokens, indices.subList(1,indices.size()));
		}
		else {
			if (i == 0) {
				condition.replace(newTokens);
			}
			else if (i == 1) {
				thenToken = newTokens;
			}
			else {
				elseToken = newTokens;
			}
		}
	}

	public GroupType groupType() {
		return GroupType.CONDITIONAL;
	}

	public ListType listType() {
		return ListType.FIXED;
	}

	public String toString() {
		if (DEBUG) {
			return String.format(DEBUG_FORMAT, condition, thenToken, elseToken);
		}
		return super.toString();
	}
}
