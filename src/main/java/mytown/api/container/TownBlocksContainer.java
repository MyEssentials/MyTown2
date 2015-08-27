package mytown.api.container;

import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.handlers.VisualsHandler;
import myessentials.utils.ColorUtils;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;

public class TownBlocksContainer extends ArrayList<TownBlock> {

    private int extraBlocks;
    private int maxFarClaims;

    public boolean add(TownBlock block) {
        boolean result = super.add(block);
        VisualsHandler.instance.updateTownBorders(this);
        return result;
    }

    public boolean remove(TownBlock block) {
        boolean result = super.remove(block);
        VisualsHandler.instance.updateTownBorders(this);
        return result;
    }

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
        this.extraBlocks = extraBlocks;
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

    public void show(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.markTownBorders(this, (EntityPlayerMP)caller.getPlayer());
    }

    public void hide(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) caller.getPlayer(), this);
    }

    @Override
    public String toString() {
        String formattedList = "";
        for(TownBlock block : this) {
            String toAdd = ColorUtils.colorComma + "{"+ ColorUtils.colorCoords + (block.getX() << 4) + ColorUtils.colorComma + ", "
                    + ColorUtils.colorCoords + (block.getZ() << 4) + ColorUtils.colorComma + "}";
            if(formattedList.equals("")) {
                formattedList = toAdd;
            } else {
                formattedList += ColorUtils.colorComma + "; " + toAdd;
            }
        }
        return formattedList;
    }
}
