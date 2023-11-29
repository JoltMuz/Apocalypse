package io.github.JoltMuz.Apocalypse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class WaveManager 
{

	int currentWaveIndex = 1;
	JavaPlugin plugin;
	private List<Wave> waves = new ArrayList<>();
	private Wave currentWave;
	World world;
	
	public WaveManager(JavaPlugin plugin) 
	{
		this.currentWaveIndex = 1;
		this.plugin = plugin;
		loadWaves();
	}
	
	public List<Wave> getWaves()
	{
		return waves;
	}
	
	public void startNextWave()
	{
		if (waves.isEmpty())
		{
			throw new IllegalArgumentException("No waves found" + plugin.getConfig().getConfigurationSection("waves"));
		}
		
		int waveIndex = currentWaveIndex - 1;
		Wave wave = waves.get(waveIndex);
		wave.setWorld(world);
		wave.start();
		currentWave = wave;
		broadcastWaveStartMessage();
		currentWaveIndex++;
	}

		
	
	private void broadcastWaveStartMessage()
	{
		Bukkit.broadcastMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Wave " + ChatColor.GOLD + ChatColor.BOLD.toString() + getCurrentWaveIndex() + ChatColor.YELLOW + ChatColor.BOLD.toString() + " has started!" );
	}
	
	public int getCurrentWaveIndex()
	{
		return currentWaveIndex;
	}
	public Wave getCurrentWave()
	{
		return currentWave;
	}
	public void resetWavesIndex()
	{
		currentWaveIndex = 1;
	}
	
	public void setWorld(World world)
	{
		this.world= world; 
	}
	public World getWorld()
	{
		return world;
	}
	
	public void loadWaves() {
	    FileConfiguration config = plugin.getConfig();
	    
	    if (world==null)
	    {
		    world = Bukkit.getWorld(config.getString("world_name"));
	    }
	    int minX = config.getInt("min_x");
	    int maxX = config.getInt("max_x");
	    int minZ = config.getInt("min_z");
	    int maxZ = config.getInt("max_z");
	    
	    ConfigurationSection wavesSection = config.getConfigurationSection("waves");

	    if (wavesSection == null) {
	        plugin.getLogger().warning("No waves found in the config.");
	        return;
	    }

	    for (String key : wavesSection.getKeys(false)) {
	        ConfigurationSection waveData = wavesSection.getConfigurationSection(key);
	        
	        int size = waveData.getInt("size");
	        int mobHealth = waveData.getInt("mob_health");
	        
	        List<EntityType> mobTypes = new ArrayList<>();
	        for (String mobType : waveData.getStringList("mob_types")) {
	            EntityType entity = EntityType.valueOf(mobType);
	            if (entity != null) {
	                mobTypes.add(entity);
	            } else {
	                plugin.getLogger().warning("Invalid mob type: " + mobType);
	            }
	        }

	        List<ItemStack> helmets = getItemsList(waveData.getStringList("helmets"));
	        List<ItemStack> chestplates = getItemsList(waveData.getStringList("chestplates"));
	        List<ItemStack> leggings = getItemsList(waveData.getStringList("leggings"));
	        List<ItemStack> boots = getItemsList(waveData.getStringList("boots"));
	        List<ItemStack> swords = getItemsList(waveData.getStringList("swords"));

	        Wave wave = new Wave(size, mobTypes, mobHealth, world, minX, maxX, minZ, maxZ,
	                helmets, chestplates, leggings, boots, swords);
	        
	        waves.add(wave);
	    }
	}

	private List<ItemStack> getItemsList(List<String> itemData) {
	    List<ItemStack> items = new ArrayList<>();
	    for (String data : itemData) {
	        String[] datas = data.split(" ");
	        
	        if (datas.length > 0) {
	            Material material = Material.matchMaterial(datas[0]);
	            if (material == null) {
	                plugin.getLogger().warning("Invalid material: " + datas[0]);
	                continue; // Skip this item
	            }
	            
	            ItemStack item = new ItemStack(material);
	            
	            if (datas.length > 1) {
	                for (int i = 1; i < datas.length; i++) {
	                    String[] enchantmentData = datas[i].split(":");
	                    if (enchantmentData.length != 2) {
	                        plugin.getLogger().warning("Invalid enchantment format: " + datas[i]);
	                        continue; // Skip this enchantment
	                    }
	                    
	                    Enchantment enchantment = Enchantment.getByName(enchantmentData[0]);
	                    try {
	                        int level = Integer.parseInt(enchantmentData[1]);
	                        if (enchantment != null) {
	                            item.addEnchantment(enchantment, level);
	                        } else {
	                            plugin.getLogger().warning("Invalid enchantment: " + enchantmentData[0]);
	                        }
	                    } catch (NumberFormatException e) {
	                        plugin.getLogger().warning("Invalid enchantment level: " + enchantmentData[1]);
	                    }
	                }
	            }
	            
	            items.add(item);
	        }
	    }
	    return items;
	}

	
}
