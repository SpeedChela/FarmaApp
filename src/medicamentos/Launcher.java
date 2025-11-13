package medicamentos;

import javax.swing.SwingUtilities;

/**
 * Launcher pequeño: lanza el UpdateChecker en background y luego muestra Login.
 * Ajusta currentVersion a la versión que distribuyes (ej. "v1.0.0").
 */
public class Launcher {
    private static final String CURRENT_TAG = "v1.0.0"; // actualiza con tu versión

    public static void main(String[] args) {
        // Start updater in background (no bloquear UI)
        Thread updater = new Thread(() -> UpdateChecker.checkAndApplyIfNeeded(CURRENT_TAG), "Updater");
        updater.setDaemon(true);
        updater.start();

        // Start UI (Login) on EDT
        SwingUtilities.invokeLater(() -> {
            // suponiendo que tienes una clase Login con constructor vacío
            try {
                new Login().setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}