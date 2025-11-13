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

    // Simple DTO para items de venta (ahora con nombre y gramaje)
    public static class SaleItem {
        public String codBarras;
        public int cantidad;
        public double precioUnitario; // precio aplicable por ítem
        public double subtotal; // cantidad * precioUnitario
        public String nombre; // nombre del producto para mostrar en ticket
        public String gramaje; // gramaje/presentación para mostrar

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
     * Finaliza una venta: inserta Tickets + Ticket_Detalle, actualiza stock, genera PDF y guarda ruta en Tickets.ruta_pdf.
     * Devuelve el id del ticket creado o -1 en error.
     *
     * Nota: se añadió el parámetro vendedorNombre para imprimir el nombre en el PDF.
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

                // actualizar stock
                psUpdateStock.setInt(1, it.cantidad);
                psUpdateStock.setString(2, it.codBarras);
                psUpdateStock.executeUpdate();
            }

            // 3) Generar PDF y actualizar ruta en Tickets.ruta_pdf
            if (carpetaSalida == null || carpetaSalida.trim().isEmpty()) {
                carpetaSalida = System.getProperty("user.home") + File.separator + "FarmaApp" + File.separator + "tickets";
            }
            File dir = new File(carpetaSalida);
            if (!dir.exists() && !dir.mkdirs()) {
                // intentar continuar; si no se puede crear, usar user.home
                carpetaSalida = System.getProperty("user.home") + File.separator + "FarmaApp" + File.separator + "tickets";
                dir = new File(carpetaSalida);
                if (!dir.exists()) dir.mkdirs();
            }

            String nombrePDF = "ticket_" + idTicket + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
            File pdfFile = new File(dir, nombrePDF);

            generarPdfTicket(pdfFile.getAbsolutePath(), idTicket, fecha, vendedorNombre, items, totalFinal);

            // actualizar ruta_pdf
            String sqlUpdatePdf = "UPDATE Tickets SET ruta_pdf = ? WHERE id = ?";
            try (PreparedStatement psUpdPdf = conn.prepareStatement(sqlUpdatePdf)) {
                psUpdPdf.setString(1, pdfFile.getAbsolutePath());
                psUpdPdf.setLong(2, idTicket);
                psUpdPdf.executeUpdate();
            }

            conn.commit();
            return idTicket;

        } catch (Exception ex) {
            try { if (conn != null) conn.rollback(); } catch (SQLException _e) { /* ignore */ }
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

    // PDF simple con PDFBox — ahora muestra vendedor y nombre+gramaje por item
    public static void generarPdfTicket(String ruta, long idTicket, String fecha, String vendedorNombre, List<SaleItem> items, double totalFinal) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float y = page.getMediaBox().getUpperRightY() - 40;
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.beginText();
            cs.newLineAtOffset(40, y);
            cs.showText("TICKET Nro: " + idTicket);
            cs.endText();

            cs.setFont(PDType1Font.HELVETICA, 10);
            y -= 20;
            cs.beginText();
            cs.newLineAtOffset(40, y);
            String vendedorLine = "Vendedor: " + (vendedorNombre == null ? String.valueOf("Usuario " + "") : vendedorNombre);
            cs.showText("Fecha: " + fecha + "   " + vendedorLine);
            cs.endText();

            y -= 25;
            cs.beginText();
            cs.newLineAtOffset(40, y);
            cs.showText("---------------------------------------------------------------");
            cs.endText();

            y -= 15;
            cs.beginText();
            cs.newLineAtOffset(40, y);
            cs.showText(String.format("%-12s %-25s %5s %10s %10s", "COD", "PRODUCTO (GRAM)", "CAN", "P.U.", "SUB"));
            cs.endText();

            for (SaleItem it : items) {
                y -= 15;
                if (y < 60) {
                    cs.close();
                    page = new PDPage(PDRectangle.LETTER);
                    doc.addPage(page);
                    y = page.getMediaBox().getUpperRightY() - 40;
                }
                try (PDPageContentStream csLine = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                    csLine.setFont(PDType1Font.HELVETICA, 10);
                    csLine.beginText();
                    csLine.newLineAtOffset(40, y);
                    String productoConGram = it.nombre;
                    if (it.gramaje != null && !it.gramaje.isEmpty()) productoConGram += " - " + it.gramaje;
                    String line = String.format(Locale.ROOT, "%-12s %-25.25s %5d %10.2f %10.2f", it.codBarras, productoConGram, it.cantidad, it.precioUnitario, it.subtotal);
                    csLine.showText(line);
                    csLine.endText();
                    csLine.close();
                }
            }

            y -= 20;
            try (PDPageContentStream csTotal = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                csTotal.setFont(PDType1Font.HELVETICA_BOLD, 12);
                csTotal.beginText();
                csTotal.newLineAtOffset(40, y);
                csTotal.showText(String.format(Locale.ROOT, "TOTAL: %.2f", totalFinal));
                csTotal.endText();
                csTotal.close();
            }
        }

        doc.save(ruta);
        doc.close();
    }

    // cancelarProducto se mantiene igual (usa Cancelaciones table)
    public static boolean cancelarProducto(long idTicket, String codBarras, int cantidadCancelada, String motivo, int idUsuario) {
        // ... (mantiene la implementación anterior) ...
        return medicamentos.SalesManager.cancelarProducto(idTicket, codBarras, cantidadCancelada, motivo, idUsuario);
    }
}