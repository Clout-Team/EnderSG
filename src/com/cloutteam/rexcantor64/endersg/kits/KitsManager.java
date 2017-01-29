package com.cloutteam.rexcantor64.endersg.kits;

import java.util.List;

import org.bukkit.Bukkit;

import com.cloutteam.rexcantor64.endersg.Main;
import com.cloutteam.rexcantor64.endersg.kits.kits.DragonKit;
import com.cloutteam.rexcantor64.endersg.kits.kits.NoneKit;
import com.google.common.collect.Lists;

public class KitsManager {

	private static KitsManager instance;

	private List<Kit> kits = Lists.newArrayList();
	
	private Kit none = new NoneKit();

	private KitsManager() {
		a(new DragonKit());
	}

	private void a(Kit kit) {
		if (kit.isListener())
			Bukkit.getPluginManager().registerEvents(kit, Main.get());
		kits.add(kit);
	}

	public List<Kit> getAll() {
		return kits;
	}

	public Kit getById(int id) {
		for (Kit kit : kits)
			if (kit.getID() == id)
				return kit;
		return none;
	}

	public static KitsManager get() {
		if (instance == null)
			instance = new KitsManager();
		return instance;
	}

}
