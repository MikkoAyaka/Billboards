package de.blablubbabc.billboards.listener;

import de.blablubbabc.billboards.BillboardsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChatPromptListener implements Listener {
    private Map<String, Function<String, Boolean>> prompts = new HashMap<>();
    BillboardsPlugin plugin;
    public ChatPromptListener(BillboardsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean contains(String player) {
        return prompts.containsKey(player.toLowerCase());
    }

    public void put(String player, Function<String, Boolean> action) {
        prompts.put(player.toLowerCase(), action);
    }

    public void remove(String player) {
        prompts.remove(player.toLowerCase());
    }

    public void onPluginEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onPluginDisable() {
        prompts.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        Function<String, Boolean> func = prompts.get(player.getName().toLowerCase());
        if (func == null) return;
        e.setCancelled(true);
        boolean result = func.apply(e.getMessage());
        if (result) remove(player.getName());
    }
}
