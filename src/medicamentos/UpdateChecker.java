package medicamentos;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * UpdateChecker: consulta GitHub Releases, descarga ZIP y SHA256, verifica y despliega
 * Requisitos:
 *  - Añadir org.json (https://github.com/stleary/JSON-java) al classpath/lib
 *  - Publicar en cada Release assets: FarmaApp-<platform>.zip y FarmaApp-<platform>.sha256
 *
 * NOTA:
 *  - No modifica la carpeta de datos (AppPaths.getDefaultAppDataDir()/data)
 *  - Hace backup de la BD antes de ejecutar migraciones
 */
public class UpdateChecker {

    // Cambia esto por el repo correcto si cambia
    private static final String GITHUB_LATEST = "https://api.github.com/repos/SpeedChela/FarmaApp/releases/latest";

    // Rutas dentro de AppPaths
    private static final Path APP_BASE = AppPaths.getDefaultAppDataDir().toPath();
    private static final Path INSTANCES_DIR = APP_BASE.resolve("app");   // donde se extraen versiones: .../app/v1.2.0/...
    private static final Path DATA_DIR = APP_BASE.resolve("data");       // datos: .../data/farma.db
    private static final Path DB_FILE = DATA_DIR.resolve("farma.db");    // ajusta si tu DB se llama distinto

    /**
     * Comprueba la última release; si hay una versión distinta a currentTag,
     * la descarga, valida checksum y la extrae a INSTANCES_DIR/<tag>.
     * Si encuentra FarmaApp.jar en el ZIP lo arranca y sale del proceso actual.
     *
     * Llamar desde un hilo en background.
     *
     * @param currentTag versión actual (ej. "v1.0.0")
     */
    public static void checkAndApplyIfNeeded(String currentTag) {
        try {
            String json = httpGet(GITHUB_LATEST);
            JSONObject release = new JSONObject(json);
            String remoteTag = release.optString("tag_name", "");
            if (remoteTag.isEmpty() || !isNewer(remoteTag, currentTag)) {
                return; // no update
            }

            String platformKey = detectPlatformKey(); // windows-x64, linux-x64, mac-x64
            JSONArray assets = release.getJSONArray("assets");

            String zipUrl = null;
            String shaUrl = null;
            String zipName = null;

            for (int i = 0; i < assets.length(); i++) {
                JSONObject a = assets.getJSONObject(i);
                String name = a.getString("name");
                if (name.contains(platformKey) && name.endsWith(".zip")) {
                    zipUrl = a.getString("browser_download_url");
                    zipName = name;
                }
                if (name.contains(platformKey) && name.endsWith(".sha256")) {
                    shaUrl = a.getString("browser_download_url");
                }
            }

            if (zipUrl == null) throw new IOException("No release asset for platform: " + platformKey);

            // descargar zip
            Path tmpZip = Files.createTempFile("farma-update-", ".zip");
            downloadToFile(zipUrl, tmpZip);

            // descargar sha si existe y validar
            if (shaUrl != null) {
                Path tmpSha = Files.createTempFile("farma-update-", ".sha256");
                downloadToFile(shaUrl, tmpSha);
                String shaTxt = new String(Files.readAllBytes(tmpSha)).trim();
                // acepta formatos "hash  filename" o solo hash
                String expected = shaTxt.split("\\s+")[0];
                if (!checkSha256(tmpZip, expected)) {
                    Files.deleteIfExists(tmpZip);
                    Files.deleteIfExists(tmpSha);
                    throw new SecurityException("Checksum mismatch for downloaded artifact.");
                }
                Files.deleteIfExists(tmpSha);
            }

            // extraer ZIP a carpeta versionada
            Path targetDir = INSTANCES_DIR.resolve(remoteTag);
            if (!Files.exists(targetDir)) Files.createDirectories(targetDir);
            unzip(tmpZip, targetDir);
            Files.deleteIfExists(tmpZip);

            // backup DB (si existe)
            backupDatabaseIfExists();

            // Opcional: ejecutar migraciones aquí (Flyway) - ver guía abajo
            // runMigrationsIfNeeded(DB_FILE.toString());

            // Ejecutar la versión nueva: asumimos que dentro del zip hay FarmaApp.jar
            Path candidateJar = targetDir.resolve("FarmaApp.jar");
            if (Files.exists(candidateJar)) {
                // Lanzar nuevo proceso y salir
                String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                new ProcessBuilder(javaBin, "-jar", candidateJar.toAbsolutePath().toString()).start();
                System.exit(0);
            } else {
                // Si tu paquete es nativo (exe), localiza según convención y lanza
                Path exeCandidate = findExecutableInDir(targetDir);
                if (exeCandidate != null) {
                    new ProcessBuilder(exeCandidate.toAbsolutePath().toString()).start();
                    System.exit(0);
                }
            }

        } catch (Throwable ex) {
            // No queremos romper la app si hay error de actualización; loguear y seguir
            ex.printStackTrace();
        }
    }

    private static Path findExecutableInDir(Path dir) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path p : ds) {
                String name = p.getFileName().toString().toLowerCase();
                if (name.endsWith(".exe") || name.endsWith(".AppImage") || name.endsWith(".bin")) {
                    return p;
                }
            }
        }
        return null;
    }

    private static String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.setRequestProperty("User-Agent", "FarmaApp-Updater");
        try (InputStream in = conn.getInputStream()) {
            return new String(in.readAllBytes(), "UTF-8");
        }
    }

    private static void downloadToFile(String urlStr, Path dest) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean checkSha256(Path file, String expectedHex) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) > 0) md.update(buf, 0, r);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString().equalsIgnoreCase(expectedHex);
    }

    private static void unzip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path out = targetDir.resolve(entry.getName()).normalize();
                if (!out.startsWith(targetDir)) throw new IOException("Zip entry outside target: " + entry.getName());
                if (entry.isDirectory()) Files.createDirectories(out);
                else {
                    Files.createDirectories(out.getParent());
                    try (OutputStream os = Files.newOutputStream(out)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = zis.read(buf)) > 0) os.write(buf, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static void backupDatabaseIfExists() throws IOException {
        if (Files.exists(DB_FILE)) {
            Path bak = DB_FILE.resolveSibling("farma.db.bak_" + System.currentTimeMillis());
            Files.copy(DB_FILE, bak, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean isNewer(String remote, String local) {
        // simple compare: si diferente -> actualizar; podrías mejorar con SemVer
        if (remote == null || remote.isEmpty() || local == null || local.isEmpty()) return !remote.equals(local);
        return !remote.equals(local);
    }

    private static String detectPlatformKey() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").contains("64") ? "x64" : "x86";
        if (os.contains("win")) return "windows-" + arch;
        if (os.contains("mac")) return "mac-" + arch;
        return "linux-" + arch;
    }
}