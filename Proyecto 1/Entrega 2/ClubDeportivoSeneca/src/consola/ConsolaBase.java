package consola; // Paquete de consolas.

import java.io.File;          // Archivo/carpeta.
import logica.ClubDeportivo;  // Fachada.

/** Utilidades compartidas por todos los programas de demostración. */
public class ConsolaBase { // Clase ConsolaBase.

    /** Carpeta con los archivos de prueba (fuera de la carpeta src del código). */
    public static final String CARPETA_DATOS = "datos"; // Ruta relativa al proyecto.
    /** Carpeta donde cada demo guarda el estado final (para no alterar los datos de prueba). */
    public static final String CARPETA_RESULTADOS = "datos_resultado"; // Ruta relativa al proyecto.

    /** Interfaz funcional: una acción que puede lanzar excepción (se usa con lambdas). */
    public interface Accion { void ejecutar() throws Exception; } // Un único método abstracto.

    /** Carga el club desde "datos"; si la carpeta no existe, genera los datos de prueba y los guarda. */
    public static ClubDeportivo cargarClub() throws Exception { // Puede fallar.
        if (!new File(CARPETA_DATOS).isDirectory()) { // ¿Existe la carpeta de datos?
            System.out.println("(No se encontró la carpeta '" + CARPETA_DATOS + "': se generan los datos de prueba.)"); // Aviso.
            GeneradorDatosPrueba.crearClub().guardar(CARPETA_DATOS); // Genera y guarda.
        }
        ClubDeportivo club = ClubDeportivo.cargar(CARPETA_DATOS); // Carga desde archivos.
        System.out.println("Datos cargados desde '" + new File(CARPETA_DATOS).getAbsolutePath() + "'."); // Informa la ruta.
        return club; // Devuelve.
    }

    /** Guarda el estado final de una demo en datos_resultado/<nombre>. */
    public static void guardarResultado(ClubDeportivo club, String nombre) throws Exception { // Recibe club y nombre.
        String ruta = CARPETA_RESULTADOS + File.separator + nombre; // Ruta de salida.
        club.guardar(ruta); // Guarda.
        System.out.println("\nEstado final guardado en '" + ruta + "'."); // Informa.
    }

    /** Imprime un título grande. */
    public static void titulo(String t) { // Recibe el texto.
        String linea = "=".repeat(t.length() + 8); // Línea de '=' del ancho del título.
        System.out.println("\n" + linea + "\n    " + t + "\n" + linea); // Imprime.
    }

    /** Imprime el nombre de una sección. */
    public static void seccion(String s) { System.out.println("\n--- " + s + " ---"); } // Imprime.

    /**
     * Ejecuta una acción y muestra si fue aceptada o rechazada por las reglas del negocio.
     * Así se ve en consola que cada regla se está validando.
     */
    public static void intentar(String descripcion, Accion accion) { // Recibe la descripción y la acción (lambda).
        try { // Intenta ejecutar.
            accion.ejecutar(); // Ejecuta.
            System.out.println("  [OK] " + descripcion); // Funcionó.
        } catch (Exception e) { // Si lanzó excepción...
            System.out.println("  [RECHAZADO] " + descripcion + "\n             -> " + e.getMessage()); // ...muestra el motivo.
        }
    }
}
