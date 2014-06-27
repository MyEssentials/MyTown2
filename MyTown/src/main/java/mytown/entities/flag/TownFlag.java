package mytown.entities.flag;

import net.minecraft.util.EnumChatFormatting;
import mytown.interfaces.ITownFlag;

public class TownFlag implements ITownFlag {

	protected int ID = -1;
	protected String key;
	protected String flagName;
	protected String localizedDescription;

	protected boolean value;

	public TownFlag(String flagName, String localizedDescription, boolean defaultValue) {
		this.flagName = flagName;
		this.localizedDescription = localizedDescription;
		value = defaultValue;
	}

	@Override
	public String getName() {
		return flagName;
	}

	@Override
	public String getLocalizedDescription() {
		return localizedDescription;
	}

	@Override
	public boolean getValue() {
		return value;
	}

	@Override
	public boolean setValue(boolean value) {
		this.value = value;
		// might want to check if value is valid
		return true;
	}

	@Override
	public int getDB_ID() {
		return ID;
	}

	@Override
	public void setDB_ID(int id) {
		ID = id;
	}

	@Override
	public String toString() {
		return String.format(EnumChatFormatting.GRAY + "%s" + EnumChatFormatting.WHITE +"[" + EnumChatFormatting.GREEN + "%s" + EnumChatFormatting.WHITE + "]:" + EnumChatFormatting.GRAY + " %s", flagName, value, localizedDescription);
	}
}