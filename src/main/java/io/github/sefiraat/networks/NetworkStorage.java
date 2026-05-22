package io.github.sefiraat.networks;

import io.github.sefiraat.networks.network.NetworkNode;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class NetworkStorage {

    private static final Map<Location, NodeDefinition> ALL_NETWORK_OBJECTS = new HashMap<>();

    public static void removeNode(Location location) {
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
                removeNode(childNode.getNodePosition());
            }
        }

        ALL_NETWORK_OBJECTS.remove(location);
    }

    public static Map<Location, NodeDefinition> getAllNetworkObjects() {
        return ALL_NETWORK_OBJECTS;
    }
}
