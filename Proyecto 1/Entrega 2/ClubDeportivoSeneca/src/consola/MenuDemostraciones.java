package consola; // Paquete de consolas.

import java.util.Scanner; // Lectura del teclado.

/** Menú que pide solo el número de la funcionalidad a demostrar (mínima información del usuario). */
public class MenuDemostraciones { // Clase MenuDemostraciones.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        String opcion; // Opción elegida.
        if (args.length > 0) { // Si se pasó por argumento (Run Configurations > Arguments)...
            opcion = args[0]; // ...se usa ese.
        } else { // Si no...
            System.out.println("CLUB DEPORTIVO SÉNECA - Programas de demostración"); // Encabezado.
            System.out.println(" 0. Regenerar datos de prueba (carpeta 'datos')");     // Opciones.
            System.out.println(" 1. Usuarios, autenticación y puntos");
            System.out.println(" 2. Equipos, jugadores y partidos internos");
            System.out.println(" 3. Competencias (liga y fases)");
            System.out.println(" 4. Academias, reservas y partidos de práctica");
            System.out.println(" 5. Entrenamientos, asistencia y turnos");
            System.out.println(" 6. Tienda, cafetería y puntos");
            System.out.println(" 7. Administrador (inventario, sugerencias, reportes)");
            System.out.println(" 8. Persistencia");
            System.out.println(" 9. Todas");
            System.out.print("Escriba el número de la demostración: "); // Pregunta.
            Scanner sc = new Scanner(System.in); // Lector del teclado.
            opcion = sc.nextLine().trim();       // Lee la línea.
        }
        ejecutar(opcion); // Ejecuta la opción.
    }

    /** Ejecuta la demostración indicada. */
    static void ejecutar(String opcion) throws Exception { // Recibe la opción.
        String[] vacio = new String[0]; // Argumentos vacíos para los main.
        switch (opcion) { // Según la opción.
            case "0": GeneradorDatosPrueba.main(vacio); break;     // Regenerar.
            case "1": DemoUsuarios.main(vacio); break;             // Demo 1.
            case "2": DemoEquiposYPartidos.main(vacio); break;     // Demo 2.
            case "3": DemoCompetencias.main(vacio); break;         // Demo 3.
            case "4": DemoAcademiasYReservas.main(vacio); break;   // Demo 4.
            case "5": DemoEntrenamientoYTurnos.main(vacio); break; // Demo 5.
            case "6": DemoTiendaYCafeteria.main(vacio); break;     // Demo 6.
            case "7": DemoAdministrador.main(vacio); break;        // Demo 7.
            case "8": DemoPersistencia.main(vacio); break;         // Demo 8.
            case "9": for (int i = 1; i <= 8; i++) ejecutar(String.valueOf(i)); break; // Todas.
            default: System.out.println("Opción no válida: " + opcion); // Error.
        }
    }
}
