package com.cloutteam.rexcantor64.endersg.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.cloutteam.rexcantor64.endersg.Main;

public class SupplyDropTask extends BukkitRunnable {

	@Override
	public void run() {
		Main.get().spawnRandomSupplyDrop();
	}

	public static void start() {
		new SupplyDropTask().runTaskTimer(Main.get(), 0, Main.get().getConfig().getInt("supplydrop.delay", 330));
	}

}
