package cl.jackstar.networks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Vigila que el cache de direcciones se vacie.
 *
 * SELECTED_DIRECTION_MAP es estatico y tenia tres put y ningun remove. La fuga de memoria era lo
 * de menos: getSelectedFace lo consulta antes que al BlockStorage, asi que una entrada vieja se
 * imponia sobre la direccion real de un bloque nuevo, y colocar un nodo donde antes hubo otro lo
 * hacia apuntar a donde el jugador no eligio.
 *
 * Se comprueba sobre el fuente y no con MockBukkit a proposito: montar un nodo direccional
 * completo exige media red en pie, y lo que hay que garantizar aqui es una propiedad estructural
 * -que exista una limpieza enganchada a la rotura del bloque-, no un comportamiento en vivo.
 */
class NetworkDirectionalCacheTest {

    private static final Path FUENTE = Path.of(
            "src", "main", "java", "io", "github", "sefiraat", "networks",
            "slimefun", "network", "NetworkDirectional.java");

    @Test
    void elCacheDeDireccionesSeVacia() throws IOException {
        String fuente = Files.readString(FUENTE);

        assertTrue(fuente.contains("SELECTED_DIRECTION_MAP.remove("),
                "el mapa tiene que poder vaciarse");
        assertTrue(fuente.contains("forgetSelectedFace"),
                "la limpieza va en un metodo con nombre, no suelta");
    }

    @Test
    void laLimpiezaCuelgaDeLaRoturaDelBloque() throws IOException {
        String fuente = Files.readString(FUENTE);

        assertTrue(fuente.contains("BlockBreakHandler"),
                "sin handler de rotura, la entrada sobrevive al bloque");
        int handler = fuente.indexOf("BlockBreakHandler");
        int limpieza = fuente.indexOf("forgetSelectedFace(event.getBlock().getLocation())");
        assertTrue(limpieza > handler,
                "la limpieza tiene que estar dentro del handler de rotura");
    }

    /**
     * AdvancedDirectional hereda de esta clase, asi que las 72 maquinas de NetworksExpansion
     * quedan cubiertas. Si alguien mueve el arreglo a una subclase, esto avisa.
     */
    @Test
    void elArregloViveEnLaClaseBase() throws IOException {
        String fuente = Files.readString(FUENTE);
        assertTrue(fuente.contains("public static void forgetSelectedFace"),
                "tiene que ser estatico y publico para que lo usen las subclases");
        assertFalse(fuente.contains("private static void forgetSelectedFace"));
    }

    @Test
    void sigueHabiendoTantosPutComoAntes() throws IOException {
        String fuente = Files.readString(FUENTE);
        assertEquals(3, fuente.split("SELECTED_DIRECTION_MAP\\.put", -1).length - 1,
                "si aparecen mas escrituras, revisar que todas tengan su limpieza");
    }
}
