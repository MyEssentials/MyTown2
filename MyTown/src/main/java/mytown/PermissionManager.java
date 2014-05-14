package mytown;

import forgeperms.api.IPermissionManager;

public class PermissionManager implements IPermissionManager{
	
	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean load() {
		return false;
	}

	@Override
	public String getLoadError() {
		return null;
	}

	@Override
	public boolean canAccess(String player, String world, String node) {
		return true;
	}

	@Override
	public boolean addGroup(String player, String group) {
		return false;
	}

	@Override
	public boolean removeGroup(String player, String group) {
		return false;
	}

	@Override
	public String[] getGroupNames(String player) {
		return null;
	}

	@Override
	public String getPrimaryGroup(String world, String playerName) {
		return null;
	}

}
