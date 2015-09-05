package mytown.entities.blocks;

import mytown.datasource.MyTownUniverse;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.EconomyProxy;
import net.minecraft.util.EnumChatFormatting;

public class SellSign extends Sign {

    private static final String IDENTIFIER = EnumChatFormatting.DARK_BLUE + "Plot Sale";
    private static final String DESCRIPTION_PRICE = EnumChatFormatting.GOLD.toString();
    private static final String DESCRIPTION_RESTRICTED = EnumChatFormatting.RED.toString() + "RESTRICTED";

    private int price;
    private boolean restricted;
    private Plot plot;

    public SellSign(int dim, int x, int y, int z, int face, Resident owner, int price, boolean restricted) {
        super(dim, x, y, z, face, owner, IDENTIFIER);
        this.price = price;
        this.restricted = restricted;
        this.plot = MyTownUniverse.instance.plots.get(dim, x, y, z);
        if(!exists()) {
            createSignBlock(DESCRIPTION_PRICE + " " + price, "", restricted ? DESCRIPTION_RESTRICTED : "");
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
}
