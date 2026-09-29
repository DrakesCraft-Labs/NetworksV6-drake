package com.balugaq.netex.api.interfaces;

import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.function.BiPredicate;

@NullMarked
@FunctionalInterface
public interface SuperRecipeHandler extends BiPredicate<Player, BlockMenu> {
    default boolean test(Player player, BlockMenu blockMenu) {
        return handle(player, blockMenu);
    }

    boolean handle(Player player, BlockMenu blockMenu);
}
