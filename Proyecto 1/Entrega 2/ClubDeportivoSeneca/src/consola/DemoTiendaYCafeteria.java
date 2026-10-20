package consola; // Paquete de consolas.

import java.time.LocalDate;          // Fecha.
import logica.ClubDeportivo;         // Fachada.
import modelo.usuarios.*;            // Usuarios.
import modelo.ventas.*;              // Ventas.

/** Demuestra: ventas de tienda (IVA, inventario, código de empleado) y cafetería (alérgenos, propina, impuesto), y puntos. */
public class DemoTiendaYCafeteria { // Clase DemoTiendaYCafeteria.

    public static void main(String[] args) throws Exception { // Punto de entrada.
        ClubDeportivo club = ConsolaBase.cargarClub(); // Carga datos.
        LocalDate hoy = LocalDate.of(2026, 10, 10); // Fecha de las ventas de la demo.
        Socio mateo = club.buscarSocio("S004"); // Socio.
        Entrenador laura = (Entrenador) club.buscarUsuario("E002"); // Empleada.
        CodigoDescuento codigoLaura = club.buscarCodigoDeEmpleado(laura); // Su código.
        ConsolaBase.titulo("DEMO 6: TIENDA, CAFETERÍA Y PUNTOS DE FIDELIDAD"); // Título.

        ConsolaBase.seccion("Catálogo"); // Sección.
        for (Articulo a : club.getArticulos()) System.out.println("  " + a); // Artículos.
        for (ProductoCafeteria p : club.getProductosCafeteria()) System.out.println("  " + p); // Productos.

        ConsolaBase.seccion("Tienda: inventario e IVA 19%"); // Sección.
        VentaTienda v1 = club.iniciarVentaTienda(mateo, hoy); // Venta.
        ConsolaBase.intentar("Agregar 6 raquetas (solo hay 4 en tienda)", () -> v1.agregarItem(club.buscarArticulo("Raqueta Wilson Pro"), 6)); // Rechazado.
        ConsolaBase.intentar("Agregar 1 malla de voleibol (0 en tienda, están en bodega)", () -> v1.agregarItem(club.buscarArticulo("Malla Voleibol"), 1)); // Rechazado.
        ConsolaBase.intentar("Agregar 2 uniformes y 1 tubo de pelotas", () -> { v1.agregarItem(club.buscarArticulo("Uniforme Séneca"), 2); v1.agregarItem(club.buscarArticulo("Tubo Pelotas Tenis"), 1); }); // Válido.
        int antes = club.buscarArticulo("Uniforme Séneca").getCantidadEn(Articulo.TIENDA); // Inventario antes.
        ConsolaBase.intentar("Confirmar la venta", () -> club.confirmarVenta(v1)); // Cierra.
        for (ItemVentaTienda i : v1.getItems()) System.out.println("     " + i); // Ítems.
        System.out.println("     " + v1.generarDetalle()); // Detalle contable.
        System.out.println("     Uniformes en tienda: " + antes + " -> " + club.buscarArticulo("Uniforme Séneca").getCantidadEn(Articulo.TIENDA)); // Inventario.

        ConsolaBase.seccion("Código de descuento del empleado: 15% para él, 8% para quien lo comparta"); // Sección.
        VentaTienda v2 = club.iniciarVentaTienda(laura, hoy); // Laura compra.
        v2.agregarItem(club.buscarArticulo("Balón Fútbol Golty"), 1); v2.aplicarDescuento(codigoLaura); club.confirmarVenta(v2); // Con su código.
        System.out.println("  Laura (dueña del código " + codigoLaura.getCodigo() + "):\n     " + v2.generarDetalle()); // 15%.
        VentaTienda v3 = club.iniciarVentaTienda(mateo, hoy); // Mateo usa el código de Laura.
        v3.agregarItem(club.buscarArticulo("Balón Fútbol Golty"), 1); v3.aplicarDescuento(club.buscarCodigo("EMP-E002")); club.confirmarVenta(v3); // 8%.
        System.out.println("  Mateo con el código compartido:\n     " + v3.generarDetalle()); // 8%.
        ConsolaBase.intentar("Aplicar un código y además redimir puntos (no acumulable)", () -> { // Rechazado.
            VentaTienda v = club.iniciarVentaTienda(mateo, hoy); v.agregarItem(club.buscarArticulo("Tubo Pelotas Tenis"), 1); // Venta.
            v.aplicarDescuento(codigoLaura); v.usarPuntosFidelidad(); // Código + puntos.
        });
        ConsolaBase.intentar("Aplicar dos códigos a la misma venta", () -> { // Rechazado.
            VentaTienda v = club.iniciarVentaTienda(mateo, hoy); v.agregarItem(club.buscarArticulo("Tubo Pelotas Tenis"), 1); // Venta.
            v.aplicarDescuento(codigoLaura); v.aplicarDescuento(club.buscarCodigo("EMP-E003")); // Dos códigos.
        });

        ConsolaBase.seccion("Redención de puntos de fidelidad"); // Sección.
        System.out.printf("  Mateo tiene %,.2f puntos.%n", mateo.getPuntosFidelidad()); // Puntos.
        VentaTienda v4 = club.iniciarVentaTienda(mateo, hoy); // Nueva venta.
        v4.agregarItem(club.buscarArticulo("Tubo Pelotas Tenis"), 1); v4.usarPuntosFidelidad(); club.confirmarVenta(v4); // Usa puntos.
        System.out.println("     " + v4.generarDetalle()); // Detalle.
        System.out.printf("  Ahora tiene %,.2f puntos (gastó los suyos y ganó el 2%% de esta compra).%n", mateo.getPuntosFidelidad()); // Puntos.

        ConsolaBase.seccion("Cafetería: alérgenos, impuesto al consumo 8% y propina sugerida 10%"); // Sección.
        VentaCafeteria c1 = club.iniciarVentaCafeteria(mateo, hoy); // Pedido.
        c1.agregarItem(club.buscarProducto("Barra de maní"), 1); c1.agregarItem(club.buscarProducto("Café americano"), 2); // Ítems.
        ConsolaBase.intentar("Cerrar sin informar los alérgenos", () -> club.confirmarVenta(c1)); // Rechazado.
        System.out.print("  Aviso al socio:\n" + c1.informarAlergenos()); // Informa.
        ConsolaBase.intentar("Cerrar después de informar (propina sugerida)", () -> club.confirmarVenta(c1)); // Válido.
        System.out.println("     " + c1.generarDetalle()); // Detalle.
        VentaCafeteria c2 = club.iniciarVentaCafeteria(club.buscarSocio("S011"), hoy); // Otro pedido.
        c2.agregarItem(club.buscarProducto("Fruta picada"), 2); // Sin alérgenos.
        ConsolaBase.intentar("Ajustar propina a -500", () -> c2.ajustarPropina(-500)); // Rechazado.
        c2.ajustarPropina(2000); club.confirmarVenta(c2); // Propina personalizada.
        System.out.println("  Propina modificada por el socio a $2.000:\n     " + c2.generarDetalle()); // Detalle.

        ConsolaBase.seccion("Todas las ventas registradas (fecha y rubros)"); // Sección.
        for (Venta v : club.getVentas()) System.out.println("  " + v.generarDetalle()); // Ventas.

        ConsolaBase.guardarResultado(club, "demo6_tienda"); // Guarda.
    }
}
