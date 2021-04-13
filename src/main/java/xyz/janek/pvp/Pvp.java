package xyz.janek.pvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import xyz.janek.pvp.commands.AbstractCommand;

import java.util.HashMap;
import java.util.Iterator;

public final class Pvp extends JavaPlugin implements Listener {
    public static HashMap<String, Team> teams = new HashMap<>();
    public static HashMap<String, ChatColor> colors = new HashMap<>();
    public static HashMap<ChatColor, Material> beds = new HashMap<>();
    public static HashMap<String, Boolean> config = new HashMap<>();

    public static Scoreboard scoreboard;
    public static boolean started = false;
    public static BukkitTask gameTask = null;
    public static int gameTime = 0;

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        //Static name -> color association
        colors.put("White", ChatColor.WHITE);
        colors.put("Black", ChatColor.BLACK);
        colors.put("Blue", ChatColor.BLUE);
        colors.put("Red", ChatColor.RED);
        colors.put("Green", ChatColor.GREEN);
        colors.put("Magenta", ChatColor.DARK_PURPLE);
        colors.put("Purple", ChatColor.LIGHT_PURPLE);
        colors.put("Cyan", ChatColor.AQUA);
        colors.put("Orange", ChatColor.GOLD);

        //Static color -> bed association
        beds.put(ChatColor.WHITE, Material.WHITE_BED);
        beds.put(ChatColor.BLACK, Material.BLACK_BED);
        beds.put(ChatColor.BLUE, Material.BLUE_BED);
        beds.put(ChatColor.RED, Material.RED_BED);
        beds.put(ChatColor.GREEN, Material.PURPLE_BED);
        beds.put(ChatColor.LIGHT_PURPLE, Material.MAGENTA_BED);
        beds.put(ChatColor.AQUA, Material.CYAN_BED);
        beds.put(ChatColor.GOLD, Material.ORANGE_BED);

        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        AbstractCommand.registerCommands(this);

        Bukkit.getConsoleSender().sendMessage("[Arena] Remove all teams");
        for (Team team : scoreboard.getTeams()) {
            Bukkit.getConsoleSender().sendMessage("[Arena] removed team " + team.getName());
            team.unregister();
        }
        Bukkit.getConsoleSender().sendMessage("[Arena] Done...");

        Bukkit.getConsoleSender().sendMessage("[Arena] Remove all bed recipes");
        Iterator<Recipe> it = getServer().recipeIterator();
        Recipe recipe;
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe != null && beds.containsValue(recipe.getResult().getType())) {
                it.remove();
            }
        }
        Bukkit.getConsoleSender().sendMessage("[Arena] Done...");
    }

    @EventHandler
    public void OnBedPlacement(BlockPlaceEvent e) {
        if (started) {
            Material blockMaterial = e.getBlockPlaced().getType();

            if (!beds.containsValue(blockMaterial)) {
                return;
            }

            Player player = e.getPlayer();

            if (beds.get(getTeamColor(player)) != blockMaterial) {
                player.getWorld().createExplosion(e.getBlockPlaced().getLocation(), 5);
            }
        }
    }

    @EventHandler
    public void OnBedDestroy(BlockBreakEvent e) {
        if (started) {
            Material blockMaterial = e.getBlock().getType();
            Player player = e.getPlayer();

            if (beds.containsValue(blockMaterial) && beds.get(getTeamColor(player)) != blockMaterial) {
                e.setDropItems(false);
                return;
            }
        }

        e.setDropItems(true);
    }

    @EventHandler
    public void OnRespawn(PlayerRespawnEvent e) {
        if (started) {
            if (!e.isBedSpawn() || gameTime >= 55) {
                Player player = e.getPlayer();
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("[Arena] Disabled plugin...");
    }

    public ChatColor getTeamColor(Player player) {
        return scoreboard.getEntryTeam(player.getName()).getColor();
    }
}
