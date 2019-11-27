package pl.extollite.MaxHealth;

import cn.nukkit.command.Command;
import cn.nukkit.command.PluginIdentifiableCommand;

public abstract class CommandManager extends Command implements PluginIdentifiableCommand {
    private MaxHealth plugin;

    public CommandManager(MaxHealth plugin, String name, String desc, String usage) {
        super(name, desc, usage);

        this.plugin = plugin;
    }

    public MaxHealth getPlugin() {
        return plugin;
    }
}
