package de.blablubbabc.billboards.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.common.collect.Lists;
import de.blablubbabc.billboards.BillboardsPlugin;
import de.blablubbabc.billboards.entry.BillboardSign;
import de.blablubbabc.billboards.entry.HologramHolder;
import de.blablubbabc.billboards.entry.SignEdit;
import de.blablubbabc.billboards.util.SoftBlockLocation;
import de.blablubbabc.billboards.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SignEditing implements Listener {

	private final BillboardsPlugin plugin;
	// player name -> editing information
	private final Map<String, SignEdit> editing = new HashMap<>();
	private ProtocolManager protocolManager;
	private int bedrock;
	private String verPL, verMC;
	public SignEditing(BillboardsPlugin plugin) {
		this.plugin = plugin;
	}

	public void onPluginEnable() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		protocolManager = ProtocolLibrary.getProtocolManager();
		bedrock = protocolManager.getMinecraftVersion().isAtLeast(new MinecraftVersion("1.18")) ? -64 : 0;
		verPL = ProtocolLib.getPlugin(ProtocolLib.class).getDescription().getVersion();
		verMC = protocolManager.getMinecraftVersion().getVersion();
		protocolManager.addPacketListener(new PacketAdapter(this.plugin, PacketType.Play.Client.UPDATE_SIGN) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				Player player = event.getPlayer();
				SignEdit signEdit = endSignEdit(player);
				if (signEdit == null) {
					return; // player wasn't editing
				}
				event.setCancelled(true);
				String[] lines = encodeColor(event.getPacket().getStringArrays().read(0), player);
				if (SignEditing.this.plugin.refreshSign(signEdit.billboard)) {
					// still owner and has still the permission?
					if (signEdit.billboard.canEdit(player) && player.hasPermission(BillboardsPlugin.RENT_PERMISSION)) {
						// update billboard sign content:
						SoftBlockLocation signLoc = signEdit.billboard.getLocation();
						HologramHolder hologram = signEdit.billboard.getHologram();
						if (hologram != null) Bukkit.getScheduler().runTask(plugin, () -> { // 更新悬浮字
							List<String> list = Lists.newArrayList(lines);
							hologram.setLines(list);
						}); else if (signLoc != null) Bukkit.getScheduler().runTask(plugin, () -> { // 更新木牌
							Sign target = (Sign) signLoc.getBukkitLocation().getBlock().getState();
							for (int i = 0; i < lines.length && i < 4; i++) {
								target.setLine(i, lines[i]);
							}
							target.update();
						});
					}
					else player.sendMessage("§7你无法编辑这个广告牌");
				}
				else player.sendMessage("§7该广告牌无效");

				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					if (player.isOnline()) {
						player.sendBlockChange(signEdit.location, signEdit.location.getBlock().getBlockData());
					}
				}, 2L);
			}
		});
	}

	public void onPluginDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			this.endSignEdit(player);
		}
		if (protocolManager != null) protocolManager.removePacketListeners(plugin);
	}
	public String[] encodeColor(String[] lines, Player player) {
		boolean color = player == null || player.hasPermission(BillboardsPlugin.SIGN_COLOR_PERMISSION);
		boolean format = player == null || player.hasPermission(BillboardsPlugin.SIGN_FORMAT_PERMISSION);
		boolean magic = player == null || player.hasPermission(BillboardsPlugin.SIGN_FORMAT_MAGIC_PERMISSION);
		if (color || format || magic) {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (color) line = line.replaceAll("&([0-9A-Fa-f])", "§$1");
				if (format) line = line.replaceAll("&([LMNORlmnor])", "§$1");
				if (magic) line = line.replace("&k", "§k");
				lines[i] = line;
			}
		}
		return lines;
	}
	public String[] decodeColor(String[] lines) {
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].replace("§", "&");
		}
		return lines;
	}
	public static Material getSignMaterial() {
		return Utils.parseMat("OAK_SIGN")
				.orElseGet(() -> Utils.parseMat("SIGN_POST").orElse(null));
	}
	public void openSignEdit(Player player, BillboardSign billboard) {
		if (!player.isOnline() || editing.containsKey(player.getName())) {
			return;
		}
		Location location = player.getLocation().clone();
		int blockY = location.getBlockY();
		if (blockY - 4 < bedrock) {
			location.setY(blockY + 4);
		} else {
			location.setY(blockY - 4);
		}

		HologramHolder hologram = billboard.getHologram();

		BlockData fakeSign;
		String[] content = new String[4];
		if (hologram != null) {
			fakeSign = getSignMaterial().createBlockData();
			// TODO: 打开悬浮字编辑
		} else {
			Block block = billboard.getLocation().getBukkitLocation().getBlock();
			BlockState state = block.getState();
			if (!(state instanceof Sign)) return;
			fakeSign = block.getBlockData();
			content = decodeColor(((Sign) state).getLines());
		}

		// create a fake sign
		player.sendBlockChange(location, fakeSign);
		player.sendSignChange(location, content);

		try {
			// open sign edit gui for player
			PacketContainer openSign = openSignEditor(location);
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);
			editing.put(player.getName(), new SignEdit(billboard, location));
		} catch (Throwable t) {
			plugin.getLogger().log(Level.SEVERE, "为玩家 " + player.getName() + " 打开木牌编辑界面时出现问题 (ProtocolLib " + verPL + ", Minecraft " + verMC + ")", t);
		}
	}

	private PacketContainer openSignEditor(Location loc) {
		PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
		BlockPosition position = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		packet.getBlockPositionModifier().write(0, position);
		StructureModifier<Boolean> booleans = packet.getBooleans();
		if (booleans.size() > 0) {
			booleans.write(0, true);
		}
		return packet;
	}

	// returns null if the player was editing
	public SignEdit endSignEdit(Player player) {
        return editing.remove(player.getName());
	}

	@EventHandler
	void onQuit(PlayerQuitEvent event) {
		this.endSignEdit(event.getPlayer());
	}
}
