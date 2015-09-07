package mytown.entities.blocks;

import myessentials.Localization;
import mytown.MyTown;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Plot;
import mytown.entities.Resident;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Arrays;

/**
 * A simple wrapper class of a sign block for the server side.
 */
public abstract class Sign {

    private int x;
    private int y;
    private int z;
    private World world;
    protected Resident owner;
    private String identifier;

    public Sign(int dim, int x, int y, int z, Resident owner, String identifier) {
        this.world = MinecraftServer.getServer().worldServerForDimension(dim);
        this.x = x;
        this.y = y;
        this.z = z;
        this.owner = owner;
        this.identifier = identifier;
    }

    public abstract void onRightClick(Resident resident);

    public void onShiftRightClick(Resident resident) {
    }

    public boolean exists() {
        TileEntity te = world.getTileEntity(x, y, z);
        if(te != null && te instanceof TileEntitySign) {
            return ((TileEntitySign) te).signText[0].equals(identifier);
        } else {
            return false;
        }
    }

    public void createSignBlock(int face, String... description) {
        ForgeDirection direction = ForgeDirection.getOrientation(face);
        if(direction == ForgeDirection.DOWN || face == 1) {
            int i1 = MathHelper.floor_double((double) ((owner.getPlayer().rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
            world.setBlock(x, y, z, Blocks.standing_sign, i1, 3);
        } else {
            world.setBlock(x, y, z, Blocks.wall_sign, face, 3);
        }

        TileEntitySign te = (TileEntitySign) world.getTileEntity(x, y, z);
        description = Arrays.copyOf(description, 3);

        te.signText[0] = identifier;
        for(int i = 0; i < 3; i++) {
            te.signText[i+1] = description[i] == null ? "" : description[i];
        }
    }

    public void deleteSignBlock() {
        world.setBlock(x, y, z, Blocks.air);
    }

    public Localization getLocal() {
        return MyTown.instance.LOCAL;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static boolean interact(PlayerInteractEvent ev) {
        if(!(ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
            return false;
        }

        Sign sign = Sign.getSign(ev.world, ev.x, ev.y, ev.z);
        if(sign == null) {
            return false;
        }

        Resident resident = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);

        if(sign.getX() == ev.x && sign.getY() == ev.y && sign.getZ() == ev.z && sign.getWorld() == ev.world) {
            if(ev.entityPlayer.isSneaking()) {
                sign.onShiftRightClick(resident);
            } else {
                sign.onRightClick(resident);
            }
            return true;
        }

        return false;
    }

    public static Sign getSign(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if(te == null || !(te instanceof TileEntitySign)) {
            return null;
        }

        Plot plot = MyTownUniverse.instance.plots.get(world.provider.dimensionId, x, y, z);
        if(plot == null) {
            return null;
        }

        return plot.signContainer.get();
    }
}
