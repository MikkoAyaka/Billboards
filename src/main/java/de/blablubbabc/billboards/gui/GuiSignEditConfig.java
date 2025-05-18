package de.blablubbabc.billboards.gui;

import de.blablubbabc.billboards.entry.BillboardSign;
import de.blablubbabc.billboards.BillboardsPlugin;
import de.blablubbabc.billboards.message.Message;
import de.blablubbabc.billboards.util.ColorHelper;
import de.blablubbabc.billboards.util.Pair;
import de.blablubbabc.billboards.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class GuiSignEditConfig {
    private final BillboardsPlugin plugin;
    private Icon iconCommandArgument;
    private Icon iconEditSign;
    private String itemActionCommand;
    private String itemActionCommandArgRegex;
    public final Map<String, Icon> otherIcon = new HashMap<>();
    protected String title;
    public char[] inventory;
    public GuiSignEditConfig(BillboardsPlugin plugin) {
        this.plugin = plugin;
    }

    public Character getSlotKey(int slot) {
        if (slot < 0 || slot >= inventory.length) return null;
        return inventory[slot];
    }

    public void executeClickCommand(Player player, String commandArg) {
        Utils.runCommand(player, itemActionCommand, commandArg);
    }

    private void loadMainIcon(ConfigurationSection section, String key, Icon loadedIcon) {
        switch (key) {
            case "C": {
                iconCommandArgument = loadedIcon;
                itemActionCommand = section.getString(key + ".action.command");
                itemActionCommandArgRegex = section.getString(key + ".action.command-arg-regex");
                break;
            }
            case "E": {
                iconEditSign = loadedIcon;
                break;
            }
        }
    }

    private ItemStack tryApplyMainIcon(Gui gui, String key, Player target) {
        switch (key) {
            case "C": {
                String arg = gui.billboard.getCommandArg();
                return iconCommandArgument.generateIcon(target, Pair.of("%arg%", arg == null ? "" : arg));
            }
            case "E": {
                return iconEditSign.generateIcon(target);
            }
        }
        return null;
    }

    public Inventory createInventory(Player target) {
        return Bukkit.createInventory(null, inventory.length, Utils.papi(target, title));
    }
    public void applyIcons(Gui gui, Inventory inv, Player target) {
        applyIcons(gui, inv::setItem, target);
    }
    public void applyIcons(Gui gui, BiConsumer<Integer, ItemStack> setItem, Player target) {
        for (int i = 0; i < this.inventory.length; i++) {
            applyIcon(gui, setItem, target, i);
        }
    }
    public void applyIcon(Gui gui, BiConsumer<Integer, ItemStack> setItem, Player target, int i) {
        if (i >= this.inventory.length) return;
        char c = this.inventory[i];
        String key = String.valueOf(c);
        if (key.equals(" ") || key.equals("　")) {
            setItem.accept(i, null);
            return;
        }
        ItemStack item = tryApplyMainIcon(gui, key, target);
        if (item != null) {
            setItem.accept(i, item);
            return;
        }
        Icon icon = otherIcon.get(key);
        if (icon != null) {
            setItem.accept(i, icon.generateIcon(target));
        } else {
            setItem.accept(i, null);
        }
    }

    public void handleClick(Player player, ClickType click, char key) {
        Icon icon = otherIcon.get(String.valueOf(key));
        if (icon != null) {
            icon.click(player, click);
        }
    }

    public void reloadConfig() {
        File file = new File(plugin.getDataFolder(), "gui.yml");
        if (!file.exists()) {
            plugin.saveResource("gui.yml", true);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section;
        title = ColorHelper.parseColor(config.getString("title", "菜单标题"));
        inventory = String.join("", config.getStringList("inventory")).toCharArray();

        section = config.getConfigurationSection("items");
        if (section != null) for (String key : section.getKeys(false)) {
            Icon icon = Icon.getIcon(section, key, false);
            loadMainIcon(section, key, icon);
        }

        otherIcon.clear();
        section = config.getConfigurationSection("other-items");
        if (section != null) for (String key : section.getKeys(false)) {
            Icon icon = Icon.getIcon(section, key, true);
            otherIcon.put(key, icon);
        }
    }

    public Gui gui(Player player, BillboardSign billboardSign) {
        return new Gui(player, billboardSign);
    }

    public class Gui implements IGui {
        private final Player player;
        private final BillboardSign billboard;

        public Gui(Player player, BillboardSign billboard) {
            this.player = player;
            this.billboard = billboard;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public Inventory newInventory() {
            Inventory inv = createInventory(player);
            applyIcons(this, inv, player);
            return inv;
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
            Character c = getSlotKey(slot);
            if (c == null) return;
            event.setCancelled(true);

            switch (String.valueOf(c)) {
                case "C": {
                    if (itemActionCommand.isEmpty()) return;
                    player.closeInventory();

                    player.sendMessage(Message.PROMPT_START.get());
                    plugin.chatPromptListener.put(player.getName(), s -> {
                        if (s.equalsIgnoreCase("#cancel")) {
                            player.sendMessage(Message.PROMPT_CANCELLED.get(player.getName()));
                            return true;
                        }
                        if (!s.matches(itemActionCommandArgRegex)) {
                            player.sendMessage(Message.PROMPT_FAILED.get(player.getName(), s));
                            return false;
                        }
                        billboard.setCommandArg(s);
                        player.sendMessage(Message.PROMPT_SUCCESS.get(player.getName(), s));
                        plugin.saveBillboards();
                        return true;
                    });
                    break;
                }
                case "E": {
                    player.closeInventory();
                    plugin.getScheduler().runNextTick((t) -> plugin.signEditing.openSignEdit(player, billboard));
                    break;
                }
                default:
                    handleClick(player, click, c);
            }
        }

        @Override
        public void onDrag(InventoryView view, InventoryDragEvent event) {
            event.setCancelled(true);
        }

        @Override
        public void onClose(InventoryView view) {
            // do nothing
        }
    }
}