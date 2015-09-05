package mytown.entities.blocks;

import mytown.MyTown;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.EconomyProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class SellSign extends Sign {

    private static final String IDENTIFIER = EnumChatFormatting.DARK_BLUE + "Plot Sale";
    private static final String DESCRIPTION_OWNER = EnumChatFormatting.BLUE + "by ";
    private static final String DESCRIPTION_PRICE = EnumChatFormatting.GOLD.toString();
    private static final String DESCRIPTION_RESTRICTED = EnumChatFormatting.RED.toString() + "RESTRICTED";

    private int price;
    private boolean restricted;
    private Plot plot;

    public SellSign(int dim, int x, int y, int z, int face, Resident owner, int price, boolean restricted) {
        super(dim, x, y, z, owner, IDENTIFIER);
        this.price = price;
        this.restricted = restricted;
        this.plot = MyTownUniverse.instance.plots.get(dim, x, y, z);
        if(!exists()) {
            createSignBlock(face, (DESCRIPTION_OWNER + owner.getPlayerName()).substring(0, 15), DESCRIPTION_PRICE + " " + price, restricted ? DESCRIPTION_RESTRICTED : "");
        }
    }

    @Override
    public void onRightClick(Resident resident) {
        if(restricted && !plot.getTown().residentsMap.containsKey(resident)) {
            resident.sendMessage(getLocal().getLocalization("mytown.cmd.err.notInTown", plot.getTown().getName()));
            return;
        }

        if(plot.ownersContainer.contains(resident)) {
            resident.sendMessage(getLocal().getLocalization("mytown.cmd.err.plot.sell.alreadyOwner"));
            return;
        }

        if(!plot.getTown().plotsContainer.canResidentMakePlot(resident)) {
            resident.sendMessage(getLocal().getLocalization("mytown.cmd.err.plot.limit", plot.getTown().plotsContainer.getMaxPlots()));
            return;
        }

        if (EconomyProxy.getEconomy().takeMoneyFromPlayer(resident.getPlayer(), price)) {
            for (Resident resInPlot : plot.ownersContainer) {
                resInPlot.sendMessage(getLocal().getLocalization("mytown.notification.plot.buy.oldOwner", plot.getName(), EconomyProxy.getCurrency(price)));
            }
            for (Resident resInPlot : plot.membersContainer) {
                DatasourceProxy.getDatasource().unlinkResidentFromPlot(resInPlot, plot);
            }
            for (Resident resInPlot : plot.ownersContainer) {
                DatasourceProxy.getDatasource().unlinkResidentFromPlot(resInPlot, plot);
            }
            if(!plot.getTown().residentsMap.containsKey(resident)) {
                DatasourceProxy.getDatasource().linkResidentToTown(resident, plot.getTown(), plot.getTown().ranksContainer.getDefaultRank());
            }
            DatasourceProxy.getDatasource().linkResidentToPlot(resident, plot, true);
            resident.sendMessage(getLocal().getLocalization("mytown.notification.plot.buy.newOwner", plot.getName()));
            plot.getTown().bank.addAmount(price);
            deleteSignBlock();
        } else {
            resident.sendMessage(getLocal().getLocalization("mytown.notification.plot.buy.failed", EconomyProxy.getCurrency(price)));
        }
    }

    @Override
    public void onShiftRightClick(Resident resident) {
        if(resident.equals(owner)) {
            deleteSignBlock();
        }
    }

    @Override
    public void deleteSignBlock() {
        super.deleteSignBlock();
        plot.signContainer.remove();
    }

    public static SellSign findSignAndCreate(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if(te == null || !(te instanceof TileEntitySign)) {
            return null;
        }
        TileEntitySign teSign = (TileEntitySign) te;
        if(teSign.signText[0].equals(IDENTIFIER)) {
            return new SellSign(world.provider.dimensionId, x, y, z, 0, getOwnerFromLore(teSign.signText[1]), getPriceFromLore(teSign.signText[2]), getRestrictedBooleanFromLore(teSign.signText[3]));
        }
        return null;
    }

    public static Resident getOwnerFromLore(String lore) {
        String username = lore.substring(DESCRIPTION_OWNER.length());
        MyTown.instance.LOG.info("Got: " + username);
        return MyTownUniverse.instance.getOrMakeResident(username);
    }

    public static int getPriceFromLore(String lore) {
        String priceString = lore.substring(DESCRIPTION_PRICE.length() + 1);
        MyTown.instance.LOG.info("Got: " + priceString);
        return Integer.parseInt(priceString);
    }

    public static boolean getRestrictedBooleanFromLore(String lore) {
        return lore.equals(DESCRIPTION_RESTRICTED);
    }
}
