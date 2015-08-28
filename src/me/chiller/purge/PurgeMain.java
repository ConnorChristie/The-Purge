package me.chiller.purge;

import me.chiller.purge.config.ShopsConfig;
import me.chiller.purge.config.TownsConfig;

import org.bukkit.plugin.java.JavaPlugin;

public class PurgeMain extends JavaPlugin
{
	private static PurgeMain instance;
	
	private PurgeHandler purgeHandler;
	private PurgeListener purgeListener;
	
	private TownsConfig townsConfig;
	private ShopsConfig shopsConfig;
	
	public PurgeMain()
	{
		instance = this;
	}
	
	public void onEnable()
	{
		townsConfig = new TownsConfig();
		shopsConfig = new ShopsConfig();
		
		getConfig().addDefault("game.startmoney", 500);
		getConfig().addDefault("game.gametime", 90);
		getConfig().addDefault("game.peacetime", 90);
		
		getConfig().addDefault("mobs.monsters.spawnable", true);
		getConfig().addDefault("mobs.monsters.chanceofspawn", "20%");
		
		getConfig().addDefault("mobs.animals.spawnable", false);
		getConfig().addDefault("mobs.animals.chanceofspawn", "20%");
		
		getConfig().options().copyDefaults(true);
		
		saveConfig();
		
		purgeHandler = new PurgeHandler();
		
		//purgeHandler.addTown(new Location(Bukkit.getWorld("world"), 0, 0, 0), new Location(Bukkit.getWorld("world"), 1000, 1000, 1000));
		//purgeHandler.saveTowns();
		
		//town.addPlayer("Chiller");
		//town.addPlayer(Bukkit.getPlayer("ChillerConnor"));
		
		//town.getGame().startPregame();
		
		getServer().getPluginManager().registerEvents(purgeListener = new PurgeListener(), this);
		getCommand("purge").setExecutor(new PurgeCommand());
		
		getLogger().info("Has been enabled!");
	}
	
	public void onDisable()
	{
		purgeHandler.stopAllGames();
		
		//getTownsConfig().saveConfig();
		
		getLogger().info("Has been disabled...");
	}
	
	public TownsConfig getTownsConfig()
	{
		return townsConfig;
	}
	
	public ShopsConfig getShopsConfig()
	{
		return shopsConfig;
	}
	
	public PurgeListener getListener()
	{
		return purgeListener;
	}
	
	public static PurgeMain getInstance()
	{
		return instance;
	}
}