package mytown.interfaces;


public interface ITownFlag {
	
	String getName();
	String getLocalizedDescription();
	
	boolean getValue();
	boolean setValue(boolean value);
}