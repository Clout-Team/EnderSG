package com.cloutteam.rexcantor64.endersg.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;

import com.cloutteam.rexcantor64.endersg.Main;
import com.cloutteam.rexcantor64.endersg.kits.Kit;
import com.cloutteam.rexcantor64.endersg.kits.KitsManager;
import com.cloutteam.rexcantor64.endersg.utils.FireworkUtils;
import com.cloutteam.rexcantor64.endersg.utils.NMSUtils;

import uk.endercraft.endercore.GameState;
import uk.endercraft.endercore.guiapi.Gui;
import uk.endercraft.endercore.guiapi.GuiButton;
import uk.endercraft.endercore.language.LanguageMain;
import uk.endercraft.endercore.managers.PlayerManager;
import uk.endercraft.endercore.utils.CustomStack;

public class PlayerListener implements Listener {

	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		PlayerManager.registerPlayer(p);
		e.setJoinMessage(null);
		if (Main.get().getGameState() == GameState.WAITING || Main.get().getGameState() == GameState.COUNTDOWN)
			Main.get().broadcast("join", p.getName(), Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers());
		if (Main.get().getGameState() == GameState.WAITING)
			if (Bukkit.getOnlinePlayers().size() == Main.get().getConfig().getInt("minimum-players", 2)) {
				Main.get().broadcast("countdown.start");
				Main.get().setGameState(GameState.COUNTDOWN);
				Main.get().setTimer(30);
			}
		Main.get().setKit(p, 0);
		p.setMaxHealth(20);
		Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
			public void run() {
				p.getInventory().clear();
				p.setTotalExperience(0);
				p.setExp(0);
				p.setLevel(0);
				p.setHealth(p.getMaxHealth());
				p.setFoodLevel(20);
				for (PotionEffect pe : p.getActivePotionEffects())
					p.removePotionEffect(pe.getType());
				if (Main.get().getGameState() != GameState.PLAYING) {
					p.setGameMode(GameMode.SURVIVAL);
					p.teleport(new Location(Bukkit.getWorld("world"),
							Main.get().getConfig().getInt("lobbyLocation.x", 493),
							Main.get().getConfig().getInt("lobbyLocation.y", 4),
							Main.get().getConfig().getInt("lobbyLocation.z", 2061)).add(0.5, 0.5, 0.5));
					p.getInventory().setItem(4,
							new CustomStack().setMaterial(Material.PAPER)
									.setDisplayName(ChatColor.GREEN + LanguageMain.get(p, "kits"))
									.setEnchantments(new Object[] { Enchantment.DURABILITY, "1" })
									.setItemInfoHidden(true).build());
				} else {
					p.setGameMode(GameMode.SPECTATOR);
					teleportNearest(p);
				}
			}
		}, 1L);
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		if (Main.get().getGameState() == GameState.ENDING)
			e.disallow(Result.KICK_OTHER, "The game already ended! Try again in a few seconds!");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLeave(PlayerQuitEvent e) {
		e.setQuitMessage(ChatColor.YELLOW + e.getPlayer().getName() + ChatColor.GOLD + " left the game! ("
				+ (Bukkit.getOnlinePlayers().size() - 1) + "/" + Bukkit.getMaxPlayers() + ")");
		if (Main.get().getGameState() == GameState.COUNTDOWN) {
			if (Bukkit.getOnlinePlayers().size() == Main.get().getConfig().getInt("minimum-players", 2)) {
				Main.get().broadcast("countdown.stop");
				Main.get().setGameState(GameState.WAITING);
			}
		} else if (Main.get().getGameState() == GameState.PLAYING) {
			Main.get().countDeath(PlayerManager.getData(e.getPlayer()));
		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onKick(PlayerKickEvent e) {
		e.setLeaveMessage(ChatColor.YELLOW + e.getPlayer().getName() + ChatColor.GOLD + " left the game! ("
				+ (Bukkit.getOnlinePlayers().size() - 1) + "/" + Bukkit.getMaxPlayers() + ")");
		if (Main.get().getGameState() == GameState.COUNTDOWN) {
			if (Bukkit.getOnlinePlayers().size() == Main.get().getConfig().getInt("minimum-players", 2)) {
				Main.get().broadcast("countdown.stop");
				Main.get().setGameState(GameState.WAITING);
			}
		} else if (Main.get().getGameState() == GameState.PLAYING) {
			Main.get().countDeath(PlayerManager.getData(e.getPlayer()));
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING)
			e.setCancelled(true);
	}

	@EventHandler
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		if (e.getDamager() instanceof Player) {
			Main.get().setLastHit((Player) e.getEntity(), (Player) e.getDamager());
		} else if (e.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) e.getDamager();
			if (!(proj.getShooter() instanceof Player))
				return;
			Main.get().setLastHit((Player) e.getEntity(), (Player) proj.getShooter());
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING || Main.get().isGraceActive()) {
			e.setCancelled(true);
			return;
		}
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		if ((p.getHealth() - e.getFinalDamage()) > 0)
			return;
		e.setCancelled(true);
		onPlayerDeath(p);
	}

	public void onPlayerDeath(Player p) {
		if (Main.get().getGameState() != GameState.PLAYING)
			return;
		Main.get().countDeath(PlayerManager.getData(p));
		p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK, 1000);
		Player killer = Main.get().getLastHitter(p);
		if (p.isDead())
			NMSUtils.forceRespawn(p);
		p.setGameMode(GameMode.SPECTATOR);
		if (Main.get().getPlayingCount() == 1) {
			Main.get().setGameState(GameState.ENDING);
			Player winner = Main.get().getWinner();
			for (int i = 0; i < 10; i++)
				FireworkUtils.shootFirework(winner);
			if (killer == null || killer == p)
				Main.get().broadcast("suicide.win", p.getName(), winner.getName());
			else {
				Main.get().broadcast("killed.win", p.getName(), killer.getName(), winner.getName());
				Main.get().countKill(PlayerManager.getData(killer));
			}
			p.teleport(winner);
			Main.get().countWin(PlayerManager.getData(winner));
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			Main.get().sendStats(winner);
			Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {

				public void run() {
					Main.get().reloadGame();
				}
			}, 200);
		} else {
			if (killer == null || killer == p) {
				Main.get().broadcast("suicide", p.getName(), Main.get().getPlayingCount());
				teleportNearest(p);
			} else {
				Main.get().broadcast("killed", p.getName(), killer.getName(), Main.get().getPlayingCount());
				p.teleport(killer);
				p.setSpectatorTarget(killer);
				Main.get().countKill(PlayerManager.getData(killer));
			}
			p.sendMessage(LanguageMain.get(p, "died"));
		}
		Main.get().sendStats(p);
	}

	public void teleportNearest(Player p) {
		for (Entity en : p.getWorld().getPlayers()) {
			if (en instanceof Player) {
				Player p2 = (Player) en;
				if (p2 == p)
					continue;
				if (p2.getGameMode() == GameMode.SURVIVAL) {
					p.teleport(p2);
					break;
				}
			}
		}
	}

	@EventHandler
	public void onEnderPearlShot(ProjectileLaunchEvent e) {
		if (!(e.getEntity() instanceof EnderPearl))
			return;
		NMSUtils.setItemCooldown((Player) e.getEntity().getShooter(), Material.ENDER_PEARL, 60);
	}

	@EventHandler
	public void onKitSelectorInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (!e.getAction().toString().contains("RIGHT"))
			return;
		if (Main.get().getGameState() != GameState.COUNTDOWN && Main.get().getGameState() != GameState.WAITING)
			return;
		if (e.getItem() == null)
			return;
		if (e.getItem().getType() != Material.PAPER)
			return;
		Gui gui = new Gui(1, LanguageMain.get(p, "kits"));
		for (Kit kit : KitsManager.get().getAll())
			gui.addButton(new GuiButton(kit.getShowcaseItem(p)).setListener(event -> {
				Main.get().setKit(p, kit.getID());
				p.sendMessage(LanguageMain.get(p, "kit.selected"));
				p.closeInventory();
			}));
		gui.open(p);
	}

	@EventHandler
	public void onInventoryInteractWhileWaiting(InventoryClickEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING)
			e.setCancelled(true);
	}

	@EventHandler
	public void onPickupWhileWaiting(PlayerPickupItemEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING)
			e.setCancelled(true);
	}

	@EventHandler
	public void onDropWhileWaiting(PlayerDropItemEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING)
			e.setCancelled(true);
	}

	@EventHandler
	public void onSwapWhileWaiting(PlayerSwapHandItemsEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING)
			e.setCancelled(true);
	}

	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == SpawnReason.DEFAULT)
			e.setCancelled(true);
	}

	@EventHandler
	public void onMoveWhilePlayingCountdown(PlayerMoveEvent e) {
		if (Main.get().getGameState() != GameState.PLAYING_COUNTDOWN)
			return;
		Location from = e.getFrom();
		Location to = e.getTo();
		if (from.getBlock().equals(to.getBlock()))
			return;
		e.setTo(e.getFrom());
	}

}
