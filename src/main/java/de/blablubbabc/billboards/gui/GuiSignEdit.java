package de.blablubbabc.billboards.gui;

import de.blablubbabc.billboards.entry.BillboardSign;
import de.blablubbabc.billboards.BillboardsPlugin;
import de.blablubbabc.billboards.message.Message;
import de.blablubbabc.billboards.message.Messages;
import de.blablubbabc.billboards.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class GuiSignEdit implements IGui {
    BillboardsPlugin plugin;
    Player player;
    BillboardSign billboard;
    public GuiSignEdit(BillboardsPlugin plugin, Player player, BillboardSign billboard) {
        this.plugin = plugin;
        this.player = player;
        this.billboard = billboard;
    }
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', plugin.editSignTitle));

        ItemStack itemAction = Items.buildItem(plugin.itemActionMaterial, plugin.itemActionName, plugin.itemActionLore);
        ItemStack itemEditSign = Items.buildItem(plugin.itemEditSignMaterial, plugin.itemEditSignName, plugin.itemEditSignLore);
        inv.setItem(0, itemAction);
        inv.setItem(1, itemEditSign);

        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        if (slot == 0) {
            player.closeInventory();

            player.sendMessage(Messages.getMessage(Message.PROMPT_START));
            plugin.chatPromptListener.put(player.getName(), s -> {
                if (s.equalsIgnoreCase("#cancel")) return true;
                if (!s.matches(plugin.itemActionCommandArgRegex)) {
                    player.sendMessage(Messages.getMessage(Message.PROMPT_FAILED, player.getName(), s));
                    return false;
                }
                billboard.setCommandArg(s);
                player.sendMessage(Messages.getMessage(Message.PROMPT_SUCCESS, player.getName(), s));
                plugin.saveBillboards();
                return true;
            });
        }
        if (slot == 1) {
            plugin.signEditing.openSignEdit(player, billboard);
        }
        event.setCancelled(true);
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
