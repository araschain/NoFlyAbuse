package com.arassz;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NoFlyAbuse extends JavaPlugin implements Listener {

    private final Set<UUID> combatPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NoFlyAbuse online!");
        getLogger().info("Author: arassz");
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        disableFlight(player);
        disableFlight(damager);

        new BukkitRunnable() {
            @Override
            public void run() {
                enableFlight(player);
                enableFlight(damager);
            }
        }.runTaskLater(this, 15 * 20);
    }

    @EventHandler
    public void onFlightAttempt(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (combatPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    private void disableFlight(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        player.setAllowFlight(false);
        player.setFlying(false);
        combatPlayers.add(player.getUniqueId());
    }

    private void enableFlight(Player player) {
        if (combatPlayers.remove(player.getUniqueId())) {
            for (String perm : getConfig().getStringList("allowed-flight-perms")) {
                if (player.hasPermission(perm)) {
                    player.setAllowFlight(true);
                    return;
                }
            }
        }
    }
}