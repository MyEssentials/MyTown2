package mytown.protection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.MyTown;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * Created by AfterWind on 9/22/2014.
 * Protection for Botania mod
 */
public class BotaniaProtection extends Protection {

    private Class<? extends Item> clsTerraPick;

    @SuppressWarnings("unchecked")
    public BotaniaProtection() {
        isHandlingEvents = true;
        try {
            clsTerraPick = (Class<? extends Item>) Class.forName("vazkii.botania.common.item.equipment.tool.ItemTerraPick");
        } catch (Exception e) {
            MyTown.instance.log.error("Failed to load Botania classes!");
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onPlayerBreaks(PlayerInteractEvent ev) {
        try {
            if (ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                if (ev.entityPlayer.getHeldItem() != null && clsTerraPick.isAssignableFrom(ev.entityPlayer.getHeldItem().getItem().getClass())) {

                    boolean isEnabled = ev.entityPlayer.getHeldItem().getTagCompound().getBoolean("enabled");

                    // Basically checking if pick is enabled for area mode
                    if (isEnabled) {
                        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);

                        ForgeDirection direction = ForgeDirection.getOrientation(ev.face);
                        boolean doX = direction.offsetX == 0;
                        boolean doZ = direction.offsetZ == 0;
                        int level = getLevel(ev.entityPlayer.getHeldItem());
                        int range = Math.max(0, level - 1);

                        // DEV:
                        //MyTown.instance.log.info("Got range: " + range);

                        List<Town> towns = Utils.getTownsInRange(ev.world.provider.dimensionId, ev.x, ev.z, doX ? range : 0, doZ ? range : 0);

                        for (Town town : towns) {
                            boolean breakFlag = (Boolean) town.getValue(FlagType.breakBlocks);
                            if (!breakFlag && town.checkPermission(res, FlagType.breakBlocks)) {
                                ev.setCanceled(true);
                                res.sendMessage(FlagType.breakBlocks.getLocalizedProtectionDenial());
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyTown.instance.log.error("Failed to call methods or some other nasty error!");
        }
    }

    /**
     * @param stack
     * @return
     * @author Vazkii
     */
    public int getLevel(ItemStack stack) {
        int[] LEVELS = new int[]{
                0, 10000, 1000000, 10000000, 100000000, 1000000000
        };

        int mana = stack.getTagCompound().getInteger("mana");
        for (int i = LEVELS.length - 1; i > 0; i--)
            if (mana >= LEVELS[i])
                return i;
        return 0;
    }


}
