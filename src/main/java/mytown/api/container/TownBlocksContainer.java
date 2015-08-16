package mytown.api.container;

import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.handlers.VisualsHandler;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;

public class TownBlocksContainer extends ArrayList<TownBlock> {

    private int extraBlocks;
    private int maxClaims, maxFarClaims;

    public boolean contains(int dim, int x, int z) {
        for(TownBlock block : this) {
            if(block.getX() == x && block.getZ() == z && block.getDim() == dim) {
                return true;
            }
        }
        return false;
    }

    public TownBlock get(int dim, int x, int z) {
        for(TownBlock block : this) {
            if(block.getX() == x && block.getZ() == z && block.getDim() == dim) {
                return block;
            }
        }
        return null;
    }

    public int getExtraBlocks() {
        return extraBlocks;
    }

    public void setExtraBlocks(int extraBlocks) {
        this.extraBlocks = extraBlocks < 0 ? 0 : extraBlocks;
    }

    public int getMaxFarClaims() {
        return maxFarClaims;
    }

    public int getFarClaims() {
        int farClaims = 0;
        for(TownBlock block : this) {
            if (block.isFarClaim()) {
                farClaims++;
            }
        }
        return farClaims;
    }

    public void setMaxFarClaims(int maxFarClaims) {
        this.maxFarClaims = maxFarClaims;
    }

    public int getMaxBlocks() {
        /*
        int maxBlocks = Config.blocksMayor + (Config.blocksResident * (residents.size() - 1)) + extraBlocks;
        for (Resident res : getResidents()) {
            maxBlocks += res.getExtraBlocks();
        }
        */
        return this.maxClaims;
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
