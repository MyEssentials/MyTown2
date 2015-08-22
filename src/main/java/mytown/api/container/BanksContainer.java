package mytown.api.container;

import mytown.entities.Bank;
import mytown.entities.Town;

import java.util.ArrayList;

public class BanksContainer extends ArrayList<Bank> {

    public Bank get(Town town) {
        for(Bank bank : this) {
            if(bank.getTown() == town) {
                return bank;
            }
        }
        return null;
    }

    public boolean contains(Town town) {
        for(Bank bank : this) {
            if(bank.getTown() == town) {
                return true;
            }
        }
        return false;
    }
}
