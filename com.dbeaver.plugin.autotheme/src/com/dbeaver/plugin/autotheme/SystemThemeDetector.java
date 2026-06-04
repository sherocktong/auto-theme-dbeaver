package com.dbeaver.plugin.autotheme;

import org.eclipse.swt.widgets.Display;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Cross-platform OS dark-mode detector.
 *
 * <p>SWT's {@code Display.isSystemDarkTheme()} works on some platforms but on macOS
 * it returns the value cached at application startup and does not reflect runtime
 * system-appearance changes. This class provides a platform-specific fallback.</p>
 */
public final class SystemThemeDetector {

    private static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");

    private SystemThemeDetector() {
        // utility
    }

    /**
     * Returns whether the OS is currently in dark mode.
     *
     * @return {@code true} if the OS appearance is dark
     */
    public static boolean isSystemDarkTheme() {
        if (IS_MAC) {
            return isMacDarkMode();
        }
        Display display = Display.getDefault();
        if (display == null || display.isDisposed()) {
            return false;
        }
        return display.isSystemDarkTheme();
    }

    /**
     * macOS: read the live appearance preference via {@code defaults}.
     * This reflects changes made in System Settings in real time.
     */
    private static boolean isMacDarkMode() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "defaults", "read", "-g", "AppleInterfaceStyle");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("[AutoTheme] defaults read timed out, falling back to SWT");
                Display display = Display.getDefault();
                return display != null && !display.isDisposed() && display.isSystemDarkTheme();
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                boolean dark = line != null && line.trim().equalsIgnoreCase("Dark");
                return dark;
            }
        } catch (Exception e) {
            System.err.println("[AutoTheme] Failed to detect macOS dark mode: " + e.getMessage());
            // Fall back to SWT
            Display display = Display.getDefault();
            if (display != null && !display.isDisposed()) {
                return display.isSystemDarkTheme();
            }
            return false;
        }
    }
}
