package com.cloutteam.rexcantor64.endersg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.cloutteam.rexcantor64.endersg.commands.AdminCMD;
import com.cloutteam.rexcantor64.endersg.kits.KitsManager;
import com.cloutteam.rexcantor64.endersg.listeners.BlockListener;
import com.cloutteam.rexcantor64.endersg.listeners.PlayerListener;
import com.cloutteam.rexcantor64.endersg.tasks.SupplyDropTask;
import com.cloutteam.rexcantor64.endersg.tasks.TickTask;
import com.cloutteam.rexcantor64.endersg.utils.NMSUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.endercraft.endercore.EnderMinigame;
import uk.endercraft.endercore.EnderPlayer;
import uk.endercraft.endercore.GameState;
import uk.endercraft.endercore.language.LanguageMain;
import uk.endercraft.endercore.managers.BungeeManager;
import uk.endercraft.endercore.managers.PlayerManager;

public class Main extends EnderMinigame {

	private static Main instance;
	private int timer = 0;
	private HashMap<Player, Player> hits = Maps.newHashMap();
	private HashMap<Player, Integer> kits = Maps.newHashMap();
	private boolean borderShrinking = false;
	private boolean grace = true;
	private List<Location> podiums = Lists.newArrayList();
	private List<Location> drops = Lists.newArrayList();
	private World world;
	private final Random rand = new Random();

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		saveResource("config.yml", false);
		if (!copyDefaultWorld())
			return;
		getLogger().info("Minigame world copied successfuly!");
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {

			public void run() {
				try {
					world = Bukkit.getServer().createWorld(new WorldCreator("world").environment(Environment.NORMAL));
					world.setGameRuleValue("doMobSpawning", "false");
					List<String> paths = new ArrayList<String>(
							getConfig().getConfigurationSection("spawnLocations").getKeys(false));
					for (String pathS : paths) {
						String path = "spawnLocations." + pathS + ".";
						podiums.add(new Location(world, getConfig().getDouble(path + "x", 0),
								getConfig().getDouble(path + "y", 0), getConfig().getDouble(path + "z", 0),
								(float) getConfig().getDouble(path + "yaw", 0),
								(float) getConfig().getDouble(path + "pitch", 0)));
					}
					for (String drop : getConfig().getStringList("supplyDropsLocation")) {
						String[] a = drop.split(";");
						drops.add(new Location(world, Integer.parseInt(a[0]), Integer.parseInt(a[1]),
								Integer.parseInt(a[2])));
					}
					world.getWorldBorder().reset();
					getLogger().info("World " + world.getName() + " loaded successfuly!");
				} catch (Exception ex) {
					forceDisable("failed to load the minigame world!");
					ex.printStackTrace();
					return;
				}
				gameState = GameState.WAITING;
			}
		}, 5L);
		registerEvent(new PlayerListener());
		registerEvent(new BlockListener());
		getCommand("admin").setExecutor(new AdminCMD());

		new TickTask().runTaskTimer(this, 20L, 20L);
		hits.clear();
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	private boolean copyDefaultWorld() {
		File world = new File(getDataFolder(), "world");
		if (!world.exists()) {
			forceDisable(
					"plugin couldn't find the default world! Please make sure you have a world folder on your plugin directory.");
			return false;
		}
		File defaultWorld = new File(".", "world");
		try {
			FileUtils.copyDirectory(world, defaultWorld);
		} catch (IOException e) {
			forceDisable("plugin couldn't copy the minigame world to the new world! Error: " + e.getMessage());
			return false;
		}
		return true;
	}

	public void setTimer(int seconds) {
		this.timer = seconds;
	}

	public int getTimer() {
		return this.timer;
	}

	public void decreaseTimer() {
		this.timer--;
	}

	public void teleportToArena() {
		Random r = new Random();
		List<Location> locs = new ArrayList<Location>(podiums);
		for (Player p : Bukkit.getOnlinePlayers()) {
			int i = r.nextInt(locs.size());
			p.teleport(locs.get(i));
			locs.remove(i);
		}
	}

	public void spawnRandomSupplyDrop() {
		Location l = drops.get(rand.nextInt(drops.size()));
		l.getBlock().setType(Material.ENDER_CHEST);
		broadcast("supplydrop", l.getBlockX(), l.getBlockY(), l.getBlockZ());
	}

	public int getPlayingCount() {
		int i = 0;
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getGameMode() == GameMode.SURVIVAL)
				i++;
		return i;
	}

	public Player getWinner() {
		if (getPlayingCount() != 1)
			return null;
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getGameMode() == GameMode.SURVIVAL)
				return p;
		return null;
	}

	public void reloadGame() {
		for (Player p : Bukkit.getOnlinePlayers())
			BungeeManager.sendToServer(p, "hub");
		Bukkit.unloadWorld("world", false);
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				copyDefaultWorld();
				Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
					public void run() {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
					}
				}, 10L);
			}
		}, 10L);
	}

	public void setLastHit(Player victim, Player hitter) {
		hits.put(victim, hitter);
	}

	public Player getLastHitter(Player victim) {
		return hits.get(victim);
	}

	public void setKit(Player player, Integer kitId) {
		kits.put(player, kitId);
	}

	public Integer getKit(Player player) {
		return kits.get(player);
	}

	@SuppressWarnings("deprecation")
	public void startGame() {
		Main.get().setGameState(GameState.PLAYING);
		setTimer(30);
		broadcast("start");
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.getInventory().clear();
			NMSUtils.setItemCooldown(p, Material.ENDER_PEARL, 30);
			KitsManager.get().getById(getKit(p)).giveItems(p);
			p.sendTitle(LanguageMain.get(p, "start.title"), "");
			countPlay(PlayerManager.getData(p));
		}
		SupplyDropTask.start();
	}

	public boolean isBorderShrinking() {
		return borderShrinking;
	}

	public void startBorderShrinking() {
		this.borderShrinking = true;
		world.getWorldBorder().setSize(Main.get().getConfig().getInt("border.stop-radius", 30),
				Main.get().getConfig().getInt("border.stop-radius", 30) * 4);
	}

	public boolean isGraceActive() {
		return grace;
	}

	public void endGracePeriod() {
		this.grace = false;
		setTimer(getConfig().getInt("border.auto-activate-time", 300));
		broadcast("endgrace");
	}

	public void updateScoreboard() {
		boolean lobbySb = gameState == GameState.WAITING || gameState == GameState.COUNTDOWN;
		for (Player p : Bukkit.getOnlinePlayers()) {
			Scoreboard sb = p.getScoreboard();
			if (sb.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
				sb = Bukkit.getScoreboardManager().getNewScoreboard();
				p.setScoreboard(sb);
			}
			try {
				sb.getObjective(getMinigameName()).unregister();
			} catch (Exception e) {
			}
			Objective obj = sb.registerNewObjective(getMinigameName(), "dummy");
			obj.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "END" + ChatColor.DARK_PURPLE
					+ ChatColor.BOLD + "WARS");
			obj.getScore("    ").setScore(11);
			obj.getScore(lobbySb ? LanguageMain.get(p, "sb.lobby.players") : LanguageMain.get(p, "sb.alive"))
					.setScore(10);
			obj.getScore(
					lobbySb ? LanguageMain.get(p, "sb.lobby.playercount", getPlayingCount(), Bukkit.getMaxPlayers())
							: ChatColor.GOLD + "" + getPlayingCount())
					.setScore(9);
			obj.getScore(" ").setScore(8);
			obj.getScore(LanguageMain.get(p, "sb.kit")).setScore(7);
			obj.getScore(LanguageMain.get(p, "sb.kit.name", KitsManager.get().getById(Main.get().getKit(p)).getName()))
					.setScore(6);
			obj.getScore("  ").setScore(5);
			obj.getScore(lobbySb ? LanguageMain.get(p, "sb.lobby.coins") : LanguageMain.get(p, "sb.border"))
					.setScore(4);
			obj.getScore(lobbySb ? ChatColor.GOLD + "" + PlayerManager.getData(p).getCoins()
					: (LanguageMain.get(p, Main.get().isBorderShrinking() ? "sb.border.active" : "sb.border.unactive")))
					.setScore(3);
			obj.getScore("   ").setScore(2);
			obj.getScore(LanguageMain.get(p, "sb.ip")).setScore(-1);
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
	}

	public void sendStats(Player p) {
		EnderPlayer ep = PlayerManager.getData(p);
		p.sendMessage(LanguageMain.get(ep, "stats.lineseparator1"));
		p.sendMessage(" ");
		p.sendMessage(LanguageMain.get(ep, "stats.participation"));
		if (ep.getCacheData().getKills() != 0)
			p.sendMessage(LanguageMain.get(ep, "stats.kills", ep.getCacheData().getKills(),
					ep.getCacheData().getKills() * 15, ep.getCacheData().getKills()));
		if (ep.getMinigameData(getMinigameName()).getDeaths() != 0)
			p.sendMessage(LanguageMain.get(ep, "stats.died"));
		if (getWinner() == p)
			p.sendMessage(LanguageMain.get(ep, "stats.win"));
		p.sendMessage(" ");
		p.sendMessage(LanguageMain.get(ep, "stats.lineseparator2"));
	}

	public void broadcast(String code, Object... complements) {
		for (Player p : Bukkit.getOnlinePlayers())
			p.sendMessage(LanguageMain.get(p, code, complements));
	}

	public static Main get() {
		return instance;
	}

	public List<Location> getPodiums() {
		return podiums;
	}

	public void countKill(EnderPlayer ep) {
		ep.getCacheData().addKills(1);
		ep.getMinigameData(getMinigameName()).addKills(1);
		ep.addCoins(15);
		ep.getMinigameData(getMinigameName()).addXp(1);
	}

	public void countDeath(EnderPlayer ep) {
		ep.getCacheData().addDeaths(1);
		ep.getMinigameData(getMinigameName()).addDeaths(1);
		ep.getMinigameData(getMinigameName()).addXp(-1);
	}

	public void countPlay(EnderPlayer ep) {
		ep.getMinigameData(getMinigameName()).addPlayed(1);
		ep.addCoins(25);
	}

	public void countWin(EnderPlayer ep) {
		ep.getCacheData().addWins(1);
		ep.getMinigameData(getMinigameName()).addWins(1);
		ep.addCoins(50);
		ep.getMinigameData(getMinigameName()).addXp(1);
	}

	@Override
	public String getMinigameName() {
		return "endersg";
	}

}
