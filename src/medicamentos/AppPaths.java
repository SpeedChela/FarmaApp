package medicamentos;

import java.io.File;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Manejo de rutas de datos de la aplicación (portable y poco visible).
 */
public final class AppPaths {
    private static final String APP_NAME = "FarmaApp";
    private static final String PREF_NODE = "com.farmaapp";
    private static final String PREF_TICKETS_DIR = "ticketsDir";

    private AppPaths() {}

    public static File getDefaultAppDataDir() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        File dir;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                dir = new File(appData, APP_NAME);
            } else {
                dir = new File(userHome, APP_NAME);
            }
        } else if (os.contains("mac")) {
            dir = new File(userHome + "/Library/Application Support/" + APP_NAME);
        } else {
            String xdg = System.getenv("XDG_DATA_HOME");
            if (xdg != null && !xdg.isEmpty()) {
                dir = new File(xdg, APP_NAME);
            } else {
                dir = new File(userHome + "/.local/share/" + APP_NAME);
            }
        }
        return dir;
    }

    /**
     * Carpeta de tickets preferida si está guardada en Preferences,
     * si no devuelve <appData>/tickets (no crea).
     */
    public static File getStoredOrDefaultTicketsDir() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String saved = prefs.get(PREF_TICKETS_DIR, "").trim();
        if (!saved.isEmpty()) {
            return new File(saved);
        }
        File base = getDefaultAppDataDir();
        return new File(base, "tickets");
    }

    public static void storeTicketsDir(File dir) {
        if (dir == null) return;
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(PREF_TICKETS_DIR, dir.getAbsolutePath());
    }

    /**
     * Intenta asegurar que la carpeta exista (intenta crearla) y devuelve la carpeta final.
     * Si no puede crearla devuelve null.
     */
    public static File ensureTicketsDirExists() {
        File dir = getStoredOrDefaultTicketsDir();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return dir;
    }
}