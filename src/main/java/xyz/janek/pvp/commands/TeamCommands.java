package xyz.janek.pvp.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import xyz.janek.pvp.Pvp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TeamCommands extends AbstractCommand implements TabCompleter {
    private static final String[] subcommands = {
            "join", "leave", "create", "add", "remove", "colors", "teams", "team", "rules, delete"
    };

    public TeamCommands() {
        super("arena", "pvp.arena", false);

        AbstractCommand.plugin.getCommand("arena").setTabCompleter(this);
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        if (!(args.length > 0)) {
            return false;
        }

        String subcommand = args[0];

        if (Arrays.asList(subcommands).contains(subcommand)) {
            try {
                Method method = this.getClass().getDeclaredMethod(subcommand, Player.class, String[].class);
                method.invoke(this, (Player) sender, args);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("Not implemented!");

                return true;
            }
        }

        return false;
    }

    /**
     * Creates new team with given name that must be also a color
     *
     * @param player Player that invoked this command
     * @param args   Arguments that came with the command
     * @return command success
     */
    public boolean create(Player player, String[] args) {
        String teamName = args[1];
        if (!player.isOp()) {
            player.sendMessage("You are not OP!");
            return false;
        }

        if (Pvp.teams.containsKey(teamName)) {
            player.sendMessage("Team already exists!");
            return false;
        }

        if (!Pvp.colors.containsKey(teamName)) {
            player.sendMessage("Team is not right color. Use: \"/arena colors\" to get colors!");
            return false;
        }

        Team team = Pvp.scoreboard.registerNewTeam(teamName);
        ChatColor color = Pvp.colors.get(teamName);

        team.setColor(color);
        team.setDisplayName(color + teamName + ChatColor.RESET);
        Pvp.teams.put(teamName, team);

        player.sendMessage("Team " + color + teamName + ChatColor.RESET + " was successfully created!");

        if (args.length > 2) {
            int count = 0;
            for (int i = 2; i < args.length; i++) {
                Player playerToAdd = Bukkit.getPlayer(args[i]);
                if (playerToAdd != null) {
                    team.addEntry(playerToAdd.getName());
                    player.setDisplayName(team.getColor() + player.getName() + ChatColor.RESET);

                    count++;
                } else {
                    player.sendMessage("Player \"" + args[i] + "\" does not exist!");
                }
            }

            player.sendMessage("Added " + count + " players to team " + color + teamName + ChatColor.RESET + "!");
        }

        return true;
    }

    /**
     * Adds players to team
     * /team add {team} {player1} {player2} ... {playerN}
     *
     * @param player OP that invoked command
     * @param args   Arguments that came with command
     * @return command success
     */
    public boolean add(Player player, String[] args) {
        if (!player.isOp()) {
            player.sendMessage("You are not OP!");
            return false;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /arena add <Team name> <Player list>");

            return false;
        }

        Team team = Pvp.teams.get(args[1]);

        if (team == null) {
            player.sendMessage("Team " + args[1] + " does not exist!");
            return false;
        }

        if (args.length > 2) {
            int count = 0;
            for (int i = 2; i < args.length; i++) {
                Player playerToAdd = Bukkit.getPlayer(args[i]);

                if (playerToAdd != null) {
                    team.addEntry(playerToAdd.getName());
                    player.setDisplayName(team.getColor() + playerToAdd.getName() + ChatColor.RESET);

                    count++;
                } else {
                    player.sendMessage("Player \"" + args[i] + "\" does not exist!");
                }
            }

            player.sendMessage("Added " + count + " players to team " + team.getColor() + args[1] + ChatColor.RESET + "!");
        } else {
            player.sendMessage("Not enough arguments.");
            return false;
        }

        return true;
    }

    public boolean teams(Player player, String[] args) {
        StringBuilder output = new StringBuilder("Team list:\n");
        for (Map.Entry<String, Team> entry : Pvp.teams.entrySet()) {
            String name = entry.getKey();
            Team team = entry.getValue();

            output.append(team.getColor()).append(name).append(ChatColor.RESET).append("\n");
        }

        player.sendMessage(output.substring(0, output.length() - 1));

        return true;
    }

    public boolean team(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /arena team <Team name>");
            return false;
        }

        String teamName = args[1];

        Team team = Pvp.teams.get(teamName);

        if (team == null) {
            player.sendMessage("Team " + teamName + " does not exist!");
            return false;
        }

        StringBuilder output = new StringBuilder("Team " + team.getColor() + team.getName() + ChatColor.RESET + ":\n");
        for (String entry : team.getEntries()) {
            output.append(entry).append("\n");
        }

        player.sendMessage(output.substring(0, output.length() - 1));

        return true;
    }

    public boolean colors(Player player, String[] args) {
        StringBuilder result = new StringBuilder("Colors: ");

        for (Map.Entry<String, ChatColor> entry : Pvp.colors.entrySet()) {
            result.append(entry.getValue()).append(entry.getKey()).append(ChatColor.RESET).append(", ");
        }

        player.sendMessage(result.substring(0, result.length() - 2));

        return true;
    }

    public boolean remove(Player player, String[] args) {
        if (!player.isOp()) {
            player.sendMessage("You are not OP!");
            return false;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /arena remove <Team name> <Player list>");

            return false;
        }

        Team team = Pvp.teams.get(args[1]);

        if (team == null) {
            player.sendMessage("Team " + args[1] + " does not exist!");
            return false;
        }

        if (args.length > 2) {
            int count = 0;
            for (int i = 2; i < args.length; i++) {
                Player playerToRemove = Bukkit.getPlayer(args[i]);

                if (playerToRemove != null) {
                    boolean removed = team.removeEntry(playerToRemove.getName());

                    if (removed) {
                        player.setDisplayName(playerToRemove.getName());

                        count++;
                    } else {
                        player.sendMessage(
                                "Player \"" + playerToRemove.getName() + "\" is not in team " + team.getColor() + team.getName() + ChatColor.RESET
                        );
                    }
                } else {
                    player.sendMessage("Player \"" + args[i] + "\" does not exist!");
                }
            }

            player.sendMessage("Removed " + count + " players from team " + team.getColor() + args[1] + ChatColor.RESET + "!");
        } else {
            player.sendMessage("Not enough arguments.");
            return false;
        }

        return true;
    }

    public boolean join(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /arena join <Team name>");

            return false;
        }

        String teamName = args[1];

        Team team = Pvp.teams.get(teamName);

        if (team == null) {
            player.sendMessage("Team " + teamName + " does not exist!");
            return false;
        }

        team.addEntry(player.getName());

        player.sendMessage("You have successfully joined team " + team.getColor() + team.getName() + ChatColor.RESET + "!");

        return true;
    }

    public boolean leave(Player player, String[] args) {
        Team team = Pvp.scoreboard.getEntryTeam(player.getName());

        if (team == null) {
            player.sendMessage("You are not member of any team!");
            return false;
        }

        player.setDisplayName(player.getName());
        team.removeEntry(player.getName());

        player.sendMessage("You left team " + team.getColor() + team.getName() + ChatColor.RESET + "!");

        return true;
    }

    public boolean delete(Player player, String[] args) {
        //todo:

        return false;
    }

    private static final String[] teamAc = {"join", "add", "remove", "team"};

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> complete = new ArrayList<>();

        if (args.length == 0) {
            return complete;
        } else if (args.length == 1) {
            if (args[0].equals("")) {
                complete.addAll(Arrays.asList(subcommands));
            } else {
                for (String entry : subcommands) {
                    if (entry.startsWith(args[0])) {
                        complete.add(entry);
                    }
                }
            }

            return complete;
        } else if (Arrays.asList(teamAc).contains(args[0])) {
            if (args[1].equals("")) {
                if (Pvp.teams.size() > 0) {
                    for (Map.Entry<String, Team> entry : Pvp.teams.entrySet()) {
                        String name = entry.getKey();

                        complete.add(name);
                    }
                } else {
                    complete.add("No team");
                }
            } else {
                for (Map.Entry<String, Team> entry : Pvp.teams.entrySet()) {
                    String name = entry.getKey();

                    if (name.startsWith(args[1])) {
                        complete.add(name);
                    }
                }
            }

            return complete;
        } else if (args[0].equals("create")) {
            if (args[1].equals("")) {
                for (Map.Entry<String, ChatColor> entry : Pvp.colors.entrySet()) {
                    String name = entry.getKey();
                    if (!Pvp.teams.containsKey(name)) {
                        complete.add(name);
                    }
                }
            } else {
                for (Map.Entry<String, ChatColor> entry : Pvp.colors.entrySet()) {
                    String name = entry.getKey();
                    if (name.startsWith(args[1]) && !Pvp.teams.containsKey(name)) {
                        complete.add(name);
                    }
                }
            }

            return complete;
        }

        return complete;
    }
}

