package io.github.sefiraat.networks.listeners;

import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.utils.NetworkIntegrity;
import io.github.sefiraat.networks.utils.NetworkUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.StructureGrowEvent;

import javax.annotation.Nonnull;

public class SyncListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(@Nonnull BlockBreakEvent event) {
        NetworkUtils.clearNetwork(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(@Nonnull BlockPlaceEvent event) {
        final Location location = event.getBlock().getLocation();
        NetworkIntegrity.onForeignBlockOccupied(location);
        NetworkUtils.clearNetwork(location);
    }

    /** Wither, dripleaf, pistons: evita nodo NTW huérfano (#229). */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(@Nonnull EntityChangeBlockEvent event) {
        final Location location = event.getBlock().getLocation();
        NetworkIntegrity.purgeGhostMembership(location);
        if (NetworkStorage.getAllNetworkObjects().containsKey(location)) {
            NetworkUtils.clearNetwork(location);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(@Nonnull BlockFromToEvent event) {
        NetworkIntegrity.purgeGhostMembership(event.getToBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakMonitor(@Nonnull BlockBreakEvent event) {
        NetworkIntegrity.purgeGhostMembership(event.getBlock().getLocation());
    }

    // Fixed a dupe — árboles/estructuras sobre nodos NTW (#229 / #230)
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onStructureGrow(@Nonnull StructureGrowEvent event) {
        for (BlockState state : event.getBlocks()) {
            final Location location = state.getBlock().getLocation();
            final NodeDefinition definition = NetworkStorage.getAllNetworkObjects().get(location);

            if (definition == null || definition.getNode() == null) {
                continue;
            }

            NetworkUtils.clearNetwork(location);
        }
    }
}
