package mytown.entities;

public class AdminTown extends Town {
    public AdminTown(String name) {
        super(name);
    }

    @Override
    public int getMaxBlocks() {
        return Integer.MAX_VALUE;
    }
}
