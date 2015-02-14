package mytown.new_protection.segment;

import net.minecraft.tileentity.TileEntity;

/**
 * Created by AfterWind on 1/1/2015.
 * An object that is considered to be able to break/place blocks.
 */
public interface IBlockModifier {

    Shape getShape();

    int getX1(TileEntity te);
    int getZ1(TileEntity te);

    int getX2(TileEntity te);
    int getZ2(TileEntity te);

    public enum Shape {
        rectangular,
        spherical
    }
}
