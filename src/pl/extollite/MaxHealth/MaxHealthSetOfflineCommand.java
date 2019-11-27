package pl.extollite.MaxHealth;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MaxHealthSetOfflineCommand extends CommandManager {

    private MaxHealth plugin;

    public MaxHealthSetOfflineCommand(MaxHealth plugin) {
        super(plugin, "mhset-offline", "mh set offline command", "/mh set-offline");
        this.plugin = plugin;
        Map<String, CommandParameter[]> parameters = new HashMap<>();
        if( !plugin.isMultiLevelHealth() ){
            parameters.put("set-offline", new CommandParameter[]{
                    new CommandParameter("Player Name", CommandParamType.STRING, false),
                    new CommandParameter("Max Health", CommandParamType.INT, false),
                    new CommandParameter("Regenerate", true, new String[]{"true", "false"})
            });
        }
        else{
            parameters.put("set-offline", new CommandParameter[]{
                    new CommandParameter("Player Name", CommandParamType.STRING, false),
                    new CommandParameter("Max Health", CommandParamType.INT, false),
                    new CommandParameter( "world", false, plugin.levelsName),
                    new CommandParameter("Regenerate", true, new String[]{"true", "false"})
            });
        }
        this.setCommandParameters(parameters);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (MaxHealthSetAllCommand.checkIfGood(sender, args)) return false;
        if(plugin.isMultiLevelHealth()){
            if (args.length < 3) {
                sender.sendMessage(TextFormat.RED + "Too few arguments!");
                return false;
            }

            if(plugin.getServer().getPlayerExact(args[0]) != null){
                sender.sendMessage(TextFormat.RED + "Player online use other command!");
                return false;
            }

            if(!plugin.levelsNameContainsKey(args[2])){
                sender.sendMessage(TextFormat.RED + "Wrong world name!");
                return false;
            }

            boolean regenerate = false;
            if (args.length == 4) {
                regenerate = Boolean.valueOf(args[3]);
            }

            int maxHealth = Integer.valueOf(args[1]);

            boolean found = false;
            try {
                File[] files = new File(plugin.getDataFolder().toString() + "/Players/").listFiles();

                if (files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        Config healths = new Config(files[i], Config.YAML);
                        ConfigSection health = healths.getSection(args[2]);
                        if (healths.getString("name").toLowerCase().equals(args[0].toLowerCase())) {
                            found = true;
                            if (health.getInt("curr-health") > maxHealth) {
                                health.set("curr-health", maxHealth);
                            }
                            health.set("max-health", maxHealth);
                            if (regenerate) {
                                health.set("curr-health", maxHealth);
                            }
                            healths.set(args[2], health);
                            healths.save();
                            break;
                        }
                    }
                }
                return responseInformation(sender, args, maxHealth, found);
            } catch (Exception e) {
                sender.sendMessage(TextFormat.RED + "Wrong command usage, use /mh to list available commands");
                sender.sendMessage(TextFormat.RED + e.getMessage());
            }
        }
        else{
            if (args.length < 2) {
                sender.sendMessage(TextFormat.RED + "Too few arguments");
                return false;
            }

            if(plugin.getServer().getPlayerExact(args[0]) != null){
                sender.sendMessage(TextFormat.RED + "Player online use other command!");
                return false;
            }

            boolean regenerate = false;
            if (args.length == 3) {
                regenerate = Boolean.valueOf(args[2]);
            }

            int maxHealth = Integer.valueOf(args[1]);

            boolean found = false;

            try {
                File[] files = new File(plugin.getDataFolder().toString() + "/Players/").listFiles();

                if (files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        if(files[i].isDirectory())
                            continue;
                        Config health = new Config(files[i], Config.YAML);
                        if (health.getString("name").toLowerCase().equals(args[0].toLowerCase())) {
                            found = true;
                            if (health.getInt("curr-health") > maxHealth) {
                                health.set("curr-health", maxHealth);
                            }
                            health.set("max-health", maxHealth);
                            if (regenerate) {
                                health.set("curr-health", maxHealth);
                            }
                            health.save();
                            break;
                        }
                    }
                }
                return responseInformation(sender, args, maxHealth, found);
            } catch (Exception e) {
                sender.sendMessage(TextFormat.RED + "Wrong command usage, use /mh to list available commands");
                sender.sendMessage(TextFormat.RED + e.getMessage());
            }
        }
        return false;
    }

    private boolean responseInformation(CommandSender sender, String[] args, int maxHealth, boolean found) {
        if(found){
            sender.sendMessage(TextFormat.GREEN + args[0] + " max health set to " + maxHealth);
        }
        else{
            sender.sendMessage(TextFormat.GREEN + args[0] + " was never on the server!");
        }
        return true;
    }
}
