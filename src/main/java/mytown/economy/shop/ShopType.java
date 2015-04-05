package mytown.economy.shop;

/**
 * Created by AfterWind on 4/2/2015.
 * Determines the type of shop.
 */
public enum ShopType {
    buy,
    sell,
    sellBuy;

    public boolean canBuy() {
        return this == buy || this == sellBuy;
    }
    public boolean canSell() {
        return this == sell || this == sellBuy;
    }

    @Override
    public String toString() {
        switch (this) {
            case buy:
                return "BUY";
            case sell:
                return "SELL";
            case sellBuy:
                return "BUY/SELL";
        }
        return super.toString();
    }

    public static ShopType fromString(String shopType) {
        try {
            if (valueOf(shopType) != null)
                return valueOf(shopType);
        } catch (IllegalArgumentException ex) {}
        if(shopType.equals("BUY")) {
            return buy;
        } else if(shopType.equals("SELL")) {
            return sell;
        } else if(shopType.equals("BUY/SELL")) {
            return sellBuy;
        }

        return null;
    }
}
