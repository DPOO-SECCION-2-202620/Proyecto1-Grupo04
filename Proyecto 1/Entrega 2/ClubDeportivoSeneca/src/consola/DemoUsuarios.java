package consola; // Paquete de consolas.

import logica.ClubDeportivo;     // Fachada.
import modelo.usuarios.Socio;    // Socio.
import modelo.usuarios.Usuario;  // Usuario.

/** Demuestra: login/password de todos los usuarios, tipos de usuario y puntos de fidelidad. */
public class DemoUsuarios { // Clase DemoUsuarios.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = ConsolaBase.cargarClub(); // Carga los datos.
        ConsolaBase.titulo("DEMO 1: USUARIOS, AUTENTICACIÓN Y PUNTOS DE FIDELIDAD"); // Título.

        ConsolaBase.seccion("Usuarios registrados"); // Sección.
        for (Usuario u : club.getUsuarios()) System.out.printf("  %-34s login=%-15s puntos=%,.0f%n", u, u.getLogin(), u.getPuntosFidelidad()); // Lista.

        ConsolaBase.seccion("Autenticación"); // Sección.
        ConsolaBase.intentar("admin / admin123", () -> System.out.println("     Entró: " + club.autenticar("admin", "admin123"))); // Correcto.
        ConsolaBase.intentar("lgomez / ent123", () -> System.out.println("     Entró: " + club.autenticar("lgomez", "ent123")));   // Correcto.
        ConsolaBase.intentar("s001 / clave-equivocada", () -> club.autenticar("s001", "clave-equivocada"));                       // Incorrecto.

        ConsolaBase.seccion("Puntos de fidelidad (2% de cada compra, 1 punto = 1 peso)"); // Sección.
        Socio ana = club.buscarSocio("S001"); // Ana.
        System.out.printf("  %s tiene %,.2f puntos (acumulados por sus compras de prueba).%n", ana.getNombre(), ana.getPuntosFidelidad()); // Puntos actuales.
        ana.acumularPuntos(100000); // Simula una compra de 100.000.
        System.out.printf("  Tras una compra de $100.000 tiene %,.2f puntos (+2.000).%n", ana.getPuntosFidelidad()); // Muestra.
        double redimidos = ana.redimirPuntos(1500); // Redime hasta 1.500.
        System.out.printf("  Redime $%,.0f en una compra; le quedan %,.2f puntos.%n", redimidos, ana.getPuntosFidelidad()); // Muestra.

        ConsolaBase.seccion("Restricción del administrador"); // Sección.
        Usuario admin = club.buscarUsuario("A001"); // Administrador.
        System.out.println("  ¿El administrador puede reservar o inscribirse a partidos? " + admin.puedeUsarInstalacionesYPartidos()); // false.

        ConsolaBase.guardarResultado(club, "demo1_usuarios"); // Guarda el estado.
    }
}
