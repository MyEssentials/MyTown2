package mytown.protection;

import cpw.mods.fml.common.registry.GameRegistry;
import mytown.MyTown;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.util.BlockPos;
import mytown.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

/**
 * Created by AfterWind on 9/22/2014.
 * BloodMagic protection
 */
public class BloodMagicProtection extends Protection {

    Class<? extends Item> clsBoundPickaxe;
    Class<? extends Item> clsSigilWater, clsSigilLava, clsSigilMagnetism, clsSigilVoid;

    @SuppressWarnings("unchecked")
    public BloodMagicProtection() {
        try {
            clsBoundPickaxe = (Class<? extends Item>)Class.forName("WayofTime.alchemicalWizardry.common.items.BoundPickaxe");
            clsSigilWater = (Class<? extends Item>)Class.forName("WayofTime.alchemicalWizardry.common.items.sigil.WaterSigil");
            clsSigilLava = (Class<? extends Item>)Class.forName("WayofTime.alchemicalWizardry.common.items.sigil.LavaSigil");
            clsSigilVoid = (Class<? extends Item>)Class.forName("WayofTime.alchemicalWizardry.common.items.sigil.VoidSigil");
            clsSigilMagnetism = (Class<? extends Item>)Class.forName("WayofTime.alchemicalWizardry.common.items.sigil.SigilOfMagnetism");
        } catch (Exception e) {
            e.printStackTrace();
            MyTown.instance.log.error("Failed to load BloodMagic classes!");
        }

        activatedBlocks.add(GameRegistry.findBlock(BloodMagicProxy.MOD_ID, "armourForge"));
        activatedBlocks.add(GameRegistry.findBlock(BloodMagicProxy.MOD_ID, "bloodAltar"));
        activatedBlocks.add(GameRegistry.findBlock(BloodMagicProxy.MOD_ID, "blockMasterStone"));
        activatedBlocks.add(GameRegistry.findBlock(BloodMagicProxy.MOD_ID, "bloodPedestal"));

        trackedItems.add(clsBoundPickaxe);
        trackedItems.add(clsSigilWater);
        trackedItems.add(clsSigilLava);
        trackedItems.add(clsSigilVoid);
        trackedItems.add(clsSigilMagnetism);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkItemUsage(ItemStack itemStack, Resident res, BlockPos bp) {
        MyTown.instance.log.info("Got item: " + itemStack.getDisplayName());
        if(clsBoundPickaxe == itemStack.getItem().getClass()) {
            // Range is 11x11 around the player.
            List<Town> townsNearby = Utils.getTownsInRange(bp.dim, bp.x, bp.z, 5, 5);
            for(Town town : townsNearby) {
                boolean breakBlock = (Boolean)town.getValue(FlagType.breakBlocks);
                if(!breakBlock) {
                    // If resident is null then it's used by a block
                    if(res == null) {
                        town.notifyEveryone(FlagType.breakBlocks.getLocalizedProtectionDenial());
                        return true;
                    } else if(!town.checkPermission(res, FlagType.breakBlocks, res.getPlayer().dimension, (int)res.getPlayer().posX, (int)res.getPlayer().posY, (int)res.getPlayer().posZ)) {
                        res.sendMessage(FlagType.breakBlocks.getLocalizedProtectionDenial());
                        return true;
                    }
                }
            }
        } else if(clsSigilLava == itemStack.getItem().getClass() || clsSigilWater == itemStack.getItem().getClass() || clsSigilVoid == itemStack.getItem().getClass()) {


            if(res != null) {
                MovingObjectPosition pos;
                pos = Utils.getMovingObjectPositionFromPlayer(res.getPlayer().getEntityWorld(), res.getPlayer(), false);

                // Only if it's a block is gonna be activated
                if (pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    int x = pos.blockX;
                    int y = pos.blockY;
                    int z = pos.blockZ;

                    switch (pos.sideHit) {
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

                    Town town = Utils.getTownAtPosition(res.getPlayer().dimension, x >> 4, z >> 4);
                    if (town != null) {
                        boolean itemUsage = (Boolean)town.getValueAtCoords(res.getPlayer().dimension, x, y, z, FlagType.useItems);
                        if (!itemUsage && !town.checkPermission(res, FlagType.useItems, res.getPlayer().dimension, x, y, z)) {
                            res.sendMessage(FlagType.useItems.getLocalizedProtectionDenial());
                            return true;
                        }
                    }
                }
            } else {
                int x = bp.x;
                int y = bp.y;
                int z = bp.z;

                switch (ThermalExpansionProtection.getFacing(DimensionManager.getWorld(bp.dim).getTileEntity(bp.x, bp.y, bp.z))) {
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

                Town town = Utils.getTownAtPosition(bp.dim, x >> 4, z >> 4);
                if (town != null) {
                    boolean itemUsage = (Boolean)town.getValueAtCoords(bp.dim, x, y, z, FlagType.useItems);
                    if (!itemUsage) {
                        town.notifyEveryone(FlagType.useItems.getLocalizedTownNotification());
                        return true;
                    }
                }

            }
        } else if(clsSigilMagnetism == itemStack.getItem().getClass()) {
            // Range to attract items is 5 from player
            List<Town> nearbyTowns = Utils.getTownsInRange(bp.dim, bp.x, bp.z, 5, 5);
            for(Town town : nearbyTowns) {
                boolean pickupFlag = (Boolean)town.getValue(FlagType.pickupItems);
                if(!pickupFlag)
                    if(res == null) {
                        deactivateSigil(itemStack);
                        return true;
                    } else if(!town.checkPermission(res, FlagType.pickupItems, res.getPlayer().dimension, (int)res.getPlayer().posX, (int)res.getPlayer().posY, (int)res.getPlayer().posZ)) {
                        res.sendMessage(getLocal().getLocalization("mytown.protection.itemUsage.nearby"));
                        // TODO: Rework the check for sigils that are already activated
                        deactivateSigil(itemStack);
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getRange() {
        return 5;
    }

    protected void deactivateSigil(ItemStack itemStack) {
        NBTTagCompound tag = itemStack.stackTagCompound;
        tag.setBoolean("isActive", false);
    }
}
