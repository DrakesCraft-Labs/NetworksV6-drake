package io.github.sefiraat.networks;

import io.github.sefiraat.networks.network.NetworkNode;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class NetworkStorage {

    private static final Map<Location, NodeDefinition> ALL_NETWORK_OBJECTS = new ConcurrentHashMap<>();

    public static void removeNode(Location location) {
        removeNodeInternal(location, true);
    }

    private static void removeNodeInternal(Location location, boolean isOrigin) {
        final NodeDefinition nodeDefinition = ALL_NETWORK_OBJECTS.get(location);

        if (nodeDefinition == null) {
            return;
        }

        final NetworkNode node = nodeDefinition.getNode();

        if (node != null) {
            final NetworkRoot root = node.getRoot();
            if (root != null) {
                root.unregisterNode(location, nodeDefinition.getType());
            }
            for (NetworkNode childNode : node.getChildrenNodes()) {
                removeNodeInternal(childNode.getNodePosition(), false);
            }
        }

        if (isOrigin) {
            ALL_NETWORK_OBJECTS.remove(location);
        } else {
            nodeDefinition.setNode(null);
        }
    }

    public static Map<Location, NodeDefinition> getAllNetworkObjects() {
        return ALL_NETWORK_OBJECTS;
    }

    /** Clears runtime node references after the final persistence pass. */
    public static void clearRuntimeState() {
        ALL_NETWORK_OBJECTS.clear();
    }
}
