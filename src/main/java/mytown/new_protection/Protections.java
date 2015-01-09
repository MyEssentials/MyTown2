package mytown.new_protection;

import buildcraft.factory.TileQuarry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mytown.MyTown;
import mytown.new_protection.segment.Getter;
import mytown.new_protection.segment.IBlockModifier;
import mytown.new_protection.segment.Segment;
import mytown.new_protection.segment.SegmentTileEntity;
import mytown.util.MyTownUtils;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 1/1/2015.
 * Handles all the protections
 */
public class Protections {

    public static List<Protection> protections;

    @SuppressWarnings("unchecked")
    public static void testingOnly_init() {
        protections = new ArrayList<Protection>();

    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void testingOnly_tick(TickEvent.WorldTickEvent ev) {
        if(ev.side == Side.CLIENT)
            return;
        for(TileEntity te : (List<TileEntity>)ev.world.loadedTileEntityList) {
            for(Protection protection : protections) {
                if (protection.checkTileEntity(te)) {
                    MyTownUtils.dropAsEntity(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, new ItemStack(te.getBlockType(), 1, te.getBlockMetadata()));
                    te.getWorldObj().setBlock(te.xCoord, te.yCoord, te.zCoord, Blocks.air);
                    MyTown.instance.log.info("TileEntity " + te.toString() + " was ATOMICALLY DISINTEGRATED!");
                } else {
                    //MyTown.instance.log.info("NOT BOOM!");
                }
            }
        }
    }
}
