package mytown.entities.flag;

import mytown.interfaces.ITownFlag;

public class TownFlag implements ITownFlag {

	protected String flagName;
	protected String localizedDescription;
	
	protected boolean value;
	
	public TownFlag(String flagName, String localizedDescription, boolean defaultValue) {
		this.flagName = flagName;
		this.localizedDescription = localizedDescription;
		this.value = defaultValue;
	}
	
	@Override
	public String getName() {
		return this.flagName;
	}
	
	@Override
	public String getLocalizedDescription() {
		return this.localizedDescription;
	}
	
	@Override
	public boolean getValue() {
		return this.value;
	}
	
	@Override
	public boolean setValue(boolean value) {
		this.value = value;
		// might want to check if value is valid
		return true;
	}
}
