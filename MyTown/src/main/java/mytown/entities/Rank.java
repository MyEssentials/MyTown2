package mytown.entities;



public class Rank {

	private String name;
	private String[] permissions;
	
	public Rank(String name, String[] permissions)
	{
		this.name = name;
		this.permissions = permissions;
	}
	
	/**
	 * Checks if the string given is the name of the rank
	 * 
	 * @param rank
	 * @return
	 */
	public boolean parse(String rank)
	{
		if(rank == this.name)
			return true;
		return false;
	}
}


