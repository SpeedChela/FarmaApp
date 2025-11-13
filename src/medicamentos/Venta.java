/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package medicamentos;


import java.awt.Component;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;


/**
 *
 * @author SP-DR
 */
public class Venta extends javax.swing.JFrame {
    
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private int selectedRow;
        private JTable table;
        private int rol;
        private JFrame parent;
        private final int idUsuario;

        public ButtonEditor(JCheckBox checkBox, JTable tableVenta, int rol, int idUsuario, JFrame parent) {
            super(checkBox);
            this.table = tableVenta;
            this.rol = rol;
            this.parent = parent;
            this.idUsuario = idUsuario;

            button = new JButton("Eliminar");
            button.addActionListener(e -> {
                fireEditingStopped();

                if (selectedRow < 0 || selectedRow >= table.getRowCount()) {
                    JOptionPane.showMessageDialog(parent, "Fila no válida.");
                    return;
                }

                String codBarras = String.valueOf(table.getValueAt(selectedRow, 0)); // columna 0 oculta con cod_barras
                int cantidad = 1;
                double precioUnit = 0.0;
                try {
                    Object cantObj = table.getValueAt(selectedRow, 2);
                    cantidad = Integer.parseInt(String.valueOf(cantObj));
                } catch (Exception ex) {
                    cantidad = 1;
                }
                try {
                    String precioStr = String.valueOf(table.getValueAt(selectedRow, 3)).replace("$", "").trim();
                    precioUnit = Double.parseDouble(precioStr);
                } catch (Exception ex) {
                    precioUnit = 0.0;
                }

                double subtotal = Math.round(precioUnit * cantidad * 100.0) / 100.0;

                try (Connection conn = Conexion.conectar()) {

                    // 1) Insertar registro en Cancelaciones (id_ticket = 0 para "sin ticket")
                    String sqlCancel = "INSERT INTO Cancelaciones (id_ticket, cod_barras, cantidad_cancelada, motivo, id_usuario, fecha, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlCancel)) {
                        psInsert.setInt(1, 0); // 0 -> sin ticket (cancelación en carrito)
                        psInsert.setString(2, codBarras);
                        psInsert.setInt(3, cantidad);
                        psInsert.setString(4, "Cancelado en pantalla de venta");
                        psInsert.setInt(5, idUsuario);
                        psInsert.setString(6, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                        psInsert.setDouble(7, subtotal);
                        psInsert.executeUpdate();
                    }

                    // 2) Preguntar si se debe devolver al stock
                    int devolver = JOptionPane.showConfirmDialog(parent,
                        "¿Deseas devolver " + cantidad + " unidad(es) del producto al stock?",
                        "Devolver al stock",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                    if (devolver == JOptionPane.YES_OPTION) {
                        // Actualizar stock: sumar la cantidad cancelada
                        String sqlUpdStock = "UPDATE Productos SET stock = stock + ? WHERE cod_barras = ?";
                        try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdStock)) {
                            psUpd.setInt(1, cantidad);
                            psUpd.setString(2, codBarras);
                            int u = psUpd.executeUpdate();
                            if (u > 0) {
                                JOptionPane.showMessageDialog(parent, "Stock actualizado: se devolvieron " + cantidad + " unidad(es).");
                            } else {
                                JOptionPane.showMessageDialog(parent, "No se pudo actualizar el stock (producto no encontrado).");
                            }
                        }
                    }

                    // 3) Remover fila del carrito visualmente
                    ((DefaultTableModel) table.getModel()).removeRow(selectedRow);

                    // 4) Recalcular totales en UI
                    if (parent instanceof Venta) {
                        ((Venta) parent).actualizarSubtotal();
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(parent, "Error registrando cancelación: " + ex.getMessage());
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Eliminar";
        }


        private boolean validarPasswordAdmin(String password) {
            try (Connection conn = Conexion.conectar();
                 PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE rol = 1 AND password = ?")) {
                stmt.setString(1, password);
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getInt(1) > 0;
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(parent, "Error al conectar con la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("Eliminar");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Venta.class.getName());
    private int rol;
    private int idUsuario;
    /**
     * Creates new form Venta
     */
    public Venta() {
        initComponents();
    }
    
    public Venta(int rol, int idUsuario) {
        initComponents(); // generado por NetBeans
        this.rol = rol;
        this.idUsuario = idUsuario;
        if (rol == 1){
            btnTickets.setEnabled(true);
        }else{
            btnTickets.setEnabled(false);
        }
                
        //  Añadimos columna de código de barras como referencia interna
        DefaultTableModel modelo = new DefaultTableModel(
            new Object[][]{},
            new String[]{ "Código de barras", "Medicamento", "Cantidad", "Precio unitario", "Subtotal", "Borrar" }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitir edición en la columna "Borrar" si es necesario
                return column == 5;
            }
        };
        
        

        tableVenta.setModel(modelo);

        // Ocultar código de barras visualmente, pero mantenerlo como dato
        TableColumnModel columnModel = tableVenta.getColumnModel();
        columnModel.getColumn(0).setMinWidth(0);
        columnModel.getColumn(0).setMaxWidth(0);
        columnModel.getColumn(0).setWidth(0);

        // Configurar botón "Borrar"
        tableVenta.getColumn("Borrar").setCellRenderer(new ButtonRenderer());
        tableVenta.getColumn("Borrar").setCellEditor(
            new ButtonEditor(new JCheckBox(), tableVenta, rol, idUsuario, this)
        );



        setLocationRelativeTo(null);

        // Personalizar título según el rol
        if (rol == 1) {
            setTitle("Venta - Administrador");
        } else if (rol == 2) {
            setTitle("Venta - Trabajador");
        }
    }
    
    private void actualizarSubtotal() {
        double total = 0.0;

        for (int i = 0; i < tableVenta.getRowCount(); i++) {
            Object valor = tableVenta.getValueAt(i, 2);
            int cantidad;

            try {
                cantidad = Integer.parseInt(valor.toString());
            } catch (NumberFormatException e) {
                cantidad = 1;
                tableVenta.setValueAt(1, i, 2);
            }

            String precioStr = tableVenta.getValueAt(i, 3).toString().replace("$", "");
            double precio = Double.parseDouble(precioStr);

            double subtotal = cantidad * precio;
            tableVenta.setValueAt("$" + String.format("%.2f", subtotal), i, 4);
            total += subtotal;
        }

        labelTotal.setText(String.format("%.2f", total));

    }
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableVenta = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        btnRegresar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtBarras = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnPagar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        labelTotal = new javax.swing.JLabel();
        btnTickets = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tableVenta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tableVenta);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Ventas");

        btnRegresar.setBackground(new java.awt.Color(255, 0, 0));
        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRegresar.setForeground(new java.awt.Color(0, 0, 0));
        btnRegresar.setText("Regresar");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Codigo de Barras");

        btnBuscar.setBackground(new java.awt.Color(0, 102, 255));
        btnBuscar.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        btnPagar.setBackground(new java.awt.Color(51, 204, 0));
        btnPagar.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnPagar.setForeground(new java.awt.Color(0, 0, 0));
        btnPagar.setText("Pagar");
        btnPagar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPagarActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Total:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("$");

        labelTotal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        labelTotal.setForeground(new java.awt.Color(0, 0, 0));
        labelTotal.setText("0.00");

        btnTickets.setBackground(new java.awt.Color(204, 0, 255));
        btnTickets.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnTickets.setForeground(new java.awt.Color(0, 0, 0));
        btnTickets.setText("Tickets");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnRegresar)
                        .addGap(147, 147, 147)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtBarras))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(59, 59, 59)
                                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(jLabel3))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addGap(60, 60, 60)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnTickets, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnPagar)))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addGap(33, 33, 33)
                                    .addComponent(jLabel2)
                                    .addGap(18, 18, 18)
                                    .addComponent(jLabel4)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(labelTotal))))
                        .addGap(0, 37, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBarras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(labelTotal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnPagar)
                        .addGap(26, 26, 26)
                        .addComponent(btnTickets, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(116, 116, 116))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(btnRegresar))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        MenuPrincipal menu = new MenuPrincipal(rol,idUsuario);
        menu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnRegresarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
       String codigo = txtBarras.getText().trim();

        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el código de barras.");
            return;
        }

        // Usar nombres de columna actuales: cod_barras, nom_com, precio_venta, stock, gramaje
        try (Connection conn = Conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT nom_com, precio_venta, stock, gramaje FROM Productos WHERE cod_barras = ? AND activo = 1 AND stock > 0"
             )) {

            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nom_com");
                    double precio = rs.getDouble("precio_venta");
                    String gramaje = "";
                    try { gramaje = rs.getString("gramaje"); } catch (Exception ex) { gramaje = ""; }

                    DefaultTableModel modelo = (DefaultTableModel) tableVenta.getModel();
                    modelo.addRow(new Object[]{
                        codigo,                // Código de barras en la columna 0 (oculta)
                        nombre,
                        1,                     // cantidad por defecto
                        "$" + String.format("%.2f", precio),
                        "$" + String.format("%.2f", precio),
                        "Eliminar"
                    });

                    // Guardamos el gramaje en una columna oculta adicional o lo mantenemos en memoria:
                    // Opcional: podrías tener otra columna oculta para gramaje si deseas.
                    // Para PDF usamos el valor leído aquí al construir los SaleItem al pagar.

                    actualizarSubtotal();
                } else {
                    JOptionPane.showMessageDialog(this, "Producto no encontrado o sin stock.");
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar: " + e.getMessage());
        }
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnPagarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPagarActionPerformed
         if (tableVenta.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "No hay productos en la venta.");
        return;
        }

        // Construir lista de items para SalesManager (ahora recogemos nombre y gramaje desde la BD por cada cod)
        List<SalesManager.SaleItem> items = new ArrayList<>();
        double totalVenta = 0.0;

        try (Connection conn = Conexion.conectar()) {
            for (int i = 0; i < tableVenta.getRowCount(); i++) {
                String codBarras = String.valueOf(tableVenta.getValueAt(i, 0));
                int cantidad = 1;
                try {
                    cantidad = Integer.parseInt(String.valueOf(tableVenta.getValueAt(i, 2)));
                } catch (Exception ex) {
                    cantidad = 1;
                }
                double precio = 0.0;
                try {
                    precio = Double.parseDouble(String.valueOf(tableVenta.getValueAt(i, 3)).replace("$", "").trim());
                } catch (Exception ex) {
                    precio = 0.0;
                }

                // Obtener nombre y gramaje desde la BD para garantizar consistencia
                String nombreProducto = "";
                String gramaje = "";
                try (PreparedStatement psProd = conn.prepareStatement("SELECT nom_com, gramaje FROM Productos WHERE cod_barras = ? LIMIT 1")) {
                    psProd.setString(1, codBarras);
                    try (ResultSet rs = psProd.executeQuery()) {
                        if (rs.next()) {
                            nombreProducto = rs.getString("nom_com");
                            gramaje = rs.getString("gramaje");
                        }
                    }
                }

                SalesManager.SaleItem it = new SalesManager.SaleItem(codBarras, cantidad, precio, nombreProducto, gramaje);
                items.add(it);
                totalVenta += it.subtotal;
            }

            totalVenta = Math.round(totalVenta * 100.0) / 100.0;

            int confirmar = JOptionPane.showConfirmDialog(this,
                "Confirma la venta por un total de $" + String.format("%.2f", totalVenta) + " ?",
                "Confirmar venta",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }

            // Obtener nombre del vendedor (usuario) para mostrar en ticket
            String vendedorNombre = null;
            try (PreparedStatement psUser = conn.prepareStatement("SELECT nombre FROM Usuarios WHERE id = ? LIMIT 1")) {
                psUser.setInt(1, idUsuario);
                try (ResultSet rsu = psUser.executeQuery()) {
                    if (rsu.next()) {
                        vendedorNombre = rsu.getString("nombre");
                    }
                }
            } catch (SQLException ex) {
                // Si la columna se llama distinto, ajusta aquí. Si falla, quedará null y SalesManager imprimirá idUsuario.
                System.out.println("No se pudo obtener nombre de usuario: " + ex.getMessage());
            }

            // Llamar a SalesManager para finalizar la venta (inserta Tickets y Ticket_Detalle, actualiza stock, genera PDF)
            long idTicket = SalesManager.finalizarVenta(items, idUsuario, null, vendedorNombre);
            if (idTicket > 0) {
                JOptionPane.showMessageDialog(this, "Venta registrada. Ticket ID: " + idTicket);
                // Limpiar UI
                ((DefaultTableModel) tableVenta.getModel()).setRowCount(0);
                labelTotal.setText("0.00");
                txtBarras.setText("");
                // Intentar abrir el PDF guardado (ruta almacenada en Tickets.ruta_pdf)
                try (PreparedStatement ps = conn.prepareStatement("SELECT ruta_pdf FROM Tickets WHERE id = ?")) {
                    ps.setLong(1, idTicket);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String ruta = rs.getString("ruta_pdf");
                            if (ruta != null && !ruta.isEmpty()) {
                                try {
                                    java.awt.Desktop.getDesktop().open(new java.io.File(ruta));
                                } catch (Exception ex) {
                                    System.out.println("No se pudo abrir el PDF: " + ex.getMessage());
                                }
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error al finalizar la venta.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar venta: " + e.getMessage());
        }
    }//GEN-LAST:event_btnPagarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Venta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnPagar;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JButton btnTickets;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelTotal;
    private javax.swing.JTable tableVenta;
    private javax.swing.JTextField txtBarras;
    // End of variables declaration//GEN-END:variables
}
