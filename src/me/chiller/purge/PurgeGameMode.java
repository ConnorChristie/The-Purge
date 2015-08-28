package me.chiller.purge;

import org.bukkit.ChatColor;

public enum PurgeGameMode
{
	OFFGAME(ChatColor.GOLD       + " The Purge "),
	PREGAME(ChatColor.DARK_GREEN + " Pre-Purge: " + ChatColor.DARK_AQUA + "0:00 "),
	INGAME (ChatColor.DARK_RED   + " Purging: "   + ChatColor.DARK_AQUA + "0:00 ");
	
	private String title;
	
	private PurgeGameMode(String title)
	{
		this.title = title;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
}