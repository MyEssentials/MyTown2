package mytown.new_protection;

import buildcraft.factory.TileQuarry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
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



        List<Getter>[] getters = new ArrayList[4];
        getters[0] = new ArrayList<Getter>();
        getters[0].add(new Getter("getBox", Getter.GetterType.methodObject));
        getters[0].add(new Getter("xMin", Getter.GetterType.fieldInt));

        getters[1] = new ArrayList<Getter>();
        getters[1].add(new Getter("getBox", Getter.GetterType.methodObject));
        getters[1].add(new Getter("zMin", Getter.GetterType.fieldInt));

        getters[2] = new ArrayList<Getter>();
        getters[2].add(new Getter("getBox", Getter.GetterType.methodObject));
        getters[2].add(new Getter("xMax", Getter.GetterType.fieldInt));

        getters[3] = new ArrayList<Getter>();
        getters[3].add(new Getter("getBox", Getter.GetterType.methodObject));
        getters[3].add(new Getter("zMax", Getter.GetterType.fieldInt));

        List<Segment> segments = new ArrayList<Segment>();
        segments.add(new SegmentTileEntity(TileQuarry.class, getters, IBlockModifier.Shape.rectangular));
        protections = new ArrayList<Protection>();
        protections.add(new Protection("BuildCraft|Factory", segments));

    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void testingOnly_tick(TickEvent.WorldTickEvent ev) {
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
