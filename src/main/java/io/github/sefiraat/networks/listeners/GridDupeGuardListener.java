package io.github.sefiraat.networks.listeners;

import io.github.sefiraat.networks.slimefun.network.grid.AbstractGrid;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage;
import com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;

/**
 * Bloquea clics de inventario abusivos en grids (#230, dupe tipo COLLECT_TO_CURSOR / middle).
 */
public class GridDupeGuardListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(@Nonnull InventoryClickEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BlockMenu blockMenu)) {
            return;
        }

        final SlimefunItem slimefunItem = BlockStorage.check(blockMenu.getLocation());
        if (!(slimefunItem instanceof AbstractGrid)) {
            return;
        }

        final ClickType click = event.getClick();
        final InventoryAction action = event.getAction();

        if (click == ClickType.MIDDLE
                || click == ClickType.DOUBLE_CLICK
                || click == ClickType.NUMBER_KEY
                || click == ClickType.SWAP_OFFHAND
                || click == ClickType.CREATIVE
                || action == InventoryAction.COLLECT_TO_CURSOR
                || action == InventoryAction.UNKNOWN
                || action == InventoryAction.DROP_ALL_CURSOR
                || action == InventoryAction.DROP_ALL_SLOT
                || action == InventoryAction.HOTBAR_MOVE_AND_READD
                || action == InventoryAction.HOTBAR_SWAP
                || action == InventoryAction.CLONE_STACK) {
            event.setCancelled(true);
        }
    }
}
