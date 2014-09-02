package mytown.protection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mytown.MyTown;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 9/2/2014.
 * Class handling all the protections
 */
public class Protections {
    private List<Protection> protections;


    public static Protections instance = new Protections();
    public Protections() {
        MyTown.instance.log.info("Protections initializing started...");
        protections = new ArrayList<Protection>();
        protections.add(new VanillaProtection());
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent ev) {
        // Why does it return only a generic List? :S
        for(Entity entity : (List<Entity>)ev.world.loadedEntityList) {
            for(Protection prot : protections) {
                if(prot.checkEntity(entity)) {
                    MyTown.instance.log.info("Entity " + entity.toString() + " was ATOMICALLY DISINTEGRATED!");
                }
            }
        }
        for(TileEntity te : (List<TileEntity>)ev.world.loadedTileEntityList) {
            for(Protection prot : protections) {
                if(prot.checkTileEntity(te)) {
                    MyTown.instance.log.info("TileEntity " + te.toString() + " was ATOMICALLY DISINTEGRATED!");
                }
            }
        }
    }


}
