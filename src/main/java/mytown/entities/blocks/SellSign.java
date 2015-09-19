package mytown.entities.blocks;

import myessentials.Localization;
import myessentials.entities.BlockPos;
import myessentials.entities.sign.Sign;
import mytown.MyTown;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.EconomyProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumChatFormatting;

public class SellSign extends Sign {

    private static final String TITLE = "Plot Sale";
    private static final String DESCRIPTION_OWNER = EnumChatFormatting.BLUE + "by ";
    private static final String DESCRIPTION_PRICE = EnumChatFormatting.GOLD.toString();
    private static final String DESCRIPTION_RESTRICTED = EnumChatFormatting.RED.toString() + "RESTRICTED";

    private int price;
    private boolean restricted;
    private Resident owner;
    private Plot plot;

    public SellSign(BlockPos bp, int face, Resident owner, int price, boolean restricted) {
        this.bp = bp;
        this.price = price;
        this.restricted = restricted;
        this.plot = MyTownUniverse.instance.plots.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
        this.owner = owner;
        createSignBlock(owner.getPlayer(), bp, face);
    }

    public SellSign(TileEntitySign te) {
        this.bp = new BlockPos(te.xCoord, te.yCoord, te.zCoord, te.getWorldObj().provider.dimensionId);
        this.owner = getOwnerFromLore();
        this.price = getPriceFromLore();
        this.restricted = getRestrictedBooleanFromLore();
        this.plot = MyTownUniverse.instance.plots.get(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord);
    }

    @Override
    public void onRightClick(EntityPlayer player) {
        Resident resident = MyTownUniverse.instance.getOrMakeResident(player);
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
    protected String[] getText() {
        return new String[] {
                TITLE,
                DESCRIPTION_OWNER + owner.getPlayerName(),
                DESCRIPTION_PRICE + price,
                restricted ? DESCRIPTION_RESTRICTED : ""
        };
    }

    @Override
    public void onShiftRightClick(EntityPlayer player) {
        if(player.getPersistentID().equals(owner.getUUID())) {
            deleteSignBlock();
        }
    }

    public Localization getLocal() {
        return MyTown.instance.LOCAL;
    }

    public Resident getOwnerFromLore() {
        String username = getTileEntity().signText[1].substring(DESCRIPTION_OWNER.length());
        return MyTownUniverse.instance.getOrMakeResident(username);
    }

    public int getPriceFromLore() {
        String priceString = getTileEntity().signText[2].substring(DESCRIPTION_PRICE.length());
        return Integer.parseInt(priceString);
    }

    public boolean getRestrictedBooleanFromLore() {
        return getTileEntity().signText[3].equals(DESCRIPTION_RESTRICTED);
    }
}
