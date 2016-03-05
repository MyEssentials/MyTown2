package mytown.entities;

import myessentials.chat.api.ChatFormat;
import myessentials.chat.api.IChatFormat;
import myessentials.entities.api.ChunkPos;
import myessentials.entities.api.Volume;
import myessentials.utils.ColorUtils;
import mytown.MyTown;
import mytown.config.Config;
import mytown.handlers.VisualsHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;

public class TownBlock extends ChatFormat {
    /**
     * Used for storing in database
     */
    public static final String KEY_FORMAT = "%s;%s;%s";

    private final int dim;
    private final int x;
    private final int z;
    private final Town town;
    private String key;

    private final boolean isFarClaim;
    private final int pricePaid;

    public final Plot.Container plotsContainer = new Plot.Container(Config.instance.defaultMaxPlots.get());

    public TownBlock(int dim, int x, int z, boolean isFarClaim, int pricePaid, Town town) {
        this.dim = dim;
        this.x = x;
        this.z = z;
        this.town = town;
        this.isFarClaim = isFarClaim;
        this.pricePaid = pricePaid;
        updateKey();
    }

    public int getDim() {
        return dim;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getCoordString() {
        return String.format("%s, %s", x, z);
    }

    public Town getTown() {
        return town;
    }

    public String getKey() {
        return key;
    }

    private void updateKey() {
        key = String.format(KEY_FORMAT, dim, x, z);
    }

    public boolean isFarClaim() {
        return this.isFarClaim;
    }

    public int getPricePaid() {
        return this.pricePaid;
    }

    @Override
    public String toString() {
        return toChatMessage().getUnformattedText();
    }

    @Override
    public IChatComponent toChatMessage(boolean shortened) {
        if (shortened) {
            return MyTown.instance.LOCAL.getLocalization("mytown.format.block.short", toChunkPos());
        } else {
            return MyTown.instance.LOCAL.getLocalization("mytown.format.block.long", dim, x, z, town.getName(), plotsContainer.size());
        }
    }

    public Volume toVolume() {
        return new Volume(x << 4, 0, z << 4, (x << 4) + 15, 255, (z << 4) + 15);
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(this.dim, this.x, this.z);
    }

    /* ----- Helpers ----- */

    /**
     * Checks if the point is inside this Block
     */
    public boolean isPointIn(int dim, float x, float z) {
        return isChunkIn(dim, ((int) x) >> 4, ((int) z) >> 4);
    }

    /**
     * Checks if the chunk is this Block
     */
    public boolean isChunkIn(int dim, int cx, int cz) {
        return dim == this.dim && cx == x && cz == z;
    }

    public static class Container extends ArrayList<TownBlock> implements IChatFormat {

        private int extraBlocks = 0;
        private int extraFarClaims = 0;

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

        public int getExtraFarClaims() {
            return extraFarClaims;
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

        public void setExtraFarClaims(int extraFarClaims) {
            this.extraFarClaims = extraFarClaims;
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
        public IChatComponent toChatMessage(boolean shortened) {
            IChatComponent result = new ChatComponentText("");

            for (TownBlock block : this) {
                result.appendSibling(MyTown.instance.LOCAL.getLocalization("mytown.format.block.short", block.toChunkPos()));
                result.appendSibling(new ChatComponentText(" "));
            }

            return result;
        }

        @Override
        public IChatComponent toChatMessage() {
            return toChatMessage(false);
        }
    }
}
