package pl.extollite.MaxHealth;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.*;
import cn.nukkit.utils.TextFormat;

import java.util.*;

public class MaxHealthCommand extends CommandManager{

    private final Map<String, Command> commands;

    static MaxHealth plugin;

    public MaxHealthCommand(MaxHealth plugin) {
        super(plugin,"mh", "main command", "/mh");

        this.plugin = plugin;

        this.setAliases(new String[]{"maxhealth"});

        this.commands = new HashMap<>();

    }

    @Override
    public CommandDataVersions generateCustomCommandData(Player player) {
        if (!this.testPermission(player)) {
            return null;
        }

        CommandData customData = this.commandData.clone();

        List<String> aliases = new ArrayList<>();
        aliases.add("mh");
        aliases.add("maxhealth");

        customData.aliases = new CommandEnum("MaxHealthAliases", aliases);

        customData.description = player.getServer().getLanguage().translateString(this.getDescription());
        this.commandParameters.forEach((key, par) -> {
            if (this.commands.get(key).testPermissionSilent(player)) {
                CommandOverload overload = new CommandOverload();
                overload.input.parameters = par;
                customData.overloads.put(key, overload);
            }
        });
        if (customData.overloads.size() == 0) customData.overloads.put("default", new CommandOverload());
        CommandDataVersions versions = new CommandDataVersions();
        versions.versions.add(customData);
        return versions;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (!this.testPermissionSilent(sender)) {
            sender.sendMessage("§cNo permission");
            return false;
        }
        if (args.length < 1) {
            sendUsage(sender);
            return false;
        }
        Command cmd = this.commands.get(args[0]);
        if (cmd == null) {
            sendUsage(sender);
            return false;
        }
        String[] newArgs = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
        cmd.execute(sender, cmd.getName(), newArgs);
        return false;
    }

    private void updateArguments() {
        Map<String, CommandParameter[]> params = new HashMap<>();
        this.commands.forEach((k, v) -> {
            List<CommandParameter> p = new ArrayList<>();
            p.add(new CommandParameter(k, false, new String[]{k}));
            v.getCommandParameters().values().forEach(s -> p.addAll(Arrays.asList(s)));
            params.put(k, p.toArray(new CommandParameter[0]));
        });
        this.setCommandParameters(params);
    }

    public void registerCommand(Command command) {
        this.commands.put(command.getName().replace("mh", "").replace("maxhealth", "").toLowerCase(), command);
        this.updateArguments();
    }

    static public void sendUsage(CommandSender sender) {
        sender.sendMessage(TextFormat.GREEN + "-- MaxHealth " + plugin.getDescription().getVersion() + " --");
        if (!plugin.isMultiLevelHealth()) {
            sender.sendMessage(TextFormat.GREEN + "/mh set <Player Name> <Max Health> [optional: Health] - Sets max health for online player");
            sender.sendMessage(TextFormat.GREEN + "/mh set-max <Max Health> [optional: Instant regenerate true/false] - Sets max health for all players and edits config");
            sender.sendMessage(TextFormat.GREEN + "/mh set-offline <Player Name> <Max Health> [optional: Instant regenerate true/false] - Sets max health for offline player only if played before");

        }else{
            sender.sendMessage(TextFormat.GREEN + "/mh set <Player Name> <World Name> <Max Health> [optional: Health] - Sets max health for online player");
            sender.sendMessage(TextFormat.GREEN + "/mh set-max <Max Health> <World Name> [optional: Instant regenerate true/false] - Sets max health for all players and edits config");
            sender.sendMessage(TextFormat.GREEN + "/mh set-offline <Player Name> <World Name> <Max Health> [optional: Instant regenerate true/false] - Sets max health for offline player only if played before");
        }
    }
}
