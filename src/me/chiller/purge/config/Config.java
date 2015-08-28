package me.chiller.purge.config;

import java.io.File;
import java.io.IOException;

import me.chiller.purge.PurgeMain;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class Config
{
	private String name = "";
	
	private File configFile;
	private FileConfiguration config;
	
	public Config(String name)
	{
		this.name = name;
		
		loadConfig();
	}
	
	public void loadConfig()
	{
		if (configFile == null)
		{
			configFile = new File(PurgeMain.getInstance().getDataFolder(), name + ".yml");
		}
		
		config = YamlConfiguration.loadConfiguration(configFile);
	}
	
	public FileConfiguration getConfig()
	{
		return config;
	}
	
	public void saveConfig()
	{
		try
		{
			config.save(configFile);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}