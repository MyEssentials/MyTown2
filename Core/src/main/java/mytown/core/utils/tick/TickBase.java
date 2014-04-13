package mytown.core.utils.tick;

public abstract class TickBase {
	public int getWaitTimeTicks() {
		return 20;
	}

	public abstract void run() throws Exception;

	public abstract String name();

	public boolean enabled() {
		return true;
	}
}