/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package medicamentos;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.util.Vector;


/**
 *
 * @author SP-DR
 */
public class VisorTickets extends javax.swing.JFrame {
    
    private int rol;
    private int idUsuario;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VisorTickets.class.getName());

    private DefaultTableModel modelTickets;
    /**
     * Creates new form VisorTickets
     */
     /**
     * Constructor usado por NetBeans (ejecución directa)
     */
    public VisorTickets() {
        initComponents();
        setupTable();
        cargarTicketsAsync(null);
    }

    /**
     * Constructor usado desde la app pasando rol e idUsuario
     */
    public VisorTickets(int rol, int idUsuario) {
        initComponents();
        this.rol = rol;
        this.idUsuario = idUsuario;
        setupTable();
        cargarTicketsAsync(null);
        setLocationRelativeTo(null);
        btnRegresar.requestFocus(true);
    }
    
    private void setupTable() {
        // Definir modelo con las columnas esperadas
        modelTickets = new DefaultTableModel(new Object[]{"ID", "Fecha", "Vendedor", "Total", "Estado", "RutaPDF"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        // Asignar modelo al JTable creado por NetBeans
        tableTickets.setModel(modelTickets);

        // Permitir orden por columnas
        tableTickets.setAutoCreateRowSorter(true);
        tableTickets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTickets.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Ajustar anchos iniciales por columna (si existe la columna)
        int[] preferredWidths = new int[]{60, 180, 220, 100, 100, 300};
        for (int i = 0; i < preferredWidths.length && i < tableTickets.getColumnModel().getColumnCount(); i++) {
            tableTickets.getColumnModel().getColumn(i).setPreferredWidth(preferredWidths[i]);
        }

        // Ocultar la columna de ruta_pdf (índice 5) para que no sea visible
        if (tableTickets.getColumnModel().getColumnCount() >= 6) {
            tableTickets.getColumnModel().getColumn(5).setMinWidth(0);
            tableTickets.getColumnModel().getColumn(5).setMaxWidth(0);
            tableTickets.getColumnModel().getColumn(5).setWidth(0);
            tableTickets.getColumnModel().getColumn(5).setResizable(false);
        }

        // Ordenador de filas (por fecha por defecto)
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelTickets);
        tableTickets.setRowSorter(sorter);

        // Doble click abre PDF
        tableTickets.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirTicketSeleccionado();
                }
            }
        });
    }

    /**
     * Carga tickets en background para no bloquear la UI.
     * filtro puede ser null o texto para filtrar por id/fecha/vendedor (uso LIKE).
     */
    private void cargarTicketsAsync(String filtro) {
        modelTickets.setRowCount(0);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                cargarTickets(filtro);
                return null;
            }
            @Override
            protected void done() {
                // opcional: actualizar estado en la UI si agregas un label
            }
        };
        worker.execute();
    }

    /**
     * Ejecuta la consulta y llena el modelo. Se llama desde el SwingWorker.
     */
    private void cargarTickets(String filtro) {
        String sql = "SELECT id, fecha, COALESCE(vendedor, (SELECT nombre FROM Usuarios WHERE id = Tickets.id_usuario)) AS vendedor, total_final, estado, ruta_pdf FROM Tickets ";
        if (filtro != null && !filtro.isEmpty()) {
            sql += "WHERE CAST(id AS TEXT) LIKE ? OR fecha LIKE ? OR COALESCE(vendedor, (SELECT nombre FROM Usuarios WHERE id = Tickets.id_usuario)) LIKE ? ";
        }
        sql += "ORDER BY fecha DESC";

        try (Connection conn = Conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filtro != null && !filtro.isEmpty()) {
                String like = "%" + filtro + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String fecha = rs.getString("fecha");
                    String vendedor = rs.getString("vendedor");
                    double total = rs.getDouble("total_final");
                    String estado = rs.getString("estado");
                    String ruta = rs.getString("ruta_pdf");

                    Vector<Object> row = new Vector<>();
                    row.add(id);
                    row.add(fecha != null ? fecha : "");
                    row.add(vendedor != null ? vendedor : "");
                    row.add(String.format("%.2f", total));
                    row.add(estado != null ? estado : "");
                    row.add(ruta != null ? ruta : "");
                    modelTickets.addRow(row);
                }
            }
        } catch (SQLException ex) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(VisorTickets.this, "Error cargando tickets: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    /**
     * Abre el PDF del ticket seleccionado (usa la columna oculta RutaPDF).
     */
    private void abrirTicketSeleccionado() {
        int rowView = tableTickets.getSelectedRow();
        if (rowView < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un ticket primero.");
            return;
        }
        int rowModel = tableTickets.convertRowIndexToModel(rowView);
        String ruta = (String) modelTickets.getValueAt(rowModel, 5);
        if (ruta == null || ruta.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay PDF asociado a este ticket.");
            return;
        }
        File f = new File(ruta);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "Archivo PDF no encontrado: " + ruta);
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
            } else {
                JOptionPane.showMessageDialog(this, "No es posible abrir archivos en esta plataforma.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error abriendo PDF: " + ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnRegresar = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableTickets = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        btnRegresar.setBackground(new java.awt.Color(255, 255, 255));

        tableTickets.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tableTickets);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Visualizador de Tickets");

        jButton1.setBackground(new java.awt.Color(255, 51, 51));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Regresar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout btnRegresarLayout = new javax.swing.GroupLayout(btnRegresar);
        btnRegresar.setLayout(btnRegresarLayout);
        btnRegresarLayout.setHorizontalGroup(
            btnRegresarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnRegresarLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 527, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(111, 111, 111))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnRegresarLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(224, 224, 224))
        );
        btnRegresarLayout.setVerticalGroup(
            btnRegresarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnRegresarLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(btnRegresarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(73, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Venta v = new Venta(rol, idUsuario);
        v.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new VisorTickets().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btnRegresar;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tableTickets;
    // End of variables declaration//GEN-END:variables
}
