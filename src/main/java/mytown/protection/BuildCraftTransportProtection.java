package mytown.protection;

import cpw.mods.fml.common.registry.GameRegistry;
import mytown.proxies.mod.ModProxies;

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
