package me.chiller.purge;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class PurgePlot
{
	private PurgeTown town;
	private PurgePlayer owner;
	private PurgeCuboid cuboid;
	
	private Location gate;
	
	public PurgePlot(PurgeTown town)
	{
		this.town = town;
	}
	
	public PurgePlot(PurgeTown town, PurgeCuboid cuboid)
	{
		this.town = town;
		
		this.cuboid = cuboid;
		
		createPlot();
	}
	
	public PurgePlot(PurgeTown town, Location pointOne, Location pointTwo)
	{
		this.town = town;
		
		cuboid = new PurgeCuboid(pointOne, pointTwo);
		
		createPlot();
	}
	
	public PurgePlot(PurgeTown town, PurgePlayer owner, Location pointOne, Location pointTwo)
	{
		this.town = town;
		this.owner = owner;
		
		if (owner != null)
		{
			owner.setPlot(this);
		}
		
		cuboid = new PurgeCuboid(pointOne, pointTwo);
		
		createPlot();
	}
	
	public Map<String, Object> serialize()
	{
		Map<String, Object> m = new HashMap<String, Object>();
		
		m.put("owner", owner.getOfflinePlayer().getName());
		m.put("cuboid", cuboid.serialize());
		m.put("gate", gate.toVector());
		
		return m;
	}
	
	@SuppressWarnings("unchecked")
	public void deserialize(Map<String, Object> m)
	{
		Vector gate = null;
		World world = null;
		
		for (String k : m.keySet())
		{
			Object v = m.get(k);
			
			if (k.equals("owner") && v != null)
			{
				owner = PurgeHandler.getInstance().getPlayer((String) v);
				
				owner.setPlot(this);
			} else if (k.equals("cuboid"))
			{
				cuboid = new PurgeCuboid();
				
				cuboid.deserialize((Map<String, Object>) v);
				
				world = cuboid.getWorld();
			} else if (k.equals("gate"))
			{
				gate = (Vector) v;
			}
		}
		
		if (gate != null && world != null)
		{
			this.gate = new Location(world, gate.getX(), gate.getY(), gate.getZ());
		}
	}
	
	public void createPlot()
	{
		int width = cuboid.getPointMax().getBlockX() - cuboid.getPointMin().getBlockX() + 1;
		int relativeZ = Math.max(cuboid.getPointMin().getBlockZ(), town.getCuboid().getPointMin().getBlockZ()) - Math.min(cuboid.getPointMin().getBlockZ(), town.getCuboid().getPointMin().getBlockZ()) - 1;
		
		relativeZ /= town.plotsBlockLength + PurgeTown.plotSpacing;
		
		for (int x = cuboid.getPointMin().getBlockX() - 1; x <= cuboid.getPointMax().getBlockX() + 1; x++)
		{
			for (int z = cuboid.getPointMin().getBlockZ() - 1; z <= cuboid.getPointMax().getBlockZ() + 1; z++)
			{
				if (x == cuboid.getPointMin().getBlockX() - 1 || x == cuboid.getPointMax().getBlockX() + 1
						|| z == cuboid.getPointMin().getBlockZ() - 1 || z == cuboid.getPointMax().getBlockZ() + 1)
				{
					if ((x == width / 2 + cuboid.getPointMin().getBlockX() && relativeZ % 2 == 0 && z == cuboid.getPointMax().getBlockZ() + 1)
							|| (x == width / 2 + cuboid.getPointMin().getBlockX() && relativeZ % 2 != 0 && z == cuboid.getPointMin().getBlockZ() - 1))
					{
						gate = new Location(cuboid.getWorld(), x + 0.5, cuboid.getAverageHeight() + 1, z + (relativeZ % 2 == 0 ? 1.5 : -0.5));
						
						Location gateLoc = new Location(cuboid.getWorld(), x, cuboid.getAverageHeight() + 1, z);
						gateLoc.getBlock().setType(Material.FENCE_GATE);
					} else
					{
						cuboid.getWorld().getBlockAt(x, cuboid.getAverageHeight() + 1, z).setType(Material.FENCE);
					}
				} else
				{
					//Flooring
				}
			}
		}
	}

	public PurgeTown getTown()
	{
		return town;
	}
	
	public void setTown(PurgeTown town)
	{
		this.town = town;
	}

	public PurgePlayer getOwner()
	{
		return owner;
	}
	
	public boolean hasOwner()
	{
		return owner != null;
	}
	
	public void setOwner(PurgePlayer owner)
	{
		this.owner = owner;
		
		if (owner != null)
		{
			owner.setPlot(this);
		}
	}
	
	public PurgeCuboid getCuboid()
	{
		return cuboid;
	}

	public PurgeCuboid getCuboid(PurgeCuboid relative)
	{
		return cuboid.getRelative(relative);
	}
	
	public void setCuboid(PurgeCuboid cuboid)
	{
		this.cuboid = cuboid;
	}
	
	public Location getGate()
	{
		return gate;
	}
}