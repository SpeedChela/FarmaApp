/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package medicamentos;
import java.util.*;
import javax.swing.JOptionPane;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.JFrame;
/**
 *
 * @author SP-DR
 */
public class Inventario extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Inventario.class.getName());

    private int rol;
    private int idUsuario;
    private String codigoSeleccionado;
    /**
     * Creates new form Inventario
     */
    public Inventario() {
        initComponents();
    }
    
    public Inventario(int rol, int idUsuario, int accion) {
        initComponents();
        this.rol = rol;
        this.idUsuario = idUsuario;
        setLocationRelativeTo(null);

        // Título según rol
        if (rol == 1) {
            setTitle("Inventario - Administrador");
        } else if (rol == 2) {
            setTitle("Inventario - Trabajador");
        }

        // Configura botones según acción
        switch (accion) {
            case 1: //  Nada activado
                btnUpdate.setEnabled(false);
                btnEliminar.setEnabled(false);
                desactivar();
                break;
            case 2: //  Solo eliminar activado
                btnUpdate.setEnabled(false);
                btnEliminar.setEnabled(true);
                break;
            case 3: // Solo actualizar activado
                btnUpdate.setEnabled(true);
                btnEliminar.setEnabled(false);
                break;
            default: // Seguridad: desactiva ambos si valor no reconocido
                btnUpdate.setEnabled(false);
                btnEliminar.setEnabled(false);
                break;
        }

        // Métodos comunes del sistema
        cargarMedicamentos();

    }
        
    private Map<Integer, String> mapaTipos = new HashMap<>();
    
    
    private void desactivar() {
        txtNom_com.setEditable(false);
        

        
    }
    
    
private void cargarMedicamentos() {
    // 1. --- Modificación: Se agregan "Gramaje" y "Presentación" ---
    DefaultTableModel modelo = new DefaultTableModel(
        new Object[][]{},
        new String[]{
            "Nombre Comercial", "Gramaje", "Presentación", "Descripción", "Precio", "Stock", "cod_barras" 
            // cod_barras ahora está en el índice 6
        }
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    tablaCatalogo.setModel(modelo);
    
    // 2. --- Ajuste del Índice: Oculta la columna de "cod_barras" (Ahora índice 6) ---
    try {
        tablaCatalogo.getColumnModel().getColumn(6).setMinWidth(0);
        tablaCatalogo.getColumnModel().getColumn(6).setMaxWidth(0);
        tablaCatalogo.getColumnModel().getColumn(6).setWidth(0);
    } catch (Exception e) {
        // Manejo de excepción si la tabla aún no se ha dibujado o si hay un error de columna
        System.err.println("Error al intentar ocultar la columna 6: " + e.getMessage());
    }


    tablaCatalogo.setRowSelectionAllowed(true);
    tablaCatalogo.setColumnSelectionAllowed(false);
    tablaCatalogo.setCellSelectionEnabled(false);

    Connection conn = Conexion.conectar();

    // 3. --- Modificación de la Consulta: Se añaden 'gramaje' y 'presentacion' ---
    String sql = "SELECT nom_com, gramaje, presentacion, descripcion, precio_venta, stock, cod_barras " +
                  "FROM Productos " +
                  "WHERE activo = 1 " +
                  "ORDER BY nom_com ASC";

    try {
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            
            // 4. --- Modificación de la Fila: Se añaden 7 elementos en el orden de la consulta ---
            Object[] fila = {
                rs.getString("nom_com"),
                rs.getString("gramaje"),       // <-- Nuevo campo (índice 1)
                rs.getString("presentacion"),  // <-- Nuevo campo (índice 2)
                rs.getString("descripcion"),
                rs.getDouble("precio_venta"),
                rs.getInt("stock"),
                rs.getString("cod_barras") // <-- El dato oculto (índice 6)
            };
            modelo.addRow(fila);
        }
        
        rs.close();
        ps.close();
        Conexion.cerrar(conn); 
        System.out.println("cargarMedicamentos ejecutado con 7 columnas.");

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar medicamentos: " + e.getMessage());
        e.printStackTrace(); 
    }
}

    
    
    private void limpiarCampos() {
        txtCod_barras.setText("");
        txtNom_com.setText("");
        txtDescripcion.setText("");
        txtContenido.setText("");
        txtTipo.setText("");
        txtGramaje.setText("");
        txtFarmaceutica.setText("");
        txtPrecio_compra.setText("");
        txtPorcentajeV.setText("");
        txtPrecio_venta.setText("");
        txtFecha_cad.setText("");
        txtStock.setText("");
        txtMin_stock.setText("");
        txtMax_stock.setText("");
        
        

    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Main = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaCatalogo = new javax.swing.JTable();
        buscar = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        Resetbtn = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtCod_barras = new javax.swing.JTextField();
        txtNom_com = new javax.swing.JTextField();
        txtDescripcion = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtContenido = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtTipo = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtGramaje = new javax.swing.JTextField();
        txtFarmaceutica = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtPrecio_compra = new javax.swing.JTextField();
        txtPorcentajeV = new javax.swing.JTextField();
        txtPrecio_venta = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtFecha_cad = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtMin_stock = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtMax_stock = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtPresentacion = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        Main.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Inventario");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Ingresa Nombre Comercial / Activo:");

        tablaCatalogo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tablaCatalogo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaCatalogoMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tablaCatalogo);

        buscar.setBackground(new java.awt.Color(255, 0, 0));
        buscar.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        buscar.setForeground(new java.awt.Color(0, 0, 0));
        buscar.setText("Buscar");
        buscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buscarActionPerformed(evt);
            }
        });

        btnUpdate.setBackground(new java.awt.Color(51, 255, 51));
        btnUpdate.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(0, 0, 0));
        btnUpdate.setText("Actualizar");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 153, 0));
        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(0, 0, 0));
        jButton2.setText("Regresar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        Resetbtn.setBackground(new java.awt.Color(153, 0, 255));
        Resetbtn.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        Resetbtn.setForeground(new java.awt.Color(255, 255, 255));
        Resetbtn.setText("Reiniciar");
        Resetbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResetbtnActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(255, 51, 51));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(0, 0, 0));
        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 153, 0));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Info del producto");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Codigo Barras");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Nombre");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Descripcion");

        txtCod_barras.setBackground(new java.awt.Color(255, 255, 255));
        txtCod_barras.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtCod_barras.setForeground(new java.awt.Color(0, 0, 0));

        txtNom_com.setBackground(new java.awt.Color(255, 255, 255));
        txtNom_com.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtNom_com.setForeground(new java.awt.Color(0, 0, 0));

        txtDescripcion.setBackground(new java.awt.Color(255, 255, 255));
        txtDescripcion.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtDescripcion.setForeground(new java.awt.Color(0, 0, 0));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Contenido");

        txtContenido.setBackground(new java.awt.Color(255, 255, 255));
        txtContenido.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtContenido.setForeground(new java.awt.Color(0, 0, 0));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Tipo");

        txtTipo.setBackground(new java.awt.Color(255, 255, 255));
        txtTipo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtTipo.setForeground(new java.awt.Color(0, 0, 0));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Gramaje");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("Farmaceutica");

        txtGramaje.setBackground(new java.awt.Color(255, 255, 255));
        txtGramaje.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtGramaje.setForeground(new java.awt.Color(0, 0, 0));

        txtFarmaceutica.setBackground(new java.awt.Color(255, 255, 255));
        txtFarmaceutica.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtFarmaceutica.setForeground(new java.awt.Color(0, 0, 0));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Precio Compra");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setText("Precio Venta");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setText("% Venta");

        txtPrecio_compra.setBackground(new java.awt.Color(255, 255, 255));
        txtPrecio_compra.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtPrecio_compra.setForeground(new java.awt.Color(0, 0, 0));

        txtPorcentajeV.setBackground(new java.awt.Color(255, 255, 255));
        txtPorcentajeV.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtPorcentajeV.setForeground(new java.awt.Color(0, 0, 0));

        txtPrecio_venta.setBackground(new java.awt.Color(255, 255, 255));
        txtPrecio_venta.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtPrecio_venta.setForeground(new java.awt.Color(0, 0, 0));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(0, 0, 0));
        jLabel14.setText("Fecha Cad");

        txtFecha_cad.setBackground(new java.awt.Color(255, 255, 255));
        txtFecha_cad.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtFecha_cad.setForeground(new java.awt.Color(0, 0, 0));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(0, 0, 0));
        jLabel15.setText("Max Stock");

        txtStock.setBackground(new java.awt.Color(255, 255, 255));
        txtStock.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtStock.setForeground(new java.awt.Color(0, 0, 0));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(0, 0, 0));
        jLabel16.setText("Stock");

        txtMin_stock.setBackground(new java.awt.Color(255, 255, 255));
        txtMin_stock.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtMin_stock.setForeground(new java.awt.Color(0, 0, 0));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(0, 0, 0));
        jLabel17.setText("Min Stock");

        txtMax_stock.setBackground(new java.awt.Color(255, 255, 255));
        txtMax_stock.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtMax_stock.setForeground(new java.awt.Color(0, 0, 0));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
        jLabel18.setText("Presentacion");

        txtPresentacion.setBackground(new java.awt.Color(255, 255, 255));
        txtPresentacion.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtPresentacion.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtPrecio_compra, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtStock, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtMin_stock, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(txtMax_stock, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtPresentacion, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtPorcentajeV, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtPrecio_venta, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtFecha_cad, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(16, 16, 16)
                                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtContenido, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtFarmaceutica, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(txtGramaje, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCod_barras, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtNom_com, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 491, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(164, 164, 164)
                                .addComponent(jLabel3)))))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNom_com, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCod_barras, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtGramaje, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPrecio_compra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPorcentajeV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrecio_venta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFecha_cad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMin_stock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMax_stock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPresentacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtContenido, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFarmaceutica, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(54, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout MainLayout = new javax.swing.GroupLayout(Main);
        Main.setLayout(MainLayout);
        MainLayout.setHorizontalGroup(
            MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MainLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(77, 77, 77)
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(MainLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(MainLayout.createSequentialGroup()
                                .addComponent(buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(Resetbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(40, 40, 40)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(MainLayout.createSequentialGroup()
                                .addGap(0, 16, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 742, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38))))))
        );
        MainLayout.setVerticalGroup(
            MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGroup(MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MainLayout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(MainLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buscar, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                            .addGroup(MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(Resetbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 651, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 24, 24))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarActionPerformed
       String texto = txtBuscar.getText().trim();
        if (texto == null || texto.trim().isEmpty()) {
            cargarMedicamentos(); // Llama al método que carga la tabla completa y limpia
            limpiarCampos();
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        DefaultTableModel modelo = (DefaultTableModel) tablaCatalogo.getModel();
        modelo.setRowCount(0); // Limpiar tabla

        try {
            conn = Conexion.conectar();

            String[] palabras = texto.toLowerCase().split("\\s+");

            // --- CORRECCIÓN 1: Agregar 'gramaje', 'presentacion' y 'cod_barras' a la SELECT ---
            StringBuilder sql = new StringBuilder(
                "SELECT nom_com, gramaje, presentacion, descripcion, precio_venta, stock, cod_barras " + 
                "FROM Productos WHERE activo = 1" // Se busca en todos, independientemente del stock para mostrar resultados
            );

            List<String> valores = new ArrayList<>();

            if (palabras.length > 0) {
                sql.append(" AND (");
                for (int i = 0; i < palabras.length; i++) {
                    if (i > 0) sql.append(" AND ");

                    // Se amplían los campos de búsqueda si es necesario
                    sql.append("(LOWER(nom_com) LIKE ? OR LOWER(descripcion) LIKE ? OR LOWER(cod_barras) LIKE ?)");

                    String palabra = palabras[i];
                    valores.add("%" + palabra + "%");
                    valores.add("%" + palabra + "%");
                    valores.add("%" + palabra + "%"); // Añadir búsqueda por cod_barras
                }
                sql.append(")");
            }

            sql.append(" ORDER BY nom_com ASC"); // Ordenar los resultados

            ps = conn.prepareStatement(sql.toString());

            int paramIndex = 1;
            for (String valor : valores) {
                ps.setString(paramIndex++, valor);
            }

            rs = ps.executeQuery();

            boolean hayResultados = false;

            while (rs.next()) {
                hayResultados = true;

                // --- CORRECCIÓN 2: Llenar la fila con los 7 campos en el mismo orden que cargarMedicamentos() ---
                Object[] fila = {
                    rs.getString("nom_com"),
                    rs.getString("gramaje"),
                    rs.getString("presentacion"),
                    rs.getString("descripcion"),
                    rs.getDouble("precio_venta"),
                    rs.getInt("stock"),
                    rs.getString("cod_barras") // <--- ¡CRÍTICO! El código de barras
                };
                modelo.addRow(fila);
            }

            if (!hayResultados) {
                JOptionPane.showMessageDialog(this, "No se encontraron coincidencias.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cierre de recursos
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                Conexion.cerrar(conn);
            } catch (SQLException ex) {
                System.out.println("Error cerrando recursos: " + ex.getMessage());
            }
        }

    }//GEN-LAST:event_buscarActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        MenuInventario menu = new MenuInventario(rol,idUsuario);
        menu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void ResetbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResetbtnActionPerformed
        cargarMedicamentos();
        txtBuscar.setText("");
    }//GEN-LAST:event_ResetbtnActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
    // 1. --- Recolección de datos desde los JTextFields (Alineado con tu tabla) ---
        String nuevoCodBarras = txtCod_barras.getText().trim();
        String nuevoNomCom = txtNom_com.getText().trim();
        String nuevaDescripcion = txtDescripcion.getText().trim();
        String nuevoContenido = txtContenido.getText().trim();
        String nuevoGramaje = txtGramaje.getText().trim();
        String nuevaPresentacion = txtPresentacion.getText().trim();
        String nuevaFarmaceutica = txtFarmaceutica.getText().trim();
        String nuevoTipo = txtTipo.getText().trim();

        // Campos numéricos/fecha
        String nuevoPrecioCompraStr = txtPrecio_compra.getText().trim();
        String nuevoPrecioVentaStr = txtPrecio_venta.getText().trim();
        String nuevaFechaCad = txtFecha_cad.getText().trim(); 
        String nuevoStockStr = txtStock.getText().trim();
        String nuevoMinStockStr = txtMin_stock.getText().trim();
        String nuevoMaxStockStr = txtMax_stock.getText().trim();

        // El campo 'activo' no tiene un textfield en tu lista, lo gestionamos como INTEGER
        // Asumimos que si no hay un control específico, el producto se mantiene activo (1).
        int nuevoActivo = 1; 

        double nuevoPrecioCompra, nuevoPrecioVenta;
        int nuevoStock, nuevoMinStock, nuevoMaxStock;

        // 2. --- Parseo de números y validación ---
        try {
            // Validación de campos numéricos (usando 0.0 o 0 si están vacíos para evitar error, o forzar la entrada)
            // Optamos por forzar la entrada para la actualización.
            if (nuevoPrecioCompraStr.isEmpty() || nuevoPrecioVentaStr.isEmpty() || 
                nuevoStockStr.isEmpty() || nuevoMinStockStr.isEmpty() || nuevoMaxStockStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Los campos de precio y stock (incluyendo min/max) no pueden estar vacíos.");
                return;
            }

            nuevoPrecioCompra = Double.parseDouble(nuevoPrecioCompraStr);
            nuevoPrecioVenta = Double.parseDouble(nuevoPrecioVentaStr);
            nuevoStock = Integer.parseInt(nuevoStockStr);
            nuevoMinStock = Integer.parseInt(nuevoMinStockStr);
            nuevoMaxStock = Integer.parseInt(nuevoMaxStockStr);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valores numéricos inválidos en precios, stock o mínimos/máximos.");
            return;
        }

        Connection conn = null;
        PreparedStatement psSelect = null, psUpdate = null;
        ResultSet rsActual = null;

        // 3. --- Validar que se haya seleccionado un producto ---
        if (codigoSeleccionado == null || codigoSeleccionado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto de la tabla para actualizar.");
            return;
        }

        try {
            conn = Conexion.conectar();

            // 4. --- Consulta para obtener valores actuales ---
            String sqlConsulta = "SELECT * FROM Productos WHERE cod_barras = ?";
            psSelect = conn.prepareStatement(sqlConsulta);
            psSelect.setString(1, codigoSeleccionado); 
            rsActual = psSelect.executeQuery();

            if (!rsActual.next()) {
                JOptionPane.showMessageDialog(this, "Producto no encontrado (quizás fue eliminado).");
                return;
            }

            // 5. --- Comparación de cambios (Alineado con TODOS los campos) ---
            boolean hayCambios =
                !nuevoNomCom.equals(rsActual.getString("nom_com")) ||
                !nuevaDescripcion.equals(rsActual.getString("descripcion")) ||
                !nuevoContenido.equals(rsActual.getString("contenido")) ||
                !nuevoGramaje.equals(rsActual.getString("gramaje")) ||
                !nuevaPresentacion.equals(rsActual.getString("presentacion")) ||
                !nuevaFarmaceutica.equals(rsActual.getString("farmaceutica")) ||
                !nuevoTipo.equals(rsActual.getString("tipo")) ||
                !nuevoCodBarras.equals(rsActual.getString("cod_barras")) || // Permite cambiar la PK
                Math.abs(nuevoPrecioCompra - rsActual.getDouble("precio_compra")) > 0.001 || // Comparación de doubles
                Math.abs(nuevoPrecioVenta - rsActual.getDouble("precio_venta")) > 0.001 || // Comparación de doubles
                nuevoStock != rsActual.getInt("stock") ||
                nuevoMinStock != rsActual.getInt("min_stock") ||
                nuevoMaxStock != rsActual.getInt("max_stock") ||
                nuevoActivo != rsActual.getInt("activo") || 
                !java.util.Objects.equals(nuevaFechaCad, rsActual.getString("fecha_cad")); 

            if (!hayCambios) {
                JOptionPane.showMessageDialog(this, "No se realizaron cambios.");
                return;
            }

            rsActual.close();
            psSelect.close();

            // 6. --- Sentencia UPDATE COMPLETA (15 campos + WHERE) ---
            String sqlUpdate = "UPDATE Productos SET " +
                "cod_barras = ?, nom_com = ?, descripcion = ?, contenido = ?, " +
                "gramaje = ?, presentacion = ?, farmaceutica = ?, tipo = ?, " +
                "precio_compra = ?, precio_venta = ?, fecha_cad = ?, stock = ?, " +
                "min_stock = ?, max_stock = ?, activo = ? " + 
                "WHERE cod_barras = ?"; 

            psUpdate = conn.prepareStatement(sqlUpdate);

            // 7. --- Asignación de Parámetros ---

            // 1. cod_barras (Nuevo valor)
            psUpdate.setString(1, nuevoCodBarras.isEmpty() ? null : nuevoCodBarras); 
            // 2. nom_com
            psUpdate.setString(2, nuevoNomCom);
            // 3. descripcion
            psUpdate.setString(3, nuevaDescripcion.isEmpty() ? null : nuevaDescripcion);
            // 4. contenido
            psUpdate.setString(4, nuevoContenido.isEmpty() ? null : nuevoContenido);
            // 5. gramaje
            psUpdate.setString(5, nuevoGramaje.isEmpty() ? null : nuevoGramaje);
            // 6. presentacion
            psUpdate.setString(6, nuevaPresentacion.isEmpty() ? null : nuevaPresentacion);
            // 7. farmaceutica
            psUpdate.setString(7, nuevaFarmaceutica.isEmpty() ? null : nuevaFarmaceutica);
            // 8. tipo
            psUpdate.setString(8, nuevoTipo.isEmpty() ? null : nuevoTipo);
            // 9. precio_compra (REAL)
            psUpdate.setDouble(9, nuevoPrecioCompra);
            // 10. precio_venta (REAL)
            psUpdate.setDouble(10, nuevoPrecioVenta);
            // 11. fecha_cad (TEXT)
            if (nuevaFechaCad == null || nuevaFechaCad.trim().isEmpty()) {
                psUpdate.setNull(11, java.sql.Types.VARCHAR);
            } else {
                psUpdate.setString(11, nuevaFechaCad);
            }
            // 12. stock (INTEGER)
            psUpdate.setInt(12, nuevoStock);
            // 13. min_stock (INTEGER)
            psUpdate.setInt(13, nuevoMinStock);
            // 14. max_stock (INTEGER)
            psUpdate.setInt(14, nuevoMaxStock);
            // 15. activo (INTEGER)
            psUpdate.setInt(15, nuevoActivo); 

            // 16. WHERE cod_barras (Valor original)
            psUpdate.setString(16, codigoSeleccionado); 

            int resultado = psUpdate.executeUpdate();

            // 8. --- Manejo de resultado ---
            if (resultado > 0) {
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
                // Actualiza la tabla y limpia la interfaz
                cargarMedicamentos(); 
                limpiarCampos(); 
                codigoSeleccionado = null; // Limpiamos el ID seleccionado
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el producto.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 9. --- Cierre de recursos ---
            try {
                if (rsActual != null) rsActual.close();
                if (psSelect != null) psSelect.close();
                if (psUpdate != null) psUpdate.close();
                Conexion.cerrar(conn);
            } catch (SQLException ex) {
                System.out.println("Error cerrando recursos: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void tablaCatalogoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaCatalogoMouseClicked
    int fila = tablaCatalogo.getSelectedRow();
        if (fila == -1) return;

        // Obtén el CÓDIGO DE BARRAS, asumiendo que está en la columna 4 de tu JTable
        String codigoDeLaFila = tablaCatalogo.getValueAt(fila, 4).toString();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Conexion.conectar();

            // Busca el producto por su cod_barras (Primary Key)
            String sql = "SELECT * FROM Productos WHERE cod_barras = ? LIMIT 1";

            ps = conn.prepareStatement(sql);
            ps.setString(1, codigoDeLaFila);

            rs = ps.executeQuery();

            if (rs.next()) {

                // 1. Asigna el código de barras a la variable de clase (para Update/Delete)
                codigoSeleccionado = rs.getString("cod_barras"); 

                // 2. Carga todos los valores en los JTextFields

                // Campos de Texto
                txtCod_barras.setText(rs.getString("cod_barras"));
                txtNom_com.setText(rs.getString("nom_com"));
                txtDescripcion.setText(rs.getString("descripcion"));
                txtContenido.setText(rs.getString("contenido"));
                txtGramaje.setText(rs.getString("gramaje"));
                txtFarmaceutica.setText(rs.getString("farmaceutica"));
                txtTipo.setText(rs.getString("tipo")); // Campo 'tipo'
                txtFecha_cad.setText(rs.getString("fecha_cad")); // Campo 'fecha_cad'

                // Campos Numéricos (convertidos a String)

                // Nota: txtPorcentajeV no está en la tabla, si es un campo calculado, se debe manejar aparte.
                // Aquí cargamos los que sí están en la tabla.

                // precio_compra (REAL)
                txtPrecio_compra.setText(String.valueOf(rs.getDouble("precio_compra"))); 
                // precio_venta (REAL)
                txtPrecio_venta.setText(String.valueOf(rs.getDouble("precio_venta"))); 
                // stock (INTEGER)
                txtStock.setText(String.valueOf(rs.getInt("stock")));
                // min_stock (INTEGER)
                txtMin_stock.setText(String.valueOf(rs.getInt("min_stock")));
                // max_stock (INTEGER)
                txtMax_stock.setText(String.valueOf(rs.getInt("max_stock")));

                // Si tienes un checkbox/textfield para 'activo', aquí iría su asignación
                // Por ejemplo, si fuera un campo de texto: 
                // txtActivo.setText(String.valueOf(rs.getInt("activo"))); 
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener detalles del producto: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cierre de recursos
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                Conexion.cerrar(conn);
            } catch (SQLException ex) {
                System.out.println("Error cerrando conexión: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_tablaCatalogoMouseClicked

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed

    if (codigoSeleccionado == null || codigoSeleccionado.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Selecciona primero un medicamento de la tabla.");
        return;
    }

    // El diálogo de confirmación está perfecto
    int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro que deseas eliminar este producto?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
    );

    if (confirmacion != JOptionPane.YES_OPTION) return;

    Connection conn = null;
    PreparedStatement ps = null;

    try {
        conn = Conexion.conectar();
        
        // --- CAMBIO 2: Consulta SQL ajustada ---
        // Usamos 'activo = 0' (INTEGER) en lugar de 'activo = FALSE' (BOOLEAN)
        // Usamos 'WHERE cod_barras = ?' (nuestra PK de texto)
        String sql = "UPDATE Productos SET activo = 0 WHERE cod_barras = ?";
        
        ps = conn.prepareStatement(sql);
        
        // --- CAMBIO 3: Asignar el parámetro (String) ---
        ps.setString(1, codigoSeleccionado);

        int resultado = ps.executeUpdate();

        if (resultado > 0) {
            JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.");
            limpiarCampos();        // ← Asumiendo que este método existe
            cargarMedicamentos();   // ← Esto recarga la tabla y quita el producto
            
            // --- CAMBIO 4: Resetear la variable de selección (String) ---
            codigoSeleccionado = null; 
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto.");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        e.printStackTrace(); // Bueno para depurar
    } finally {
        // Tu bloque 'finally' está correcto
        try {
            if (ps != null) ps.close();
            Conexion.cerrar(conn); // Asumiendo que este método existe
        } catch (SQLException ex) {
            System.out.println("Error cerrando conexión: " + ex.getMessage());
        }
    }

    }//GEN-LAST:event_btnEliminarActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Inventario().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Main;
    private javax.swing.JButton Resetbtn;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton buscar;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tablaCatalogo;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCod_barras;
    private javax.swing.JTextField txtContenido;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtFarmaceutica;
    private javax.swing.JTextField txtFecha_cad;
    private javax.swing.JTextField txtGramaje;
    private javax.swing.JTextField txtMax_stock;
    private javax.swing.JTextField txtMin_stock;
    private javax.swing.JTextField txtNom_com;
    private javax.swing.JTextField txtPorcentajeV;
    private javax.swing.JTextField txtPrecio_compra;
    private javax.swing.JTextField txtPrecio_venta;
    private javax.swing.JTextField txtPresentacion;
    private javax.swing.JTextField txtStock;
    private javax.swing.JTextField txtTipo;
    // End of variables declaration//GEN-END:variables
}
