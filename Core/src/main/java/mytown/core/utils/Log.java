package mytown.core.utils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		this.name = logger.getName();
	}
	
	/**
	 * Returns the underlying Logger object, or null if it has none
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}
	
	/**
	 * Returns the name of the Logger
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the parent of this Log, or null if it has no parent
	 * @return
	 */
	public Log getParent() {
		return parent;
	}
	
	/**
	 * Sets this Log's name. Only useful if it has a parent
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
	public void log(Level level, String msg, Object...args) {
		if (parent != null) {
			parent.log(level, "[" + getName() + "]" + msg, args);  // Find a better way to pass the name to the parent?
			return;
		}
		
		Throwable t = null;
		if (args.length > 0 && args[0] instanceof Throwable) {
			t = (Throwable)args[0];
			args = Arrays.copyOfRange(args, 1, args.length-1); // TODO Will this actually work?!?
		}
		
		msg = String.format(msg, args);
		
		if (t != null) {
			logger.log(level, msg, t);
		} else {
			logger.log(level, msg);
		}
	}
	
	public void finest(String msg, Object...args) {
		log(Level.FINEST, msg, args);
	}
	
	public void finer(String msg, Object...args) {
		log(Level.FINER, msg, args);
	}
	
	public void fine(String msg, Object...args) {
		log(Level.FINE, msg, args);
	}
	
	public void config(String msg, Object...args) {
		log(Level.CONFIG, msg, args);
	}
	
	public void info(String msg, Object...args) {
		log(Level.INFO, msg, args);
	}
	
	public void warning(String msg, Object...args) {
		log(Level.WARNING, msg, args);
	}
	
	public void severe(String msg, Object...args) {
		log(Level.SEVERE, msg, args);
	}

	/**
	 * Helper method to create a child of this logger
	 * @param name
	 * @return
	 */
	public Log createChild(String name) {
		return new Log(name, this);
	}
}