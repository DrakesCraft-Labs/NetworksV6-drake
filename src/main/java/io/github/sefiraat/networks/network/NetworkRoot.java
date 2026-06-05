package io.github.sefiraat.networks.network;

import com.cryptomorin.xseries.particles.XParticle;
import com.github.drakescraft_labs.infinityexpansion.items.storage.StorageCache;
import com.github.drakescraft_labs.infinityexpansion.items.storage.StorageUnit;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.barrel.NetworkStorage;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkAutoCrafter;
import io.github.sefiraat.networks.slimefun.network.NetworkCell;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.slimefun.network.NetworkMonitor;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.NetworkIntegrity;
import io.github.sefiraat.networks.utils.NetworkStackAggregator;
import io.github.sefiraat.networks.utils.NetworkTransportUtils;
import io.github.sefiraat.networks.utils.StackUtils;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import lombok.Getter;
import lombok.Setter;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage;
import com.github.drakescraft_labs.slimefun4.legacy.api.inventory.BlockMenu;
import com.github.drakescraft_labs.slimefun4.legacy.api.item_transport.ItemTransportFlow;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkRoot extends NetworkNode {
    public static final int persistentThreshold = 15;
    public static final int cacheMissThreshold = 15;
    public static final int reduceMs = 8000;
    public static final int transportMissThreshold = 120;
    public static final Map<Location, Map<Location, Integer /* Access times */>> observingAccessHistory =
            new ConcurrentHashMap<>();
    public static final Map<Location, Map<Location, Integer /* Cache miss times */>> persistentAccessHistory =
            new ConcurrentHashMap<>();
    public static final Map<Location, Integer /* Transport miss times */> transportMissInputHistory =
            new ConcurrentHashMap<>();
    public static final Map<Location, Integer /* Transport miss times */> transportMissOutputHistory =
            new ConcurrentHashMap<>();
    public static final Map<Location, Long> controlledAccessInputHistory = new ConcurrentHashMap<>();
    public static final Map<Location, Long> controlledAccessOutputHistory = new ConcurrentHashMap<>();
    @Getter
    private final long CREATED_TIME = System.currentTimeMillis();
    @Getter
    private final Set<Location> nodeLocations = ConcurrentHashMap.newKeySet();
    private final int[] CELL_AVAILABLE_SLOTS = NetworkCell.SLOTS;
    private final int[] GREEDY_BLOCK_AVAILABLE_SLOTS = new int[]{NetworkGreedyBlock.INPUT_SLOT};
    @Getter
    private final Set<Location> bridges = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> monitors = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> importers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> exporters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> grids = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> cells = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> grabbers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> pushers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> purgers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> crafters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerNodes = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerDisplays = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> encoders = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> greedyBlocks = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> cutters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> pasters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> vacuums = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> wirelessTransmitters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> wirelessReceivers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerOutlets = ConcurrentHashMap.newKeySet();
    @Getter
    private final int maxNodes;
    @Getter
    private final boolean recordFlow;
    private int cellsSize = -1;
    @Getter
    private @Nullable Location controller = null;

    @Getter
    private boolean isOverburdened = false;

    @Deprecated
    private @Nullable Set<BarrelIdentity> barrels = null;

    private @Nullable Set<BarrelIdentity> inputAbleBarrels = null;
    private @Nullable Set<BarrelIdentity> outputAbleBarrels = null;
    
    private @Nullable Map<Location, BarrelIdentity> mapInputAbleBarrels = null;
    private @Nullable Map<Location, BarrelIdentity> mapOutputAbleBarrels = null;

    @Setter
    @Getter
    private long rootPower = 0;

    @Setter
    @Getter
    private boolean displayParticles = false;

    public NetworkRoot(@NotNull Location location, @NotNull NodeType type, int maxNodes) {
        this(location, type, maxNodes, false);
    }

    public NetworkRoot(
            @NotNull Location location,
            @NotNull NodeType type,
            int maxNodes,
            boolean recordFlow
        ) {
        super(location, type);
        this.maxNodes = maxNodes;
        this.root = this;
        this.recordFlow = recordFlow;

        registerNode(location, type);
    }

    public static void addPersistentAccessHistory(Location location, Location accessLocation) {
        Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        locations.put(accessLocation, 0);
        persistentAccessHistory.put(location, locations);
    }

    public static void addCacheMiss(Location location, Location accessLocation) {
        Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        int value = locations.getOrDefault(accessLocation, 0) + 1;
        if (value > cacheMissThreshold) {
            removePersistentAccessHistory(location, accessLocation);
            return;
        }
        locations.put(accessLocation, value);
        persistentAccessHistory.put(location, locations);
    }

    public static void minusCacheMiss(Location location, Location accessLocation) {
        Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        int value = Math.max(locations.getOrDefault(accessLocation, 0) - 1, 0);
        locations.put(accessLocation, value);
    }

    public static Map<Location, Integer> getPersistentAccessHistory(Location location) {
        return persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
    }

    public static void removePersistentAccessHistory(Location location) {
        persistentAccessHistory.remove(location);
    }

    public static void removePersistentAccessHistory(Location location, Location accessLocation) {
        Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        locations.remove(accessLocation);
        persistentAccessHistory.put(location, locations);
    }

    public static void addCountObservingAccessHistory(Location location, Location accessLocation) {
        Map<Location, Integer> locations = observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        Integer count = locations.getOrDefault(accessLocation, 0);
        if (count >= persistentThreshold) {
            removeCountObservingAccessHistory(location, accessLocation);
            addPersistentAccessHistory(location, accessLocation);
            return;
        }
        locations.put(accessLocation, count + 1);
        observingAccessHistory.put(location, locations);
    }

    public static Map<Location, Integer> getCountObservingAccessHistory(Location location) {
        return observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
    }

    public static void removeCountObservingAccessHistory(Location location) {
        observingAccessHistory.remove(location);
    }

    public static void removeCountObservingAccessHistory(Location location, Location accessLocation) {
        Map<Location, Integer> locations = observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        locations.remove(accessLocation);
        observingAccessHistory.put(location, locations);
    }

    @Nullable
    public static InfinityBarrel getInfinityBarrel(@NotNull BlockMenu blockMenu, @NotNull StorageUnit storageUnit) {
        return getInfinityBarrel(blockMenu, storageUnit, false);
    }

    @Nullable
    public static InfinityBarrel getInfinityBarrel(
            @NotNull BlockMenu blockMenu, @NotNull StorageUnit storageUnit, boolean includeEmpty) {
        final ItemStack itemStack = blockMenu.getItemInSlot(16);
        final Config data = BlockStorage.getLocationInfo(blockMenu.getLocation());
        // Guard: getLocationInfo puede retornar null si el chunk acaba de cargar o no tiene datos (#NPE-blockdata).
        if (data == null) {
            return null;
        }
        final String storedString = data.getString("stored");

        if (storedString == null) {
            return null;
        }

        final int storedInt;
        try {
            storedInt = Integer.parseInt(storedString);
        } catch (NumberFormatException e) {
            return null;
        }

        if (!includeEmpty && (itemStack == null || itemStack.getType() == Material.AIR)) {
            return null;
        }

        final StorageCache cache = storageUnit.getCache(blockMenu.getLocation());

        if (cache == null) {
            return null;
        }

        final ItemStack clone;
        if (itemStack == null) {
            clone = null;
        } else {
            clone = itemStack.clone();
            clone.setAmount(1);
        }

        return new InfinityBarrel(
                blockMenu.getLocation(), clone, storedInt + (itemStack == null ? 0 : itemStack.getAmount()), cache);
    }

    @Nullable
    public static NetworkStorage getNetworkStorage(@NotNull BlockMenu blockMenu) {
        return getNetworkStorage(blockMenu, false);
    }

    @Nullable
    public static NetworkStorage getNetworkStorage(@NotNull BlockMenu blockMenu, boolean includeEmpty) {
        final QuantumCache cache = NetworkQuantumStorage.getDatabaseCache(blockMenu.getLocation());

        if (cache == null) {
            return null;
        }

        final ItemStack itemStack = cache.getItemStack();
        if ((itemStack == null || itemStack.getType() == Material.AIR) && !includeEmpty) {
            return null;
        }

        final ItemStack output = blockMenu.getItemInSlot(NetworkQuantumStorage.OUTPUT_SLOT);
        int storedInt = cache.getAmount();
        if (output != null && output.getType() != Material.AIR && StackUtils.itemsMatch(cache.getItemStack(), output)) {
            storedInt = storedInt + output.getAmount();
        }

        final ItemStack clone;

        if (itemStack != null) {
            clone = itemStack.clone();
            clone.setAmount(1);
        } else {
            clone = null;
        }

        return new NetworkStorage(blockMenu.getLocation(), clone, storedInt);
    }

    public void registerNode(@NotNull Location location, @NotNull NodeType type) {
        nodeLocations.add(location);
        switch (type) {
            case CONTROLLER -> this.controller = location;
            case BRIDGE -> bridges.add(location);
            case STORAGE_MONITOR -> monitors.add(location);
            case IMPORT -> importers.add(location);
            case EXPORT -> exporters.add(location);
            case GRID -> grids.add(location);
            case CELL -> cells.add(location);
            case GRABBER -> grabbers.add(location);
            case PUSHER -> pushers.add(location);
            case PURGER -> purgers.add(location);
            case CRAFTER -> crafters.add(location);
            case POWER_NODE -> powerNodes.add(location);
            case POWER_DISPLAY -> powerDisplays.add(location);
            case ENCODER -> encoders.add(location);
            case GREEDY_BLOCK -> greedyBlocks.add(location);
            case CUTTER -> cutters.add(location);
            case PASTER -> pasters.add(location);
            case VACUUM -> vacuums.add(location);
            case WIRELESS_TRANSMITTER -> wirelessTransmitters.add(location);
            case WIRELESS_RECEIVER -> wirelessReceivers.add(location);
            case POWER_OUTLET -> powerOutlets.add(location);
        }
    }

    /**
     * Quita la ubicación de los sets internos al desmontar un nodo (#229 / #230).
     */
    public void unregisterNode(@NotNull Location location, @NotNull NodeType type) {
        nodeLocations.remove(location);
        switch (type) {
            case CONTROLLER -> {
                if (location.equals(this.controller)) {
                    this.controller = null;
                }
            }
            case BRIDGE -> bridges.remove(location);
            case STORAGE_MONITOR -> monitors.remove(location);
            case IMPORT -> importers.remove(location);
            case EXPORT -> exporters.remove(location);
            case GRID -> grids.remove(location);
            case CELL -> cells.remove(location);
            case GRABBER -> grabbers.remove(location);
            case PUSHER -> pushers.remove(location);
            case PURGER -> purgers.remove(location);
            case CRAFTER -> crafters.remove(location);
            case POWER_NODE -> powerNodes.remove(location);
            case POWER_DISPLAY -> powerDisplays.remove(location);
            case ENCODER -> encoders.remove(location);
            case GREEDY_BLOCK -> greedyBlocks.remove(location);
            case CUTTER -> cutters.remove(location);
            case PASTER -> pasters.remove(location);
            case VACUUM -> vacuums.remove(location);
            case WIRELESS_TRANSMITTER -> wirelessTransmitters.remove(location);
            case WIRELESS_RECEIVER -> wirelessReceivers.remove(location);
            case POWER_OUTLET -> powerOutlets.remove(location);
            default -> {
            }
        }
        invalidateBarrelCaches();
    }

    /**
     * Expulsa una coordenada del grafo cuando un bloque ajeno ocupa una posición de nodo NTW (#230).
     */
    public void evictStaleLocation(@NotNull Location location) {
        if (NetworkIntegrity.isNetworksMachine(location)) {
            return;
        }
        nodeLocations.remove(location);
        cells.remove(location);
        monitors.remove(location);
        bridges.remove(location);
        importers.remove(location);
        exporters.remove(location);
        grids.remove(location);
        grabbers.remove(location);
        pushers.remove(location);
        purgers.remove(location);
        crafters.remove(location);
        powerNodes.remove(location);
        powerDisplays.remove(location);
        encoders.remove(location);
        greedyBlocks.remove(location);
        cutters.remove(location);
        pasters.remove(location);
        vacuums.remove(location);
        wirelessTransmitters.remove(location);
        wirelessReceivers.remove(location);
        powerOutlets.remove(location);
        if (location.equals(this.controller)) {
            this.controller = null;
        }
        invalidateBarrelCaches();
    }

    private void invalidateBarrelCaches() {
        this.barrels = null;
        this.inputAbleBarrels = null;
        this.outputAbleBarrels = null;
        this.mapInputAbleBarrels = null;
        this.mapOutputAbleBarrels = null;
    }

    public int getNodeCount() {
        return this.nodeLocations.size();
    }

    public void setOverburdened(boolean overburdened) {
        if (overburdened && !isOverburdened) {
            final Location loc = this.nodePosition.clone();
            for (int x = 0; x <= 1; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = 0; z <= 1; z++) {
                        loc.getWorld()
                                .spawnParticle(
                                        XParticle.EXPLOSION.get(),
                                        loc.clone().add(x, y, z),
                                        0);
                    }
                }
            }
        }
        this.isOverburdened = overburdened;
    }

    public @NotNull Map<ItemStack, Integer> getAllNetworkItems() {
        final NetworkStackAggregator aggregator = new NetworkStackAggregator();

        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            final ItemStack sample = barrelIdentity.getItemStack();
            if (sample != null && !sample.getType().isAir()) {
                aggregator.add(sample, (int) Math.min(barrelIdentity.getAmount(), Integer.MAX_VALUE));
            }
        }

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            for (int slot : GREEDY_BLOCK_AVAILABLE_SLOTS) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    aggregator.add(itemStack, itemStack.getAmount());
                }
            }
        }

        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    aggregator.add(itemStack, itemStack.getAmount());
                }
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            for (int slot : CELL_AVAILABLE_SLOTS) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    aggregator.add(itemStack, itemStack.getAmount());
                }
            }
        }

        return aggregator.asMap();
    }

    @Deprecated
    @NotNull
    public Set<BarrelIdentity> getBarrels() {
        if (this.barrels != null) {
            return this.barrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Set<BarrelIdentity> barrelSet = ConcurrentHashMap.newKeySet();

        for (Location cellLocation : this.monitors) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = BlockStorage.check(testLocation);

            if (Networks.getSupportedPluginManager().isInfinityExpansion()
                    && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = BlockStorage.getInventory(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.add(infinityBarrel);
                }
            } else if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = BlockStorage.getInventory(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.add(storage);
                }
            }
        }

        this.barrels = barrelSet;
        return barrelSet;
    }

    @NotNull
    public Set<BlockMenu> getCellMenus() {
        NetworkIntegrity.pruneStaleLocations(this.cells, NetworkCell.class);
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location cellLocation : this.cells) {
            if (!NetworkIntegrity.isExpectedMachine(cellLocation, NetworkCell.class)) {
                continue;
            }
            final BlockMenu menu = BlockStorage.getInventory(cellLocation);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @NotNull
    public Set<BlockMenu> getCrafterOutputs() {
        NetworkIntegrity.pruneStaleLocations(this.crafters, NetworkAutoCrafter.class);
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location location : this.crafters) {
            final BlockMenu menu = BlockStorage.getInventory(location);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @NotNull
    public Set<BlockMenu> getGreedyBlockMenus() {
        NetworkIntegrity.pruneStaleLocations(this.greedyBlocks, NetworkGreedyBlock.class);
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location location : this.greedyBlocks) {
            final BlockMenu menu = BlockStorage.getInventory(location);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    public boolean contains(@NotNull ItemStack itemStack) {
        return contains(new ItemRequest(itemStack, 1));
    }

    public boolean contains(@NotNull ItemRequest request) {

        long found = 0;

        // Barrels
        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            final ItemStack itemStack = barrelIdentity.getItemStack();

            if (itemStack == null || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                continue;
            }

            if (barrelIdentity instanceof InfinityBarrel) {
                if (barrelIdentity.getItemStack().getMaxStackSize() > 1) {
                    found += barrelIdentity.getAmount() - 2;
                }
            } else {
                found += barrelIdentity.getAmount();
            }

            // Escape if found all we need
            if (found >= request.getAmount()) {
                return true;
            }
        }

        // Crafters
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= request.getAmount()) {
                    return true;
                }
            }
        }

        // Greedy Blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            for (int slot : GREEDY_BLOCK_AVAILABLE_SLOTS) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= request.getAmount()) {
                    return true;
                }
            }
        }

        // Cells
        for (BlockMenu blockMenu : getCellMenus()) {
            for (int slot : CELL_AVAILABLE_SLOTS) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= request.getAmount()) {
                    return true;
                }
            }
        }

        return false;
    }

    public int getAmount(@NotNull ItemStack itemStack) {
        long totalAmount = 0;

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            for (int slot : GREEDY_BLOCK_AVAILABLE_SLOTS) {
                ItemStack inputSlotItem = blockMenu.getItemInSlot(slot);
                if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                    totalAmount += inputSlotItem.getAmount();
                }
            }
        }

        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            if (StackUtils.itemsMatch(barrelIdentity.getItemStack(), itemStack)) {
                totalAmount += barrelIdentity.getAmount();
                if (barrelIdentity instanceof InfinityBarrel) {
                    totalAmount -= 2;
                }
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            for (int slot : CELL_AVAILABLE_SLOTS) {
                final ItemStack cellItem = blockMenu.getItemInSlot(slot);
                if (cellItem != null && StackUtils.itemsMatch(cellItem, itemStack)) {
                    totalAmount += cellItem.getAmount();
                }
            }
        }
        if (totalAmount > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) totalAmount;
        }
    }

    public @NotNull HashMap<ItemStack, Long> getAmount(@NotNull Set<ItemStack> itemStacks) {
        HashMap<ItemStack, Long> totalAmounts = new HashMap<>();

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            for (int slot : GREEDY_BLOCK_AVAILABLE_SLOTS) {
                ItemStack inputSlotItem = blockMenu.getItemInSlot(slot);
                if (inputSlotItem != null) {
                    for (ItemStack itemStack : itemStacks) {
                        if (StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                            totalAmounts.put(
                                    itemStack, totalAmounts.getOrDefault(itemStack, 0L) + inputSlotItem.getAmount());
                        }
                    }
                }
            }
        }

        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            for (ItemStack itemStack : itemStacks) {
                if (StackUtils.itemsMatch(barrelIdentity.getItemStack(), itemStack)) {
                    long totalAmount = barrelIdentity.getAmount();
                    if (barrelIdentity instanceof InfinityBarrel) {
                        totalAmount -= 2;
                    }
                    totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + totalAmount);
                }
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            for (int slot : CELL_AVAILABLE_SLOTS) {
                final ItemStack cellItem = blockMenu.getItemInSlot(slot);
                if (cellItem != null) {
                    for (ItemStack itemStack : itemStacks) {
                        if (StackUtils.itemsMatch(cellItem, itemStack)) {
                            totalAmounts.put(
                                    itemStack, totalAmounts.getOrDefault(itemStack, 0L) + cellItem.getAmount());
                        }
                    }
                }
            }
        }

        return totalAmounts;
    }

    @Override
    public long retrieveBlockCharge() {
        return 0;
    }

    public void addRootPower(long power) {
        this.rootPower += power;
    }

    public void removeRootPower(long power) {
        if (power <= 0) {
            return;
        }

        int removed = 0;
        for (Location node : powerNodes) {
            final SlimefunItem item = BlockStorage.check(node);
            if (item instanceof NetworkPowerNode powerNode) {
                final int charge = powerNode.getCharge(node);
                if (charge <= 0) {
                    continue;
                }
                final int toRemove = (int) Math.min(power - removed, charge);
                powerNode.removeCharge(node, toRemove);
                // Restar solo lo que realmente se extrajo de este nodo, no el total pedido (#rootPower-underflow).
                this.rootPower = Math.max(0, this.rootPower - toRemove);
                removed = removed + toRemove;
            }
            if (removed >= power) {
                return;
            }
        }
    }

    @NotNull
    public List<ItemStack> getItemStacks0(@NotNull Location location, @NotNull List<ItemRequest> itemRequests) {
        List<ItemStack> retrievedItems = new ArrayList<>();
        for (ItemRequest request : itemRequests) {
            ItemStack retrieved = getItemStack0(location, request);
            if (retrieved != null) {
                retrievedItems.add(retrieved);
            }
        }
        return retrievedItems;
    }

    @NotNull
    public Set<BarrelIdentity> getInputAbleBarrels() {
        if (this.inputAbleBarrels != null) {
            return this.inputAbleBarrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Set<BarrelIdentity> barrelSet = ConcurrentHashMap.newKeySet();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = BlockStorage.check(testLocation);

            if (Networks.getSupportedPluginManager().isInfinityExpansion()
                    && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = BlockStorage.getInventory(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.add(infinityBarrel);
                }
                continue;
            }
            if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = BlockStorage.getInventory(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.add(storage);
                }
            }
        }

        this.inputAbleBarrels = barrelSet;
        this.mapInputAbleBarrels = new ConcurrentHashMap<>();
        for (BarrelIdentity storage : barrelSet) {
            this.mapInputAbleBarrels.put(storage.getLocation(), storage);
        }

        return barrelSet;
    }

    @NotNull
    public Set<BarrelIdentity> getOutputAbleBarrels() {

        if (this.outputAbleBarrels != null) {
            return this.outputAbleBarrels;
        }

        NetworkIntegrity.pruneStaleLocations(this.monitors, NetworkMonitor.class);

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Set<BarrelIdentity> barrelSet = ConcurrentHashMap.newKeySet();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = BlockStorage.check(testLocation);

            if (Networks.getSupportedPluginManager().isInfinityExpansion()
                    && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = BlockStorage.getInventory(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.add(infinityBarrel);
                }
                continue;
            }
            if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = BlockStorage.getInventory(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.add(storage);
                }
            }
        }

        this.outputAbleBarrels = barrelSet;
        this.mapOutputAbleBarrels = new ConcurrentHashMap<>();
        for (BarrelIdentity storage : barrelSet) {
            this.mapOutputAbleBarrels.put(storage.getLocation(), storage);
        }
        return barrelSet;
    }

    public boolean refreshRootItems() {
        invalidateBarrelCaches();

        getBarrels();
        getInputAbleBarrels();
        getOutputAbleBarrels();
        return true;
    }

    @Nullable
    public BarrelIdentity accessInputAbleBarrel(Location barrelLocation) {
        return getMapInputAbleBarrels().get(barrelLocation);
    }

    @Nullable
    public BarrelIdentity accessOutputAbleBarrel(Location barrelLocation) {
        return getMapOutputAbleBarrels().get(barrelLocation);
    }

    @Nullable
    public ItemStack requestItem(@NotNull Location accessor, @NotNull ItemRequest request) {
        return getItemStack0(accessor, request);
    }

    @Nullable
    public ItemStack requestItem(@NotNull Location accessor, @NotNull ItemStack itemStack) {
        return requestItem(accessor, new ItemRequest(itemStack, itemStack.getAmount()));
    }

    public void tryRecord(@NotNull Location accessor, @NotNull ItemRequest request) {
    }

    public ItemStack getItemStack0(@NotNull Location accessor, @NotNull ItemRequest request) {
        ItemStack stackToReturn = null; // 按F8

        if (request.getAmount() <= 0) {// 按F8
            return null;
        }

        if (!allowAccessOutput(accessor)) {// 按F8
            return null;
        }

        //true here
        Map<Location, Integer> m = getPersistentAccessHistory(accessor);
        if (m != null) {
            // Netex - Cache start
            boolean found = false;
            List<Location> misses = new ArrayList<>();
            // Netex - Cache end
            for (Map.Entry<Location, Integer> entry : m.entrySet()) { // 到这里
                // try cache first
                BarrelIdentity barrelIdentity = accessOutputAbleBarrel(entry.getKey()); // 到这里
                if (barrelIdentity != null) {
                    final ItemStack itemStack = barrelIdentity.getItemStack();

                    if (itemStack == null || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                        // Netex - Cache start
                        misses.add(entry.getKey());
                        // Netex - Cache end
                        continue;
                    }

                    // Netex - Cache start
                    minusCacheMiss(accessor, entry.getKey());
                    found = true; // -> false
                    // Netex - Cache end

                    boolean infinity = barrelIdentity instanceof InfinityBarrel;
                    final ItemStack fetched = barrelIdentity.requestItem(request);
                    if (fetched == null
                            || fetched.getType() == Material.AIR
                            || (infinity && fetched.getAmount() == 1)) {
                        continue;
                    }

                    // Stack is null, so we can fill it here
                    if (stackToReturn == null) {
                        stackToReturn = fetched.clone();
                        stackToReturn.setAmount(0);
                    }

                    final int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();

                    if (request.getAmount() <= preserveAmount) {
                        // Netex - Reduce start
                        uncontrolAccessOutput(accessor);
                        // Netex - Reduce end
                        stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                        fetched.setAmount(fetched.getAmount() - request.getAmount());
                        // Netex - Record start
                        tryRecord(accessor, request);
                        // Netex - Record end
                        return stackToReturn;
                    } else {
                        stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                        request.receiveAmount(preserveAmount);
                        fetched.setAmount(fetched.getAmount() - preserveAmount);
                    }
                }
            }

            // Netex - Cache start
            if (!found) {
                for (Location miss : misses) {
                    minusCacheMiss(accessor, miss);
                }
            } // 到这里
            // Netex - Cache end
        }

        // Barrels first
        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) { // outputable barrels size 是 0,
            // <editor-fold desc="do barrel"> // 你有放存储吗， CELL，只有单元？yea 量子肯定可以
            final ItemStack itemStack = barrelIdentity.getItemStack();

            if (itemStack == null || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                continue;
            }

            // Netex - Cache start
            addCountObservingAccessHistory(accessor, barrelIdentity.getLocation());
            // Netex - Cache end

            boolean infinity = barrelIdentity instanceof InfinityBarrel;
            final ItemStack fetched = barrelIdentity.requestItem(request);
            if (fetched == null || fetched.getType() == Material.AIR || (infinity && fetched.getAmount() == 1)) {
                continue;
            }

            // Stack is null, so we can fill it here
            if (stackToReturn == null) {
                stackToReturn = fetched.clone();
                stackToReturn.setAmount(0);
            }

            final int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();

            if (request.getAmount() <= preserveAmount) {
                // Netex - Reduce start
                uncontrolAccessOutput(accessor);
                // Netex - Reduce end
                stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                fetched.setAmount(fetched.getAmount() - request.getAmount());
                // Netex - Record start
                tryRecord(accessor, request);
                // Netex - Record end
                return stackToReturn;
            } else {
                stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                request.receiveAmount(preserveAmount);
                fetched.setAmount(fetched.getAmount() - preserveAmount);
            }
            // </editor-fold>
        }

        // Cell
        for (BlockMenu blockMenu : getCellMenus()) {
            for (int slot : CELL_AVAILABLE_SLOTS) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                    continue;
                }

                // Mark the Cell as dirty otherwise the changes will not save on shutdown
                blockMenu.markDirty();

                // If the return stack is null, we need to set it up
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(1);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // Netex - Reduce start
                    uncontrolAccessOutput(accessor);
                    // Netex - Reduce end
                    // We can't take more than this stack. Level to request amount, remove items and then return
                    stackToReturn.setAmount(request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    blockMenu.markDirty();
                    // Netex - Record start
                    tryRecord(accessor, request);
                    // Netex - Record end
                    return stackToReturn;
                } else {
                    // We can take more than what is here, consume before trying to take more
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                    blockMenu.markDirty();
                }
            }
        }

        // Crafters
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                    continue;
                }

                // Stack is null, so we can fill it here
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // Netex - Reduce start
                    uncontrolAccessOutput(accessor);
                    // Netex - Reduce end
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    blockMenu.markDirty();
                    // Netex - Record start
                    tryRecord(accessor, request);
                    // Netex - Record end
                    return stackToReturn;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0); //not null
                    blockMenu.markDirty();
                }
            }
        }

        // Greedy Blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            for (int slot : GREEDY_BLOCK_AVAILABLE_SLOTS) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request.getItemStack(), itemStack)) {
                    continue;
                }

                // Mark the storage block as dirty otherwise the changes will not save on shutdown
                blockMenu.markDirty();

                // If the return stack is null, we need to set it up
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // Netex - Reduce start
                    uncontrolAccessOutput(accessor);
                    // Netex - Reduce end
                    // We can't take more than this stack. Level to request amount, remove items and then return
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    // Netex - Record start
                    tryRecord(accessor, request);
                    // Netex - Record end
                    return stackToReturn;
                } else {
                    // We can take more than what is here, consume before trying to take more
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        if (stackToReturn == null || stackToReturn.getAmount() == 0) {
            addTransportOutputMiss(accessor);
            return null;
        }

        // Netex - Reduce start
        uncontrolAccessOutput(accessor);
        // Netex - Reduce end
        // Netex - Record start
        tryRecord(accessor, request);
        // Netex - Record end

        return stackToReturn;
    }

    public void addItem(@NotNull Location accessor, @NotNull ItemStack incoming) {
        addItemStack0(accessor, incoming);
    }

    public void tryRecord(@NotNull Location accessor, @Nullable ItemStack before, int after) {
    }

    public void addItemStack0(@NotNull Location accessor, @NotNull ItemStack incoming) {
        if (!allowAccessInput(accessor)) {
            return;
        }

        ItemStack beforeItemStack = null;
        if (recordFlow) {
            beforeItemStack = incoming.clone();
        }

        int before = incoming.getAmount();

        Map<Location, Integer> m = getPersistentAccessHistory(accessor);
        if (m != null) {
            // Netex - Cache start
            boolean found = false;
            List<Location> misses = new ArrayList<>();
            // Netex - Cache end
            for (Map.Entry<Location, Integer> entry : m.entrySet()) {
                BarrelIdentity barrelIdentity = accessInputAbleBarrel(entry.getKey());
                if (barrelIdentity != null) {
                    // <editor-fold desc="do barrel">
                    if (StackUtils.itemsMatch(barrelIdentity.getItemStack(), incoming)) {
                        // Netex - Cache start
                        minusCacheMiss(accessor, entry.getKey());
                        found = true;
                        // Netex - Cache end

                        barrelIdentity.depositItemStack(incoming);

                        // All distributed, can escape
                        if (incoming.getAmount() == 0) {
                            // Netex - Reduce start
                            uncontrolAccessInput(accessor);
                            // Netex - Reduce end
                            // Netex - Record start
                            tryRecord(accessor, beforeItemStack, 0);
                            // Netex - Record end
                            return;
                        }
                    } else {
                        // Netex - Cache start
                        misses.add(entry.getKey());
                        // Netex - Cache end
                    }
                    // </editor-fold>
                }
            }

            // Netex - Cache start
            if (!found) {
                for (Location miss : misses) {
                    addCacheMiss(accessor, miss);
                }
            }
            // Netex - Cache end
        }

        // Run for matching greedy blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(NetworkGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incoming, template)) {
                continue;
            }

            blockMenu.markDirty();
            NetworkTransportUtils.pushIntoMenu(blockMenu, incoming, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            if (incoming.getAmount() == 0) {
                uncontrolAccessInput(accessor);
                tryRecord(accessor, beforeItemStack, 0);
                return;
            }
            // Plantilla coincide pero el greedy no absorbió todo: seguir con barriles/celdas
        }

        // Run for matching barrels
        for (BarrelIdentity barrelIdentity : getInputAbleBarrels()) {
            // <editor-fold desc="do barrel">
            if (StackUtils.itemsMatch(barrelIdentity.getItemStack(), incoming)) {
                // Netex - Cache start
                addCountObservingAccessHistory(accessor, barrelIdentity.getLocation());
                // Netex - Cache end

                barrelIdentity.depositItemStack(incoming);

                // All distributed, can escape
                if (incoming.getAmount() == 0) {
                    // Netex - Reduce start
                    uncontrolAccessInput(accessor);
                    // Netex - Reduce end
                    // Netex - Record start
                    tryRecord(accessor, beforeItemStack, 0);
                    // Netex - Record end
                    return;
                }
            }
            // </editor-fold>
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            blockMenu.markDirty();
            NetworkTransportUtils.pushIntoMenu(blockMenu, incoming, CELL_AVAILABLE_SLOTS);
            if (incoming.getAmount() == 0) {
                // Netex - Reduce start
                uncontrolAccessInput(accessor);
                // Netex - Reduce end
                // Netex - Record start
                tryRecord(accessor, beforeItemStack, 0);
                // Netex - Record end
                return;
            }
        }

        // Netex - Reduce start
        if (before == incoming.getAmount()) {
            // No item moved, limit the accessor
            addTransportInputMiss(accessor);
        } else {
            uncontrolAccessInput(accessor);
        }
        // Netex - Reduce end
        // Netex - Record start
        tryRecord(accessor, beforeItemStack, incoming.getAmount());
        // Netex - Record end
    }

    public Map<Location, BarrelIdentity> getMapInputAbleBarrels() {
        if (this.mapInputAbleBarrels != null) {
            return this.mapInputAbleBarrels;
        }

        this.mapInputAbleBarrels = new ConcurrentHashMap<>();
        for (BarrelIdentity barrel : getInputAbleBarrels()) {
            this.mapInputAbleBarrels.put(barrel.getLocation(), barrel);
        }
        return this.mapInputAbleBarrels;
    }

    public Map<Location, BarrelIdentity> getMapOutputAbleBarrels() {
        if (this.mapOutputAbleBarrels != null) {
            return this.mapOutputAbleBarrels;
        }

        this.mapOutputAbleBarrels = new ConcurrentHashMap<>();
        for (BarrelIdentity barrel : getOutputAbleBarrels()) {
            this.mapOutputAbleBarrels.put(barrel.getLocation(), barrel);
        }
        return this.mapOutputAbleBarrels;
    }


    public boolean allowAccessInput(@NotNull Location accessor) {
        Long lastTime = controlledAccessInputHistory.get(accessor);
        if (lastTime == null) {
            return true;
        } else {
            return System.currentTimeMillis() - lastTime > reduceMs;
        }
    }

    public boolean allowAccessOutput(@NotNull Location accessor) {
        Long lastTime = controlledAccessOutputHistory.get(accessor);
        if (lastTime == null) {
            return true;
        } else {
            return System.currentTimeMillis() - lastTime > reduceMs;
        }
    }

    public void addTransportInputMiss(@NotNull Location location) {
        transportMissInputHistory.merge(location, 1, (a, b) -> {
            if (a + b > transportMissThreshold) {
                controlAccessInput(location);
                return transportMissThreshold;
            } else {
                return a + b;
            }
        });
    }

    public void addTransportOutputMiss(@NotNull Location location) {
        transportMissOutputHistory.merge(location, 1, (a, b) -> {
            if (a + b > transportMissThreshold) {
                controlAccessOutput(location);
                return transportMissThreshold;
            } else {
                return a + b;
            }
        });
    }

    public void reduceTransportInputMiss(@NotNull Location location) {
        transportMissInputHistory.merge(location, -1, (a, b) -> Math.max(a + b, 0));
    }

    public void reduceTransportOutputMiss(@NotNull Location location) {
        transportMissOutputHistory.merge(location, -1, (a, b) -> Math.max(a + b, 0));
    }

    public void controlAccessInput(@NotNull Location accessor) {
        controlledAccessInputHistory.put(accessor, System.currentTimeMillis());
    }

    public void controlAccessOutput(@NotNull Location accessor) {
        controlledAccessOutputHistory.put(accessor, System.currentTimeMillis());
    }

    public void uncontrolAccessInput(@NotNull Location accessor) {
        controlledAccessInputHistory.remove(accessor);
        reduceTransportInputMiss(accessor);
    }

    public void uncontrolAccessOutput(@NotNull Location accessor) {
        controlledAccessOutputHistory.remove(accessor);
        reduceTransportOutputMiss(accessor);
    }

    public int getCellsSize() {
        if (cellsSize != -1) {
            return cellsSize;
        }

        cellsSize = getCells().size();
        return cellsSize;
    }

    @Warning(
            reason =
                    "This method is deprecated and will be removed in the future. Use addItemStack0(Location, ItemStack) instead.")
    @Deprecated(forRemoval = true)
    public void addItemStack(@NotNull ItemStack incoming) {
        // Run for matching greedy blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(NetworkGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incoming, template)) {
                continue;
            }

            blockMenu.markDirty();
            NetworkTransportUtils.pushIntoMenu(blockMenu, incoming, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }

        // Run for matching barrels
        for (BarrelIdentity barrelIdentity : getInputAbleBarrels()) {
            if (StackUtils.itemsMatch(barrelIdentity, incoming, true)) {
                barrelIdentity.depositItemStack(incoming);

                // All distributed, can escape
                if (incoming.getAmount() == 0) {
                    return;
                }
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            blockMenu.markDirty();
            NetworkTransportUtils.pushIntoMenu(blockMenu, incoming, CELL_AVAILABLE_SLOTS);
            if (incoming.getAmount() == 0) {
                return;
            }
        }
    }
}
