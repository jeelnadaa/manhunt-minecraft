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
    private long currentGenerationId;
    private boolean chatLogs = true;
    private long headStartSeconds = 0;

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
        headStartSeconds = plugin.getConfig().getLong("head-start", 0);
        baseWorld = plugin.getConfig().getString("base-world", "world");
        manhuntWorldPrefix = plugin.getConfig().getString("manhunt-world", "manhunt_world");

        runnerUuids.clear();
        List<String> uuidStrings = plugin.getConfig().getStringList("runners");
        for (String s : uuidStrings) {
            try {
                runnerUuids.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
        currentGenerationId = plugin.getConfig().getLong("current-generation-id", System.currentTimeMillis());
        chatLogs = plugin.getConfig().getBoolean("chat-logs", true);
    }

    public synchronized void save() {
        plugin.getConfig().set("compass-slots", compassSlots);
        plugin.getConfig().set("time-limit", timeLimitSeconds);
        plugin.getConfig().set("head-start", headStartSeconds);
        plugin.getConfig().set("base-world", baseWorld);
        List<String> strings = runnerUuids.stream().map(UUID::toString).toList();
        plugin.getConfig().set("runners", strings);
        plugin.getConfig().set("current-generation-id", currentGenerationId);
        plugin.getConfig().set("chat-logs", chatLogs);
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

    public long getHeadStartSeconds() {
        return headStartSeconds;
    }

    public synchronized void setHeadStartSeconds(long seconds) {
        this.headStartSeconds = seconds;
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

    public long getCurrentGenerationId() {
        return currentGenerationId;
    }

    public synchronized void updateCurrentGenerationId() {
        this.currentGenerationId = System.currentTimeMillis();
        save();
    }

    public boolean isChatLogsEnabled() {
        return chatLogs;
    }

    public synchronized void setChatLogsEnabled(boolean chatLogs) {
        this.chatLogs = chatLogs;
        save();
    }
}
