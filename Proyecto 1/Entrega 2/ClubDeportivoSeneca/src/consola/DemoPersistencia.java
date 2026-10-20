package consola; // Paquete de consolas.

import java.io.File;                  // Archivo/carpeta.
import java.time.LocalDate;           // Fecha.
import logica.ClubDeportivo;          // Fachada.
import modelo.ventas.VentaTienda;     // Venta de tienda.

/** Demuestra que toda la información es persistente: cargar, modificar, guardar, volver a cargar y comparar. */
public class DemoPersistencia { // Clase DemoPersistencia.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = ConsolaBase.cargarClub(); // Carga datos.
        ConsolaBase.titulo("DEMO 8: PERSISTENCIA EN ARCHIVOS"); // Título.

        ConsolaBase.seccion("Archivos de la carpeta de datos"); // Sección.
        File[] archivos = new File(ConsolaBase.CARPETA_DATOS).listFiles(); // Lista los archivos.
        if (archivos != null) for (File f : archivos) System.out.printf("  %-32s %6d bytes%n", f.getName(), f.length()); // Nombre y tamaño.

        ConsolaBase.seccion("Modificar el estado"); // Sección.
        VentaTienda v = club.iniciarVentaTienda(club.buscarSocio("S012"), LocalDate.of(2026, 10, 11)); // Nueva venta.
        v.agregarItem(club.buscarArticulo("Uniforme Séneca"), 3); // 3 uniformes.
        club.confirmarVenta(v); // Cierra.
        System.out.println("  Nueva venta: " + v.generarDetalle()); // Detalle.
        int ventas = club.getVentas().size(); // Conteo de ventas.
        int uniformes = club.buscarArticulo("Uniforme Séneca").getCantidadEn("TIENDA"); // Inventario.
        double puntos = club.buscarSocio("S012").getPuntosFidelidad(); // Puntos del socio.
        System.out.printf("  En memoria: %d ventas, %d uniformes en tienda, Nicolás tiene %,.2f puntos.%n", ventas, uniformes, puntos); // Estado.

        String ruta = ConsolaBase.CARPETA_RESULTADOS + File.separator + "demo8_persistencia"; // Carpeta de salida.
        club.guardar(ruta); // Guarda.
        System.out.println("  Guardado en " + new File(ruta).getAbsolutePath()); // Informa.

        ConsolaBase.seccion("Volver a cargar desde los archivos y comparar"); // Sección.
        ClubDeportivo recargado = ClubDeportivo.cargar(ruta); // Carga otra vez.
        int ventas2 = recargado.getVentas().size(); // Conteo recargado.
        int uniformes2 = recargado.buscarArticulo("Uniforme Séneca").getCantidadEn("TIENDA"); // Inventario recargado.
        double puntos2 = recargado.buscarSocio("S012").getPuntosFidelidad(); // Puntos recargados.
        System.out.printf("  Recargado: %d ventas, %d uniformes en tienda, Nicolás tiene %,.2f puntos.%n", ventas2, uniformes2, puntos2); // Estado.
        System.out.println("  ¿Coinciden? " + (ventas == ventas2 && uniformes == uniformes2 && Math.abs(puntos - puntos2) < 0.001)); // Comparación.
        System.out.println("  Usuarios: " + club.getUsuarios().size() + " vs " + recargado.getUsuarios().size()
                + " | Equipos: " + club.getEquipos().size() + " vs " + recargado.getEquipos().size()
                + " | Partidos: " + club.getTodosLosPartidos().size() + " vs " + recargado.getTodosLosPartidos().size()
                + " | Sesiones: " + club.getSesiones().size() + " vs " + recargado.getSesiones().size()); // Más comparaciones.
        System.out.println("  Suspendidos en el Torneo de los Cerros tras recargar: "
                + recargado.buscarCompetencia("Torneo de los Cerros").getInscripciones().get(0).getSuspendidos()); // Estado de sanciones.
        ClubDeportivo.cargar(ConsolaBase.CARPETA_DATOS).guardar(ruta + "_copia"); // Ida y vuelta de los datos originales.
        System.out.println("  También se hizo ida y vuelta de los datos originales en " + ruta + "_copia"); // Informa.
    }
}
