package io.github.sefiraat.networks.slimefun.network;

import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NetworkNode;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.utils.Theme;
import com.github.drakescraft_labs.slimefun4.api.events.PlayerRightClickEvent;
import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.ItemSetting;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.items.settings.IntRangeSetting;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import com.github.drakescraft_labs.slimefun4.legacy.Objects.handlers.BlockTicker;
import com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkController extends NetworkObject {

    private static final String CRAYON = "crayon";
    private static final Map<Location, NetworkRoot> NETWORKS = new ConcurrentHashMap<>();
    private static final Set<Location> CRAYONS = ConcurrentHashMap.newKeySet();
    private static final Set<Location> DIRTY_NETWORKS = ConcurrentHashMap.newKeySet();

    private final ItemSetting<Integer> maxNodes;
    protected final Set<Location> initializedControllers = ConcurrentHashMap.newKeySet();

    public NetworkController(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.CONTROLLER);

        maxNodes = new IntRangeSetting(this, "max_nodes", 10, 2000, 5000);
        addItemSetting(maxNodes);

        addItemHandler(
            new BlockTicker() {
                @Override
                public boolean isSynchronized() {
                    return true;
                }

                @Override
                public void tick(Block block, SlimefunItem item, Config data) {

                    final Location location = block.getLocation();
                    if (initializedControllers.add(location)) {
                        onFirstTick(block, data);
                    }

                    addToRegistry(block);
                    NetworkRoot networkRoot = NETWORKS.get(location);
                    if (networkRoot == null || DIRTY_NETWORKS.remove(location)) {
                        networkRoot = rebuildNetwork(location, networkRoot);
                    }

                    networkRoot.setDisplayParticles(CRAYONS.contains(location));
                }
            }
        );
    }

    @Override
    protected void prePlace(@Nonnull PlayerRightClickEvent event) {
        Optional<Block> blockOptional = event.getClickedBlock();

        if (blockOptional.isPresent()) {
            Block block = blockOptional.get();
            Block target = block.getRelative(event.getClickedFace());

            for (BlockFace checkFace : CHECK_FACES) {
                Block checkBlock = target.getRelative(checkFace);
                SlimefunItem slimefunItem = BlockStorage.check(checkBlock);

                // For directly adjacent controllers
                if (slimefunItem instanceof NetworkController) {
                    cancelPlace(event);
                    return;
                }

                // Check for node definitions. If there isn't one, we don't care
                NodeDefinition definition = NetworkStorage.getAllNetworkObjects().get(checkBlock.getLocation());
                if (definition == null) {
                    continue;
                }

                // There is a definition, if it has a node, then it's part of an active network.
                if (definition.getNode() != null) {
                    cancelPlace(event);
                    return;
                }
            }
        }
    }

    @Override
    protected void cancelPlace(PlayerRightClickEvent event) {
        event.getPlayer().sendMessage(Theme.ERROR.getColor() + "This network already has a controller!");
        event.cancel();
    }

    private void onFirstTick(@Nonnull Block block, @Nonnull Config data) {
        final String crayon = data.getString(CRAYON);
        if (Boolean.parseBoolean(crayon)) {
            CRAYONS.add(block.getLocation());
        }
    }

    /**
     * Rebuilds the transient topology from persistent Slimefun block data.
     * This intentionally never writes or recreates blocks, inventories or items.
     */
    public @Nonnull NetworkRoot rebuildNetwork(@Nonnull Block block) {
        addToRegistry(block);
        final Location location = block.getLocation();
        final NetworkRoot networkRoot = rebuildNetwork(location, NETWORKS.get(location));
        networkRoot.setDisplayParticles(CRAYONS.contains(location));
        DIRTY_NETWORKS.remove(location);
        return networkRoot;
    }

    @Override
    protected void clearCachedState(@Nonnull Location location) {
        initializedControllers.remove(location);
    }

    public static Map<Location, NetworkRoot> getNetworks() {
        return NETWORKS;
    }

    public static Set<Location> getCrayons() {
        return CRAYONS;
    }

    public static void addCrayon(@Nonnull Location location) {
        BlockStorage.addBlockInfo(location, CRAYON, String.valueOf(true));
        CRAYONS.add(location);
    }

    public static void removeCrayon(@Nonnull Location location) {
        BlockStorage.addBlockInfo(location, CRAYON, null);
        CRAYONS.remove(location);
    }

    public static boolean hasCrayon(@Nonnull Location location) {
        return CRAYONS.contains(location);
    }

    public static void markDirty(@Nonnull Location changedLocation) {
        final NodeDefinition changedDefinition = NetworkStorage.getAllNetworkObjects().get(changedLocation);
        markDefinitionRootDirty(changedDefinition);

        if (NETWORKS.containsKey(changedLocation)) {
            DIRTY_NETWORKS.add(changedLocation);
        }

        for (BlockFace face : CHECK_FACES) {
            final Location adjacent = changedLocation.clone().add(face.getDirection());
            markDefinitionRootDirty(NetworkStorage.getAllNetworkObjects().get(adjacent));
        }
    }

    private static void markDefinitionRootDirty(NodeDefinition definition) {
        if (definition == null || definition.getNode() == null) {
            return;
        }

        final Location controller = definition.getNode().getRoot().getController();
        if (controller != null) {
            DIRTY_NETWORKS.add(controller);
        }
    }

    @Nonnull
    private NetworkRoot rebuildNetwork(@Nonnull Location location, NetworkRoot previousRoot) {
        clearAssignments(previousRoot);

        final NetworkRoot networkRoot = new NetworkRoot(location, NodeType.CONTROLLER, maxNodes.getValue());
        networkRoot.addAllChildren();

        final NodeDefinition definition = NetworkStorage.getAllNetworkObjects().get(location);
        if (definition != null) {
            definition.setNode(networkRoot);
        }
        NETWORKS.put(location, networkRoot);
        return networkRoot;
    }

    private static void clearAssignments(NetworkRoot networkRoot) {
        if (networkRoot == null) {
            return;
        }

        for (Location nodeLocation : networkRoot.getNodeLocations()) {
            final NodeDefinition definition = NetworkStorage.getAllNetworkObjects().get(nodeLocation);
            if (definition != null && definition.getNode() != null
                    && definition.getNode().getRoot() == networkRoot) {
                definition.setNode(null);
            }
        }
    }

    public static void wipeNetwork(@Nonnull Location location) {
        clearAssignments(NETWORKS.remove(location));
        DIRTY_NETWORKS.remove(location);
        CRAYONS.remove(location);
    }

    /** Drops runtime-only controller state so reloads cannot reuse stale graphs. */
    public static void clearRuntimeState() {
        NETWORKS.clear();
        CRAYONS.clear();
        firstTickMap.clear();
    }
}
