package tokens;
import java.util.regex.*;

/** A parenthesized group of tokens, potentially a lookahead/behind */
public class TokenGroup extends TokenBase {
	private static final Pattern MODE_MODIFIER = Pattern.compile("\\?[a-eil-np-uwxJLUX^]*(-[a-eil-np-uwxJLUX^]+)?:?");

	/** The token within this group */
	private TokenBase token;
	/** The escape/modifier sequence at the beginning of the group */
	private String mod;

	public TokenGroup() {
		this("", null);
	}

	public TokenGroup(TokenBase token) {
		this("", token);
	}

	public TokenGroup(String modifier) {
		this(modifier, null);
	}

	public TokenGroup(String modifier, TokenBase token) {
		mod = modifier;
		this.token = token;
	}

	public TokenGroup reverse() {
		String newMod;
		switch (mod) {
			case "?="://Positive lookahead -> positive lookbehind
				newMod = "?<=";
				break;
			case "*pla:":
				newMod = "*plb:";
				break;
			case "*positive_lookahead:":
				newMod = "*positive_lookbehind:";
				break;
			case "?!"://Negative lookahead -> negative lookbehind
				newMod = "?<!";
				break;
			case "*nla:":
				newMod = "*nlb:";
				break;
			case "*negative_lookahead:":
				newMod = "*negative_lookbehind:";
				break;
			case "?<="://Positive lookbehind -> positive lookahead
				newMod = "?=";
				break;
			case "*plb:":
				newMod = "*pla:";
				break;
			case "*positive_lookbehind:":
				newMod = "*positive_lookahead:";
				break;
			case "?<!"://Negative lookbehind -> negative lookahead
				newMod = "?!";
				break;
			case "*nlb:":
				newMod = "*nla:";
				break;
			case "*negative_lookbehind:":
				newMod = "*negative_lookahead";
				break;
			default:
				newMod = mod;
				break;
		}
		return new TokenGroup(newMod, token.reverse());
	}

	public String compile() {
		return "(" + mod + token.compile() + ")";
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
		token = newTokens;
	}

	public String toString() {
		if (DEBUG) {
			if (token == null) {
				return "Group()";
			}
			else {
				return "Group("+token.toString()+")";
			}
		}
		return super.toString();
	}

	public GroupType groupType() {
		switch (mod) {
			case "?=":
			case "*pla:":
			case "*positive_lookahead:":
				return GroupType.POSITIVE_LOOKAHEAD;
			case "?!":
			case "*nla:":
			case "*negative_lookahead:":
				return GroupType.NEGATIVE_LOOKAHEAD;
			case "?<=":
			case "*plb:":
			case "*positive_lookbehind:":
				return GroupType.POSITIVE_LOOKBEHIND;
			case "?<!":
			case "*nlb:":
			case "*negative_lookbehind:":
				return GroupType.NEGATIVE_LOOKBEHIND;
			case "?|":
				return GroupType.BRANCH_RESET;
			case "?>":
			case "*atomic:":
				return GroupType.ATOMIC;
			case "?#":
				return GroupType.ATOMIC;
			case "?:":
				return GroupType.NON_CAPTURING;
			case "":
				return GroupType.CAPTURING;
			default:
				if (MODE_MODIFIER.matcher(mod).matches()) {
					return GroupType.MODE_MODIFIER;
				}
				return GroupType.CAPTURING;
		}
	}

	public TokenGroup withFirst(TokenBase newFirst) {
		return new TokenGroup(mod,token.withFirst(newFirst));
	}
}
