package io.github.sefiraat.networks.utils;

import io.github.sefiraat.networks.network.NetworkRoot;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage;
import com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Evita dupes al mover items desde menús externos hacia la red (#240, #235 y similares).
 */
public final class NetworkTransportUtils {

    private static final String NETWORK_ID_PREFIX = "NTW_";

    private NetworkTransportUtils() {}

    /**
     * Inserta en la red el stack del slot origen y solo cuenta éxito si el amount bajó.
     *
     * @return cantidad realmente absorbida por la red
     */
    public static int pullIntoNetwork(
            @Nonnull NetworkRoot root,
            @Nonnull org.bukkit.Location accessor,
            @Nonnull BlockMenu sourceMenu,
            int slot) {
        final ItemStack stack = sourceMenu.getItemInSlot(slot);
        if (stack == null || stack.getType() == Material.AIR) {
            return 0;
        }

        final int before = stack.getAmount();
        root.addItemStack0(accessor, stack);
        final int consumed = before - stack.getAmount();

        if (consumed > 0) {
            sourceMenu.markDirty();
            root.uncontrolAccessInput(accessor);
        }

        return consumed;
    }

    /**
     * Extrae desde un inventario vanilla (barriles, cofres) con escritura explícita en el slot.
     */
    public static int pullFromInventory(
            @Nonnull NetworkRoot root,
            @Nonnull org.bukkit.Location accessor,
            @Nonnull Inventory inventory,
            int slot) {
        final ItemStack stack = inventory.getItem(slot);
        if (stack == null || stack.getType() == Material.AIR) {
            return 0;
        }

        final int before = stack.getAmount();
        root.addItemStack0(accessor, stack);
        final int consumed = before - stack.getAmount();

        if (consumed <= 0) {
            return 0;
        }

        final int remaining = before - consumed;
        if (remaining <= 0) {
            inventory.setItem(slot, null);
        } else {
            final ItemStack updated = stack.clone();
            updated.setAmount(remaining);
            inventory.setItem(slot, updated);
        }

        if (inventory.getHolder() instanceof org.bukkit.block.BlockState state) {
            state.update(true, false);
        }

        root.uncontrolAccessInput(accessor);
        return consumed;
    }

    /**
     * Vacía el slot de salida interno de un vanilla grabber hacia la red (#235 jam en OUTPUT).
     */
    public static int flushMenuSlotToNetwork(
            @Nonnull NetworkRoot root,
            @Nonnull org.bukkit.Location accessor,
            @Nonnull BlockMenu menu,
            int slot) {
        return pullIntoNetwork(root, accessor, menu, slot);
    }

    /**
     * No extraer desde otra máquina Networks (evita estados híbridos tipo #230).
     */
    public static boolean isExternalInventory(@Nullable BlockMenu menu) {
        if (menu == null) {
            return false;
        }
        final SlimefunItem item = BlockStorage.check(menu.getBlock());
        return item == null || !item.getId().startsWith(NETWORK_ID_PREFIX);
    }
}
