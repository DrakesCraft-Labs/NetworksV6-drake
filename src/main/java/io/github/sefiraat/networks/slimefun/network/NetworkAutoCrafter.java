package io.github.sefiraat.networks.slimefun.network;

import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.SupportedRecipes;
import io.github.sefiraat.networks.network.stackcaches.BlueprintInstance;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.sefiraat.networks.slimefun.tools.CraftingBlueprint;
import io.github.sefiraat.networks.utils.ItemCreator;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.NetworkTransportUtils;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.Theme;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentCraftingBlueprintType;
import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.implementation.Slimefun;
import com.github.drakescraft_labs.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import com.github.drakescraft_labs.slimefun4.legacy.Objects.handlers.BlockTicker;
import com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage;
import com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenu;
import com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenuPreset;
import com.github.drakescraft_labs.slimefun4.legacy.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkAutoCrafter extends NetworkObject {

    private static final int[] BACKGROUND_SLOTS = new int[]{
        3, 4, 5, 12, 13, 14, 21, 22, 23
    };
    private static final int[] BLUEPRINT_BACKGROUND = new int[]{0, 1, 2, 9, 11, 18, 19, 20};
    private static final int[] OUTPUT_BACKGROUND = new int[]{6, 7, 8, 15, 17, 24, 25, 26};

    private static final int BLUEPRINT_SLOT = 10;
    private static final int OUTPUT_SLOT = 16;

    public static final ItemStack BLUEPRINT_BACKGROUND_STACK = ItemCreator.create(
        Material.BLUE_STAINED_GLASS_PANE, Theme.PASSIVE + "Crafting Blueprint"
    );

    public static final ItemStack OUTPUT_BACKGROUND_STACK = ItemCreator.create(
        Material.GREEN_STAINED_GLASS_PANE, Theme.PASSIVE + "Output"
    );

    private final int chargePerCraft;
    private final boolean withholding;

    private static final Map<Location, BlueprintInstance> INSTANCE_MAP = new ConcurrentHashMap<>();

    public NetworkAutoCrafter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int chargePerCraft, boolean withholding) {
        super(itemGroup, item, recipeType, recipe, NodeType.CRAFTER);

        this.chargePerCraft = chargePerCraft;
        this.withholding = withholding;

        this.getSlotsToDrop().add(BLUEPRINT_SLOT);
        this.getSlotsToDrop().add(OUTPUT_SLOT);

        addItemHandler(
            new BlockTicker() {
                @Override
                public boolean isSynchronized() {
                    return true;
                }

                @Override
                public void tick(Block block, SlimefunItem slimefunItem, Config config) {
                    BlockMenu blockMenu = BlockStorage.getInventory(block);
                    if (blockMenu != null) {
                        addToRegistry(block);
                        craftPreFlight(blockMenu);
                    }
                }
            }
        );
    }

    protected void craftPreFlight(@Nonnull BlockMenu blockMenu) {
        releaseCache(blockMenu);

        final NodeDefinition definition = NetworkStorage.getAllNetworkObjects().get(blockMenu.getLocation());

        if (definition == null || definition.getNode() == null) {
            return;
        }

        final NetworkRoot root = definition.getNode().getRoot();

        if (!this.withholding) {
            flushOutputIntoNetwork(blockMenu, root);
        }

        final ItemStack blueprint = blockMenu.getItemInSlot(BLUEPRINT_SLOT);

        if (blueprint == null || blueprint.getType() == Material.AIR) {
            return;
        }

        final long networkCharge = root.getRootPower();

        if (hasSufficientPower(networkCharge, this.chargePerCraft)) {
            final SlimefunItem item = SlimefunItem.getByItem(blueprint);

            if (!(item instanceof CraftingBlueprint)) {
                return;
            }

            BlueprintInstance instance = INSTANCE_MAP.get(blockMenu.getLocation());

            if (instance == null) {
                final ItemMeta blueprintMeta = blueprint.getItemMeta();
                final Optional<BlueprintInstance> optional = DataTypeMethods.getOptionalCustom(blueprintMeta, Keys.BLUEPRINT_INSTANCE, PersistentCraftingBlueprintType.TYPE);

                if (optional.isEmpty()) {
                    return;
                }

                instance = optional.get();
                setCache(blockMenu, instance);
            }

            final ItemStack output = blockMenu.getItemInSlot(OUTPUT_SLOT);

            if (!canFitOutput(output, instance.getItemStack())) {
                return;
            }

            if (tryCraft(blockMenu, instance, root)) {
                root.removeRootPower(this.chargePerCraft);
            }
        }
    }

    private void flushOutputIntoNetwork(@Nonnull BlockMenu blockMenu, @Nonnull NetworkRoot root) {
        final ItemStack stored = blockMenu.getItemInSlot(OUTPUT_SLOT);
        if (stored == null || stored.getType() == Material.AIR) {
            return;
        }

        final int previousAmount = stored.getAmount();
        root.addItemStack0(blockMenu.getLocation(), stored);
        if (stored.getAmount() <= 0) {
            blockMenu.replaceExistingItem(OUTPUT_SLOT, null);
        }
        if (stored.getAmount() != previousAmount) {
            blockMenu.markDirty();
        }
    }

    @Override
    protected void clearCachedState(@Nonnull Location location) {
        INSTANCE_MAP.remove(location);
    }

    private boolean tryCraft(@Nonnull BlockMenu blockMenu, @Nonnull BlueprintInstance instance, @Nonnull NetworkRoot root) {
        // Get the recipe input
        final ItemStack[] inputs = new ItemStack[9];

        final ItemRequest[] requests = new ItemRequest[9];
        for (int i = 0; i < 9; i++) {
            final ItemStack requested = instance.getRecipeItems()[i];
            if (requested != null) {
                requests[i] = new ItemRequest(requested, 1);
            }
        }

        final ItemStack[] extracted = root.getItemStacks0(blockMenu.getLocation(), requests);
        if (extracted == null) {
            return false;
        }
        System.arraycopy(extracted, 0, inputs, 0, inputs.length);

        ItemStack crafted = SupportedRecipes.findRecipe(inputs).orElse(null);

        // If no slimefun recipe found, try a vanilla one
        if (crafted == null) {
            instance.generateVanillaRecipe(blockMenu.getLocation().getWorld());
            if (instance.getRecipe() == null) {
                returnItems(root, inputs, blockMenu.getLocation());
                return false;
            } else {
                boolean recipeMatches = true;
                for (int j = 0; j < 9; j++) {
                    if (!StackUtils.itemsMatch(instance.getRecipeItems()[j], inputs[j])) {
                        recipeMatches = false;
                        break;
                    }
                }
                if (recipeMatches) {
                    setCache(blockMenu, instance);
                    // CRÍTICO: clonar para no mutar el singleton de Bukkit Recipe
                    crafted = instance.getRecipe().getResult().clone();
                }
            }
        }

        // If no item crafted OR result doesn't fit, escape
        if (crafted == null || crafted.getType() == Material.AIR) {
            returnItems(root, inputs, blockMenu.getLocation());
            return false;
        }

        // Push item
        final Location location = blockMenu.getLocation().clone().add(0.5, 1.1, 0.5);
        if (root.isDisplayParticles()) {
            location.getWorld().spawnParticle(Particle.WAX_OFF, location, 0, 0, 4, 0);
        }
        final int craftedAmount = crafted.getAmount();
        final ItemStack leftover = blockMenu.pushItem(crafted, OUTPUT_SLOT);
        if (leftover != null && leftover.getAmount() > 0) {
            if (leftover.getAmount() == craftedAmount) {
                returnItems(root, inputs, blockMenu.getLocation());
                return false;
            }
            blockMenu.getLocation().getWorld().dropItemNaturally(blockMenu.getLocation(), leftover.clone());
        }
        return true;
    }

    private void returnItems(@Nonnull NetworkRoot root, @Nonnull ItemStack[] inputs, @Nonnull Location origin) {
        for (ItemStack input : inputs) {
            if (input != null && input.getAmount() > 0) {
                root.uncontrolAccessInput(origin);
                root.addItemStack0(origin, input);
                if (input.getAmount() > 0) {
                    // Network full — drop in-world so items are not silently lost
                    final org.bukkit.Location dropLoc = origin.clone().add(0.5, 1.0, 0.5);
                    dropLoc.getWorld().dropItem(dropLoc, input.clone());
                    input.setAmount(0);
                }
            }
        }
    }

    static boolean hasSufficientPower(long availablePower, int requiredPower) {
        return availablePower >= requiredPower;
    }

    static boolean canFitOutput(@Nullable ItemStack currentOutput, @Nonnull ItemStack craftedOutput) {
        if (currentOutput == null || currentOutput.getType() == Material.AIR) {
            return true;
        }
        return StackUtils.itemsMatch(craftedOutput, currentOutput)
            && currentOutput.getAmount() + craftedOutput.getAmount() <= currentOutput.getMaxStackSize();
    }

    public void releaseCache(@Nonnull BlockMenu blockMenu) {
        if (blockMenu.hasViewer()) {
            INSTANCE_MAP.remove(blockMenu.getLocation());
        }
    }

    public void setCache(@Nonnull BlockMenu blockMenu, @Nonnull BlueprintInstance blueprintInstance) {
        if (!blockMenu.hasViewer()) {
            INSTANCE_MAP.putIfAbsent(blockMenu.getLocation().clone(), blueprintInstance);
        }
    }


    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {

            @Override
            public void init() {
                drawBackground(BACKGROUND_SLOTS);
                drawBackground(BLUEPRINT_BACKGROUND_STACK, BLUEPRINT_BACKGROUND);
                drawBackground(OUTPUT_BACKGROUND_STACK, OUTPUT_BACKGROUND);
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return NetworkSlimefunItems.NETWORK_AUTO_CRAFTER.canUse(player, false)
                    && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (NetworkAutoCrafter.this.withholding && flow == ItemTransportFlow.WITHDRAW) {
                    return new int[]{OUTPUT_SLOT};
                }
                return new int[0];
            }
        };
    }
}
