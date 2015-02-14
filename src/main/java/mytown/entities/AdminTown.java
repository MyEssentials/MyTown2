package mytown.entities;

public class AdminTown extends Town {
    public AdminTown(String name) {
        super(name);

        Rank rank = new Rank(Rank.theMayorDefaultRank, Rank.defaultRanks.get(Rank.theMayorDefaultRank), this);
        addRank(rank);
        setDefaultRank(rank);
    }

    // TODO: Finish this up
    // TODO: Add checks for some commands so that people don't try to add stuff to it.


    @Override
    public void addBlock(TownBlock townBlock) {
        blocks.put(townBlock.getKey(), townBlock);
        // TODO: To be edited when checking distance between towns.
    }

    @Override
    public boolean hasMaxAmountOfBlocks() {
        return false;
    }
}