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
	private List<Action> actions;

	public ActionRule(String name, String match) {
		super(name, match);
		actions = new ArrayList<Action>();
	}
	
	public List<Action> getActions() {
		return actions;
	}
	
	public void addAction(Action action) {
		actions.add(action);
	}
	
	public void removeAction(Action action) {
		actions.remove(action);
	}

	@Override
	public boolean run() {
		for (Action a : actions) {
			a.run();
		}
		return true;
	}
}