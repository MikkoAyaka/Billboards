package de.blablubbabc.billboards.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import de.blablubbabc.billboards.BillboardsPlugin;
import de.blablubbabc.billboards.entry.BillboardSign;
import de.blablubbabc.billboards.entry.SignEdit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SignEditing implements Listener {

	private final BillboardsPlugin plugin;
	// player name -> editing information
	private final Map<String, SignEdit> editing = new HashMap<>();
	private ProtocolManager protocolManager;
	public SignEditing(BillboardsPlugin plugin) {
		this.plugin = plugin;
	}

	public void onPluginEnable() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(new PacketAdapter(this.plugin, PacketType.Play.Client.UPDATE_SIGN) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				Player player = event.getPlayer();
				SignEdit signEdit = endSignEdit(player);
				if (signEdit == null) {
					return; // player wasn't editing
				}
				event.setCancelled(true);

				String[] input;
				PacketContainer packet = event.getPacket();
				StructureModifier<String[]> stringArrays = packet.getStringArrays();
				StructureModifier<String> strings = packet.getStrings();
				if (stringArrays.size() > 0) {
					input = stringArrays.read(0);
				} else if (strings.size() >= 4) {
					input = new String[] {
							strings.read(0),
							strings.read(1),
							strings.read(2),
							strings.read(3)
					};
				} else {
					plugin.getLogger().warning("收到来自玩家 " + player.getName() + " 的无效 UPDATE_SIGN 包，这代表插件可能不支持当前版本");
					return;
				}
				String[] lines = encodeColor(input, player);
				if (SignEditing.this.plugin.refreshSign(signEdit.billboard)) {
					// still owner and has still the permission?
					if (signEdit.billboard.canEdit(player) && player.hasPermission(BillboardsPlugin.RENT_PERMISSION)) {
						// update billboard sign content:
						Bukkit.getScheduler().runTask(plugin, () -> {
							Sign target = (Sign) signEdit.billboard.getLocation().getBukkitLocation().getBlock().getState();
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
	public void openSignEdit(Player player, BillboardSign billboard) {
		if (!player.isOnline() || editing.containsKey(player.getName())) {
			return;
		}
		Location location = player.getLocation();
		location.setY(location.getBlockY() - 4);

		Block block = billboard.getLocation().getBukkitLocation().getBlock();

		// create a fake sign
		player.sendBlockChange(location, block.getBlockData());
		player.sendSignChange(location, decodeColor(((Sign) block.getState()).getLines()));

		// open sign edit gui for player
		PacketContainer openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
		BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		openSign.getBlockPositionModifier().write(0, position);
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);
			editing.put(player.getName(), new SignEdit(billboard, location));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
