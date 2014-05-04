package mytown.entities.comparator;

import java.util.Comparator;

import mytown.entities.Town;

public class TownComparator implements Comparator<Town>{

	public enum Order { Name, Residents, Blocks};
	
	private Order sortingBy = Order.Name;
	
	public TownComparator(Order order)
	{
		this.sortingBy = order;
	}
	
	@Override
	public int compare(Town arg0, Town arg1) {
		switch(sortingBy)
		{
			case Name: return arg0.getName().compareTo(arg1.getName());
			case Residents: return ((Integer)arg0.getResidents().size()).compareTo((Integer)arg1.getResidents().size());
			case Blocks: return ((Integer)arg0.getExtraBlocks()).compareTo((Integer)arg1.getExtraBlocks());
		}
		throw new RuntimeException("Something really weird happened with comparing towns.");
	}
}
