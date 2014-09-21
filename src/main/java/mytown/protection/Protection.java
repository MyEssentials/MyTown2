package mytown.protection;

import mytown.core.Localization;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.ModProxy;
import mytown.util.BlockPair;
import mytown.util.BlockPos;
import mytown.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 8/31/2014.
 * Base class for any kind of protection, be it modded or not
 */
public abstract class Protection {

    /**
     * The items which there is protection against
     */
    public List<Class<? extends Item>> itemUsageProtection;

    /**
     * The list of any entity found in each protection
     */
    public List<Class<? extends Entity>> anyEntity;

    /**
     * The list of the types of hostile entities which there is protection against
     */
    public List<Class<? extends Entity>> hostileEntities;

    /**
     * The list of protected types of entities
     */
    public List<Class<? extends Entity>> protectedEntities;

    /**
     * The list of TRACKED types of tile entities. This list is checked every world tick.
     */
    public List<Class<? extends TileEntity>> trackedTileEntities;

    /**
     * The list of TRACKED types of entities. This list is checked every world tick
     */
    public List<Class<? extends Entity>> trackedEntities;

    /**
     * The list of types of protected blocks that can be actuvated by a right click.
     */
    public List<net.minecraft.block.Block> activatedBlocks;

    /**
     * The list of types of blocks that are affected by the "explosions" flag
     */
    public List<Class<? extends Entity>> explosiveBlocks;

    /**
     * List of blocks that can interact with other blocks if put near each other
     */
    public List<Block> interactiveBlocks;

    //TODO: Map for place check outside towns
    //public Map<Class<? extends Block>>

    public boolean isHandlingEvents;

    public Protection() {
        itemUsageProtection = new ArrayList<Class<? extends Item>>();
        anyEntity = new ArrayList<Class<? extends Entity>>();
        hostileEntities = new ArrayList<Class<? extends Entity>>();
        protectedEntities = new ArrayList<Class<? extends Entity>>();
        trackedTileEntities = new ArrayList<Class<? extends TileEntity>>();
        trackedEntities = new ArrayList<Class<? extends Entity>>();
        activatedBlocks = new ArrayList<net.minecraft.block.Block>();
        explosiveBlocks = new ArrayList<Class<? extends Entity>>();
        interactiveBlocks = new ArrayList<Block>();
        isHandlingEvents = false;
    }
    /**
     * Checks the entity and returns whether or not the entity was destroyed
     * If you override this, call super method.
     *
     * @param entity
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean checkEntity(Entity entity) {
        Town town = Utils.getTownAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
        if(town == null)
            return false;

        Flag<String> mobFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, FlagType.mobs);
        String value = mobFlag.getValue();

        if(value.equals("all")) {
            if(entity instanceof EntityLivingBase) {
                return true;
            }
        } else if(value.equals("hostiles")) {
            if(hostileEntities.contains(entity.getClass())) {
                return true;
            }
        }

        Flag<Boolean> explosionsFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, FlagType.explosions);

        if(!explosionsFlag.getValue()) {
            if(explosiveBlocks.contains(entity.getClass())) {
                return true;
            }
        }


        return false;
    }

    /**
     * Checks the tile entity and returns whether or not the te was destroyed
     *
     * @param te
     * @return
     */
    public boolean checkTileEntity(TileEntity te) { return false; }

    /**
     * Checks if the block with position is valid.
     *
     * @param bp
     * @return
     */
    public boolean checkPlacement(BlockPos bp, Item item) { return false; }

    /**
     * Checks if the tile entity specified needs to be checked on server tick
     *
     * @param te
     * @return
     */
    public boolean hasToCheckTileEntity(TileEntity te) {

        if(trackedTileEntities.contains(te.getClass()))
            return true;
        for(Class<? extends TileEntity> cls : trackedTileEntities) {
            if(cls.isAssignableFrom(te.getClass()))
                return true;
        }
        return false;
    }

    /**
     * * Checks if the entity specified needs to be checked on server tick
     *
     * @param e
     * @return
     */
    public boolean hasToCheckEntity(Entity e) {
        return trackedEntities.contains(e.getClass()) || hostileEntities.contains(e.getClass()) || anyEntity.contains(e.getClass()) || explosiveBlocks.contains(e.getClass());
    }

    /**
     * Checks if the given tile entity position has any whitelists
     *
     * @param te
     * @return
     */
    public boolean checkForWhitelist(TileEntity te) { return false; }

    public List<FlagType> getFlagTypeForTile(TileEntity te) { return getFlagTypeForTile(te.getClass()); }
    public List<FlagType> getFlagTypeForTile(Class<? extends TileEntity> te) { return null; }

    /* ---- Helpers ---- */
    protected MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
    protected Localization getLocal() {return LocalizationProxy.getLocalization(); }

}
