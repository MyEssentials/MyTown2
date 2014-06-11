package mytown.chat.filter;

import java.util.ArrayList;
import java.util.List;

import mytown.chat.api.Rule;

/**
 * Defines {@link Action}'s to loop through when a match is made
 * 
 * @author Joe Goett
 */
public class ActionRule extends Rule {
	public List<Action> actions;
	
	public ActionRule(String name, String match) {
		super(name, match);
		actions = new ArrayList<Action>();
	}

	@Override
	public boolean run() {
		for (Action a : actions) {
			a.run();
		}
		return true;
	}
}