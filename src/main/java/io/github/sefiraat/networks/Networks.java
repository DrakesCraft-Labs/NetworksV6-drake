package io.github.sefiraat.networks;

import com.github.drakescraft_labs.slimefun4.api.SlimefunAddon;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage;
import com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenu;
import com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenuPreset;
import io.github.sefiraat.networks.commands.NetworksMain;
import io.github.sefiraat.networks.integrations.HudCallbacks;
import io.github.sefiraat.networks.integrations.NetheoPlants;
import io.github.sefiraat.networks.listeners.SyncListener;
import io.github.sefiraat.networks.managers.ListenerManager;
import io.github.sefiraat.networks.managers.SupportedPluginManager;
import io.github.sefiraat.networks.network.SupportedRecipes;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import com.github.drakescraft_labs.slimefun4.implementation.Slimefun;
import io.github.sefiraat.networks.utils.NetworkUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.HashSet;

public class Networks extends JavaPlugin implements SlimefunAddon {

    private static Networks instance;

    private final String username;
    private final String repo;
    private final String branch;

    private ListenerManager listenerManager;
    private SupportedPluginManager supportedPluginManager;

    public Networks() {
        this.username = "DrakesCraft-Labs";
        this.repo = "NetworksV6-drake";
        this.branch = "main";
    }

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("########################################");
        getLogger().info("         Networks - By Sefiraat         ");
        getLogger().info("     Port Drake Labs / NetworksV6       ");
        getLogger().info("########################################");

        saveDefaultConfig();

        this.supportedPluginManager = new SupportedPluginManager();
        setupSlimefun();

        this.listenerManager = new ListenerManager();
        this.getCommand("networks").setExecutor(new NetworksMain());

        SupportedRecipes.setup();

        // Slimefun finishes restoring block storage after addon enablement. Reindex twice
        // so controllers can rebuild networks in chunks that never fire ChunkLoadEvent.
        scheduleLoadedChunkReindex(200L);
        scheduleLoadedChunkReindex(600L);

        // Fix dupe bug which breaks the network controller data without player interaction
        Bukkit.getScheduler().runTaskTimer(
                this,
                () -> {
                    Set<Location> wrongs = new HashSet<>();
                    Set<Location> controllers = new HashSet<>(
                            NetworkController.getNetworks().keySet());
                    for (Location controller : controllers) {
                        if (controller != null && controller.getWorld() != null) {
                            int chunkX = controller.getBlockX() >> 4;
                            int chunkZ = controller.getBlockZ() >> 4;
                            if (controller.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                                if (!(BlockStorage.check(controller) instanceof NetworkController)) {
                                    wrongs.add(controller);
                                }
                            }
                        }
                    }

                    for (Location wrong : wrongs) {
                        NetworkUtils.clearNetwork(wrong);
                    }
                },
                5, Slimefun.getTickerTask().getTickRate()
        );
    }

    private void scheduleLoadedChunkReindex(long delayTicks) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                int indexed = SyncListener.indexLoadedChunks();
                getLogger().info("Startup network reindex added " + indexed + " node(s).");
            } catch (Exception exception) {
                getLogger().severe("Startup network reindex failed: " + exception.getMessage());
                exception.printStackTrace();
            }
        }, delayTicks);
    }

    @Override
    public void onDisable() {
        if (instance == null) {
            return;
        }

        try {
            Bukkit.getScheduler().cancelTasks(this);
        } catch (Throwable t) {
            getLogger().severe("Failed to cancel scheduler tasks: " + t.getMessage());
        }
        
        saveData();
        instance = null;
    }

    private void saveData() {
        getLogger().info("Saving Networks data before shutdown...");

        try {
            markNetworkInventoriesDirty();
        } catch (Throwable t) {
            getLogger().severe("Failed to mark all network inventories dirty: " + t.getMessage());
            t.printStackTrace();
        }

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            try {
                final BlockStorage storage = BlockStorage.getStorage(world);
                if (storage != null) {
                    storage.save();
                }
            } catch (Throwable t) {
                getLogger().severe("Failed to save BlockStorage for world " + world.getName() + ": " + t.getMessage());
                t.printStackTrace();
            }
        }

        try {
            BlockStorage.saveChunks();
        } catch (Throwable t) {
            getLogger().severe("Failed to save BlockStorage chunks: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void markNetworkInventoriesDirty() {
        try {
            for (Location location : new HashSet<>(NetworkStorage.getAllNetworkObjects().keySet())) {
                markNetworkInventoryDirty(location);
            }
        } catch (Throwable t) {
            getLogger().severe("Failed to dirty-mark stored network objects: " + t.getMessage());
        }

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            try {
                final BlockStorage storage = BlockStorage.getStorage(world);
                if (storage == null) {
                    continue;
                }

                for (Location location : new HashSet<>(storage.getRawStorage().keySet())) {
                    markNetworkInventoryDirty(location);
                }
            } catch (Throwable t) {
                getLogger().severe("Failed to dirty-mark world raw storage for " + world.getName() + ": " + t.getMessage());
            }
        }
    }

    private void markNetworkInventoryDirty(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        try {
            final SlimefunItem item = BlockStorage.check(location);
            if (item == null || !item.getId().startsWith("NTW_") || !BlockMenuPreset.isInventory(item.getId())) {
                return;
            }

            final BlockMenu menu = BlockStorage.getInventory(location);
            if (menu != null) {
                menu.markDirty();
            }
        } catch (Throwable t) {
            getLogger().severe("Error marking network inventory dirty at location " + location + ": " + t.getMessage());
            t.printStackTrace();
        }
    }

    public void setupSlimefun() {
        getLogger().info("[Networks] --- Starting Slimefun Setup ---");
        NetworkSlimefunItems.setup();

        if (supportedPluginManager.isNetheopoiesis()) {
            try {
                NetheoPlants.setup();
            } catch (NoClassDefFoundError e) {
                getLogger().severe("Netheopoiesis must be updated to meet Networks' requirements.");
            }
        }
        if (supportedPluginManager.isSlimeHud()) {
            try {
                HudCallbacks.setup();
            } catch (NoClassDefFoundError e) {
                getLogger().severe("SlimeHUD must be updated to meet Networks' requirements.");
            }
        }
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return MessageFormat.format("https://github.com/{0}/{1}/issues/", this.username, this.repo);
    }

    @Nonnull
    public static PluginManager getPluginManager() {
        return Networks.getInstance().getServer().getPluginManager();
    }

    public static Networks getInstance() {
        return Networks.instance;
    }

    public static SupportedPluginManager getSupportedPluginManager() {
        return Networks.getInstance().supportedPluginManager;
    }

    public static ListenerManager getListenerManager() {
        return Networks.getInstance().listenerManager;
    }
}
