package me.chiller.purge;

import org.bukkit.ChatColor;

public class PurgeChat
{
	public static enum ChatType
	{
		SUCCESS(ChatColor.DARK_GREEN),
		INFO(ChatColor.AQUA),
		ERROR(ChatColor.DARK_RED);
		
		private ChatColor color;
		
		private ChatType(ChatColor color)
		{
			this.color = color;
		}
		
		public ChatColor getColor()
		{
			return color;
		}
	}
	
	private static String prefix = ChatColor.GOLD + "[The Purge] ";
	
	public static String colorChat(String message, ChatType type)
	{
		return prefix + type.getColor() + message;
	}
	
	public static String success(String message)
	{
		return colorChat(message, ChatType.SUCCESS);
	}
	
	public static String info(String message)
	{
		return colorChat(message, ChatType.INFO);
	}
	
	public static String error(String message)
	{
		return colorChat(message, ChatType.ERROR);
	}
}