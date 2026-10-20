package consola; // Paquete de consolas.

import java.time.LocalDate;              // Fecha.
import java.time.LocalDateTime;          // Fecha y hora.
import java.util.Arrays;                 // Utilidades de arreglos.
import logica.ClubDeportivo;             // Fachada.
import modelo.academias.*;               // Academias.
import modelo.enums.Disciplina;          // Disciplina.
import modelo.usuarios.Socio;            // Socio.
import modelo.ventas.VentaCafeteria;     // Venta de cafetería.

/** Demuestra: reservas (capacidad, cupo simultáneo, bebidas calientes), partidos de práctica por niveles y ELO. */
public class DemoAcademiasYReservas { // Clase DemoAcademiasYReservas.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = ConsolaBase.cargarClub(); // Carga datos.
        Socio ana = club.buscarSocio("S001"), luis = club.buscarSocio("S002"), sofia = club.buscarSocio("S003"), mateo = club.buscarSocio("S004"); // Socios.
        Socio carlos = club.buscarSocio("S021"); // Perfil de jugador del entrenador Carlos.
        LocalDateTime sabado10 = LocalDateTime.of(2026, 10, 17, 10, 0); // Sábado 10:00.
        ConsolaBase.titulo("DEMO 4: ACADEMIAS, RESERVAS Y PARTIDOS DE PRÁCTICA"); // Título.

        ConsolaBase.seccion("Instalaciones e inscritos"); // Sección.
        for (Instalacion i : club.getInstalaciones()) System.out.println("  " + i + (i.permiteBebidaCaliente() ? "" : "  [no se permiten bebidas calientes]")); // Instalaciones.
        for (InscripcionDisciplina i : club.getInscripcionesDisciplina()) System.out.println("  " + i); // Inscritos.

        ConsolaBase.seccion("Reservas: capacidad y modalidad"); // Sección.
        ConsolaBase.intentar("Tenis TEN-1 sencillos con 2 jugadores", () -> System.out.println("     " + club.reservar(ana, "TEN-1", sabado10, 2, Instalacion.SENCILLOS))); // Válida.
        ConsolaBase.intentar("Tenis TEN-2 con 3 jugadores", () -> club.reservar(luis, "TEN-2", sabado10, 3, Instalacion.DOBLES)); // Rechazada.
        ConsolaBase.intentar("Pádel PAD-2 sencillos con 2 (pádel siempre es dobles)", () -> club.reservar(luis, "PAD-2", sabado10, 2, Instalacion.SENCILLOS)); // Rechazada.
        ConsolaBase.intentar("TEN-1 a las 10:30 (se cruza con la reserva de las 10:00)", () -> club.reservar(luis, "TEN-1", sabado10.plusMinutes(30), 2, Instalacion.SENCILLOS)); // Ocupada.

        ConsolaBase.seccion("Reservas: máximo de instalaciones simultáneas por disciplina (tenis: 2)"); // Sección.
        ConsolaBase.intentar("TEN-2 dobles a la misma hora", () -> club.reservar(mateo, "TEN-2", sabado10, 4, Instalacion.DOBLES)); // Segunda: válida.
        ConsolaBase.intentar("TEN-3 a la misma hora (3ª cancha de tenis simultánea)", () -> club.reservar(sofia, "TEN-3", sabado10, 2, Instalacion.SENCILLOS)); // Tercera: rechazada.
        ConsolaBase.intentar("TEN-3 una hora después", () -> club.reservar(sofia, "TEN-3", sabado10.plusHours(1), 2, Instalacion.SENCILLOS)); // Válida.
        ConsolaBase.intentar("El administrador reserva MESA-2", () -> club.reservar(club.buscarUsuario("A001"), "MESA-2", sabado10, 2, Instalacion.SENCILLOS)); // Admin: rechazada.

        ConsolaBase.seccion("Bebidas calientes y seguridad"); // Sección.
        Socio valentina = club.buscarSocio("S005"); // Socia de pádel.
        VentaCafeteria cafe = club.iniciarVentaCafeteria(valentina, LocalDate.of(2026, 10, 17)); // Compra un café.
        cafe.agregarItem(club.buscarProducto("Café americano"), 1); // Café caliente.
        club.confirmarVenta(cafe); // Cierra la venta.
        System.out.println("  Valentina compró un café americano; ¿tiene bebida caliente? " + valentina.tieneBebidaCaliente()); // true.
        ConsolaBase.intentar("Valentina reserva PAD-2 con la bebida caliente", () -> club.reservar(valentina, "PAD-2", sabado10, 4, Instalacion.DOBLES)); // Rechazada.
        ConsolaBase.intentar("Valentina entra a MESA-1 (techada)", () -> club.entrarAInstalacion(valentina, "MESA-1")); // Rechazada.
        ConsolaBase.intentar("Valentina entra a MESA-2 (no techada)", () -> club.entrarAInstalacion(valentina, "MESA-2")); // Válida.
        club.salirDeInstalacion(valentina); club.terminarBebidaCaliente(valentina); // Sale y termina el café.
        ConsolaBase.intentar("Valentina (sin bebida) entra a PAD-1", () -> club.entrarAInstalacion(valentina, "PAD-1")); // Válida.
        ConsolaBase.intentar("Venderle un chocolate caliente estando en PAD-1", () -> { // Rechazada.
            VentaCafeteria v = club.iniciarVentaCafeteria(valentina, LocalDate.of(2026, 10, 17)); // Venta.
            v.agregarItem(club.buscarProducto("Chocolate caliente"), 1); // Bebida caliente.
        });
        ConsolaBase.intentar("Venderle un jugo natural estando en PAD-1", () -> { // Válida.
            VentaCafeteria v = club.iniciarVentaCafeteria(valentina, LocalDate.of(2026, 10, 17)); // Venta.
            v.agregarItem(club.buscarProducto("Jugo natural"), 1); // Bebida fría.
            club.confirmarVenta(v); // Cierra.
        });
        club.salirDeInstalacion(valentina); // Sale.

        ConsolaBase.seccion("Partidos de práctica: niveles iguales o adyacentes"); // Sección.
        ConsolaBase.intentar("Sofía (PRINCIPIANTE) vs Ana (AVANZADO)", () -> club.programarPartidoPractica(sofia, Disciplina.TENIS, Arrays.asList(sofia, ana), LocalDate.of(2026, 10, 18))); // Rechazado.
        ConsolaBase.intentar("Sofía (PRINCIPIANTE) vs Luis (INTERMEDIO)", () -> System.out.println("     " + club.programarPartidoPractica(sofia, Disciplina.TENIS, Arrays.asList(sofia, luis), LocalDate.of(2026, 10, 18)))); // Válido.
        ConsolaBase.intentar("Sofía (PRINCIPIANTE) vs Carlos (AVANZADO, pero es ENTRENADOR)", () -> System.out.println("     " + club.programarPartidoPractica(sofia, Disciplina.TENIS, Arrays.asList(sofia, carlos), LocalDate.of(2026, 10, 18)))); // Válido por excepción.
        ConsolaBase.intentar("Sofía vs Isabella (Isabella no está inscrita en tenis)", () -> club.programarPartidoPractica(sofia, Disciplina.TENIS, Arrays.asList(sofia, club.buscarSocio("S009")), LocalDate.of(2026, 10, 18))); // Rechazado.
        ConsolaBase.intentar("El administrador programa un partido", () -> club.programarPartidoPractica(club.buscarUsuario("A001"), Disciplina.TENIS, Arrays.asList(luis, mateo), LocalDate.of(2026, 10, 18))); // Rechazado.

        ConsolaBase.seccion("Resultado y actualización del ranking ELO"); // Sección.
        PartidoPractica p = club.getPartidosPractica().get(club.getPartidosPractica().size() - 2); // Sofía vs Luis.
        System.out.printf("  Antes: Sofía ELO %.1f | Luis ELO %.1f%n", club.buscarInscripcionDisciplina(sofia, Disciplina.TENIS).getRankingElo(), club.buscarInscripcionDisciplina(luis, Disciplina.TENIS).getRankingElo()); // Antes.
        ConsolaBase.intentar("Registrar con estadística de tenis de mesa (tipo incorrecto)", () -> club.registrarResultadoPractica(p.getId(), Arrays.asList(new EstadisticaTenisMesa(sofia, 2, 30), new EstadisticaTenisMesa(luis, 1, 25)))); // Rechazado.
        ConsolaBase.intentar("Sofía gana 2 sets a 1", () -> club.registrarResultadoPractica(p.getId(), Arrays.asList(
                new EstadisticaTenisPadel(sofia, 2, 12, 2, 15), new EstadisticaTenisPadel(luis, 1, 11, 0, 20)))); // Válido.
        for (EstadisticaPractica e : p.getEstadisticas()) System.out.println("     " + e); // Muestra ELO antes/después.
        System.out.println("  Ganadores: " + p.getGanadores()); // Ganadores.

        ConsolaBase.seccion("Partido de dobles en pádel (ya registrado en los datos)"); // Sección.
        PartidoPractica dobles = club.getPartidosPractica().get(1); // Dobles.
        System.out.println("  " + dobles); // Partido.
        for (EstadisticaPractica e : dobles.getEstadisticas()) System.out.println("     " + e); // Estadísticas.

        ConsolaBase.seccion("Ranking actual por disciplina"); // Sección.
        for (InscripcionDisciplina i : club.getInscripcionesDisciplina()) System.out.println("  " + i); // Rankings.

        ConsolaBase.guardarResultado(club, "demo4_academias"); // Guarda.
    }
}
