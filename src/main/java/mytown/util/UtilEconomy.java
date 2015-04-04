package mytown.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public class UtilEconomy implements IEconManager{
	Class<?> c;
	Object o;
	UUID uuid;
	
	public UtilEconomy(UUID uuid){
		this.uuid=uuid;
		try {
			c = Class.forName(EcoClass);
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		try {
			 o= c.newInstance();
		} catch (InstantiationException e1) {e1.printStackTrace();} catch (IllegalAccessException e1) {e1.printStackTrace();}
	}
	/**
	 * use this only to get the currency used or methods where you dont need a player
	 */
	public UtilEconomy(){		
	}

	static String EcoClass = "com.forgeessentials.economy.WalletHandler";

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

	@Override
	public int getWallet() {	
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = new Class[1];
		paramTypes[0]=UUID.class;
		int money=0;
			try {
				money = (Integer) c.getDeclaredMethod("getWallet", paramTypes).invoke(o, uuid);
			} catch (IllegalAccessException e) {e.printStackTrace();} catch (IllegalArgumentException e){e.printStackTrace();} catch (InvocationTargetException e) {e.printStackTrace();} catch (NoSuchMethodException e) {e.printStackTrace();} catch (SecurityException e) {e.printStackTrace();}
		
			return money;
	}

	@Override
	public String getMoneyString() {	
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = new Class[1];
		paramTypes[0]=UUID.class;
		String money="$";

					try {
						money = (String) c.getDeclaredMethod("getMoneyString", paramTypes).invoke(o, uuid);
					} catch (IllegalAccessException e) {e.printStackTrace();} catch (IllegalArgumentException e){e.printStackTrace();} catch (InvocationTargetException e) {e.printStackTrace();} catch (NoSuchMethodException e) {e.printStackTrace();} catch (SecurityException e) {e.printStackTrace();}		
			return money;
	}

	@Override
	public void addToWallet(int amountToAdd) {
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = new Class[2];
		paramTypes[0]=Integer.TYPE;
		paramTypes[1]=UUID.class;
		
		try {
			c.getDeclaredMethod("addToWallet", paramTypes).invoke(o, amountToAdd, uuid);
		} catch (IllegalAccessException e) {e.printStackTrace();} catch (IllegalArgumentException e){e.printStackTrace();} catch (InvocationTargetException e) {e.printStackTrace();} catch (NoSuchMethodException e) {e.printStackTrace();} catch (SecurityException e) {e.printStackTrace();}		
			
		
	}

	@Override
	public boolean removeFromWallet(int amountToSubtract) {
		boolean result = false;
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = new Class[2];
		paramTypes[0]=Integer.TYPE;
		paramTypes[1]=UUID.class;
		
		try {
			result = (Boolean) c.getDeclaredMethod("removeFromWallet", paramTypes).invoke(o, amountToSubtract, uuid);
		} catch (IllegalAccessException e) {e.printStackTrace();} catch (IllegalArgumentException e){e.printStackTrace();} catch (InvocationTargetException e) {e.printStackTrace();} catch (NoSuchMethodException e) {e.printStackTrace();} catch (SecurityException e) {e.printStackTrace();}		
			return result;
	}

	@Override
	public void setWallet(int setAmount, EntityPlayer player) {
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = new Class[2];
		paramTypes[0]=Integer.TYPE;
		paramTypes[1]=EntityPlayer.class;
		
		try {
			c.getDeclaredMethod("setWallet", paramTypes).invoke(o, setAmount, player);
		} catch (IllegalAccessException e) {e.printStackTrace();} catch (IllegalArgumentException e){e.printStackTrace();} catch (InvocationTargetException e) {e.printStackTrace();} catch (NoSuchMethodException e) {e.printStackTrace();} catch (SecurityException e) {e.printStackTrace();}		
			
		
	}

	/**
	 * this is broken for some reason... i think
	 */
	@Override
	public String currency(int amount) {
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = new Class[1];
		paramTypes[0]=Integer.TYPE;
		String money="$";
		try {
			money = (String) c.getDeclaredMethod("currency", paramTypes).invoke(o, amount);
		} catch (IllegalAccessException e) {e.printStackTrace();} catch (IllegalArgumentException e){e.printStackTrace();} catch (InvocationTargetException e) {e.printStackTrace();} catch (NoSuchMethodException e) {e.printStackTrace();} catch (SecurityException e) {e.printStackTrace();}		
		return money;
	}

	@Override
	public void save() {
		@SuppressWarnings("rawtypes")
		Class[] paramTypes = new Class[0];
					try {
						c.getDeclaredMethod("save", paramTypes).invoke(o);
					} catch (IllegalAccessException e) {e.printStackTrace();} catch (IllegalArgumentException e){e.printStackTrace();} catch (InvocationTargetException e) {e.printStackTrace();} catch (NoSuchMethodException e) {e.printStackTrace();} catch (SecurityException e) {e.printStackTrace();}		
		
	}

	@Override
	public Map<String, Integer> getItemTables() {
		// TODO Auto-generated method stub
		return null;
	}
}
