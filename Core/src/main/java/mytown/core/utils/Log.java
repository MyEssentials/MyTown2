package mytown.core.utils;

import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class Log {
	private Logger logger = null;
	private String name = "";
	private Log parent = null;

	public Log(String name, Log parent) {
		this.name = name;
		this.parent = parent;
	}

	public Log(Logger logger) {
		this.logger = logger;
		name = logger.getName();
	}

	/**
	 * Returns the underlying Logger object, or null if it has none
	 * 
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Returns the name of the Logger
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parent of this Log, or null if it has no parent
	 * 
	 * @return
	 */
	public Log getParent() {
		return parent;
	}

	/**
	 * Sets this Log's name. Only useful if it has a parent
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Logs a message with the given level and args
	 * 
	 * if args[0] is a Throwable, it will be taken out of args and passed directly to the Logger
	 * 
	 * @param level
	 * @param msg
	 * @param args
	 */
	public void log(Level level, String msg, Object... args) {
		if (parent != null) {
			parent.log(level, "[" + getName() + "]" + msg, args); // Find a better way to pass the name to the parent?
			return;
		}

		Throwable t = null;
		if (args.length > 0 && args[0] instanceof Throwable) {
			t = (Throwable) args[0];
			args = Arrays.copyOfRange(args, 1, args.length);
		}

		msg = String.format(msg, args);

		if (t != null) {
			logger.log(level, msg, t);
		} else {
			logger.log(level, msg);
		}
	}

	public void fatal(String msg, Object... args) {
		log(Level.FATAL, msg, args);
	}
	
	public void error(String msg, Object... args) {
		log(Level.ERROR, msg, args);
	}
	
	public void warn(String msg, Object... args) {
		log(Level.WARN, msg, args);
	}
	
	public void info(String msg, Object... args) {
		log(Level.INFO, msg, args);
	}
	
	public void debug(String msg, Object... args) {
		log(Level.DEBUG, msg, args);
	}
	
	public void trace(String msg, Object... args) {
		log(Level.TRACE, msg, args);
	}

	/**
	 * Helper method to create a child of this logger
	 * 
	 * @param name
	 * @return
	 */
	public Log createChild(String name) {
		return new Log(name, this);
	}
}