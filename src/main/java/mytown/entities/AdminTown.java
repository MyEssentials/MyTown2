package mytown.entities;

public class AdminTown extends Town {
    public AdminTown(String name) {
        super(name);
    }

    @Override
    public void addBlock(Block block) {
        blocks.put(block.getKey(), block);
        // TODO: To be edited when checking distance between towns.
    }

    @Override
    public void addResident(Resident res, Rank rank) {
        // Nothing, since admin towns shouldn't have residents
    }

}