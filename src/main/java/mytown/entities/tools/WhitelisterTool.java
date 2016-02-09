package mytown.entities.tools;

import myessentials.entities.api.BlockPos;
import myessentials.entities.api.tool.Tool;
import myessentials.entities.api.tool.ToolManager;
import mytown.MyTown;
import mytown.entities.BlockWhitelist;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.util.MyTownUtils;

/**
 * A tool that selects a block to add it to whitelists for protection.
 */
public class WhitelisterTool extends Tool {

    private static final String NAME = MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.name").getUnformattedTextForChat();
    private static final String DESCRIPTION_HEADER_1 = MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.description.header1").getUnformattedTextForChat();
    private static final String DESCRIPTION_HEADER_2 = MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.description.header2").getUnformattedTextForChat();
    private static final String DESCRIPTION_FLAG = MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.description.flag").getUnformattedTextForChat() + " ";
    private static final String DESCRIPTION_FLAG_REMOVAL = MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.description.removal").getUnformattedTextForChat();

    private Resident owner;
    private FlagType flagType = FlagType.ACCESS;

    public WhitelisterTool(Resident owner) {
        super(owner.getPlayer(), NAME);
        this.owner = owner;
    }

    @Override
    public void onItemUse(BlockPos bp, int face) {
        Town town = MyTownUtils.getTownAtPosition(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);

        if(!hasPermission(town, bp)) {
            return;
        }

        if (flagType == null) {
            removeWhitelists(town, bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
        } else {
            addWhitelists(flagType, town, bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
        }
        ToolManager.instance.remove(this);
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
                DESCRIPTION_HEADER_1,
                DESCRIPTION_HEADER_2,
                DESCRIPTION_FLAG + (flagType == null ? DESCRIPTION_FLAG_REMOVAL : flagType.name)
        };
    }

    @Override
    public void onShiftRightClick() {
        if(flagType == FlagType.getWhitelistable().get(FlagType.getWhitelistable().size() - 1)) {
            flagType = null;
            updateDescription();
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.tool.mode",
                    MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.mode"),
                    MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.mode.removal")
            ));
        } else if(flagType == null) {
            flagType = FlagType.getWhitelistable().get(0);
            updateDescription();
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.tool.mode",
                    MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.mode.flagType"),
                    flagType.name
            ));
        } else {
            flagType = FlagType.getWhitelistable().get(FlagType.getWhitelistable().indexOf(flagType) + 1);
            updateDescription();
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.tool.mode",
                    MyTown.instance.LOCAL.getLocalization("mytown.tool.whitelister.mode.flagType"),
                    flagType.name
            ));
        }
    }

    protected boolean hasPermission(Town town, BlockPos bp) {
        if(town == null) {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.notInTown", owner.townsContainer.getMainTown().getName()));
            return false;
        }

        //TODO: Switch to using proper permission strings
        if(!(town.residentsMap.get(owner).getName().equals("Assistant") || town.residentsMap.get(owner).getName().equals("Mayor"))) {
            Plot plot = town.plotsContainer.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
            if(plot == null || !plot.ownersContainer.contains(owner)) {
                owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.perm.whitelist.noPermssion"));
                return false;
            }
        }

        return true;
    }

    private void removeWhitelists(Town town, int dim, int x, int y, int z) {
        for (FlagType flagType : FlagType.getWhitelistable()) {
            BlockWhitelist bw = town.blockWhitelistsContainer.get(dim, x, y, z, flagType);
            if (bw != null) {
                MyTown.instance.datasource.deleteBlockWhitelist(bw, town);
                owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.perm.town.whitelist.removed"));
            }
        }
    }

    private void addWhitelists(FlagType flagType, Town town, int dim, int x, int y, int z) {
        BlockWhitelist bw = town.blockWhitelistsContainer.get(dim, x, y, z, flagType);
        if (bw == null) {
            bw = new BlockWhitelist(dim, x, y, z, flagType);
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.perm.town.whitelist.added"));
            MyTown.instance.datasource.saveBlockWhitelist(bw, town);
        } else {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.perm.town.whitelist.already"));
        }
    }
}
