package mytown.protection;

import mytown.MyTown;
import mytown.entities.Plot;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.util.BlockPos;
import mytown.util.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

            trackedTileEntities.add(clsTileActivator);
            trackedTileEntities.add(clsTileBreaker);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            MyTown.instance.log.error("Failed to load Thermal Expansion classes!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {
        if(clsTileActivator.isAssignableFrom(te.getClass())) {

            //Get the position at which is pointing at

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

            IInventory inv = (IInventory)te;
            for(int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null) {
                    if (stack.getItem() instanceof ItemBlock) {
                        Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, x >> 4, z >> 4);
                        if (town != null) {
                            Flag<Boolean> flag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, x, y, z, FlagType.placeBlocks);
                            // TODO: Should there be a whitelist check here?
                            if (!flag.getValue())
                                return true;
                        }
                    } else if (!Utils.isBlockWhitelisted(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.useItems)
                            && Protections.instance.checkItemUsage(stack, null, new BlockPos(te.xCoord, te.yCoord, te.zCoord, te.getWorldObj().provider.dimensionId))) {
                        return true;
                    }
                }
            }

            // The break flag

            Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, x >> 4, z >> 4);
            if(town != null) {
                Flag<Boolean> flag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, x, y, z, FlagType.breakBlocks);
                if (!flag.getValue() && !town.hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.breakBlocks)) {
                    //TODO: Change this to something proper
                    town.notifyEveryone(FlagType.breakBlocks.getLocalizedProtectionDenial());
                    return true;
                } else {
                    // The activate flag
                    flag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, x, y, z, FlagType.activateBlocks);
                    if (!flag.getValue() && !town.hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.activateBlocks)) {
                        town.notifyEveryone(FlagType.activateBlocks.getLocalizedProtectionDenial());
                        return true;
                    }
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

            Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, x >> 4, z >> 4);
            if(town != null) {
                Flag<Boolean> breakFlag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, x, y, z, FlagType.breakBlocks);
                if(!breakFlag.getValue() && !town.hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.breakBlocks)) {
                    town.notifyEveryone(FlagType.breakBlocks.getLocalizedProtectionDenial());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<FlagType> getFlagTypeForTile(Class<? extends TileEntity> te) {
        List<FlagType> list = new ArrayList<FlagType>();
        if(clsTileActivator.isAssignableFrom(te)) {
            list.add(FlagType.useItems);
            list.add(FlagType.activateBlocks);
            list.add(FlagType.placeBlocks);
            list.add(FlagType.breakBlocks);
        } else if(clsTileBreaker.isAssignableFrom(te)) {
            list.add(FlagType.breakBlocks);
        }
        return list;
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
