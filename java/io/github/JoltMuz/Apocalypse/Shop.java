package io.github.JoltMuz.Apocalypse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import net.md_5.bungee.api.ChatColor;

public class Shop implements Listener
{
	JavaPlugin plugin;
	
	static int size;
	private static Map<Integer, ShopItem> slotItems;

	public Shop(JavaPlugin plugin) 
	{
		this.plugin = plugin;
		slotItems = new HashMap<>();
		loadShop();
		
	}
	
	public static void openShop(Player player)
	{
		Inventory inventory = Bukkit.createInventory(player, size, "Shop");
		for (int i = 0; i < getSize(); i++)
		{
			if (slotItems.containsKey(i))
			{
				ShopItem shopItem = slotItems.get(i);
				ItemStack item = shopItem.getDisplayItem();
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', shopItem.getDisplayName()));
				
			    List<String> lore = new ArrayList<>();
			    int nextTier = shopItem.getNextTier(player);
			    String tierNumber = ChatColor.YELLOW + "Tier: " + ChatColor.GRAY + nextTier;
			    if (nextTier == shopItem.getMaxTier())
			    {
			    	tierNumber.concat(ChatColor.YELLOW + ChatColor.BOLD.toString() + "MAX");
			    }
			    lore.add(tierNumber);
			    lore.add(ChatColor.YELLOW + "Price: " + ChatColor.GRAY + shopItem.getPrice(nextTier));
			    meta.setLore(lore);
			    item.setItemMeta(meta);
				

				inventory.setItem(i,slotItems.get(i).getDisplayItem());
			}
			else
			{
				inventory.setItem(i,new ItemStack(Material.STAINED_GLASS_PANE));
			}
		}
		player.openInventory(inventory);
		
	}
	
	@EventHandler
	public void purchase(InventoryClickEvent event)
	{
		if (!event.getInventory().getName().equalsIgnoreCase("Shop"))
		{
			return;
		}
		
		event.setCancelled(true);
		
		if (!slotItems.containsKey(event.getSlot()))
		{
			
			return;
		}
	
		ShopItem shopItem = slotItems.get(event.getSlot());
		Player player = (Player) event.getWhoClicked();
		shopItem.purchase(player);
		player.closeInventory();
		openShop(player);
		
	}
	
	public static void establishShop(Location location)
	{
		Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		villager.setCustomName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Shop");
	    villager.setCustomNameVisible(true);
	    villager.setRemoveWhenFarAway(false);
	}
	
	@EventHandler
    public void onPlayerRightClickEntity(PlayerInteractEntityEvent event) 
	{
        if (event.getRightClicked() instanceof Villager) {
            // Prevent the default interaction with the Villager

            // Open your custom shop GUI
            Player player = event.getPlayer();
            Villager villager = (Villager) event.getRightClicked();
            if (villager.getCustomName().equals(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Shop"))
            {
            	event.setCancelled(true);
            	openShop(player);
            }
        }
	}
	
	private static int getSize()
	{
		return size;
	}
	private static void setSize(int newSize)
	{
		size = newSize;
	}
	
	private void loadShop() {
	    FileConfiguration config = plugin.getConfig();
	    ConfigurationSection shopData = config.getConfigurationSection("shop");
	    setSize(config.getInt("shop_size"));
	    // Define the slotItems map to store shop items
	    
	    for (String key : shopData.getKeys(false)) {
	        ConfigurationSection shopItemData = shopData.getConfigurationSection(key);

	        int slot = shopItemData.getInt("slot");
	        String displayName = shopItemData.getString("display_name");
	        ItemStack displayItem = getItem(shopItemData.getString("display_item"));
	        ItemMeta meta = displayItem.getItemMeta();
	        meta.setDisplayName(displayName);
	        displayItem.setItemMeta(meta);

	        List<String> itemData = shopItemData.getStringList("tiers");
	        LinkedHashMap<List<ItemStack>, Integer> tiersPrices = getTiersPrices(itemData);

	        ShopItem shopItem = new ShopItem(displayName, displayItem, tiersPrices);
	        slotItems.put(slot, shopItem);
	    }
	    // Store the slotItems map in your class for later use.
	}

	private LinkedHashMap<List<ItemStack>, Integer> getTiersPrices(List<String> itemData) 
	{
		LinkedHashMap<List<ItemStack>, Integer> tiersPrices = new LinkedHashMap<>();
	    for (String data : itemData) 
	    {
	        String[] price_items = data.split(" - ");
	        int price = Integer.parseInt(price_items[0]);
	        
	        String[] itemsData = price_items[1].split(" ");
	        List<ItemStack> items = new ArrayList<>();
	        
	        for (int i = 0; i < itemsData.length; i++) 
	        {
	            ItemStack item = getItem(itemsData[i]);
	            
	            if (item != null) 
	            {
	                items.add(item);
	            }
	        }
	        tiersPrices.put(items, price);
	    }
	    return tiersPrices;
	}

	private ItemStack getItem(String string) 
	{
	    String[] materialEnchantments = string.split("\\|");
	    Material material = Material.matchMaterial(materialEnchantments[0]);
	    if (material == null)
	    {
	    	ItemStack item = getPotion(materialEnchantments[0]);
	    	if (item == null)
	    	{
	    		plugin.getLogger().warning("Invalid item format: " + materialEnchantments[0]);
	    	}
	    	return item;
	    }
	    ItemStack item = new ItemStack(material);
	    
	    if (materialEnchantments.length > 1)
	    {
	    	String[] enchantments = materialEnchantments[1].split(",");
	    	for (String enchantmentString : enchantments)
	    	{
	    		String[] enchantmentData = enchantmentString.split(":");
	            if (enchantmentData.length != 2) 
	            {
	                plugin.getLogger().warning("Invalid enchantment format: " + enchantmentString);
	                continue; // Skip this enchantment
	            }

	            Enchantment enchantment = Enchantment.getByName(enchantmentData[0]);
	            try {
	                int level = Integer.parseInt(enchantmentData[1]);
	                if (enchantment != null) 
	                {
	                    item.addUnsafeEnchantment(enchantment, level);
	                } else 
	                {
	                    plugin.getLogger().warning("Invalid enchantment: " + enchantmentData[0]);
	                }
	            } catch (NumberFormatException e) {
	                plugin.getLogger().warning("Invalid enchantment level: " + enchantmentData[1]);
	            }
	    	}
	    }
	    return item;
	}
	
	private ItemStack getPotion(String string)
	{
	    String[] splitString = string.split(":");
	    
	    if (splitString.length < 2) {
	        plugin.getLogger().warning("Invalid potion format: " + string);
	        return null; // Handle the error as needed
	    }
	    
	    Potion potion = new Potion(PotionType.valueOf(splitString[0]));
	    
	    try
	    {
	        potion.setLevel(Integer.parseInt(splitString[1]));
	    }
	    catch (NumberFormatException e)
	    {
	        plugin.getLogger().warning("Invalid potion level: " + splitString[1]);
	    }
	    potion.setSplash(true);

	    return potion.toItemStack(1);
	}

}