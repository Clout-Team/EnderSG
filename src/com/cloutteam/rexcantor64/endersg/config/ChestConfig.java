package com.cloutteam.rexcantor64.endersg.config;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.endercraft.endercore.config.ConfigFile;
import uk.endercraft.endercore.utils.CustomStack;

public class ChestConfig extends ConfigFile {

	/**
	 * @author Rexcantor64
	 * @category Create the unique ChestConfig instance.
	 */
	private ChestConfig() {
		super("chests");
		for (String s : toBukkit().getKeys(false)) {
			String path = s + ".";
			Material type = Material.getMaterial(toBukkit().getString(path + "type", "AIR"));
			int amount = toBukkit().getInt(path + "amount", 1);
			short durability = (short) toBukkit().getInt(path + "durability", 0);
			String name = getColoredString(path + "name", "");
			List<String> lore = getColoredStringList(path + "lore");
			Object[][] enchants = getEnchantmentsArray(path + "enchantments");
			int chance = toBukkit().getInt(path + "chance", 0);
			chestItems.add(new ChestItem(type, amount, durability, name, lore, enchants, chance));
		}
	}

	private List<ChestItem> chestItems = Lists.newArrayList();
	private static ChestConfig instance;

	public List<ChestItem> getAllChestItems() {
		return chestItems;
	}

	public class ChestItem {

		private Material type;
		private int amount;
		private short durability;
		private String name;
		private List<String> lore;
		private Object[][] enchants;
		private double chance;

		public ChestItem(Material type, int amount, short durability, String name, List<String> lore,
				Object[][] enchantments, double chance) {
			this.type = type;
			this.amount = amount;
			this.durability = durability;
			this.name = name;
			this.lore = lore;
			this.enchants = enchantments;
			this.chance = chance;
		}

		public Material getType() {
			return type;
		}

		public int getAmount() {
			return amount;
		}

		public short getDurability() {
			return durability;
		}

		public String getName() {
			return name;
		}

		public List<String> getLore() {
			return lore;
		}

		public Object[][] getEnchants() {
			return enchants;
		}

		public double getChance() {
			return chance;
		}

		public ItemStack toBukkit() {
			return new CustomStack().setMaterial(type).setAmount(amount).setDurability(durability).setDisplayName(name)
					.setLore(lore.toArray(new String[0])).setEnchantments(enchants).build();
		}

	}
	
	private Random r = new Random();

	/**
	 * Get random items for a specific amount of chest slots.
	 * 
	 * @author Rexcantor64
	 * @param chestAmount
	 *            The size of the map.
	 * @return Returns a map with where key=slot and value=item
	 * @since v2.0.0-SNAPSHOT
	 */
	public Map<Integer, ItemStack> getRandomItems(int chestAmount) {
		Map<Integer, ItemStack> items = Maps.newHashMap();
		List<ChestItem> cItems = getAllChestItems();
		int airPercent = 80;
		for (int i = 0; i < chestAmount; i++) {
			if (r.nextInt(100) <= airPercent) {
				items.put(i, new ItemStack(Material.AIR));
				continue;
			}
			double totalWeight = 0.0d;
			for (ChestItem item : cItems) {
				totalWeight += item.getChance();
			}
			int randomIndex = -1;
			double random = Math.random() * totalWeight;
			for (int i2 = 0; i2 < cItems.size(); ++i2) {
				random -= cItems.get(i2).getChance();
				if (random <= 0.0d) {
					randomIndex = i2;
					break;
				}
			}
			items.put(i, cItems.get(randomIndex).toBukkit());
		}
		return items;
	}

	public static ChestConfig get() {
		if (instance == null)
			instance = new ChestConfig();
		return instance;
	}

}
