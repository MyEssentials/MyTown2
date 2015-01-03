package mytown.protection;

import mytown.core.Localization;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 8/31/2014.
 * Base class for any kind of protection, be it modded or not
 */
public abstract class Protection {

    /**
     * The items which there is protection against
     */
    public List<Class<? extends Item>> trackedItems;

    /**
     * The list of the types of hostile entities which there is protection against
     */
    public List<Class<? extends Entity>> hostileEntities;

    /**
     * The list of protected types of entities
     */
    public List<Class<? extends Entity>> protectedEntities;

    /**
     * The list of TRACKED types of entities. This list is checked every world tick
     */
    public List<Class<? extends Entity>> trackedEntities;

    /**
     * The list of types of protected blocks that can be actuvated by a right click.
     */
    public List<Block> activatedBlocks;

    /**
     * The list of types of blocks that are affected by the "explosions" flag
     */
    public List<Class<? extends Entity>> explosiveBlocks;

    /**
     * The list of types of tile entities which need to be checked
     */
    public List<Class<? extends TileEntity>> trackedTileEntities;

    /**
     * List of blocks that can interact with other blocks if put near each other
     */
    public List<Block> interactiveBlocks;

    //TODO: Map for place check outside towns
    //public Map<Class<? extends Block>>

    public boolean isHandlingEvents;

    public Protection() {
        trackedItems = new ArrayList<Class<? extends Item>>();
        hostileEntities = new ArrayList<Class<? extends Entity>>();
        protectedEntities = new ArrayList<Class<? extends Entity>>();
        trackedEntities = new ArrayList<Class<? extends Entity>>();
        activatedBlocks = new ArrayList<net.minecraft.block.Block>();
        explosiveBlocks = new ArrayList<Class<? extends Entity>>();
        trackedTileEntities = new ArrayList<Class<? extends TileEntity>>();
        interactiveBlocks = new ArrayList<Block>();
        isHandlingEvents = false;
    }

    /**
     * Checks the entity and returns whether or not the entity was destroyed
     *
     * @param entity
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean checkEntity(Entity entity) {
        return false;
    }

    /**
     * Checks the tile entity and returns whether or not the te was destroyed
     *
     * @param te
     * @return
     */
    public boolean checkTileEntity(TileEntity te) {
        return false;
    }

    /**
     * Checks if the resident or block with tile entity can use this item.
     * Resident can be null.
     *
     * @param itemStack
     * @param res
     * @return
     */
    public boolean checkItemUsage(ItemStack itemStack, Resident res, BlockPos bp) {
        return false;
    }

    public boolean hasToCheckTileEntity(TileEntity te) {
        for (Class<? extends TileEntity> cls : trackedTileEntities) {
            if (cls.isAssignableFrom(te.getClass()))
                return true;
        }
        return false;
    }

    public boolean hasToCheckEntity(Entity entity) {
        return trackedEntities.contains(entity.getClass()) || explosiveBlocks.contains(entity.getClass());
    }

    /**
     * Gets the range at which items can range bypassing flags
     *
     * @return
     */
    public int getRange() {
        return 0;
    }

    public List<FlagType> getFlagTypeForTile(TileEntity te) {
        return getFlagTypeForTile(te.getClass());
    }

    public List<FlagType> getFlagTypeForTile(Class<? extends TileEntity> te) {
        return null;
    }

    /* ---- Helpers ---- */
    protected MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }

    protected Localization getLocal() {
        return LocalizationProxy.getLocalization();
    }

}
