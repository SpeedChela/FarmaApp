/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package medicamentos;


import java.awt.Component;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

            String codBarras = table.getValueAt(selectedRow, 0).toString(); // Supone columna 0 oculta
            int idProducto = -1;

            try (Connection conn = Conexion.conectar();
                 PreparedStatement psBuscar = conn.prepareStatement("SELECT id FROM Productos WHERE codigo_barras = ?")) {

                psBuscar.setString(1, codBarras);
                ResultSet rs = psBuscar.executeQuery();

                if (rs.next()) {
                    idProducto = rs.getInt("id");

                    // Insertar cancelación
                    String sqlCancel = "INSERT INTO Cancelaciones (idUsuario, idProducto, fecha_cancelacion) VALUES (?, ?, ?)";
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlCancel)) {
                        psInsert.setInt(1, idUsuario);
                        psInsert.setInt(2, idProducto);
                        psInsert.setString(3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        psInsert.executeUpdate();
                    }

                    // Remover producto visualmente
                    ((DefaultTableModel) table.getModel()).removeRow(selectedRow);

                } else {
                    JOptionPane.showMessageDialog(parent, "Producto no encontrado en base de datos.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(parent, "Error registrando eliminación: " + ex.getMessage());
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

        // 🛠 Añadimos columna de código de barras como referencia interna
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

        // 🎭 Personalizar título según el rol
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
                                .addGap(37, 37, 37)
                                .addComponent(jLabel3))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnPagar)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelTotal))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(59, 59, 59)
                                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(labelTotal))
                        .addGap(18, 18, 18)
                        .addComponent(btnPagar)
                        .addGap(171, 171, 171))
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

        try (Connection conn = Conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT  nombre_comercial, precio, stock FROM Productos WHERE codigo_barras = ? AND activo = 1 AND stock > 0"
             )) {

            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre_comercial");
                    double precio = rs.getDouble("precio");

                    DefaultTableModel modelo = (DefaultTableModel) tableVenta.getModel();
                    modelo.addRow(new Object[]{
                    codigo,                // Código de barras en la columna 0
                    nombre,
                    1,                     // cantidad por defecto
                    "$" + String.format("%.2f", precio),
                    "$" + String.format("%.2f", precio)
                });

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

        try (Connection conn = Conexion.conectar()) {
            conn.setAutoCommit(false);

            double totalVenta = 0;


            for (int i = 0; i < tableVenta.getRowCount(); i++) {
                String subtotalStr = tableVenta.getValueAt(i, 4).toString().replace("$", "");
                totalVenta += Double.parseDouble(subtotalStr);
            }


            String sqlVenta = "INSERT INTO Ventas (fecha_Venta, idUsuario, total) VALUES (?, ?, ?)";
            PreparedStatement psVenta = conn.prepareStatement(sqlVenta, java.sql.Statement.RETURN_GENERATED_KEYS);
            psVenta.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            psVenta.setInt(2, idUsuario);
            psVenta.setDouble(3, totalVenta);
            psVenta.executeUpdate();

            ResultSet rsVenta = psVenta.getGeneratedKeys();
            int idVenta = rsVenta.next() ? rsVenta.getInt(1) : -1;
            rsVenta.close();
            psVenta.close();

            // 🧾 Inserta detalles
            String sqlDetalle = "INSERT INTO Detalles_venta (idVenta, idProducto, cantidad, precioInd, subtotal) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psDetalle = conn.prepareStatement(sqlDetalle);

            for (int i = 0; i < tableVenta.getRowCount(); i++) {
                String codBarras = tableVenta.getValueAt(i, 0).toString(); // Código de barras en la columna 0

                // 🔎 Obtener el idProducto basado en el codBarras
                int idProducto = -1;
                try (PreparedStatement psBuscar = conn.prepareStatement("SELECT id FROM Productos WHERE codigo_barras = ?")) {
                    psBuscar.setString(1, codBarras);
                    ResultSet rs = psBuscar.executeQuery();
                    if (rs.next()) {
                        idProducto = rs.getInt("id");
                    } else {
                        JOptionPane.showMessageDialog(this, "Producto no encontrado: " + codBarras);
                        conn.rollback();
                        return;
                    }
                    rs.close();
                }

                int cantidad;
                try {
                    cantidad = Integer.parseInt(tableVenta.getValueAt(i, 2).toString());
                } catch (NumberFormatException e) {
                    cantidad = 1;
                }

                double precio = Double.parseDouble(tableVenta.getValueAt(i, 3).toString().replace("$", ""));
                double subtotal = Double.parseDouble(tableVenta.getValueAt(i, 4).toString().replace("$", ""));

                // Agrega detalle
                psDetalle.setInt(1, idVenta);
                psDetalle.setInt(2, idProducto);
                psDetalle.setInt(3, cantidad);
                psDetalle.setDouble(4, precio);
                psDetalle.setDouble(5, subtotal);
                psDetalle.addBatch();

                // Actualiza el stock
                try (PreparedStatement psStock = conn.prepareStatement(
                        "UPDATE Productos SET stock = stock - ? WHERE id = ?")) {
                    psStock.setInt(1, cantidad);
                    psStock.setInt(2, idProducto);
                    psStock.executeUpdate();
                }
            }

            psDetalle.executeBatch();
            psDetalle.close();

            conn.commit();

            JOptionPane.showMessageDialog(this, "Venta registrada exitosamente.");
            ((DefaultTableModel) tableVenta.getModel()).setRowCount(0);
            labelTotal.setText("Total: $0.00");
            txtBarras.setText("");

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
