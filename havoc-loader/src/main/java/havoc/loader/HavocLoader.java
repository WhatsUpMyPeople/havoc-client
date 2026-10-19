/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 Havoc.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.loader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Havoc self-updater.
 *
 * <p>On client startup it hits {@value #MANIFEST_URL}, parses the JSON manifest, compares the
 * server version with the currently loaded {@code havoc} mod version, and if a newer build is
 * available downloads the new payload jar to {@code .minecraft/mods/havoc-payload.jar.pending}
 * (SHA-256 verified). A JVM shutdown hook spawns a tiny OS helper that waits two seconds for
 * Fabric to release its file lock, then renames the pending file over {@code havoc-payload.jar}.
 * The next Minecraft launch loads the new build automatically.</p>
 *
 * <p>No license-key enforcement in this phase — purely a public update channel. Phase 2 will
 * add a licensed endpoint keyed by Discord ID.</p>
 */
public final class HavocLoader implements ClientModInitializer {

    private static final Logger LOG = LoggerFactory.getLogger("Havoc/Loader");

    /** Where the update manifest lives. Served by GitHub Pages from the repo's docs/ folder. */
    private static final String MANIFEST_URL =
            "https://whatsupmypeople.github.io/havoc-client/latest.json";

    /** The fixed filename for the installed payload jar. */
    private static final String PAYLOAD_FILENAME = "havoc-payload.jar";
    private static final String PAYLOAD_PENDING  = "havoc-payload.jar.pending";

    /** Mod id of the payload — matches gradle.properties {@code mod_id}. */
    private static final String PAYLOAD_MOD_ID = "havoc";

    /** Networking: short, since the client should launch promptly even on bad links. */
    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int READ_TIMEOUT_MS    = 15000;

    @Override
    public void onInitializeClient() {
        // Run the update check off the client thread so a slow/missing update server never
        // delays the main menu rendering.
        Thread t = new Thread(this::checkForUpdate, "Havoc Loader Updater");
        t.setDaemon(true);
        t.start();
    }

    // =======================================================================
    // Update flow
    // =======================================================================
    private void checkForUpdate() {
        try {
            String currentVersion = currentPayloadVersion();
            LOG.info("Havoc loader — running payload v{}", currentVersion);

            Manifest manifest = fetchManifest();
            if (manifest == null) {
                LOG.info("Havoc loader — no manifest reachable; keeping current payload");
                return;
            }
            if (!isNewer(manifest.version, currentVersion)) {
                LOG.info("Havoc loader — already on the latest payload (v{})", currentVersion);
                return;
            }

            LOG.info("Havoc loader — update available: v{} -> v{}", currentVersion, manifest.version);
            Path modsDir  = modsDirectory();
            Path pending  = modsDir.resolve(PAYLOAD_PENDING);
            Path target   = modsDir.resolve(PAYLOAD_FILENAME);

            downloadAndVerify(manifest, pending);
            scheduleSwap(pending, target);
            notifyUserInChat(manifest.version);
        } catch (Throwable t) {
            LOG.warn("Havoc loader — update check failed: {}", t.toString());
        }
    }

    // =======================================================================
    // Manifest
    // =======================================================================
    private static final class Manifest {
        final String version;
        final String url;
        final String sha256;
        Manifest(String version, String url, String sha256) {
            this.version = version;
            this.url = url;
            this.sha256 = sha256 == null ? "" : sha256.toLowerCase();
        }
    }

    private Manifest fetchManifest() {
        try {
            String body = httpGet(MANIFEST_URL);
            if (body == null || body.isBlank()) return null;
            String version = jsonString(body, "version");
            String url     = jsonString(body, "url");
            String sha     = jsonString(body, "sha256");
            if (version == null || url == null) return null;
            return new Manifest(version, url, sha);
        } catch (Exception e) {
            LOG.warn("Havoc loader — manifest fetch failed: {}", e.toString());
            return null;
        }
    }

    /** Minimal JSON string-field extractor. Avoids pulling in gson for a 50 KB loader. */
    private static String jsonString(String json, String key) {
        String needle = "\"" + key + "\"";
        int k = json.indexOf(needle);
        if (k < 0) return null;
        int colon = json.indexOf(':', k + needle.length());
        if (colon < 0) return null;
        int quote = json.indexOf('"', colon + 1);
        if (quote < 0) return null;
        StringBuilder out = new StringBuilder();
        for (int i = quote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) { out.append(json.charAt(++i)); continue; }
            if (c == '"') return out.toString();
            out.append(c);
        }
        return null;
    }

    // =======================================================================
    // HTTP + download + hash
    // =======================================================================
    private static String httpGet(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
        c.setConnectTimeout(CONNECT_TIMEOUT_MS);
        c.setReadTimeout(READ_TIMEOUT_MS);
        c.setRequestProperty("User-Agent", "HavocLoader/1 (+" + MANIFEST_URL + ")");
        c.setInstanceFollowRedirects(true);
        c.setRequestMethod("GET");
        int code = c.getResponseCode();
        if (code / 100 != 2) throw new RuntimeException("HTTP " + code + " for " + url);
        try (InputStream in = c.getInputStream(); ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            in.transferTo(buf);
            return buf.toString(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private void downloadAndVerify(Manifest manifest, Path dest) throws Exception {
        Files.createDirectories(dest.getParent());
        Path tmp = dest.resolveSibling(dest.getFileName() + ".downloading");
        Files.deleteIfExists(tmp);

        LOG.info("Havoc loader — downloading {} -> {}", manifest.url, tmp);
        HttpURLConnection c = (HttpURLConnection) URI.create(manifest.url).toURL().openConnection();
        c.setConnectTimeout(CONNECT_TIMEOUT_MS);
        c.setReadTimeout(READ_TIMEOUT_MS * 10); // the payload is big; be patient
        c.setRequestProperty("User-Agent", "HavocLoader/1");
        c.setInstanceFollowRedirects(true);

        int code = c.getResponseCode();
        if (code / 100 != 2) throw new RuntimeException("payload HTTP " + code);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = c.getInputStream();
             java.io.OutputStream out = Files.newOutputStream(tmp,
                     java.nio.file.StandardOpenOption.CREATE,
                     java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                     java.nio.file.StandardOpenOption.WRITE)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                md.update(buf, 0, n);
            }
        }

        String got = HexFormat.of().formatHex(md.digest());
        if (!manifest.sha256.isEmpty() && !manifest.sha256.equals(got)) {
            try { Files.deleteIfExists(tmp); } catch (Exception ignored) {}
            throw new RuntimeException("sha256 mismatch: manifest=" + manifest.sha256 + " got=" + got);
        }
        Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING);
        LOG.info("Havoc loader — downloaded, verified sha256={}", got);
    }

    // =======================================================================
    // Swap-on-exit
    //
    // Fabric holds a file lock on the currently-loaded havoc-payload.jar for as long as the JVM
    // is alive. We can't overwrite it from inside this process on Windows. The trick is to
    // spawn a small OS-native helper on JVM shutdown that waits a couple of seconds for the
    // lock to release and then does the file rename. That helper outlives this JVM.
    // =======================================================================
    private void scheduleSwap(Path pending, Path target) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                spawnSwapHelper(pending, target);
            } catch (Throwable t) {
                // Can't log reliably from a shutdown hook; swallow.
            }
        }, "Havoc Loader Swap"));
    }

    private void spawnSwapHelper(Path pending, Path target) throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // powershell -Command "Start-Sleep -Seconds 3; Move-Item -Force -Path pending -Destination target"
            new ProcessBuilder("powershell", "-NoProfile", "-WindowStyle", "Hidden", "-Command",
                    "Start-Sleep -Seconds 3; " +
                    "if (Test-Path '" + escapeForPwsh(pending.toString()) + "') { " +
                    "  Move-Item -Force -Path '" + escapeForPwsh(pending.toString()) + "' " +
                              "-Destination '" + escapeForPwsh(target.toString()) + "' " +
                    "}"
            ).redirectErrorStream(true).start();
        } else {
            // sh -c "sleep 3; mv -f <pending> <target>"
            new ProcessBuilder("sh", "-c",
                    "sleep 3; mv -f '" + pending.toString().replace("'", "'\\''") + "' " +
                    "'" + target.toString().replace("'", "'\\''") + "'"
            ).redirectErrorStream(true).start();
        }
    }

    private static String escapeForPwsh(String s) {
        return s.replace("'", "''");
    }

    // =======================================================================
    // Version resolution + comparison
    // =======================================================================
    /** The version currently loaded (as Fabric reports). "0.0.0" means payload not installed yet. */
    private String currentPayloadVersion() {
        Optional<ModContainer> payload = FabricLoader.getInstance().getModContainer(PAYLOAD_MOD_ID);
        return payload.map(mc -> mc.getMetadata().getVersion().getFriendlyString()).orElse("0.0.0");
    }

    /** Semver-ish: splits on dots, compares integer parts; non-numeric parts fall back to string compare. */
    static boolean isNewer(String a, String b) {
        if (a == null) return false;
        if (b == null) return true;
        String[] aa = a.split("[.\\-+]");
        String[] bb = b.split("[.\\-+]");
        int len = Math.max(aa.length, bb.length);
        for (int i = 0; i < len; i++) {
            String ap = i < aa.length ? aa[i] : "0";
            String bp = i < bb.length ? bb[i] : "0";
            try {
                long ai = Long.parseLong(ap);
                long bi = Long.parseLong(bp);
                if (ai != bi) return ai > bi;
            } catch (NumberFormatException nfe) {
                int cmp = ap.compareTo(bp);
                if (cmp != 0) return cmp > 0;
            }
        }
        return false;
    }

    // =======================================================================
    // Filesystem
    // =======================================================================
    private static Path modsDirectory() {
        return FabricLoader.getInstance().getGameDir().resolve("mods");
    }

    // =======================================================================
    // In-game chat notification (best-effort, fired once Minecraft is ready)
    // =======================================================================
    private void notifyUserInChat(String newVersion) {
        // The client thread may not be fully initialized when this fires; schedule onto the
        // Minecraft executor and let the server-side chat system render it when safe.
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return;
            mc.execute(() -> {
                if (mc.gui != null) {
                    mc.gui.getChat().addMessage(Component.literal(
                            "§c§lHavoc §r§7— update downloaded (§f" + newVersion
                                    + "§7). Restart Minecraft to apply."));
                }
            });
        } catch (Throwable ignored) {
            // Chat is a nice-to-have. If it fails, the user sees the update next launch anyway.
        }
    }
}
