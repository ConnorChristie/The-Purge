package me.chiller.purge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class PurgeSign
{
	private PurgeShop shop;
	
	private Sign sign;
	private ItemStack[] items = new ItemStack[27];
	
	private String editingPlayer = "";
	
	public PurgeSign(PurgeShop shop)
	{
		this.shop = shop;
	}
	
	public PurgeSign(PurgeShop shop, Sign sign)
	{
		this.shop = shop;
		this.sign = sign;
	}
	
	public ItemStack setItem(int index, ItemStack item)
	{
		items[index] = item;
		
		return items[index];
	}
	
	public ItemStack setItemPrice(int index, int price)
	{
		ItemStack item = items[index];
		
		if (item != null)
		{
			setItemLore(item, ChatColor.AQUA + "Price: " + ChatColor.GOLD + (price < 0 ? "Undefined" : (price == 0 ? "Free" : "$" + price)));
			
			items[index] = item;
		}
		
		PurgeHandler.getInstance().saveShops();
		
		return items[index];
	}
	
	public int getItemPrice(int index)
	{
		ItemStack item = items[index];
		
		if (item != null)
		{
			ItemMeta meta = item.getItemMeta();
			
			List<String> lore = meta.getLore();
			
			if (lore != null)
			{
				String price = lore.get(0).replace(ChatColor.AQUA + "Price: " + ChatColor.GOLD, "");
				
				if (price.equals("Undefined"))
				{
					return -1;
				} else if (price.equals("Free"))
				{
					return 0;
				} else
				{
					price = price.replace("$", "");
					
					return Integer.parseInt(price);
				}
			}
		}
		
		return -1;
	}
	
	public int getItemPrice(ItemStack item)
	{
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] != null)
			{
				boolean type = items[i].getType().equals(item.getType());
				boolean data = items[i].getData().equals(item.getData());
				
				if (type && data)
				{
					return getItemPrice(i);
				}
			}
		}
		
		return -1;
	}
	
	public ItemStack setItemTown(int index, PurgeTown town)
	{
		ItemStack item = items[index];
		
		if (item != null)
		{
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + (town != null ? town.getName() : "Town Plot"));
			
			item.setItemMeta(meta);
			
			if (town != null)
			{
				setItemLore(item, ChatColor.AQUA + "Plot Size: " + ChatColor.GOLD + (town.plotsBlockWidth + 1) + "x" + (town.plotsBlockLength + 1));
			} else
			{
				setItemLore(item, ChatColor.AQUA + "Plot Size: " + ChatColor.GOLD + "Undefined");
			}
			
			items[index] = item;
		}
		
		PurgeHandler.getInstance().saveShops();
		
		return item;
	}
	
	public String getItemTown(int index)
	{
		ItemStack item = items[index];
		
		if (item != null)
		{
			String town = ChatColor.stripColor(item.getItemMeta().getDisplayName());
			
			return town;
		}
		
		return "Undefined";
	}
	
	public InventoryView openInventory(Player player, boolean isEditing)
	{
		if (isEditing)
		{
			editingPlayer = player.getName();
		}
		
		shop.openedSign(player, this);
		
		Inventory inv = getInventory(isEditing);
		InventoryView view = player.openInventory(inv);
		
		return view;
	}
	
	public void closeInventory(Player player)
	{
		if (editingPlayer.equals(player.getName()))
		{
			editingPlayer = "";
		}
		
		shop.closedSign(player);
	}
	
	public Inventory getInventory(boolean isEditing)
	{
		Inventory inv = Bukkit.getServer().createInventory(null, items.length, (isEditing ? "Editing " : "") + "The Purge Shop");
		
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] != null)
			{
				inv.setItem(i, items[i]);
			}
		}
		
		return inv;
	}
	
	public Sign getSign()
	{
		return sign;
	}
	
	public boolean isPlayerEditing(Player player)
	{
		return editingPlayer.equals(player.getName());
	}
	
	public boolean isBeingEdited()
	{
		return editingPlayer != "";
	}
	
	public void setItemLore(ItemStack item, String loreStr, String loreStr1)
	{
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		
		lore.add(loreStr);
		lore.add(loreStr1);
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
	
	public void setItemLore(ItemStack item, String loreStr)
	{
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		
		lore.add(loreStr);
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
	
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> mapOne = new HashMap<String, Object>();
		
		mapOne.put("position", sign.getLocation().toVector());
		mapOne.put("world", sign.getLocation().getWorld().getName());
		
		map.put("location", mapOne);
		
		Map<Integer, Object> list = new HashMap<Integer, Object>();
		
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] != null)
			{
				mapOne = new HashMap<String, Object>();
				
				ItemStack item = items[i];
				
				mapOne.put("type", item.getTypeId());
				mapOne.put("amount", item.getAmount());
				mapOne.put("data", item.getData().getData());
				mapOne.put("price", item.getItemMeta().getLore().get(0).replace(ChatColor.AQUA + "Price: " + ChatColor.GOLD, ""));
				
				list.put(i, mapOne);
			}
		}
		
		map.put("items", list);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public boolean deserialize(Map<String, Object> map)
	{
		Vector pos = null;
		String world = "";
		
		for (String k : map.keySet())
		{
			Object o = map.get(k);
			
			if (k.equals("location"))
			{
				Map<String, Object> mapOne = (Map<String, Object>) o;
				
				pos = (Vector) mapOne.get("position");
				world = (String) mapOne.get("world");
				
				BlockState state = Bukkit.getWorld(world).getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).getState();
				
				if (state instanceof Sign)
				{
					sign = (Sign) state;
				} else
				{
					Bukkit.getLogger().warning("[The Purge] Broken shop sign at: " + pos.toString());
					
					return false;
				}
			} else if (k.equals("items"))
			{
				Map<Integer, Object> list = (Map<Integer, Object>) o;
				
				for (int l : list.keySet())
				{
					Map<String, Object> mapOne = (Map<String, Object>) list.get(l);
					
					int type = (Integer) mapOne.get("type");
					int amount = (Integer) mapOne.get("amount");
					int dataInt = (Integer) mapOne.get("data");
					byte data = (byte) dataInt;
					String price = (String) mapOne.get("price");
					
					ItemStack item = new ItemStack(type, amount);
					
					item.getData().setData(data);
					
					setItemLore(item, ChatColor.AQUA + "Price: " + ChatColor.GOLD + price);
					
					items[l] = item;
				}
			}
		}
		
		return true;
	}
}