package consola; // Paquete de consolas.

import java.time.LocalDate;            // Fecha.
import logica.ClubDeportivo;           // Fachada.
import modelo.enums.Categoria;         // Categoría.
import modelo.enums.Deporte;           // Deporte.
import modelo.usuarios.Administrador;  // Administrador.
import modelo.ventas.Articulo;         // Artículo.
import modelo.ventas.Sugerencia;       // Sugerencia.

/** Demuestra las funciones del administrador: inventario, historial, equipos, sugerencias y reportes. */
public class DemoAdministrador { // Clase DemoAdministrador.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = ConsolaBase.cargarClub(); // Carga datos.
        Administrador admin = (Administrador) club.buscarUsuario("A001"); // Administrador.
        LocalDate ref = LocalDate.of(2026, 10, 2); // Fecha de referencia para reportes.
        ConsolaBase.titulo("DEMO 7: ADMINISTRADOR"); // Título.

        ConsolaBase.seccion("Estado del inventario"); // Sección.
        System.out.print(club.generarEstadoInventario()); // Inventario.

        ConsolaBase.seccion("Mover artículos y reabastecer"); // Sección.
        ConsolaBase.intentar("Mover 2 mallas de BODEGA a TIENDA", () -> club.moverArticulo(admin, "Malla Voleibol", Articulo.BODEGA, Articulo.TIENDA, 2)); // Válido.
        ConsolaBase.intentar("Mover 50 balones de BODEGA a TIENDA (solo hay 20)", () -> club.moverArticulo(admin, "Balón Fútbol Golty", Articulo.BODEGA, Articulo.TIENDA, 50)); // Rechazado.
        ConsolaBase.intentar("Mover uniformes a una nueva ubicación 'VITRINA'", () -> club.moverArticulo(admin, "Uniforme Séneca", Articulo.BODEGA, "VITRINA", 3)); // Válido.
        ConsolaBase.intentar("Reabastecer 10 raquetas en TIENDA", () -> club.reabastecer(admin, "Raqueta Wilson Pro", Articulo.TIENDA, 10)); // Válido.
        for (String n : new String[] { "Malla Voleibol", "Uniforme Séneca", "Raqueta Wilson Pro" }) System.out.println("     " + club.buscarArticulo(n)); // Resultado.

        ConsolaBase.seccion("Sugerencias de empleados"); // Sección.
        for (Sugerencia s : club.getSugerencias()) System.out.println("  " + s); // Sugerencias.
        ConsolaBase.intentar("Aprobar sugerencia #1 (snack Brownie)", () -> club.aprobarSugerencia(admin, 1)); // Válido.
        ConsolaBase.intentar("Rechazar sugerencia #2 (banda elástica)", () -> club.rechazarSugerencia(admin, 2)); // Válido.
        ConsolaBase.intentar("Aprobar de nuevo la sugerencia #1", () -> club.aprobarSugerencia(admin, 1)); // Rechazado.
        System.out.println("     Nuevo producto en cafetería: " + club.buscarProducto("Brownie")); // Nuevo producto.
        club.sugerirProducto((modelo.usuarios.Empleado) club.buscarUsuario("E004"), Sugerencia.ARTICULO, "Rodillera", "Protección", 60000, ref); // Nueva sugerencia.
        ConsolaBase.intentar("Aprobar sugerencia #3 (artículo Rodillera)", () -> club.aprobarSugerencia(admin, 3)); // Válido.
        ConsolaBase.intentar("Reabastecer 8 rodilleras", () -> club.reabastecer(admin, "Rodillera", Articulo.TIENDA, 8)); // Válido.

        ConsolaBase.seccion("Crear y dar de baja equipos y jugadores"); // Sección.
        ConsolaBase.intentar("Crear 'Séneca Baloncesto Sub-17' (cupo 12)", () -> club.crearEquipo(admin, "Séneca Baloncesto Sub-17", Deporte.BALONCESTO, Categoria.SUB17, 12)); // Válido.
        ConsolaBase.intentar("Crear equipo de fútbol con cupo 5 (mínimo para jugar: 7)", () -> club.crearEquipo(admin, "Séneca Fútbol C", Deporte.FUTBOL, Categoria.MAYORES, 5)); // Rechazado.
        ConsolaBase.intentar("Crear un equipo con nombre repetido", () -> club.crearEquipo(admin, "Séneca Voleibol", Deporte.VOLEIBOL, Categoria.MAYORES, 12)); // Rechazado.
        ConsolaBase.intentar("Dar de baja el equipo 'Séneca Baloncesto Sub-17'", () -> club.darDeBajaEquipo(admin, "Séneca Baloncesto Sub-17")); // Válido.
        ConsolaBase.intentar("Dar de baja 'Séneca Fútbol A' (inscrito en el Torneo de los Cerros)", () -> club.darDeBajaEquipo(admin, "Séneca Fútbol A")); // Rechazado.
        ConsolaBase.intentar("Dar de baja a Diego (S018) de Voleibol", () -> club.darDeBajaJugador(admin, "Séneca Voleibol", club.buscarSocio("S018"))); // Válido.
        ConsolaBase.intentar("Dar de baja a Paula (S017) del club (sale de todos sus equipos)", () -> System.out.println("     Bajas: " + club.darDeBajaJugadorDelClub(admin, club.buscarSocio("S017")))); // Válido.
        System.out.println("     " + club.buscarEquipo("Séneca Voleibol")); // Equipo actualizado.

        ConsolaBase.seccion("Reportes de ventas por rubro y periodo"); // Sección.
        System.out.print(club.generarReporteVentas(admin, "TODOS", "DIARIO", ref)); // Diario.
        System.out.print(club.generarReporteVentas(admin, "TIENDA", "SEMANAL", ref)); // Semanal tienda.
        System.out.print(club.generarReporteVentas(admin, "MENSUALIDAD", "MENSUAL", ref)); // Mensual mensualidades.

        ConsolaBase.seccion("Reportes deportivos"); // Sección.
        System.out.print(club.generarReporteDeportivo(admin, "ASISTENCIA", "MENSUAL", ref)); // Asistencia.
        System.out.print(club.generarReporteDeportivo(admin, "RESULTADOS", "MENSUAL", LocalDate.of(2026, 9, 15))); // Resultados septiembre.
        System.out.print(club.generarReporteDeportivo(admin, "RANKINGS", "MENSUAL", ref)); // Rankings.

        ConsolaBase.seccion("Historial completo"); // Sección.
        System.out.print(club.generarHistorial()); // Historial.

        ConsolaBase.guardarResultado(club, "demo7_administrador"); // Guarda.
    }
}
