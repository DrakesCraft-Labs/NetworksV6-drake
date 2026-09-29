package cl.jackstar.networks.compat;

import com.ytdd9527.networksexpansion.core.items.machines.AdvancedAutoCrafter;
import com.ytdd9527.networksexpansion.implementation.ExpansionItemsMenus;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedExport;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedImport;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedPurger;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedVacuum;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.SmartGrabber;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.SmartPusher;

import io.github.sefiraat.networks.Networks;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * COMPATIBILIDAD DE ÍTEMS (item-safety) para la fusión Igdrassil→V6.
 *
 * <p>La era V6 de DrakesCraft registró 9 ids de máquina que NO existen en la base
 * NetworksExpansion (Igdrassil): {@code NTW_ADVANCED_*}. Los jugadores tienen esas
 * máquinas colocadas en el mundo AHORA. Si el plugin fusionado no las registra,
 * Slimefun las trataría como "unregistered" y quedarían inertes / se perderían al
 * romperlas.
 *
 * <p>Esta clase vuelve a registrar exactamente esos 9 ids (mismo id y material que en
 * V6) respaldados por las clases de máquina equivalentes de Igdrassil, de modo que los
 * bloques existentes siguen reconociéndose y funcionando. No llevan receta
 * ({@link RecipeType#NULL}): son legacy, no se craftean nuevos; sólo se preservan.
 */
public final class DrakeLegacyMachines {

    private static final ItemStack[] NO_RECIPE = new ItemStack[9];

    private DrakeLegacyMachines() {
    }

    public static void setup(Networks plugin) {
        var group = ExpansionItemsMenus.MENU_FUNCTIONAL_MACHINE;

        new AdvancedImport(group,
                new SlimefunItemStack("NTW_ADVANCED_IMPORT", Material.BLUE_GLAZED_TERRACOTTA,
                        "&bAdvanced Import &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE).register(plugin);

        new AdvancedExport(group,
                new SlimefunItemStack("NTW_ADVANCED_EXPORT", Material.PURPLE_GLAZED_TERRACOTTA,
                        "&bAdvanced Export &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE).register(plugin);

        new SmartGrabber(group,
                new SlimefunItemStack("NTW_ADVANCED_GRABBER", Material.PURPLE_GLAZED_TERRACOTTA,
                        "&bAdvanced Grabber &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE).register(plugin);

        new SmartPusher(group,
                new SlimefunItemStack("NTW_ADVANCED_PUSHER", Material.BLUE_GLAZED_TERRACOTTA,
                        "&bAdvanced Pusher &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE).register(plugin);

        new AdvancedVacuum(group,
                new SlimefunItemStack("NTW_ADVANCED_VACUUM", Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
                        "&bAdvanced Vacuum &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE).register(plugin);

        new AdvancedPurger(group,
                new SlimefunItemStack("NTW_ADVANCED_PURGER", Material.RED_GLAZED_TERRACOTTA,
                        "&bAdvanced Purger &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE).register(plugin);

        new AdvancedGreedyBlock(group,
                new SlimefunItemStack("NTW_ADVANCED_GREEDY_BLOCK", Material.GLOWSTONE,
                        "&bAdvanced Greedy Block &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE).register(plugin);

        new AdvancedAutoCrafter(group,
                new SlimefunItemStack("NTW_ADVANCED_AUTO_CRAFTER", Material.CYAN_GLAZED_TERRACOTTA,
                        "&bAdvanced Auto Crafter &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE, 640, false).register(plugin);

        new AdvancedAutoCrafter(group,
                new SlimefunItemStack("NTW_ADVANCED_AUTO_CRAFTER_WITHHOLDING", Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
                        "&bAdvanced Auto Crafter (Withholding) &7(Legacy V6)"),
                RecipeType.NULL, NO_RECIPE, 640, true).register(plugin);

        plugin.getLogger().info("[Drake] 9 ids legacy NTW_ADVANCED_* re-registrados (item-safety fusion Igdrassil).");
    }
}
