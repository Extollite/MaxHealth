package pl.extollite.MaxHealth;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MaxHealthSetAllCommand extends CommandManager {

    private MaxHealth plugin;

    public MaxHealthSetAllCommand(MaxHealth plugin) {
        super(plugin, "mhset-all", "mh set all command", "/mh set-all");
        this.plugin = plugin;
        Map<String, CommandParameter[]> parameters = new HashMap<>();
        if( !plugin.isMultiLevelHealth() ){
            parameters.put("set-all", new CommandParameter[]{
                    new CommandParameter("Max Health", CommandParamType.INT, false),
                    new CommandParameter("Regenerate", true, new String[]{"true", "false"})
            });
        }
        else{
            parameters.put("set-all", new CommandParameter[]{
                    new CommandParameter("Max Health", CommandParamType.INT, false),
                    new CommandParameter( "world", false, plugin.levelsName),
                    new CommandParameter("Regenerate", true, new String[]{"true", "false"})
            });
        }
        this.setCommandParameters(parameters);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (checkIfGood(sender, args)) return false;
        if(plugin.isMultiLevelHealth()){
            if (args.length < 2) {
                sender.sendMessage(TextFormat.RED + "Too few arguments");
                return false;
            }

            if(!plugin.levelsNameContainsKey(args[1])){
                sender.sendMessage(TextFormat.RED + "Wrong world name");
                return false;
            }

            boolean regenerate = false;
            if (args.length == 3) {
                regenerate = Boolean.valueOf(args[2]);
            }

            int maxHealth = Integer.valueOf(args[0]);
            int currMaxHealth = plugin.getConfig().getInt(args[1]+"-max-health");

            try {
                File[] files = new File(plugin.getDataFolder().toString() + "/Players/").listFiles();
                if (files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        Config healths = new Config(files[i], Config.YAML);
                        ConfigSection health = healths.getSection(args[1]);
                        if (health.getInt("max-health") == currMaxHealth || plugin.getConfig().getBoolean("set-allAffectCustomHealth", false)) {
                            if (health.getInt("curr-health") > maxHealth) {
                                health.set("curr-health", maxHealth);
                            }
                            health.set("max-health", maxHealth);
                            if (regenerate) {
                                health.set("curr-health", maxHealth);
                            }
                            healths.set(args[1], health);
                            healths.save();
                            Player online = plugin.getServer().getPlayerExact(health.getString("name"));
                            if (online != null && online.getLevel().getName().equals(args[1]) ) {
                                if (online.getHealth() > maxHealth) {
                                    online.setHealth(maxHealth);
                                }
                                online.setMaxHealth(maxHealth);
                                if (regenerate) {
                                    online.setHealth(maxHealth);
                                }
                            }
                        }
                    }
                }
                plugin.getConfig().set(args[1]+"-max-health", maxHealth);
                plugin.getConfig().set(args[1]+"-start-health", maxHealth);
                plugin.getConfig().save();
                plugin.getConfig().reload();
                sender.sendMessage(TextFormat.GREEN + "All players max health set to " + maxHealth);
                return true;
            } catch (Exception e) {
                sender.sendMessage(TextFormat.RED + "Wrong command usage, use /mh to list available commands");
                sender.sendMessage(TextFormat.RED + e.getMessage());
            }
        }
        else{
            if (args.length < 1) {
                sender.sendMessage(TextFormat.RED + "Too few arguments");
                return false;
            }

            boolean regenerate = false;
            if (args.length == 2) {
                regenerate = Boolean.valueOf(args[1]);
            }

            int maxHealth = Integer.valueOf(args[0]);
            int currMaxHealth = plugin.getConfig().getInt("max-health");

            try {
                File[] files = new File(plugin.getDataFolder().toString() + "/Players/").listFiles();
                if (files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        if(files[i].isDirectory())
                            continue;
                        Config health = new Config(files[i], Config.YAML);
                        if (health.getInt("max-health") == currMaxHealth || plugin.getConfig().getBoolean("set-allAffectCustomHealth", false)) {
                            if (health.getInt("curr-health") > maxHealth) {
                                health.set("curr-health", maxHealth);
                            }
                            health.set("max-health", maxHealth);
                            if (regenerate) {
                                health.set("curr-health", maxHealth);
                            }
                            health.save();
                            Player online = plugin.getServer().getPlayerExact(health.getString("name"));
                            if (online != null ) {
                                if (online.getHealth() > maxHealth) {
                                    online.setHealth(maxHealth);
                                }
                                online.setMaxHealth(maxHealth);
                                if (regenerate) {
                                    online.setHealth(maxHealth);
                                }
                            }
                        }
                    }
                }
                plugin.getConfig().set("max-health", maxHealth);
                plugin.getConfig().set("start-health", maxHealth);
                plugin.getConfig().save();
                plugin.getConfig().reload();
                sender.sendMessage(TextFormat.GREEN + "All players max health set to " + maxHealth);
                return true;
            } catch (Exception e) {
                sender.sendMessage(TextFormat.RED + "Wrong command usage, use /mh to list available commands");
                sender.sendMessage(TextFormat.RED + e.getMessage());
            }
        }
        return false;
    }

    static boolean checkIfGood(CommandSender sender, String[] args) {
        if(!sender.hasPermission("maxhealth.command") && !(sender.isOp())) {
            sender.sendMessage("§cNo permission");
            return true;
        }
        if (args.length == 0) {
            MaxHealthCommand.sendUsage(sender);
            return true;
        }
        return false;
    }
}
