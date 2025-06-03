package me.minebuilders.clearlag;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Modern updater for Clearlag that uses the GitHub API
 */
public class BukkitUpdater implements Runnable {

    // GitHub API URL for releases
    private static final String GITHUB_API_URL = "https://api.github.com/repos/minebuilders/clearlag/releases/latest";

    // Connection timeouts
    private static final int CONNECT_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 15000;    // 15 seconds

    // User agent for GitHub API
    private static final String USER_AGENT = "Clearlag-Updater/4.1.0";

    private String newVersion;
    private String downloadUrl;
    private final File pluginFile;

    /**
     * Creates a new updater instance and starts the update check asynchronously
     * @param pluginFile The plugin JAR file
     */
    public BukkitUpdater(File pluginFile) {
        this.pluginFile = pluginFile;

        // Start the update check asynchronously
        CompletableFuture.runAsync(this)
            .exceptionally(ex -> {
                Util.warning("Failed to check for updates: " + ex.getMessage());
                if (Clearlag.getInstance().getConfig().getBoolean("settings.debug-mode", false)) {
                    ex.printStackTrace();
                }
                return null;
            });
    }

    /**
     * Compares version strings in a more reliable way
     * @param currentVersion The current version
     * @param newVersion The new version to compare against
     * @return True if the new version is newer than the current version
     */
    private boolean isNewerVersion(String currentVersion, String newVersion) {
        try {
            // Remove any non-numeric prefixes (like "v")
            currentVersion = currentVersion.replaceAll("^[^0-9]*", "");
            newVersion = newVersion.replaceAll("^[^0-9]*", "");

            String[] currentParts = currentVersion.split("\\.");
            String[] newParts = newVersion.split("\\.");

            // Compare each part of the version
            int length = Math.max(currentParts.length, newParts.length);
            for (int i = 0; i < length; i++) {
                int currentPart = i < currentParts.length ? 
                    Integer.parseInt(currentParts[i]) : 0;
                int newPart = i < newParts.length ? 
                    Integer.parseInt(newParts[i]) : 0;

                if (newPart > currentPart) {
                    return true;
                } else if (newPart < currentPart) {
                    return false;
                }
            }

            // If we get here, the versions are equal
            return false;
        } catch (NumberFormatException e) {
            // Fall back to simple string comparison if parsing fails
            Util.warning("Failed to parse version numbers, falling back to string comparison");
            return !currentVersion.equals(newVersion);
        }
    }

    /**
     * Checks if an update is available
     * @return A CompletableFuture that resolves to true if an update is available
     */
    private CompletableFuture<Boolean> checkForUpdate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Util.log("Checking for updates...");

                HttpURLConnection connection = createConnection(GITHUB_API_URL);

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                    JSONParser parser = new JSONParser();
                    JSONObject releaseInfo = (JSONObject) parser.parse(reader);

                    // Get the tag name (version)
                    this.newVersion = ((String) releaseInfo.get("tag_name")).trim();

                    // Get the download URL from the assets
                    JSONArray assets = (JSONArray) releaseInfo.get("assets");
                    if (assets != null && !assets.isEmpty()) {
                        JSONObject asset = (JSONObject) assets.get(0);
                        this.downloadUrl = (String) asset.get("browser_download_url");
                    } else {
                        // Fallback to the zipball URL if no assets
                        this.downloadUrl = (String) releaseInfo.get("zipball_url");
                    }

                    String currentVersion = Clearlag.getInstance().getDescription().getVersion();
                    boolean isNewer = isNewerVersion(currentVersion, newVersion);

                    if (isNewer) {
                        Util.log("New version available: " + newVersion + " (current: " + currentVersion + ")");
                    } else {
                        Util.log("You are running the latest version of Clearlag");
                    }

                    return isNewer;
                }
            } catch (IOException | ParseException e) {
                Util.warning("Failed to check for updates: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Creates an HTTP connection with proper timeouts and headers
     * @param url The URL to connect to
     * @return The configured HttpURLConnection
     * @throws IOException If an I/O error occurs
     */
    private HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

        return connection;
    }

    /**
     * Runs the update check and downloads the update if available
     */
    @Override
    public void run() {
        try {
            // Check for updates
            boolean updateAvailable = checkForUpdate().get(30, TimeUnit.SECONDS);

            if (updateAvailable && downloadUrl != null) {
                downloadUpdate();
            }
        } catch (Exception e) {
            Util.warning("Failed to check for updates: " + e.getMessage());
            if (Clearlag.getInstance().getConfig().getBoolean("settings.debug-mode", false)) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Downloads the update to the server's update folder
     */
    private void downloadUpdate() {
        try {
            Util.log("Downloading Clearlag version " + newVersion + "...");

            // Ensure update folder exists
            File updateFolder = Bukkit.getUpdateFolderFile();
            if (!updateFolder.exists() && !updateFolder.mkdirs()) {
                Util.warning("Failed to create update folder");
                return;
            }

            // Create connection to download URL
            HttpURLConnection connection = createConnection(downloadUrl);

            // Follow redirects if needed
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || 
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String newUrl = connection.getHeaderField("Location");
                connection = createConnection(newUrl);
            }

            // Download the file
            File outputFile = new File(updateFolder, pluginFile.getName());
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            Util.log("Update downloaded successfully! Restart your server to apply the update.");

        } catch (IOException e) {
            Util.warning("Failed to download update: " + e.getMessage());
            if (Clearlag.getInstance().getConfig().getBoolean("settings.debug-mode", false)) {
                e.printStackTrace();
            }
        }
    }
}
