package logica; // Paquete de la lógica central (fachada del sistema).

import java.time.LocalDate;                       // Fecha.
import java.time.LocalDateTime;                   // Fecha y hora.
import java.time.LocalTime;                       // Hora.
import java.util.ArrayList;                       // Lista dinámica.
import java.util.Arrays;                          // Utilidades de arreglos.
import java.util.EnumMap;                         // Mapa cuyas claves son valores de un enum.
import java.util.LinkedHashMap;                   // Mapa ordenado por inserción.
import java.util.List;                            // Lista.
import java.util.Map;                             // Mapa.
import excepciones.ReglaNegocioException;         // Excepción de negocio.
import modelo.academias.EstadisticaPractica;      // Estadística de práctica.
import modelo.academias.InscripcionDisciplina;    // Inscripción a disciplina.
import modelo.academias.Instalacion;              // Instalación.
import modelo.academias.PartidoPractica;          // Partido de práctica.
import modelo.academias.Reserva;                  // Reserva.
import modelo.competencias.Competencia;           // Competencia.
import modelo.competencias.Enfrentamiento;        // Resultado externo.
import modelo.competencias.InscripcionCompetencia;// Inscripción a competencia.
import modelo.conjunto.Equipo;                    // Equipo.
import modelo.conjunto.Partido;                   // Partido.
import modelo.conjunto.PartidoCompetencia;        // Partido de competencia.
import modelo.conjunto.PartidoInterno;            // Partido interno.
import modelo.entrenamiento.SesionEntrenamiento;  // Sesión.
import modelo.entrenamiento.SolicitudCambioTurno; // Solicitud de cambio de turno.
import modelo.entrenamiento.Turno;                // Turno.
import modelo.enums.Categoria;                    // Categoría.
import modelo.enums.Deporte;                      // Deporte.
import modelo.enums.Disciplina;                   // Disciplina.
import modelo.enums.Nivel;                        // Nivel.
import modelo.reportes.Reporte;                   // Reporte (para calcular periodos).
import modelo.reportes.ReporteDeportivo;          // Reporte deportivo.
import modelo.reportes.ReporteVentas;             // Reporte de ventas.
import modelo.usuarios.Administrador;             // Administrador.
import modelo.usuarios.Empleado;                  // Empleado.
import modelo.usuarios.Entrenador;                // Entrenador.
import modelo.usuarios.Socio;                     // Socio.
import modelo.usuarios.Usuario;                   // Usuario.
import modelo.ventas.Articulo;                    // Artículo.
import modelo.ventas.CodigoDescuento;             // Código de descuento.
import modelo.ventas.ProductoCafeteria;           // Producto de cafetería.
import modelo.ventas.Snack;                       // Snack.
import modelo.ventas.Sugerencia;                  // Sugerencia.
import modelo.ventas.Venta;                       // Venta.
import modelo.ventas.VentaCafeteria;              // Venta de cafetería.
import modelo.ventas.VentaMensualidad;            // Mensualidad.
import modelo.ventas.VentaTienda;                 // Venta de tienda.
import persistencia.PersistenciaClub;             // Persistencia en archivos.

/**
 * Fachada del sistema: guarda todas las colecciones del club y ofrece las operaciones que
 * involucran a varias clases a la vez (reglas que ninguna clase individual puede validar sola,
 * como "un jugador solo puede estar en un equipo por deporte" o "cupo máximo de instalaciones
 * simultáneas por disciplina"). Los programas de consola y la persistencia trabajan contra esta clase.
 */
public class ClubDeportivo { // Clase central del sistema.

    // ---------------- Configuración del club ----------------
    private List<String> diasAtencion;                        // Días en que el club atiende.
    private LocalTime horaApertura;                           // Hora de apertura.
    private LocalTime horaCierre;                             // Hora de cierre.
    private Map<Disciplina, Integer> maxInstalacionesSimultaneas; // Máximo de instalaciones en uso a la vez por disciplina.
    private Map<String, Integer> contadores;                  // Contadores para generar ids ("PARTIDO" -> 5, ...).

    // ---------------- Información del club ----------------
    private List<Usuario> usuarios;                           // Todos los usuarios.
    private List<Equipo> equipos;                             // Equipos de conjunto.
    private List<PartidoInterno> partidosInternos;            // Partidos internos.
    private List<Competencia> competencias;                   // Competencias externas.
    private List<Instalacion> instalaciones;                  // Instalaciones de las academias.
    private List<InscripcionDisciplina> inscripcionesDisciplina; // Inscripciones a academias.
    private List<PartidoPractica> partidosPractica;           // Partidos de práctica.
    private List<SesionEntrenamiento> sesiones;               // Sesiones de entrenamiento.
    private List<SolicitudCambioTurno> solicitudesTurno;      // Solicitudes de cambio de turno.
    private List<Articulo> articulos;                         // Artículos de la tienda.
    private List<ProductoCafeteria> productosCafeteria;       // Productos de la cafetería.
    private List<CodigoDescuento> codigosDescuento;           // Códigos de descuento de empleados.
    private List<Venta> ventas;                               // Ventas cerradas.
    private List<Sugerencia> sugerencias;                     // Sugerencias de productos.

    public ClubDeportivo() { // Constructor: club vacío con configuración por defecto.
        diasAtencion = new ArrayList<>(Arrays.asList("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO")); // Lunes a sábado.
        horaApertura = LocalTime.of(6, 0);  // 6:00 a.m.
        horaCierre = LocalTime.of(22, 0);   // 10:00 p.m.
        maxInstalacionesSimultaneas = new EnumMap<>(Disciplina.class); // Mapa con claves de Disciplina.
        for (Disciplina d : Disciplina.values()) maxInstalacionesSimultaneas.put(d, 2); // Por defecto 2 por disciplina.
        contadores = new LinkedHashMap<>(); // Contadores vacíos.
        usuarios = new ArrayList<>(); equipos = new ArrayList<>(); partidosInternos = new ArrayList<>(); // Listas vacías.
        competencias = new ArrayList<>(); instalaciones = new ArrayList<>(); inscripcionesDisciplina = new ArrayList<>(); // Listas vacías.
        partidosPractica = new ArrayList<>(); sesiones = new ArrayList<>(); solicitudesTurno = new ArrayList<>(); // Listas vacías.
        articulos = new ArrayList<>(); productosCafeteria = new ArrayList<>(); codigosDescuento = new ArrayList<>(); // Listas vacías.
        ventas = new ArrayList<>(); sugerencias = new ArrayList<>(); // Listas vacías.
    }

    /** Muestra cuál sería el siguiente id SIN consumirlo (se usa cuando la creación puede fallar). */
    public int proximoId(String tipo) { return contadores.getOrDefault(tipo, 0) + 1; } // Último valor + 1.

    /** Genera el siguiente id para un tipo de objeto (1, 2, 3...). */
    public int siguienteId(String tipo) { // Recibe la clave del contador.
        int valor = contadores.getOrDefault(tipo, 0) + 1; // Último valor + 1.
        contadores.put(tipo, valor); // Guarda el nuevo valor.
        return valor; // Lo devuelve.
    }

    // =====================================================================
    //                              USUARIOS
    // =====================================================================

    /** Registra un usuario; si es empleado le crea automáticamente su código de descuento. */
    public void registrarUsuario(Usuario u) throws ReglaNegocioException { // Recibe el usuario.
        for (Usuario existente : usuarios) { // Valida unicidad.
            if (existente.getId().equals(u.getId())) throw new ReglaNegocioException("Ya existe un usuario con id " + u.getId() + "."); // Id repetido.
            if (existente.getLogin().equals(u.getLogin())) throw new ReglaNegocioException("El login " + u.getLogin() + " ya está en uso."); // Login repetido.
        }
        usuarios.add(u); // Lo agrega.
        if (u instanceof Empleado && buscarCodigoDeEmpleado((Empleado) u) == null) { // Los empleados siempre tienen código.
            codigosDescuento.add(new CodigoDescuento("EMP-" + u.getId(), (Empleado) u)); // Código "EMP-<id>".
        }
    }

    /** Autentica con login y password; lanza excepción si no coinciden. */
    public Usuario autenticar(String login, String password) throws ReglaNegocioException { // Recibe credenciales.
        for (Usuario u : usuarios) if (u.autenticar(login, password)) return u; // Pregunta a cada usuario.
        throw new ReglaNegocioException("Login o password incorrectos."); // Nadie coincidió.
    }

    /** Busca un usuario por id (null si no existe). */
    public Usuario buscarUsuario(String id) { // Recibe el id.
        for (Usuario u : usuarios) if (u.getId().equals(id)) return u; // Busca.
        return null; // No existe.
    }

    /** Busca un socio por id. */
    public Socio buscarSocio(String id) throws ReglaNegocioException { // Recibe el id.
        Usuario u = buscarUsuario(id); // Busca el usuario.
        if (!(u instanceof Socio)) throw new ReglaNegocioException("No existe el socio " + id + "."); // Debe ser Socio.
        return (Socio) u; // Cast seguro.
    }

    /** Lista de empleados (entrenadores y fisioterapeutas). */
    public List<Empleado> getEmpleados() { // Sin parámetros.
        List<Empleado> lista = new ArrayList<>(); // Resultado.
        for (Usuario u : usuarios) if (u instanceof Empleado) lista.add((Empleado) u); // Filtra.
        return lista; // Devuelve.
    }

    /** ¿El socio es también entrenador? (para la excepción de niveles en partidos de práctica) */
    public boolean esTambienEntrenador(Socio s) { // Recibe el socio.
        for (Usuario u : usuarios) { // Recorre usuarios.
            if (u instanceof Entrenador && ((Entrenador) u).getPerfilJugador() == s) return true; // Es el perfil de un entrenador.
        }
        return false; // No es entrenador.
    }

    /** Código de descuento de un empleado. */
    public CodigoDescuento buscarCodigoDeEmpleado(Empleado e) { // Recibe el empleado.
        for (CodigoDescuento c : codigosDescuento) if (c.getDueno() == e) return c; // Busca.
        return null; // No tiene.
    }

    /** Busca un código por su texto (para cuando un socio lo digita). */
    public CodigoDescuento buscarCodigo(String codigo) throws ReglaNegocioException { // Recibe el texto.
        for (CodigoDescuento c : codigosDescuento) if (c.getCodigo().equalsIgnoreCase(codigo)) return c; // Busca.
        throw new ReglaNegocioException("El código " + codigo + " no existe."); // No existe.
    }

    // =====================================================================
    //                        EQUIPOS Y PARTIDOS INTERNOS
    // =====================================================================

    /** El administrador crea un equipo (nombre único). */
    public Equipo crearEquipo(Administrador admin, String nombre, Deporte deporte, Categoria categoria, int cupo) throws ReglaNegocioException {
        if (buscarEquipoONull(nombre) != null) throw new ReglaNegocioException("Ya existe un equipo llamado " + nombre + "."); // Nombre único.
        Equipo e = admin.crearEquipo(nombre, deporte, categoria, cupo); // Lo crea el administrador.
        equipos.add(e); // Lo registra.
        return e;       // Lo devuelve.
    }

    /** El administrador da de baja (elimina) un equipo que no esté inscrito en competencias. */
    public void darDeBajaEquipo(Administrador admin, String nombre) throws ReglaNegocioException { // Recibe admin y nombre.
        Equipo e = buscarEquipo(nombre); // Busca el equipo.
        for (Competencia c : competencias) { // No se puede si está en alguna competencia.
            if (c.buscarInscripcion(e) != null) throw new ReglaNegocioException(nombre + " está inscrito en " + c.getNombre() + " y no se puede dar de baja."); // Validación.
        }
        equipos.remove(e); // Lo elimina.
    }

    /** Busca un equipo por nombre (excepción si no existe). */
    public Equipo buscarEquipo(String nombre) throws ReglaNegocioException { // Recibe el nombre.
        Equipo e = buscarEquipoONull(nombre); // Busca.
        if (e == null) throw new ReglaNegocioException("No existe el equipo " + nombre + "."); // No existe.
        return e; // Lo devuelve.
    }

    /** Busca un equipo por nombre (null si no existe). */
    public Equipo buscarEquipoONull(String nombre) { // Recibe el nombre.
        for (Equipo e : equipos) if (e.getNombre().equalsIgnoreCase(nombre)) return e; // Busca ignorando mayúsculas.
        return null; // No existe.
    }

    /**
     * Inscribe un socio en un equipo. Regla que solo el club puede validar:
     * un jugador puede pertenecer a UN único equipo por deporte.
     */
    
    public void inscribirJugadorEnEquipo(Socio socio, String nombreEquipo,
            String posicion, int camiseta, LocalDate fechaReferencia)
            throws ReglaNegocioException {

        // Busca el equipo
        Equipo destino = buscarEquipo(nombreEquipo);

        // Revisa que no esté inscrito en otro equipo del mismo deporte
        for (Equipo e : equipos) {
            if (e != destino && e.getDeporte() == destino.getDeporte()
                    && e.buscarInscripcionActiva(socio) != null) {

                throw new ReglaNegocioException(
                        socio.getNombre() + " ya juega " + e.getDeporte()
                        + " en " + e.getNombre() + ".");
            }
        }

        // Inscribe al jugador
        destino.inscribirJugador(socio, posicion, camiseta, fechaReferencia);
    }
   

    /** El administrador da de baja a un jugador de un equipo. */
    public void darDeBajaJugador(Administrador admin, String nombreEquipo, Socio socio) throws ReglaNegocioException { // Recibe datos.
        admin.darDeBajaJugador(buscarEquipo(nombreEquipo), socio); // Delega en el administrador.
    }

    /** El administrador da de baja a un jugador del club: se retira de todos sus equipos. */
    public int darDeBajaJugadorDelClub(Administrador admin, Socio socio) throws ReglaNegocioException { // Recibe datos.
        int bajas = 0; // Contador de equipos de los que salió.
        for (Equipo e : equipos) { // Recorre equipos.
            if (e.buscarInscripcionActiva(socio) != null) { admin.darDeBajaJugador(e, socio); bajas++; } // Baja en cada uno.
        }
        return bajas; // Cuántas bajas se hicieron.
    }

    /** Programa un partido interno entre dos equipos del club. */
    public PartidoInterno programarPartidoInterno(LocalDate fecha, String local, String visitante) throws ReglaNegocioException {
        PartidoInterno p = new PartidoInterno(proximoId("PARTIDO"), fecha, buscarEquipo(local), buscarEquipo(visitante)); // El constructor valida deporte/categoría.
        siguienteId("PARTIDO");  // Solo si no hubo excepción se consume el id.
        partidosInternos.add(p); // Lo registra.
        return p; // Lo devuelve.
    }

    /** Busca un partido de conjunto (interno o de competencia) por id. */
    public Partido buscarPartido(int id) throws ReglaNegocioException { // Recibe el id.
        for (Partido p : getTodosLosPartidos()) if (p.getId() == id) return p; // Busca.
        throw new ReglaNegocioException("No existe el partido " + id + "."); // No existe.
    }

    /** Todos los partidos de conjunto (internos + de competencias). */
    public List<Partido> getTodosLosPartidos() { // Sin parámetros.
        List<Partido> lista = new ArrayList<>(partidosInternos); // Empieza con los internos.
        for (Competencia c : competencias) lista.addAll(c.getPartidos()); // Agrega los de cada competencia.
        return lista; // Devuelve.
    }

    // =====================================================================
    //                            COMPETENCIAS
    // =====================================================================

    /** Registra una competencia (nombre + temporada únicos). */
    public void registrarCompetencia(Competencia c) throws ReglaNegocioException { // Recibe la competencia.
        for (Competencia x : competencias) { // Valida unicidad.
            if (x.getNombre().equalsIgnoreCase(c.getNombre()) && x.getTemporada().equals(c.getTemporada())) {
                throw new ReglaNegocioException("La competencia " + c.getNombre() + " " + c.getTemporada() + " ya existe."); // Repetida.
            }
        }
        competencias.add(c); // La registra.
    }

    /** Busca una competencia por nombre. */
    public Competencia buscarCompetencia(String nombre) throws ReglaNegocioException { // Recibe el nombre.
        for (Competencia c : competencias) if (c.getNombre().equalsIgnoreCase(nombre)) return c; // Busca.
        throw new ReglaNegocioException("No existe la competencia " + nombre + "."); // No existe.
    }

    /** Inscribe un equipo con su nómina en una competencia. */
    public InscripcionCompetencia inscribirEquipoEnCompetencia(String competencia, String equipo, List<Socio> nomina) throws ReglaNegocioException {
        return buscarCompetencia(competencia).inscribirEquipo(buscarEquipo(equipo), nomina); // Delega en la competencia.
    }

    /** Programa un partido oficial del club en una competencia. */
    public PartidoCompetencia programarPartidoCompetencia(String competencia, String equipo, String rival, String escenario,
                                                          LocalDate fecha, String fase, String grupo) throws ReglaNegocioException {
        Competencia c = buscarCompetencia(competencia);              // Competencia.
        InscripcionCompetencia ins = c.buscarInscripcion(buscarEquipo(equipo)); // Inscripción del equipo.
        if (ins == null) throw new ReglaNegocioException(equipo + " no está inscrito en " + competencia + "."); // Debe estar inscrito.
        PartidoCompetencia p = new PartidoCompetencia(siguienteId("PARTIDO"), fecha, ins, rival, escenario, fase, grupo); // Crea el partido.
        c.agregarPartido(p); // Lo agrega a la competencia.
        return p; // Lo devuelve.
    }

    /** Registra un resultado entre dos instituciones externas (para completar tablas). */
    public void registrarResultadoExterno(String competencia, String equipoA, String equipoB, int golesA, int golesB,
                                          String fase, String grupo) throws ReglaNegocioException { // Recibe datos.
        Competencia c = buscarCompetencia(competencia); // Competencia.
        if (golesA == golesB && !c.getDeporte().permiteEmpate()) throw new ReglaNegocioException("En " + c.getDeporte() + " no hay empates."); // Regla.
        c.registrarResultadoExterno(new Enfrentamiento(equipoA, equipoB, golesA, golesB, fase, grupo)); // Lo registra.
    }

    // =====================================================================
    //                       ACADEMIAS Y RESERVAS
    // =====================================================================

    /** Agrega una instalación (id único). */
    public void agregarInstalacion(Instalacion i) throws ReglaNegocioException { // Recibe la instalación.
        if (buscarInstalacionONull(i.getId()) != null) throw new ReglaNegocioException("Ya existe la instalación " + i.getId() + "."); // Id único.
        instalaciones.add(i); // La agrega.
    }

    /** Busca una instalación por id (excepción si no existe). */
    public Instalacion buscarInstalacion(String id) throws ReglaNegocioException { // Recibe el id.
        Instalacion i = buscarInstalacionONull(id); // Busca.
        if (i == null) throw new ReglaNegocioException("No existe la instalación " + id + "."); // No existe.
        return i; // La devuelve.
    }

    /** Busca una instalación por id (null si no existe). */
    public Instalacion buscarInstalacionONull(String id) { // Recibe el id.
        for (Instalacion i : instalaciones) if (i.getId().equalsIgnoreCase(id)) return i; // Busca.
        return null; // No existe.
    }

    /** Inscribe a un socio en una academia con un nivel. */
    public InscripcionDisciplina inscribirEnDisciplina(Socio socio, Disciplina disciplina, Nivel nivel) throws ReglaNegocioException {
        if (buscarInscripcionDisciplina(socio, disciplina) != null) throw new ReglaNegocioException(socio.getNombre() + " ya está inscrito en " + disciplina + "."); // Una vez por disciplina.
        InscripcionDisciplina ins = new InscripcionDisciplina(socio, disciplina, nivel); // Crea la inscripción.
        inscripcionesDisciplina.add(ins); // La registra.
        return ins; // La devuelve.
    }

    /** Inscripción de un socio en una disciplina (null si no tiene). */
    public InscripcionDisciplina buscarInscripcionDisciplina(Socio s, Disciplina d) { // Recibe socio y disciplina.
        for (InscripcionDisciplina i : inscripcionesDisciplina) if (i.getSocio() == s && i.getDisciplina() == d) return i; // Busca.
        return null; // No tiene.
    }

    /**
     * Reserva una instalación. Reglas que valida el club:
     * - el administrador no puede reservar; solo socios reservan;
     * - máximo de instalaciones simultáneas por disciplina (si está lleno se rechaza);
     * - no se cruza con una sesión de entrenamiento en esa instalación.
     * La instalación valida cantidad/modalidad, disponibilidad y bebida caliente.
     */
    public Reserva reservar(Usuario solicitante, String idInstalacion, LocalDateTime fechaHora, int cantidad, String modalidad)
            throws ReglaNegocioException {
        if (!solicitante.puedeUsarInstalacionesYPartidos()) {
            throw new ReglaNegocioException("El administrador no puede reservar instalaciones.");
        }
        if (!(solicitante instanceof Socio)) {
            throw new ReglaNegocioException("Solo los socios pueden reservar instalaciones.");
        }
        Instalacion inst = buscarInstalacion(idInstalacion);
        Disciplina disciplina = inst.getDisciplina();

        int ocupadas = 0;
        for (Instalacion i : instalaciones) {
            if (i.getDisciplina() == disciplina && i.estaOcupada(fechaHora)) {
                ocupadas++;
            }
        }
        int maximo = maxInstalacionesSimultaneas.get(disciplina);
        if (ocupadas >= maximo) {
            throw new ReglaNegocioException("Ya hay " + ocupadas + " instalaciones de " + disciplina
                    + " ocupadas en ese bloque (máximo simultáneo: " + maximo + ").");
        }

        for (SesionEntrenamiento s : sesiones) {
            if (s.getInstalacion() == inst && s.seCruzaCon(fechaHora, Reserva.DURACION_BLOQUE_MINUTOS)) {
                throw new ReglaNegocioException(inst.getId() + " tiene la sesión #" + s.getId() + " en ese horario.");
            }
        }

        Reserva reserva = inst.reservar(proximoId("RESERVA"), (Socio) solicitante, fechaHora, cantidad, modalidad);
        siguienteId("RESERVA");
        return reserva;
    }

    /** Un socio entra a una instalación (regla: no puede entrar a pádel/mesa techada con bebida caliente). */
    public void entrarAInstalacion(Socio socio, String idInstalacion) throws ReglaNegocioException { // Recibe datos.
        Instalacion inst = buscarInstalacion(idInstalacion); // Instalación.
        if (socio.tieneBebidaCaliente() && !inst.permiteBebidaCaliente()) { // Regla de seguridad.
            throw new ReglaNegocioException(socio.getNombre() + " no puede entrar a " + inst.getId() + " con una bebida caliente."); // Se rechaza.
        }
        socio.setInstalacionActual(inst); // Registra dónde está.
    }

    /** El socio sale de la instalación en la que está. */
    public void salirDeInstalacion(Socio socio) { socio.setInstalacionActual(null); } // Ya no está en ninguna.

    /** El socio terminó (o dejó) su bebida caliente. */
    public void terminarBebidaCaliente(Socio socio) { socio.setTieneBebidaCaliente(false); } // Ya no tiene.

    /**
     * Programa un partido de práctica entre socios inscritos en la disciplina.
     * El club averigua si alguno es entrenador (excepción de niveles).
     */
    public PartidoPractica programarPartidoPractica(Usuario solicitante, Disciplina disciplina, List<Socio> jugadores, LocalDate fecha)
            throws ReglaNegocioException { // Puede fallar.
        if (!solicitante.puedeUsarInstalacionesYPartidos()) throw new ReglaNegocioException("El administrador no puede inscribirse a partidos."); // Regla admin.
        if (jugadores.size() < 2) throw new ReglaNegocioException("Se necesitan al menos 2 jugadores."); // Mínimo.
        List<InscripcionDisciplina> inscritos = new ArrayList<>(); // Inscripciones de los jugadores.
        boolean hayEntrenador = false; // ¿Alguno es entrenador?
        for (Socio s : jugadores) { // Recorre jugadores.
            InscripcionDisciplina ins = buscarInscripcionDisciplina(s, disciplina); // Su inscripción.
            if (ins == null) throw new ReglaNegocioException(s.getNombre() + " no está inscrito en " + disciplina + "."); // Debe estar inscrito.
            inscritos.add(ins); // La guarda.
            if (esTambienEntrenador(s)) hayEntrenador = true; // Marca si es entrenador.
        }
        PartidoPractica p = inscritos.get(0).programarPartidoPractica(proximoId("PRACTICA"), // El primer jugador "programa" el partido.
                inscritos.subList(1, inscritos.size()), fecha, hayEntrenador); // Con el resto como rivales/compañeros.
        siguienteId("PRACTICA"); // Se consume el id solo si se pudo programar.
        partidosPractica.add(p); // Lo registra.
        return p; // Lo devuelve.
    }

    /** Registra el resultado de un partido de práctica (actualiza ELO). */
    public void registrarResultadoPractica(int idPartido, List<EstadisticaPractica> stats) throws ReglaNegocioException { // Recibe datos.
        buscarPartidoPractica(idPartido).registrarResultado(stats); // Delega en el partido.
    }

    /** Busca un partido de práctica por id. */
    public PartidoPractica buscarPartidoPractica(int id) throws ReglaNegocioException { // Recibe el id.
        for (PartidoPractica p : partidosPractica) if (p.getId() == id) return p; // Busca.
        throw new ReglaNegocioException("No existe el partido de práctica " + id + "."); // No existe.
    }

    // =====================================================================
    //                    ENTRENAMIENTOS Y TURNOS
    // =====================================================================

    /** Planea una sesión en una instalación (o sin instalación si idInstalacion es null). */
    public SesionEntrenamiento planearSesion(Entrenador entrenador, LocalDateTime fechaHora, int duracion, String idInstalacion,
                                             List<Socio> convocados) throws ReglaNegocioException { // Puede fallar.
        Instalacion inst = idInstalacion == null ? null : buscarInstalacion(idInstalacion); // Instalación (opcional).
        if (inst != null) { // Si usa instalación, debe estar libre.
            LocalDateTime fin = fechaHora.plusMinutes(duracion); // Fin de la sesión.
            for (Reserva r : inst.getReservas()) { // Revisa reservas.
                if (r.getFechaHora().isBefore(fin) && fechaHora.isBefore(r.getFin())) { // Cruce de intervalos.
                    throw new ReglaNegocioException(inst.getId() + " tiene la reserva #" + r.getId() + " en ese horario."); // Ocupada.
                }
            }
            for (SesionEntrenamiento s : sesiones) { // Revisa otras sesiones.
                if (s.getInstalacion() == inst && s.seCruzaCon(fechaHora, duracion)) { // Misma instalación y se cruzan.
                    throw new ReglaNegocioException(inst.getId() + " ya tiene la sesión #" + s.getId() + " en ese horario."); // Ocupada.
                }
            }
        }
        SesionEntrenamiento s = entrenador.planearSesion(proximoId("SESION"), fechaHora, duracion, inst, convocados); // La crea el entrenador.
        siguienteId("SESION"); // Se consume el id solo si se pudo crear.
        sesiones.add(s); // La registra.
        return s; // La devuelve.
    }

    /** Planea una sesión grupal convocando al equipo completo (jugadores activos). */
    public SesionEntrenamiento planearSesionEquipo(Entrenador entrenador, LocalDateTime fechaHora, int duracion, String nombreEquipo)
            throws ReglaNegocioException { // Puede fallar.
        return planearSesion(entrenador, fechaHora, duracion, null, buscarEquipo(nombreEquipo).getJugadoresActivos()); // Convoca a todos.
    }

    /** Busca una sesión por id. */
    public SesionEntrenamiento buscarSesion(int id) throws ReglaNegocioException { // Recibe el id.
        for (SesionEntrenamiento s : sesiones) if (s.getId() == id) return s; // Busca.
        throw new ReglaNegocioException("No existe la sesión " + id + "."); // No existe.
    }

    /** Un empleado solicita cambiar un turno por otro. */
    public SolicitudCambioTurno solicitarCambioTurno(Empleado empleado, Turno actual, Turno propuesto) { // Recibe datos.
        SolicitudCambioTurno s = new SolicitudCambioTurno(siguienteId("SOLICITUD"), empleado, actual, propuesto); // Crea.
        solicitudesTurno.add(s); // Registra.
        return s; // Devuelve.
    }

    /** El administrador revisa una solicitud de cambio de turno. */
    public boolean aprobarCambioTurno(Administrador admin, SolicitudCambioTurno s) throws ReglaNegocioException { // Recibe datos.
        return admin.aprobarCambioTurno(s, getEmpleados(), diasAtencion, horaApertura, horaCierre); // Delega con la configuración del club.
    }

    /** Verifica si la cobertura actual cumple la regla (2 entrenadores y 1 fisioterapeuta en todo momento). */
    public String verificarCobertura() { // Devuelve "OK" o el primer hueco encontrado.
        for (String dia : diasAtencion) { // Cada día.
            for (LocalTime t = horaApertura; t.isBefore(horaCierre); t = t.plusMinutes(Administrador.PASO_MINUTOS)) { // Cada 15 min.
                int ent = 0, fis = 0; // Contadores.
                for (Empleado e : getEmpleados()) { // Cada empleado.
                    if (e.estaDisponible(dia, t)) { if (e instanceof Entrenador) ent++; else fis++; } // Cuenta.
                }
                if (ent < Administrador.MIN_ENTRENADORES || fis < Administrador.MIN_FISIOTERAPEUTAS) { // Hueco.
                    return "Cobertura insuficiente el " + dia + " a las " + t + " (" + ent + " entrenadores, " + fis + " fisioterapeutas)"; // Lo describe.
                }
            }
        }
        return "OK: siempre hay al menos " + Administrador.MIN_ENTRENADORES + " entrenadores y " + Administrador.MIN_FISIOTERAPEUTAS + " fisioterapeuta"; // Todo bien.
    }

    // =====================================================================
    //                      TIENDA, CAFETERÍA Y VENTAS
    // =====================================================================

    /** Agrega un artículo al catálogo (nombre único). */
    public void agregarArticulo(Articulo a) throws ReglaNegocioException { // Recibe el artículo.
        if (buscarArticuloONull(a.getNombre()) != null) throw new ReglaNegocioException("Ya existe el artículo " + a.getNombre() + "."); // Único.
        articulos.add(a); // Lo agrega.
    }

    /** Busca un artículo por nombre (excepción si no existe). */
    public Articulo buscarArticulo(String nombre) throws ReglaNegocioException { // Recibe el nombre.
        Articulo a = buscarArticuloONull(nombre); // Busca.
        if (a == null) throw new ReglaNegocioException("No existe el artículo " + nombre + "."); // No existe.
        return a; // Lo devuelve.
    }

    /** Busca un artículo por nombre (null si no existe). */
    public Articulo buscarArticuloONull(String nombre) { // Recibe el nombre.
        for (Articulo a : articulos) if (a.getNombre().equalsIgnoreCase(nombre)) return a; // Busca.
        return null; // No existe.
    }

    /** Agrega un producto de cafetería (nombre único). */
    public void agregarProductoCafeteria(ProductoCafeteria p) throws ReglaNegocioException { // Recibe el producto.
        if (buscarProductoONull(p.getNombre()) != null) throw new ReglaNegocioException("Ya existe el producto " + p.getNombre() + "."); // Único.
        productosCafeteria.add(p); // Lo agrega.
    }

    /** Busca un producto de cafetería por nombre (excepción si no existe). */
    public ProductoCafeteria buscarProducto(String nombre) throws ReglaNegocioException { // Recibe el nombre.
        ProductoCafeteria p = buscarProductoONull(nombre); // Busca.
        if (p == null) throw new ReglaNegocioException("No existe el producto " + nombre + "."); // No existe.
        return p; // Lo devuelve.
    }

    /** Busca un producto de cafetería por nombre (null si no existe). */
    public ProductoCafeteria buscarProductoONull(String nombre) { // Recibe el nombre.
        for (ProductoCafeteria p : productosCafeteria) if (p.getNombre().equalsIgnoreCase(nombre)) return p; // Busca.
        return null; // No existe.
    }

    /** Abre una venta de tienda (todavía no se registra; se registra al confirmarla). */
    public VentaTienda iniciarVentaTienda(Usuario comprador, LocalDate fecha) { // Recibe comprador y fecha.
        return new VentaTienda(siguienteId("VENTA"), fecha, comprador); // Crea la venta abierta.
    }

    /** Abre una venta de cafetería. */
    public VentaCafeteria iniciarVentaCafeteria(Usuario comprador, LocalDate fecha) { // Recibe comprador y fecha.
        return new VentaCafeteria(siguienteId("VENTA"), fecha, comprador); // Crea la venta abierta.
    }

    /** Confirma (cierra) una venta y la registra con fecha y rubros para contabilidad. */
    public void confirmarVenta(Venta v) throws ReglaNegocioException { // Recibe la venta.
        v.cerrar();    // La venta valida, calcula, descuenta inventario y abona puntos.
        ventas.add(v); // Se registra.
    }

    /** Registra el pago de una mensualidad. */
    public VentaMensualidad pagarMensualidad(Socio socio, String periodo, double valor, LocalDate fecha) throws ReglaNegocioException {
        VentaMensualidad v = new VentaMensualidad(siguienteId("VENTA"), fecha, socio, periodo, valor); // Crea.
        confirmarVenta(v); // Cierra y registra.
        return v; // Devuelve.
    }

    /** El administrador mueve artículos entre ubicaciones. */
    public void moverArticulo(Administrador admin, String articulo, String origen, String destino, int cantidad) throws ReglaNegocioException {
        admin.moverArticulo(buscarArticulo(articulo), origen, destino, cantidad); // Delega.
    }

    /** El administrador reabastece un artículo. */
    public void reabastecer(Administrador admin, String articulo, String ubicacion, int cantidad) throws ReglaNegocioException {
        admin.reabastecer(buscarArticulo(articulo), ubicacion, cantidad); // Delega.
    }

    /** Un empleado sugiere un nuevo artículo o snack. */
    public Sugerencia sugerirProducto(Empleado autor, String tipo, String nombre, String categoriaOAlergenos, double precio, LocalDate fecha) {
        Sugerencia s = new Sugerencia(siguienteId("SUGERENCIA"), autor, tipo, nombre, categoriaOAlergenos, precio, fecha); // Crea.
        sugerencias.add(s); // Registra.
        return s; // Devuelve.
    }

    /** El administrador aprueba una sugerencia: el producto se agrega al catálogo correspondiente. */
    public void aprobarSugerencia(Administrador admin, int idSugerencia) throws ReglaNegocioException { // Recibe datos.
        Sugerencia s = buscarSugerencia(idSugerencia); // Busca la sugerencia.
        s.aprobar(); // Cambia su estado (valida que esté pendiente).
        if (Sugerencia.ARTICULO.equals(s.getTipoProducto())) { // Si es artículo de tienda...
            agregarArticulo(new Articulo(s.getNombreProducto(), s.getCategoria(), s.getPrecioSugerido())); // ...se crea (sin inventario).
        } else { // Si es snack...
            List<String> alergenos = new ArrayList<>(); // Lista de alérgenos.
            for (String a : s.getCategoria().split(",")) if (!a.isBlank()) alergenos.add(a.trim().toUpperCase()); // Separa por comas.
            agregarProductoCafeteria(new Snack(s.getNombreProducto(), s.getPrecioSugerido(), alergenos)); // ...se crea el snack.
        }
    }

    /** El administrador rechaza una sugerencia. */
    public void rechazarSugerencia(Administrador admin, int idSugerencia) throws ReglaNegocioException { // Recibe datos.
        buscarSugerencia(idSugerencia).rechazar(); // Cambia el estado.
    }

    /** Busca una sugerencia por id. */
    public Sugerencia buscarSugerencia(int id) throws ReglaNegocioException { // Recibe el id.
        for (Sugerencia s : sugerencias) if (s.getId() == id) return s; // Busca.
        throw new ReglaNegocioException("No existe la sugerencia " + id + "."); // No existe.
    }

    // =====================================================================
    //                    VISTAS Y REPORTES DEL ADMINISTRADOR
    // =====================================================================

    /** Estado detallado del inventario de instalaciones y artículos. */
    public String generarEstadoInventario() { // Sin parámetros.
        StringBuilder sb = new StringBuilder("=== INVENTARIO DE INSTALACIONES ===\n"); // Encabezado.
        for (Instalacion i : instalaciones) sb.append("  ").append(i).append("\n"); // Una línea por instalación.
        sb.append("  Máximo simultáneo por disciplina: ").append(maxInstalacionesSimultaneas).append("\n"); // Configuración.
        sb.append("=== INVENTARIO DE ARTÍCULOS ===\n"); // Encabezado.
        for (Articulo a : articulos) sb.append("  ").append(a).append("\n"); // Una línea por artículo.
        sb.append("=== PRODUCTOS DE CAFETERÍA ===\n"); // Encabezado.
        for (ProductoCafeteria p : productosCafeteria) sb.append("  ").append(p).append("\n"); // Una línea por producto.
        return sb.toString(); // Devuelve el texto.
    }

    /** Historial completo de reservas, partidos y sesiones. */
    public String generarHistorial() { // Sin parámetros.
        StringBuilder sb = new StringBuilder("=== HISTORIAL DE RESERVAS ===\n"); // Encabezado.
        for (Instalacion i : instalaciones) for (Reserva r : i.getReservas()) sb.append("  ").append(r).append("\n"); // Reservas.
        sb.append("=== HISTORIAL DE PARTIDOS DE CONJUNTO ===\n"); // Encabezado.
        for (Partido p : getTodosLosPartidos()) sb.append("  ").append(p).append("\n"); // Partidos.
        sb.append("=== HISTORIAL DE PARTIDOS DE PRÁCTICA ===\n"); // Encabezado.
        for (PartidoPractica p : partidosPractica) sb.append("  ").append(p).append("\n"); // Prácticas.
        sb.append("=== HISTORIAL DE SESIONES DE ENTRENAMIENTO ===\n"); // Encabezado.
        for (SesionEntrenamiento s : sesiones) sb.append("  ").append(s).append("\n"); // Sesiones.
        return sb.toString(); // Devuelve.
    }

    /** Reporte de ventas de un rubro en un periodo DIARIO, SEMANAL o MENSUAL que contiene la fecha dada. */
    public ReporteVentas generarReporteVentas(Administrador admin, String rubro, String tipoPeriodo, LocalDate referencia) {
        LocalDate[] p = Reporte.calcularPeriodo(tipoPeriodo, referencia); // Calcula [inicio, fin].
        return admin.generarReporteVentas(ventas, rubro, p[0], p[1]);     // Lo genera el administrador.
    }

    /** Reporte deportivo (ASISTENCIA, RESULTADOS o RANKINGS) en un periodo. */
    public ReporteDeportivo generarReporteDeportivo(Administrador admin, String tipo, String tipoPeriodo, LocalDate referencia) {
        LocalDate[] p = Reporte.calcularPeriodo(tipoPeriodo, referencia); // Calcula [inicio, fin].
        return admin.generarReporteDeportivo(tipo, p[0], p[1], sesiones, getTodosLosPartidos(), partidosPractica, inscripcionesDisciplina); // Genera.
    }

    // =====================================================================
    //                            PERSISTENCIA
    // =====================================================================

    /** Guarda todo el estado del club en la carpeta indicada. */
    public void guardar(String carpeta) throws Exception { new PersistenciaClub().guardar(this, carpeta); } // Delega.

    /** Carga un club desde la carpeta indicada. */
    public static ClubDeportivo cargar(String carpeta) throws Exception { return new PersistenciaClub().cargar(carpeta); } // Delega.

    // =====================================================================
    //                        GETTERS (y setters de configuración)
    // =====================================================================
    public List<String> getDiasAtencion() { return diasAtencion; }                         // Getter.
    public void setDiasAtencion(List<String> d) { this.diasAtencion = new ArrayList<>(d); } // Setter.
    public LocalTime getHoraApertura() { return horaApertura; }                            // Getter.
    public void setHoraApertura(LocalTime h) { this.horaApertura = h; }                    // Setter.
    public LocalTime getHoraCierre() { return horaCierre; }                                // Getter.
    public void setHoraCierre(LocalTime h) { this.horaCierre = h; }                        // Setter.
    public Map<Disciplina, Integer> getMaxInstalacionesSimultaneas() { return maxInstalacionesSimultaneas; } // Getter.
    public void setMaxInstalacionesSimultaneas(Disciplina d, int max) { maxInstalacionesSimultaneas.put(d, max); } // Setter.
    public Map<String, Integer> getContadores() { return contadores; }                     // Getter.
    public List<Usuario> getUsuarios() { return usuarios; }                                // Getter.
    public List<Equipo> getEquipos() { return equipos; }                                   // Getter.
    public List<PartidoInterno> getPartidosInternos() { return partidosInternos; }         // Getter.
    public List<Competencia> getCompetencias() { return competencias; }                    // Getter.
    public List<Instalacion> getInstalaciones() { return instalaciones; }                  // Getter.
    public List<InscripcionDisciplina> getInscripcionesDisciplina() { return inscripcionesDisciplina; } // Getter.
    public List<PartidoPractica> getPartidosPractica() { return partidosPractica; }        // Getter.
    public List<SesionEntrenamiento> getSesiones() { return sesiones; }                    // Getter.
    public List<SolicitudCambioTurno> getSolicitudesTurno() { return solicitudesTurno; }   // Getter.
    public List<Articulo> getArticulos() { return articulos; }                             // Getter.
    public List<ProductoCafeteria> getProductosCafeteria() { return productosCafeteria; }  // Getter.
    public List<CodigoDescuento> getCodigosDescuento() { return codigosDescuento; }        // Getter.
    public List<Venta> getVentas() { return ventas; }                                      // Getter.
    public List<Sugerencia> getSugerencias() { return sugerencias; }                       // Getter.
}
