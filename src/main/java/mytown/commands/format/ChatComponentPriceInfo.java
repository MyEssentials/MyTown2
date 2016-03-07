package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import mytown.MyTown;
import mytown.config.Config;
import mytown.proxies.EconomyProxy;

public class ChatComponentPriceInfo extends ChatComponentContainer {
    public ChatComponentPriceInfo() {
        // Add header
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.1"));

        // Add data
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.2", EconomyProxy.getCurrency(Config.instance.costAmountMakeTown.get())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.3", EconomyProxy.getCurrency(Config.instance.costAmountClaim.get())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.4", EconomyProxy.getCurrency(Config.instance.costAdditionClaim.get())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.5", EconomyProxy.getCurrency(Config.instance.costAmountClaimFar.get())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.6", EconomyProxy.getCurrency(Config.instance.costAmountSpawn.get())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.7", EconomyProxy.getCurrency(Config.instance.costAmountSetSpawn.get())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.8", EconomyProxy.getCurrency(Config.instance.costAmountOtherSpawn.get())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.9", EconomyProxy.getCurrency(Config.instance.costTownUpkeep.get())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.prices.info.10", EconomyProxy.getCurrency(Config.instance.costAdditionalUpkeep.get())));
    }
}
