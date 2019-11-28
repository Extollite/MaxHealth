package pl.extollite.MaxHealth;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

class PlayerData {
    private Player player;
    private MaxHealth plugin;
    Map<String, Map.Entry<Integer, Integer>> hpInLevels;

    PlayerData(Player player, MaxHealth plugin) {
        this.player = player;
        this.plugin = plugin;
        hpInLevels = new HashMap<>();
        Config health = new Config( plugin.getDataFolder() + "/Players/" + player.getUniqueId() + ".dat", Config.YAML);
        String[] levels = plugin.getLevelsName();
        for(String level : levels){
            ConfigSection configSection = health.getSection(level);
            if(configSection != null){
                Map.Entry<Integer, Integer> hp = new AbstractMap.SimpleImmutableEntry<>(health.getSection(level).getInt("max-health", plugin.getConfig().getInt(level+"-max-health", 20)),
                        health.getSection(level).getInt("curr-health", plugin.getConfig().getInt(level+"-start-health", 20)));
                hpInLevels.put(level, hp);
            }
            else{
                Map.Entry<Integer, Integer> hp = new AbstractMap.SimpleImmutableEntry<>(plugin.getConfig().getInt(level+"-max-health", 20),
                        plugin.getConfig().getInt(level+"-start-health", 20));
                hpInLevels.put(level, hp);
            }
        }
    }

    void savePlayer(){
        Config health = new Config( plugin.getDataFolder() + "/Players/" + player.getUniqueId() + ".dat", Config.YAML);
        health.set("name", player.getName());
        String[] levels = plugin.getLevelsName();
        for(String level : levels){
            if(level.equals(player.getLevel().getName()) && player.isAlive()){
                ConfigSection configSection = new ConfigSection();
                configSection.set("max-health", player.getMaxHealth());
                configSection.set("curr-health", player.getHealth());
                health.set(level, configSection);
            }
            else if(level.equals(player.getLevel().getName())){
                ConfigSection configSection = new ConfigSection();
                configSection.set("max-health", hpInLevels.get(level).getKey());
                configSection.set("curr-health", 0);
                health.set(level, configSection);
            }
            else{
                ConfigSection configSection = new ConfigSection();
                configSection.set("max-health", hpInLevels.get(level).getKey());
                configSection.set("curr-health", hpInLevels.get(level).getValue());
                health.set(level, configSection);
            }
        }
        health.save();
    }

    void setHp(String level){
        Map.Entry <Integer, Integer> hp = hpInLevels.get(level);
        player.setMaxHealth(hp.getKey());
        player.setHealth(hp.getValue());
    }
}
