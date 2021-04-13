package xyz.janek.pvp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

abstract public class AbstractCommand implements CommandExecutor {
    private final String commandName;
    private final String permission;
    private final boolean canConsoleUse;
    public static JavaPlugin plugin;

    public AbstractCommand(String commandName, String permission, boolean canConsoleUse) {
        this.commandName = commandName;
        this.permission = permission;
        this.canConsoleUse = canConsoleUse;

        plugin.getCommand(commandName).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(this.permission)) {
            sender.sendMessage("Not enough permissions!");

            return true;
        }

        if (!canConsoleUse && !(sender instanceof Player)) {
            sender.sendMessage("Only player can use this command!");

            return true;
        }

        return execute(sender, args);
    }

    public abstract boolean execute(final CommandSender sender, final String[] args);

    public static void registerCommands(JavaPlugin pl) {
        plugin = pl;

        new TeamCommands();
        new GameCommands();
    }
}
