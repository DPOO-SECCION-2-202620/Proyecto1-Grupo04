package consola; // Paquete de consolas.

import java.time.LocalDateTime;                    // Fecha y hora.
import java.time.LocalTime;                        // Hora.
import java.util.Arrays;                           // Utilidades.
import logica.ClubDeportivo;                       // Fachada.
import modelo.entrenamiento.*;                     // Sesiones y turnos.
import modelo.enums.EstadoAsistencia;              // Estado de asistencia.
import modelo.usuarios.*;                          // Usuarios.

/** Demuestra: sesiones de entrenamiento, asistencia y evaluación, carga física y cambios de turno. */
public class DemoEntrenamientoYTurnos { // Clase DemoEntrenamientoYTurnos.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = ConsolaBase.cargarClub(); // Carga datos.
        Entrenador laura = (Entrenador) club.buscarUsuario("E002"); // Entrenadora.
        Entrenador andres = (Entrenador) club.buscarUsuario("E003"); // Entrenador.
        Fisioterapeuta paula = (Fisioterapeuta) club.buscarUsuario("F001"); // Fisioterapeuta.
        Administrador admin = (Administrador) club.buscarUsuario("A001"); // Administrador.
        Socio ana = club.buscarSocio("S001"), luis = club.buscarSocio("S002"), sofia = club.buscarSocio("S003"); // Socios.
        ConsolaBase.titulo("DEMO 5: ENTRENAMIENTOS, ASISTENCIA Y TURNOS"); // Título.

        ConsolaBase.seccion("Sesiones registradas"); // Sección.
        for (SesionEntrenamiento s : club.getSesiones()) { // Cada sesión.
            System.out.printf("  %s | asistencia %.0f%%%n", s, s.calcularPorcentajeAsistencia()); // Resumen.
            for (AsistenciaEvaluacion a : s.getAsistencias()) System.out.println("      " + a); // Asistencias.
        }

        ConsolaBase.seccion("Planear una sesión"); // Sección.
        ConsolaBase.intentar("Sesión en TEN-1 el 12/10 a las 7:30 (se cruza con la reserva de las 8:00)",
                () -> club.planearSesion(laura, LocalDateTime.of(2026, 10, 12, 7, 30), 60, "TEN-1", Arrays.asList(ana, luis))); // Rechazada.
        ConsolaBase.intentar("Sesión sin convocados", () -> club.planearSesion(laura, LocalDateTime.of(2026, 10, 13, 7, 0), 60, "TEN-3", Arrays.asList())); // Rechazada.
        SesionEntrenamiento nueva = club.planearSesion(laura, LocalDateTime.of(2026, 10, 13, 7, 0), 60, "TEN-3", Arrays.asList(ana, luis, sofia)); // Válida.
        System.out.println("  [OK] Planeada: " + nueva); // Muestra.
        ConsolaBase.intentar("Otra sesión en TEN-3 a las 7:30 (se cruza)", () -> club.planearSesion(andres, LocalDateTime.of(2026, 10, 13, 7, 30), 60, "TEN-3", Arrays.asList(ana))); // Rechazada.

        ConsolaBase.seccion("Asistencia y evaluación (solo al finalizar, solo convocados, nota 1-5)"); // Sección.
        ConsolaBase.intentar("Registrar asistencia antes de finalizar", () -> laura.registrarAsistenciaYEvaluacion(nueva, ana, EstadoAsistencia.ASISTIO, 5, "")); // Rechazada.
        nueva.finalizar(); // Finaliza.
        System.out.println("  (sesión finalizada)"); // Aviso.
        ConsolaBase.intentar("Ana ASISTIÓ, nota 5", () -> laura.registrarAsistenciaYEvaluacion(nueva, ana, EstadoAsistencia.ASISTIO, 5, "Gran avance en el revés")); // Válida.
        ConsolaBase.intentar("Luis llegó TARDE, sin evaluación", () -> laura.registrarAsistenciaYEvaluacion(nueva, luis, EstadoAsistencia.TARDE, 0, "")); // Válida.
        ConsolaBase.intentar("Sofía con nota 7", () -> laura.registrarAsistenciaYEvaluacion(nueva, sofia, EstadoAsistencia.ASISTIO, 7, "")); // Rechazada.
        ConsolaBase.intentar("Sofía FALTÓ", () -> laura.registrarAsistenciaYEvaluacion(nueva, sofia, EstadoAsistencia.FALTO, 0, "")); // Válida.
        ConsolaBase.intentar("Registrar a Mateo (no convocado)", () -> laura.registrarAsistenciaYEvaluacion(nueva, club.buscarSocio("S004"), EstadoAsistencia.ASISTIO, 3, "")); // Rechazada.
        ConsolaBase.intentar("Andrés registra asistencia en la sesión de Laura", () -> andres.registrarAsistenciaYEvaluacion(nueva, ana, EstadoAsistencia.ASISTIO, 4, "")); // Rechazada.
        for (AsistenciaEvaluacion a : nueva.getAsistencias()) System.out.println("      " + a); // Resultado.

        ConsolaBase.seccion("Carga física consultada por la fisioterapeuta"); // Sección.
        for (Socio s : Arrays.asList(ana, luis, sofia)) { // Para tres socios.
            System.out.println("  " + s.getNombre() + ": " + paula.consultarCargaFisica(s, club.getSesiones()).size() + " sesiones, "
                    + paula.calcularMinutosDeCarga(s, club.getSesiones()) + " minutos"); // Carga.
        }

        ConsolaBase.seccion("Turnos y regla de cobertura (mín. 2 entrenadores y 1 fisioterapeuta)"); // Sección.
        for (Empleado e : club.getEmpleados()) System.out.println("  " + e.getNombre() + ": " + e.getTurnos().get(0) + (e.getTurnos().size() > 1 ? " ... (" + e.getTurnos().size() + " turnos)" : "")); // Turnos.
        System.out.println("  Cobertura actual: " + club.verificarCobertura()); // Verificación.
        Empleado felipe = (Empleado) club.buscarUsuario("E005"); // Refuerzo.
        SolicitudCambioTurno s1 = club.solicitarCambioTurno(felipe, new Turno("LUNES", LocalTime.of(10, 0), LocalTime.of(18, 0)), new Turno("MARTES", LocalTime.of(10, 0), LocalTime.of(18, 0))); // Solicitud válida.
        System.out.println("  " + s1 + " -> aprobada? " + club.aprobarCambioTurno(admin, s1)); // Aprobada.
        Empleado carlos = (Empleado) club.buscarUsuario("E001"); // Entrenador de la mañana.
        SolicitudCambioTurno s2 = club.solicitarCambioTurno(carlos, new Turno("LUNES", LocalTime.of(6, 0), LocalTime.of(14, 0)), new Turno("LUNES", LocalTime.of(14, 0), LocalTime.of(22, 0))); // Dejaría la mañana con 1 entrenador.
        System.out.println("  " + s2 + " -> aprobada? " + club.aprobarCambioTurno(admin, s2)); // Rechazada.
        System.out.println("  Estado final: " + s2); // Estado.
        ConsolaBase.intentar("Revisar de nuevo la solicitud ya rechazada", () -> club.aprobarCambioTurno(admin, s2)); // Rechazado.
        SolicitudCambioTurno s3 = club.solicitarCambioTurno(carlos, new Turno("DOMINGO", LocalTime.of(6, 0), LocalTime.of(14, 0)), new Turno("LUNES", LocalTime.of(6, 0), LocalTime.of(14, 0))); // Turno que no tiene.
        ConsolaBase.intentar("Solicitud sobre un turno que el empleado no tiene", () -> club.aprobarCambioTurno(admin, s3)); // Rechazado.
        System.out.println("  Cobertura tras los cambios: " + club.verificarCobertura()); // Sigue OK.

        ConsolaBase.guardarResultado(club, "demo5_entrenamiento"); // Guarda.
    }
}
