
package mytown.modules;

import mytown.entities.Resident;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 7/10/2014.
 * Buildcraft module protection
 */

public class BuildcraftModule extends ModuleBase {
    public static final String ModID = "BuildCraft";
    public List<TileEntity> checkedEntitys = new ArrayList<TileEntity>();

    Class<?> clQuarry = null, clFiller, clBuilder, clBox;
    Field fBoxQ, fBoxF, fBoxB, fmx, fmy, fmz, fxx, fxy, fxz, fBoxInit, fQuarryOwner, fQuarryBuilderDone;

    @Override
    public void load() {
        try {
            clQuarry = Class.forName("buildcraft.factory.TileQuarry");
            clFiller = Class.forName("buildcraft.builders.TileFiller");
            clBuilder = Class.forName("buildcraft.builders.TileBuilder");

            clBox = Class.forName("buildcraft.core.Box");

            fBoxQ = clQuarry.getField("box");
            fQuarryOwner = clQuarry.getField("placedBy");
            fQuarryBuilderDone = clQuarry.getField("builderDone");
            fBoxF = clFiller.getDeclaredField("box");
            fBoxF.setAccessible(true);
            fBoxB = clBuilder.getField("box");

            fmx = clBox.getField("xMin");
            fmy = clBox.getField("yMin");
            fmz = clBox.getField("zMin");
            fxx = clBox.getField("xMax");
            fxy = clBox.getField("yMax");
            fxz = clBox.getField("zMax");
            fBoxInit = clBox.getField("initialized");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isEntityInstance(TileEntity e) {
        Class<?> c = e.getClass();

        return c == clQuarry || c == clFiller || c == clBuilder;
    }

    @Override
    public String getModID() {
        return ModID;
    }

    @Override
    public boolean check(TileEntity te, Resident resident) {
        if (checkedEntitys.contains(te)) {
            return true;
        }
        boolean status = true;
        try {
            status = updateSub(te, resident);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!status) {
            checkedEntitys.add(te);
        }

        return status;
    }

    private boolean updateSub(TileEntity e, Resident resident) throws Exception {
        Object box = null;
        Class<?> clazz = e.getClass();

        if (clazz == clQuarry) {
            box = fBoxQ.get(e);
        } else if (clazz == clFiller) {
            box = fBoxF.get(e);
        } else if (clazz == clBuilder) {
            box = fBoxB.get(e);
        }

        boolean init = fBoxInit.getBoolean(box);
        if (!init) {
            return true;
        }

        if (clazz == clQuarry && fQuarryBuilderDone.getBoolean(e)) {
            return true;
        }

        int ax = fmx.getInt(box);
        fmy.getInt(box);
        int az = fmz.getInt(box);

        int bx = fxx.getInt(box);
        fxy.getInt(box);
        int bz = fxz.getInt(box);

        int fx = ax >> 4;
        int fz = az >> 4;
        int tx = bx >> 4;
        int tz = bz >> 4;
        /*
        Resident owner = null;
        if (clazz == clQuarry) {
            EntityPlayer pl = (EntityPlayer) fQuarryOwner.get(e);
            if (pl != null) {
                owner = DatasourceProxy.getDatasource().getResident(pl.username);
            } else {
                return null;
            }
        }

        for (int z = fz; z <= tz; z++) {
            for (int x = fx; x <= tx; x++) {
                TownBlock block = DatasourceProxy.getDatasource().getTownBlock(e.worldObj.provider.dimensionId, x, z, true);

                boolean allowed = false;
                if (block == null || block.town() == null) {
                    allowed = MyTown.instance.getWorldWildSettings(e.worldObj.provider.dimensionId).allowBuildcraftMiners;
                } else if (owner != null) {
                    allowed = owner.canInteract(block, Permissions.Build);
                } else {
                    allowed = block.settings.allowBuildcraftMiners;
                }

                if (!allowed) {
                    ProtectionEvents.instance.lastOwner = owner;

                    String b = block == null || block.town() == null ? "wild" : block.town().name() + (block.owner() != null ? " owned by " + block.ownerDisplay() : "");
                    b = String.format("%s @ dim %s (%s,%s)", b, e.worldObj.provider.dimensionId, x, z);

                    return "Region will hit " + b + " which doesn't allow buildcraft block breakers";
                }
            }
        }
*/
        return true;
    }



}
