package de.blablubbabc.billboards.gui;

import de.blablubbabc.billboards.util.Items;
import de.blablubbabc.billboards.util.Pair;
import de.blablubbabc.billboards.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.blablubbabc.billboards.util.ColorHelper.t;
import static de.blablubbabc.billboards.util.Pair.replace;

public class Icon {
    public final String material;
    public final boolean glow;
    @Nullable
    public final Integer customModel;
    @Nullable
    public final String display;
    public final List<String> lore;
    List<String> leftClick = null;
    List<String> rightClick = null;
    List<String> shiftLeftClick = null;
    List<String> shiftRightClick = null;
    List<String> dropClick = null;

    private Icon(String material, boolean glow, @Nullable Integer customModel, @Nullable String display, List<String> lore) {
        this.material = material;
        this.glow = glow;
        this.customModel = customModel;
        this.display = display;
        this.lore = lore;
    }

    @SafeVarargs
    public final ItemStack generateIcon(OfflinePlayer target, ItemStack item, Pair<String, Object>... replacements) {
        if (display != null) {
            Items.setItemDisplayName(item, Utils.papi(target, replace(display, replacements)));
        }
        if (!lore.isEmpty()) {
            Items.setItemLore(item, Utils.papi(target, replace(lore, replacements)));
        }
        if (glow) {
            Items.setGlow(item);
        }
        if (customModel != null) try { // 1.13 or less
            ItemMeta meta = Items.getItemMeta(item);
            meta.setCustomModelData(customModel);
            item.setItemMeta(meta);
        } catch (Throwable ignored) {
        }
        return item;
    }
    @SafeVarargs
    public final ItemStack generateIcon(OfflinePlayer target, Pair<String, Object>... replacements) {
        ItemStack item = new ItemStack(Utils.parseMat(material).orElse(Material.PAPER));
        return generateIcon(target, item, replacements);
    }

    public void click(Player player, ClickType type) {
        List<String> commands = null;
        switch (type) {
            case LEFT:
                commands = leftClick;
                break;
            case RIGHT:
                commands = rightClick;
                break;
            case SHIFT_LEFT:
                commands = shiftLeftClick;
                break;
            case SHIFT_RIGHT:
                commands = shiftRightClick;
                break;
            case DROP:
                commands = dropClick;
                break;
        }
        if (commands == null || commands.isEmpty()) return;
        commands = Utils.papi(player, commands);
        for (String s : commands) {
            if (s.startsWith("[console]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.substring(9).trim());
            }
            if (s.startsWith("[player]")) {
                Bukkit.dispatchCommand(player, s.substring(8).trim());
            }
            if (s.startsWith("[message]")) {
                t(player, s.substring(9).replace("&", "§"));
            }
        }
    }

    public static Icon getIcon(ConfigurationSection section, String key, boolean commands) {
        String material = section.getString(key + ".material", "STONE");
        boolean glow = section.getBoolean(key + ".glow", false);
        Integer customModel = section.contains(key + ".custom-model")
                ? section.getInt(key + ".custom-model")
                : null;
        String display = section.getString(key + ".display", null);
        List<String> lore = section.getStringList(key + ".lore");
        Icon icon = new Icon(material, glow, customModel, display, lore);
        if (commands) {
            icon.leftClick = section.getStringList(key + ".left-click-commands");
            icon.rightClick = section.getStringList(key + ".right-click-commands");
            icon.shiftLeftClick = section.getStringList(key + ".shift-left-click-commands");
            icon.shiftRightClick = section.getStringList(key + ".shift-right-click-commands");
            icon.dropClick = section.getStringList(key + ".drop-commands");
        }
        return icon;
    }
}
