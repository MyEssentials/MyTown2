package mytown.economy.forgeessentials;

import mytown.economy.IEconManager;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;
import java.util.UUID;

public class ForgeessentialsEconomy implements IEconManager {
    private Class<?> walletHandlerClass;
    private Object walletHandlerObject;
    private UUID uuid;

    public ForgeessentialsEconomy(UUID uuid){
        this.uuid=uuid;
        try {
            walletHandlerClass = Class.forName("com.forgeessentials.economy.WalletHandler");
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        }
        try {
            walletHandlerObject = walletHandlerClass.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ForgeessentialsEconomy() {
        try {
            walletHandlerClass = Class.forName("com.forgeessentials.economy.WalletHandler");
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        }
        try {
            walletHandlerObject = walletHandlerClass.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public int getWallet() {
        int money = 0;
        try {
            money = (Integer) walletHandlerClass.getDeclaredMethod("getWallet", UUID.class).invoke(walletHandlerObject, uuid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return money;
    }

    @Override
    public String getMoneyString() {
        String money="$";
        try {
            money = (String) walletHandlerClass.getDeclaredMethod("getMoneyString", UUID.class).invoke(walletHandlerObject, uuid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return money;
    }

    @Override
    public void addToWallet(int amountToAdd) {
        try {
            walletHandlerClass.getDeclaredMethod("addToWallet", Integer.TYPE, UUID.class).invoke(walletHandlerObject, amountToAdd, uuid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean removeFromWallet(int amountToSubtract) {
        boolean result = false;
        try {
            result = (Boolean) walletHandlerClass.getDeclaredMethod("removeFromWallet", Integer.TYPE, UUID.class).invoke(walletHandlerObject, amountToSubtract, uuid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public void setWallet(int setAmount, EntityPlayer player) {
        try {
            walletHandlerClass.getDeclaredMethod("setWallet", Integer.TYPE, EntityPlayer.class).invoke(walletHandlerObject, setAmount, player);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * this is broken for some reason... i think
     */
    @Override
    public String currency(int amount) {
        String money="$";
        try {
            money = (String) walletHandlerClass.getDeclaredMethod("currency", Integer.TYPE).invoke(walletHandlerObject, amount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return money;
    }

    @Override
    public void save() {
        try {
            walletHandlerClass.getDeclaredMethod("save").invoke(walletHandlerObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public Map<String, Integer> getItemTables() {
        // TODO Auto-generated method stub
        return null;
    }
}
