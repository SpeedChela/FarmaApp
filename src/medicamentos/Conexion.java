/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package medicamentos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class Conexion {
    
    // Ruta relativa a la base de datos
    private static final String DB_PATH = "datbases/FarmaApp.db";

    public static Connection conectar() {
        Connection conn = null;
        try {
            // Obtener la ruta absoluta
            File dbFile = new File(DB_PATH);
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            // Conectar con SQLite
            conn = DriverManager.getConnection(url);
            System.out.println("Conexión exitosa a SQLite.");
        } catch (SQLException e) {
            System.out.println("Error al conectar con SQLite: " + e.getMessage());
        }
        return conn;
    }

    public static void cerrar(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
