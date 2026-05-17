package com.manhunt;

import com.manhunt.command.ManhuntCommand;
import com.manhunt.config.ConfigManager;
import com.manhunt.game.GameManager;
import com.manhunt.game.TimerManager;
import com.manhunt.gui.ManhuntGUI;
import com.manhunt.listener.GameListener;
import com.manhunt.listener.InventoryListener;
import com.manhunt.listener.PlayerListener;
import com.manhunt.player.CompassManager;
import com.manhunt.player.PlayerManager;
import com.manhunt.player.ProfileManager;
import com.manhunt.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public class ManhuntPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private WorldManager worldManager;
    private PlayerManager playerManager;
    private CompassManager compassManager;
    private GameManager gameManager;
    private TimerManager timerManager;
    private ManhuntGUI guiManager;
    private ProfileManager profileManager;

    @Override
    public void onEnable() {
        // 1. Config
        this.configManager = new ConfigManager(this);
        this.configManager.init();

        // 2. Managers
        this.profileManager = new ProfileManager(this);
        this.worldManager = new WorldManager(this);
        this.playerManager = new PlayerManager(this);
        this.compassManager = new CompassManager(this);
        this.gameManager = new GameManager(this);
        this.timerManager = new TimerManager(this);
        this.guiManager = new ManhuntGUI(this);

        // 3. Init HUD & Tasks
        this.timerManager.init();
        this.compassManager.startCompassUpdaterTask();

        // 4. Listeners
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(this.guiManager, this);

        // 5. Commands
        ManhuntCommand cmd = new ManhuntCommand(this);
        Objects.requireNonNull(getCommand("manhunt")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("manhunt")).setTabCompleter(cmd);

        getLogger().info("=======================================");
        getLogger().info("     Manhunt Plugin v1.0.0 Enabled!    ");
        getLogger().info("=======================================");
    }

    @Override
    public void onDisable() {
        if (this.timerManager != null) {
            this.timerManager.reset();
        }
        if (this.configManager != null) {
            this.configManager.getSettings().save();
        }
        getLogger().info("Manhunt Plugin Disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CompassManager getCompassManager() {
        return compassManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public ManhuntGUI getGuiManager() {
        return guiManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }
}
