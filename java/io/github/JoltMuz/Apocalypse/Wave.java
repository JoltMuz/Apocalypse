package io.github.JoltMuz.Apocalypse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;


public class Wave {

	//for random locations around the area
	World world;
	int minX, maxX,  minZ, maxZ;
	
	//Wave's mobs
	private int size;
	
	private int mobHealth;
	
    private List<EntityType> mobTypes;
    
    private List<ItemStack> helmets;
    private List<ItemStack> chestplates;
    private List<ItemStack> leggings;
    private List<ItemStack> boots;
    private List<ItemStack> swords;
    
    private List<LivingEntity> waveMobs = new ArrayList<>();
	
	public Wave(int size, List<EntityType> mobTypes, int mobHealth,
			World world, int minX, int maxX, int minZ, int maxZ,
			List<ItemStack> helmets,List<ItemStack> chestplates,List<ItemStack> leggings,List<ItemStack> boots,List<ItemStack> swords)
	{
		this.size = size;
        this.mobTypes = mobTypes;
        this.mobHealth = mobHealth;
        
        this.world = world;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        
        this.helmets = helmets;
        this.chestplates = chestplates;
        this.leggings = leggings;
        this.boots = boots;
        this.swords = swords;

	}
	public void setWorld(World world)
	{
		this.world = world;
	}
	
	public void start() 
	{
        for (int i = 0; i < size; i++) {
            
        	Location randomLocation = findSuitableLocation();
        	LivingEntity mob = generateRandomMob(randomLocation);
        	waveMobs.add(mob);
        }
    }
	
	public List<LivingEntity> getMobs()
	{
		return waveMobs;
	}
	
	public void removeMob(LivingEntity mob)
	{
		waveMobs.remove(mob);
	}
	
	private Location findSuitableLocation() 
	{
	    int maxAttempts = 100; // Limit the number of attempts to find a suitable location
	    
	    for (int attempt = 0; attempt < maxAttempts; attempt++) {
	        int randomX = minX + (int) (Math.random() * (maxX - minX + 1));
	        int randomZ = minZ + (int) (Math.random() * (maxZ - minZ + 1));
	        int coordinateY = world.getHighestBlockYAt(randomX, randomZ) + 1;
	        Location randomLocation = new Location(world, randomX, coordinateY, randomZ);
	        
	        if (!randomLocation.getBlock().isLiquid()) {
	            return randomLocation; // Found a suitable location
	        }
	    }
	    
	    return new Location(world, 0, world.getHighestBlockYAt(0, 0) + 1, 0); // Couldn't find a suitable location after maxAttempts
	}
	
	private LivingEntity generateRandomMob(Location location) {
	    // Select a random mob type from the list
	    EntityType randomMobType = mobTypes.get((int) (Math.random() * mobTypes.size()));
	    LivingEntity mob = (LivingEntity) location.getWorld().spawnEntity(location, randomMobType);
	    
	    mob.setMaxHealth(mobHealth);
	    mob.setHealth(mobHealth);
	    
	    mob.setCustomNameVisible(true);
	    mob.setCustomName(ChatColor.RED + "❤"+ ChatColor.GREEN + "██████████");
	    
	    mob.setCanPickupItems(false);
	    mob.setRemoveWhenFarAway(false);

	    List<ItemStack> gearList = getRandomGearSet();
	    equipMobWithGear(mob, gearList);

	    return mob;
	}

	private List<ItemStack> getRandomGearSet() {

	    // Select a random set of armor
	    List<ItemStack> randomGearSet = new ArrayList<>();
	    randomGearSet.add(getRandomItemFromList(helmets));
	    randomGearSet.add(getRandomItemFromList(chestplates));
	    randomGearSet.add(getRandomItemFromList(leggings));
	    randomGearSet.add(getRandomItemFromList(boots));
	    
	    randomGearSet.add(getRandomItemFromList(swords));

	    return randomGearSet;
	}

	private void equipMobWithGear(LivingEntity mob, List<ItemStack> gearSet) {
	    // Equip the mob with the selected armor set
	    mob.getEquipment().setHelmet(gearSet.get(0));
	    mob.getEquipment().setChestplate(gearSet.get(1));
	    mob.getEquipment().setLeggings(gearSet.get(2));
	    mob.getEquipment().setBoots(gearSet.get(3));
	    mob.getEquipment().setItemInHand(gearSet.get(4));
	}
	
	private ItemStack getRandomItemFromList(List<ItemStack> itemList) {
	    // Select a random item from the provided list.
	    if (itemList.isEmpty()) {
	        return new ItemStack(Material.AIR); // Default to no item if the list is empty.
	    }
	    return itemList.get((int) (Math.random() * itemList.size()));
	}

}
