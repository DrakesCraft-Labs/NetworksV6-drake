package cl.jackstar.networks;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Vigila que un nodo entre en el registro al colocarlo.
 *
 * addToRegistry solo se llamaba desde el BlockTicker, y en el primer tick hay un return antes de
 * llegar. El nodo tardaba dos ticks suyos en ser visible para el BFS del controlador, y como el
 * BFS es quien enlaza cada nodo con su red, lo que no alcanza se queda apuntando al root de la
 * pasada anterior --ya muerto--. Las maquinas piden objetos a una red que no existe y se paran
 * en silencio: "pones un nodo y se paraliza la red".
 */
class NodoAlColocarTest {

    private static final Path OBJETO = Path.of(
            "src", "main", "java", "io", "github", "sefiraat", "networks",
            "slimefun", "network", "NetworkObject.java");

    @Test
    void colocarUnNodoLoRegistraEnElActo() throws IOException {
        String fuente = Files.readString(OBJETO);

        int onPlace = fuente.indexOf("protected void onPlace(");
        assertTrue(onPlace > 0, "onPlace tiene que existir");

        int registro = fuente.indexOf("addToRegistry(event.getBlockPlaced())");
        assertTrue(registro > onPlace,
                "onPlace tiene que registrar el nodo; si vuelve a quedarse vacio, la red se "
                + "paraliza al añadir nodos");
    }

    @Test
    void elRegistroSigueOcurriendoTambienEnElTicker() throws IOException {
        // El del ticker cubre los nodos que ya existian antes de este arreglo.
        String fuente = Files.readString(OBJETO);
        assertTrue(fuente.contains("addToRegistry(b)"),
                "no quitar el registro del ticker: cubre los nodos ya colocados");
    }
}
