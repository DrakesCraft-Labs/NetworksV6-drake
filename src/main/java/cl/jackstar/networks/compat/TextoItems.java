package cl.jackstar.networks.compat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Sustituto de los ayudantes de texto de GuizhanLib.
 *
 * NetworksExpansion dependia de GuizhanLibPlugin y se apagaba solo si no estaba. Pero de esa
 * libreria solo usaba cuatro cosas, y ninguna es funcionalidad:
 *
 *   MaterialHelper / ItemStackHelper - nombres de materiales y objetos **en chino**
 *   GuizhanUpdater                   - autoactualizador desde su servicio de compilacion
 *   WikiUtils                        - enlaces a su wiki en chino
 *
 * El autoactualizador es lo mas serio: habria reemplazado nuestro jar por el suyo sin avisar,
 * borrando todos los arreglos de este fork. Eso solo ya justifica quitar la dependencia.
 *
 * Asi que en vez de empaquetar la libreria dentro (que traeria el chino y el actualizador), se
 * sustituye lo poco que se usaba. Los nombres salen ahora de Bukkit, que respeta el idioma del
 * cliente de cada jugador en vez de imponer uno.
 */
public final class TextoItems {

    private TextoItems() {
    }

    /**
     * El nombre visible de un objeto: el personalizado si lo tiene, y si no el del material.
     *
     * GuizhanLib devolvia aqui la traduccion al chino desde sus propias tablas. Bukkit ya expone
     * la clave de traduccion del material, que el cliente resuelve en el idioma del jugador; es
     * mejor resultado y sin tabla que mantener.
     */
    @Nonnull
    public static String nombreVisible(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "";
        }

        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        if (meta != null && meta.hasDisplayName()) {
            String nombre = meta.getDisplayName();
            if (!nombre.isEmpty()) {
                return nombre;
            }
        }

        return nombreMaterial(item.getType());
    }

    /** El nombre de un material, legible. "DIAMOND_PICKAXE" queda en "Diamond Pickaxe". */
    @Nonnull
    public static String nombreMaterial(@Nullable Material material) {
        if (material == null) {
            return "";
        }

        String[] partes = material.name().toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder salida = new StringBuilder(material.name().length());

        for (String parte : partes) {
            if (parte.isEmpty()) {
                continue;
            }
            if (salida.length() > 0) {
                salida.append(' ');
            }
            salida.append(Character.toUpperCase(parte.charAt(0))).append(parte, 1, parte.length());
        }

        return salida.toString();
    }
}
