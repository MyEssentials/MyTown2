package mytown.protection;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import cpw.mods.fml.common.registry.GameRegistry;
import mytown.MyTown;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.proxies.mod.ModProxies;
import mytown.util.BlockPos;
import mytown.util.ChunkPos;
import mytown.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 10/11/2014.
 * MFR protection
 */
public class MinefactoryReloadedProtection extends Protection {

    private Class<? extends TileEntity> clsTileEntityFactory;
    private Item safariNet, safariNetR, safariNetJ, portaSpawner;
    private Class<? extends Item> clsSafariNet;

    private FieldAccess fAccessArea;
    private MethodAccess mAccessArea, mAccessTile;

    @SuppressWarnings("unchecked")
    public MinefactoryReloadedProtection() {
        try {
            Class<? extends Entity> clsFishingRod = (Class<? extends Entity>)Class.forName("powercrystals.minefactoryreloaded.entity.EntityFishingRod");
            Class<? extends Entity> clsRocket = (Class<? extends Entity>)Class.forName("powercrystals.minefactoryreloaded.entity.EntityRocket");

            clsTileEntityFactory = (Class<? extends TileEntity>)Class.forName("powercrystals.minefactoryreloaded.tile.base.TileEntityFactory");
            Class<?> clsAreaManager = Class.forName("powercrystals.minefactoryreloaded.core.HarvestAreaManager");
            Class<?> clsArea = Class.forName("cofh.lib.util.position.Area");

            fAccessArea = FieldAccess.get(clsArea);
            mAccessTile = MethodAccess.get(clsTileEntityFactory);
            mAccessArea = MethodAccess.get(clsAreaManager);

            clsSafariNet = (Class<? extends Item>) Class.forName("powercrystals.minefactoryreloaded.item.ItemSafariNet");

            safariNet = GameRegistry.findItem(ModProxies.MFR_MOD_ID, "mfr.safarinet.singleuse");
            safariNetR = GameRegistry.findItem(ModProxies.MFR_MOD_ID, "mfr.safarinet.reusable");
            safariNetJ = GameRegistry.findItem(ModProxies.MFR_MOD_ID, "mfr.safarinet.jailer");
            portaSpawner = GameRegistry.findItem(ModProxies.MFR_MOD_ID, "mfr.portaspawner");

            trackedItems.add(clsSafariNet);
            explosiveBlocks.add(clsFishingRod);
            explosiveBlocks.add(clsRocket);
            trackedTileEntities.add(clsTileEntityFactory);
        } catch (Exception e) {
            MyTown.instance.log.error("Failed to load MFR classes!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {
        if(clsTileEntityFactory.isAssignableFrom(te.getClass())) {
            MyTown.instance.log.info("Checking machine in mfr.");

            // Getting harvest area manager
            Object areaManager = mAccessTile.invoke(te, "getHAM");
            if(areaManager == null)
                return false;

            // Getting area
            Object areaObj = mAccessArea.invoke(areaManager, "getHarvestArea");
            if(areaObj == null)
                return false;

            List<ChunkPos> chunkPos = Utils.getChunksInBox(fAccessArea.getInt(areaObj, fAccessArea.getIndex("xMin")), fAccessArea.getInt(areaObj, fAccessArea.getIndex("zMin")), fAccessArea.getInt(areaObj, fAccessArea.getIndex("xMax")), fAccessArea.getInt(areaObj, fAccessArea.getIndex("zMax")));
            for (ChunkPos pos : chunkPos) {
                Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, pos.getX(), pos.getZ());
                if (town != null) {
                    //DEV
                    MyTown.instance.log.info("Found town for mfr protection: " + town.getName());
                    boolean breakFlag = (Boolean)town.getValue(FlagType.breakBlocks);
                    if (!breakFlag && !town.hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.breakBlocks)) {
                        town.notifyEveryone(FlagType.breakBlocks.getLocalizedTownNotification());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean checkItemUsage(ItemStack itemStack, Resident res, BlockPos bp) {
        if(itemStack.getItem() == safariNet || itemStack.getItem() == safariNetR || itemStack.getItem() == safariNetJ || itemStack.getItem() == portaSpawner) {
            MyTown.instance.log.info("Found safari net usage!");
            Town town = Utils.getTownAtPosition(bp.dim, bp.x >> 4, bp.z >> 4);
            if(town != null) {
                boolean entityFlag = (Boolean)town.getValueAtCoords(bp.dim, bp.x, bp.y, bp.z, FlagType.attackEntities);
                if(!entityFlag && (res == null || !town.checkPermission(res, FlagType.attackEntities))) {
                    if(res != null)
                        res.sendMessage(FlagType.attackEntities.getLocalizedProtectionDenial());
                    else
                        town.notifyEveryone(FlagType.attackEntities.getLocalizedTownNotification());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<FlagType> getFlagTypeForTile(Class<? extends TileEntity> te) {
        List<FlagType> list = new ArrayList<FlagType>();
        if(te.isAssignableFrom(clsTileEntityFactory))
            list.add(FlagType.breakBlocks);
        return list;
    }
}
