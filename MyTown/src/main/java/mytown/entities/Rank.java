package mytown.entities;

import mytown.Constants;


public class Rank {

	private String name;
	private String[] permissions;
	
	public Rank(String name, String[] permissions)
	{
		this.name = name;
		this.permissions = permissions;
	}
	
	public boolean parse(String rank)
	{
		if(rank == this.name)
			return true;
		return false;
	}
}


