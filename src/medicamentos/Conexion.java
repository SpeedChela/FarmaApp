/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package medicamentos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Conexion {
    
    // Ruta relativa original a la base de datos (se mantiene como fallback)
    private static final String DB_RELATIVE_PATH = "datbases/FarmaApp.db";
    // Carpeta dentro de %APPDATA% donde buscaremos/guardaremos la BD
    private static final String APP_FOLDER = "FarmaApp";

    public static Connection conectar() {
        Connection conn = null;
        try {
            String dbPath = locateDbPath();
            String url = "jdbc:sqlite:" + dbPath;

            // Conectar con SQLite
            conn = DriverManager.getConnection(url);
            System.out.println("Conexión exitosa a SQLite. DB en: " + dbPath);
        } catch (IOException e) {
            System.out.println("Error de E/S al localizar la base de datos: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error al conectar con SQLite: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Localiza la ruta de la base de datos en este orden:
     * 1) Si existe la ruta relativa original (DB_RELATIVE_PATH), la usa.
     * 2) Busca en %APPDATA%/<APP_FOLDER>/FarmaApp.db (Windows Roaming).
     * 3) Busca en %LOCALAPPDATA%/<APP_FOLDER>/FarmaApp.db (Windows Local).
     * 4) Busca en $XDG_CONFIG_HOME/<APP_FOLDER>/FarmaApp.db o ~/ .config/<APP_FOLDER>/FarmaApp.db (Unix-like).
     * Si la carpeta padre no existe, la crea para que SQLite pueda crear el archivo.
     */
    private static String locateDbPath() throws IOException {
        // 1) Comprobar ruta relativa existente (compatibilidad con la versión previa)
        File rel = new File(DB_RELATIVE_PATH);
        if (rel.exists()) {
            return rel.getAbsolutePath();
        }

        // Nombre del archivo (por ejemplo "FarmaApp.db")
        String dbFileName = new File(DB_RELATIVE_PATH).getName();

        // 2) %APPDATA% (Roaming) - Windows
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isEmpty()) {
            Path p = Paths.get(appData, APP_FOLDER, dbFileName);
            ensureParentExists(p);
            return p.toAbsolutePath().toString();
        }

        // 3) %LOCALAPPDATA% (Windows local)
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isEmpty()) {
            Path p = Paths.get(localAppData, APP_FOLDER, dbFileName);
            ensureParentExists(p);
            return p.toAbsolutePath().toString();
        }

        // 4) Unix-like: XDG_CONFIG_HOME or ~/.config
        String xdg = System.getenv("XDG_CONFIG_HOME");
        String userHome = System.getProperty("user.home");
        Path configBase;
        if (xdg != null && !xdg.isEmpty()) {
            configBase = Paths.get(xdg);
        } else {
            configBase = Paths.get(userHome, ".config");
        }
        Path p = configBase.resolve(APP_FOLDER).resolve(dbFileName);
        ensureParentExists(p);
        return p.toAbsolutePath().toString();
    }

    private static void ensureParentExists(Path dbPath) throws IOException {
        Path parent = dbPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
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