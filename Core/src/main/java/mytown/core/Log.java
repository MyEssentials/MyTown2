package mytown.core;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic log wrapper class
 * @author Joe Goett
 */
public class Log {
	/**
	 * Underlying Logger object
	 */
	protected Logger logger;
	
	/**
	 * Creates a logger with the given name and parent
	 * @param name
	 * @param parent
	 */
	public Log(String name, Logger parent){
		this(name);
		logger.setParent(parent);
	}
	
	/**
	 * Creates a logger with the given name
	 * @param name
	 */
	public Log(String name){
		logger = Logger.getLogger(name);
	}
	
	/**
	 * Writes a log msg with the given Logging level, arguments, and throwable
	 * @param level
	 * @param msg
	 * @param t
	 * @param args
	 */
	public void log(Level level, String msg, Throwable t, Object... args) {
		logger.log(level, String.format("[%s] ", logger.getName()) + String.format(msg, args), t);
	}

	/**
	 * Writes the given message to the logger with the given level
	 * @param level
	 * @param msg
	 * @param args
	 */
	public void log(Level level, String msg, Object... args) {
		log(level, msg, null, args);
	}

	/**
	 * Logs a Level.FINE
	 * @param msg
	 * @param args
	 */
	public void fine(String msg, Object... args) {
		log(Level.FINE, msg, args);
	}

	/**
	 * Logs a Level.FINER
	 * @param msg
	 * @param args
	 */
	public void finer(String msg, Object... args) {
		log(Level.FINER, msg, args);
	}

	/**
	 * Logs a Level.FINEST
	 * @param msg
	 * @param args
	 */
	public void finest(String msg, Object... args) {
		log(Level.FINEST, msg, args);
	}

	/**
	 * Logs a Level.INFO
	 * @param msg
	 * @param args
	 */
	public void info(String msg, Object... args) {
		log(Level.INFO, msg, args);
	}

	/**
	 * Logs a Level.SEVERE
	 * @param msg
	 * @param args
	 */
	public void severe(String msg, Object... args) {
		log(Level.SEVERE, msg, args);
	}

	/**
	 * Logs a Level.SEVERE including the throwable in the log
	 * @param msg
	 * @param t
	 * @param args
	 */
	public void severe(String msg, Throwable t, Object... args) {
		log(Level.SEVERE, msg, t, args);
	}

	/**
	 * Logs a Level.WARNING
	 * @param msg
	 * @param args
	 */
	public void warning(String msg, Object... args) {
		log(Level.WARNING, msg, args);
	}

	/**
	 * Logs a Level.WARNING including a throwable in the log
	 * @param msg
	 * @param t
	 * @param args
	 */
	public void warning(String msg, Throwable t, Object... args) {
		log(Level.WARNING, msg, t, args);
	}

	/**
	 * Logs a Level.CONFIG
	 * @param msg
	 * @param args
	 */
	public void config(String msg, Object... args){
		log(Level.CONFIG, msg, args);
	}
	
	/**
	 * Logs a Level.CONFIG including a throwable in the log
	 * @param msg
	 * @param t
	 * @param args
	 */
	public void config(String msg, Throwable t, Object... args){
		log(Level.CONFIG, msg, t, args);
	}
	
	/**
	 * Returns the underlying logger
	 * @return
	 */
	public Logger getLogger(){
		return logger;
	}
}