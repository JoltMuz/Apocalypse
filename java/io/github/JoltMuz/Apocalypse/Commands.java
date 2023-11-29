package io.github.JoltMuz.Apocalypse;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor
{
	WaveManager waveManager;
	JavaPlugin plugin;
	
	private String not_OP = ChatColor.RED + "(!) " + ChatColor.GRAY + "This command is for operators only.";
	private String started =ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Started Wave: ";
	private String errorMark = ChatColor.RED + "(!) " + ChatColor.GRAY;
	private String reloaded =ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Reloaded Config!";
	
	private String not_Player = ChatColor.RED + "(!) " + ChatColor.GRAY + "This command can be used by players only.";
	private String allWavesDone = ChatColor.RED + "(!) " + ChatColor.GRAY + "All waves done";
	

	
	public Commands(WaveManager waveManager, JavaPlugin plugin)
	{
		this.waveManager = waveManager;
		this.plugin = plugin;
	}
	

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp())
		{
			sender.sendMessage(not_OP);
			return true;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
			if (waveManager.getCurrentWaveIndex() == waveManager.getWaves().size())
			{
				sender.sendMessage(allWavesDone);
				return true;
			}
		    try {
		        sender.sendMessage(started + waveManager.getCurrentWaveIndex());
		        if (waveManager.getCurrentWave() != null) {
		            Iterator<LivingEntity> iterator = waveManager.getCurrentWave().getMobs().iterator();
		            while (iterator.hasNext()) {
		                LivingEntity mob = iterator.next();
		                iterator.remove(); // Remove the mob from the list
		                mob.setHealth(0);
		            }
		        }
		        waveManager.startNextWave();
		    } catch (IllegalArgumentException e) {
		        sender.sendMessage(errorMark + e.getMessage());
		    }
		}

		else if (args.length == 1 && args[0].equalsIgnoreCase("reload"))
		{
			try
			{
				waveManager.resetWavesIndex();
				plugin.reloadConfig();
				sender.sendMessage(reloaded);
			}
			catch (IllegalArgumentException e)
			{
				sender.sendMessage(errorMark + e.getMessage());
			}
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("shop"))
		{
			if (sender instanceof Player)
			{
				Location location = ((Player) sender).getLocation();
				Shop.establishShop(location);
			}
			else
			{
				sender.sendMessage(not_Player);
			}
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("remaining"))
		{
			if (waveManager.getCurrentWave() == null)
			{
				sender.sendMessage(ChatColor.GRAY+ "No waves.");
				return true;
			}
			sender.sendMessage(ChatColor.GRAY+ "Mobs remaining in current Wave: "+ ChatColor.YELLOW+ waveManager.getCurrentWave().getMobs().size());
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("world"))
		{
			if (sender instanceof Player)
			{
				World world = ((Player) sender).getWorld();
				waveManager.setWorld(world);
				sender.sendMessage(ChatColor.GREEN + "✔ " + ChatColor.GRAY + "World changed to" + ChatColor.YELLOW + world.getName());
			}
			else
			{
				sender.sendMessage(not_Player);
			}
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("checkworld"))
		{
				sender.sendMessage(ChatColor.GREEN + "✔ " + ChatColor.GRAY + "World is: " + ChatColor.YELLOW + waveManager.getWorld());
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("killRemaining")) 
		{
			if (waveManager.getCurrentWave() == null)
			{
				sender.sendMessage(ChatColor.GRAY+ "No waves.");
				return true;
			}
		    sender.sendMessage(ChatColor.GRAY + "Killed Mobs: " + ChatColor.YELLOW + waveManager.getCurrentWave().getMobs().size());

		    Iterator<LivingEntity> mobIterator = waveManager.getCurrentWave().getMobs().iterator();
		    while (mobIterator.hasNext()) {
		        LivingEntity mob = mobIterator.next();
		        mobIterator.remove(); // Remove the mob from the list
		        mob.setHealth(0);
		    }
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("tp"))
		{
			if (waveManager.getCurrentWave() == null)
			{
				sender.sendMessage(ChatColor.GRAY+ "No waves.");
				return true;
			}
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				if( waveManager.getCurrentWave().getMobs().size() > 0)
				{
					player.teleport(waveManager.getCurrentWave().getMobs().get(0));
				}
			}
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("tpall"))
		{
			if (waveManager.getCurrentWave() == null)
			{
				sender.sendMessage(ChatColor.GRAY+ "No waves.");
				return true;
			}
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				for(LivingEntity mob: waveManager.getCurrentWave().getMobs())
				{
					mob.teleport(player);
				}
			}
		}

		else
		{
			sendCommandHelp(sender);
		}
		return true;
	}
	
	private void sendCommandHelp(CommandSender sender)
	{
		StringBuilder message = new StringBuilder();
	   	 message.append(ChatColor.GOLD + "   │  " + ChatColor.YELLOW + "" + "Apocalypse" + ChatColor.WHITE + " " + "Commands \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/apocalypse start ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Start next wave\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/apocalypse reload ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Reset wave and reload the config.\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/apocalypse remaining ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Tells how many remaining mobs in current wave.\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/apocalypse killRemaining ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Kills remaining mobs in current wave\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/apocalypse shop ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Establish a shop at your location\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + ChatColor.ITALIC.toString() + "NOTE: Must be a player to use this. \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/apocalypse ? ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Shows this message.\n")
			   	.append(" ");
	       	
	   	 sender.sendMessage(message.toString());
	}

}
