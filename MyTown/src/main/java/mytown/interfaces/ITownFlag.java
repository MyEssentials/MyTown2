package mytown.interfaces;

import mytown.entities.flag.EnumFlagValue;

public interface ITownFlag {
	
	String getName();
	String getLocalizedDescription();
	
	EnumFlagValue getValue();
}