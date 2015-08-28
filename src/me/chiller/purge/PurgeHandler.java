package me.chiller.purge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PurgeHandler
{
	private static PurgeHandler instance;
	
	private PurgeShop shop = new PurgeShop();
	
	public List<PurgeTown> towns = new ArrayList<PurgeTown>();
	public List<PurgePlayer> players = new ArrayList<PurgePlayer>();
	
	public PurgeHandler()
	{
		instance = this;
		
		loadTowns();
		loadShops();
	}
	
	@SuppressWarnings("unchecked")
	public void loadTowns()
	{
		List<Map<?, ?>> l = PurgeMain.getInstance().getTownsConfig().getConfig().getMapList("towns");
		List<Map<?, ?>> p = PurgeMain.getInstance().getTownsConfig().getConfig().getMapList("players");
		
		for (Map<?, ?> m : p)
		{
			PurgePlayer player = getPlayer((String) ((Map<String, Object>) m).get("name"));
			
			player.deserialize((Map<String, Object>) m);
		}
		
		for (Map<?, ?> m : l)
		{
			PurgeTown town = new PurgeTown();
			
			town.deserialize((Map<String, Object>) m);
			
			towns.add(town);
		}
		
		for (PurgeTown town : towns)
		{
			town.getGame().setGamemode(PurgeGameMode.OFFGAME);
		}
		
		Bukkit.getLogger().info("[The Purge] Successfully loaded " + towns.size() + " town" + (towns.size() != 1 ? "s" : "") + "!");
	}
	
	public void saveTowns()
	{
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		
		savePlayers();
		
		for (PurgeTown town : towns)
		{
			l.add(town.serialize());
		}
		
		PurgeMain.getInstance().getTownsConfig().getConfig().set("towns", l);
		PurgeMain.getInstance().getTownsConfig().saveConfig();
	}
	
	public void savePlayers()
	{
		List<Map<String, Object>> p = new ArrayList<Map<String, Object>>();
		
		for (PurgePlayer player : players)
		{
			p.add(player.serialize());
		}
		
		PurgeMain.getInstance().getTownsConfig().getConfig().set("players", p);
		PurgeMain.getInstance().getTownsConfig().saveConfig();
	}
	
	public boolean addTown(String name, int blocksWidth, int blocksLength, Location pointOne, Location pointTwo)
	{
		PurgeTown town = new PurgeTown(name, blocksWidth, blocksLength, pointOne, pointTwo);
		
		if (!doesTownCollide(town))
		{
			town.fillCuboids();
			town.createTown();
			town.initTown();
			
			towns.add(town);
			
			return true;
		}
		
		return false;
	}
	
	public void loadShops()
	{
		List<Map<?, ?>> l = PurgeMain.getInstance().getShopsConfig().getConfig().getMapList("signs");
		
		shop.deserialize(l);
		
		Bukkit.getLogger().info("[The Purge] Successfully loaded " + shop.getSigns().size() + " shop" + (shop.getSigns().size() != 1 ? "s" : "") + "!");
	}
	
	public void saveShops()
	{
		List<Map<String, Object>> l = shop.serialize();
		
		PurgeMain.getInstance().getShopsConfig().getConfig().set("signs", l);
		PurgeMain.getInstance().getShopsConfig().saveConfig();
	}
	
	public boolean hasPlayedBefore(String playerName)
	{
		for (PurgePlayer player : players)
		{
			if (player.getOfflinePlayer().getName().equals(playerName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public PurgePlayer getPlayer(String playerName)
	{
		for (PurgePlayer player : players)
		{
			if (player.getOfflinePlayer().getName().equals(playerName))
			{
				return player;
			}
		}
		
		PurgePlayer player = new PurgePlayer(playerName);
		
		players.add(player);
		
		return player;
	}
	
	public void stopAllGames()
	{
		for (PurgeTown town : towns)
		{
			town.getGame().forceStop();
		}
	}
	
	public PurgeTown getTown(Location point)
	{
		for (PurgeTown town : towns)
		{
			if (town.getCuboid().contains(point))
			{
				return town;
			}
		}
		
		return null;
	}
	
	public PurgeTown getTown(String name)
	{
		for (PurgeTown town : towns)
		{
			if (town.getName().equalsIgnoreCase(name))
			{
				return town;
			}
		}
		
		return null;
	}
	
	public PurgeTown getNextTown(String town, int next)
	{
		if (town != null && !town.equals("Town Plot"))
		{
			for (int i = 0; i < towns.size(); i++)
			{
				if (towns.get(i).getName().equalsIgnoreCase(town))
				{
					if (i + next >= 0 && i + next < towns.size())
					{
						return towns.get(i + next);
					} else if (i + next >= towns.size())
					{
						return towns.get(0);
					}
				}
			}
		} else
		{
			return towns.get(0);
		}
		
		return null;
	}

	public boolean isInsideTown(Location point)
	{
		for (PurgeTown town : towns)
		{
			if (town.getCuboid().contains(point))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean doesTownCollide(PurgeTown town)
	{
		for (PurgeTown t : towns)
		{
			if (t.getCuboid().intersects(town.getCuboid()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public List<PurgeTown> getTowns()
	{
		return towns;
	}
	
	public PurgeShop getShop()
	{
		return shop;
	}
	
	public static PurgeHandler getInstance()
	{
		return instance;
	}
}