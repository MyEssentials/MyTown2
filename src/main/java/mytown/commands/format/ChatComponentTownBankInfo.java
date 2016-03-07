package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import mytown.MyTown;
import mytown.entities.Bank;
import mytown.proxies.EconomyProxy;

public class ChatComponentTownBankInfo extends ChatComponentContainer {
    public ChatComponentTownBankInfo(Bank bank) {
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.bank.info.balance", EconomyProxy.getCurrency(bank.getAmount())));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.bank.info.balance", EconomyProxy.getCurrency(bank.getNextPaymentAmount())));
    }
}
