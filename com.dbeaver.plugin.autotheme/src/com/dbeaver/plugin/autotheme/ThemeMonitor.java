package com.dbeaver.plugin.autotheme;

import com.dbeaver.plugin.autotheme.preferences.AutoThemePreferences;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitors the OS system theme and triggers theme switches in DBeaver.
 * Registered via the {@code org.eclipse.ui.startup} extension point.
 */
public class ThemeMonitor implements IStartup {

    private static ThemeMonitor instance;

    private volatile Boolean lastKnownDarkMode;
    private volatile boolean disposed = false;
    private ScheduledExecutorService scheduler;

    @Override
    public void earlyStartup() {
        // Ensure defaults are set before any check — IStartup can run before Activator.start().
        AutoThemePreferences.ensureDefaults();

        Display display = Display.getDefault();
        if (display == null || display.isDisposed()) {
            System.err.println("[AutoTheme] Display not available, aborting startup.");
            return;
        }

        instance = this;
        System.out.println("[AutoTheme] Monitor started. Enabled=" + AutoThemePreferences.isEnabled()
                + " interval=" + AutoThemePreferences.getPollInterval() + "ms"
                + " light=" + AutoThemePreferences.getLightThemeId()
                + " dark=" + AutoThemePreferences.getDarkThemeId());

        // Detect current system theme and sync DBeaver to match on startup.
        boolean systemDark = SystemThemeDetector.isSystemDarkTheme();
        lastKnownDarkMode = systemDark;
        System.out.println("[AutoTheme] Initial OS dark mode: " + systemDark);

        if (AutoThemePreferences.isEnabled()) {
            String targetThemeId = systemDark
                    ? AutoThemePreferences.getDarkThemeId()
                    : AutoThemePreferences.getLightThemeId();
            System.out.println("[AutoTheme] Startup sync: switching to " + targetThemeId);
            // Delay the startup repaint so the workbench finishes painting all its
            // shells first. Without the delay the shell list is empty or partially
            // constructed and applyStyles has nothing to repaint, leaving the pink
            // background visible.
            ThemeSwitchJob job = new ThemeSwitchJob(targetThemeId);
            job.schedule(2000);
        }

        try {
            int interval = AutoThemePreferences.getPollInterval();
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "AutoTheme-Monitor");
                t.setDaemon(true);
                return t;
            });
            scheduler.scheduleAtFixedRate(this::checkAndSwitch, interval, interval, TimeUnit.MILLISECONDS);
            System.out.println("[AutoTheme] Scheduler started.");
        } catch (Exception e) {
            System.err.println("[AutoTheme] Failed to start scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ThemeMonitor getInstance() {
        return instance;
    }

    private void checkAndSwitch() {
        if (disposed) {
            return;
        }
        try {
            boolean currentDarkMode = SystemThemeDetector.isSystemDarkTheme();

            if (lastKnownDarkMode == null || lastKnownDarkMode != currentDarkMode) {
                lastKnownDarkMode = currentDarkMode;
                String targetThemeId = currentDarkMode
                        ? AutoThemePreferences.getDarkThemeId()
                        : AutoThemePreferences.getLightThemeId();
                System.out.println("[AutoTheme] OS theme changed. Dark=" + currentDarkMode
                        + " -> switching to " + targetThemeId);
                ThemeSwitchJob.switchToTheme(targetThemeId);
            }
        } catch (Exception e) {
            System.err.println("[AutoTheme] Error during theme check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void dispose() {
        disposed = true;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        System.out.println("[AutoTheme] Monitor disposed.");
    }
}
