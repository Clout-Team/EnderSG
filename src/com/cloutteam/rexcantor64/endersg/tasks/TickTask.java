package com.cloutteam.rexcantor64.endersg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.cloutteam.rexcantor64.endersg.Main;

import uk.endercraft.endercore.GameState;

public class TickTask extends BukkitRunnable {

	private Location center;

	@SuppressWarnings("deprecation")
	public void run() {
		Main.get().updateScoreboard();
		if (Main.get().getGameState() == GameState.COUNTDOWN
				|| Main.get().getGameState() == GameState.PLAYING_COUNTDOWN) {
			int timer = Main.get().getTimer();
			if (timer == 0) {
				if (Main.get().getGameState() == GameState.COUNTDOWN) {
					Main.get().teleportToArena();
					Main.get().setTimer(10);
					Main.get().setGameState(GameState.PLAYING_COUNTDOWN);
				} else
					Main.get().startGame();
				return;
			}
			if (timer % 10 == 0 || timer <= 5) {
				Main.get().broadcast("countdown", timer);
				switch (timer) {
				case 5:
				case 4:
					for (Player p : Bukkit.getOnlinePlayers())
						p.sendTitle(ChatColor.GREEN + "" + timer, "");
					break;
				case 3:
					for (Player p : Bukkit.getOnlinePlayers())
						p.sendTitle(ChatColor.YELLOW + "" + timer, "");
					break;
				case 2:
					for (Player p : Bukkit.getOnlinePlayers())
						p.sendTitle(ChatColor.RED + "" + timer, "");
					break;
				case 1:
					for (Player p : Bukkit.getOnlinePlayers())
						p.sendTitle(ChatColor.DARK_RED + "" + timer, "");
					break;
				}
			}
			Main.get().decreaseTimer();
		} else if (Main.get().getGameState() == GameState.PLAYING) {
			Main.get().decreaseTimer();
			if (center == null) {
				center = new Location(Bukkit.getWorld("world"), Main.get().getConfig().getInt("centerLocation.x"),
						Main.get().getConfig().getInt("centerLocation.y"),
						Main.get().getConfig().getInt("centerLocation.z"));
				WorldBorder border = center.getWorld().getWorldBorder();
				border.setCenter(center);
				border.setDamageAmount(1);
				border.setDamageBuffer(1);
				border.setWarningDistance(3);
				border.setSize(Main.get().getConfig().getInt("border.start-radius", 256));
			}
			int timer = Main.get().getTimer();
			if (timer == 0) {
				if (Main.get().isGraceActive())
					Main.get().endGracePeriod();
				else
					Main.get().startBorderShrinking();
				return;
			}
			if ((timer % 30 == 0 || timer <= 5) && timer > 0 && Main.get().isGraceActive())
				Main.get().broadcast("countdown.grace", timer);
		}
	}

}
