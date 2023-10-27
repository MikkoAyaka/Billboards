package de.blablubbabc.billboards.util;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.*;

public class Items {
	private final ItemStack itemStack;
	private ItemMeta itemMeta;
	public Items(Material material, int amount) {
		itemStack = new ItemStack(material, amount);
		itemMeta = itemStack.getItemMeta();
	}

	public Items(ItemStack itemStack) {
		this.itemStack = new ItemStack(itemStack);
		itemMeta = itemStack.getItemMeta();
	}

	public Items(Material material) {
		this(material, 1);
		itemMeta = itemStack.getItemMeta();
	}

	public static boolean isOre(Material material) {
		return material.equals(Material.COAL_ORE) || material.equals(Material.IRON_ORE)
				|| material.equals(Material.GOLD_ORE) || material.equals(Material.LAPIS_ORE)
				|| material.equals(Material.DIAMOND_ORE) || material.equals(Material.DIAMOND_ORE)
				|| material.equals(Material.REDSTONE_ORE) || material.equals(Material.NETHER_QUARTZ_ORE);
	}

	public Items name(String displayName) {

		if (displayName == null)
			return this;

		itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		itemStack.setItemMeta(itemMeta);

		return this;
	}

	public Items lore(String... lore) {

		List<String> itemLore = new ArrayList<>();
		for(String s : lore) {
			itemLore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		itemMeta.setLore(itemLore);
		itemStack.setItemMeta(itemMeta);

		return this;
	}

	public Items lore(List<String> lore) {
		List<String> itemLore = new ArrayList<>();
		for(String s : lore) {
			itemLore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		itemMeta.setLore(itemLore);
		itemStack.setItemMeta(itemMeta);

		return this;
	}

	public Items addLore(String... lore) {

		List<String> itemLore = itemMeta.getLore();
		if (itemLore == null)
			return this;
		for(String s : lore) {
			itemLore.add(ChatColor.translateAlternateColorCodes('&', s));
		}

		itemMeta.setLore(itemLore);
		itemStack.setItemMeta(itemMeta);
		return this;
	}

	public Items addLore(List<String> lore) {

		List<String> itemLore = itemMeta.getLore();
		if (itemLore == null)
			return this;

		for(String s : lore) {
			itemLore.add(ChatColor.translateAlternateColorCodes('&', s));
		}

		itemMeta.setLore(itemLore);
		itemStack.setItemMeta(itemMeta);
		return this;
	}

	public Items addEnchantment(Enchantment enchantment, int level) {
		itemStack.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	public Items addFlag(ItemFlag itemFlag) {
		itemMeta.addItemFlags(itemFlag);
		itemStack.setItemMeta(itemMeta);
		return this;
	}

	public Items setUnbreakable() {

		itemMeta.setUnbreakable(true);

		itemStack.setItemMeta(itemMeta);

		return this;
	}

	public ItemStack build() {
		return itemStack;
	}

	public static boolean isDisplayNameContains(ItemStack item, String value) {
		return getItemDisplayName(item).contains(value);
	}

	public static String getItemDisplayName(ItemStack item) {
		if ((item == null) || !item.hasItemMeta() || item.getItemMeta() == null)
			return item != null ? item.getType().name() : "";
		return item.getItemMeta().getDisplayName();
	}

	public static List<String> getItemLore(ItemStack item) {
		if ((item == null) || !item.hasItemMeta() || item.getItemMeta() == null
				|| (item.getItemMeta().getLore() == null))
			return new ArrayList<>();
		return item.getItemMeta().getLore();
	}

	public static void reduceItemInMainHand(Player player) {
		reduceItemInMainHand(player, 1);
	}

	public static void reduceItemInMainHand(Player player, int amount) {
		ItemStack im = player.getInventory().getItemInMainHand();
		if (im.getAmount() - amount > 0) {
			im.setAmount(im.getAmount() - amount);
			player.getInventory().setItemInMainHand(im);
		} else {
			player.getInventory().setItemInMainHand(null);
		}
	}

	public static void reduceItemInOffHand(Player player) {
		reduceItemInOffHand(player, 1);
	}

	public static void reduceItemInOffHand(Player player, int amount) {
		ItemStack im = player.getInventory().getItemInOffHand();
		if (im.getAmount() - amount > 0) {
			im.setAmount(im.getAmount() - amount);
			player.getInventory().setItemInOffHand(im);
		} else {
			player.getInventory().setItemInOffHand(null);
		}
	}

	public static boolean hasLore(ItemStack item, String lore) {
		return getItemLore(item).contains(lore);
	}

	public static boolean containsLore(ItemStack item, String lore) {
		return String.join("\n", getItemLore(item)).contains(lore);
	}

	public static boolean containsLoreIgnoreCase(ItemStack item, String lore) {
		return String.join("\n", getItemLore(item)).toLowerCase().contains(lore.toLowerCase());
	}

	public static void setItemDisplayName(ItemStack item, String name) {
		if (item == null)
			return;
		ItemMeta im = item.getItemMeta() == null ? getItemMeta(item.getType()) : item.getItemMeta();
		if (im == null)
			return;
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		item.setItemMeta(im);
	}

	public static void setItemLore(ItemStack item, String... lore){
		setItemLore(item, Lists.newArrayList(lore));
	}

	public static void setItemLore(ItemStack item, List<String> lore) {
		if (item == null)
			return;
		ItemMeta im = item.getItemMeta() == null ? getItemMeta(item.getType()) : item.getItemMeta();
		if (im == null)
			return;
		List<String> newLore = new ArrayList<>();
		lore.forEach(s -> newLore.add(ChatColor.translateAlternateColorCodes('&', s)));
		im.setLore(newLore);
		item.setItemMeta(im);
	}

	public static ItemStack buildFrameItem(Material material) {
		return buildItem(material, "&f&l*", Lists.newArrayList());
	}

	public static ItemStack buildItem(Material material, String name) {
		return buildItem(material, name, Lists.newArrayList());
	}

	public static ItemStack buildItem(Material material, String name, List<String> lore) {
		ItemStack item = new ItemStack(material, 1);
		setItemDisplayName(item, name);
		setItemLore(item, lore);
		return item;
	}

	public static ItemMeta getItemMeta(Material material) {
		return Bukkit.getItemFactory().getItemMeta(material);
	}

	public static ItemStack buildItem(Material material, String name, String... lore) {
		return buildItem(material, name, Lists.newArrayList(lore));
	}

	public static void setRowItems(Inventory inv, int row, ItemStack item) {
		for (int i = 0; i < 9; i++) {
			inv.setItem(((row - 1) * 9) + i, item);
		}
	}

	public static ItemStack getEnchantedBook(Enchantment ench, int level) {
		Map<Enchantment, Integer> map = new HashMap<>();
		map.put(ench, level);
		return getEnchantedBook(map);
	}

	public static ItemStack getEnchantedBook(Map<Enchantment, Integer> map) {
		ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta im = item.getItemMeta();
		if (im == null)
			return item;
		for (Enchantment ench : map.keySet()) {
			((org.bukkit.inventory.meta.EnchantmentStorageMeta) im).addStoredEnchant(ench, map.get(ench), true);
		}
		item.setItemMeta(im);
		return item;
	}

	public static void giveItemToPlayer(final Player player, final ItemStack... items) {
		giveItemToPlayer(player, Lists.newArrayList(items));
	}
	public static void giveItemToPlayer(final Player player, final List<ItemStack> items) {
		giveItemToPlayer(player, "", "", items);
	}
	public static void giveItemToPlayer(final Player player, final String msg, final String msgFull,
			final ItemStack... items) {
		giveItemToPlayer(player, msg, msgFull, Lists.newArrayList(items));
	}
	public static void giveItemToPlayer(final Player player, final String msg, final String msgFull, final List<ItemStack> items) {
		final Collection<ItemStack> last = player.getInventory().addItem(items.toArray(new ItemStack[0])).values();
		if (msg.length() > 0 || (!last.isEmpty() && msgFull.length() > 0)) {
			String m = ChatColor.translateAlternateColorCodes('&', msg + (last.isEmpty() ? "" : ("\n&r" + msgFull)));
			player.sendMessage(m);
		}
		for (final ItemStack item : last) {
			player.getWorld().dropItem(player.getLocation(), item);
		}
	}
}
