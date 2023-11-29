package io.github.JoltMuz.Apocalypse;

import org.bukkit.plugin.java.JavaPlugin;


public final class Main extends JavaPlugin 
{
	private static Main instance;
	private static JavaPlugin plugin;
	
	@Override
    public void onEnable() 
	{
		instance = this;
		plugin = this;
		
		getConfig();
		saveDefaultConfig();
        reloadConfig();
		
		WaveManager waveManager = new WaveManager(plugin);
		PointsManager pointsManager = new PointsManager(plugin);
		
		this.getCommand("apocalypse").setExecutor(new Commands(waveManager,plugin));
		this.getCommand("points").setExecutor(pointsManager);
		
		getServer().getPluginManager().registerEvents(new Listeners(waveManager, pointsManager), plugin);
		getServer().getPluginManager().registerEvents(new Shop(plugin), plugin);
	}
	

	public static Main getInstance()
	{
		return instance;
	}
}
