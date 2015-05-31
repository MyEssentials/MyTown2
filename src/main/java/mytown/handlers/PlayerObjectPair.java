package mytown.handlers;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Map which holds two keys.
 */
public class PlayerObjectPair {

    public EntityPlayerMP player;
    public Object object;

    public PlayerObjectPair(EntityPlayerMP player, Object object) {
        this.player = player;
        this.object = object;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof PlayerObjectPair) {
            return ((PlayerObjectPair) other).player.equals(this.player) && ((((PlayerObjectPair) other).object == null && this.object == null) || ((PlayerObjectPair) other).object == this.object);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return object == null ? player.hashCode() : player.hashCode() * object.hashCode();
    }
}
