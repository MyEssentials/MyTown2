package mytown.modules;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import ic2.api.info.Info;
import mytown.entities.Resident;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by AfterWind on 7/10/2014.
 *
 */
public class UniversalChecker implements ITickHandler{

    public static UniversalChecker instance = new UniversalChecker();
    public static final String LABEL = "MyTownUniversalCheckerTickHandler";

    private List<ModuleBase> modules;
    private List<ResidentBlockCoordsPair> blocks;

    public UniversalChecker() {
        modules = new ArrayList<ModuleBase>();
        blocks = new ArrayList<ResidentBlockCoordsPair>();

        modules.add(new VanillaModule());
        if(Info.isIc2Available())
            modules.add(new IC2Module());

        for(ModuleBase module : modules)
            module.load();
    }

    public boolean addToChecklist(ResidentBlockCoordsPair residentBlockPair) {
        return blocks.add(residentBlockPair);
    }


    public List<ModuleBase> getModules() {
        return modules;
    }

    public boolean addModule(ModuleBase module) {
        return modules.add(module);
    }

    public ModuleBase getModule(String modid) {
        for(ModuleBase module : modules) {
            if(module.getModID().equals(modid))
                return module;
        }
        return null;
    }

    @Override
    public void tickEnd(EnumSet<TickType> tickTypes, Object... objects) {
        if(blocks.size() == 0) return;
        Iterator<ResidentBlockCoordsPair> iterator = blocks.iterator();
        while(iterator.hasNext()) {
            ResidentBlockCoordsPair block = iterator.next();
            if(block.counter == 0) {
                World world = (World) objects[0];
                TileEntity te = world.getBlockTileEntity(block.x, block.y, block.z);
                if (te == null) {
                    System.out.println("TileEntity is null");
                    getModule(VanillaModule.ModID).check(block);
                } else {
                    for (ModuleBase module : modules) {
                        if (module.isEntityInstance(te))
                            module.check(te, block.owner);
                    }
                }
                iterator.remove();
            } else {
                block.counter--;
            }
        }
    }

    @Override
    public void tickStart(EnumSet<TickType> tickTypes, Object... objects) {

    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return LABEL;
    }
}
