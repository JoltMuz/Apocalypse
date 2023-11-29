package io.github.JoltMuz.Apocalypse;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PointsManager implements CommandExecutor
{
    private Map<Player, Integer> playerPoints;
    private Map<String, Integer> unrewardedPoints;
    
    int pointsConversion;

    public PointsManager(JavaPlugin plugin) 
    {
        playerPoints = new HashMap<>();
        unrewardedPoints = new HashMap<>();
        
        pointsConversion = plugin.getConfig().getInt("points_to_candy");
        
    }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			showLeaderboardInventory(player);
		}
		
		return true;
	}

    public int getPoints(Player player) {
        return playerPoints.getOrDefault(player, 0);
    }

    public void setPoints(Player player, int points) {
        playerPoints.put(player, points);
    }

    public void addPoints(Player player, int pointsToAdd) 
    {
        int currentPoints = getPoints(player);
        playerPoints.put(player, currentPoints + pointsToAdd);
        ActionBar.sendToPlayer(player, ChatColor.GOLD + "Points: " + ChatColor.YELLOW + " + " + pointsToAdd + " = " + getPoints(player) );
        updateCandies(player,pointsToAdd);
        
    }

    public void subtractPoints(Player player, int pointsToSubtract) {
        int currentPoints = getPoints(player);
        playerPoints.put(player, currentPoints - pointsToSubtract);
    }
    
    private void updateCandies(Player player, int pointsToAdd)
    {
    	String name = player.getName();
    	int points = unrewardedPoints.getOrDefault(player.getName(), 0);
    	unrewardedPoints.put(player.getName(), points + pointsToAdd);
    	if (unrewardedPoints.get(name) >= pointsConversion)
    	{
    		unrewardedPoints.put(name, points-pointsConversion);
    		player.getInventory().addItem(getCandyItem());
    		player.sendMessage(ChatColor.YELLOW + "You got a candy for " + pointsConversion + " points!");
    	}
    }
    
    private ItemStack getCandyItem()
    {
    	ItemStack headItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
        skullMeta.setOwner("Candy_Cane");
        skullMeta.setDisplayName(ChatColor.YELLOW + "Candy");
        headItem.setItemMeta(skullMeta);
        return headItem;
    }
    
    private void showLeaderboardInventory(Player player)
    {
    	Inventory inventory = Bukkit.createInventory(player, 54, "Your Points: " + getPoints(player));
    	List<Player> topPlayers = getTopPlayers(54);
        
        for (Player participant : topPlayers) 
        {
            ItemStack playerItem = createPlayerItem(participant);
            inventory.addItem(playerItem);
        }

        player.openInventory(inventory);
    
    }

	public List<Player> getTopPlayers(int count) 
	{
	    return playerPoints.entrySet()
	            .stream()
	            .sorted(Comparator.comparingInt(entry -> -entry.getValue())) // Sort in descending order
	            .limit(count)
	            .map(HashMap.Entry::getKey)
	            .collect(Collectors.toList());
	}
	
	private ItemStack createPlayerItem(Player player) 
	{
		String playerName = player.getName();
	    // Customize the ItemStack creation as per your plugin's needs
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		if (meta != null) 
		{
		    meta.setOwner(playerName);
		    meta.setDisplayName(ChatColor.GOLD + playerName + ChatColor.DARK_GRAY + " [" + ChatColor.YELLOW + getPoints(player) + ChatColor.DARK_GRAY + "]");	
		        head.setItemMeta(meta);
		}
		return head;
	}
}


