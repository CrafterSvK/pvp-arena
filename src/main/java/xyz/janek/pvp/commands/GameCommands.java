package xyz.janek.pvp.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import xyz.janek.pvp.Pvp;

import java.lang.reflect.Method;
import java.util.Arrays;

public class GameCommands extends AbstractCommand {
    private static final String[] subcommands = {
            "start", "stop", "pause", "unpause"
    };

    public GameCommands() {
        super("game", "pvp.game", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(args.length > 0)) {
            return false;
        }

        String subcommand = args[0];

        if (Arrays.asList(subcommands).contains(subcommand)) {
            try {
                Method method = this.getClass().getDeclaredMethod(subcommand, CommandSender.class, String[].class);
                method.invoke(this, sender, args);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("Not implemented!");

                return true;
            }
        }

        return false;
    }

    private boolean start(CommandSender sender, String[] args) {
        if (Pvp.started) {
            sender.sendMessage("Already started!");
        }

        Pvp.started = true;
        World world = Bukkit.getWorld("world");

        world.setTime(1000);
        world.setPVP(false);

        Location spawn = world.getSpawnLocation();
        world.getWorldBorder().setCenter(spawn);
        world.getWorldBorder().setSize(500);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Team team = Pvp.scoreboard.getEntryTeam(player.getName());

            Inventory inv = player.getInventory();
            inv.clear();

            if (team != null) {
                player.teleport(spawn);
                player.setHealth(20);
                player.setFoodLevel(20);
                inv.addItem(new ItemStack(Pvp.beds.get(team.getColor())));

                player.setGameMode(GameMode.SURVIVAL);
            } else {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }

        Bukkit.getServer().broadcastMessage("Game has started, 10 minutes till PVP");

        this.startGameLoop();

        return true;
    }

    private void startGameLoop() {
        Pvp.gameTask = Bukkit.getServer().getScheduler().runTaskTimer(AbstractCommand.plugin, () -> {
            World world = Bukkit.getWorld("world");
            switch (Pvp.gameTime) {
                case 10: {
                    world.setPVP(true);
                    Bukkit.getServer().broadcastMessage(ChatColor.RED + "PVP Enabled" + ChatColor.RESET);
                    break;
                }
                case 29:
                case 39:
                case 49:
                case 54: {
                    Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "One minute to border shrink" + ChatColor.RESET);
                    break;
                }
                case 30: {
                    world.getWorldBorder().setSize(400, 120);
                    Bukkit.getServer().broadcastMessage(ChatColor.RED + "Border is shrinking" + ChatColor.RESET);
                    break;
                }
                case 40: {
                    world.getWorldBorder().setSize(300, 120);
                    Bukkit.getServer().broadcastMessage(ChatColor.RED + "Border is shrinking" + ChatColor.RESET);
                    break;
                }
                case 50: {
                    world.getWorldBorder().setSize(150, 180);
                    Bukkit.getServer().broadcastMessage(ChatColor.RED + "Border is shrinking" + ChatColor.RESET);
                    break;
                }
                case 55: {
                    world.getWorldBorder().setSize(100, 60);
                    Bukkit.getServer().broadcastMessage(ChatColor.RED + "Border is shrinking" + ChatColor.RESET);
                    Bukkit.getServer().broadcastMessage(ChatColor.RED + "Don't die, you've been warned" + ChatColor.RESET);
                    break;
                }
            }

            Pvp.gameTime++;
        }, 0, 20 * 60);
    }

    private boolean pause(CommandSender sender, String[] args) {
        if (!Pvp.started) {
            sender.sendMessage("Not yet started!");
            return false;
        }

        Bukkit.getServer().broadcastMessage("Game paused");

        if (Pvp.gameTask != null) {
            Pvp.gameTask.cancel();
            Pvp.gameTask = null;
        }

        return true;
    }

    private boolean unpause(CommandSender sender, String[] args) {
        if (!Pvp.started) {
            sender.sendMessage("Not yet started!");
            return false;
        }

        Bukkit.getServer().broadcastMessage("Game unpaused");

        if (Pvp.gameTask == null) {
            this.startGameLoop();
        }

        return true;
    }

    private boolean stop(CommandSender sender, String[] args) {
        if (!Pvp.started) {
            sender.sendMessage("Not yet started!");
            return false;
        }

        Pvp.started = false;
        Pvp.gameTime = 0;

        if (Pvp.gameTask != null) {
            Pvp.gameTask.cancel();
            Pvp.gameTask = null;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        Bukkit.getServer().broadcastMessage("Game has ended!");

        return true;
    }
}
