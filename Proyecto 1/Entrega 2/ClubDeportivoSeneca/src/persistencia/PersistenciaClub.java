package persistencia; // Paquete encargado de guardar y cargar la información en archivos.

import java.io.BufferedReader;                    // Lector de texto por líneas.
import java.io.BufferedWriter;                    // Escritor de texto con buffer.
import java.io.File;                              // Representa un archivo o carpeta.
import java.io.IOException;                       // Errores de entrada/salida.
import java.nio.charset.StandardCharsets;         // Codificación UTF-8.
import java.nio.file.Files;                       // Utilidades para abrir archivos.
import java.time.LocalDate;                       // Fecha.
import java.time.LocalDateTime;                   // Fecha y hora.
import java.time.LocalTime;                       // Hora.
import java.util.ArrayList;                       // Lista dinámica.
import java.util.Arrays;                          // Utilidades de arreglos.
import java.util.LinkedHashMap;                   // Mapa ordenado.
import java.util.List;                            // Lista.
import java.util.Map;                             // Mapa.
import logica.ClubDeportivo;                      // Fachada del club.
import modelo.academias.*;                        // Todas las clases de academias.
import modelo.competencias.*;                     // Todas las clases de competencias.
import modelo.conjunto.*;                         // Todas las clases de deportes de conjunto.
import modelo.entrenamiento.*;                    // Todas las clases de entrenamiento.
import modelo.enums.*;                            // Todos los enums.
import modelo.usuarios.*;                         // Todos los usuarios.
import modelo.ventas.*;                           // Todas las clases de ventas.

/**
 * Persistencia en archivos de texto plano, uno por tipo de información, dentro de una carpeta
 * (que NO es la carpeta del código fuente). Formato: una línea por registro, campos separados por ';',
 * listas separadas por '|', "-" significa "vacío/null". Las líneas que empiezan por '#' son comentarios.
 * Las referencias entre objetos se guardan por su identificador (id del usuario, nombre del equipo, etc.).
 */
public class PersistenciaClub { // Clase PersistenciaClub.

    private static final String SEP = ";";   // Separador de campos.
    private static final String LISTA = "|"; // Separador de elementos de una lista.
    private static final String NULO = "-";  // Marca de valor vacío.

    // =====================================================================
    //                               GUARDAR
    // =====================================================================

    /** Guarda todo el estado del club en la carpeta (la crea si no existe). */
    public void guardar(ClubDeportivo club, String rutaCarpeta) throws IOException { // Recibe el club y la carpeta.
        File carpeta = new File(rutaCarpeta);      // Objeto que representa la carpeta.
        if (!carpeta.exists() && !carpeta.mkdirs()) throw new IOException("No se pudo crear la carpeta " + rutaCarpeta); // La crea si hace falta.

        // ---- config.txt: configuración y contadores ----
        List<String> l = new ArrayList<>(); // Líneas a escribir (se reutiliza la variable en cada archivo).
        l.add("DIAS;" + String.join(LISTA, club.getDiasAtencion())); // Días de atención.
        l.add("HORARIO;" + club.getHoraApertura() + SEP + club.getHoraCierre()); // Horario.
        for (Map.Entry<Disciplina, Integer> e : club.getMaxInstalacionesSimultaneas().entrySet()) l.add("MAX;" + e.getKey() + SEP + e.getValue()); // Máximos.
        for (Map.Entry<String, Integer> e : club.getContadores().entrySet()) l.add("CONTADOR;" + e.getKey() + SEP + e.getValue()); // Contadores de ids.
        escribir(carpeta, "config.txt", "clave;valores...", l); // Escribe el archivo.

        // ---- instalaciones.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (Instalacion i : club.getInstalaciones()) { // Una línea por instalación.
            boolean techada = i instanceof MesaDeTenisMesa && ((MesaDeTenisMesa) i).isTechada(); // Solo aplica a mesas.
            l.add(i.getTipo() + SEP + i.getId() + SEP + techada); // tipo;id;techada
        }
        escribir(carpeta, "instalaciones.txt", "tipo;id;techada", l); // Escribe.

        // ---- usuarios.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (Usuario u : club.getUsuarios()) { // Una línea por usuario.
            String base = u.getTipo() + SEP + u.getId() + SEP + txt(u.getNombre()) + SEP + txt(u.getLogin()) + SEP + txt(u.getPassword()) + SEP + u.getPuntosFidelidad(); // Campos comunes.
            if (u instanceof Socio) { // Campos extra de Socio.
                Socio s = (Socio) u; // Cast.
                base += SEP + s.getFechaDeNacimiento() + SEP + (s.getInstalacionActual() == null ? NULO : s.getInstalacionActual().getId()) + SEP + s.tieneBebidaCaliente(); // fecha;instalación;bebida
            } else if (u instanceof Entrenador) { // Campo extra de Entrenador.
                Socio p = ((Entrenador) u).getPerfilJugador(); // Perfil de jugador.
                base += SEP + (p == null ? NULO : p.getId()); // id del socio o "-".
            }
            l.add(base); // Agrega la línea.
        }
        escribir(carpeta, "usuarios.txt", "tipo;id;nombre;login;password;puntos;[SOCIO: fechaNac;instalacionActual;bebidaCaliente | ENTRENADOR: idPerfilJugador]", l); // Escribe.

        // ---- turnos.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (Empleado e : club.getEmpleados()) for (Turno t : e.getTurnos()) l.add(e.getId() + SEP + t.getDia() + SEP + t.getHoraInicio() + SEP + t.getHoraFin()); // Un turno por línea.
        escribir(carpeta, "turnos.txt", "idEmpleado;dia;horaInicio;horaFin", l); // Escribe.

        // ---- codigos_descuento.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (CodigoDescuento c : club.getCodigosDescuento()) l.add(c.getCodigo() + SEP + c.getDueno().getId()); // codigo;idEmpleado
        escribir(carpeta, "codigos_descuento.txt", "codigo;idEmpleado", l); // Escribe.

        // ---- equipos.txt e inscripciones_equipo.txt ----
        l = new ArrayList<>(); // Equipos.
        List<String> l2 = new ArrayList<>(); // Inscripciones.
        for (Equipo e : club.getEquipos()) { // Recorre equipos.
            l.add(txt(e.getNombre()) + SEP + e.getDeporte() + SEP + e.getCategoria() + SEP + e.getCupoMaximo()); // nombre;deporte;categoria;cupo
            for (InscripcionEquipo i : e.getInscripciones()) { // Sus inscripciones.
                l2.add(txt(e.getNombre()) + SEP + i.getJugador().getId() + SEP + txt(i.getPosicion()) + SEP + i.getNumeroCamiseta() + SEP + i.isActivo() + SEP + i.isSuspendidoProximoPartido()); // Línea.
            }
        }
        escribir(carpeta, "equipos.txt", "nombre;deporte;categoria;cupoMaximo", l); // Escribe equipos.
        escribir(carpeta, "inscripciones_equipo.txt", "equipo;idSocio;posicion;camiseta;activo;suspendido", l2); // Escribe inscripciones.

        // ---- competencias, fases, grupos, inscripciones, resultados externos ----
        l = new ArrayList<>(); l2 = new ArrayList<>(); // Competencias y fases.
        List<String> l3 = new ArrayList<>(), l4 = new ArrayList<>(), l5 = new ArrayList<>(); // Grupos, inscripciones, externos.
        for (Competencia c : club.getCompetencias()) { // Recorre competencias.
            String extra = NULO + SEP + NULO; // índiceFase;campeón (solo para fases).
            if (c instanceof CompetenciaFases) { // Si es por fases...
                CompetenciaFases cf = (CompetenciaFases) c; // Cast.
                extra = cf.getIndiceFaseActual() + SEP + (cf.getCampeon().isEmpty() ? NULO : txt(cf.getCampeon())); // Datos de fase.
                for (Fase f : cf.getFases()) { // Sus fases.
                    l2.add(txt(c.getNombre()) + SEP + txt(f.getNombre()) + SEP + f.getTipo() + SEP + f.getClasificadosPorGrupo() + SEP + f.isFinalizada() + SEP + unir(f.getParticipantes())); // Fase.
                    for (Grupo g : f.getGrupos()) l3.add(txt(c.getNombre()) + SEP + txt(f.getNombre()) + SEP + txt(g.getNombre()) + SEP + unir(g.getEquipos())); // Grupo.
                }
            }
            l.add(c.getFormato() + SEP + txt(c.getNombre()) + SEP + txt(c.getEntidadOrganizadora()) + SEP + txt(c.getTemporada()) + SEP
                    + c.getDeporte() + SEP + c.getCategoria() + SEP + c.getNominaMinima() + SEP + extra); // Competencia.
            for (InscripcionCompetencia i : c.getInscripciones()) { // Inscripciones.
                List<String> nomina = new ArrayList<>(); for (Socio s : i.getNomina()) nomina.add(s.getId()); // Ids de la nómina.
                List<String> amarillas = new ArrayList<>(); for (Map.Entry<Socio, Integer> e : i.getAmarillasAcumuladas().entrySet()) amarillas.add(e.getKey().getId() + ":" + e.getValue()); // id:amarillas
                List<String> susp = new ArrayList<>(); for (Socio s : i.getSuspendidos()) susp.add(s.getId()); // Ids suspendidos.
                l4.add(txt(c.getNombre()) + SEP + txt(i.getEquipo().getNombre()) + SEP + unir(nomina) + SEP + unir(amarillas) + SEP + unir(susp)); // Línea.
            }
            for (Enfrentamiento e : c.getResultadosExternos()) { // Resultados externos.
                l5.add(txt(c.getNombre()) + SEP + txt(e.getEquipoA()) + SEP + txt(e.getEquipoB()) + SEP + e.getGolesA() + SEP + e.getGolesB() + SEP + vacio(e.getFase()) + SEP + vacio(e.getGrupo())); // Línea.
            }
        }
        escribir(carpeta, "competencias.txt", "formato;nombre;entidad;temporada;deporte;categoria;nominaMinima;indiceFaseActual;campeon", l); // Escribe.
        escribir(carpeta, "fases.txt", "competencia;fase;tipo;clasificadosPorGrupo;finalizada;participantes", l2); // Escribe.
        escribir(carpeta, "grupos.txt", "competencia;fase;grupo;equipos", l3); // Escribe.
        escribir(carpeta, "inscripciones_competencia.txt", "competencia;equipo;nomina;amarillasAcumuladas(id:n);suspendidos", l4); // Escribe.
        escribir(carpeta, "resultados_externos.txt", "competencia;equipoA;equipoB;golesA;golesB;fase;grupo", l5); // Escribe.

        // ---- partidos.txt y estadisticas_partido.txt ----
        l = new ArrayList<>(); l2 = new ArrayList<>(); // Partidos y estadísticas.
        for (Partido p : club.getTodosLosPartidos()) { // Recorre todos los partidos.
            String fin = SEP + p.getMarcadorA() + SEP + p.getMarcadorB() + SEP + p.isJugado(); // Resultado al final de la línea.
            if (p instanceof PartidoInterno) { // Interno.
                PartidoInterno pi = (PartidoInterno) p; // Cast.
                l.add("INTERNO;" + p.getId() + SEP + p.getFecha() + SEP + txt(pi.getEquipoLocal().getNombre()) + SEP + txt(pi.getEquipoVisitante().getNombre()) + fin); // Línea.
            } else { // De competencia.
                PartidoCompetencia pc = (PartidoCompetencia) p; // Cast.
                l.add("COMPETENCIA;" + p.getId() + SEP + p.getFecha() + SEP + txt(pc.getCompetencia().getNombre()) + SEP + txt(pc.getEquipo().getNombre()) + SEP
                        + txt(pc.getInstitucionRival()) + SEP + txt(pc.getEscenario()) + SEP + vacio(pc.getFase()) + SEP + vacio(pc.getGrupo()) + fin); // Línea.
            }
            for (EstadisticaPartido e : p.getEstadisticas()) l2.add(p.getId() + SEP + serializarEstadistica(e)); // Estadísticas.
        }
        escribir(carpeta, "partidos.txt", "INTERNO;id;fecha;local;visitante;marcadorA;marcadorB;jugado | COMPETENCIA;id;fecha;competencia;equipo;rival;escenario;fase;grupo;marcadorA;marcadorB;jugado", l); // Escribe.
        escribir(carpeta, "estadisticas_partido.txt", "idPartido;deporte;idSocio;valores...", l2); // Escribe.

        // ---- reservas.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (Instalacion i : club.getInstalaciones()) for (Reserva r : i.getReservas()) { // Cada reserva.
            l.add(r.getId() + SEP + i.getId() + SEP + r.getSocioResponsable().getId() + SEP + r.getFechaHora() + SEP + r.getCantidadJugadores() + SEP + r.getModalidad()); // Línea.
        }
        escribir(carpeta, "reservas.txt", "id;instalacion;idSocio;fechaHora;cantidadJugadores;modalidad", l); // Escribe.

        // ---- inscripciones_disciplina.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (InscripcionDisciplina i : club.getInscripcionesDisciplina()) l.add(i.getSocio().getId() + SEP + i.getDisciplina() + SEP + i.getNivel() + SEP + i.getRankingElo()); // Línea.
        escribir(carpeta, "inscripciones_disciplina.txt", "idSocio;disciplina;nivel;rankingElo", l); // Escribe.

        // ---- partidos_practica.txt y estadisticas_practica.txt ----
        l = new ArrayList<>(); l2 = new ArrayList<>(); // Partidos y estadísticas.
        for (PartidoPractica p : club.getPartidosPractica()) { // Recorre.
            List<String> ids = new ArrayList<>(); for (InscripcionDisciplina i : p.getJugadores()) ids.add(i.getSocio().getId()); // Ids de jugadores (en orden).
            l.add(p.getId() + SEP + p.getDisciplina() + SEP + p.getFecha() + SEP + unir(ids) + SEP + p.isJugado()); // Línea.
            for (EstadisticaPractica e : p.getEstadisticas()) { // Estadísticas.
                String valores = e instanceof EstadisticaTenisPadel // Valores según el tipo.
                        ? "TENIS_PADEL;" + e.getSetsGanados() + SEP + ((EstadisticaTenisPadel) e).getGamesGanados() + SEP + ((EstadisticaTenisPadel) e).getAces() + SEP + ((EstadisticaTenisPadel) e).getErroresNoForzados()
                        : "TENIS_MESA;" + e.getSetsGanados() + SEP + ((EstadisticaTenisMesa) e).getPuntosTotales();
                l2.add(p.getId() + SEP + e.getJugador().getId() + SEP + e.getEloAntes() + SEP + e.getEloDespues() + SEP + valores); // Línea.
            }
        }
        escribir(carpeta, "partidos_practica.txt", "id;disciplina;fecha;idsJugadores(ladoA|ladoB);jugado", l); // Escribe.
        escribir(carpeta, "estadisticas_practica.txt", "idPartido;idSocio;eloAntes;eloDespues;tipo;valores...", l2); // Escribe.

        // ---- sesiones.txt y asistencias.txt ----
        l = new ArrayList<>(); l2 = new ArrayList<>(); // Sesiones y asistencias.
        for (SesionEntrenamiento s : club.getSesiones()) { // Recorre.
            List<String> ids = new ArrayList<>(); for (Socio c : s.getConvocados()) ids.add(c.getId()); // Ids convocados.
            l.add(s.getId() + SEP + s.getEntrenador().getId() + SEP + (s.getInstalacion() == null ? NULO : s.getInstalacion().getId()) + SEP
                    + s.getFechaHora() + SEP + s.getDuracion() + SEP + unir(ids) + SEP + s.isFinalizada()); // Línea.
            for (AsistenciaEvaluacion a : s.getAsistencias()) l2.add(s.getId() + SEP + a.getJugador().getId() + SEP + a.getEstado() + SEP + a.getNota() + SEP + vacio(txt(a.getComentario()))); // Asistencia.
        }
        escribir(carpeta, "sesiones.txt", "id;idEntrenador;instalacion;fechaHora;duracion;convocados;finalizada", l); // Escribe.
        escribir(carpeta, "asistencias.txt", "idSesion;idSocio;estado;nota;comentario", l2); // Escribe.

        // ---- solicitudes_turno.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (SolicitudCambioTurno s : club.getSolicitudesTurno()) { // Recorre.
            Turno a = s.getTurnoActual(), p = s.getTurnoPropuesto(); // Turnos.
            l.add(s.getId() + SEP + s.getEmpleado().getId() + SEP + a.getDia() + SEP + a.getHoraInicio() + SEP + a.getHoraFin() + SEP
                    + p.getDia() + SEP + p.getHoraInicio() + SEP + p.getHoraFin() + SEP + s.getEstado()); // Línea.
        }
        escribir(carpeta, "solicitudes_turno.txt", "id;idEmpleado;diaActual;iniActual;finActual;diaProp;iniProp;finProp;estado", l); // Escribe.

        // ---- articulos.txt e inventario.txt ----
        l = new ArrayList<>(); l2 = new ArrayList<>(); // Artículos e inventario.
        for (Articulo a : club.getArticulos()) { // Recorre.
            l.add(txt(a.getNombre()) + SEP + txt(a.getCategoria()) + SEP + a.getPrecio()); // Artículo.
            for (InventarioUbicacion iu : a.getInventario()) l2.add(txt(a.getNombre()) + SEP + txt(iu.getUbicacion()) + SEP + iu.getCantidad()); // Inventario.
        }
        escribir(carpeta, "articulos.txt", "nombre;categoria;precio", l); // Escribe.
        escribir(carpeta, "inventario.txt", "articulo;ubicacion;cantidad", l2); // Escribe.

        // ---- productos_cafeteria.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (ProductoCafeteria p : club.getProductosCafeteria()) { // Recorre.
            String extra = p instanceof Bebida ? ((Bebida) p).getTipoBebida().toString() : unir(((Snack) p).getAlergenos()); // Tipo o alérgenos.
            l.add(p.getTipo() + SEP + txt(p.getNombre()) + SEP + p.getPrecio() + SEP + extra); // Línea.
        }
        escribir(carpeta, "productos_cafeteria.txt", "tipo;nombre;precio;[BEBIDA: FRIA/CALIENTE | SNACK: alergenos]", l); // Escribe.

        // ---- ventas.txt e items_venta.txt ----
        l = new ArrayList<>(); l2 = new ArrayList<>(); // Ventas e ítems.
        for (Venta v : club.getVentas()) { // Recorre.
            String extra; // Campos propios de cada rubro.
            if (v instanceof VentaTienda) { // Tienda: código aplicado.
                CodigoDescuento c = ((VentaTienda) v).getDescuentoAplicado(); // Código.
                extra = (c == null ? NULO : c.getCodigo()) + SEP + NULO; // codigo;-
                for (ItemVentaTienda i : ((VentaTienda) v).getItems()) l2.add(v.getId() + SEP + txt(i.getArticulo().getNombre()) + SEP + i.getCantidad() + SEP + i.getPrecioUnitario()); // Ítems.
            } else if (v instanceof VentaCafeteria) { // Cafetería: propina.
                extra = ((VentaCafeteria) v).getPropina() + SEP + NULO; // propina;-
                for (ItemVentaCafeteria i : ((VentaCafeteria) v).getItems()) l2.add(v.getId() + SEP + txt(i.getProducto().getNombre()) + SEP + i.getCantidad() + SEP + i.getPrecioUnitario()); // Ítems.
            } else { // Mensualidad: periodo y valor.
                VentaMensualidad m = (VentaMensualidad) v; // Cast.
                extra = txt(m.getPeriodo()) + SEP + m.getValor(); // periodo;valor
            }
            l.add(v.getId() + SEP + v.getRubro() + SEP + v.getFecha() + SEP + v.getComprador().getId() + SEP + v.getSubtotal() + SEP + v.getDescuento() + SEP
                    + v.getPuntosRedimidos() + SEP + v.getImpuesto() + SEP + v.getTotal() + SEP + v.getPuntosFidelidadGenerados() + SEP + extra); // Línea.
        }
        escribir(carpeta, "ventas.txt", "id;rubro;fecha;idComprador;subtotal;descuento;puntosRedimidos;impuesto;total;puntosGenerados;extra1;extra2", l); // Escribe.
        escribir(carpeta, "items_venta.txt", "idVenta;producto;cantidad;precioUnitario", l2); // Escribe.

        // ---- sugerencias.txt ----
        l = new ArrayList<>(); // Nueva lista.
        for (Sugerencia s : club.getSugerencias()) { // Recorre.
            l.add(s.getId() + SEP + s.getAutor().getId() + SEP + s.getTipoProducto() + SEP + txt(s.getNombreProducto()) + SEP + vacio(txt(s.getCategoria())) + SEP
                    + s.getPrecioSugerido() + SEP + s.getEstado() + SEP + s.getFecha()); // Línea.
        }
        escribir(carpeta, "sugerencias.txt", "id;idAutor;tipo;nombre;categoriaOAlergenos;precio;estado;fecha", l); // Escribe.
    }

    /** Convierte una estadística de partido a texto según su deporte. */
    private String serializarEstadistica(EstadisticaPartido e) { // Recibe la estadística.
        String id = e.getJugador().getId(); // Id del jugador.
        if (e instanceof EstadisticaFutbol) { // Fútbol.
            EstadisticaFutbol f = (EstadisticaFutbol) e; // Cast.
            return "FUTBOL;" + id + SEP + f.getGoles() + SEP + f.getAsistencias() + SEP + f.getAmarillas() + SEP + f.tieneRoja(); // Campos.
        } else if (e instanceof EstadisticaBaloncesto) { // Baloncesto.
            EstadisticaBaloncesto b = (EstadisticaBaloncesto) e; // Cast.
            return "BALONCESTO;" + id + SEP + b.getPuntos() + SEP + b.getRebotes(); // Campos.
        } else { // Voleibol.
            EstadisticaVoleibol v = (EstadisticaVoleibol) e; // Cast.
            return "VOLEIBOL;" + id + SEP + v.getAces() + SEP + v.getBloqueos(); // Campos.
        }
    }

    // =====================================================================
    //                                CARGAR
    // =====================================================================

    /** Reconstruye el club leyendo los archivos de la carpeta. El orden importa: primero lo que otros referencian. */
    public ClubDeportivo cargar(String rutaCarpeta) throws Exception { // Recibe la carpeta.
        File carpeta = new File(rutaCarpeta); // Carpeta.
        if (!carpeta.isDirectory()) throw new IOException("No existe la carpeta de datos " + carpeta.getAbsolutePath()); // Debe existir.
        ClubDeportivo club = new ClubDeportivo(); // Club vacío.

        // ---- config ----
        for (String[] c : leer(carpeta, "config.txt")) { // Cada línea.
            switch (c[0]) { // Según la clave.
                case "DIAS": club.setDiasAtencion(partir(c[1])); break; // Días.
                case "HORARIO": club.setHoraApertura(LocalTime.parse(c[1])); club.setHoraCierre(LocalTime.parse(c[2])); break; // Horario.
                case "MAX": club.setMaxInstalacionesSimultaneas(Disciplina.valueOf(c[1]), Integer.parseInt(c[2])); break; // Máximos.
                case "CONTADOR": club.getContadores().put(c[1], Integer.parseInt(c[2])); break; // Contadores.
                default: break; // Clave desconocida: se ignora.
            }
        }

        // ---- instalaciones (antes que usuarios, porque el socio guarda su instalación actual) ----
        for (String[] c : leer(carpeta, "instalaciones.txt")) { // Cada línea.
            Instalacion i; // Instalación a crear.
            switch (c[0]) { // Según el tipo.
                case "CANCHA_TENIS": i = new CanchaTenis(c[1]); break;                                  // Tenis.
                case "CANCHA_PADEL": i = new CanchaPadel(c[1]); break;                                  // Pádel.
                default: i = new MesaDeTenisMesa(c[1], Boolean.parseBoolean(c[2]));                     // Mesa.
            }
            club.getInstalaciones().add(i); // La agrega.
        }

        // ---- usuarios (dos pasadas: el perfil de jugador del entrenador es un Socio que puede aparecer después) ----
        Map<Entrenador, String> perfilesPendientes = new LinkedHashMap<>(); // Entrenador -> id de su perfil.
        for (String[] c : leer(carpeta, "usuarios.txt")) { // Cada línea.
            Usuario u; // Usuario a crear.
            switch (c[0]) { // Según el tipo.
                case "SOCIO": { // Socio.
                    Socio s = new Socio(c[1], c[2], c[3], c[4], LocalDate.parse(c[6])); // Crea.
                    if (!NULO.equals(c[7])) s.setInstalacionActual(club.buscarInstalacionONull(c[7])); // Instalación actual.
                    s.setTieneBebidaCaliente(Boolean.parseBoolean(c[8])); // Bebida.
                    u = s; break; // Listo.
                }
                case "ENTRENADOR": { // Entrenador.
                    Entrenador e = new Entrenador(c[1], c[2], c[3], c[4]); // Crea.
                    if (c.length > 6 && !NULO.equals(c[6])) perfilesPendientes.put(e, c[6]); // Perfil pendiente.
                    u = e; break; // Listo.
                }
                case "FISIOTERAPEUTA": u = new Fisioterapeuta(c[1], c[2], c[3], c[4]); break; // Fisioterapeuta.
                default: u = new Administrador(c[1], c[2], c[3], c[4]);                     // Administrador.
            }
            u.setPuntosFidelidad(Double.parseDouble(c[5])); // Puntos.
            club.getUsuarios().add(u); // Se agrega directo (sin crear códigos: se cargan de su archivo).
        }
        for (Map.Entry<Entrenador, String> e : perfilesPendientes.entrySet()) e.getKey().setPerfilJugador(club.buscarSocio(e.getValue())); // Segunda pasada.

        // ---- turnos y códigos ----
        for (String[] c : leer(carpeta, "turnos.txt")) ((Empleado) club.buscarUsuario(c[0])).agregarTurno(new Turno(c[1], LocalTime.parse(c[2]), LocalTime.parse(c[3]))); // Turnos.
        for (String[] c : leer(carpeta, "codigos_descuento.txt")) club.getCodigosDescuento().add(new CodigoDescuento(c[0], (Empleado) club.buscarUsuario(c[1]))); // Códigos.

        // ---- equipos e inscripciones ----
        for (String[] c : leer(carpeta, "equipos.txt")) club.getEquipos().add(new Equipo(c[0], Deporte.valueOf(c[1]), Categoria.valueOf(c[2]), Integer.parseInt(c[3]))); // Equipos.
        for (String[] c : leer(carpeta, "inscripciones_equipo.txt")) { // Inscripciones.
            Equipo e = club.buscarEquipo(c[0]); // Equipo.
            InscripcionEquipo i = new InscripcionEquipo(e, club.buscarSocio(c[1]), c[2], Integer.parseInt(c[3])); // Crea.
            i.setActivo(Boolean.parseBoolean(c[4]));                  // Activo.
            i.setSuspendidoProximoPartido(Boolean.parseBoolean(c[5])); // Suspendido.
            e.agregarInscripcionCargada(i); // Agrega.
        }

        // ---- competencias ----
        for (String[] c : leer(carpeta, "competencias.txt")) { // Cada competencia.
            Deporte d = Deporte.valueOf(c[4]); Categoria cat = Categoria.valueOf(c[5]); int nom = Integer.parseInt(c[6]); // Campos comunes.
            if ("LIGA".equals(c[0])) club.getCompetencias().add(new CompetenciaLiga(c[1], c[2], c[3], d, cat, nom)); // Liga.
            else { // Por fases.
                CompetenciaFases cf = new CompetenciaFases(c[1], c[2], c[3], d, cat, nom); // Crea.
                cf.setIndiceFaseActual(Integer.parseInt(c[7]));             // Fase actual.
                cf.setCampeon(NULO.equals(c[8]) ? "" : c[8]);              // Campeón.
                club.getCompetencias().add(cf); // Agrega.
            }
        }
        Map<String, String> participantesFase = new LinkedHashMap<>(); // "competencia/fase" -> participantes (se aplican después de los grupos).
        for (String[] c : leer(carpeta, "fases.txt")) { // Fases.
            Fase f = new Fase(c[1], TipoFase.valueOf(c[2]), Integer.parseInt(c[3])); // Crea.
            f.setFinalizada(Boolean.parseBoolean(c[4])); // Finalizada.
            ((CompetenciaFases) club.buscarCompetencia(c[0])).agregarFase(f); // Agrega a su competencia.
            participantesFase.put(c[0] + "/" + c[1], c[5]); // Guarda los participantes para después.
        }
        for (String[] c : leer(carpeta, "grupos.txt")) buscarFase(club, c[0], c[1]).agregarGrupo(new Grupo(c[2], partir(c[3]))); // Grupos.
        for (Map.Entry<String, String> e : participantesFase.entrySet()) { // Participantes definitivos.
            String[] k = e.getKey().split("/", 2); // Separa competencia y fase.
            buscarFase(club, k[0], k[1]).setParticipantes(partir(e.getValue())); // Los asigna.
        }
        for (String[] c : leer(carpeta, "inscripciones_competencia.txt")) { // Inscripciones a competencias.
            Competencia comp = club.buscarCompetencia(c[0]); // Competencia.
            List<Socio> nomina = new ArrayList<>(); for (String id : partir(c[2])) nomina.add(club.buscarSocio(id)); // Nómina.
            InscripcionCompetencia ins = new InscripcionCompetencia(comp, club.buscarEquipo(c[1]), nomina); // Crea.
            for (String par : partir(c[3])) { String[] p = par.split(":"); ins.getAmarillasAcumuladas().put(club.buscarSocio(p[0]), Integer.parseInt(p[1])); } // Amarillas.
            for (String id : partir(c[4])) ins.getSuspendidos().add(club.buscarSocio(id)); // Suspendidos.
            comp.agregarInscripcionCargada(ins); // Agrega.
        }
        for (String[] c : leer(carpeta, "resultados_externos.txt")) { // Resultados externos.
            club.buscarCompetencia(c[0]).registrarResultadoExterno(new Enfrentamiento(c[1], c[2], Integer.parseInt(c[3]), Integer.parseInt(c[4]), lleno(c[5]), lleno(c[6]))); // Agrega.
        }

        // ---- partidos y estadísticas ----
        for (String[] c : leer(carpeta, "partidos.txt")) { // Partidos.
            Partido p; int n = c.length; // Partido y número de campos.
            if ("INTERNO".equals(c[0])) { // Interno.
                PartidoInterno pi = new PartidoInterno(Integer.parseInt(c[1]), LocalDate.parse(c[2]), club.buscarEquipo(c[3]), club.buscarEquipo(c[4])); // Crea.
                club.getPartidosInternos().add(pi); p = pi; // Agrega.
            } else { // De competencia.
                Competencia comp = club.buscarCompetencia(c[3]); // Competencia.
                PartidoCompetencia pc = new PartidoCompetencia(Integer.parseInt(c[1]), LocalDate.parse(c[2]), comp.buscarInscripcion(club.buscarEquipo(c[4])), c[5], c[6], lleno(c[7]), lleno(c[8])); // Crea.
                comp.agregarPartido(pc); p = pc; // Agrega.
            }
            p.fijarResultadoCargado(Integer.parseInt(c[n - 3]), Integer.parseInt(c[n - 2]), Boolean.parseBoolean(c[n - 1])); // Resultado (últimos 3 campos).
        }
        for (String[] c : leer(carpeta, "estadisticas_partido.txt")) { // Estadísticas.
            Partido p = club.buscarPartido(Integer.parseInt(c[0])); Socio s = club.buscarSocio(c[2]); EstadisticaPartido e; // Datos.
            switch (c[1]) { // Según el deporte.
                case "FUTBOL": e = new EstadisticaFutbol(s, Integer.parseInt(c[3]), Integer.parseInt(c[4]), Integer.parseInt(c[5]), Boolean.parseBoolean(c[6])); break; // Fútbol.
                case "BALONCESTO": e = new EstadisticaBaloncesto(s, Integer.parseInt(c[3]), Integer.parseInt(c[4])); break; // Baloncesto.
                default: e = new EstadisticaVoleibol(s, Integer.parseInt(c[3]), Integer.parseInt(c[4]));                  // Voleibol.
            }
            p.agregarEstadisticaCargada(e); // Agrega.
        }

        // ---- reservas e inscripciones a disciplinas ----
        for (String[] c : leer(carpeta, "reservas.txt")) { // Reservas.
            Instalacion i = club.buscarInstalacion(c[1]); // Instalación.
            i.agregarReservaCargada(new Reserva(Integer.parseInt(c[0]), i, club.buscarSocio(c[2]), LocalDateTime.parse(c[3]), Integer.parseInt(c[4]), c[5])); // Agrega.
        }
        for (String[] c : leer(carpeta, "inscripciones_disciplina.txt")) { // Inscripciones.
            InscripcionDisciplina i = new InscripcionDisciplina(club.buscarSocio(c[0]), Disciplina.valueOf(c[1]), Nivel.valueOf(c[2])); // Crea.
            i.setRankingElo(Double.parseDouble(c[3])); // Ranking.
            club.getInscripcionesDisciplina().add(i); // Agrega.
        }

        // ---- partidos de práctica ----
        Map<Integer, List<EstadisticaPractica>> statsPractica = new LinkedHashMap<>(); // id partido -> estadísticas.
        for (String[] c : leer(carpeta, "estadisticas_practica.txt")) { // Estadísticas.
            Socio s = club.buscarSocio(c[1]); EstadisticaPractica e; // Datos.
            if ("TENIS_PADEL".equals(c[4])) e = new EstadisticaTenisPadel(s, Integer.parseInt(c[5]), Integer.parseInt(c[6]), Integer.parseInt(c[7]), Integer.parseInt(c[8])); // Tenis/pádel.
            else e = new EstadisticaTenisMesa(s, Integer.parseInt(c[5]), Integer.parseInt(c[6])); // Tenis de mesa.
            e.setElo(Double.parseDouble(c[2]), Double.parseDouble(c[3])); // Evolución del ELO.
            statsPractica.computeIfAbsent(Integer.parseInt(c[0]), k -> new ArrayList<>()).add(e); // Agrupa por partido.
        }
        for (String[] c : leer(carpeta, "partidos_practica.txt")) { // Partidos.
            Disciplina d = Disciplina.valueOf(c[1]); // Disciplina.
            List<InscripcionDisciplina> jug = new ArrayList<>(); for (String id : partir(c[3])) jug.add(club.buscarInscripcionDisciplina(club.buscarSocio(id), d)); // Jugadores.
            PartidoPractica p = new PartidoPractica(Integer.parseInt(c[0]), d, LocalDate.parse(c[2]), jug); // Crea.
            if (Boolean.parseBoolean(c[4])) p.cargarResultado(statsPractica.getOrDefault(p.getId(), new ArrayList<>())); // Resultado.
            club.getPartidosPractica().add(p); // Agrega.
        }

        // ---- sesiones y asistencias ----
        for (String[] c : leer(carpeta, "sesiones.txt")) { // Sesiones.
            List<Socio> conv = new ArrayList<>(); for (String id : partir(c[5])) conv.add(club.buscarSocio(id)); // Convocados.
            SesionEntrenamiento s = new SesionEntrenamiento(Integer.parseInt(c[0]), LocalDateTime.parse(c[3]), Integer.parseInt(c[4]),
                    (Entrenador) club.buscarUsuario(c[1]), NULO.equals(c[2]) ? null : club.buscarInstalacion(c[2]), conv); // Crea.
            s.setFinalizada(Boolean.parseBoolean(c[6])); // Estado.
            club.getSesiones().add(s); // Agrega.
        }
        for (String[] c : leer(carpeta, "asistencias.txt")) { // Asistencias.
            club.buscarSesion(Integer.parseInt(c[0])).agregarAsistenciaCargada(new AsistenciaEvaluacion(club.buscarSocio(c[1]), EstadoAsistencia.valueOf(c[2]), Integer.parseInt(c[3]), lleno(c[4]))); // Agrega.
        }

        // ---- solicitudes de turno ----
        for (String[] c : leer(carpeta, "solicitudes_turno.txt")) { // Solicitudes.
            SolicitudCambioTurno s = new SolicitudCambioTurno(Integer.parseInt(c[0]), (Empleado) club.buscarUsuario(c[1]),
                    new Turno(c[2], LocalTime.parse(c[3]), LocalTime.parse(c[4])), new Turno(c[5], LocalTime.parse(c[6]), LocalTime.parse(c[7]))); // Crea.
            s.setEstado(EstadoSolicitud.valueOf(c[8])); // Estado.
            club.getSolicitudesTurno().add(s); // Agrega.
        }

        // ---- artículos, inventario y productos ----
        for (String[] c : leer(carpeta, "articulos.txt")) club.getArticulos().add(new Articulo(c[0], c[1], Double.parseDouble(c[2]))); // Artículos.
        for (String[] c : leer(carpeta, "inventario.txt")) club.buscarArticulo(c[0]).agregarInventario(c[1], Integer.parseInt(c[2])); // Inventario.
        for (String[] c : leer(carpeta, "productos_cafeteria.txt")) { // Productos.
            if ("BEBIDA".equals(c[0])) club.getProductosCafeteria().add(new Bebida(c[1], Double.parseDouble(c[2]), TipoBebida.valueOf(c[3]))); // Bebida.
            else club.getProductosCafeteria().add(new Snack(c[1], Double.parseDouble(c[2]), partir(c[3]))); // Snack.
        }

        // ---- ventas e ítems ----
        Map<Integer, List<String[]>> items = new LinkedHashMap<>(); // id venta -> ítems.
        for (String[] c : leer(carpeta, "items_venta.txt")) items.computeIfAbsent(Integer.parseInt(c[0]), k -> new ArrayList<>()).add(c); // Agrupa.
        for (String[] c : leer(carpeta, "ventas.txt")) { // Ventas.
            int id = Integer.parseInt(c[0]); LocalDate f = LocalDate.parse(c[2]); Usuario comp = club.buscarUsuario(c[3]); Venta v; // Datos.
            if ("TIENDA".equals(c[1])) { // Tienda.
                VentaTienda vt = new VentaTienda(id, f, comp); // Crea.
                if (!NULO.equals(c[10])) vt.setDescuentoAplicado(club.buscarCodigo(c[10])); // Código.
                for (String[] it : items.getOrDefault(id, new ArrayList<>())) vt.agregarItemCargado(new ItemVentaTienda(club.buscarArticulo(it[1]), Integer.parseInt(it[2]), Double.parseDouble(it[3]))); // Ítems.
                v = vt; // Listo.
            } else if ("CAFETERIA".equals(c[1])) { // Cafetería.
                VentaCafeteria vc = new VentaCafeteria(id, f, comp); // Crea.
                vc.fijarPropinaCargada(Double.parseDouble(c[10])); // Propina.
                for (String[] it : items.getOrDefault(id, new ArrayList<>())) vc.agregarItemCargado(new ItemVentaCafeteria(club.buscarProducto(it[1]), Integer.parseInt(it[2]), Double.parseDouble(it[3]))); // Ítems.
                v = vc; // Listo.
            } else { // Mensualidad.
                v = new VentaMensualidad(id, f, (Socio) comp, c[10], Double.parseDouble(c[11])); // Crea.
            }
            v.fijarValoresCargados(Double.parseDouble(c[4]), Double.parseDouble(c[5]), Double.parseDouble(c[6]), Double.parseDouble(c[7]),
                    Double.parseDouble(c[8]), Double.parseDouble(c[9]), true); // Valores guardados (las ventas guardadas están cerradas).
            club.getVentas().add(v); // Agrega.
        }

        // ---- sugerencias ----
        for (String[] c : leer(carpeta, "sugerencias.txt")) { // Sugerencias.
            Sugerencia s = new Sugerencia(Integer.parseInt(c[0]), (Empleado) club.buscarUsuario(c[1]), c[2], c[3], lleno(c[4]), Double.parseDouble(c[5]), LocalDate.parse(c[7])); // Crea.
            s.setEstado(EstadoSugerencia.valueOf(c[6])); // Estado.
            club.getSugerencias().add(s); // Agrega.
        }
        return club; // Devuelve el club reconstruido.
    }

    /** Busca una fase por competencia y nombre. */
    private Fase buscarFase(ClubDeportivo club, String competencia, String fase) throws Exception { // Recibe nombres.
        for (Fase f : ((CompetenciaFases) club.buscarCompetencia(competencia)).getFases()) if (f.getNombre().equals(fase)) return f; // Busca.
        throw new IOException("Fase no encontrada: " + competencia + "/" + fase); // Error de datos.
    }

    // =====================================================================
    //                         UTILIDADES DE ARCHIVO
    // =====================================================================

    /** Escribe un archivo: primera línea comentario con el formato y luego una línea por registro. */
    private void escribir(File carpeta, String nombre, String formato, List<String> lineas) throws IOException { // Recibe datos.
        try (BufferedWriter w = Files.newBufferedWriter(new File(carpeta, nombre).toPath(), StandardCharsets.UTF_8)) { // try-with-resources: cierra el archivo solo.
            w.write("# " + formato); // Comentario con el formato.
            w.newLine();             // Salto de línea.
            for (String linea : lineas) { w.write(linea); w.newLine(); } // Cada registro en su línea.
        }
    }

    /** Lee un archivo y devuelve sus registros como arreglos de campos (lista vacía si no existe). */
    private List<String[]> leer(File carpeta, String nombre) throws IOException { // Recibe carpeta y nombre.
        List<String[]> registros = new ArrayList<>(); // Resultado.
        File f = new File(carpeta, nombre);           // Archivo.
        if (!f.exists()) return registros;            // Si no existe, no hay registros.
        try (BufferedReader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) { // Abre en UTF-8.
            String linea; // Línea actual.
            while ((linea = r.readLine()) != null) {  // Lee hasta el final.
                if (linea.isBlank() || linea.startsWith("#")) continue; // Ignora vacías y comentarios.
                registros.add(linea.split(SEP, -1));  // Separa por ';' (el -1 conserva campos vacíos al final).
            }
        }
        return registros; // Devuelve.
    }

    /** Limpia un texto para que no rompa el formato (quita ';', '|' y saltos de línea). */
    private static String txt(String s) { // Recibe el texto.
        return s == null ? "" : s.replace(SEP, ",").replace(LISTA, "/").replace("\n", " ").replace("\r", " "); // Reemplazos.
    }

    /** Une una lista con '|' ("-" si está vacía). */
    private static String unir(List<String> lista) { // Recibe la lista.
        if (lista.isEmpty()) return NULO; // Vacía.
        List<String> limpia = new ArrayList<>(); for (String s : lista) limpia.add(txt(s)); // Limpia cada elemento.
        return String.join(LISTA, limpia); // Une.
    }

    /** Separa un campo de lista ("-" o vacío => lista vacía). */
    private static List<String> partir(String campo) { // Recibe el campo.
        if (campo == null || campo.isBlank() || NULO.equals(campo)) return new ArrayList<>(); // Vacía.
        return new ArrayList<>(Arrays.asList(campo.split("\\|"))); // Separa por '|' (se escapa porque es especial en regex).
    }

    /** Convierte "" en "-" al guardar. */
    private static String vacio(String s) { return s == null || s.isEmpty() ? NULO : s; } // Marca de vacío.

    /** Convierte "-" en "" al cargar. */
    private static String lleno(String s) { return NULO.equals(s) ? "" : s; } // Quita la marca.
}
