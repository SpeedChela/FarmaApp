package medicamentos;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Sales helper: finalizar ventas, generar PDF y registrar cancelaciones.
 * Requiere: org.apache.pdfbox:pdfbox y fontbox en el classpath.
 */
public class SalesManager {

    // DTO para items de venta (incluye nombre y gramaje)
    public static class SaleItem {
        public String codBarras;
        public int cantidad;
        public double precioUnitario;
        public double subtotal;
        public String nombre;
        public String gramaje;

        public SaleItem(String codBarras, int cantidad, double precioUnitario, String nombre, String gramaje) {
            this.codBarras = codBarras;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = Math.round(cantidad * precioUnitario * 100.0) / 100.0;
            this.nombre = nombre == null ? "" : nombre;
            this.gramaje = gramaje == null ? "" : gramaje;
        }
    }

    /**
     * Finaliza una venta: inserta Tickets y Ticket_Detalle, actualiza stock, genera PDF y guarda ruta en Tickets.ruta_pdf.
     * Devuelve el id del ticket creado o -1 en error.
     *
     * vendedorNombre puede ser null; si no es null aparecerá en el PDF.
     * carpetaSalida: si null -> se usa AppPaths.getStoredOrDefaultTicketsDir() (y se crea si es necesario).
     */
    public static long finalizarVenta(List<SaleItem> items, int idUsuario, String carpetaSalida, String vendedorNombre) {
        Connection conn = null;
        PreparedStatement psTicket = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psUpdateStock = null;
        ResultSet rs = null;

        long idTicket = -1;
        double totalFinal = 0.0;

        for (SaleItem it : items) totalFinal += it.subtotal;
        totalFinal = Math.round(totalFinal * 100.0) / 100.0;

        try {
            conn = Conexion.conectar();
            conn.setAutoCommit(false);

            // Validar stock antes de insertar (evita vender más de lo disponible)
            String sqlCheckStock = "SELECT stock FROM Productos WHERE cod_barras = ? LIMIT 1";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckStock)) {
                for (SaleItem it : items) {
                    psCheck.setString(1, it.codBarras);
                    try (ResultSet rsc = psCheck.executeQuery()) {
                        if (!rsc.next()) {
                            throw new SQLException("Producto no encontrado: " + it.codBarras);
                        }
                        int stock = rsc.getInt("stock");
                        if (stock < it.cantidad) {
                            throw new SQLException("Stock insuficiente para " + it.codBarras + " (disponible: " + stock + ", pedido: " + it.cantidad + ")");
                        }
                    }
                }
            }

            // 1) Insert ticket
            String sqlTicket = "INSERT INTO Tickets (fecha, id_usuario, total_final, ruta_pdf, estado) VALUES (?, ?, ?, ?, ?)";
            psTicket = conn.prepareStatement(sqlTicket, Statement.RETURN_GENERATED_KEYS);
            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date());
            psTicket.setString(1, fecha);
            psTicket.setInt(2, idUsuario);
            psTicket.setDouble(3, totalFinal);
            psTicket.setString(4, null);
            psTicket.setString(5, "Activo");
            int t = psTicket.executeUpdate();
            if (t == 0) throw new SQLException("No se pudo crear ticket");
            rs = psTicket.getGeneratedKeys();
            if (rs.next()) idTicket = rs.getLong(1);
            else throw new SQLException("No se pudo obtener id del ticket");

            // 2) Insert detalles y actualizar stock
            String sqlDetalle = "INSERT INTO Ticket_Detalle (id_ticket, cod_barras, cantidad_vendida, precio_unitario_venta, subtotal, cantidad_devuelta) VALUES (?, ?, ?, ?, ?, ?)";
            psDetalle = conn.prepareStatement(sqlDetalle);

            String sqlUpdStock = "UPDATE Productos SET stock = stock - ? WHERE cod_barras = ?";
            psUpdateStock = conn.prepareStatement(sqlUpdStock);

            for (SaleItem it : items) {
                psDetalle.setLong(1, idTicket);
                psDetalle.setString(2, it.codBarras);
                psDetalle.setInt(3, it.cantidad);
                psDetalle.setDouble(4, it.precioUnitario);
                psDetalle.setDouble(5, it.subtotal);
                psDetalle.setInt(6, 0);
                psDetalle.executeUpdate();

                psUpdateStock.setInt(1, it.cantidad);
                psUpdateStock.setString(2, it.codBarras);
                psUpdateStock.executeUpdate();
            }

            // 3) Generar PDF y actualizar ruta en Tickets.ruta_pdf
            File dir;
            if (carpetaSalida != null && !carpetaSalida.trim().isEmpty()) {
                dir = new File(carpetaSalida);
                if (!dir.exists()) dir.mkdirs();
            } else {
                dir = AppPaths.getStoredOrDefaultTicketsDir();
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        // fallback a user.home/FarmaApp/tickets
                        dir = new File(System.getProperty("user.home"), "FarmaApp" + File.separator + "tickets");
                        dir.mkdirs();
                    }
                }
            }

            String nombrePDF = "ticket_" + idTicket + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
            File pdfFile = new File(dir, nombrePDF);

            generarPdfTicket(pdfFile.getAbsolutePath(), idTicket, fecha, vendedorNombre, items, totalFinal);

            // intentar restringir permisos POSIX (no crítico)
            try {
                java.nio.file.Path path = pdfFile.toPath();
                java.nio.file.attribute.PosixFileAttributeView view =
                    java.nio.file.Files.getFileAttributeView(path, java.nio.file.attribute.PosixFileAttributeView.class);
                if (view != null) {
                    java.util.Set<java.nio.file.attribute.PosixFilePermission> perms = java.util.EnumSet.of(
                        java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                        java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
                    );
                    java.nio.file.Files.setPosixFilePermissions(path, perms);
                }
            } catch (Throwable ignore) {
                // ignorar en Windows / si no soporta
            }

            // actualizar ruta_pdf en Tickets
            String sqlUpdatePdf = "UPDATE Tickets SET ruta_pdf = ? WHERE id = ?";
            try (PreparedStatement psUpdPdf = conn.prepareStatement(sqlUpdatePdf)) {
                psUpdPdf.setString(1, pdfFile.getAbsolutePath());
                psUpdPdf.setLong(2, idTicket);
                psUpdPdf.executeUpdate();
            }

            conn.commit();
            return idTicket;

        } catch (Exception ex) {
            try { if (conn != null) conn.rollback(); } catch (SQLException _e) {}
            ex.printStackTrace();
            return -1;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (psTicket != null) psTicket.close(); } catch (Exception ignored) {}
            try { if (psDetalle != null) psDetalle.close(); } catch (Exception ignored) {}
            try { if (psUpdateStock != null) psUpdateStock.close(); } catch (Exception ignored) {}
            try { if (conn != null) { conn.setAutoCommit(true); Conexion.cerrar(conn); } } catch (Exception ignored) {}
        }
    }

    /**
     * Genera el PDF del ticket. Lista: cod, nombre - gramaje, cantidad, p.u., subtotal.
     */
    
    /**
    * Genera el PDF del ticket formateado para impresoras de 58mm (mini impresoras).
    */
        public static void generarPdfTicket(String ruta, long idTicket, String fecha, String vendedorNombre, List<SaleItem> items, double totalFinal) throws IOException {
            float pageWidth = 164; // 58mm en puntos (1 pulgada = 72 puntos)
            float margin = 10; // Márgenes pequeños para aprovechar mejor el espacio
            float y; // Coordenada vertical inicial

            PDDocument doc = new PDDocument();
            PDPage page = new PDPage(new PDRectangle(pageWidth, 400)); // Altura inicial personalizada
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                y = page.getMediaBox().getHeight() - margin; // Coordenada "y" inicial

                // Título
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText("TICKET Nro: " + idTicket);
                cs.endText();

                // Información del vendedor y fecha
                y -= 12; // Espaciado
                cs.setFont(PDType1Font.HELVETICA, 8);
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                String vendedorLine = vendedorNombre != null && !vendedorNombre.isEmpty() ? "Vendedor: " + vendedorNombre : "Vendedor ID: N/A";
                cs.showText("Fecha: " + fecha);
                cs.endText();

                y -= 10;
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText(vendedorLine);
                cs.endText();

                // Separador
                y -= 10;
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText("----------------------------------------");
                cs.endText();

                // Encabezado de la tabla
                y -= 12;
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format("%-6s %-15s %3s %6s %7s", "COD", "PROD", "CAN", "P.U.", "SUB"));
                cs.endText();
            }

            // Productos vendidos
            for (SaleItem it : items) {
                if (y < margin) { // Agregar nueva página si el espacio no es suficiente
                    page = new PDPage(new PDRectangle(pageWidth, 400)); // Nueva página
                    doc.addPage(page);
                    y = page.getMediaBox().getHeight() - margin; // Reiniciar coordenada "y"
                }

                String productoConGram = it.nombre + (it.gramaje != null && !it.gramaje.isEmpty() ? " " + it.gramaje : "");
                String line = String.format(Locale.ROOT, "%-6s %-15.15s %3d %6.2f %7.2f",
                        it.codBarras, productoConGram, it.cantidad, it.precioUnitario, it.subtotal);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                    y -= 10;
                    cs.setFont(PDType1Font.HELVETICA, 7);
                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    cs.showText(line);
                    cs.endText();
                }
            }

            // Total Final
            if (y < margin) {
                page = new PDPage(new PDRectangle(pageWidth, 400)); // Nueva página
                doc.addPage(page);
                y = page.getMediaBox().getHeight() - margin;
            }

            try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                y -= 15;
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format(Locale.ROOT, "TOTAL: %.2f", totalFinal));
                cs.endText();

                y -= 20;
                cs.setFont(PDType1Font.HELVETICA, 7);
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText("GRACIAS POR SU COMPRA");
                cs.endText();
            }

            // Guardar y cerrar el documento
            doc.save(ruta);
            doc.close();
        }
    
    /**
     * Registra una cancelación: inserta en Cancelaciones, actualiza Ticket_Detalle.cantidad_devuelta y repone stock en Productos.
     * Devuelve true si OK.
     */
    public static boolean cancelarProducto(long idTicket, String codBarras, int cantidadCancelada, String motivo, int idUsuario) {
        Connection conn = null;
        PreparedStatement psInsertCancel = null;
        PreparedStatement psUpdDetalle = null;
        PreparedStatement psUpdStock = null;
        try {
            conn = Conexion.conectar();
            conn.setAutoCommit(false);

            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date());

            // 1) Obtener precio_unitario_venta desde Ticket_Detalle (si existe) para calcular subtotal
            double precioUnitario = 0.0;
            String sqlGetPrecio = "SELECT precio_unitario_venta FROM Ticket_Detalle WHERE id_ticket = ? AND cod_barras = ? LIMIT 1";
            try (PreparedStatement psGet = conn.prepareStatement(sqlGetPrecio)) {
                psGet.setLong(1, idTicket);
                psGet.setString(2, codBarras);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) precioUnitario = rs.getDouble("precio_unitario_venta");
                }
            }

            double subtotal = Math.round(precioUnitario * cantidadCancelada * 100.0) / 100.0;

            // 2) Insertar en Cancelaciones
            String sqlInsertCancel = "INSERT INTO Cancelaciones (id_ticket, cod_barras, cantidad_cancelada, motivo, id_usuario, fecha, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
            psInsertCancel = conn.prepareStatement(sqlInsertCancel);
            psInsertCancel.setLong(1, idTicket);
            psInsertCancel.setString(2, codBarras);
            psInsertCancel.setInt(3, cantidadCancelada);
            psInsertCancel.setString(4, motivo);
            psInsertCancel.setInt(5, idUsuario);
            psInsertCancel.setString(6, fecha);
            psInsertCancel.setDouble(7, subtotal);
            psInsertCancel.executeUpdate();

            // 3) Actualizar cantidad_devuelta en Ticket_Detalle
            String sqlUpdDetalleStr = "UPDATE Ticket_Detalle SET cantidad_devuelta = cantidad_devuelta + ? WHERE id_ticket = ? AND cod_barras = ?";
            psUpdDetalle = conn.prepareStatement(sqlUpdDetalleStr);
            psUpdDetalle.setInt(1, cantidadCancelada);
            psUpdDetalle.setLong(2, idTicket);
            psUpdDetalle.setString(3, codBarras);
            psUpdDetalle.executeUpdate();

            // 4) Reponer stock en Productos
            String sqlUpdStock = "UPDATE Productos SET stock = stock + ? WHERE cod_barras = ?";
            psUpdStock = conn.prepareStatement(sqlUpdStock);
            psUpdStock.setInt(1, cantidadCancelada);
            psUpdStock.setString(2, codBarras);
            psUpdStock.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception ex) {
            try { if (conn != null) conn.rollback(); } catch (SQLException _e) {}
            ex.printStackTrace();
            return false;
        } finally {
            try { if (psInsertCancel != null) psInsertCancel.close(); } catch (Exception ignored) {}
            try { if (psUpdDetalle != null) psUpdDetalle.close(); } catch (Exception ignored) {}
            try { if (psUpdStock != null) psUpdStock.close(); } catch (Exception ignored) {}
            try { if (conn != null) { conn.setAutoCommit(true); Conexion.cerrar(conn); } } catch (Exception ignored) {}
        }
    }
}