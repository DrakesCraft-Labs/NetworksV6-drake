package io.github.sefiraat.networks.commands;

import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.Theme;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class NetworksMain implements CommandExecutor {

    private static final Map<Integer, NetworkQuantumStorage> QUANTUM_REPLACEMENT_MAP = new HashMap<>();

    static {
        QUANTUM_REPLACEMENT_MAP.put(4096, NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_1);
        QUANTUM_REPLACEMENT_MAP.put(32768, NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_2);
        QUANTUM_REPLACEMENT_MAP.put(262144, NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_3);
        QUANTUM_REPLACEMENT_MAP.put(2097152, NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_4);
        QUANTUM_REPLACEMENT_MAP.put(16777216, NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_5);
        QUANTUM_REPLACEMENT_MAP.put(134217728, NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_6);
        QUANTUM_REPLACEMENT_MAP.put(1073741824, NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_7);
        QUANTUM_REPLACEMENT_MAP.put(Integer.MAX_VALUE, NetworkSlimefunItems.NETWORK_QUANTUM_STORAGE_8);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (sender instanceof Player player) {

            if (args.length == 0) {
                return false;
            }

            if (args[0].equalsIgnoreCase("fillquantum")) {
                if ((player.isOp() || player.hasPermission("networks.admin")) && args.length >= 2) {
                    try {
                        int number = Integer.parseInt(args[1]);
                        fillQuantum(player, number);
                        return true;
                    } catch (NumberFormatException exception) {
                        return false;
                    }
                }
            }

            if (args[0].equalsIgnoreCase("repair")) {
                repairNetwork(player);
                return true;
            }
        }
        return true;
    }

    /**
     * Restores only the in-memory topology of the controller a staff member is targeting.
     * A non-controller or a vanilla block is reported and left untouched.
     */
    private void repairNetwork(@Nonnull Player player) {
        if (!player.isOp() && !player.hasPermission("networks.admin")) {
            player.sendMessage(Theme.ERROR + "You do not have permission to repair a network.");
            return;
        }

        Block block = player.getTargetBlockExact(8);
        if (block == null) {
            player.sendMessage(Theme.ERROR + "Look directly at a Network Controller within 8 blocks.");
            return;
        }

        SlimefunItem item = com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage.check(block);
        if (item instanceof NetworkController controller) {
            int nodes = controller.rebuildNetwork(block).getNodeCount();
            player.sendMessage(Theme.SUCCESS + "Network topology rebuilt: " + nodes + " node(s).");
        } else if (item instanceof NetworkObject) {
            player.sendMessage(Theme.WARNING + "Look directly at this network's controller, not a child node.");
        } else {
            player.sendMessage(Theme.ERROR + "This block has no valid Networks identity. Nothing was changed.");
        }
    }

    public void fillQuantum(Player player, int amount) {
        final ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            player.sendMessage(Theme.ERROR + "Item in hand must be a Quantum Storage.");
            return;
        }

        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);

        if (!(slimefunItem instanceof NetworkQuantumStorage)) {
            player.sendMessage(Theme.ERROR + "Item in hand must be a Quantum Storage.");
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        final QuantumCache quantumCache = DataTypeMethods.getCustom(
            meta,
            Keys.QUANTUM_STORAGE_INSTANCE,
            PersistentQuantumStorageType.TYPE
        );

        if (quantumCache == null || quantumCache.getItemStack() == null) {
            player.sendMessage(Theme.ERROR + "This card has either not been set to an item yet or is a corrupted Quantum Storage.");
            return;
        }

        quantumCache.setAmount(amount);
        DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, quantumCache);
        quantumCache.updateMetaLore(meta);
        itemStack.setItemMeta(meta);
        player.sendMessage(Theme.SUCCESS + "Item updated");
    }
}
