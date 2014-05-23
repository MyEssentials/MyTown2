package mytown.entities.flag;

import mytown.interfaces.ITownFlag;

public class TownFlag implements ITownFlag {

	protected String flagName;
	protected String localizedDescription;
	
	protected EnumFlagValue value;
	
	public TownFlag(String flagName, String localizedDescription, EnumFlagValue defaultValue) {
		this.flagName = flagName;
		this.localizedDescription = localizedDescription;
		this.value = defaultValue;
	}
	
	@Override
	public String getName() {
		return this.flagName;
	}
	
	public String getLocalizedDescription() {
		return this.localizedDescription;
	}
	
	@Override
	public EnumFlagValue getValue() {
		return this.value;
	}
	
	
}
