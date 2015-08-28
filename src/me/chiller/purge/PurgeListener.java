package me.chiller.purge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PurgeListener implements Listener
{
	public Map<String, Object[]> points = new HashMap<String, Object[]>();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		boolean hasPlayedBefore = PurgeHandler.getInstance().hasPlayedBefore(event.getPlayer().getName());
		
		PurgePlayer player = PurgeHandler.getInstance().getPlayer(event.getPlayer().getName());
		
		if (!hasPlayedBefore)
		{
			player.setMoney(PurgeMain.getInstance().getConfig().getInt("game.startmoney", 500));
			
			PurgeHandler.getInstance().savePlayers();
		}
		
		player.setupScoreboard();
		player.updateScoreboard();
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		Player player = (Player) event.getPlayer();
		
		if (PurgeHandler.getInstance().getShop().isViewingSign(player))
		{
			PurgeHandler.getInstance().getShop().getSign(player).closeInventory(player);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		
		if (PurgeHandler.getInstance().getShop().isViewingSign(player))
		{
			if (event.getRawSlot() == -999 || event.getRawSlot() >= 27)
			{
				if (event.isShiftClick() && event.getCurrentItem().getType() != Material.AIR)
				{
					//Sell whole stack
					
					event.setCancelled(true);
					
					if (inventoryContains(event.getView().getTopInventory(), event.getCurrentItem()))
					{
						PurgeSign sign = PurgeHandler.getInstance().getShop().getSign(player);
						PurgePlayer pPlayer = PurgeHandler.getInstance().getPlayer(player.getName());
						
						int price = event.getCurrentItem().getAmount() * sign.getItemPrice(event.getCurrentItem());
						int money = pPlayer.getMoney() + price;
						
						pPlayer.setMoney(money);
						pPlayer.updateScoreboard();
						
						player.getInventory().setItem(event.getSlot(), null);
					}
				}
				
				return;
			}
			
			event.setCancelled(true);
			
			PurgeSign sign = PurgeHandler.getInstance().getShop().getSign(player);
			
			if (event.isLeftClick() || event.isRightClick())
			{
				if (event.getCursor().getType() == Material.AIR && event.getCurrentItem().getType() != Material.AIR)
				{
					if (!sign.isPlayerEditing(player))
					{
						if (event.getCurrentItem().getType() == Material.EMPTY_MAP)
						{
							PurgeTown town = PurgeHandler.getInstance().getTown(sign.getItemTown(event.getRawSlot()));
							
							if (town != null)
							{
								PurgePlayer pPlayer = PurgeHandler.getInstance().getPlayer(player.getName());
								
								PurgePlot plot = town.occupyPlot(pPlayer, town);
								
								if (plot != null)
								{
									player.sendMessage(PurgeChat.success("You have successfully purchased a plot!"));
									
									player.teleport(plot.getCuboid().getCenter());
									
									//town.getGame().startPregame();
								} else
								{
									player.sendMessage(PurgeChat.error("There are no plots available in this town..."));
								}
							} else
							{
								//Undefined town
								
								player.sendMessage(PurgeChat.error("That items town has not yet been set..."));
							}
						} else
						{
							if (sign.getItemPrice(event.getRawSlot()) != -1)
							{
								ItemStack item = event.getCurrentItem().clone();
								ItemMeta meta = item.getItemMeta();
								
								meta.setLore(new ArrayList<String>());
								item.setItemMeta(meta);
								
								if (event.isLeftClick())
								{
									//Buy item
									
									PurgePlayer pPlayer = PurgeHandler.getInstance().getPlayer(player.getName());
									
									int price = item.getAmount() * sign.getItemPrice(event.getRawSlot());
									int money = pPlayer.getMoney() - price;
									
									if (money >= 0)
									{
										pPlayer.setMoney(money >= 0 ? money : 0);
										pPlayer.updateScoreboard();
										
										player.getInventory().addItem(item);
									} else
									{
										player.sendMessage(PurgeChat.error("You do not have enough money to do that..."));
									}
								} else if (event.isRightClick())
								{
									//Sell item
									
									if (inventoryContains(player.getInventory(), item))
									{
										PurgePlayer pPlayer = PurgeHandler.getInstance().getPlayer(player.getName());
										
										int price = item.getAmount() * sign.getItemPrice(event.getRawSlot());
										int money = pPlayer.getMoney() + price;
										
										pPlayer.setMoney(money);
										pPlayer.updateScoreboard();
										
										player.getInventory().removeItem(item);
									}
								}
							} else
							{
								//Undefined price
								
								player.sendMessage(PurgeChat.error("That items price has not yet been set..."));
							}
						}
					} else
					{
						//Editing items
						
						if (event.getCurrentItem().getType() == Material.EMPTY_MAP)
						{
							if (event.isLeftClick())
							{
								//Display next town
								
								String town = sign.getItemTown(event.getRawSlot());
								PurgeTown purgeTown = PurgeHandler.getInstance().getNextTown(town, 1);
								
								if (purgeTown != null)
								{
									event.setCurrentItem(sign.setItemTown(event.getRawSlot(), purgeTown));
								}
							} else if (event.isRightClick())
							{
								//Display previous town
								
								String town = sign.getItemTown(event.getRawSlot());
								PurgeTown purgeTown = PurgeHandler.getInstance().getNextTown(town, -1);
								
								if (purgeTown != null)
								{
									event.setCurrentItem(sign.setItemTown(event.getRawSlot(), purgeTown));
								} else
								{
									sign.setItem(event.getRawSlot(), null);
									
									event.setCurrentItem(null);
								}
							}
						} else
						{
							int interval = event.isShiftClick() ? 10 : 1;
							
							if (event.isLeftClick())
							{
								//Increase price
								
								int price = sign.getItemPrice(event.getRawSlot()) + interval;
								
								event.setCurrentItem(sign.setItemPrice(event.getRawSlot(), price));
							} else if (event.isRightClick())
							{
								//Decrease price
								
								int price = sign.getItemPrice(event.getRawSlot());
								
								if (price - interval >= 0)
								{
									price -= price - interval >= 0 ? interval : 0;
									
									event.setCurrentItem(sign.setItemPrice(event.getRawSlot(), price));
								} else if (interval > 1)
								{
									//If interval is greater than 1 and goes past 0, set to 0
									
									event.setCurrentItem(sign.setItemPrice(event.getRawSlot(), 0));
								} else
								{
									//If price decreased below 0, remove item
									
									sign.setItem(event.getRawSlot(), null);
									
									event.setCurrentItem(null);
								}
							}
						}
					}
				} else if (event.getCursor().getType() != Material.AIR)
				{
					ItemStack item = event.getCursor();
					
					if (sign.isPlayerEditing(player) && event.getCurrentItem().getType() == Material.AIR)
					{
						if (!inventoryContains(event.getView().getTopInventory(), item))
						{
							//Set item to this
							
							item = item.clone();
							
							sign.setItem(event.getRawSlot(), item);
							
							if (item.getType() == Material.EMPTY_MAP)
							{
								event.setCurrentItem(sign.setItemTown(event.getRawSlot(), null));
							} else
							{
								event.setCurrentItem(sign.setItemPrice(event.getRawSlot(), -1));
							}
						}
					} else if (!sign.isPlayerEditing(player))
					{
						if (inventoryContains(event.getView().getTopInventory(), item))
						{
							int itemPrice = sign.getItemPrice(item);
							
							if (itemPrice != -1)
							{
								//Sell whole stack
								
								PurgePlayer pPlayer = PurgeHandler.getInstance().getPlayer(player.getName());
								
								int price = item.getAmount() * itemPrice;
								int money = pPlayer.getMoney() + price;
								
								pPlayer.setMoney(money);
								pPlayer.updateScoreboard();
								
								event.setCursor(null);
							}
						}
					}
				}
			} else
			{
				System.out.println("Woops");
			}
		}
	}
	
	public boolean inventoryContains(Inventory inv, ItemStack item)
	{
		if (item != null)
		{
			for (ItemStack itemS : inv.getContents())
			{
				if (itemS != null)
				{
					boolean type = itemS.getType().equals(item.getType());
					boolean data = itemS.getData().equals(item.getData());
					
					if (type && data)
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled()) return;
		
		//Only players
		if (event.getEntityType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER)
		{
			PurgePlayer damaged = PurgeHandler.getInstance().getPlayer(((Player) event.getEntity()).getName());
			PurgePlayer damager = PurgeHandler.getInstance().getPlayer(((Player) event.getDamager()).getName());
			
			if (damaged.hasPlot() && damaged.isAliveInGame() && damager.hasPlot() && damager.isAliveInGame())
			{
				//Allow players to kill each other
			} else
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		PurgePlayer player = PurgeHandler.getInstance().getPlayer(event.getPlayer().getName());
		
		PurgeTown townFrom = PurgeHandler.getInstance().getTown(event.getFrom());
		PurgeTown townTo = PurgeHandler.getInstance().getTown(event.getTo());
		
		if ((townFrom != null && townTo == null) || townFrom != townTo)
		{
			//Left town
			
			if (player.hasPlot())
			{
				PurgeGameMode gamemode = player.getPlot().getTown().getGame().getGamemode();
				
				if (player.getPlot().getTown() == townFrom && (gamemode == PurgeGameMode.PREGAME || gamemode == PurgeGameMode.INGAME))
				{
					event.setCancelled(true);
					
					return;
				}
			}
			
			if (townTo == null)
			{
				event.getPlayer().sendMessage(PurgeChat.info("You have left " + ChatColor.DARK_AQUA + townFrom.getName()));
				
				return;
			}
		}
		
		if ((townFrom == null && townTo != null) || townFrom != townTo)
		{
			event.getPlayer().sendMessage(PurgeChat.info("Welcome to " + ChatColor.DARK_AQUA + townTo.getName()));
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		PurgePlayer player = PurgeHandler.getInstance().getPlayer(event.getPlayer().getName());
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Material clickMaterial = Material.ARROW;
			
			if (event.getItem() != null && event.getItem().getType() == clickMaterial)
			{
				if (event.getClickedBlock().getState() instanceof Sign)
				{
					Sign sign = (Sign) event.getClickedBlock().getState();
					
					if (PurgeHandler.getInstance().getShop().getSign(sign) == null)
					{
						if (!sign.getLine(0).equals(ChatColor.DARK_BLUE + "[Purge Shop]"))
						{
							sign.setLine(3, sign.getLine(1));
							sign.setLine(2, sign.getLine(0));
							sign.setLine(1, "");
							
							sign.setLine(0, ChatColor.DARK_BLUE + "[Purge Shop]");
						}
						
						sign.update();
						
						PurgeHandler.getInstance().getShop().addSign(event.getPlayer(), sign);
						PurgeHandler.getInstance().saveShops();
					} else if (!PurgeHandler.getInstance().getShop().getSign(sign).isBeingEdited())
					{
						PurgeHandler.getInstance().getShop().clickedSign(event.getPlayer(), sign, true);
					}
				} else if ((player.hasPlot() && !player.getPlot().getTown().getGame().isAnythingRunning()) || !player.hasPlot())
				{
					PurgeTown town = PurgeHandler.getInstance().getTown(event.getClickedBlock().getLocation());
					
					if (town != null)
					{
						//!
						if (!player.hasPlot())
						{
							if (town.plotExists(event.getClickedBlock().getLocation()))
							{
								boolean success = town.occupyPlot(player, event.getClickedBlock().getLocation());
								
								if (success)
								{
									event.getPlayer().sendMessage(PurgeChat.success("You have successfully purchased that plot!"));
									
									//town.getGame().startPregame();
								} else
								{
									event.getPlayer().sendMessage(PurgeChat.error("That plot has already been taken..."));
								}
							} else
							{
								event.getPlayer().sendMessage(PurgeChat.error("There is not a plot there..."));
							}
						} else
						{
							event.getPlayer().sendMessage(PurgeChat.error("You are only able to have one plot..."));
						}
					} else
					{
						/*
						boolean success = PurgeHandler.getInstance().addTown(event.getClickedBlock().getLocation());
						
						if (success)
						{
							PurgeHandler.getInstance().saveTowns();
							
							event.getPlayer().sendMessage(PurgeChat.success("You have successfully created a town!"));
						} else
						{
							event.getPlayer().sendMessage(PurgeChat.error("You are not able to overlap towns..."));
						}
						*/
					}
				}
				
				return;
			} else if (event.getItem() != null && event.getClickedBlock() != null)
			{
				boolean isCreateStick = false;
				
				if (event.getItem().getType() == Material.STICK && event.getItem().getItemMeta().getLore() != null && !event.getItem().getItemMeta().getLore().isEmpty())
				{
					isCreateStick = event.getItem().getItemMeta().getLore().get(0).contains(ChatColor.AQUA + "Purge Town: " + ChatColor.GOLD);
				}
				
				if (isCreateStick && !points.containsKey(event.getPlayer().getName()))
				{
					event.getPlayer().sendMessage(PurgeChat.success("You have selected the first point now select the second!"));
					
					Object[] locs = new Object[3];
					locs[0] = event.getClickedBlock().getLocation();
					
					points.put(event.getPlayer().getName(), locs);
					
					return;
				} else if (isCreateStick)
				{
					Object[] locs = points.get(event.getPlayer().getName());
					locs[1] = event.getClickedBlock().getLocation();
					locs[2] = event.getItem();
					
					points.put(event.getPlayer().getName(), locs);
					
					String[] blocksSize = ((ItemStack) locs[2]).getItemMeta().getLore().get(0).replace(ChatColor.AQUA + "Purge Town: " + ChatColor.GOLD, "").split("x");
					
					int plotsBlockWidth = Integer.parseInt(blocksSize[0]);
					int plotsBlockLength = Integer.parseInt(blocksSize[1]);
					
					PurgeCuboid cuboid = new PurgeCuboid((Location) locs[0], (Location) locs[1]);
					
					int plotsWidth = cuboid.getWidth() / (plotsBlockWidth + (PurgeTown.plotSpacing - 2));
					int plotsLength = cuboid.getLength() / (plotsBlockLength + (PurgeTown.plotSpacing - 2));
					
					plotsWidth += plotsWidth == 0 ? 2 : 0;
					plotsLength += plotsLength == 0 ? 2 : 0;
					
					plotsWidth += plotsWidth % 2 != 0 ? (plotsWidth % 2) : 0;
					plotsLength += plotsLength % 2 != 0 ? (plotsLength % 2) : 0;
					
					event.getPlayer().sendMessage(PurgeChat.info("Purge Towns can only have an even number of plots, the town you selected will be " + plotsWidth + " plots wide by " + plotsLength + " plots long"));
					event.getPlayer().sendMessage(PurgeChat.info("If that is alright, do: /purge town create <town-name> else re-select the second point"));
					
					return;
				}
			}
			
			if (event.getClickedBlock().getState() instanceof Sign)
			{
				Sign sign = (Sign) event.getClickedBlock().getState();
				
				PurgeHandler.getInstance().getShop().clickedSign(event.getPlayer(), sign, false);
				
				event.setCancelled(true);
				
				return;
			}
		}
		
		if (event.getClickedBlock() != null)
		{
			if ((player.hasPlot() && !player.getPlot().getCuboid().plotContains(event.getClickedBlock().getLocation())) || !player.hasPlot())
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (event.getEntity().getKiller() instanceof Player)
		{
			PurgePlayer deathPlayer = PurgeHandler.getInstance().getPlayer(event.getEntity().getName());
			PurgePlayer player = PurgeHandler.getInstance().getPlayer(event.getEntity().getKiller().getName());
			
			deathPlayer.setDead(true);
			player.setScore(player.getScore() + 1);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (PurgeHandler.getInstance().isInsideTown(event.getLocation()))
		{
			Random rand = new Random();
			
			String chance = "20%";
			
			if (event.getEntity() instanceof Monster)
			{
				if (!PurgeMain.getInstance().getConfig().getBoolean("mobs.monsters.spawnable", true))
				{
					event.setCancelled(true);
					
					return;
				}
				
				chance = PurgeMain.getInstance().getConfig().getString("mobs.monsters.chanceofspawn", "20%");
			} else if (event.getEntity() instanceof Animals)
			{
				if (!PurgeMain.getInstance().getConfig().getBoolean("mobs.animals.spawnable", false))
				{
					event.setCancelled(true);
					
					return;
				}
				
				chance = PurgeMain.getInstance().getConfig().getString("mobs.animals.chanceofspawn", "20%");
			}
			
			chance = chance.replace("%", "");
			
			int ch = 20;
			
			try {
				ch = Integer.parseInt(chance);
			} catch (NumberFormatException e) { }
			
			if (rand.nextInt(100) > ch)
			{
				event.setCancelled(true);
			}
		}
	}
}