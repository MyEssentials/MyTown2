package mytown.handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import forgeperms.api.ForgePermsAPI;
import mytown.config.Config;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.FakePlayer;

/**
 * @author Joe Goett
 */
public class SafemodeHandler {
    private static boolean safemode = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent ev) {
        if (safemode && ev.player instanceof EntityPlayerMP) {
            kickPlayer((EntityPlayerMP) ev.player);
        }
    }

    /**
     * Kicks all players that can't bypass safemode (mytown.adm.safemode)
     */
    public static void kickPlayers() {
        if (!SafemodeHandler.safemode)
            return;
        for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (!(obj instanceof EntityPlayerMP)) continue;
            SafemodeHandler.kickPlayer((EntityPlayerMP) obj);
        }
    }

    /**
     * Kicks the given EntityPlayerMP if they dont have mytown.adm.safemode
     *
     * @param pl
     */
    public static void kickPlayer(EntityPlayerMP pl) {
        if (!(pl instanceof FakePlayer) && !ForgePermsAPI.permManager.canAccess(pl.getCommandSenderName(), pl.worldObj.provider.getDimensionName(), "mytown.adm.safemode")) {
            pl.playerNetServerHandler.kickPlayerFromServer(Config.safeModeMsg);
        }
    }

    public static boolean isInSafemode() {
        return SafemodeHandler.safemode;
    }

    public static void setSafemode(boolean safemode) {
        SafemodeHandler.safemode = safemode;
        if (!SafemodeHandler.safemode) {
            kickPlayers();
        }
    }
}
