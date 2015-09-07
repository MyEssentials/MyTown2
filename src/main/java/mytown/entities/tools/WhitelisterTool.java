package mytown.entities.tools;

import mytown.entities.BlockWhitelist;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.util.MyTownUtils;
import net.minecraft.init.Items;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * A tool that selects a block to add it to whitelists for protection.
 */
public class WhitelisterTool extends Tool {

    private static final String NAME = EnumChatFormatting.BLUE + "Whitelister";
    private static final String DESCRIPTION_HEADER_1 = EnumChatFormatting.DARK_AQUA + "Select block for bypassing protection.";
    private static final String DESCRIPTION_HEADER_2 = EnumChatFormatting.DARK_AQUA + "Shift right-click air to change flag.";
    private static final String DESCRIPTION_FLAG = EnumChatFormatting.DARK_AQUA + "Flag: " + FlagType.ACCESS.toString().toLowerCase();

    private static final List<FlagType> whitelistableFlags = new ArrayList<FlagType>();

    static {
        for(FlagType flagType : FlagType.values()) {
            if(flagType.isWhitelistable())
                whitelistableFlags.add(flagType);
        }
    }

    public WhitelisterTool(Resident owner) {
        super(owner, NAME);
        giveItemStack(createItemStack(Items.wooden_hoe, DESCRIPTION_HEADER_1, DESCRIPTION_HEADER_2, DESCRIPTION_FLAG));
    }

    @Override
    public void onItemUse(int dim, int x, int y, int z, int face) {
        Town town = MyTownUtils.getTownAtPosition(dim, x >> 4, z >> 4);

        if(!hasPermission(town, dim, x, y, z))
            return;

        // If town is found then create or delete the block whitelist
        FlagType flagType = getFlagFromLore();
        //ev.entityPlayer.setCurrentItemOrArmor(0, null);
        if (flagType == null) {
            removeWhitelists(town, dim, x, y, z);
        } else {
            addWhitelists(flagType, town, dim, x, y, z);
        }
        deleteItemStack();

    }

    @Override
    public void onShiftRightClick() {
        FlagType currentFlag = getFlagFromLore();
        if(currentFlag == whitelistableFlags.get(whitelistableFlags.size() - 1)) {
            setDescription(EnumChatFormatting.RED + "WHITELIST REMOVAL", 2);
            owner.sendMessage(getLocal().getLocalization("mytown.notification.tool.mode", "mode", "WHITELIST REMOVAL"));
        } else {
            setDescription(EnumChatFormatting.DARK_AQUA + "Flag: " + whitelistableFlags.get(whitelistableFlags.indexOf(currentFlag) + 1).toString().toLowerCase(), 2);
            owner.sendMessage(getLocal().getLocalization("mytown.notification.tool.mode", "flagType", currentFlag));
        }
    }

    @Override
    protected boolean hasPermission(Town town, int dim, int x, int y, int z) {
        if(town == null) {
            owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.notInTown", owner.townsContainer.getMainTown().getName()));
            return false;
        }

        //TODO: Switch to using proper permission strings
        if(!(town.residentsMap.get(owner).getName().equals("Assistant") || town.residentsMap.get(owner).getName().equals("Mayor"))) {
            Plot plot = town.plotsContainer.get(dim, x, y, z);
            if(plot == null || !plot.ownersContainer.contains(owner)) {
                owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.perm.whitelist"));
                return false;
            }
        }

        return true;
    }

    private void removeWhitelists(Town town, int dim, int x, int y, int z) {
        for (FlagType flagType : whitelistableFlags) {
            BlockWhitelist bw = town.blockWhitelistsContainer.get(dim, x, y, z, flagType);
            if (bw != null) {
                DatasourceProxy.getDatasource().deleteBlockWhitelist(bw, town);
                owner.sendMessage(getLocal().getLocalization("mytown.notification.perm.town.whitelist.removed"));
            }
        }
    }

    private void addWhitelists(FlagType flagType, Town town, int dim, int x, int y, int z) {
        BlockWhitelist bw = town.blockWhitelistsContainer.get(dim, x, y, z, flagType);
        if (bw == null) {
            bw = new BlockWhitelist(dim, x, y, z, flagType);
            owner.sendMessage(getLocal().getLocalization("mytown.notification.perm.town.whitelist.added"));
            DatasourceProxy.getDatasource().saveBlockWhitelist(bw, town);
        } else {
            owner.sendMessage(getLocal().getLocalization("mytown.notification.perm.town.whitelist.already"));
        }
    }

    private FlagType getFlagFromLore() {
        String flagLore = getDescription(1);
        for(FlagType flagType : whitelistableFlags) {
            if (flagLore.contains(flagType.toString().toLowerCase())) {
                return flagType;
            }
        }
        return null;
    }
}
