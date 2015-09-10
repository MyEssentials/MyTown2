package mytown.datasource;

import com.mojang.authlib.GameProfile;
import java.util.*;
import myessentials.teleport.Teleport;
import myessentials.utils.PlayerUtils;
import mypermissions.api.command.CommandCompletion;
import mytown.MyTown;
import mytown.api.container.*;
import mytown.api.events.*;
import mytown.config.Config;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.VisualsHandler;
import mytown.proxies.DatasourceProxy;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import org.apache.logging.log4j.Logger;

// Referenced classes of package mytown.datasource:
//            MyTownDatasource

public class MyTownUniverse
{

    public MyTownUniverse()
    {
    }

    public final Town newTown(String name, Resident creator)
    {
        Town town = new Town(name);
        configureTown(town, creator);
        return town;
    }

    public final AdminTown newAdminTown(String name, Resident creator)
    {
        AdminTown town = new AdminTown(name);
        configureTown(town, creator);
        return town;
    }

    private void configureTown(Town town, Resident creator)
    {
        net.minecraft.world.WorldServer aworldserver[] = MinecraftServer.func_71276_C().field_71305_c;
        int i = aworldserver.length;
        for(int j = 0; j < i; j++)
        {
            World world = aworldserver[j];
            if(!instance.worlds.contains(Integer.valueOf(world.field_73011_w.field_76574_g)))
                getDatasource().saveWorld(world.field_73011_w.field_76574_g);
        }

        town.setSpawn(new Teleport(creator.getPlayer().field_71093_bK, (float)creator.getPlayer().field_70165_t, (float)creator.getPlayer().field_70163_u, (float)creator.getPlayer().field_70161_v, creator.getPlayer().field_71109_bG, creator.getPlayer().field_70726_aT));
        if(!getDatasource().saveTown(town))
            throw new CommandException("Failed to save Town", new Object[0]);
        TownBlock block = newBlock(creator.getPlayer().field_71093_bK, (int)creator.getPlayer().field_70165_t >> 4, (int)creator.getPlayer().field_70161_v >> 4, false, Config.costAmountClaim, town);
        if(instance.blocks.contains(creator.getPlayer().field_71093_bK, (int)creator.getPlayer().field_70165_t >> 4, (int)creator.getPlayer().field_70161_v >> 4))
            throw new MyTownCommandException("mytown.cmd.err.claim.already", new Object[0]);
        getDatasource().saveBlock(block);
        FlagType aflagtype[] = FlagType.values();
        int k = aflagtype.length;
        for(int l = 0; l < k; l++)
        {
            FlagType type = aflagtype[l];
            if(type.canTownsModify())
                getDatasource().saveFlag(new Flag(type, type.getDefaultValue()), town);
        }

        if(!(town instanceof AdminTown))
        {
            Rank rank;
            for(Iterator iterator = Rank.defaultRanks.iterator(); iterator.hasNext(); getDatasource().saveRank(rank))
            {
                Rank template = (Rank)iterator.next();
                rank = new Rank(template.getName(), town, template.getType());
                rank.permissionsContainer.addAll(template.permissionsContainer);
            }

            if(!getDatasource().linkResidentToTown(creator, town, town.ranksContainer.getMayorRank()))
                MyTown.instance.LOG.error("Problem linking resident {} to town {}", new Object[] {
                    creator.getPlayerName(), town.getName()
                });
            getDatasource().saveTownBank(town.bank);
        }
        TownEvent.fire(new mytown.api.events.TownEvent.TownCreateEvent(town));
    }

    public final TownBlock newBlock(int dim, int x, int z, boolean isFarClaim, int pricePaid, Town town)
    {
        if(!worlds.contains(Integer.valueOf(dim)))
            getDatasource().saveWorld(dim);
        TownBlock block = new TownBlock(dim, x, z, isFarClaim, pricePaid, town);
        if(TownBlockEvent.fire(new mytown.api.events.TownBlockEvent.BlockCreateEvent(block)))
            return null;
        else
            return block;
    }

    public final Rank newRank(String name, Town town, mytown.entities.Rank.Type type)
    {
        Rank rank = new Rank(name, town, type);
        if(RankEvent.fire(new mytown.api.events.RankEvent.RankCreateEvent(rank)))
            return null;
        else
            return rank;
    }

    public final Resident newResident(UUID uuid, String playerName)
    {
        Resident resident = new Resident(uuid, playerName);
        if(ResidentEvent.fire(new mytown.api.events.ResidentEvent.ResidentCreateEvent(resident)))
            return null;
        else
            return resident;
    }

    public final Plot newPlot(String name, Town town, int dim, int x1, int y1, int z1, int x2, 
            int y2, int z2)
    {
        Plot plot = new Plot(name, town, dim, x1, y1, z1, x2, y2, z2);
        if(PlotEvent.fire(new mytown.api.events.PlotEvent.PlotCreateEvent(plot)))
            return null;
        else
            return plot;
    }

    public final Nation newNation(String name)
    {
        Nation nation = new Nation(name);
        if(NationEvent.fire(new mytown.api.events.NationEvent.NationCreateEvent(nation)))
            return null;
        else
            return nation;
    }

    public final Flag newFlag(FlagType type, Object value)
    {
        Flag flag = new Flag(type, value);
        return flag;
    }

    public Resident getOrMakeResident(UUID uuid, String playerName, boolean save)
    {
        Resident res = instance.residents.get(uuid);
        if(res == null)
        {
            res = instance.newResident(uuid, playerName);
            if(save && res != null && !getDatasource().saveResident(res))
                return null;
        }
        return res;
    }

    public Resident getOrMakeResident(UUID uuid, String playerName)
    {
        return getOrMakeResident(uuid, playerName, true);
    }

    public Resident getOrMakeResident(UUID uuid)
    {
        return getOrMakeResident(uuid, PlayerUtils.getUsernameFromUUID(uuid));
    }

    public Resident getOrMakeResident(EntityPlayer player)
    {
        return getOrMakeResident(player.getPersistentID(), player.getDisplayName());
    }

    public Resident getOrMakeResident(Entity e)
    {
        if(e instanceof EntityPlayer)
            return getOrMakeResident((EntityPlayer)e);
        else
            return null;
    }

    public Resident getOrMakeResident(ICommandSender sender)
    {
        if(sender instanceof EntityPlayer)
            return getOrMakeResident((EntityPlayer)sender);
        else
            return null;
    }

    public Resident getOrMakeResident(String username)
    {
        GameProfile profile = MinecraftServer.func_71276_C().func_152358_ax().func_152655_a(username);
        return profile != null ? getOrMakeResident(profile.getId(), profile.getName()) : null;
    }

    public final void renameTown(Town town, String newName)
    {
        String oldName = town.getName();
        town.rename(newName);
        getDatasource().saveTown(town);
        CommandCompletion.removeCompletion("townCompletion", oldName);
        CommandCompletion.removeCompletion("townCompletionAndAll", oldName);
        CommandCompletion.addCompletion("townCompletion", newName);
        CommandCompletion.addCompletion("townCompletionAndAll", newName);
    }

    public final void addResident(Resident res)
    {
        residents.add(res);
        CommandCompletion.addCompletion("residentCompletion", res.getPlayerName());
    }

    public final void addTown(Town town)
    {
        towns.add(town);
        CommandCompletion.addCompletion("townCompletionAndAll", town.getName());
        CommandCompletion.addCompletion("townCompletion", town.getName());
    }

    public final void addTownBlock(TownBlock block)
    {
        blocks.add(block);
    }

    public final void addRank(Rank rank)
    {
        ranks.add(rank);
        CommandCompletion.addCompletion("rankCompletion", rank.getName());
    }

    public final void addPlot(Plot plot)
    {
        for (int x = plot.getStartChunkX(); x <= plot.getEndChunkX(); x++)
        {
            for (int z = plot.getStartChunkZ(); z <= plot.getEndChunkZ(); z++)
            {
                TownBlock b = blocks.get(plot.getDim(), x, z);
                if (b != null)
                }
                    b.plotsContainer.add(plot);
                }
            }
        }

        plots.add(plot);
        plot.checkForSellSign();
        CommandCompletion.addCompletion("plotCompletion", plot.getName());
    }

    public final void addBank(Bank bank)
    {
        banks.add(bank);
    }

    public final void addWorld(int dim)
    {
        worlds.add(Integer.valueOf(dim));
    }

    public final void removeResident(Resident res)
    {
        residents.remove(res);
        CommandCompletion.removeCompletion("residentCompletion", res.getPlayerName());
    }

    public final void removeTown(Town town)
    {
        towns.remove(town);
        VisualsHandler.instance.unmarkBlocks(town);
        CommandCompletion.removeCompletion("townCompletionAndAll", town.getName());
        CommandCompletion.removeCompletion("townCompletion", town.getName());
    }

    public final void removeTownBlock(TownBlock block)
    {
        blocks.remove(block);
    }

    public final void removeRank(Rank rank)
    {
        ranks.remove(rank);
    }

    public final void removePlot(Plot plot)
    {
        plots.remove(plot);
        boolean removeFromCompletionMap = true;
        Iterator iterator = plots.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            Plot p = (Plot)iterator.next();
            if(p.getName().equals(plot.getName()))
                removeFromCompletionMap = false;
        } while(true);
        if(removeFromCompletionMap)
            CommandCompletion.removeCompletion("plotCompletion", plot.getName());
        VisualsHandler.instance.unmarkBlocks(plot);
    }

    public final void removeWorld(int dim)
    {
        worlds.remove(Integer.valueOf(dim));
    }

    private MyTownDatasource getDatasource()
    {
        return DatasourceProxy.getDatasource();
    }

    public static final MyTownUniverse instance = new MyTownUniverse();
    public final ResidentsContainer residents = new ResidentsContainer();
    public final TownsContainer towns = new TownsContainer();
    public final TownBlocksContainer blocks = new TownBlocksContainer();
    public final PlotsContainer plots = new PlotsContainer();
    public final RanksContainer ranks = new RanksContainer();
    public final BanksContainer banks = new BanksContainer();
    public final List worlds = new ArrayList();

}
