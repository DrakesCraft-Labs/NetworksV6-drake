package io.github.sefiraat.networks.utils;

import io.github.sefiraat.networks.slimefun.network.NetworkCell;
import io.github.sefiraat.networks.slimefun.network.NetworkVanillaGrabber;
import io.github.sefiraat.networks.slimefun.network.NetworkVanillaPusher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkTransportUtilsTest {

    @Test
    void vanillaBridgeNodesAreValidExternalTransportTargets() {
        assertTrue(NetworkTransportUtils.isExternalInventoryType("NTW_VANILLA_GRABBER", NetworkVanillaGrabber.class));
        assertTrue(NetworkTransportUtils.isExternalInventoryType("NTW_VANILLA_PUSHER", NetworkVanillaPusher.class));
    }

    @Test
    void internalNetworkStorageNodesStayBlockedAsExternalTargets() {
        assertFalse(NetworkTransportUtils.isExternalInventoryType("NTW_CELL", NetworkCell.class));
        assertFalse(NetworkTransportUtils.isExternalInventoryType("NTW_QUANTUM_STORAGE_1", Object.class));
    }

    @Test
    void nonNetworkInventoriesRemainExternalTargets() {
        assertTrue(NetworkTransportUtils.isExternalInventoryType(null, null));
        assertTrue(NetworkTransportUtils.isExternalInventoryType("SOME_OTHER_MACHINE", Object.class));
    }
}
