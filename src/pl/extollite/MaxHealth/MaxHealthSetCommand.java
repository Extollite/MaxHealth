package pl.extollite.MaxHealth;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;

public class MaxHealthSetCommand extends CommandManager {

    private MaxHealth plugin;

    public MaxHealthSetCommand(MaxHealth plugin) {
        super(plugin, "mhset", "mh set command", "/mh set");
        this.plugin = plugin;
        Map<String, CommandParameter[]> parameters = new HashMap<>();
        if( !plugin.isMultiLevelHealth() ){
            parameters.put("set", new CommandParameter[]{
                    new CommandParameter("Player Name", CommandParamType.TARGET, false),
                    new CommandParameter("Max Health", CommandParamType.INT, false),
                    new CommandParameter("Health", CommandParamType.INT, true)
            });
        }
        else{
            parameters.put("set", new CommandParameter[]{
                    new CommandParameter("Player Name", CommandParamType.TARGET, false),
                    new CommandParameter( "world", false, plugin.levelsName),
                    new CommandParameter("Max Health", CommandParamType.INT, false),
                    new CommandParameter("Health", CommandParamType.INT, true)
            });
        }
        this.setCommandParameters(parameters);
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (MaxHealthSetAllCommand.checkIfGood(sender, args)) return false;
        if(plugin.isMultiLevelHealth()){
            if (args.length < 3) {
                sender.sendMessage(TextFormat.RED + "Too few arguments");
                return false;
            }
            try {
                Player p = plugin.getServer().getPlayerExact(args[0]);
                if (p == null) {
                    sender.sendMessage(TextFormat.RED + "Wrong player name or player offline");
                    return false;
                }
                if(!plugin.levelsNameContainsKey(args[1])){
                    sender.sendMessage(TextFormat.RED + "Wrong world name");
                    return false;
                }
                if (p.getLevel().getName().equals(args[1])) {
                    p.setMaxHealth(Integer.valueOf(args[2]));
                    if (args.length > 3) {
                        p.setHealth(Integer.valueOf(args[3]));
                    }
                } else{
                    Config healths = new Config(
                            plugin.getDataFolder() + "/Players/" + p.getUniqueId() + ".dat"
                            , Config.YAML);
                    ConfigSection health = healths.getSection(args[1]);
                    health.set("name", p.getName());
                    health.set("max-health", Integer.valueOf(args[2]));
                    if (args.length > 3) {
                        health.set("curr-health", Integer.valueOf(args[3]));
                    }
                    healths.set(args[1], health);
                    healths.save();
                }
                sender.sendMessage(TextFormat.GREEN + p.getName() + "'s Max Health set to " + p.getMaxHealth());
                return true;
            } catch (Exception e) {
                sender.sendMessage(TextFormat.RED + "Wrong command usage, use /mh to list available commands");
            }
        }
        else{
            if (args.length < 2) {
                sender.sendMessage(TextFormat.RED + "Too few arguments");
                return false;
            }
            try {
                Player p = plugin.getServer().getPlayerExact(args[0]);
                if (p == null) {
                    sender.sendMessage(TextFormat.RED + "Wrong player name or player offline");
                    return false;
                }

                p.setMaxHealth(Integer.valueOf(args[1]));


                Config health = new Config(plugin.getDataFolder() + "/Players/" + p.getUniqueId() + ".dat", Config.YAML);
                health.set("name", p.getName());
                health.set("max-health", Integer.valueOf(args[1]));
                if (args.length > 2) {
                    health.set("curr-health", Integer.valueOf(args[2]));
                    p.setHealth(Integer.valueOf(args[2]));
                }
                health.save();
                sender.sendMessage(TextFormat.GREEN + p.getName() + "'s Max Health set to " + p.getMaxHealth());
                return true;
            } catch (Exception e) {
                sender.sendMessage(TextFormat.RED + "Wrong command usage, use /mh to list available commands");
            }
        }
        return false;
    }
}
