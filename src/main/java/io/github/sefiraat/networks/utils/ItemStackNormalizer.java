package io.github.sefiraat.networks.utils;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Converts specialized Slimefun stacks into plain Bukkit stacks before persistence.
 */
public final class ItemStackNormalizer {

    private ItemStackNormalizer() {
    }

    @Nonnull
    public static ItemStack normalize(@Nonnull ItemStack itemStack) {
        return new ItemStack(itemStack);
    }

    @Nonnull
    public static ItemStack[] normalizeRecipe(@Nonnull ItemStack[] recipeItems) {
        final ItemStack[] normalized = new ItemStack[recipeItems.length];

        for (int index = 0; index < recipeItems.length; index++) {
            final ItemStack itemStack = recipeItems[index];
            normalized[index] = itemStack == null ? null : normalize(itemStack);
        }

        return normalized;
    }
}
