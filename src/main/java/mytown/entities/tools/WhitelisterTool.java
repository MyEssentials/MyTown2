package mytown.entities.tools;

import myessentials.chat.api.ChatManager;
import myessentials.entities.api.BlockPos;
import myessentials.entities.api.tool.Tool;
import myessentials.entities.api.tool.ToolManager;
import myessentials.localization.api.LocalManager;
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

    private Resident owner;
    private FlagType flagType = FlagType.ACCESS;

    public WhitelisterTool(Resident owner) {
        super(owner.getPlayer(), LocalManager.get("myessentials.tool.name", LocalManager.get("mytown.tool.whitelister.name")).getLegacyFormattedText()[0]);
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
        return LocalManager.get("mytown.tool.whitelister.description", flagType == null ? LocalManager.get("mytown.tool.whitelister.removal").getUnformattedText() : flagType.toString()).getLegacyFormattedText();
    }

    @Override
    public void onShiftRightClick() {
        if(flagType == FlagType.getWhitelistable().get(FlagType.getWhitelistable().size() - 1)) {
            flagType = null;
            updateDescription();
            ChatManager.send(owner.getPlayer(), "myessentials.tool.mode",
                    LocalManager.get("mytown.tool.whitelister.property"),
                    LocalManager.get("mytown.tool.whitelister.removal").getUnformattedText());
        } else {
            if(flagType == null) {
                flagType = FlagType.getWhitelistable().get(0);
            } else {
                flagType = FlagType.getWhitelistable().get(FlagType.getWhitelistable().indexOf(flagType) + 1);
            }
            updateDescription();
            ChatManager.send(owner.getPlayer(), "myessentials.tool.mode",
                    LocalManager.get("mytown.tool.whitelister.property"),
                    flagType.name);
        }
    }

    protected boolean hasPermission(Town town, BlockPos bp) {
        if(town == null) {
            ChatManager.send(owner.getPlayer(), "mytown.cmd.err.notInTown", owner.townsContainer.getMainTown());
            return false;
        }

        //TODO: Switch to using proper permission strings
        if(!(town.residentsMap.get(owner).getName().equals("Assistant") || town.residentsMap.get(owner).getName().equals("Mayor"))) {
            Plot plot = town.plotsContainer.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
            if(plot == null || !plot.ownersContainer.contains(owner)) {
                ChatManager.send(owner.getPlayer(), "mytown.cmd.err.perm.whitelist.noPermssion");
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
                ChatManager.send(owner.getPlayer(), "mytown.notification.perm.town.whitelist.removed");
            }
        }
    }

    private void addWhitelists(FlagType flagType, Town town, int dim, int x, int y, int z) {
        BlockWhitelist bw = town.blockWhitelistsContainer.get(dim, x, y, z, flagType);
        if (bw == null) {
            bw = new BlockWhitelist(dim, x, y, z, flagType);
            ChatManager.send(owner.getPlayer(), "mytown.notification.perm.town.whitelist.added");
            MyTown.instance.datasource.saveBlockWhitelist(bw, town);
        } else {
            ChatManager.send(owner.getPlayer(), "mytown.notification.perm.town.whitelist.already");
        }
    }
}
