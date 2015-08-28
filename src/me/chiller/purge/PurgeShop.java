package me.chiller.purge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class PurgeShop
{
	private List<PurgeSign> signs = new ArrayList<PurgeSign>();
	private Map<String, PurgeSign> players = new HashMap<String, PurgeSign>();
	
	public void addSign(Player player, Sign sign)
	{
		PurgeSign s = new PurgeSign(this, sign);
		
		signs.add(s);
		
		s.openInventory(player, true);
	}
	
	public void clickedSign(Player player, Sign sign, boolean isEditing)
	{
		for (PurgeSign s : signs)
		{
			if (s.getSign().equals(sign))
			{
				s.openInventory(player, isEditing);
			}
		}
	}
	
	public void openedSign(Player player, PurgeSign sign)
	{
		players.put(player.getName(), sign);
	}
	
	public void closedSign(Player player)
	{
		players.remove(player.getName());
	}
	
	public boolean isViewingSign(Player player)
	{
		return players.containsKey(player.getName());
	}
	
	public PurgeSign getSign(Player player)
	{
		return players.get(player.getName());
	}
	
	public List<PurgeSign> getSigns()
	{
		return signs;
	}
	
	public PurgeSign getSign(Sign sign)
	{
		for (PurgeSign s : signs)
		{
			if (s.getSign().equals(sign))
			{
				return s;
			}
		}
		
		return null;
	}
	
	public List<Map<String, Object>> serialize()
	{
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		
		for (PurgeSign s : signs)
		{
			l.add(s.serialize());
		}
		
		return l;
	}
	
	@SuppressWarnings("unchecked")
	public void deserialize(List<Map<?, ?>> l)
	{
		for (Map<?, ?> m : l)
		{
			Map<String, Object> map = (Map<String, Object>) m;
			
			PurgeSign sign = new PurgeSign(this);
			
			boolean success = sign.deserialize(map);
			
			if (success)
			{
				signs.add(sign);
			}
		}
	}
}