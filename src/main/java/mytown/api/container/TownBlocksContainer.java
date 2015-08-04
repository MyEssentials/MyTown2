package mytown.api.container;

import com.google.common.collect.ImmutableList;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.handlers.VisualsHandler;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;

public class TownBlocksContainer {

    private ArrayList<TownBlock> townBlocks = new ArrayList<TownBlock>();

    public void addBlock(TownBlock block) {
        townBlocks.add(block);
        //VisualsHandler.instance.updateTownBorders(this);
    }


    public void removeBlock(TownBlock block) {
        townBlocks.remove(block);
        //VisualsHandler.instance.updateTownBorders(this);
    }

    public boolean hasBlock(TownBlock block) {
        return townBlocks.contains(block);
    }

    public boolean hasBlock(int dim, int x, int z) {
        for(TownBlock block : townBlocks) {
            if(block.getX() == x && block.getZ() == z && block.getDim() == dim) {
                return true;
            }
        }
        return false;
    }

    public TownBlock getBlockAtCoords(int dim, int x, int z) {
        for(TownBlock block : townBlocks) {
            if(block.getX() == x && block.getZ() == z && block.getDim() == dim) {
                return block;
            }
        }
        return null;
    }

    /*
    public int getExtraBlocks() { return extraBlocks; }

    public void setExtraBlocks(int extra) { extraBlocks = extra < 0 ? 0 : extra; }

    public int getMaxBlocks() { // TODO Cache this stuff?
        int maxBlocks = Config.blocksMayor + (Config.blocksResident * (residents.size() - 1)) + extraBlocks;
        for (Resident res : getResidents()) {
            maxBlocks += res.getExtraBlocks();
        }

        return maxBlocks;
    }
    */

    public int getFarClaims() {
        int farClaims = 0;
        for(TownBlock block : townBlocks) {
            if (block.isFarClaim()) {
                farClaims++;
            }
        }
        return farClaims;
    }

    /*
    public int getMaxFarClaims() { return maxFarClaims; }

    public void setMaxFarClaims(int maxFarClaims) { this.maxFarClaims = maxFarClaims; }
    */

    public ImmutableList<TownBlock> getBlocks() {
        return ImmutableList.copyOf(townBlocks);
    }

    /*
    public void showBorders(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.markTownBorders(this, (EntityPlayerMP)caller.getPlayer());
    }
    */

    public void hideBorders(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) caller.getPlayer(), this);
    }

}
