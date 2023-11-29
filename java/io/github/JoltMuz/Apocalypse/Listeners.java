package io.github.JoltMuz.Apocalypse;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.md_5.bungee.api.ChatColor;

public class Listeners implements Listener 
{

	private WaveManager waveManager;
	private PointsManager pointsManager;
	public Listeners(WaveManager waveManager, PointsManager pointsManager) 
	{
		this.waveManager = waveManager;
		this.pointsManager = pointsManager;
	}
	Wither wither;
	
	@EventHandler
	public void damageMob(EntityDamageByEntityEvent event)
	{
		if (event.getEntity() instanceof Villager)
		{
			if (event.getEntity().getCustomName().contains("Shop"))
			{
				event.setCancelled(true);
			}
		}
		if (!(event.getEntity() instanceof LivingEntity))
		{
			return;
		}
		Wave wave = waveManager.getCurrentWave();
		if (wave == null)
		{
			return;
		}
		LivingEntity entity = (LivingEntity) event.getEntity();
		if (wave.getMobs().contains(entity))
		{
			entity.setCustomName(getHealthBar(entity));
			if (event.getDamager() instanceof Player)
			{
				pointsManager.addPoints((Player) event.getDamager(), (int) event.getFinalDamage());
			}
			
		}
		if (entity instanceof Wither)
		{
			pointsManager.addPoints((Player) event.getDamager(), (int) event.getFinalDamage());
		}
	}
	
	private String getHealthBar(LivingEntity mob) {
		StringBuilder healthbar = new StringBuilder();
	    double healthPercentage = mob.getHealth() / mob.getMaxHealth();

	    healthbar.append(ChatColor.RED + "❤ ");
	    
	    ChatColor healthColor;
	    if (healthPercentage >= 0.6) {
	        healthColor = ChatColor.GREEN;
	    } else if (healthPercentage >= 0.3) {
	        healthColor = ChatColor.YELLOW;
	    } else {
	        healthColor = ChatColor.RED;
	    }

	    int numBars = (int) (healthPercentage * 10);
	    
	    healthbar.append(healthColor);

	    for (int i = 0; i < numBars; i++) {
	        healthbar.append("█");
	    }
	    healthbar.append(ChatColor.RESET);
	    for (int i = numBars; i < 10; i++) {
	        healthbar.append("█");
	    }

	    return healthbar.toString();
	}
	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
		Player player = event.getEntity();
		player.setHealth(20);
		player.setGameMode(GameMode.SPECTATOR);
        TitleSender.sendTitle(player, ChatColor.RED.toString() +  "You Died", ChatColor.YELLOW.toString() + "Respawning in 10 seconds...", 10, 160, 10);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> 
        {
			if (player.getGameMode() == GameMode.SPECTATOR) 
			{
				Location location = player.getLocation();
				player.teleport(location.getWorld().getHighestBlockAt(location).getLocation().add(0,1,0));
				player.setGameMode(GameMode.SURVIVAL);
				player.sendMessage(ChatColor.GREEN + "Respawned!");
			} 
		}, 20L * 10);
    }
	
	@EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
		
		if (!(event.getEntity() instanceof LivingEntity))
		{
			return;
		}
		Wave wave = waveManager.getCurrentWave();
		if (wave==null)
		{
			return;
		}
		LivingEntity entity = (LivingEntity) event.getEntity();
		if (wave.getMobs().contains(entity))
		{
            wave.removeMob(event.getEntity());
            event.setDroppedExp(0);
        }
		
		if (entity instanceof Wither)
		{
			List<Player> topPlayers = pointsManager.getTopPlayers(3);

			StringBuilder message = new StringBuilder();
			message.append(ChatColor.YELLOW + ChatColor.BOLD.toString() + "The Apocalypse has Ended!\n")
			       .append(" \n")
			       .append(ChatColor.GREEN + "――――――――――――――――――――――――――――――――\n")
			       .append(" \n");

			for (int i = 0; i < topPlayers.size(); i++) 
			{
			    String playerName = topPlayers.get(i).getName();
			    ChatColor rankColor = ChatColor.YELLOW;
			    if (i == 1) 
			    {
			        rankColor = ChatColor.WHITE;
			    } 
			    else if (i == 2) 
			    {
			        rankColor = ChatColor.GOLD;
			    }

			    message.append(rankColor + " #" + (i + 1) + " - " + playerName + "\n");
			}
			message.append(" ");

			Bukkit.broadcastMessage(message.toString());
			return;
		}
		
		if (wave.getMobs().isEmpty())
		{
			if (waveManager.getCurrentWaveIndex() == waveManager.getWaves().size() + 1)
			{
				if (wither == null)
				{
					summonWyvern(waveManager.getWorld().getSpawnLocation().add(0,1,0));
					return;
				}
				
			}
			waveManager.startNextWave();
		}
    }
	
	private void summonWyvern(Location location)
	{
		Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "The Wyvern has awoken!");

        wither = (Wither) location.getWorld().spawnEntity(location, EntityType.WITHER);
        wither.setMaxHealth(2000);
        wither.setHealth(2000);
        wither.setCustomName(ChatColor.LIGHT_PURPLE + "Wyvern");
        wither.setCustomNameVisible(true);

	}
	
	@EventHandler
    public void onEntityTarget(EntityTargetEvent event) 
	{
        if (event.getTarget() != null && event.getTarget() instanceof Villager) {
            event.setCancelled(true);
        }
    }
	
	
	@EventHandler
    public void inventoryClick(InventoryClickEvent e)
    {
    	if (e.getInventory().getName().contains("Points"))
    	{
    		e.setCancelled(true);
    	}
    }

}
