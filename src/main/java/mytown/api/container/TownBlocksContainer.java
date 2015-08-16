package mytown.api.container;

import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.handlers.VisualsHandler;
import mytown.util.ColorUtils;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.Collection;

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


    public void show(Resident caller) {
        //if(caller.getPlayer() instanceof EntityPlayerMP)
        //    VisualsHandler.instance.markTownBorders(this, (EntityPlayerMP)caller.getPlayer());
    }



    public void hide(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) caller.getPlayer(), this);
    }

    @Override
    public String toString() {
        String formattedList = null;
        for(TownBlock block : this) {
            String toAdd = ColorUtils.colorComma + "{"+ ColorUtils.colorCoords + (block.getX() << 4) + ColorUtils.colorComma + ","
                    + ColorUtils.colorCoords + (block.getZ() << 4) + ColorUtils.colorComma + "}";
            if(formattedList == null) {
                formattedList = toAdd;
            } else {
                formattedList += ColorUtils.colorComma + "; " + toAdd;
            }
        }
        return formattedList;
    }
}
