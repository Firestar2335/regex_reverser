package tokens;

public class Backreference extends TokenBase {
	private String group;
	private String prefix;
	private String suffix;

	public Backreference(String groupName) {
		this(groupName, "\\","");
	}

	public Backreference(String groupName, String prefix, String suffix) {
		this.group = groupName;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public Backreference reverse() {
		return this;
	}

	public String compile() {
		return prefix + group + suffix;
	}
}
