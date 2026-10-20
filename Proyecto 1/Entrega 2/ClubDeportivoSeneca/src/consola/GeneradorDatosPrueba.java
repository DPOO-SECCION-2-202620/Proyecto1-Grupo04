package consola; // Paquete de los programas de demostración (consolas).

import java.time.LocalDate;                       // Fecha.
import java.time.LocalDateTime;                   // Fecha y hora.
import java.time.LocalTime;                       // Hora.
import java.util.ArrayList;                       // Lista dinámica.
import java.util.Arrays;                          // Utilidades de arreglos.
import java.util.List;                            // Lista.
import logica.ClubDeportivo;                      // Fachada.
import modelo.academias.*;                        // Academias.
import modelo.competencias.*;                     // Competencias.
import modelo.conjunto.*;                         // Conjunto.
import modelo.entrenamiento.*;                    // Entrenamiento.
import modelo.enums.*;                            // Enums.
import modelo.usuarios.*;                         // Usuarios.
import modelo.ventas.*;                           // Ventas.

/**
 * Crea un club con datos de prueba USANDO LA MISMA LÓGICA del sistema (así los datos respetan las reglas)
 * y los guarda en la carpeta "datos". Ejecute este programa si quiere regenerar los archivos de prueba.
 */
public class GeneradorDatosPrueba { // Clase GeneradorDatosPrueba.

    /** Fecha de referencia fija para que las edades y los resultados sean reproducibles. */
    public static final LocalDate FECHA_REF = LocalDate.of(2026, 10, 1); // 1 de octubre de 2026.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = crearClub();          // Crea el club con datos.
        club.guardar(ConsolaBase.CARPETA_DATOS);   // Lo guarda en "datos".
        System.out.println("Datos de prueba generados en la carpeta '" + ConsolaBase.CARPETA_DATOS + "'."); // Mensaje.
        System.out.println("Usuarios: " + club.getUsuarios().size() + ", equipos: " + club.getEquipos().size()
                + ", competencias: " + club.getCompetencias().size() + ", ventas: " + club.getVentas().size()); // Resumen.
    }

    /** Construye el club de prueba paso a paso. */
    public static ClubDeportivo crearClub() throws Exception { // Puede lanzar excepciones de negocio.
        ClubDeportivo club = new ClubDeportivo(); // Club vacío.

        // ---------------- Usuarios ----------------
        Administrador admin = new Administrador("A001", "Marta Rojas", "admin", "admin123"); // Administrador.
        club.registrarUsuario(admin); // Lo registra.
        String[] nombres = { "Ana Pérez", "Luis Martínez", "Sofía Castro", "Mateo Gómez", "Valentina Ruiz", "Santiago López",
                "Camila Herrera", "Daniel Vargas", "Isabella Moreno", "Samuel Jiménez", "Mariana Torres", "Nicolás Ramírez",
                "Gabriela Díaz", "Tomás Álvarez", "Lucía Romero", "Martín Suárez", "Paula Navarro", "Diego Ortiz" }; // 18 socios mayores.
        for (int i = 0; i < nombres.length; i++) { // Crea los socios S001..S018.
            String id = String.format("S%03d", i + 1); // Id con tres dígitos: S001, S002...
            LocalDate nacimiento = LocalDate.of(2000 + (i % 6), 1 + (i % 12), 10); // Nacidos entre 2000 y 2005 (Mayores).
            club.registrarUsuario(new Socio(id, nombres[i], id.toLowerCase(), "1234", nacimiento)); // login = s001, password = 1234.
        }
        club.registrarUsuario(new Socio("S019", "Emilio Rincón", "s019", "1234", LocalDate.of(2012, 3, 5)));  // Socio Sub-15 (14 años).
        club.registrarUsuario(new Socio("S020", "Sara Quintero", "s020", "1234", LocalDate.of(2012, 8, 20))); // Socia Sub-15.
        Socio perfilCarlos = new Socio("S021", "Carlos Méndez", "carlos.jugador", "1234", LocalDate.of(1990, 5, 15)); // Perfil de jugador del entrenador.
        club.registrarUsuario(perfilCarlos); // Lo registra.

        Entrenador e1 = new Entrenador("E001", "Carlos Méndez", "cmendez", "ent123");   // Entrenador (también juega tenis).
        e1.setPerfilJugador(perfilCarlos); // Asocia su perfil de jugador.
        Entrenador e2 = new Entrenador("E002", "Laura Gómez", "lgomez", "ent123");      // Entrenadora.
        Entrenador e3 = new Entrenador("E003", "Andrés Pardo", "apardo", "ent123");     // Entrenador.
        Entrenador e4 = new Entrenador("E004", "Diana Torres", "dtorres", "ent123");    // Entrenadora.
        Entrenador e5 = new Entrenador("E005", "Felipe Castro", "fcastro", "ent123");   // Entrenador (refuerzo de mediodía).
        Fisioterapeuta f1 = new Fisioterapeuta("F001", "Paula Ríos", "prios", "fis123"); // Fisioterapeuta mañana.
        Fisioterapeuta f2 = new Fisioterapeuta("F002", "Jorge Silva", "jsilva", "fis123"); // Fisioterapeuta tarde.
        for (Usuario u : Arrays.asList(e1, e2, e3, e4, e5, f1, f2)) club.registrarUsuario(u); // Los registra (se crean sus códigos).

        // ---------------- Turnos (cobertura: 2 entrenadores y 1 fisio de 6:00 a 22:00) ----------------
        for (String dia : club.getDiasAtencion()) { // Lunes a sábado.
            e1.agregarTurno(new Turno(dia, LocalTime.of(6, 0), LocalTime.of(14, 0)));  // Mañana.
            e2.agregarTurno(new Turno(dia, LocalTime.of(6, 0), LocalTime.of(14, 0)));  // Mañana.
            e3.agregarTurno(new Turno(dia, LocalTime.of(14, 0), LocalTime.of(22, 0))); // Tarde.
            e4.agregarTurno(new Turno(dia, LocalTime.of(14, 0), LocalTime.of(22, 0))); // Tarde.
            f1.agregarTurno(new Turno(dia, LocalTime.of(6, 0), LocalTime.of(14, 0)));  // Fisio mañana.
            f2.agregarTurno(new Turno(dia, LocalTime.of(14, 0), LocalTime.of(22, 0))); // Fisio tarde.
        }
        e5.agregarTurno(new Turno("LUNES", LocalTime.of(10, 0), LocalTime.of(18, 0)));     // Refuerzo solo los lunes.

        // ---------------- Equipos ----------------
        club.crearEquipo(admin, "Séneca Fútbol A", Deporte.FUTBOL, Categoria.MAYORES, 15);       // Fútbol A.
        club.crearEquipo(admin, "Séneca Fútbol B", Deporte.FUTBOL, Categoria.MAYORES, 15);       // Fútbol B.
        club.crearEquipo(admin, "Séneca Baloncesto", Deporte.BALONCESTO, Categoria.MAYORES, 12); // Baloncesto.
        club.crearEquipo(admin, "Séneca Voleibol", Deporte.VOLEIBOL, Categoria.MAYORES, 12);     // Voleibol.
        club.crearEquipo(admin, "Séneca Fútbol Sub-15", Deporte.FUTBOL, Categoria.SUB15, 18);    // Fútbol Sub-15.
        String[] posFutbol = { "PORTERO", "DEFENSA", "DEFENSA", "MEDIOCAMPISTA", "MEDIOCAMPISTA", "DELANTERO", "DELANTERO", "DEFENSA" }; // Posiciones.
        for (int i = 1; i <= 8; i++) club.inscribirJugadorEnEquipo(socio(club, i), "Séneca Fútbol A", posFutbol[i - 1], i, FECHA_REF);     // S001-S008.
        for (int i = 9; i <= 16; i++) club.inscribirJugadorEnEquipo(socio(club, i), "Séneca Fútbol B", posFutbol[i - 9], i - 8, FECHA_REF); // S009-S016.
        int[] basket = { 1, 3, 9, 11, 17, 18 }; String[] posBasket = { "BASE", "ESCOLTA", "ALERO", "ALA-PIVOT", "PIVOT", "ALERO" }; // Baloncesto.
        for (int i = 0; i < basket.length; i++) club.inscribirJugadorEnEquipo(socio(club, basket[i]), "Séneca Baloncesto", posBasket[i], 4 + i, FECHA_REF); // Inscribe.
        int[] voley = { 2, 4, 10, 12, 13, 17, 18 }; String[] posVoley = { "ARMADOR", "OPUESTO", "CENTRAL", "CENTRAL", "PUNTA", "PUNTA", "LIBERO" }; // Voleibol.
        for (int i = 0; i < voley.length; i++) club.inscribirJugadorEnEquipo(socio(club, voley[i]), "Séneca Voleibol", posVoley[i], 1 + i, FECHA_REF); // Inscribe.
        club.inscribirJugadorEnEquipo(socio(club, 19), "Séneca Fútbol Sub-15", "DELANTERO", 9, FECHA_REF);  // Sub-15.
        club.inscribirJugadorEnEquipo(socio(club, 20), "Séneca Fútbol Sub-15", "PORTERO", 1, FECHA_REF);    // Sub-15.

        // ---------------- Competencia en liga: Torneo de los Cerros (fútbol) ----------------
        club.registrarCompetencia(new CompetenciaLiga("Torneo de los Cerros", "Universidades de Bogotá", "2026", Deporte.FUTBOL, Categoria.MAYORES, 7)); // Liga.
        List<Socio> nominaFutbol = new ArrayList<>(); for (int i = 1; i <= 8; i++) nominaFutbol.add(socio(club, i)); // Nómina S001-S008.
        club.inscribirEquipoEnCompetencia("Torneo de los Cerros", "Séneca Fútbol A", nominaFutbol); // Inscribe Fútbol A.
        PartidoCompetencia c1 = club.programarPartidoCompetencia("Torneo de los Cerros", "Séneca Fútbol A", "U. de los Andes", "Estadio El Campín - Cancha 2", LocalDate.of(2026, 9, 20), "", ""); // Fecha 1.
        c1.registrarEstadisticas(new EstadisticaFutbol(socio(club, 6), 2, 0, 0, false)); // S006: 2 goles.
        c1.registrarEstadisticas(new EstadisticaFutbol(socio(club, 7), 1, 1, 1, false)); // S007: 1 gol, 1 asistencia, 1 amarilla.
        c1.registrarEstadisticas(new EstadisticaFutbol(socio(club, 4), 0, 2, 0, false)); // S004: 2 asistencias.
        e3.registrarResultadoPartido(c1, 3, 1); // Séneca 3-1 Andes.
        PartidoCompetencia c2 = club.programarPartidoCompetencia("Torneo de los Cerros", "Séneca Fútbol A", "U. Javeriana", "Cancha Javeriana", LocalDate.of(2026, 9, 27), "", ""); // Fecha 2.
        c2.registrarEstadisticas(new EstadisticaFutbol(socio(club, 6), 1, 0, 0, false)); // S006: 1 gol.
        c2.registrarEstadisticas(new EstadisticaFutbol(socio(club, 7), 0, 0, 1, false)); // S007: 2ª amarilla acumulada => suspendido próximo partido oficial.
        e3.registrarResultadoPartido(c2, 1, 1); // Empate 1-1.
        club.programarPartidoCompetencia("Torneo de los Cerros", "Séneca Fútbol A", "U. del Rosario", "Cancha Rosario", LocalDate.of(2026, 10, 11), "", ""); // Fecha 3 (pendiente).
        club.registrarResultadoExterno("Torneo de los Cerros", "U. de los Andes", "U. Javeriana", 2, 0, "", "");  // Resultado externo.
        club.registrarResultadoExterno("Torneo de los Cerros", "U. del Rosario", "U. de los Andes", 1, 1, "", ""); // Resultado externo.
        club.registrarResultadoExterno("Torneo de los Cerros", "U. Javeriana", "U. del Rosario", 0, 2, "", "");   // Resultado externo.

        // ---------------- Competencia por fases: Copa ASCUN (baloncesto) ----------------
        CompetenciaFases ascun = new CompetenciaFases("Copa ASCUN", "ASCUN", "2026", Deporte.BALONCESTO, Categoria.MAYORES, 5); // Fases.
        Fase grupos = new Fase("Grupos", TipoFase.GRUPOS, 2); // Fase de grupos: clasifican 2 por grupo.
        grupos.agregarGrupo(new Grupo("A", Arrays.asList("Séneca Baloncesto", "U. Nacional", "U. Externado"))); // Grupo A.
        grupos.agregarGrupo(new Grupo("B", Arrays.asList("U. de los Andes", "U. Javeriana", "U. Sabana")));     // Grupo B.
        ascun.agregarFase(grupos);                                          // Agrega grupos.
        ascun.agregarFase(new Fase("Semifinal", TipoFase.ELIMINACION, 0));   // Semifinal.
        ascun.agregarFase(new Fase("Final", TipoFase.ELIMINACION, 0));       // Final.
        club.registrarCompetencia(ascun); // Registra la competencia.
        List<Socio> nominaBasket = new ArrayList<>(); for (int i : basket) nominaBasket.add(socio(club, i)); // Nómina de baloncesto.
        club.inscribirEquipoEnCompetencia("Copa ASCUN", "Séneca Baloncesto", nominaBasket); // Inscribe.
        PartidoCompetencia b1 = club.programarPartidoCompetencia("Copa ASCUN", "Séneca Baloncesto", "U. Nacional", "Coliseo UN", LocalDate.of(2026, 9, 18), "Grupos", "A"); // Partido.
        b1.registrarEstadisticas(new EstadisticaBaloncesto(socio(club, 17), 24, 10)); // S017: 24 pts, 10 reb.
        b1.registrarEstadisticas(new EstadisticaBaloncesto(socio(club, 1), 18, 3));   // S001: 18 pts.
        e4.registrarResultadoPartido(b1, 70, 65); // Gana Séneca.
        PartidoCompetencia b2 = club.programarPartidoCompetencia("Copa ASCUN", "Séneca Baloncesto", "U. Externado", "Coliseo Séneca", LocalDate.of(2026, 9, 25), "Grupos", "A"); // Partido.
        b2.registrarEstadisticas(new EstadisticaBaloncesto(socio(club, 17), 30, 12)); // S017: 30 pts.
        b2.registrarEstadisticas(new EstadisticaBaloncesto(socio(club, 9), 15, 4));   // S009: 15 pts.
        e4.registrarResultadoPartido(b2, 75, 70); // Gana Séneca.
        club.registrarResultadoExterno("Copa ASCUN", "U. Nacional", "U. Externado", 80, 75, "Grupos", "A");   // Externo grupo A.
        club.registrarResultadoExterno("Copa ASCUN", "U. de los Andes", "U. Javeriana", 70, 60, "Grupos", "B"); // Externo grupo B.
        club.registrarResultadoExterno("Copa ASCUN", "U. Sabana", "U. de los Andes", 70, 66, "Grupos", "B");    // Externo grupo B.
        club.registrarResultadoExterno("Copa ASCUN", "U. Sabana", "U. Javeriana", 75, 72, "Grupos", "B");       // Externo grupo B.

        // ---------------- Partidos internos ----------------
        PartidoInterno p1 = club.programarPartidoInterno(LocalDate.of(2026, 10, 3), "Séneca Fútbol A", "Séneca Fútbol B"); // Partido jugado.
        p1.registrarEstadisticas(new EstadisticaFutbol(socio(club, 1), 2, 0, 0, false));  // S001: 2 goles.
        p1.registrarEstadisticas(new EstadisticaFutbol(socio(club, 5), 0, 1, 2, false));  // S005: 2 amarillas => suspendido.
        p1.registrarEstadisticas(new EstadisticaFutbol(socio(club, 9), 1, 0, 0, false));  // S009: 1 gol.
        p1.registrarEstadisticas(new EstadisticaFutbol(socio(club, 10), 0, 0, 0, true));  // S010: roja => suspendido.
        e3.registrarResultadoPartido(p1, 2, 1); // Fútbol A 2-1 Fútbol B.
        club.programarPartidoInterno(LocalDate.of(2026, 10, 17), "Séneca Fútbol B", "Séneca Fútbol A"); // Partido pendiente.

        // ---------------- Instalaciones ----------------
        club.agregarInstalacion(new CanchaTenis("TEN-1")); club.agregarInstalacion(new CanchaTenis("TEN-2")); club.agregarInstalacion(new CanchaTenis("TEN-3")); // Tenis.
        club.agregarInstalacion(new CanchaPadel("PAD-1")); club.agregarInstalacion(new CanchaPadel("PAD-2")); // Pádel.
        club.agregarInstalacion(new MesaDeTenisMesa("MESA-1", true)); club.agregarInstalacion(new MesaDeTenisMesa("MESA-2", false)); // Mesas.
        for (Disciplina d : Disciplina.values()) club.setMaxInstalacionesSimultaneas(d, 2); // Máximo 2 simultáneas por disciplina.

        // ---------------- Academias ----------------
        club.inscribirEnDisciplina(socio(club, 1), Disciplina.TENIS, Nivel.AVANZADO);       // Ana: avanzado.
        club.inscribirEnDisciplina(socio(club, 2), Disciplina.TENIS, Nivel.INTERMEDIO);     // Luis: intermedio.
        club.inscribirEnDisciplina(socio(club, 3), Disciplina.TENIS, Nivel.PRINCIPIANTE);   // Sofía: principiante.
        club.inscribirEnDisciplina(socio(club, 4), Disciplina.TENIS, Nivel.INTERMEDIO);     // Mateo: intermedio.
        club.inscribirEnDisciplina(perfilCarlos, Disciplina.TENIS, Nivel.AVANZADO);          // Carlos (entrenador): avanzado.
        for (int i = 5; i <= 8; i++) club.inscribirEnDisciplina(socio(club, i), Disciplina.PADEL, Nivel.INTERMEDIO); // Pádel.
        club.inscribirEnDisciplina(socio(club, 9), Disciplina.TENIS_MESA, Nivel.PRINCIPIANTE);  // Tenis de mesa.
        club.inscribirEnDisciplina(socio(club, 10), Disciplina.TENIS_MESA, Nivel.INTERMEDIO);   // Tenis de mesa.
        club.reservar(socio(club, 1), "TEN-1", LocalDateTime.of(2026, 10, 12, 8, 0), 2, Instalacion.SENCILLOS); // Reserva tenis.
        club.reservar(socio(club, 5), "PAD-1", LocalDateTime.of(2026, 10, 12, 9, 0), 4, Instalacion.DOBLES);    // Reserva pádel.
        PartidoPractica pp1 = club.programarPartidoPractica(socio(club, 1), Disciplina.TENIS, Arrays.asList(socio(club, 1), socio(club, 2)), LocalDate.of(2026, 10, 4)); // Ana vs Luis.
        club.registrarResultadoPractica(pp1.getId(), Arrays.asList(
                new EstadisticaTenisPadel(socio(club, 1), 2, 13, 4, 10),
                new EstadisticaTenisPadel(socio(club, 2), 1, 10, 1, 18))); // Gana Ana 2 sets a 1.
        PartidoPractica pp2 = club.programarPartidoPractica(socio(club, 5), Disciplina.PADEL,
                Arrays.asList(socio(club, 5), socio(club, 6), socio(club, 7), socio(club, 8)), LocalDate.of(2026, 10, 5)); // Dobles.
        club.registrarResultadoPractica(pp2.getId(), Arrays.asList(
                new EstadisticaTenisPadel(socio(club, 5), 0, 7, 0, 12), new EstadisticaTenisPadel(socio(club, 6), 0, 7, 1, 9),
                new EstadisticaTenisPadel(socio(club, 7), 2, 12, 2, 6), new EstadisticaTenisPadel(socio(club, 8), 2, 12, 1, 8))); // Gana la pareja B.

        // ---------------- Sesiones de entrenamiento ----------------
        SesionEntrenamiento s1 = club.planearSesion(e2, LocalDateTime.of(2026, 10, 6, 7, 0), 90, "TEN-2",
                Arrays.asList(socio(club, 1), socio(club, 2), socio(club, 3), socio(club, 4))); // Sesión de tenis.
        s1.finalizar(); // Termina.
        e2.registrarAsistenciaYEvaluacion(s1, socio(club, 1), EstadoAsistencia.ASISTIO, 5, "Excelente saque"); // Asistencias.
        e2.registrarAsistenciaYEvaluacion(s1, socio(club, 2), EstadoAsistencia.TARDE, 3, "Llegó 15 min tarde");
        e2.registrarAsistenciaYEvaluacion(s1, socio(club, 3), EstadoAsistencia.ASISTIO, 4, "");
        e2.registrarAsistenciaYEvaluacion(s1, socio(club, 4), EstadoAsistencia.FALTO, 0, "");
        SesionEntrenamiento s2 = club.planearSesionEquipo(e3, LocalDateTime.of(2026, 10, 7, 15, 0), 120, "Séneca Fútbol A"); // Sesión del equipo.
        s2.finalizar(); // Termina.
        for (Socio j : s2.getConvocados()) { // Todos asistieron salvo S008.
            EstadoAsistencia est = j.getId().equals("S008") ? EstadoAsistencia.FALTO : EstadoAsistencia.ASISTIO; // Estado.
            e3.registrarAsistenciaYEvaluacion(s2, j, est, est == EstadoAsistencia.FALTO ? 0 : 4, ""); // Registra.
        }
        club.planearSesionEquipo(e4, LocalDateTime.of(2026, 10, 14, 16, 0), 90, "Séneca Baloncesto"); // Sesión futura.

        // ---------------- Tienda y cafetería ----------------
        String[][] arts = { { "Raqueta Wilson Pro", "Raquetas", "450000", "5", "10" }, { "Balón Fútbol Golty", "Balones", "120000", "10", "20" },
                { "Malla Voleibol", "Mallas", "200000", "0", "3" }, { "Uniforme Séneca", "Uniformes", "90000", "15", "10" },
                { "Tubo Pelotas Tenis", "Pelotas", "35000", "30", "40" } }; // nombre, categoría, precio, tienda, bodega.
        for (String[] a : arts) { // Crea cada artículo.
            Articulo art = new Articulo(a[0], a[1], Double.parseDouble(a[2])); // Crea.
            club.agregarArticulo(art); // Agrega.
            if (!a[3].equals("0")) club.reabastecer(admin, a[0], Articulo.TIENDA, Integer.parseInt(a[3])); // Inventario en tienda.
            club.reabastecer(admin, a[0], Articulo.BODEGA, Integer.parseInt(a[4])); // Inventario en bodega.
        }
        club.agregarProductoCafeteria(new Bebida("Café americano", 4000, TipoBebida.CALIENTE));      // Bebida caliente.
        club.agregarProductoCafeteria(new Bebida("Chocolate caliente", 5000, TipoBebida.CALIENTE));  // Bebida caliente.
        club.agregarProductoCafeteria(new Bebida("Jugo natural", 6000, TipoBebida.FRIA));             // Bebida fría.
        club.agregarProductoCafeteria(new Bebida("Agua", 3000, TipoBebida.FRIA));                     // Bebida fría.
        club.agregarProductoCafeteria(new Snack("Barra de maní", 3500, Arrays.asList("MANI")));        // Snack.
        club.agregarProductoCafeteria(new Snack("Galletas de avena", 3000, Arrays.asList("GLUTEN", "LACTEOS"))); // Snack.
        club.agregarProductoCafeteria(new Snack("Fruta picada", 5000, new ArrayList<>()));            // Snack sin alérgenos.

        VentaTienda v1 = club.iniciarVentaTienda(socio(club, 1), LocalDate.of(2026, 10, 2)); // Ana compra.
        v1.agregarItem(club.buscarArticulo("Balón Fútbol Golty"), 1); // 1 balón.
        v1.agregarItem(club.buscarArticulo("Tubo Pelotas Tenis"), 2); // 2 tubos.
        club.confirmarVenta(v1); // Cierra.
        VentaTienda v2 = club.iniciarVentaTienda(e2, LocalDate.of(2026, 10, 2)); // Laura (empleada) compra con su código.
        v2.agregarItem(club.buscarArticulo("Raqueta Wilson Pro"), 1); // 1 raqueta.
        v2.aplicarDescuento(club.buscarCodigoDeEmpleado(e2)); // 15%.
        club.confirmarVenta(v2); // Cierra.
        VentaCafeteria v3 = club.iniciarVentaCafeteria(socio(club, 3), LocalDate.of(2026, 10, 2)); // Sofía en la cafetería.
        v3.agregarItem(club.buscarProducto("Jugo natural"), 1); // Jugo.
        v3.agregarItem(club.buscarProducto("Galletas de avena"), 2); // Galletas.
        v3.informarAlergenos(); // Se informan alérgenos.
        club.confirmarVenta(v3); // Cierra.
        for (int i = 1; i <= 4; i++) club.pagarMensualidad(socio(club, i), "2026-10", 150000, LocalDate.of(2026, 10, 1)); // Mensualidades.

        // ---------------- Sugerencias ----------------
        club.sugerirProducto(e3, Sugerencia.SNACK, "Brownie", "GLUTEN,LACTEOS", 4500, LocalDate.of(2026, 10, 5));     // Sugerencia de snack.
        club.sugerirProducto(f1, Sugerencia.ARTICULO, "Banda elástica", "Rehabilitación", 30000, LocalDate.of(2026, 10, 6)); // Sugerencia de artículo.
        return club; // Devuelve el club armado.
    }

    /** Atajo: el socio número n (S00n). */
    private static Socio socio(ClubDeportivo club, int n) throws Exception { return club.buscarSocio(String.format("S%03d", n)); } // Busca por id.
}
