package de.blablubbabc.billboards.gui;

import de.blablubbabc.billboards.BillboardSign;
import de.blablubbabc.billboards.SignEditing;
import de.blablubbabc.billboards.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class GuiSignEdit implements IGui {
    SignEditing editing;
    Player player;
    BillboardSign billboard;
    public GuiSignEdit(SignEditing editing, Player player, BillboardSign billboard) {
        this.editing = editing;
        this.player = player;
        this.billboard = billboard;
    }
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, 9, "编辑广告牌");
        ItemStack itemAction = Items.buildItem(Material.ARROW, "&e&l编辑点击操作",
                "",
                "&f  其它人右键点击广告牌时  ",
                "&f  传送到的领地  ",
                "");
        ItemStack itemEditSign = Items.buildItem(Material.OAK_SIGN, "&e&l编辑内容",
                "",
                "&f  编辑告示牌内容  ",
                "");
        inv.setItem(0, itemAction);
        inv.setItem(1, itemEditSign);
        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        if (slot == 0) {

        }
        if (slot == 1) {
            editing.openSignEdit(player, billboard);
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
