package consola; // Paquete de consolas.

import java.time.LocalDate;                // Fecha.
import logica.ClubDeportivo;               // Fachada.
import modelo.conjunto.*;                  // Equipos y partidos.
import modelo.enums.Categoria;             // Categoría.
import modelo.enums.Deporte;               // Deporte.
import modelo.usuarios.Administrador;      // Administrador.
import modelo.usuarios.Entrenador;         // Entrenador.

/** Demuestra: equipos, inscripción de jugadores con sus reglas, partidos internos, estadísticas y suspensiones. */
public class DemoEquiposYPartidos { // Clase DemoEquiposYPartidos.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = ConsolaBase.cargarClub(); // Carga datos.
        Administrador admin = (Administrador) club.buscarUsuario("A001"); // Administrador.
        Entrenador andres = (Entrenador) club.buscarUsuario("E003");      // Entrenador.
        LocalDate ref = GeneradorDatosPrueba.FECHA_REF;                    // Fecha de referencia para edades.
        ConsolaBase.titulo("DEMO 2: EQUIPOS, JUGADORES Y PARTIDOS INTERNOS"); // Título.

        ConsolaBase.seccion("Equipos del club"); // Sección.
        for (Equipo e : club.getEquipos()) { // Cada equipo.
            System.out.println("  " + e + (e.tieneMinimoJugadoresHabilitados() ? "" : "  <-- NO tiene el mínimo de habilitados")); // Resumen.
            for (InscripcionEquipo i : e.getInscripciones()) System.out.println("      " + i); // Jugadores.
        }

        ConsolaBase.seccion("Inscripción de jugadores (reglas)"); // Sección.
        ConsolaBase.intentar("Inscribir a Ana (S001) también en Voleibol (puede jugar varios deportes)",
                () -> club.inscribirJugadorEnEquipo(club.buscarSocio("S001"), "Séneca Voleibol", "PUNTA", 11, ref)); // Válido.
        ConsolaBase.intentar("Inscribir a Ana (S001) en Fútbol B (ya juega fútbol en Fútbol A)",
                () -> club.inscribirJugadorEnEquipo(club.buscarSocio("S001"), "Séneca Fútbol B", "DELANTERO", 20, ref)); // Un equipo por deporte.
        ConsolaBase.intentar("Inscribir a Emilio (14 años) en Baloncesto Mayores",
                () -> club.inscribirJugadorEnEquipo(club.buscarSocio("S019"), "Séneca Baloncesto", "BASE", 20, ref)); // Edad.
        ConsolaBase.intentar("Inscribir a Paula (S017) en Fútbol B con la camiseta #1 (ya usada)",
                () -> club.inscribirJugadorEnEquipo(club.buscarSocio("S017"), "Séneca Fútbol B", "PORTERO", 1, ref)); // Camiseta.
        club.crearEquipo(admin, "Séneca Voleibol Mini", Deporte.VOLEIBOL, Categoria.MAYORES, 6); // Equipo de cupo 6.
        String[] seis = { "S005", "S006", "S007", "S008", "S014", "S015" }; // Seis socios que no juegan voleibol.
        for (int i = 0; i < seis.length; i++) { final String id = seis[i]; final int camiseta = i + 1; // Variables "final" para usarlas en la lambda.
            ConsolaBase.intentar("Inscribir " + id + " en Voleibol Mini", () -> club.inscribirJugadorEnEquipo(club.buscarSocio(id), "Séneca Voleibol Mini", "PUNTA", camiseta, ref)); // Llena el cupo.
        }
        ConsolaBase.intentar("Inscribir a S016 en Voleibol Mini (cupo lleno: 6/6)",
                () -> club.inscribirJugadorEnEquipo(club.buscarSocio("S016"), "Séneca Voleibol Mini", "PUNTA", 16, ref)); // Cupo.

        ConsolaBase.seccion("Partido interno ya jugado y sus suspensiones"); // Sección.
        PartidoInterno jugado = club.getPartidosInternos().get(0); // Primer partido (jugado).
        System.out.println("  " + jugado); // Datos.
        for (EstadisticaPartido e : jugado.getEstadisticas()) System.out.println("     " + e + (e.quedaSuspendido() ? "  => SUSPENDIDO próximo partido" : "")); // Estadísticas.
        Equipo a = club.buscarEquipo("Séneca Fútbol A"), b = club.buscarEquipo("Séneca Fútbol B"); // Equipos.
        System.out.println("  Habilitados Fútbol A: " + a.getJugadoresHabilitados().size() + " | Fútbol B: " + b.getJugadoresHabilitados().size()); // Conteos.

        ConsolaBase.seccion("Siguiente partido interno: los suspendidos no pueden jugar"); // Sección.
        PartidoInterno pendiente = club.getPartidosInternos().get(1); // Partido pendiente.
        System.out.println("  " + pendiente + " | ¿puede jugarse? " + pendiente.puedeJugarse()); // Estado.
        ConsolaBase.intentar("Registrar estadísticas de Valentina (S005, suspendida por 2 amarillas)",
                () -> pendiente.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S005"), 1, 0, 0, false))); // Rechazado.
        ConsolaBase.intentar("Registrar estadísticas de Samuel (S010, suspendido por roja)",
                () -> pendiente.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S010"), 0, 0, 0, false))); // Rechazado.
        ConsolaBase.intentar("Registrar estadística de baloncesto en un partido de fútbol",
                () -> pendiente.registrarEstadisticas(new EstadisticaBaloncesto(club.buscarSocio("S011"), 10, 2))); // Tipo incorrecto.
        ConsolaBase.intentar("Registrar estadísticas válidas (S011 gol, S002 gol, S003 dos amarillas)", () -> { // Válidas.
            pendiente.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S011"), 1, 0, 0, false)); // S011.
            pendiente.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S002"), 1, 0, 0, false)); // S002.
            pendiente.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S003"), 0, 0, 2, false)); // S003 dos amarillas.
        });
        ConsolaBase.intentar("El entrenador registra el resultado 1-1", () -> andres.registrarResultadoPartido(pendiente, 1, 1)); // Resultado.
        System.out.println("  " + pendiente); // Partido cerrado.
        System.out.println("  Después del partido: S005 y S010 cumplieron su sanción; S003 queda suspendido:"); // Explicación.
        System.out.println("     S005 habilitado? " + a.estaHabilitado(club.buscarSocio("S005")) + " | S010 habilitado? " + b.estaHabilitado(club.buscarSocio("S010"))
                + " | S003 habilitado? " + a.estaHabilitado(club.buscarSocio("S003"))); // Estado final.
        ConsolaBase.intentar("Registrar otra vez el resultado", () -> andres.registrarResultadoPartido(pendiente, 2, 0)); // Ya registrado.

        ConsolaBase.seccion("Partido que NO puede jugarse por falta de jugadores habilitados"); // Sección.
        club.crearEquipo(admin, "Séneca Voleibol B", Deporte.VOLEIBOL, Categoria.MAYORES, 12); // Equipo vacío.
        PartidoInterno sinJugadores = club.programarPartidoInterno(LocalDate.of(2026, 10, 20), "Séneca Voleibol", "Séneca Voleibol B"); // Partido.
        System.out.println("  " + sinJugadores + " | ¿puede jugarse? " + sinJugadores.puedeJugarse() + " (Voleibol B tiene 0 de 6)"); // Estado.
        ConsolaBase.intentar("Registrar resultado 3-1", () -> andres.registrarResultadoPartido(sinJugadores, 3, 1)); // Rechazado.
        ConsolaBase.intentar("Programar partido entre equipos de distinto deporte", () -> club.programarPartidoInterno(LocalDate.of(2026, 10, 21), "Séneca Fútbol A", "Séneca Baloncesto")); // Rechazado.
        ConsolaBase.intentar("Programar partido entre categorías distintas", () -> club.programarPartidoInterno(LocalDate.of(2026, 10, 21), "Séneca Fútbol A", "Séneca Fútbol Sub-15")); // Rechazado.
        PartidoInterno voley = club.programarPartidoInterno(LocalDate.of(2026, 10, 22), "Séneca Voleibol", "Séneca Voleibol Mini"); // Partido de voleibol.
        ConsolaBase.intentar("Registrar empate 2-2 en voleibol (no existen empates)", () -> andres.registrarResultadoPartido(voley, 2, 2)); // Rechazado.

        ConsolaBase.guardarResultado(club, "demo2_equipos"); // Guarda.
    }
}
