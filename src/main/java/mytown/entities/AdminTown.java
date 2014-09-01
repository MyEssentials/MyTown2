package mytown.entities;

import mytown.MyTown;
import mytown.core.utils.teleport.Teleport;
import mytown.entities.flag.Flag;
import net.minecraft.command.CommandException;

public class AdminTown extends Town {
    public AdminTown(String name) {
        super(name);
    }

    public AdminTown(String name, Resident creator) {
        super(name);

        Rank onCreationDefaultRank = null;

        // Setting spawn before saving
        setSpawn(new Teleport(creator.getPlayer().dimension, (float)creator.getPlayer().posX, (float)creator.getPlayer().posY, (float)creator.getPlayer().posZ, creator.getPlayer().cameraYaw, creator.getPlayer().cameraPitch));

        // Saving town to database
        if (!getDatasource().saveTown(this))
            throw new CommandException("Failed to save Town"); // TODO Localize!


        //Claiming first block
        Block block = getDatasource().newBlock(creator.getPlayer().dimension, creator.getPlayer().chunkCoordX, creator.getPlayer().chunkCoordZ, this);
        // Saving block to db and town
        getDatasource().saveBlock(block);

        // Saving and adding all flags to the database
        getDatasource().saveFlag(new Flag<Boolean>("enter", false), this);
        getDatasource().saveFlag(new Flag<Boolean>("breakBlocks", false), this);
        getDatasource().saveFlag(new Flag<Boolean>("explosions", false), this);
        getDatasource().saveFlag(new Flag<Boolean>("accessBlocks", false), this);
        getDatasource().saveFlag(new Flag<Boolean>("pickup", true), this);
        getDatasource().saveFlag(new Flag<Boolean>("enter", true), this);
        getDatasource().saveFlag(new Flag<String>("mobs", "all"), this);
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