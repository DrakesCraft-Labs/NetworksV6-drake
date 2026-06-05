package io.github.sefiraat.networks.slimefun.network;

import io.github.sefiraat.networks.BukkitTestSupport;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkAutoCrafterTest extends BukkitTestSupport {

    @Test
    void exactPowerAmountAllowsCrafting() {
        assertTrue(NetworkAutoCrafter.hasSufficientPower(500, 500));
        assertFalse(NetworkAutoCrafter.hasSufficientPower(499, 500));
    }

    @Test
    void outputCanFillStackExactly() {
        final ItemStack current = new ItemStack(Material.DIAMOND, 63);
        final ItemStack crafted = new ItemStack(Material.DIAMOND, 1);

        assertTrue(NetworkAutoCrafter.canFitOutput(current, crafted));
    }

    @Test
    void outputRejectsOverflowAndMismatchedItems() {
        assertFalse(NetworkAutoCrafter.canFitOutput(
            new ItemStack(Material.DIAMOND, 64),
            new ItemStack(Material.DIAMOND, 1)
        ));
        assertFalse(NetworkAutoCrafter.canFitOutput(
            new ItemStack(Material.DIAMOND, 1),
            new ItemStack(Material.EMERALD, 1)
        ));
    }
}
