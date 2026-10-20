# Club Deportivo Séneca — Proyecto 1 (DPOO)
Implementación en Java del diagrama de clases de la Entrega 1 (lógica + persistencia + programas de demostración).


## Cómo ejecutarlo
Todos los programas están en el paquete `consola` (clic derecho > *Run As > Java Application*):

| Programa | Qué demuestra |
|---|---|
| `MenuDemostraciones` | Pide solo el número de la demostración (0–9) |
| `GeneradorDatosPrueba` | Regenera los archivos de prueba de la carpeta `datos/` |
| `DemoUsuarios` | Login/password, tipos de usuario, puntos de fidelidad, restricción del administrador |
| `DemoEquiposYPartidos` | Inscripción con reglas (cupo, edad/categoría, un equipo por deporte, camiseta única), partidos internos, mínimo de habilitados, suspensiones por tarjetas, empates |
| `DemoCompetencias` | Liga (tabla de posiciones, goleadores, destacado), nómina, amarillas acumuladas en la competencia, competencia por fases (grupos con enfrentamiento directo, semifinal, final y campeón) |
| `DemoAcademiasYReservas` | Reservas (capacidad/modalidad, bloque ocupado, máximo simultáneo por disciplina), bebidas calientes, partidos de práctica por niveles, excepción del entrenador, ELO |
| `DemoEntrenamientoYTurnos` | Sesiones, asistencia y evaluación, carga física, cambios de turno con la regla de 2 entrenadores + 1 fisioterapeuta |
| `DemoTiendaYCafeteria` | IVA 19 %, inventario, código de empleado 15 %/8 %, no acumulable, puntos, alérgenos, impuesto 8 % y propina |
| `DemoAdministrador` | Inventario, mover/reabastecer, sugerencias, crear/dar de baja equipos y jugadores, reportes de ventas y deportivos, historial |
| `DemoPersistencia` | Cargar → modificar → guardar → recargar y comparar |

Cada demo **carga** los datos de `datos/` y **guarda** su estado final en `datos_resultado/<demo>` para no alterar los datos de prueba (así se pueden correr las demos en cualquier orden y repetirlas).

## Estructura
```
src/
  excepciones/   ReglaNegocioException
  modelo/enums/          Categoria, Deporte, Disciplina, Nivel, EstadoAsistencia, TipoBebida, EstadoSolicitud, EstadoSugerencia, TipoFase
  modelo/usuarios/       Usuario, Socio, Empleado, Entrenador, Fisioterapeuta, Administrador
  modelo/conjunto/       Equipo, InscripcionEquipo, Partido, PartidoInterno, PartidoCompetencia, EstadisticaPartido (+Futbol, Baloncesto, Voleibol)
  modelo/competencias/   Competencia, CompetenciaLiga, CompetenciaFases, InscripcionCompetencia, Fase, Grupo, PosicionTabla, Enfrentamiento
  modelo/academias/      Instalacion (+CanchaTenis, CanchaPadel, MesaDeTenisMesa), Reserva, InscripcionDisciplina, PartidoPractica, EstadisticaPractica (+TenisPadel, TenisMesa)
  modelo/entrenamiento/  SesionEntrenamiento, AsistenciaEvaluacion, Turno, SolicitudCambioTurno
  modelo/ventas/         Venta (+Tienda, Cafeteria, Mensualidad), Articulo, InventarioUbicacion, CodigoDescuento, ItemVentaTienda, ItemVentaCafeteria, ProductoCafeteria (+Bebida, Snack), Sugerencia
  modelo/reportes/       Reporte, ReporteVentas, ReporteDeportivo
  logica/        ClubDeportivo (fachada con todas las colecciones y las reglas que cruzan varias clases)
  persistencia/  PersistenciaClub (archivos de texto)
  consola/       programas de demostración
datos/           archivos de prueba (fuera de src, como pide el enunciado)
```

## Persistencia
Archivos de texto en `datos/`, uno por tipo de información (`usuarios.txt`, `equipos.txt`, `partidos.txt`, `ventas.txt`, ...).
Campos separados por `;`, listas por `|`, `-` = vacío; la primera línea de cada archivo (`# ...`) documenta su formato.
Las referencias entre objetos se guardan por identificador (id de usuario, nombre de equipo, id de partido...).

## Usuarios de prueba
| Rol | Login | Password |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Entrenadores | `cmendez`, `lgomez`, `apardo`, `dtorres`, `fcastro` | `ent123` |
| Fisioterapeutas | `prios`, `jsilva` | `fis123` |
| Socios | `s001` … `s020`, `carlos.jugador` | `1234` |

## Supuestos de diseño
- Categorías por franja cerrada: Sub-13 (≤12 años), Sub-15 (13–14), Sub-17 (15–16), Mayores (≥17).
- En liga, victoria = 3 puntos en los tres deportes; empate = 1 (solo fútbol). Baloncesto y voleibol no admiten empates.
- Suspensión en partidos internos: 2 amarillas o 1 roja en un partido ⇒ fuera del siguiente partido interno del equipo.
- Suspensión en competencias: roja, o cada vez que el acumulado de amarillas en la competencia llega a 2, 4, 6… ⇒ fuera del siguiente partido oficial.
- Desempate en grupos: puntos → enfrentamiento directo → diferencia de goles → goles a favor.
- En eliminación directa el marcador registrado debe definir un ganador (incluir penales si aplica).
- Reservas por bloques fijos de 60 minutos; máximo 2 instalaciones simultáneas por disciplina (configurable en `config.txt`).
- ELO simplificado: K = 32, ranking inicial 1000; en dobles se usa el promedio de cada pareja.
- El código de descuento de empleado no se acumula con la redención de puntos.
- Puntos de fidelidad = 2 % del valor pagado sin propina. Mensualidades sin impuesto.
- Propina sugerida = 10 % del consumo antes de impuesto.
- Horario de atención: lunes a sábado de 6:00 a 22:00 (configurable).

