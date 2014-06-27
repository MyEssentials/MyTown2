package mytown.chat.filter;

import mytown.chat.api.ActionType;

public class Action {
	private ActionType type;
	private String[] args; // TODO Change to a List instead?

	public Action(ActionType type, String[] args) {
		this.type = type;
		this.args = args;
	}

	public void run() {
		type.run(args);
	}
}