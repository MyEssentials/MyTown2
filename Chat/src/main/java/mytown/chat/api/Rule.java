package mytown.chat.api;

import java.util.regex.Pattern;

public abstract class Rule {
	private final String name;
	private final String match;
	private final Pattern pattern;
	
	public Rule(String name, String match) {
		this.name = name;
		this.match = match;
		pattern = Pattern.compile(match);
	}
	
	public boolean matches(String msg) {
		return pattern.matcher(msg).matches();
	}
	
	/**
	 * Runs the Rule. If part of a RuleChain, returns false to not run the rules after this
	 * @return
	 */
	public abstract boolean run();
	
	public final String getName() {
		return name;
	}
	
	public final String getPatternString() {
		return match;
	}
	
	public final Pattern getPattern() {
		return pattern;
	}
}