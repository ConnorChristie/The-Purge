package me.chiller.purge;

import java.util.ArrayList;
import java.util.List;

import me.chiller.purge.PurgeChat.ChatType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PurgeCommand implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		boolean isPlayer = sender instanceof Player;
		
		Player player = isPlayer ? (Player) sender : null;
		
		if (args.length < 1)
		{
			sendMessage("Command usage: /purge <command> [args]", ChatType.ERROR, sender, isPlayer);
			
			return true;
		}
		
		if (isPlayer && ((Player) sender).hasPermission("purge.setup") && args.length >= 1)
		{
			if (args[0].equalsIgnoreCase("sethub"))
			{
				if (args.length >= 2)
				{
					if (args[1].equalsIgnoreCase("spawn") && args.length == 2)
					{
						
						
						return true;
					} else if (args[1].equalsIgnoreCase("death") && args.length == 2)
					{
						
						
						return true;
					} else if (args[1].equalsIgnoreCase("ready") && args.length == 2)
					{
	
						
						return true;
					} else
					{
						sendMessage("Command usage: /purge sethub <spawn:death:ready>", ChatType.ERROR, sender, isPlayer);
						
						return true;
					}
				} else
				{
					sendMessage("Command usage: /purge sethub <spawn:death:ready>", ChatType.ERROR, sender, isPlayer);
					
					return true;
				}
			} else if (args[0].equalsIgnoreCase("town"))
			{
				if (args.length >= 3)
				{
					if (args[1].equalsIgnoreCase("tool") && args.length == 4)
					{
						int blocksWidth = 5;
						int blocksLength = 5;
						
						try {
							blocksWidth = Integer.parseInt(args[2]);
							blocksLength = Integer.parseInt(args[3]);
						} catch (NumberFormatException e)
						{
							sendMessage("Command usage: /purge town tool <plot-width> <plot-length>", ChatType.ERROR, sender, isPlayer);
						}
						
						ItemStack item = new ItemStack(Material.STICK, 1);
						ItemMeta meta = item.getItemMeta();
						
						List<String> lore = new ArrayList<String>();
						lore.add(ChatColor.AQUA + "Purge Town: " + ChatColor.GOLD + blocksWidth + "x" + blocksLength);
						
						meta.setLore(lore);
						item.setItemMeta(meta);
						
						if (!player.getInventory().contains(item))
						{
							player.getInventory().addItem(item);
							
							sendMessage("Use this stick and select the cuboid of the town!", ChatType.INFO, player, isPlayer);
						} else
						{
							sendMessage("Use your stick and select the cuboid of the town!", ChatType.INFO, player, isPlayer);
						}
						
						sendMessage("After the cuboid is selected, type: /purge town create <town-name>", ChatType.INFO, player, isPlayer);
						
						return true;
					} else if (args[1].equalsIgnoreCase("create") && args.length >= 3)
					{
						PurgeListener listener = PurgeMain.getInstance().getListener();
						
						if (listener.points.containsKey(player.getName()))
						{
							Object[] locs = listener.points.get(player.getName());
							
							if (locs[0] != null && locs[1] != null)
							{
								String townName = "";;
								
								for (int i = 2; i < args.length; i++)
								{
									townName += (i != 2 ? " " : "") + args[i];
								}
								
								if (townName.length() > 16)
								{
									townName = townName.substring(0, 16);
								}
								
								String[] blocksSize = ((ItemStack) locs[2]).getItemMeta().getLore().get(0).replace(ChatColor.AQUA + "Purge Town: " + ChatColor.GOLD, "").split("x");
								
								boolean success = PurgeHandler.getInstance().addTown(townName, Integer.parseInt(blocksSize[0]) - 1, Integer.parseInt(blocksSize[1]) - 1, (Location) locs[0], (Location) locs[1]);
								
								if (success)
								{
									PurgeHandler.getInstance().saveTowns();
									
									sendMessage("You have successfully created a town!", ChatType.SUCCESS, player, isPlayer);
								} else
								{
									sendMessage("You are not able to overlap towns...", ChatType.ERROR, player, isPlayer);
								}
								
								listener.points.remove(player.getName());
							} else
							{
								sendMessage("You first have to select a cuboid...", ChatType.ERROR, player, isPlayer);
							}
						}
						
						return true;
					} else if (args[1].equalsIgnoreCase("destroy") && args.length == 3)
					{
						
						
						return true;
					} else
					{
						sendMessage("Command usage: /purge town <tool:create:destroy> [town] <plot-width> <plot-length>", ChatType.ERROR, sender, isPlayer);
						
						return true;
					}
				} else
				{
					sendMessage("Command usage: /purge town <tool:create:destroy> [town] <plot-width> <plot-length>", ChatType.ERROR, sender, isPlayer);
					
					return true;
				}
			}
		}
		
		if (((isPlayer && ((Player) sender).hasPermission("purge.control")) || !isPlayer) && args.length >= 1)
		{
			if (args[0].equalsIgnoreCase("start"))
			{
				if (args.length >= 2)
				{
					//Named a town
					
					String townName = "";
					
					for (int i = 1; i < args.length; i++)
					{
						townName += (i != 1 ? " " : "") + args[i];
					}
					
					PurgeTown town = PurgeHandler.getInstance().getTown(townName);
					
					if (town != null)
					{
						town.getGame().startPregame();
					} else
					{
						sendMessage("The town you supplied does not exist...", ChatType.ERROR, sender, isPlayer);
					}
				} else if (args.length == 1 && isPlayer)
				{
					//Get town player is in
					
					PurgeTown town = PurgeHandler.getInstance().getTown(player.getLocation());
					
					if (town != null)
					{
						town.getGame().startPregame();
					} else
					{
						sendMessage("You are not inside of a town...", ChatType.ERROR, sender, isPlayer);
						sendMessage("Use: /purge start [town]", ChatType.ERROR, sender, isPlayer);
					}
				} else
				{
					sendMessage("Command usage: /purge start [town]", ChatType.ERROR, sender, isPlayer);
				}
				
				return true;
			} else if (args[0].equalsIgnoreCase("stop"))
			{
				if (args.length == 2)
				{
					//Named a town
					
					String townName = "";
					
					for (int i = 1; i < args.length; i++)
					{
						townName += (i != 1 ? " " : "") + args[i];
					}
					
					PurgeTown town = PurgeHandler.getInstance().getTown(townName);
					
					if (town != null)
					{
						town.getGame().forceStop();
					} else
					{
						sendMessage("The town you supplied does not exist...", ChatType.ERROR, sender, isPlayer);
					}
				} else if (args.length == 1 && isPlayer)
				{
					//Get town player is in
					
					PurgeTown town = PurgeHandler.getInstance().getTown(player.getLocation());
					
					if (town != null)
					{
						town.getGame().forceStop();
					} else
					{
						sendMessage("You are not inside of a town...", ChatType.ERROR, sender, isPlayer);
						sendMessage("Use: /purge start [town]", ChatType.ERROR, sender, isPlayer);
					}
				} else
				{
					sendMessage("Command usage: /purge stop [town]", ChatType.ERROR, sender, isPlayer);
				}
				
				return true;
			}
		}
		
		if (((isPlayer && ((Player) sender).hasPermission("purge.give")) || !isPlayer) && args.length >= 1)
		{
			if (args[0].equalsIgnoreCase("give") && args.length >= 2)
			{
				Player p = Bukkit.getPlayer(args[1]);
				
				if (p != null && args.length == 3)
				{
					Material material = null;
					
					try {
						material = Material.getMaterial(Integer.parseInt(args[2]));
					} catch (NumberFormatException e) { }
					
					if (material == null)
					{
						material = Material.getMaterial(args[2]);
					}
					
					if (material != null)
					{
						ItemStack stack = new ItemStack(material, 1);
						
						p.getInventory().addItem(stack);
						
						sendMessage("Gave " + p.getName() + " 1 " + material.name(), ChatType.INFO, sender, isPlayer);
					} else
					{
						sendMessage("The item you entered could not be found", ChatType.ERROR, sender, isPlayer);
					}
					
					return true;
				} else if (p == null)
				{
					sendMessage("The player you entered could not be found", ChatType.ERROR, sender, isPlayer);
					
					return true;
				} else
				{
					sendMessage("Command usage: /purge give <player> <item>", ChatType.ERROR, sender, isPlayer);
					
					return true;
				}
			} else if (args[0].equalsIgnoreCase("give"))
			{
				sendMessage("Command usage: /purge give <player> <item>", ChatType.ERROR, sender, isPlayer);
				
				return true;
			}
		}
		
		//Default no permissions needed - if (player.hasPermission("purge.showstats"))
		if (args.length >= 1)
		{
			if (args[0].equalsIgnoreCase("stats"))
			{
				if (args.length >= 2)
				{
					if (isPlayer && args[1].equalsIgnoreCase("me") && args.length == 2)
					{
						
						
						return true;
					} else if (args[1].equalsIgnoreCase("top") && args.length == 2)
					{
	
						
						return true;
					} else if (args.length == 2)
					{
						Player p = Bukkit.getPlayer(args[1]);
						
						if (p != null)
						{
							
							
							return true;
						} else
						{
							sendMessage("The player you entered could not be found", ChatType.ERROR, sender, isPlayer);
							
							return true;
						}
					} else
					{
						sendMessage("Command usage: /purge stats <" + (isPlayer ? "me:" : "") + "top:player>", ChatType.ERROR, sender, isPlayer);
						
						return true;
					}
				} else
				{
					sendMessage("Command usage: /purge stats <" + (isPlayer ? "me:" : "") + "top:player>", ChatType.ERROR, sender, isPlayer);
					
					return true;
				}
			}
		}
		
		sendMessage("The command you entered does not exist", ChatType.ERROR, sender, isPlayer);
		
		return true;
	}
	
	private void sendMessage(String message, ChatType type, CommandSender sender, boolean isPlayer)
	{
		if (isPlayer)
		{
			((Player) sender).sendMessage(PurgeChat.colorChat(message, type));
		} else
		{
			Bukkit.getLogger().info("[The Purge] " + ChatColor.stripColor(message.replaceFirst("/", "")));
		}
	}
}