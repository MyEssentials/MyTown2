package mytown.core.utils.tick;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mytown.core.Log;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler {
	private String label = "TickHandler!";
	private List<TickBase> handlers;
	private long tick = 0;
	private Log log;
	
	public TickHandler(String label, Log log) {
		handlers = new ArrayList<TickBase>();
		this.label = label;
		this.log = log;
	}
	
	public TickHandler(String label) {
		this(label, new Log(label));
	}
	
	public TickHandler(Log log) {
		this(log.getLogger().getName(), log);
	}
	
	public TickHandler() {
		this("TickHandler");
	}
	
	public void addTickHandler(TickBase base) {
		synchronized(handlers) {
			handlers.add(base);
		}
	}
	
	public void removeTickHandler(TickBase base) {
		synchronized(handlers) {
			handlers.remove(base);
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (tick == Long.MAX_VALUE) {
			tick = 0;
		}

		tick++;

		synchronized(handlers) {
			for (TickBase t : handlers) {
				if (!t.enabled()) {
					continue;
				}
	
				if (tick % t.getWaitTimeTicks() != 0) {
					continue;
				}
	
				try {
					t.run();
				} catch (Throwable e) {
					log.severe("Tick handler %s failed to run.", e, t.name());
				}
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel() {
		return label;
	}
}