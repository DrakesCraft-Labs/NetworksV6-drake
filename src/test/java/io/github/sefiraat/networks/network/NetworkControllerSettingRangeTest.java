package io.github.sefiraat.networks.network;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Guards the supported server configuration from silently becoming invalid again. */
class NetworkControllerSettingRangeTest {

    private static final Path SOURCE = Path.of(
        "src/main/java/io/github/sefiraat/networks/slimefun/network/NetworkController.java"
    );

    @Test
    void acceptsTheDocumentedTenThousandNodeControllerLimit() throws IOException {
        String source = Files.readString(SOURCE, StandardCharsets.UTF_8);

        assertTrue(source.contains("new IntRangeSetting(this, \"max_nodes\", 10, 2000, 10_000)"));
    }
}
