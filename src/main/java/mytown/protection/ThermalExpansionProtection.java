package mytown.protection;

import mytown.MyTown;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.util.BlockPos;
import mytown.util.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Method;

/**
 * Created by AfterWind on 9/27/2014.
 * Protection module for Thermal Expansion
 */
public class ThermalExpansionProtection extends Protection {

    public static Class<? extends TileEntity> clsTileActivator;
    public static Class<? extends TileEntity> clsTileBreaker;

    @SuppressWarnings("unchecked")
    public ThermalExpansionProtection() {
        try {

            clsTileActivator = (Class<? extends TileEntity>)Class.forName("thermalexpansion.block.device.TileActivator");
            clsTileBreaker = (Class<? extends TileEntity>)Class.forName("thermalexpansion.block.device.TileBreaker");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            MyTown.instance.log.error("Failed to load Thermal Expansion classes!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {
        MyTown.instance.log.info("Checking tile " + te);
        if(clsTileActivator.isAssignableFrom(te.getClass())) {
            IInventory inv = (IInventory)te;
            for(int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if(stack != null) {
                    if(Protections.instance.checkItemUsage(stack, null, new BlockPos(te.xCoord, te.yCoord, te.zCoord, te.getWorldObj().provider.dimensionId)))
                        return true;
                }
            }
        } else if(clsTileBreaker.isAssignableFrom(te.getClass())) {
            int x = te.xCoord;
            int y = te.yCoord;
            int z = te.zCoord;

            switch (getFacing(te)) {
                case 0:
                    y--;
                    break;
                case 1:
                    y++;
                    break;
                case 2:
                    z--;
                    break;
                case 3:
                    z++;
                    break;
                case 4:
                    x--;
                    break;
                case 5:
                    x++;
                    break;
            }

            MyTown.instance.log.info("Got facing: " + getFacing(te));

            Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, x >> 4, z >> 4);
            if(town != null) {
                Flag<Boolean> breakFlag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, x, y, z, FlagType.breakBlocks);
                if(!breakFlag.getValue()) {
                    // TODO: This is temporary
                    town.notifyEveryone(FlagType.breakBlocks.getLocalizedProtectionDenial());
                    return true;
                }
            }

        }
        return false;
    }

    public static int getFacing(TileEntity te) {
        try {
            Method method = clsTileActivator.getMethod("getFacing");
            return (Integer)method.invoke(te);

        } catch (Exception e) {
            e.printStackTrace();
            MyTown.instance.log.error("Failed to get facing for " + te + ", maybe it's a mistake?");
        }
        return -1;
    }

}
