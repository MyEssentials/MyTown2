package mytown.core;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic log wrapper class
 * @author Joe Goett
 */
public class Log {
	protected Logger logger;
	
	public Log(String name){
		this(name, null);
	}
	
	public Log(String name, Logger parent){
		logger = Logger.getLogger(name);
	}
	
	public void log(Level level, String msg, Throwable t, Object... args) {
		logger.log(level, String.format(msg, args), t);
	}

	public void log(Level level, String msg, Object... args) {
		log(level, msg, null, args);
	}

	public void fine(String msg, Object... args) {
		log(Level.FINE, msg, args);
	}

	public void finer(String msg, Object... args) {
		log(Level.FINER, msg, args);
	}

	public void finest(String msg, Object... args) {
		log(Level.FINEST, msg, args);
	}

	public void info(String msg, Object... args) {
		log(Level.INFO, msg, args);
	}

	public void severe(String msg, Object... args) {
		log(Level.SEVERE, msg, args);
	}

	public void severe(String msg, Throwable t, Object... args) {
		log(Level.SEVERE, msg, t, args);
	}

	public void warning(String msg, Object... args) {
		log(Level.WARNING, msg, args);
	}

	public void warning(String msg, Throwable t, Object... args) {
		log(Level.WARNING, msg, t, args);
	}

	public void debug(String msg, Object... args) {
		log(Level.ALL, msg, args);
	}

	public void debug(String msg, Throwable t, Object... args) {
		log(Level.ALL, msg, t, args);
	}
	
	public Logger getLogger(){
		return logger;
	}
}