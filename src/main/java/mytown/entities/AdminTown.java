package mytown.entities;

import mytown.MyTown;
import mytown.core.utils.teleport.Teleport;
import mytown.entities.flag.Flag;
import net.minecraft.command.CommandException;

public class AdminTown extends Town {
    public AdminTown(String name) {
        super(name);
    }

    // TODO: Finish this up
    // TODO: Add checks for some commands so that people don't try to add stuff to it.


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