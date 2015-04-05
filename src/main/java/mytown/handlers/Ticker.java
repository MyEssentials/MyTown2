package mytown.handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mytown.MyTown;
import mytown.api.events.TownEvent;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.Utils;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.economy.EconomyUtils;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.economy.shop.Shop;
import mytown.util.Constants;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import mytown.economy.shop.ShopType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Calendar;

/**
 * @author Joe Goett
 */
public class Ticker {

    private boolean ticked = true;
    private int lastCalendarDay = -1;
    @SubscribeEvent
    public void onTickEvent(TickEvent.WorldTickEvent ev) {
        if(ev.side == Side.CLIENT)
            return;


        for(Resident res : MyTownUniverse.getInstance().getResidentsMap().values()) {
            res.tick();
        }

        if(Config.costTownUpkeep > 0 || Config.costAdditionalUpkeep > 0) {
            if (ev.phase == TickEvent.Phase.START) {
                if (ticked) {
                    if(lastCalendarDay != -1 && Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != lastCalendarDay) {
                        for (int i = 0; i < MyTownUniverse.getInstance().getTownsMap().size(); i++) {
                            Town town = MyTownUniverse.getInstance().getTownsMap().values().asList().get(i);
                            if (!(town instanceof AdminTown)) {
                                town.payUpkeep();
                                if(town.getDaysNotPaid() == Config.upkeepTownDeletionDays && Config.upkeepTownDeletionDays > 0) {
                                    MyTown.instance.log.info("Town " + town.getName() + " has been deleted because it didn't pay upkeep for " + Config.upkeepTownDeletionDays + " days.");
                                    DatasourceProxy.getDatasource().deleteTown(town);
                                } else {
                                    DatasourceProxy.getDatasource().updateTownBank(town, town.getBankAmount());
                                }
                            }
                        }
                        ticked = false;
                    }
                    lastCalendarDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                } else {
                    ticked = true;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent ev) {
        MyTownDatasource ds = DatasourceProxy.getDatasource();
        Resident res = ds.getOrMakeResident(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        } else {
            MyTown.instance.log.error("Didn't create resident for player %s (%s)", ev.player.getCommandSenderName(), ev.player.getPersistentID());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
        MyTownDatasource ds = DatasourceProxy.getDatasource();
        Resident res = ds.getOrMakeResident(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        }

    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent ev) {
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.player);
        res.checkLocationOnDimensionChanged(ev.player.chunkCoordX, ev.player.chunkCoordZ, ev.toDim);
    }

    @SubscribeEvent
    public void onEnterChunk(EntityEvent.EnteringChunk ev) {
        if (ev.entity == null || !(ev.entity instanceof EntityPlayer))
            return;
        checkLocationAndSendMap(ev);
    }

    private void checkLocationAndSendMap(EntityEvent.EnteringChunk ev) {
        if (ev.entity instanceof FakePlayer || ev.entity.worldObj.isRemote)
            return;
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entity);
        if (res == null) return; // TODO Log?
        // TODO Check Resident location

        res.checkLocation(ev.oldChunkX, ev.oldChunkZ, ev.newChunkX, ev.newChunkZ, ev.entity.dimension);

        Town lastTown = MyTownUtils.getTownAtPosition(ev.entity.dimension, ev.oldChunkX, ev.oldChunkZ);
        Town currTown = MyTownUtils.getTownAtPosition(ev.entity.dimension, ev.newChunkX, ev.newChunkZ);

        if (currTown != null && (lastTown == null || currTown != lastTown))
            TownEvent.fire(new TownEvent.TownEnterEvent(currTown, res));

        if (res.isMapOn()) {
            Formatter.sendMap(res);
        }
    }

    // Because I can
    @SubscribeEvent
    public void onUseHoe(UseHoeEvent ev) {
        if (ev.current.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
            ev.setCanceled(true);
        }

    }

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote)
            return;


        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();
        if (currentStack == null)
            return;
        if ((ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) && ev.entityPlayer.isSneaking()) {
            if (currentStack.getItem().equals(Items.wooden_hoe) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
                // For shift right clicking the selector, we may need it
            }
        }
        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK ) {
            if(currentStack.getItem().equals(Items.wooden_hoe) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
                Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
                Town town;
                //TODO: Verify permission

                NBTTagList lore = currentStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                String description = lore.getStringTagAt(0);

                if (description.equals(Constants.EDIT_TOOL_DESCRIPTION_PLOT)) {
                    if (res.isFirstPlotSelectionActive() && res.isSecondPlotSelectionActive()) {
                        ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.alreadySelected");
                    } else {
                        boolean result = res.selectBlockForPlot(ev.entityPlayer.dimension, ev.x, ev.y, ev.z);
                        if (result) {
                            if (!res.isSecondPlotSelectionActive()) {
                                ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.notification.plot.selectionStart");
                            } else {
                                ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.notification.plot.selectionEnd");
                            }
                        } else
                            ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.selectionFailed");

                    }
                } else if (description.equals(Constants.EDIT_TOOL_DESCRIPTION_BLOCK_WHITELIST)) {
                    town = MyTownUniverse.getInstance().getTownsMap().get(MyTownUtils.getTownNameFromLore(ev.entityPlayer));
                    Town townAt = MyTownUtils.getTownAtPosition(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
                    if (town == null || town != townAt) {
                        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.blockNotInTown"));
                    } else {
                        // If town is found then create of delete the block whitelist

                        FlagType flagType = FlagType.valueOf(MyTownUtils.getFlagNameFromLore(ev.entityPlayer));
                        ev.entityPlayer.setCurrentItemOrArmor(0, null);
                        BlockWhitelist bw = town.getBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, flagType);
                        if (bw == null) {
                            bw = new BlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, flagType);
                            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.perm.town.whitelist.added"));
                            DatasourceProxy.getDatasource().saveBlockWhitelist(bw, town);
                        } else {
                            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.perm.town.whitelist.removed"));
                            DatasourceProxy.getDatasource().deleteBlockWhitelist(bw, town);
                        }
                        ev.setCanceled(true);
                    }
                }
            } else if(currentStack.getItem().equals(Items.wooden_hoe) && currentStack.getDisplayName().equals(Constants.SIGN_SHOP_NAME)) {
                ForgeDirection direction = ForgeDirection.getOrientation(ev.face);
                int x = ev.x + direction.offsetX;
                int y = ev.y + direction.offsetY;
                int z = ev.z + direction.offsetZ;

                if(ev.world.getBlock(x, y, z) != Blocks.air)
                    return;

                if(direction == ForgeDirection.DOWN || ev.face == 1) {
                    int i1 = MathHelper.floor_double((double) ((ev.entityPlayer.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                    ev.world.setBlock(x, y, z, Blocks.standing_sign, i1, 3);
                } else {
                    ev.world.setBlock(x, y, z, Blocks.wall_sign, ev.face, 3);
                }

                TileEntitySign te = (TileEntitySign)ev.world.getTileEntity(x, y, z);

                NBTTagList tagList = currentStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                int amount = Integer.parseInt(tagList.getStringTagAt(0).split(" ")[1]);
                int price = Integer.parseInt(tagList.getStringTagAt(0).split(" ")[3]);
                ShopType shopType = ShopType.fromString(tagList.getStringTagAt(1).split(" ")[1]);
                String itemString = tagList.getStringTagAt(2).split(" ")[1];

                Shop shop =  new Shop(itemString, amount, price, shopType, ev.world.provider.dimensionId, x, y, z);
                DatasourceProxy.getDatasource().saveShop(shop);

                String[] signText = new String[4];
                signText[0] = EnumChatFormatting.BLACK + "[ " + shopType.toString() + " ]";
                for(int i = 0; i < (15 - signText[0].length()) / 2; i++)
                    signText[0] = " " + signText[0];

                signText[1] = (amount > 1 ? (amount + "x") : "") + EnumChatFormatting.DARK_BLUE;
                signText[1] += signText[1].length() + shop.itemStack.getDisplayName().length() > 15 ? shop.itemStack.getDisplayName().substring(0, 15 - signText[1].length()) : shop.itemStack.getDisplayName();
                for(int i = 0; i < (15 - signText[1].length()) / 2; i++)
                    signText[1] = " " + signText[1];

                signText[2] = "" + EnumChatFormatting.GOLD + price;
                signText[3] = Constants.SIGN_ID_TEXT + shop.db_ID;

                te.signText = signText;
            } else if(currentStack.getItem().equals(Items.wooden_hoe) && currentStack.getDisplayName().equals(Constants.PLOT_SELL_NAME)) {
                ForgeDirection direction = ForgeDirection.getOrientation(ev.face);
                int x = ev.x + direction.offsetX;
                int y = ev.y + direction.offsetY;
                int z = ev.z + direction.offsetZ;

                if(ev.world.getBlock(x, y, z) != Blocks.air)
                    return;

                NBTTagList tagList = currentStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                Town town  = MyTownUniverse.getInstance().getTown(tagList.getStringTagAt(0).split(" ")[1]);
                if(town == null)
                    return;
                Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
                int price = Integer.parseInt(tagList.getStringTagAt(1).split(" ")[1]);

                Plot plot = town.getPlotAtCoords(ev.world.provider.dimensionId, x, y, z);
                if(plot == null) {
                    res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.sell.notInPlot", town.getName()));
                    return;
                }
                if(!plot.hasOwner(res)) {
                    res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.notOwner"));
                    return;
                }

                if(direction == ForgeDirection.DOWN || ev.face == 1) {
                    int i1 = MathHelper.floor_double((double) ((ev.entityPlayer.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                    ev.world.setBlock(x, y, z, Blocks.standing_sign, i1, 3);
                } else {
                    ev.world.setBlock(x, y, z, Blocks.wall_sign, ev.face, 3);
                }

                TileEntitySign te = (TileEntitySign)ev.world.getTileEntity(x, y, z);

                String[] signText = new String[4];
                signText[0] = "";
                signText[1] = Constants.PLOT_SELL_IDENTIFIER;
                signText[2] = "" + EnumChatFormatting.GOLD + price;
                signText[3] = "";
                te.signText = signText;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if (VisualsTickHandler.getInstance().isBlockMarked(ev.x, ev.y, ev.z, ev.world.provider.dimensionId)) {
            // Cancel event if it's a border that has been broken
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent ev) {
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
        Block block = ev.world.getBlock(ev.x, ev.y, ev.z);

        // Shop and plot sale click verify
        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {

            if (block == Blocks.wall_sign || block == Blocks.standing_sign) {
                TileEntitySign te = (TileEntitySign) ev.world.getTileEntity(ev.x, ev.y, ev.z);

                if (te.signText[3].startsWith(Constants.SIGN_ID_TEXT)) {
                    if (ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && ev.entityPlayer.isSneaking() && Utils.isOp(ev.entityPlayer)) {
                        ev.world.setBlock(ev.x, ev.y, ev.z, Blocks.air);
                        DatasourceProxy.getDatasource().deleteShop(Integer.parseInt(te.signText[3].split(" ")[1]));
                    } else {
                        Shop shop = MyTownUniverse.getInstance().getShop(Integer.parseInt(te.signText[3].split(" ")[1]));

                        // Right click to sell
                        if(ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && shop.type.canSell()) {
                            if(MyTownUtils.takeItemFromPlayer(ev.entityPlayer, shop.itemStack, shop.getAmount())) {
                                EconomyUtils.giveMoneyToPlayer(ev.entityPlayer, shop.price);
                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.shop.sell.success", shop.getAmount(), shop.itemStack.getDisplayName(), shop.price, EconomyUtils.getCurrency(shop.price)));
                            } else {
                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.shop.sell.failed", shop.getAmount(), shop.itemStack.getDisplayName()));
                            }
                        // Left click to buy if shoptype is sellbuy and right click if not.
                        } else if(ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && shop.type == ShopType.sellBuy || ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && shop.type == ShopType.buy) {
                            if(EconomyUtils.takeMoneyFromPlayer(ev.entityPlayer, shop.price)) {
                                MyTownUtils.giveItemToPlayer(ev.entityPlayer, shop.itemStack, shop.getAmount());
                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.shop.buy.success", shop.getAmount(), shop.itemStack.getDisplayName(), shop.price, EconomyUtils.getCurrency(shop.price)));
                            } else {
                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.shop.buy.failed", shop.price, EconomyUtils.getCurrency(shop.price)));
                            }
                        }
                        ev.setCanceled(true);
                    }
                } else if(te.signText[1].equals(Constants.PLOT_SELL_IDENTIFIER)) {
                    if (ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && ev.entityPlayer.isSneaking() && Utils.isOp(ev.entityPlayer)) {
                        ev.world.setBlock(ev.x, ev.y, ev.z, Blocks.air);
                    } else if(ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                        Town town = MyTownUtils.getTownAtPosition(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
                        if(town != null) {
                            if(town.hasResident(res)) {
                                Plot plot = town.getPlotAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z);
                                if(plot != null) {
                                    if(!plot.hasOwner(res)) {
                                        if (town.canResidentMakePlot(res)) {
                                            int price = Integer.parseInt(te.signText[2].substring(2, te.signText[2].length()));
                                            if (EconomyUtils.takeMoneyFromPlayer(ev.entityPlayer, price)) {
                                                for(Resident resInPlot : plot.getOwners()) {
                                                    resInPlot.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.buy.oldOwner", plot.getName()));
                                                }
                                                for(Resident resInPlot : plot.getResidents()) {
                                                    DatasourceProxy.getDatasource().unlinkResidentFromPlot(resInPlot, plot);
                                                }
                                                DatasourceProxy.getDatasource().linkResidentToPlot(res, plot, true);
                                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.buy.newOwner", plot.getName()));
                                                ev.world.setBlock(ev.x, ev.y, ev.z, Blocks.air);
                                            } else {
                                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.shop.buy.failed", price, EconomyUtils.getCurrency(price)));
                                            }
                                        } else {
                                            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.limit", town.getMaxPlots()));
                                        }
                                    } else {
                                        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.sell.alreadyOwner"));
                                    }
                                }
                            } else {
                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.notInTown", town.getName()));
                            }
                        }
                    }
                    ev.setCanceled(true);
                }
            }
        }
    }

}
