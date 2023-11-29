package io.github.JoltMuz.Apocalypse; 

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShopItem {
	LinkedHashMap<List<ItemStack>, Integer> tiersPrice;
    String displayName;
    ItemStack displayItem;

    public ShopItem(String displayName, ItemStack displayItem, LinkedHashMap<List<ItemStack>, Integer> tiersPrice) {
        this.tiersPrice = tiersPrice;
        this.displayName = displayName;
        this.displayItem = displayItem;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }
    
    public int getMaxTier()
    {
    	return tiersPrice.size()-1;
    }

    public void purchase(Player player) {

        int tierToPurchase = getNextTier(player);
        
        if (tierToPurchase >= 0) 
        {
            // Get the next tier's items and their count
            Map.Entry<List<ItemStack>, Integer> nextTierEntry = new ArrayList<>(tiersPrice.entrySet()).get(tierToPurchase);
            List<ItemStack> nextTierItems = nextTierEntry.getKey();
            int tierPrice = nextTierEntry.getValue();
            
            PlayerInventory inventory = player.getInventory();
            if (canAfford(player, tierPrice))
            {
            	if (tierToPurchase > 0)
            	{
            		Map.Entry<List<ItemStack>, Integer> currentTierEntry = new ArrayList<>(tiersPrice.entrySet()).get(tierToPurchase-1);
                    List<ItemStack> currentTierItems = currentTierEntry.getKey();
                    for (ItemStack item : currentTierItems)
                	{
                    	if (inventory.contains(item))
                    	{
                    		inventory.removeItem(item);
                    	}
                	}
            	}
            	for (ItemStack item : nextTierItems)
            	{
                	ItemMeta meta = item.getItemMeta();
                	meta.spigot().setUnbreakable(true);
                	item.setItemMeta(meta);
            		if (isHelmet(item))
            		{
            			inventory.setHelmet(item);
            		}
            		else if (isChestplate(item))
            		{
            			inventory.setChestplate(item);
            		}
            		else if (isLeggings(item))
            		{
            			inventory.setLeggings(item);
            		}
            		else if (isBoots(item))
            		{
            			inventory.setBoots(item);
            		}
            		else
            		{
            			inventory.addItem(item);
            		}
            	}
            	removeCandies(inventory, tierPrice);
            	player.sendMessage(ChatColor.BLUE + "✔ " + ChatColor.GRAY + "You purchased " + ChatColor.translateAlternateColorCodes('&',getDisplayName()) + ChatColor.RED + " -" + tierPrice);
            }
            else
            {
            	player.sendMessage(ChatColor.RED + "(!) " + ChatColor.GRAY + "You can't afford this");
            }
        }

    }
    
    private void removeCandies(Inventory inventory, int price)
    {
    	int count = 0;
        
        for (ItemStack item : inventory.getContents()) 
        {
            if (item != null && item.getType() == Material.SKULL_ITEM) 
            {
                count += item.getAmount();
            }
        }
        int remaining = count - price;
        inventory.remove(Material.SKULL_ITEM);
        if (remaining > 0)
        {
            inventory.addItem(getCandyItems(remaining));
        }
        
    }

    private List<ItemStack> getItemsInInventory(PlayerInventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        items.addAll(Arrays.asList(inventory.getContents()));
        items.addAll(Arrays.asList(inventory.getArmorContents()));
        return items;
    }
    
    public int getPrice(int tierToPurchase)
    {
    	Map.Entry<List<ItemStack>, Integer> nextTierEntry = new ArrayList<>(tiersPrice.entrySet()).get(tierToPurchase);
        return nextTierEntry.getValue();
    }

    public int getNextTier(Player player) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> itemsInInventory = getItemsInInventory(inventory);
        int currentTier = 0; 
        
        // Iterate through the tiers in the tiersPrice map
        for (Map.Entry<List<ItemStack>, Integer> entry : tiersPrice.entrySet()) 
        {
            List<ItemStack> tierItems = entry.getKey();

            if (itemsInInventory.containsAll(tierItems))
            {
            	if (currentTier == (tiersPrice.size() -1))
            	{
            		return currentTier;
            	}
            	else
            	{
            		return currentTier + 1;
            	}
            	
            }
            // If the player has all items from the current tier, return its index

            currentTier++;  // Move to the next tier
        }

        // If no matching tier is found, you can return -1 or handle it in your specific way
        return 0;
    }
    
    private boolean canAfford(Player player, int price)
    {
    	return player.getInventory().contains(Material.SKULL_ITEM, price);
    }
    
    public boolean isHelmet(ItemStack item) {
        Material itemType = item.getType();
        return itemType == Material.LEATHER_HELMET || 
               itemType == Material.IRON_HELMET || 
               itemType == Material.GOLD_HELMET || 
               itemType == Material.DIAMOND_HELMET || 
               itemType == Material.CHAINMAIL_HELMET;
    }

    public boolean isChestplate(ItemStack item) {
        Material itemType = item.getType();
        return itemType == Material.LEATHER_CHESTPLATE || 
               itemType == Material.IRON_CHESTPLATE || 
               itemType == Material.GOLD_CHESTPLATE || 
               itemType == Material.DIAMOND_CHESTPLATE || 
               itemType == Material.CHAINMAIL_CHESTPLATE;
    }

    public boolean isLeggings(ItemStack item) {
        Material itemType = item.getType();
        return itemType == Material.LEATHER_LEGGINGS || 
               itemType == Material.IRON_LEGGINGS || 
               itemType == Material.GOLD_LEGGINGS || 
               itemType == Material.DIAMOND_LEGGINGS || 
               itemType == Material.CHAINMAIL_LEGGINGS;
    }

    public boolean isBoots(ItemStack item) {
        Material itemType = item.getType();
        return itemType == Material.LEATHER_BOOTS || 
               itemType == Material.IRON_BOOTS || 
               itemType == Material.GOLD_BOOTS || 
               itemType == Material.DIAMOND_BOOTS || 
               itemType == Material.CHAINMAIL_BOOTS;
    }
    
    private ItemStack getCandyItems(int amount)
    {
    	ItemStack headItem = new ItemStack(Material.SKULL_ITEM, amount, (short) 3);
        SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
        skullMeta.setOwner("Candy_Cane");
        skullMeta.setDisplayName(ChatColor.YELLOW + "Candy");
        headItem.setItemMeta(skullMeta);
        return headItem;
    }

}
