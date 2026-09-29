package com.balugaq.netex.api.keybind;

import com.balugaq.netex.api.interfaces.functions.Function5;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
@SuppressWarnings("deprecation")
public interface KeybindPredicate extends Function5<Player, Integer, ItemStack, ClickAction, BlockMenu, Boolean> {
    default Boolean apply(Player player, Integer slot, ItemStack itemStack, ClickAction clickAction, BlockMenu menu) {
        return test(player, slot, itemStack, clickAction, menu);
    }

    boolean test(Player player, int slot, ItemStack itemStack, ClickAction clickAction, BlockMenu menu);
}