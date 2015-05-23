package mytown.api.events;

import net.minecraft.block.Block;

/**
 * Created by AfterWind on 10/14/2014.
 * Fired when a fluid is flowing in a town's block
 */
public class FluidFlowEvent {

    public int x, y, z;
    public Block block;

    public FluidFlowEvent(int x, int y, int z, Block block) {

    }

}
