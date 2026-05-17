package com.manhunt.command;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ManhuntCommand implements CommandExecutor, TabCompleter {
    private final ManhuntPlugin plugin;

    public ManhuntCommand(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.getGuiManager().openMainGUI(player);
                return true;
            } else {
                sender.sendMessage("§cConsole must specify a subcommand. Try /manhunt help");
                return true;
            }
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "gui" -> {
                if (sender instanceof Player player) {
                    plugin.getGuiManager().openMainGUI(player);
                } else {
                    sender.sendMessage("§cOnly players can open the GUI.");
                }
            }
            case "generate" -> {
                sender.sendMessage("§eGenerating Manhunt worlds...");
                plugin.getWorldManager().generateWorlds(() -> sender.sendMessage("§aWorlds generated successfully!"));
            }
            case "tp" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
                    plugin.getWorldManager().teleportAllToManhunt();
                    sender.sendMessage("§aTeleported all players to Manhunt World.");
                } else if (args.length > 1) {
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target != null) {
                        plugin.getWorldManager().teleportToManhunt(target);
                        sender.sendMessage("§aTeleported " + target.getName() + " to Manhunt World.");
                    } else {
                        sender.sendMessage("§cPlayer not found.");
                    }
                } else if (sender instanceof Player player) {
                    plugin.getWorldManager().teleportToManhunt(player);
                } else {
                    sender.sendMessage("§cConsole must specify a player.");
                }
            }
            case "start" -> {
                plugin.getGameManager().start();
                sender.sendMessage("§aStarted Manhunt match!");
            }
            case "pause" -> {
                plugin.getGameManager().pause();
                sender.sendMessage("§ePaused Manhunt match!");
            }
            case "resume" -> {
                plugin.getGameManager().resume();
                sender.sendMessage("§aResumed Manhunt match!");
            }
            case "end" -> {
                plugin.getGameManager().end();
                sender.sendMessage("§cEnded Manhunt match!");
            }
            case "runner" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /manhunt runner <add|remove|list> [player]");
                    return true;
                }
                String rAction = args[1].toLowerCase();
                if (rAction.equals("list")) {
                    sender.sendMessage("§eActive Runners: ");
                    plugin.getPlayerManager().getOnlineRunners().forEach(r -> sender.sendMessage("§7- §a" + r.getName()));
                } else if (args.length >= 3) {
                    Player target = Bukkit.getPlayerExact(args[2]);
                    if (target != null) {
                        if (rAction.equals("add")) {
                            plugin.getPlayerManager().addRunner(target);
                            sender.sendMessage("§aAdded " + target.getName() + " as a runner!");
                        } else if (rAction.equals("remove")) {
                            plugin.getPlayerManager().removeRunner(target);
                            sender.sendMessage("§cRemoved " + target.getName() + " from runners.");
                        }
                    } else {
                        sender.sendMessage("§cPlayer not found.");
                    }
                } else {
                    sender.sendMessage("§cSpecify a player.");
                }
            }
            case "timer" -> {
                if (args.length >= 3 && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("limit"))) {
                    try {
                        long secs = Long.parseLong(args[2]);
                        plugin.getConfigManager().getSettings().setTimeLimitSeconds(secs);
                        sender.sendMessage("§aTime limit instantly updated to " + secs + " seconds.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid number.");
                    }
                } else {
                    sender.sendMessage("§cUsage: /manhunt timer limit <seconds>");
                }
            }
            case "reload" -> {
                plugin.getConfigManager().reload();
                sender.sendMessage("§aManhunt configuration reloaded from disk.");
            }
            default -> sender.sendMessage("§cUnknown subcommand. Options: gui, generate, tp, start, pause, resume, end, runner, timer, reload");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("gui", "generate", "tp", "start", "pause", "resume", "end", "runner", "timer", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("runner")) {
            return List.of("add", "remove", "list");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("runner") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            List<String> list = new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            list.add("all");
            return list;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("timer")) {
            return List.of("limit");
        }
        return List.of();
    }
}
