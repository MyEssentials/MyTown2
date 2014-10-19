package mytown.protection;

import cpw.mods.fml.common.registry.GameRegistry;
import mytown.MyTown;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.flag.FlagType;
import mytown.proxies.mod.ModProxies;
import mytown.util.BlockPos;
import mytown.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

/**
 * Created by AfterWind on 9/21/2014.
 * Protection for pipes for buildcraft
 */
public class BuildCraftTransportProtection extends Protection {

    @SuppressWarnings("unchecked")
    public BuildCraftTransportProtection() {
        activatedBlocks.add(GameRegistry.findBlock(ModProxies.BC_TRANSPORT_MOD_ID, "tile.pipeBlock"));
    }
}
