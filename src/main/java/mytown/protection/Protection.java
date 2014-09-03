package mytown.protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Block;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.mod.ModProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 8/31/2014.
 * Base class for any kind of protection, be it modded or not
 */
public abstract class Protection {

    /**
     * Any entity except the player and hostile entities
     */

    public List<Class<? extends Item>> itemUsageProtection;
    public List<Class<? extends Entity>> trackedAnyEntity;
    public List<Class<? extends Entity>> trackedHostileEntities;
    public List<Class<? extends Entity>> protectedEntities;
    public List<Class<? extends TileEntity>> trackedTileEntities;
    public List<net.minecraft.block.Block> activatedBlocks;

    public boolean isHandlingEvents;

    public Protection() {
        itemUsageProtection = new ArrayList<Class<? extends Item>>();
        trackedAnyEntity = new ArrayList<Class<? extends Entity>>();
        trackedHostileEntities = new ArrayList<Class<? extends Entity>>();
        protectedEntities = new ArrayList<Class<? extends Entity>>();
        trackedTileEntities = new ArrayList<Class<? extends TileEntity>>();
        activatedBlocks = new ArrayList<net.minecraft.block.Block>();
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
        Town town = Town.getTownAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
        if(town == null)
            return false;

        Flag<String> mobFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, "mobs");
        String value = mobFlag.getValue();

        if(value.equals("all")) {
            if(entity instanceof EntityLivingBase) {
                entity.setDead();
                return true;
            }
        } else if(value.equals("hostiles")) {
            if(trackedHostileEntities.contains(entity.getClass())) {
                entity.setDead();
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
