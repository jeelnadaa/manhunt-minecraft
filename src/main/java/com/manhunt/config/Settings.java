package com.manhunt.config;

import com.manhunt.ManhuntPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Settings {
    private final ManhuntPlugin plugin;
    private List<Integer> compassSlots = new ArrayList<>();
    private long timeLimitSeconds;
    private String baseWorld;
    private String manhuntWorldPrefix;
    private List<UUID> runnerUuids = new ArrayList<>();

    public Settings(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        compassSlots = plugin.getConfig().getIntegerList("compass-slots");
        if (compassSlots.isEmpty()) {
            compassSlots.addAll(List.of(8, 7, 6, 5, 4, 40));
        }
        timeLimitSeconds = plugin.getConfig().getLong("time-limit", 0);
        baseWorld = plugin.getConfig().getString("base-world", "world");
        manhuntWorldPrefix = plugin.getConfig().getString("manhunt-world", "manhunt_world");

        runnerUuids.clear();
        List<String> uuidStrings = plugin.getConfig().getStringList("runners");
        for (String s : uuidStrings) {
            try {
                runnerUuids.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public synchronized void save() {
        plugin.getConfig().set("compass-slots", compassSlots);
        plugin.getConfig().set("time-limit", timeLimitSeconds);
        plugin.getConfig().set("base-world", baseWorld);
        plugin.getConfig().set("manhunt-world", manhuntWorldPrefix);
        List<String> strings = runnerUuids.stream().map(UUID::toString).toList();
        plugin.getConfig().set("runners", strings);
        plugin.saveConfig();
    }

    public List<Integer> getCompassSlots() {
        return new ArrayList<>(compassSlots);
    }

    public synchronized void setCompassSlots(List<Integer> slots) {
        this.compassSlots = new ArrayList<>(slots);
        save();
    }

    public long getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public synchronized void setTimeLimitSeconds(long seconds) {
        this.timeLimitSeconds = seconds;
        save();
    }

    public String getBaseWorld() {
        return baseWorld;
    }

    public synchronized void setBaseWorld(String baseWorld) {
        this.baseWorld = baseWorld;
        save();
    }

    public String getManhuntWorldPrefix() {
        return manhuntWorldPrefix;
    }

    public synchronized void setManhuntWorldPrefix(String manhuntWorldPrefix) {
        this.manhuntWorldPrefix = manhuntWorldPrefix;
        save();
    }

    public List<UUID> getRunnerUuids() {
        return new ArrayList<>(runnerUuids);
    }

    public synchronized void setRunnerUuids(List<UUID> runnerUuids) {
        this.runnerUuids = new ArrayList<>(runnerUuids);
        save();
    }

    public synchronized void addRunnerUuid(UUID uuid) {
        if (!runnerUuids.contains(uuid)) {
            runnerUuids.add(uuid);
            save();
        }
    }

    public synchronized void removeRunnerUuid(UUID uuid) {
        if (runnerUuids.contains(uuid)) {
            runnerUuids.remove(uuid);
            save();
        }
    }
}
