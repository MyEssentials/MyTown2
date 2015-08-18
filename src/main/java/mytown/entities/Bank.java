package mytown.entities;

import mytown.config.Config;
import mytown.proxies.LocalizationProxy;

public class Bank {

    private Town town;

    private int bankAmount = 0;
    private int daysNotPaid = 0;

    public Bank(Town town) {
        this.town = town;
    }

    public boolean makePayment(int amount) {
        if (bankAmount >= amount) {
            bankAmount -= amount;
            return true;
        }
        return false;
    }

    public void payUpkeep() {
        int amount = getNextPaymentAmount();
        if(makePayment(amount)) {
            daysNotPaid = 0;
            town.notifyEveryone(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.upkeep"));
        } else {
            daysNotPaid++;
            town.notifyEveryone(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.upkeep.failed", Config.upkeepTownDeletionDays - daysNotPaid));
        }
    }

    public void setDaysNotPaid(int days) {
        this.daysNotPaid = days;
    }

    public int getDaysNotPaid() {
        return this.daysNotPaid;
    }

    public void setBankAmount(int amount) {
        bankAmount = amount;
    }
    public int getBankAmount() {
        return this.bankAmount;
    }

    public int getNextPaymentAmount() {
        return (Config.costTownUpkeep + Config.costAdditionalUpkeep * town.townBlocksContainer.size()) * (1 + daysNotPaid);
    }
}
