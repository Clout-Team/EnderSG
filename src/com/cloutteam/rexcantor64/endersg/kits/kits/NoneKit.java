package com.cloutteam.rexcantor64.endersg.kits.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.cloutteam.rexcantor64.endersg.kits.Kit;

public class NoneKit extends Kit{

	@Override
	public int getID() {
		return -1;
	}

	@Override
	public boolean canUse(Player p) {
		return true;
	}

	@Override
	public void giveItems(Player p) {
	}

	@Override
	public boolean isListener() {
		return false;
	}

	@Override
	public ItemStack getShowcaseItem(Player p) {
		return new ItemStack(Material.AIR);
	}

	@Override
	public String getName() {
		return "--";
	}

}
