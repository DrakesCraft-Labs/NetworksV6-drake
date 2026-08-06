package io.github.sefiraat.networks.slimefun.network;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El cache de direcciones tiene que vaciarse igual que se llena.
 *
 * SELECTED_DIRECTION_MAP es estatico y tenia cinco put sin un solo remove. Dos consecuencias
 * medidas: crecia con cada nodo direccional colocado en la historia del servidor y solo se
 * vaciaba al reiniciar, y como getSelectedFace lo consulta antes que a BlockStorage, un nodo
 * nuevo heredaba la direccion del que hubo antes en esa ubicacion.
 */
class NetworkDirectionalCacheTest {

    private static final Path FUENTE = Paths.get(
            "src/main/java/io/github/sefiraat/networks/slimefun/network/NetworkDirectional.java");

    private static String fuente() throws IOException {
        return Files.readString(FUENTE, StandardCharsets.UTF_8);
    }

    private static int veces(String texto, String patron) {
        Matcher matcher = Pattern.compile(patron).matcher(texto);
        int total = 0;
        while (matcher.find()) total++;
        return total;
    }

    @Test
    void theCacheIsEmptiedAsWellAsFilled() throws IOException {
        String texto = fuente();
        int remove = veces(texto, "SELECTED_DIRECTION_MAP\\.remove");
        assertTrue(remove > 0,
                "SELECTED_DIRECTION_MAP se llena pero nunca se vacia: fuga y direcciones heredadas");
    }

    @Test
    void breakingTheBlockForgetsItsDirection() throws IOException {
        String texto = fuente();
        assertTrue(texto.contains("BlockBreakHandler"),
                "sin BlockBreakHandler la direccion sobrevive al bloque que la eligio");
        assertTrue(texto.contains("forgetSelectedFace"),
                "la limpieza deberia pasar por forgetSelectedFace, no por accesos sueltos al mapa");
    }

    @Test
    void placingANodeClearsWhateverWasThereBefore() throws IOException {
        String texto = fuente();
        int inicio = texto.indexOf("onPlayerPlace");
        assertTrue(inicio > 0, "deberia existir onPlayerPlace");
        String cuerpo = texto.substring(inicio, Math.min(texto.length(), inicio + 900));
        assertTrue(cuerpo.contains("forgetSelectedFace"),
                "colocar reinicia BlockStorage a SELF, pero getSelectedFace lee antes el cache: "
                        + "hay que limpiarlo o el nodo nuevo apunta a donde apuntaba el viejo");
    }
}
