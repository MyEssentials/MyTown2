package mytown.protection;

import mytown.datasource.MyTownDatasource;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.mod.ModProxy;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

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
    public List<Class<? extends Entity>> trackedAnyEntity = new ArrayList<Class<? extends Entity>>();
    public List<Class<? extends Entity>> trackedHostileEntities = new ArrayList<Class<? extends Entity>>();
    public List<Class<? extends TileEntity>> trackedTileEntities = new ArrayList<Class<? extends TileEntity>>();

    public boolean isHandlingEvents;

    /**
     * Checks the entity and returns whether or not the entity was destroyed
     *
     * @param entity
     * @return
     */
    public boolean checkEntity(Entity entity) { return false; }

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
