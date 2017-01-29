package com.cloutteam.rexcantor64.endersg.utils;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import net.minecraft.server.v1_10_R1.Item;

public class NMSUtils {
	public static final String BUKKIT_PACKAGE;
	public static final String MINECRAFT_PACKAGE;

	public static Class<?> getNMSClass(String className) {
		try {
			return Class.forName(MINECRAFT_PACKAGE + className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object getHandle(@Nonnull Object target) {
		return getMethod(target, "getHandle");
	}

	public static Object getMethod(@Nonnull Object target, @Nonnull String methodName) {
		return getMethod(target, methodName, new Class[0], new Object[0]);
	}

	public static Object getMethod(@Nonnull Object target, @Nonnull String methodName, @Nonnull Class<?>[] paramTypes,
			@Nonnull Object... params) {
		Preconditions.checkNotNull(target, "Target is null");
		Preconditions.checkNotNull(methodName, "Method name is null");

		Class<?> currentClazz = target.getClass();
		Object returnValue = null;
		do {
			try {
				Method method = currentClazz.getDeclaredMethod(methodName, paramTypes);
				returnValue = method.invoke(target, params);
			} catch (Exception exception) {
				currentClazz = currentClazz.getSuperclass();
			}
		} while ((currentClazz != null) && (currentClazz.getSuperclass() != null) && (returnValue == null));

		return returnValue;
	}

	public static void forceRespawn(Player player) {
		Preconditions.checkNotNull(player, "Player is null");

		if (!player.isDead()) {
			return;
		}

		Object playerHandle = getHandle(player);
		if (playerHandle == null) {
			return;
		}
		Object serverHandle = getHandle(Bukkit.getServer());
		if (serverHandle == null) {
			return;
		}
		serverHandle = getMethod(serverHandle, "getServer");
		if (serverHandle == null) {
			return;
		}
		Object playerListHandle = getMethod(serverHandle, "getPlayerList");
		if (playerListHandle == null) {
			return;
		}
		getMethod(playerListHandle, "moveToWorld", new Class[] { playerHandle.getClass(), Integer.TYPE, Boolean.TYPE },
				playerHandle, Integer.valueOf(0), Boolean.valueOf(false));
	}

	public static boolean isRunning() {
		Object minecraftServer = getMethod(Bukkit.getServer(), "getServer");
		if (minecraftServer == null) {
			return false;
		}

		Object isRunning = getMethod(minecraftServer, "isRunning");
		return ((isRunning instanceof Boolean)) && (((Boolean) isRunning).booleanValue());
	}

	@SuppressWarnings("deprecation")
	public static void setItemCooldown(Player p, Material m, int seconds) {
		Preconditions.checkNotNull(p, "Player is null");
		((CraftPlayer) p).getHandle().df().a(Item.getById(m.getId()), seconds * 20);
		// Removed reflection as it was resource-expensive and it was crashing the server.
		/*
		 * Object playerHandle = getHandle(p); if (playerHandle == null) return;
		 * Object itemCooldown = getMethod(playerHandle, "df"); try {
		 * getMethod(itemCooldown, "a", new Class<?>[] { getNMSClass("Item"),
		 * int.class }, getNMSClass("Item").getMethod("getById",
		 * int.class).invoke(null, 368), seconds * 20); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
	}

	static {
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		String bukkitVersion = packageName.substring(packageName.lastIndexOf('.') + 1);

		BUKKIT_PACKAGE = "org.bukkit.craftbukkit." + bukkitVersion + ".";
		MINECRAFT_PACKAGE = "net.minecraft.server." + bukkitVersion + ".";
	}
}
