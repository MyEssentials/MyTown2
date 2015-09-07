package mytown.entities;

import mytown.MyTown;
import mytown.config.Config;

public class Bank {

    private Town town;

    private int amount = 0;
    private int daysNotPaid = 0;

    public Bank(Town town) {
        this.town = town;
    }

    public boolean makePayment(int amount) {
        if (this.amount >= amount) {
            this.amount -= amount;
            return true;
        }
        return false;
    }

    public void payUpkeep() {
        int amount = getNextPaymentAmount();
        if(makePayment(amount)) {
            daysNotPaid = 0;
            town.notifyEveryone(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.upkeep"));
        } else {
            daysNotPaid++;
            town.notifyEveryone(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.upkeep.failed", Config.upkeepTownDeletionDays - daysNotPaid));
        }
    }

    public Town getTown() {
        return town;
    }

    public void setDaysNotPaid(int days) {
        this.daysNotPaid = days;
    }

    public int getDaysNotPaid() {
        return this.daysNotPaid;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    public int getNextPaymentAmount() {
        return (Config.costTownUpkeep + Config.costAdditionalUpkeep * town.townBlocksContainer.size()) * (1 + daysNotPaid);
    }
}
