package de.blablubbabc.billboards.util;

import java.util.Optional;
import java.util.UUID;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Utils {
	private static Boolean hasPAPI = null;
	public static void runCommand(Player player, String command, Object... args) {
		if (command.startsWith("player:")) {
			String s = String.format(command.substring(7), args);
			Bukkit.dispatchCommand(player, papi(player, s));
		}
		if (command.startsWith("console:")) {
			String s = String.format(command.substring(8), args);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), papi(player, s));
		}
	}
	public static String papi(Player p, String s) {
		if (hasPAPI == null) try {
			Class.forName("me.clip.placeholderapi.PlaceholderAPI");
			hasPAPI = true;
		} catch (ClassNotFoundException e) {
			hasPAPI = false;
		}
		if (!hasPAPI) return s;
		return PlaceholderAPI.setPlaceholders(p, s);
	}

	public static Optional<Material> parseMat(String s) {
		if (s != null && !s.isEmpty()) {
			for (Material m : Material.values()) {
				if (m.name().equalsIgnoreCase(s)) {
					return Optional.of(m);
				}
			}
		}
		return Optional.empty();
	}

	public static boolean isEmpty(String string) {
		return (string == null || string.isEmpty());
	}

	public static boolean isSign(Material material) {
		if (material == null) return false;
		return material.data.isAssignableFrom(org.bukkit.block.data.type.Sign.class) || material.data.isAssignableFrom(org.bukkit.block.data.type.WallSign.class);
	}

	public static Integer parseInteger(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static UUID parseUUID(String string) {
		if (string == null) return null;
		try {
			return UUID.fromString(string);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static long addSaturated(long x, long y) {
		try {
			return Math.addExact(x, y);
		} catch (ArithmeticException e) {
			if (y > 0) {
				return Long.MAX_VALUE;
			} else {
				return Long.MIN_VALUE;
			}
		}
	}
}
