package mytown.interfaces;


public interface ITownFlag {
	
	String getName();
	String getLocalizedDescription();
	
	int getDB_ID();
	void setDB_ID(int id);
	
	boolean getValue();
	boolean setValue(boolean value);
}