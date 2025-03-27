package de.blablubbabc.billboards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import de.blablubbabc.billboards.entry.BillboardSign;
import de.blablubbabc.billboards.entry.HologramHolder;
import de.blablubbabc.billboards.gui.GuiSignEditConfig;
import de.blablubbabc.billboards.listener.*;
import de.blablubbabc.billboards.message.Message;
import de.blablubbabc.billboards.message.Messages;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.blablubbabc.billboards.util.SoftBlockLocation;
import de.blablubbabc.billboards.util.Utils;

import net.milkbowl.vault.economy.Economy;
import org.holoeasy.HoloEasy;
import org.holoeasy.pool.IHologramPool;

import static org.holoeasy.builder.HologramBuilder.*;

public class BillboardsPlugin extends JavaPlugin implements Listener {

	private static BillboardsPlugin instance;

	public static BillboardsPlugin getInstance() {
		return instance;
	}

	public static Economy economy = null;
	public static final String ADMIN_PERMISSION = "billboards.admin";
	public static final String RENT_PERMISSION = "billboards.rent";
	public static final String SIGN_COLOR_PERMISSION = "billboards.sign.color";
	public static final String SIGN_FORMAT_PERMISSION = "billboards.sign.format";
	public static final String SIGN_FORMAT_MAGIC_PERMISSION = "billboards.sign.format.magic";
	public static final String CREATE_PERMISSION = "billboards.create";

	private static final String BILLBOARDS_DATA_FILE = "billboards.yml";
	private static final String BILLBOARDS_DATA_FILE_ENCODING = "UTF-8";

	// settings:
	public int defaultPrice = 10;
	public int defaultDurationInDays = 7;
	public int maxBillboardsPerPlayer = -1; // no limit by default
	public final GuiSignEditConfig signEditGuiConfig = new GuiSignEditConfig(this);
	// data:
	private final Map<SoftBlockLocation, BillboardSign> billboards = new LinkedHashMap<>();
	private final Collection<BillboardSign> billboardsView = Collections.unmodifiableCollection(billboards.values());

	// controllers:
	public final SignInteraction signInteraction = new SignInteraction(this);
	public final SignProtection signProtection = new SignProtection(this);
	public final SignEditing signEditing = new SignEditing(this);
	public final ChatPromptListener chatPromptListener = new ChatPromptListener(this);

	private GuiManager guiManager = null;
	private IHologramPool hologramPool;

	@Override
	public void onEnable() {
		instance = this;
		if (!Bukkit.getOnlinePlayers().isEmpty() && !setupEconomy()) {
			return;
		}
		Utils.init();
		hologramPool = HoloEasy.startInteractivePool(this, 60, 0.5f, 5f);

		// load messages:
		this.reloadMessages();

		// load config (and write defaults back to file):
		this.reloadConfig();

		// loads signs:
		this.loadBillboards();

		// initialize controllers:
		Bukkit.getPluginManager().registerEvents(this, this);
		this.guiManager = new GuiManager(this);
		signInteraction.onPluginEnable();
		signProtection.onPluginEnable();
		signEditing.onPluginEnable();
		chatPromptListener.onPluginEnable();

		// register command handler:
		PluginCommand command = this.getCommand("billboard");
		if (command != null) {
			BillboardCommands commands = new BillboardCommands(this);
			command.setExecutor(commands);
		}

		// start refresh timer:
		Bukkit.getScheduler().runTaskTimer(this, this::refreshAllSigns, 5L, 20L * 60 * 10);
	}

	@Override
	public void onDisable() {
		// inform controllers:
		signInteraction.onPluginDisable();
		signProtection.onPluginDisable();
		signEditing.onPluginDisable();
		chatPromptListener.onPluginDisable();

		if (guiManager != null) guiManager.onDisable();

		Bukkit.getScheduler().cancelTasks(this);
		instance = null;
	}

	// CONFIG

	public void reloadMessages() {
		Messages.loadMessages(new File(getDataFolder(), "messages.yml"), this.getLogger());
		signInteraction.onReloadMessages();
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		// load settings:
		FileConfiguration config = this.getConfig();
		defaultPrice = config.getInt("default-price", 10);
		defaultDurationInDays = config.getInt("default-duration-in-days", 7);
		maxBillboardsPerPlayer = config.getInt("max-billboards-per-player", -1);

		signEditGuiConfig.reloadConfig();

		// write changes back to config:
		this.saveConfig();
	}

	@Override
	public void saveConfig() {
		// write current settings to config:
		FileConfiguration config = this.getConfig();
		config.set("default-price", defaultPrice);
		config.set("default-duration-in-days", defaultDurationInDays);
		config.set("max-billboards-per-player", maxBillboardsPerPlayer);

		super.saveConfig();
	}

	// SETUP

	private boolean setupEconomy() {
		if (economy != null) return true;
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		if (economy == null) {
			this.getLogger().severe("No economy plugin was found! Disables now!");
			Bukkit.getPluginManager().disablePlugin(this);
			return false;
		}
		return true;
	}

	@EventHandler
	public void onServerLoaded(ServerLoadEvent event) {
		setupEconomy();
	}

	// BILLBOARDS

	public void addBillboard(BillboardSign billboard) {
		Validate.isTrue(!billboard.isValid(), "Billboard was already added!");
		if (billboard.getHologram() == null) {
			billboards.put(billboard.getLocation(), billboard);
		} else {
			// TODO: 添加到悬浮字广告牌列表
		}
		billboard.setValid(true);
	}

	public void removeBillboard(BillboardSign billboard) {
		Validate.isTrue(billboard.isValid(), "Billboard is not valid!");
		if (billboard.getHologram() == null) {
			billboards.remove(billboard.getLocation());
		} else {
			// TODO: 从悬浮字广告牌列表移除
		}
		billboard.setValid(false);
	}

	public void removeAllBillboards() {
		Iterator<BillboardSign> iterator = billboards.values().iterator();
		while (iterator.hasNext()) {
			BillboardSign billboard = iterator.next();
			billboard.setValid(false);
			iterator.remove();
		}
	}

	public BillboardSign getBillboard(SoftBlockLocation location) {
		return billboards.get(location);
	}

	public List<BillboardSign> getRentBillboards(UUID playerUUID) {
		List<BillboardSign> playerBillboards = new ArrayList<>();
		for (BillboardSign billboard : billboardsView) {
			if (billboard.isOwner(playerUUID)) {
				playerBillboards.add(billboard);
			}
		}
		return playerBillboards;
	}

	public void refreshAllSigns() {
		List<BillboardSign> forRemoval = new ArrayList<>();
		for (BillboardSign billboard : billboardsView) {
			HologramHolder hologram = billboard.getHologram();
			if (hologram != null) {
				// TODO: 更新悬浮字
				continue;
			}
			Location location = billboard.getLocation().getBukkitLocation();
			if (location == null) {
				// TODO really remove? what if the world is only temporarily unloaded?
				// TODO add a cleanup command instead?
				this.getLogger().warning("World '" + billboard.getLocation().worldName + "' not found. Removing this billboard sign.");
				forRemoval.add(billboard);
				continue;
			}
			// TODO really load chunks here?
			Block block = location.getBlock();
			if (!(block.getState() instanceof Sign)) {
				this.getLogger().warning("Billboard sign '" + billboard.getLocation().toString() + "' is no longer a sign. Removing this billboard sign.");
				forRemoval.add(billboard);
				continue;
			}

			// check rent time if has owner:
			if (billboard.hasOwner() && billboard.isRentOver()) {
				billboard.resetOwner();
			}
			// update text if has no owner:
			if (!billboard.hasOwner()) {
				Sign sign = (Sign) block.getState();
				setRentableText(billboard, sign);
			}

		}
		// remove invalid billboards:
		if (!forRemoval.isEmpty()) {
			for (BillboardSign billboard : forRemoval) {
				this.removeBillboard(billboard);
			}
			this.saveBillboards();
		}
	}

	// return true if the sign is still valid
	public boolean refreshSign(BillboardSign billboard) {
		if (!billboard.isValid()) {
			this.getLogger().warning("Cannot refresh sign of an no longer valid billboard at '" + billboard.getLocation().toString() + "'.");
			return false;
		}

		Location location = billboard.getLocation().getBukkitLocation();
		if (location == null) {
			this.getLogger().warning("World '" + billboard.getLocation().worldName + "' not found. Removing this billboard sign.");
			this.removeBillboard(billboard);
			this.saveBillboards();
			return false;
		}

		Block block = location.getBlock();
		Material type = block.getType();
		if (Utils.isNotSign(type)) {
			this.getLogger().warning("Billboard '" + billboard.getLocation().toString() + "' is no longer a sign. Removing this billboard sign.");
			this.removeBillboard(billboard);
			this.saveBillboards();
			return false;
		}

		// check rent time if it has an owner:
		if (billboard.hasOwner() && billboard.isRentOver()) {
			billboard.resetOwner();
		}
		// update text if it has no owner:
		if (!billboard.hasOwner()) {
			Sign sign = (Sign) block.getState();
			this.setRentableText(billboard, sign);
		}

		return true;
	}

	private void setRentableText(BillboardSign billboard, Sign sign) {
		String[] msgArgs = billboard.getMessageArgs();

		sign.setLine(0, Message.SIGN_LINE_1.get(msgArgs));
		sign.setLine(1, Message.SIGN_LINE_2.get(msgArgs));
		sign.setLine(2, Message.SIGN_LINE_3.get(msgArgs));
		sign.setLine(3, Message.SIGN_LINE_4.get(msgArgs));
		sign.update();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		this.updateLastKnownName(player.getUniqueId(), player.getName());
	}

	// updates last known names for the specified player:
	void updateLastKnownName(UUID playerUUID, String playerName) {
		Validate.notNull(playerUUID);
		Validate.notNull(playerName);
		boolean dirty = false;
		for (BillboardSign billboard : billboards.values()) {
			if (billboard.isCreator(playerUUID)) {
				if (!playerName.equals(billboard.getLastKnownCreatorName())) {
					billboard.setLastKnownCreatorName(playerName);
					dirty = true;
				}
			}
			if (billboard.isOwner(playerUUID)) {
				if (!playerName.equals(billboard.getLastKnownOwnerName())) {
					billboard.setLastOwnerCreatorName(playerName);
					dirty = true;
				}
			}
		}
		if (dirty) {
			this.saveBillboards();
		}
	}

	// BILLBOARDS DATA

	private File getBillboardsDataFile() {
		return new File(this.getDataFolder(), BILLBOARDS_DATA_FILE);
	}

	private void loadBillboards() {
		// loads billboards data from file:
		File signsDataFile = this.getBillboardsDataFile();
		YamlConfiguration signsData = new YamlConfiguration();
		try (	FileInputStream stream = new FileInputStream(signsDataFile);
				InputStreamReader reader = new InputStreamReader(stream, BILLBOARDS_DATA_FILE_ENCODING)) {
			signsData.load(reader);
		} catch (FileNotFoundException ex) {
			// ignore
		} catch (Exception e) {
			this.getLogger().log(Level.SEVERE, "Failed to load billboards data file!", e);
			return;
		}

		// unload currently loaded signs:
		this.removeAllBillboards();

		// freshly load signs:
		for (String node : signsData.getKeys(false)) {
			ConfigurationSection signSection = signsData.getConfigurationSection(node);
			if (signSection == null) {
				this.getLogger().warning("Couldn't load sign (invalid config section): " + node);
				continue;
			}

			HologramHolder hologram;
			SoftBlockLocation signLocation = null;
			if (node.startsWith("hologram-")) {
				Location loc = SoftBlockLocation.getBukkit(node.substring(9));
				if (loc == null) {
					this.getLogger().warning("Couldn't load hologram (invalid location): " + node);
					continue;
				}
				List<String> lines = signSection.getStringList("hologram");
				hologram = new HologramHolder(node);
				hologramPool.registerHolograms(() -> {
					hologram.hologram = hologram(loc, () -> {});
					hologram.setLines(lines);
				});
			} else {
				hologram = null;
				signLocation = SoftBlockLocation.getFromString(node);
				if (signLocation == null) {
					this.getLogger().warning("Couldn't load sign (invalid location): " + node);
					continue;
				}
			}

			String creatorUUIDString = signSection.getString("creator-uuid");
			UUID creatorUUID = Utils.parseUUID(creatorUUIDString);
			if (creatorUUID == null && !Utils.isEmpty(creatorUUIDString)) {
				this.getLogger().warning("Couldn't load sign (invalid creator uuid): " + node);
				continue;
			}
			String creatorName = signSection.getString("creator-last-known-name");

			String ownerUUIDString = signSection.getString("owner-uuid");
			UUID ownerUUID = Utils.parseUUID(ownerUUIDString);
			if (ownerUUID == null && !Utils.isEmpty(ownerUUIDString)) {
				this.getLogger().warning("Couldn't load sign (invalid owner uuid): " + node);
				continue;
			}
			String ownerName = signSection.getString("owner-last-known-name");

			int durationInDays = signSection.getInt("duration", defaultDurationInDays);
			int price = signSection.getInt("price", defaultPrice);
			long startTime = signSection.getLong("start-time", 0L);

			String commandArg = signSection.getString("command-arg", "");

			BillboardSign billboard = new BillboardSign(hologram, hologram == null ? signLocation : null, creatorUUID, creatorName, ownerUUID, ownerName, durationInDays, price, startTime, commandArg);
			this.addBillboard(billboard);
		}
	}

	public void saveBillboards() {
		YamlConfiguration signsData = new YamlConfiguration();
		// store signs in signs data config:
		for (BillboardSign billboard : billboardsView) {
			String node;
			HologramHolder hologram = billboard.getHologram();
			if (hologram != null) {
				node = hologram.id;
				List<String> holoLines = hologram.getLines();
				signsData.set(node + ".hologram", holoLines);
			} else {
				node = billboard.getLocation().toString();
			}
			signsData.set(node + ".creator-uuid", getUUIDString(billboard.getCreatorUUID()));
			signsData.set(node + ".creator-last-known-name", billboard.getLastKnownCreatorName());
			signsData.set(node + ".owner-uuid", getUUIDString(billboard.getOwnerUUID()));
			signsData.set(node + ".owner-last-known-name", billboard.getLastKnownOwnerName());
			signsData.set(node + ".duration", billboard.getDurationInDays());
			signsData.set(node + ".price", billboard.getPrice());
			signsData.set(node + ".start-time", billboard.getStartTime());
			signsData.set(node + ".command-arg", billboard.getCommandArg());
		}

		// save signs data to file:
		File signsDataFile = this.getBillboardsDataFile();
		File parent = signsDataFile.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		String data = signsData.saveToString();
		try (	FileOutputStream stream = new FileOutputStream(signsDataFile);
				OutputStreamWriter writer = new OutputStreamWriter(stream, BILLBOARDS_DATA_FILE_ENCODING)) {
			writer.write(data);
		} catch (Exception e) {
			this.getLogger().log(Level.SEVERE, "Failed to save billboards data file!", e);
		}
	}

	public GuiManager getGuiManager() {
		return guiManager;
	}

	private static String getUUIDString(UUID uuid) {
		if (uuid == null) return null;
		return uuid.toString();
	}
}
