package com.cloutteam.rexcantor64.endersg.listeners;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.cloutteam.rexcantor64.endersg.Main;
import com.cloutteam.rexcantor64.endersg.config.ChestConfig;
import com.cloutteam.rexcantor64.endersg.config.SupplyChestConfig;
import com.google.common.collect.Lists;

import uk.endercraft.endercore.GameState;

public class BlockListener implements Listener {

	private List<Location> openedChests = Lists.newArrayList();

	@EventHandler
	public void onEmptyChestOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Block b = e.getClickedBlock();
		if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST)
			return;
		if (openedChests.contains(b.getLocation()))
			return;
		int size = 27;
		for (BlockFace face : new BlockFace[] { BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST }) {
			Block relative = b.getRelative(face);
			if (relative.getType() == b.getType()) {
				size = 54;
				break;
			}
		}
		Chest chest = (Chest) b.getState();
		for (Entry<Integer, ItemStack> entry : ChestConfig.get().getRandomItems(size).entrySet())
			chest.getBlockInventory().setItem(entry.getKey(), entry.getValue());
		openedChests.add(b.getLocation());
	}

	@EventHandler
	public void onSupplyDropOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Block b = e.getClickedBlock();
		if (b.getType() != Material.ENDER_CHEST)
			return;
		b.setType(Material.AIR);
		b.getWorld().dropItem(b.getLocation().add(0.5, 0.5, 0.5), SupplyChestConfig.get().getRandomItem())
				.setVelocity(new Vector(0, 0, 0));
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING) {
			e.setCancelled(true);
			return;
		}

		if (e.getBlockPlaced().getType() == Material.CHEST) {
			openedChests.add(e.getBlockPlaced().getLocation());
		}
		if (e.getBlockPlaced().getType() == Material.TNT) {
			e.getBlockPlaced().setType(Material.AIR);
			e.getPlayer().getWorld().spawn(e.getBlockPlaced().getLocation().clone().add(0.5, 0.5, 0.5),
					TNTPrimed.class);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onServerPing(ServerListPingEvent e) {
		int i = 2;
		switch (Main.get().getGameState()) {
		case COUNTDOWN:
		case WAITING:
			i = 0;
			break;
		case PLAYING_COUNTDOWN:
		case PLAYING:
			i = 1;
			break;
		default:
			break;
		}
		e.setMotd(i + ";" + Main.get().getConfig().getString("map-name", "Unknown. Please warn a moderator."));
	}

}
