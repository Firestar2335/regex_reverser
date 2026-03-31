package tokens;
import java.util.*;

public class Alternation extends TokenBase {
	private List<TokenBase> alts;

	public Alternation(List<TokenBase> alternatives) {
		alts = alternatives;
	}

	public Alternation reverse() {
		List<TokenBase> res = new ArrayList<>();
		for (TokenBase alt : alts) {
			res.add(alt.reverse());
		}
		return new Alternation(res);
	}

	public String compile() {
		String result = alts.getFirst().compile();
		for (int i = 1; i < alts.size(); i++) {
			result += "|" + alts.get(i).compile();
		}
		return result;
	}

	public TokenBase get(int index) {
		return alts.get(index);
	}

	public void set(int index, TokenBase item) {
		alts.set(index,item);
	}

	public int size() {
		return alts.size();
	}

	public TokenBase getLastToken() {
		TokenBase t = get(size()-1);
		if (t.listType() == ListType.NOTLIST) {
			return t;
		}
		return t.getLastToken();
	}

	public void setLastToken(TokenBase item) {
		TokenBase t = get(size()-1);
		if (t.listType() == ListType.NOTLIST) {
			set(size()-1, item);
		}
		else {
			t.setLastToken(item);
		}
	}

	public void newAlt() {
		alts.add(new TokenList());
	}

	public void append(TokenBase item) {
		TokenBase t = alts.getLast();
		if (t.listType() == ListType.NOTLIST) {
			List<TokenBase> r = new ArrayList<>();
			r.add(t);
			r.add(item);
			alts.set(alts.size()-1, new TokenList(r));
		}
		else {
			t.append(item);
		}
	}

	//public int listType() {
	//	return 1;
	//}

	public ListType listType() {
		return ListType.ALTERNATION;
	}

	/**
	 * Creates an Alternation instance from a pre-existing token. {@code old} is placed at the 
	 * beginning of the alternation group and a blank group is added to the end
	 * @param old The token to create the alternation group from.
	 * @return An alternation group containing {@code old} followed by an empty group.
	 */
	public static Alternation fromTokens(TokenBase old) {
		List<TokenBase> res = new ArrayList<>();
		res.add(old);
		res.add(new TokenList());
		return new Alternation(res);
	}

	public String toString() {
		if (DEBUG) {
			return "Alternation(" + alts.toString() + ")";
		}
		return super.toString();
	}

	public Alternation reverseMulti() {
		if (alts.size() < BATCH_SIZE) {
			return reverse();
		}
		return new Alternation(new ReverseWorker(alts.spliterator()).invoke().reversed());
	}
}