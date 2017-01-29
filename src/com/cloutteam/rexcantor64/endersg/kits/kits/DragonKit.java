package com.cloutteam.rexcantor64.endersg.kits.kits;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;

import com.cloutteam.rexcantor64.endersg.Main;
import com.cloutteam.rexcantor64.endersg.kits.Kit;
import com.google.common.collect.Maps;

public class DragonKit extends Kit {

	private HashMap<Skeleton, Player> skeletons = Maps.newHashMap();

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public boolean canUse(Player p) {
		return true;
	}

	@Override
	public void giveItems(Player p) {
		SpawnEgg se = new SpawnEgg(EntityType.SKELETON);
		ItemStack is = se.toItemStack();
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Dragon");
		is.setItemMeta(im);
		p.getInventory().addItem(is);
	}

	@Override
	public boolean isListener() {
		return true;
	}

	@Override
	public ItemStack getShowcaseItem(Player p) {
		SpawnEgg se = new SpawnEgg(EntityType.SKELETON);
		ItemStack is = se.toItemStack();
		ItemMeta im = is.getItemMeta();
		im.setDisplayName((canUse(p) ? ChatColor.GREEN : ChatColor.RED) + "Dragon");
		is.setItemMeta(im);
		if (Main.get().getKit(p) == getID())
			is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		return is;
	}

	@Override
	public String getName() {
		return "Dragon";
	}

	@EventHandler
	public void onDragonSpawn(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		ItemStack is = e.getItem();
		if (is == null)
			return;
		if (is.getType() != Material.MONSTER_EGG)
			return;
		if (!is.hasItemMeta())
			return;
		ItemMeta im = is.getItemMeta();
		if (!im.getDisplayName().equals(ChatColor.GOLD + "Dragon"))
			return;
		e.setCancelled(true);
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		Location l = b.getRelative(e.getBlockFace()).getLocation().clone().add(0.5, 0.5, 0.5);
		Skeleton s = l.getWorld().spawn(l, Skeleton.class);
		s.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
		s.getEquipment().setChestplateDropChance(0);
		s.getEquipment().setItemInMainHand(new ItemStack(Material.WOOD_SWORD));
		s.getEquipment().setItemInMainHandDropChance(0);
		skeletons.put(s, p);
	}

	@EventHandler
	public void onDragonTargetOwner(EntityTargetEvent e) {
		if (skeletons.containsKey(e.getEntity()))
			if (skeletons.get(e.getEntity()) == e.getTarget())
				e.setCancelled(true);
	}

	@EventHandler
	public void onDragonOwnerHit(EntityDamageByEntityEvent e) {
		if (skeletons.containsValue(e.getEntity()))
			if (e.getDamager() instanceof LivingEntity)
				for (Entry<Skeleton, Player> entry : skeletons.entrySet())
					if (e.getEntity().equals(entry.getValue()))
						entry.getKey().setTarget((LivingEntity) e.getDamager());
	}

}
