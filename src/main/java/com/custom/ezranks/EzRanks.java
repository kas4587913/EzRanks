package com.custom.ezranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class EzRanks extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("ezranks").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("EzRanks has been enabled!");
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public void updatePlayerVisuals(Player player) {
        String rankName = getConfig().getString("players." + player.getUniqueId());
        if (rankName != null && getConfig().contains("ranks." + rankName)) {
            String prefix = getConfig().getString("ranks." + rankName + ".prefix", "");
            player.setPlayerListName(color(prefix + " " + player.getName()));
        } else {
            player.setPlayerListName(player.getName());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerVisuals(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String rankName = getConfig().getString("players." + player.getUniqueId());

        if (rankName != null && getConfig().contains("ranks." + rankName)) {
            String prefix = color(getConfig().getString("ranks." + rankName + ".prefix", ""));
            event.setFormat(prefix + " " + ChatColor.WHITE + "%1$s" + ChatColor.GRAY + ": " + ChatColor.WHITE + "%2$s");
        } else {
            event.setFormat(ChatColor.GRAY + "%1$s: " + ChatColor.WHITE + "%2$s");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] rawArgs) {
        if (!sender.hasPermission("ezranks.admin")) {
            sender.sendMessage(color("&cYou do not have permission to use this command."));
            return true;
        }

        List<String> args = Arrays.asList(rawArgs);

        if (args.isEmpty()) {
            sendHelp(sender);
            return true;
        }

        String sub = args.get(0).toLowerCase();

        if (sub.equals("create")) {
            if (args.size() < 2) {
                sender.sendMessage(color("&cUsage: /ezranks create <rankName>"));
                return true;
            }
            String rank = args.get(1).toLowerCase();
            if (getConfig().contains("ranks." + rank)) {
                sender.sendMessage(color("&cThat rank already exists!"));
                return true;
            }
            String defaultPrefix = "&7[" + args.get(1).toUpperCase() + "&7]";
            getConfig().set("ranks." + rank + ".prefix", defaultPrefix);
            saveConfig();
            sender.sendMessage(color("&aCreated rank &e" + rank + " &awith default prefix " + defaultPrefix));
            return true;
        }

        if (sub.equals("prefix")) {
            if (args.size() < 3) {
                sender.sendMessage(color("&cUsage: /ezranks prefix <rankName> <prefix>"));
                return true;
            }
            String rank = args.get(1).toLowerCase();
            if (!getConfig().contains("ranks." + rank)) {
                sender.sendMessage(color("&cThat rank does not exist! Create it first."));
                return true;
            }
            String newPrefix = String.join(" ", args.subList(2, args.size()));
            getConfig().set("ranks." + rank + ".prefix", newPrefix);
            saveConfig();

            for (Player online : Bukkit.getOnlinePlayers()) {
                updatePlayerVisuals(online);
            }
            sender.sendMessage(color("&aUpdated prefix for &e" + rank + " &ato " + newPrefix));
            return true;
        }

        if (sub.equals("set")) {
            if (args.size() < 3) {
                sender.sendMessage(color("&cUsage: /ezranks set <player> <rankName>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args.get(1));
            if (target == null) {
                sender.sendMessage(color("&cPlayer must be online to set their rank."));
                return true;
            }
            String rank = args.get(2).toLowerCase();
            if (!getConfig().contains("ranks." + rank)) {
                sender.sendMessage(color("&cThat rank does not exist!"));
                return true;
            }
            getConfig().set("players." + target.getUniqueId(), rank);
            saveConfig();
            updatePlayerVisuals(target);
            sender.sendMessage(color("&aSet &e" + target.getName() + "&a's rank to &e" + rank));
            return true;
        }

        if (sub.equals("delete")) {
            if (args.size() < 2) {
                sender.sendMessage(color("&cUsage: /ezranks delete <rankName>"));
                return true;
            }
            String rank = args.get(1).toLowerCase();
            if (!getConfig().contains("ranks." + rank)) {
                sender.sendMessage(color("&cThat rank does not exist!"));
                return true;
            }
            getConfig().set("ranks." + rank, null);
            saveConfig();
            for (Player online : Bukkit.getOnlinePlayers()) {
                updatePlayerVisuals(online);
            }
            sender.sendMessage(color("&aDeleted rank &e" + rank));
            return true;
        }

        if (sub.equals("list")) {
            if (!getConfig().contains("ranks") || getConfig().getConfigurationSection("ranks").getKeys(false).isEmpty()) {
                sender.sendMessage(color("&eNo ranks created yet."));
                return true;
            }
            Set<String> ranks = getConfig().getConfigurationSection("ranks").getKeys(false);
            sender.sendMessage(color("&6&lAvailable Ranks:"));
            for (String r : ranks) {
                String pref = getConfig().getString("ranks." + r + ".prefix");
                sender.sendMessage(color("&7- &e" + r + " &8-> " + pref));
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&6&lEzRanks Commands:"));
        sender.sendMessage(color("&e/ezranks create <name> &7- Create a new rank"));
        sender.sendMessage(color("&e/ezranks prefix <name> <prefix> &7- Set rank prefix (supports & color codes)"));
        sender.sendMessage(color("&e/ezranks set <player> <name> &7- Give a player a rank"));
        sender.sendMessage(color("&e/ezranks delete <name> &7- Delete a rank"));
        sender.sendMessage(color("&e/ezranks list &7- View all ranks"));
    }
}
