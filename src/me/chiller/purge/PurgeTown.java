package me.chiller.purge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

public class PurgeTown
{
	private String name = "MyTown";
	
	private PurgeGame purgeGame;
	private PurgeCuboid cuboid;
	
	public int plotsBlockWidth = 5;
	public int plotsBlockLength = 5;
	
	public int plotsWidth = 4;
	public int plotsLength = 4;
	
	public static int plotSpacing = 4;
	
	private PurgePlot[][] plots;
	private PurgeCuboid[][] plotCuboids;
	
	public PurgeTown() { }
	
	public PurgeTown(String name, int plotsBlockWidth, int plotsBlockLength, Location pointOne, Location pointTwo)
	{
		this.name = name;
		
		this.plotsBlockWidth = plotsBlockWidth;
		this.plotsBlockLength = plotsBlockLength;
		
		cuboid = new PurgeCuboid(pointOne, pointTwo);
		
		plotsWidth = cuboid.getWidth() / (plotsBlockWidth + (plotSpacing - 2));
		plotsLength = cuboid.getLength() / (plotsBlockLength + (plotSpacing - 2));
		
		plotsWidth += plotsWidth == 0 ? 2 : 0;
		plotsLength += plotsLength == 0 ? 2 : 0;
		
		plotsWidth += plotsWidth % 2 != 0 ? (plotsWidth % 2) : 0;
		plotsLength += plotsLength % 2 != 0 ? (plotsLength % 2) : 0;
		
		cuboid.setPointMax(cuboid.getPointMin().clone().add(plotsWidth * plotsBlockWidth + (plotsWidth * plotSpacing - 2), 0, plotsLength * plotsBlockLength + (plotsLength * plotSpacing - 2)));
		
		plots = new PurgePlot[plotsWidth][plotsLength];
		plotCuboids = new PurgeCuboid[plotsWidth][plotsLength];
	}
	
	protected void initTown()
	{
		int gameTime = PurgeMain.getInstance().getConfig().getInt("game.gametime", 900);
		int peaceTime = PurgeMain.getInstance().getConfig().getInt("game.peacetime", 120);
		
		purgeGame = new PurgeGame(this, gameTime, peaceTime);
		
		destroyMobs();
	}
	
	public Map<String, Object> serialize()
	{
		Map<String, Object> m = new HashMap<String, Object>();
		
		List<Map<String, Object>> p = new ArrayList<Map<String, Object>>();
		
		for (PurgePlot plot : getPlots())
		{
			p.add(plot.serialize());
		}
		
		Map<String, Object> mm = new HashMap<String, Object>();
		Map<String, Object> mmm = new HashMap<String, Object>();
		
		mm.put("width", plotsWidth);
		mm.put("length", plotsLength);
		mm.put("spacing", plotSpacing);
		
		mmm.put("width", plotsBlockWidth);
		mmm.put("length", plotsBlockLength);
		
		mm.put("blocks", mmm);
		m.put("size", mm);
		
		m.put("name", name);
		m.put("cuboid", cuboid.serialize());
		m.put("plots", p);
		
		return m;
	}
	
	@SuppressWarnings("unchecked")
	public void deserialize(Map<String, Object> m)
	{
		List<PurgePlot> ps = new ArrayList<PurgePlot>();
		
		for (String k : m.keySet())
		{
			Object v = m.get(k);
			
			if (k.equals("cuboid"))
			{
				cuboid = new PurgeCuboid();
				
				cuboid.deserialize((Map<String, Object>) v);
			} else if (k.equals("plots"))
			{
				List<Map<String, Object>> l = (List<Map<String, Object>>) v;
				
				for (Map<String, Object> map : l)
				{
					PurgePlot plot = new PurgePlot(this);
					plot.deserialize(map);
					
					ps.add(plot);
				}
			} else if (k.equals("size"))
			{
				plotsWidth = (Integer) ((Map<String, Object>) v).get("width");
				plotsLength = (Integer) ((Map<String, Object>) v).get("length");
				
				Object blocks = ((Map<String, Object>) v).get("blocks");
				
				plotsBlockWidth = (Integer) ((Map<String, Object>) blocks).get("width");
				plotsBlockLength = (Integer) ((Map<String, Object>) blocks).get("length");
				
				plotSpacing = (Integer) ((Map<String, Object>) v).get("spacing");
				
				plots = new PurgePlot[plotsWidth][plotsLength];
				plotCuboids = new PurgeCuboid[plotsWidth][plotsLength];
				
				fillCuboids();
			} else if (k.equals("name"))
			{
				name = (String) v;
			}
		}
		
		for (PurgePlot p : ps)
		{
			setPlot(p);
		}
		
		initTown();
	}
	
	public void createTown()
	{
		int highestBlock = cuboid.getHighestBlock();
		
		for (int x = cuboid.getPointMin().getBlockX() - 1; x <= cuboid.getPointMax().getBlockX() + 1; x++)
		{
			for (int z = cuboid.getPointMin().getBlockZ() - 1; z <= cuboid.getPointMax().getBlockZ() + 1; z++)
			{
				for (int y = cuboid.getPointMin().getBlockY(); y <= highestBlock; y++)
				{
					cuboid.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
				}
				
				int relativeX = x - cuboid.getPointMin().getBlockX();
				int relativeZ = z - cuboid.getPointMin().getBlockZ();
				
				if (x == cuboid.getPointMin().getBlockX() || x == cuboid.getPointMax().getBlockX()
						|| z == cuboid.getPointMin().getBlockZ() || z == cuboid.getPointMax().getBlockZ())
				{
					if ((relativeX + 1) % (plotsBlockWidth + plotSpacing) != 0 && (relativeZ + 1) % (plotsBlockLength + plotSpacing) != 0)
					{
						cuboid.getWorld().getBlockAt(x, cuboid.getAverageHeight(), z).setType(Material.STONE);
						cuboid.getWorld().getBlockAt(x, cuboid.getAverageHeight() + 1, z).setType(Material.FENCE);
					} else
					{
						cuboid.getWorld().getBlockAt(x, cuboid.getAverageHeight(), z).setType(Material.STONE);
					}
				} else if ((relativeX + 1) % (plotsBlockWidth + plotSpacing) == 0 || (relativeZ + 1) % (plotsBlockLength + plotSpacing) == 0)
				{
					cuboid.getWorld().getBlockAt(x, cuboid.getAverageHeight(), z).setType(Material.STONE);
				} else
				{
					cuboid.getWorld().getBlockAt(x, cuboid.getAverageHeight(), z).setType(Material.GRASS);
				}
			}
		}
	}
	
	public PurgePlot occupyPlot(PurgePlayer player, PurgeTown town)
	{
		for (int x = 0; x < plotCuboids.length; x++)
		{
			for (int z = 0; z < plotCuboids[x].length; z++)
			{
				if (!isPlotTaken(plots[x][z]))
				{
					PurgePlot plot = plots[x][z];
					
					if (plot == null)
					{
						plot = plots[x][z] = new PurgePlot(this, plotCuboids[x][z]);
					}
					
					plot.setOwner(player);
					player.setPlot(plot);
					
					//player.setMoney(player.getMoney() - 200);
					player.setupScoreboard();
					
					PurgeHandler.getInstance().saveTowns();
					
					return plot;
				}
			}
		}
		
		return null;
	}
	
	public boolean occupyPlot(PurgePlayer player, Location plotLocation)
	{
		if (!isPlotTaken(plotLocation))
		{
			PurgePlot plot = getPlotAt(plotLocation);
			
			if (plot == null)
			{
				plot = new PurgePlot(this, getCuboidAt(plotLocation));
				
				setPlot(plot);
			}
			
			plot.setOwner(player);
			player.setPlot(plot);
			
			//player.setMoney(player.getMoney() - 200);
			player.setupScoreboard();
			
			PurgeHandler.getInstance().saveTowns();
			
			return true;
		}
		
		return false;
	}
	
	public void destroyMobs()
	{
		Collection<Entity> entities = cuboid.getWorld().getEntitiesByClasses(Monster.class, Animals.class);
		
		for (Entity entity : entities)
		{
			if (cuboid.contains(entity.getLocation()))
			{
				if ((entity instanceof Monster && !PurgeMain.getInstance().getConfig().getBoolean("mobs.monsters.spawnable", true))
						|| (entity instanceof Animals && !PurgeMain.getInstance().getConfig().getBoolean("mobs.animals.spawnable", false)))
				{
						entity.remove();
				}
			}
		}
	}
	
	public void fillCuboids()
	{
		for (int x = 0; x < plotCuboids.length; x++)
		{
			for (int z = 0; z < plotCuboids[x].length; z++)
			{
				int minX = x * plotsBlockWidth + (x == 0 ? 1 : (x * plotSpacing + 1));
				int minZ = z * plotsBlockLength + (z == 0 ? 1 : (z * plotSpacing + 1));
				
				plotCuboids[x][z] = cuboid.getAbsolute(minX, minZ, minX + plotsBlockWidth, minZ + plotsBlockLength);
			}
		}
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setPlot(PurgePlot plot)
	{
		int x = plot.getCuboid().getRelative(cuboid).getPointMin().getBlockX() / plotsBlockWidth;
		int z = plot.getCuboid().getRelative(cuboid).getPointMin().getBlockZ() / plotsBlockLength;
		
		plots[x][z] = plot;
	}
	
	public List<PurgePlot> getPlots()
	{
		List<PurgePlot> ps = new ArrayList<PurgePlot>();
		
		for (int x = 0; x < plotsWidth; x++)
		{
			for (int z = 0; z < plotsLength; z++)
			{
				if (plots[x][z] != null)
				{
					ps.add(plots[x][z]);
				}
			}
		}
		
		return ps;
	}
	
	public List<PurgePlayer> getOnlinePlayers()
	{
		List<PurgePlayer> players = new ArrayList<PurgePlayer>();
		List<String> playerNames = new ArrayList<String>();
		
		for (PurgePlot plot : getPlots())
		{
			if (plot.hasOwner() && plot.getOwner().isOnline() && !playerNames.contains(plot.getOwner().getPlayer().getName()))
			{
				playerNames.add(plot.getOwner().getPlayer().getName());
			}
		}
		
		for (String playerName : playerNames)
		{
			players.add(PurgeHandler.getInstance().getPlayer(playerName));
		}
		
		return players;
	}
	
	public List<PurgePlayer> getAlivePlayers()
	{
		List<PurgePlayer> players = new ArrayList<PurgePlayer>();
		
		for (PurgePlayer player : getOnlinePlayers())
		{
			if (player.isAlive())
			{
				players.add(player);
			}
		}
		
		return players;
	}
	
	public List<PurgePlayer> getDeadPlayers()
	{
		List<PurgePlayer> players = new ArrayList<PurgePlayer>();
		
		for (PurgePlayer player : getOnlinePlayers())
		{
			if (!player.isDeadInGame())
			{
				players.add(player);
			}
		}
		
		return players;
	}
	
	public void sendMessage(String message)
	{
		for (PurgePlayer player : getOnlinePlayers())
		{
			player.getPlayer().sendMessage(message);
		}
	}
	
	public void playSound(Sound sound, int i, int d)
	{
		for (PurgePlayer player : getOnlinePlayers())
		{
			player.getPlayer().playSound(player.getPlayer().getLocation(), sound, i, d);
		}
	}
	
	public PurgePlot getPlotAt(Location location)
	{
		for (PurgePlot plot : getPlots())
		{
			if (plot.getCuboid().contains(location))
			{
				return plot;
			}
		}
		
		return null;
	}
	
	public boolean plotExists(Location location)
	{
		return getCuboidAt(location) != null;
	}
	
	public PurgeCuboid getCuboidAt(Location location)
	{
		for (int x = 0; x < plotCuboids.length; x++)
		{
			for (int z = 0; z < plotCuboids[x].length; z++)
			{
				if (plotCuboids[x][z].contains(location))
				{
					return plotCuboids[x][z];
				}
			}
		}
		
		return null;
	}
	
	public boolean isPlotTaken(Location location)
	{
		for (PurgePlot plot : getPlots())
		{
			if (plot.hasOwner() && plot.getCuboid().contains(location))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isPlotTaken(PurgePlot plot)
	{
		if (plot != null)
		{
			if (plot.hasOwner())
			{
				return true;
			}
		}
		
		return false;
	}

	public PurgeCuboid getCuboid()
	{
		return cuboid;
	}
	
	public PurgeGame getGame()
	{
		return purgeGame;
	}
}