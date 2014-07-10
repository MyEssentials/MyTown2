package mytown.modules;

import mytown.api.datasource.MyTownDatasource;
import mytown.core.Localization;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.Map;

/**
 * Created by AfterWind on 7/8/2014.
 *
 * Abstract class for implementation for mods
 */
public abstract class ModuleBase {

    boolean enabled;

    abstract public void load();

    public boolean check(TileEntity te, Resident resident) {
        return true;
    }

    public boolean check(Entity e) {
        return true;
    }

    public boolean check(ResidentBlockCoordsPair block) {
        return true;
    }

    public boolean check(ItemStack itemStack, Resident resident) {
        return true;
    }

    public boolean isEntityInstance(TileEntity te) {
        return false;
    }

    public boolean isEntityInstance(Entity entity) {
        return false;
    }

    public boolean isEntityInstance(Item item) {
        return false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    void disable() {
        enabled = false;
    }

    void enable() {
        enabled = true;
    }

    abstract public String getModID();

    public MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }

    public Localization getLocal() {
        return LocalizationProxy.getLocalization();
    }

}
