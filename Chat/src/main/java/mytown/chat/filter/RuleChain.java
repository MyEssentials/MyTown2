package mytown.chat.filter;

import java.util.ArrayList;
import java.util.List;

import mytown.chat.api.Rule;

/**
 * Defines a chain of {@link Rule}'s to loop through when a match is made
 * 
 * @author Joe Goett
 */
public class RuleChain extends Rule {
	private List<Rule> rules;

	public RuleChain(String name, String match) {
		super(name, match);

		rules = new ArrayList<Rule>();
	}

	@Override
	public boolean run() {
		for (Rule r : rules) {
			if (!r.run()) {
				break; // Stop the loop because rule told me to!
			}
		}
		return true;
	}
}