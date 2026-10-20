package modelo.usuarios; // Paquete de usuarios.

import java.time.LocalTime;            // Hora del día sin fecha.
import java.util.ArrayList;            // Implementación de lista dinámica.
import java.util.List;                 // Interfaz de lista.
import modelo.entrenamiento.Turno;     // Turno semanal de trabajo.

/**
 * Empleado del club (entrenador o fisioterapeuta). Tiene turnos semanales
 * y puede tener un código de descuento para la tienda.
 */
public abstract class Empleado extends Usuario { // Abstracta: solo existen Entrenador y Fisioterapeuta.

    private List<Turno> turnos; // Turnos semanales asignados al empleado.

    public Empleado(String id, String nombre, String login, String password) { // Constructor.
        super(id, nombre, login, password); // Inicializa la parte de Usuario.
        this.turnos = new ArrayList<>();    // Empieza sin turnos.
    }

    /** Agrega un turno al empleado. */
    public void agregarTurno(Turno t) { // Recibe el turno.
        turnos.add(t); // Lo agrega a la lista.
    }

    /** Indica si el empleado está trabajando en un día y hora dados. */
    public boolean estaDisponible(String dia, LocalTime hora) {
        for (Turno t : turnos) {
            if (t.cubre(dia, hora)) {
                return true;
            }
        }
        return false;
    }

    public List<Turno> getTurnos() { return turnos; } // Devuelve la lista de turnos (se modifica al aprobar cambios).
}
