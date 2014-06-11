package mytown.chat.api;

public abstract class ActionType {
	public abstract String getName();
	public abstract void run(String[] args);
}