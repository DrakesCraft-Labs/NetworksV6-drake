package io.github.sefiraat.networks.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SyncListenerColdLoadTest {

    private static final Path SOURCE = Path.of(
        "src/main/java/io/github/sefiraat/networks/listeners/SyncListener.java"
    );

    @Test
    void coldLoadUsesPersistentBlockStorageIndex() throws IOException {
        String source = Files.readString(SOURCE, StandardCharsets.UTF_8);
        assertTrue(source.contains("BlockStorage.getLocations(chunk)"));
        assertFalse(source.contains("getTickerTask()\n            .getLocations(chunk)"));
    }

    @Test
    void chunkReindexWaitsForSlimefunRestore() throws IOException {
        String source = Files.readString(SOURCE, StandardCharsets.UTF_8);
        assertTrue(source.contains("getScheduler().runTask"));
        assertTrue(source.contains("if (chunk.isLoaded())"));
    }
}
