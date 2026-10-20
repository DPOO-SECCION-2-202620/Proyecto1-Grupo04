package consola; // Paquete de consolas.

import java.time.LocalDate;            // Fecha.
import java.util.ArrayList;            // Lista dinámica.
import java.util.List;                 // Lista.
import java.util.Map;                  // Mapa.
import logica.ClubDeportivo;           // Fachada.
import modelo.competencias.*;          // Competencias.
import modelo.conjunto.*;              // Partidos y estadísticas.
import modelo.usuarios.Entrenador;     // Entrenador.
import modelo.usuarios.Socio;          // Socio.

/** Demuestra: competencias externas en liga y por fases, nómina, sanciones acumuladas, tablas y clasificaciones. */
public class DemoCompetencias { // Clase DemoCompetencias.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = ConsolaBase.cargarClub(); // Carga datos.
        Entrenador andres = (Entrenador) club.buscarUsuario("E003"); // Entrenador de fútbol.
        Entrenador diana = (Entrenador) club.buscarUsuario("E004");  // Entrenadora de baloncesto.
        ConsolaBase.titulo("DEMO 3: COMPETENCIAS EXTERNAS (LIGA Y FASES)"); // Título.

        // ================= LIGA =================
        CompetenciaLiga cerros = (CompetenciaLiga) club.buscarCompetencia("Torneo de los Cerros"); // Liga.
        ConsolaBase.seccion("Competencia en liga: " + cerros); // Sección.
        for (InscripcionCompetencia i : cerros.getInscripciones()) { // Inscritos.
            System.out.println("  Inscrito: " + i); // Inscripción.
            System.out.println("  Amarillas acumuladas: " + nombres(i.getAmarillasAcumuladas()) + " | Suspendidos: " + lista(new ArrayList<>(i.getSuspendidos()))); // Sanciones.
        }
        ConsolaBase.seccion("Requisitos de inscripción"); // Sección.
        List<Socio> pocos = new ArrayList<>(); for (int i = 9; i <= 12; i++) pocos.add(club.buscarSocio("S0" + (i < 10 ? "0" : "") + i)); // 4 jugadores de Fútbol B.
        ConsolaBase.intentar("Inscribir Fútbol B con nómina de 4 (mínimo 7)", () -> club.inscribirEquipoEnCompetencia("Torneo de los Cerros", "Séneca Fútbol B", pocos)); // Rechazado.
        ConsolaBase.intentar("Inscribir Fútbol Sub-15 (categoría no permitida)", () -> club.inscribirEquipoEnCompetencia("Torneo de los Cerros", "Séneca Fútbol Sub-15", new ArrayList<>())); // Rechazado.
        ConsolaBase.intentar("Inscribir Baloncesto en una competencia de fútbol", () -> club.inscribirEquipoEnCompetencia("Torneo de los Cerros", "Séneca Baloncesto", new ArrayList<>())); // Rechazado.

        ConsolaBase.seccion("Tabla de posiciones actual"); // Sección.
        imprimirTabla(cerros.calcularTablaPosiciones()); // Tabla.

        ConsolaBase.seccion("Fecha 3 vs U. del Rosario: solo juega la nómina y se respetan sanciones"); // Sección.
        PartidoCompetencia rosario = null; // Partido pendiente.
        for (PartidoCompetencia p : cerros.getPartidos()) if (!p.isJugado()) rosario = p; // Lo busca.
        final PartidoCompetencia pr = rosario; // Variable final para usarla en lambdas.
        System.out.println("  " + pr + " | ¿puede jugarse? " + pr.puedeJugarse()); // Estado.
        ConsolaBase.intentar("Alinear a Camila (S007): acumuló 2 amarillas en el torneo", () -> pr.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S007"), 1, 0, 0, false))); // Suspendida.
        ConsolaBase.intentar("Alinear a Isabella (S009): no está en la nómina", () -> pr.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S009"), 1, 0, 0, false))); // No nómina.
        ConsolaBase.intentar("Registrar goles de S006 (x2) y S001 (x1)", () -> { // Válido.
            pr.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S006"), 2, 0, 0, false)); // S006.
            pr.registrarEstadisticas(new EstadisticaFutbol(club.buscarSocio("S001"), 1, 1, 1, false)); // S001.
        });
        ConsolaBase.intentar("Resultado Séneca 3-0 Rosario", () -> andres.registrarResultadoPartido(pr, 3, 0)); // Resultado.
        System.out.println("  Tras el partido, Camila cumplió su sanción. Suspendidos ahora: " + lista(new ArrayList<>(pr.getInscripcion().getSuspendidos()))); // Sanción cumplida.

        ConsolaBase.seccion("Tabla de posiciones actualizada"); // Sección.
        imprimirTabla(cerros.calcularTablaPosiciones()); // Tabla.
        ConsolaBase.seccion("Tabla de goleadores y jugador más destacado"); // Sección.
        for (Map.Entry<Socio, Integer> e : cerros.calcularTablaGoleadores()) System.out.println("  " + e.getKey().getNombre() + ": " + e.getValue() + " goles"); // Goleadores.
        System.out.println("  Jugador más destacado: " + cerros.calcularJugadorMasDestacado().getNombre()); // Destacado.

        // ================= FASES =================
        CompetenciaFases ascun = (CompetenciaFases) club.buscarCompetencia("Copa ASCUN"); // Por fases.
        ConsolaBase.seccion("Competencia por fases: " + ascun); // Sección.
        Fase grupos = ascun.getFaseActual(); // Fase actual.
        for (Grupo g : grupos.getGrupos()) { // Cada grupo.
            System.out.println("  Grupo " + g.getNombre() + ":"); // Nombre.
            List<Enfrentamiento> enfs = new ArrayList<>(); // Resultados de la fase.
            for (Enfrentamiento e : ascun.getEnfrentamientosJugados()) if (e.getFase().equals(grupos.getNombre())) enfs.add(e); // Filtra.
            imprimirTabla(g.calcularTabla(enfs, ascun.getDeporte())); // Tabla del grupo.
        }
        System.out.println("  Clasificados de la fase '" + ascun.getNombreFaseActual() + "': " + ascun.calcularClasificados()); // Clasificados.
        ascun.avanzarFase(); // Avanza a semifinal.
        System.out.println("  Fase actual: " + ascun.getNombreFaseActual() + " con " + ascun.getFaseActual().getParticipantes()); // Semifinal.

        PartidoCompetencia semi = club.programarPartidoCompetencia("Copa ASCUN", "Séneca Baloncesto", "U. de los Andes", "Coliseo El Salitre", LocalDate.of(2026, 10, 9), "Semifinal", ""); // Semifinal del club.
        semi.registrarEstadisticas(new EstadisticaBaloncesto(club.buscarSocio("S017"), 28, 9)); // S017.
        diana.registrarResultadoPartido(semi, 81, 77); // Gana Séneca.
        club.registrarResultadoExterno("Copa ASCUN", "U. Sabana", "U. Nacional", 69, 64, "Semifinal", ""); // Otra semifinal.
        System.out.println("  Semifinales: " + semi + " | U. Sabana 69-64 U. Nacional"); // Resultados.
        ascun.avanzarFase(); // Avanza a la final.
        System.out.println("  Fase actual: " + ascun.getNombreFaseActual() + " con " + ascun.getFaseActual().getParticipantes()); // Final.
        PartidoCompetencia fin = club.programarPartidoCompetencia("Copa ASCUN", "Séneca Baloncesto", "U. Sabana", "Coliseo El Salitre", LocalDate.of(2026, 10, 16), "Final", ""); // Final.
        fin.registrarEstadisticas(new EstadisticaBaloncesto(club.buscarSocio("S001"), 22, 5)); // S001.
        diana.registrarResultadoPartido(fin, 90, 85); // Gana Séneca.
        ascun.avanzarFase(); // Cierra la final.
        System.out.println("  CAMPEÓN de la " + ascun.getNombre() + ": " + ascun.getCampeon()); // Campeón.
        ConsolaBase.intentar("Avanzar de fase otra vez", ascun::avanzarFase); // Ya terminó (referencia a método).
        System.out.println("  Máximos anotadores de la copa:"); // Anotadores.
        for (Map.Entry<Socio, Integer> e : ascun.calcularTablaGoleadores()) System.out.println("    " + e.getKey().getNombre() + ": " + e.getValue() + " puntos"); // Lista.

        ConsolaBase.guardarResultado(club, "demo3_competencias"); // Guarda.
    }

    /** Imprime una tabla de posiciones numerada. */
    static void imprimirTabla(List<PosicionTabla> tabla) { // Recibe la tabla.
        for (int i = 0; i < tabla.size(); i++) System.out.println("    " + (i + 1) + ". " + tabla.get(i)); // Una fila por línea.
    }

    /** Nombres de una lista de socios. */
    static String lista(List<Socio> socios) { List<String> n = new ArrayList<>(); for (Socio s : socios) n.add(s.getNombre()); return n.toString(); } // Arma texto.

    /** Mapa socio->número como texto. */
    static String nombres(Map<Socio, Integer> m) { List<String> n = new ArrayList<>(); for (Map.Entry<Socio, Integer> e : m.entrySet()) n.add(e.getKey().getNombre() + "=" + e.getValue()); return n.toString(); } // Arma texto.
}
