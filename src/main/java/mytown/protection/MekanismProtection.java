package mytown.protection;

import mytown.MyTown;
import net.minecraft.entity.Entity;

/**
 * Created by AfterWind on 10/19/2014.
 * Protection for Mekanism mod
 */
public class MekanismProtection extends Protection {
    @SuppressWarnings("unchecked")
    public MekanismProtection() {
        try {
            Class<? extends Entity> clsObsidianTNT = (Class<? extends Entity>)Class.forName("mekanism.common.entity.EntityObisidianTNT");
            explosiveBlocks.add(clsObsidianTNT);
        } catch (Exception e) {
            MyTown.instance.log.info("Failed to load Mekanism classes!");
        }
    }
}
