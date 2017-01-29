package com.cloutteam.rexcantor64.endersg.kits;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class Kit implements Listener{
	
	public abstract int getID();
	
	public abstract boolean canUse(Player p);
	
	public abstract void giveItems(Player p);
	
	public abstract boolean isListener();
	
	public abstract ItemStack getShowcaseItem(Player p);
	
	public abstract String getName();

}
