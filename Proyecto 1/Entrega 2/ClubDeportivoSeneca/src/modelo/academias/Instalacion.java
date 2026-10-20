package modelo.academias; // Paquete de academias.

import java.time.LocalDateTime;           // Fecha y hora.
import java.util.ArrayList;               // Lista dinámica.
import java.util.List;                    // Lista.
import excepciones.ReglaNegocioException; // Excepción de negocio.
import modelo.enums.Disciplina;           // Disciplina.
import modelo.usuarios.Socio;             // Socio.

/** Instalación de una academia (cancha de tenis, cancha de pádel o mesa de tenis de mesa). */
public abstract class Instalacion { // Clase abstracta.

    public static final String SENCILLOS = "SENCILLOS"; // Constante de modalidad sencillos.
    public static final String DOBLES = "DOBLES";       // Constante de modalidad dobles.

    private String id;               // Identificador (ej. "TEN-1").
    private int capacidadMaxima;     // Máximo de jugadores que admite.
    private List<Reserva> reservas;  // Reservas de la instalación.

    public Instalacion(String id, int capacidadMaxima) { // Constructor.
        this.id = id;                         // Guarda el id.
        this.capacidadMaxima = capacidadMaxima; // Guarda la capacidad.
        this.reservas = new ArrayList<>();    // Sin reservas.
    }

    /** Disciplina a la que pertenece la instalación. */
    public abstract Disciplina getDisciplina(); // Abstracto.

    /** Reglas de cantidad de jugadores y modalidad de cada tipo de instalación. */
    public abstract boolean admiteJugadores(int cantidad, String modalidad); // Abstracto.

    /** Tipo de instalación en texto (para persistencia y reportes). */
    public abstract String getTipo(); // Abstracto.

    /** Por defecto se permite estar con bebida caliente (pádel y mesa techada lo prohíben). */
    public boolean permiteBebidaCaliente() { return true; } // Comportamiento por defecto.

    /** ¿Hay alguna reserva que se cruce con el bloque que inicia en "inicio"? */
    public boolean estaOcupada(LocalDateTime inicio) {
        for (Reserva r : reservas) {
            if (r.seCruzaCon(inicio)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reserva la instalación. Valida cantidad/modalidad, disponibilidad del bloque y la
     * regla de bebidas calientes. (El cupo máximo simultáneo por disciplina lo valida ClubDeportivo.)
     */
    public Reserva reservar(int idReserva, Socio socio, LocalDateTime fechaHora, int cantidadJugadores, String modalidad)
            throws ReglaNegocioException {
        if (!admiteJugadores(cantidadJugadores, modalidad)) {
            throw new ReglaNegocioException(id + " no admite " + cantidadJugadores + " jugadores en modalidad " + modalidad + ".");
        }
        if (estaOcupada(fechaHora)) {
            throw new ReglaNegocioException(id + " ya está reservada en ese bloque horario.");
        }
        if (socio.tieneBebidaCaliente() && !permiteBebidaCaliente()) {
            throw new ReglaNegocioException("No se puede reservar " + id + " con una bebida caliente: está prohibida en esta instalación.");
        }
        Reserva reserva = new Reserva(idReserva, this, socio, fechaHora, cantidadJugadores, modalidad);
        reservas.add(reserva);
        return reserva;
    }

    /** Agrega una reserva cargada desde archivo. */
    public void agregarReservaCargada(Reserva r) { reservas.add(r); } // Agrega directo.

    public String getId() { return id; }                       // Getter.
    public int getCapacidadMaxima() { return capacidadMaxima; } // Getter.
    public List<Reserva> getReservas() { return reservas; }    // Getter.

    @Override // Texto.
    public String toString() { return getTipo() + " " + id + " (cap. " + capacidadMaxima + ", " + reservas.size() + " reservas)"; }
}
