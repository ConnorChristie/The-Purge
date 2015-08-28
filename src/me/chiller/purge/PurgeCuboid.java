package me.chiller.purge;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class PurgeCuboid
{
	private World world;
	
	private Location pointOne;
	private Location pointTwo;
	
	private Location pointMin;
	private Location pointMax;
	
	public PurgeCuboid() { }
	
	public PurgeCuboid(Location pointOne, Location pointTwo)
	{
		this(pointOne.getWorld(), pointOne, pointTwo);
	}
	
	public PurgeCuboid(World world, Location pointOne, Location pointTwo)
	{
		this.world = world;
		
		this.pointOne = pointOne;
		this.pointTwo = pointTwo;
		
		pointMin = new Location(world, Math.min(pointOne.getX(), pointTwo.getX()), Math.min(pointOne.getY(), pointTwo.getY()), Math.min(pointOne.getZ(), pointTwo.getZ()));
		pointMax = new Location(world, Math.max(pointOne.getX(), pointTwo.getX()), Math.max(pointOne.getY(), pointTwo.getY()), Math.max(pointOne.getZ(), pointTwo.getZ()));
	}
	
	public Map<String, Object> serialize()
	{
		Map<String, Object> m = new HashMap<String, Object>();
		
		m.put("world", getWorld().getName());
		
		m.put("min", getPointMin().toVector());
		m.put("max", getPointMax().toVector());
		
		return m;
	}
	
	public void deserialize(Map<String, Object> m)
	{
		for (String k : m.keySet())
		{
			Object v = m.get(k);
			
			if (k.equals("world"))
			{
				String world = (String) v;
				
				setWorld(Bukkit.getWorld(world));
			} else if (k.equals("min"))
			{
				Vector vector = (Vector) v;
				
				setPointMin(new Location(null, vector.getX(), vector.getY(), vector.getZ()));
			} else if (k.equals("max"))
			{
				Vector vector = (Vector) v;
				
				setPointMax(new Location(null, vector.getX(), vector.getY(), vector.getZ()));
			}
		}
	}
	
	public Location getCenter()
	{
		int width = getWidth() / 2;
		int length = getLength() / 2;
		
		return new Location(getPointMin().getWorld(), getPointMin().getX() + width + 0.5, getAverageHeight() + 1, getPointMin().getZ() + length + 0.5);
	}
	
	public int getWidth()
	{
		return getPointMax().getBlockX() - getPointMin().getBlockX();
	}
	
	public void setWidth(int width)
	{
		getPointMax().setX(getPointMin().getBlockX() + width);
	}
	
	public int getLength()
	{
		return getPointMax().getBlockZ() - getPointMin().getBlockZ();
	}

	public void setLength(int length)
	{
		getPointMax().setZ(getPointMin().getBlockZ() + length);
	}
	
	public boolean contains(Location point)
	{
		return point.getX() >= getPointMin().getX() && point.getZ() >= getPointMin().getZ()
				&& point.getX() <= getPointMax().getX() && point.getZ() <= getPointMax().getZ();
	}
	
	public boolean plotContains(Location point)
	{
		return point.getX() >= getPointMin().getX() - 1 && point.getZ() >= getPointMin().getZ() - 1
				&& point.getX() <= getPointMax().getX() + 1 && point.getZ() <= getPointMax().getZ() + 1;
	}
	
	public int getTotalBlocks()
	{
		return (getPointMax().getBlockX() - getPointMin().getBlockX()) * (getPointMax().getBlockZ() - getPointMin().getBlockZ());
	}
	
	public int getHighestBlock()
	{
		int height = 0;
		
		for (int x = getPointMin().getBlockX(); x <= getPointMax().getBlockX(); x++)
		{
			for (int z = getPointMin().getBlockZ(); z <= getPointMax().getBlockZ(); z++)
			{
				int height1 = getWorld().getHighestBlockYAt(x, z) - 1;
				
				if (height1 > height)
				{
					height = height1;
				}
			}
		}
		
		return height;
	}
	
	public int getAverageHeight()
	{
		/*
		int iters = 0;
		int height = 0;
		
		for (int x = getPointMin().getBlockX(); x <= getPointMax().getBlockX(); x++)
		{
			for (int z = getPointMin().getBlockZ(); z <= getPointMax().getBlockZ(); z++)
			{
				height += getWorld().getHighestBlockYAt(x, z) - 1;
				
				iters++;
			}
		}
		
		return iters != 0 ? height / iters : 0;
		*/
		
		return getPointMin().getBlockY();
	}
	
	public void setAverageHeight()
	{
		int averageY = getAverageHeight();
		
		getPointMin().setY(averageY);
		getPointMax().setY(averageY);
	}
	
	public PurgeCuboid getRelative(PurgeCuboid cuboid)
	{
		double xMin = Math.max(getPointMin().getX(), cuboid.getPointMin().getX()) - Math.min(getPointMin().getX(), cuboid.getPointMin().getX());
		double yMin = Math.max(getPointMin().getY(), cuboid.getPointMin().getY()) - Math.min(getPointMin().getY(), cuboid.getPointMin().getY());
		double zMin = Math.max(getPointMin().getZ(), cuboid.getPointMin().getZ()) - Math.min(getPointMin().getZ(), cuboid.getPointMin().getZ());
		
		double xMax = Math.max(getPointMax().getX(), cuboid.getPointMax().getX()) - Math.min(getPointMax().getX(), cuboid.getPointMax().getX());
		double yMax = Math.max(getPointMax().getY(), cuboid.getPointMax().getY()) - Math.min(getPointMax().getY(), cuboid.getPointMax().getY());
		double zMax = Math.max(getPointMax().getZ(), cuboid.getPointMax().getZ()) - Math.min(getPointMax().getZ(), cuboid.getPointMax().getZ());
		
		Location locMin = new Location(getWorld(), xMin, yMin, zMin);
		Location locMax = new Location(getWorld(), xMax, yMax, zMax);
		
		return new PurgeCuboid(locMin, locMax);
	}
	
	public PurgeCuboid getAbsolute(double xMin, double zMin, double xMax, double zMax)
	{
		double absXMin = xMin + getPointMin().getX();
		double absYMin = getPointMin().getY();
		double absZMin = zMin + getPointMin().getZ();
		
		double absXMax = xMax + getPointMin().getX();
		double absYMax = getPointMin().getY();
		double absZMax = zMax + getPointMin().getZ();
		
		Location locMin = new Location(getWorld(), absXMin, absYMin, absZMin);
		Location locMax = new Location(getWorld(), absXMax, absYMax, absZMax);
		
		return new PurgeCuboid(locMin, locMax);
	}
	
	public boolean intersects(PurgeCuboid cuboid)
	{
		return !(getPointMin().getBlockX() > cuboid.getPointMax().getBlockX()
				|| getPointMax().getBlockX() < cuboid.getPointMin().getBlockX()
				|| getPointMin().getBlockZ() > cuboid.getPointMax().getBlockZ()
				|| getPointMax().getBlockZ() < cuboid.getPointMin().getBlockZ());
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public void setWorld(World world)
	{
		this.world = world;
	}

	public Location getPointMin()
	{
		pointMin.setWorld(world);
		
		return pointMin;
	}

	public Location getPointMax()
	{
		pointMax.setWorld(world);
		
		return pointMax;
	}
	
	public void setPointMin(Location pointMin)
	{
		this.pointMin = pointMin;
	}
	
	public void setPointMax(Location pointMax)
	{
		this.pointMax = pointMax;
	}

	public void setPointOne(Location pointOne)
	{
		this.pointOne = pointOne;
		
		pointMin = new Location(null, Math.min(pointOne.getX(), pointTwo.getX()), Math.min(pointOne.getY(), pointTwo.getY()), Math.min(pointOne.getZ(), pointTwo.getZ()));
		pointMax = new Location(null, Math.max(pointOne.getX(), pointTwo.getX()), Math.max(pointOne.getY(), pointTwo.getY()), Math.max(pointOne.getZ(), pointTwo.getZ()));
	}

	public void setPointTwo(Location pointTwo)
	{
		this.pointTwo = pointTwo;
		
		pointMin = new Location(null, Math.min(pointOne.getX(), pointTwo.getX()), Math.min(pointOne.getY(), pointTwo.getY()), Math.min(pointOne.getZ(), pointTwo.getZ()));
		pointMax = new Location(null, Math.max(pointOne.getX(), pointTwo.getX()), Math.max(pointOne.getY(), pointTwo.getY()), Math.max(pointOne.getZ(), pointTwo.getZ()));
	}
}