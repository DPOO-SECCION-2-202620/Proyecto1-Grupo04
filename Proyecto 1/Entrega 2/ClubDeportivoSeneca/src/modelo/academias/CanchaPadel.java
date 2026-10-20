package modelo.academias; // Paquete de academias.

import modelo.enums.Disciplina; // Disciplina.

/** Cancha de pádel: siempre dobles (4 jugadores). No se permiten bebidas calientes. */
public class CanchaPadel extends Instalacion { // Hereda de Instalacion.

    public CanchaPadel(String id) { super(id, 4); } // Capacidad 4.

    @Override public Disciplina getDisciplina() { return Disciplina.PADEL; } // Disciplina pádel.
    @Override public String getTipo() { return "CANCHA_PADEL"; }             // Tipo.
    @Override public boolean permiteBebidaCaliente() { return false; }       // Regla de seguridad.

    @Override // Regla de cantidad.

    public boolean admiteJugadores(int cantidad, String modalidad) {
        return cantidad == 4 && DOBLES.equals(modalidad);
    }
}
