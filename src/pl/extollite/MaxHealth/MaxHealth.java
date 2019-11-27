package pl.extollite.MaxHealth;

import cn.nukkit.command.Command;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.*;

public class MaxHealth extends PluginBase implements Listener{

    String[] levelsName;
    private MaxHealthCommand mainCommand;
    Map<UUID, PlayerData> players = new HashMap<>();
    private boolean multiLevelHealth;

    String[] getLevelsName() {
        return levelsName;
    }

    boolean isMultiLevelHealth() {
        return multiLevelHealth;
    }

    @Override
    public void onEnable() {

        this.saveDefaultConfig();
        multiLevelHealth = this.getConfig().getBoolean("multiLevelHealth", false);
        new File(this.getDataFolder()+"/Players").mkdirs();
        if(multiLevelHealth){
            List<String> levelsNameBuilder = new LinkedList<>();
            for( Map.Entry<Integer, Level> level : this.getServer().getLevels().entrySet() ){
                if(!this.getConfig().exists(level.getValue().getName()+"-max-health")){
                    this.getConfig().set(level.getValue().getName()+"-max-health", 20);
                    this.getConfig().set(level.getValue().getName()+"-start-health", 20);
                    this.getConfig().save();
                }
                levelsNameBuilder.add(level.getValue().getName());
            }
            levelsName = new String[levelsNameBuilder.size()];
            levelsName = levelsNameBuilder.toArray(levelsName);
        }
        else if(!this.getConfig().exists("max-health")){
            this.getConfig().set("max-health", 20);
            this.getConfig().set("start-health", 20);
            this.getConfig().save();
        }

        double version = this.getConfig().getDouble("version", 0);
        if(version < 3.0){
            this.getServer().getLogger().warning("MaxHealth's config file is outdated! Remove old file and Player Files!");
            this.getLogger().error("MaxHealth will be disabled!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        List<String> authors = this.getDescription().getAuthors();
        this.getLogger().info(TextFormat.DARK_GREEN + "Plugin by "+authors.get(0));
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        this.mainCommand = new MaxHealthCommand(this);
        this.getServer().getCommandMap().register("maxhealth", this.mainCommand);

        this.registerCommand(new MaxHealthSetCommand(this));
        this.registerCommand(new MaxHealthSetAllCommand(this));
        this.registerCommand(new MaxHealthSetOfflineCommand(this));
    }

    private void registerCommand(Command command) {
        this.mainCommand.registerCommand(command);
    }

    boolean levelsNameContainsKey(String key){
        for (String s : levelsName) {
            if (s.equals(key)) {
                return true;
            }
        }
        return false;
    }



}
