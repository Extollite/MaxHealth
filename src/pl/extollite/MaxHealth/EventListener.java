package pl.extollite.MaxHealth;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.*;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventListener implements Listener {

    private MaxHealth plugin;

    EventListener(MaxHealth plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();
        Config health = new Config(plugin.getDataFolder() + "/Players/" + player.getUniqueId() + ".dat", Config.YAML);
        if (!health.exists("name")) {
            health.set("name", player.getName());
            if (plugin.isMultiLevelHealth()) {
                PlayerData playerData = new PlayerData(player, plugin);
                plugin.players.put(player.getUniqueId(), playerData);
                playerData.setHp(player.getLevel().getName());
            } else {
                player.setMaxHealth(plugin.getConfig().getInt("max-health", 20));
                player.setHealth(plugin.getConfig().getInt("start-health", 20));
            }
        } else {
            if (plugin.isMultiLevelHealth()) {
                PlayerData playerData = new PlayerData(player, plugin);
                plugin.players.put(player.getUniqueId(), playerData);
                playerData.setHp(player.getLevel().getName());
            } else {
                player.setMaxHealth(health.getInt("max-health", plugin.getConfig().getInt("max-health", 20)));
                player.setHealth(health.getInt("curr-health", plugin.getConfig().getInt("start-health", 20)));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        if (plugin.isMultiLevelHealth()) {
            plugin.players.get(ev.getPlayer().getUniqueId()).savePlayer();
        } else {
            Player player = ev.getPlayer();
            Config health = new Config(plugin.getDataFolder() + "/Players/" + player.getUniqueId() + ".dat", Config.YAML);
            health.set("name", player.getName());
            health.set("max-health", player.getMaxHealth());
            health.set("curr-health", player.getHealth());
            health.save();
        }
    }

    Map<UUID, TaskHandler> task1 = new HashMap<>();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent ev) {
        if (plugin.isMultiLevelHealth() && !ev.getFrom().level.getName().equalsIgnoreCase(ev.getTo().level.getName()) && ev.getPlayer().isAlive()) {
            Player player = ev.getPlayer();
            plugin.players.get(player.getUniqueId()).hpInLevels.replace(ev.getFrom().getLevel().getName(), new AbstractMap.SimpleImmutableEntry<>(player.getMaxHealth(), (int) player.getHealth()));
            task1.put(ev.getPlayer().getUniqueId(),
                    plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin, () -> {
                        if (ev.getPlayer().getLevel().getName().equalsIgnoreCase(ev.getTo().getLevel().getName())) {
                            plugin.players.get(player.getUniqueId()).setHp(player.getLevel().getName());
                            task1.remove(player.getUniqueId()).cancel();
                        }
                    }, 5, 5)
            );
        }
    }

    Map<UUID, TaskHandler> task = new HashMap<>();
    @EventHandler
    public void onDeath(PlayerDeathEvent ev) {
        if (plugin.isMultiLevelHealth() && !ev.getEntity().getSpawn().getLevel().getName().equalsIgnoreCase(ev.getEntity().getLevel().getName())) {
            Player player = ev.getEntity();
            plugin.players.get(player.getUniqueId()).hpInLevels.replace(player.getLevel().getName(), new AbstractMap.SimpleImmutableEntry<>(player.getMaxHealth(), player.getMaxHealth()));
            task.put(player.getUniqueId(),
                    plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin, () -> {
                        if (player.getLevel().getName().equalsIgnoreCase(player.getSpawn().getLevel().getName()) && player.isOnline() && player.isAlive()) {
                            plugin.players.get(player.getUniqueId()).setHp(player.getLevel().getName());
                            task.remove(player.getUniqueId()).cancel();
                        } else if (!player.isOnline()) {
                            task.remove(player.getUniqueId()).cancel();
                        }
                    }, 5, 5)
            );
        }
    }
/*    @EventHandler
    public void onRespawn(PlayerRespawnEvent ev){
        if(plugin.isMultiLevelHealth() && !ev.isFirstSpawn()){
            task.put(ev.getPlayer().getUniqueId(),
                    plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin, () -> {
                        if(ev.getPlayer().getLevel().getName().equalsIgnoreCase(ev.getRespawnPosition().getLevel().getName())){
                            Player player = ev.getPlayer();
                            plugin.players.get(player.getUniqueId()).setHp(player.getLevel().getName());
                            plugin.getLogger().info(player.getLevel().getName());
                            task.remove(player.getUniqueId()).cancel();
                        }
                    }, 5,5)
            );
        }
    }*/

}
