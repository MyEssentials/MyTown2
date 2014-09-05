package mytown.protection;

import mytown.datasource.MyTownDatasource;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.mod.ModProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
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

    public boolean isHandlingEvents;

    public Protection() {
        itemUsageProtection = new ArrayList<Class<? extends Item>>();
        anyEntity = new ArrayList<Class<? extends Entity>>();
        hostileEntities = new ArrayList<Class<? extends Entity>>();
        protectedEntities = new ArrayList<Class<? extends Entity>>();
        trackedTileEntities = new ArrayList<Class<? extends TileEntity>>();
        trackedEntities = new ArrayList<Class<? extends Entity>>();
        activatedBlocks = new ArrayList<net.minecraft.block.Block>();
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
        Town town = Town.getTownAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
        if(town == null)
            return false;

        Flag<String> mobFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, "mobs");
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
     * Checks if the tile entity specified needs to be checked on server tick
     *
     * @param te
     * @return
     */
    public boolean hasToCheckTileEntity(TileEntity te) {
        return trackedTileEntities.contains(te.getClass());
    }

    /**
     * * Checks if the entity specified needs to be checked on server tick
     *
     * @param e
     * @return
     */
    public boolean hasToCheckEntity(Entity e) {
        return trackedEntities.contains(e.getClass()) || hostileEntities.contains(e.getClass()) || anyEntity.contains(e.getClass());
    }


    /* ---- Proxy ---- */
    protected ModProxy proxy;
    public void setProxy(ModProxy proxy) {
        this.proxy = proxy;
    }
    public ModProxy getProxy() {
        return proxy;
    }

    /* ---- Helpers ---- */
    protected MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}
