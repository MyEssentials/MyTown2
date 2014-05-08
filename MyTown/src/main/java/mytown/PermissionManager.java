package mytown;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.core.utils.command.CommandUtils;
import forgeperms.api.IPermissionManager;

public class PermissionManager implements IPermissionManager {

	private String name;

	private Map<String, List<String>> players; // username / groups
	private Map<String, List<String>> groups; // group / nodes

	public PermissionManager(String name) {
		this.name = name;
		players = new Hashtable<String, List<String>>();
		groups = new Hashtable<String, List<String>>();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean load() {

		// Testing
		/*
		List<String> permsToBeAdded = new ArrayList<String>();
		for(String s : CommandUtils.permissionList.values())
		{
			permsToBeAdded.add(s);
			System.out.println(s);
		}
		groups.put("Troll", permsToBeAdded);
		List<String> groupsToBeAdded = new ArrayList<String>();
		groupsToBeAdded.add("Troll");
		players.put("DerBanhammer", groupsToBeAdded);
		*/
		// #addAllThePerms
		
		List<String> pOutsider = new ArrayList<String>();
		List<String> pResident = new ArrayList<String>();
		List<String> pAssistant = new ArrayList<String>();
		List<String> pMayor = new ArrayList<String>();
		
		for(String s : CommandUtils.permissionList.values())
		{
			if(s.startsWith("mytown.cmd"))
			{
				pMayor.add(s);
				if(s.startsWith("mytown.cmd.assistant") || s.startsWith("mytown.cmd.resident") || s.startsWith("mytown.cmd.outsider"))
					pAssistant.add(s);
				if(s.startsWith("mytown.cmd.resident") || s.startsWith("mytown.cmd.outsider"))
					pResident.add(s);
				if(s.startsWith("mytown.cmd.outsider"))
					pOutsider.add(s);
			}
		}
		
		Constants.DEFAULT_RANK_VALUES.put("Outsider", pOutsider);
		Constants.DEFAULT_RANK_VALUES.put("Resident", pResident);
		Constants.DEFAULT_RANK_VALUES.put("Assistant", pAssistant);
		Constants.DEFAULT_RANK_VALUES.put("Mayor", pMayor);
		
		
		
		return false;
	}

	@Override
	public String getLoadError() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canAccess(String player, String world, String node) {

		// For now ignoring per world permissions
		List<String> groups2 = players.get(player);
		if (groups2 == null) 
			return false;
		for (String group : groups2) {
			List<String> perms = groups.get(group);
			if (perms == null) continue;
			if (perms.contains(node)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean addGroup(String player, String group) {
		List<String> groups = players.get(player);
		if (groups == null) groups = new ArrayList<String>();

		if (groups.add(group)) return true;
		return false;
	}

	@Override
	public boolean removeGroup(String player, String group) {
		if (players.containsKey(player)) {
			if (players.get(player).remove(group))
				return true;
			else
				return false;
		}
		return false;
		// TODO Add permission exceptions... maybe
	}

	@Override
	public String[] getGroupNames(String player) {
		if (groups.values().size() != 0) {
			String[] names = (String[]) groups.get(player).toArray();
			return names;
		}
		return null;
	}

	@Override
	public String getPrimaryGroup(String world, String playerName) {
		List<String> perms = players.get(playerName);
		if (perms != null)
			return perms.get(0);
		else
			return null;
	}
}
