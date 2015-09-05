package mytown.entities.blocks;

import myessentials.Localization;
import mytown.entities.Resident;
import mytown.proxies.LocalizationProxy;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;

/**
 * A simple wrapper class of a sign block for the server side.
 */
public abstract class Sign {

    protected int x, y, z, face;
    protected World world;
    protected Resident owner;
    protected String identifier;

    public Sign(int dim, int x, int y, int z, int face, Resident owner, String identifier) {
        this.world = MinecraftServer.getServer().worldServerForDimension(dim);
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
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

    public void createSignBlock(String... description) {
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
        return LocalizationProxy.getLocalization();
    }
}
