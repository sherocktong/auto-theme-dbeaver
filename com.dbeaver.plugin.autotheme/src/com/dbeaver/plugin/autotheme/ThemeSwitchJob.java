package com.dbeaver.plugin.autotheme;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * UI job that switches the Eclipse CSS theme to the requested ID.
 */
public class ThemeSwitchJob extends UIJob {

    private final String themeId;

    public ThemeSwitchJob(String themeId) {
        super("Switch DBeaver Theme");
        this.themeId = themeId;
        setUser(false);
        setSystem(true);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        if (themeId == null || themeId.isBlank()) {
            System.err.println("[AutoTheme] ThemeSwitchJob: empty themeId");
            return Status.OK_STATUS;
        }

        Display display = Display.getCurrent();
        if (display == null || display.isDisposed()) {
            System.err.println("[AutoTheme] ThemeSwitchJob: no valid display");
            return Status.OK_STATUS;
        }

        IThemeEngine engine = getThemeEngine(display);
        if (engine == null) {
            System.err.println("[AutoTheme] ThemeSwitchJob: IThemeEngine not available");
            return new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                    "Theme engine not available");
        }

        ITheme targetTheme = null;
        for (ITheme theme : engine.getThemes()) {
            if (themeId.equals(theme.getId())) {
                targetTheme = theme;
                break;
            }
        }

        if (targetTheme == null) {
            System.err.println("[AutoTheme] ThemeSwitchJob: theme not found: " + themeId);
            StringBuilder available = new StringBuilder("Available themes: ");
            for (ITheme t : engine.getThemes()) {
                available.append(t.getId()).append(" ");
            }
            System.err.println("[AutoTheme] " + available);
            return new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                    "Theme not found: " + themeId);
        }

        ITheme currentTheme = engine.getActiveTheme();
        boolean alreadyOnTheme = currentTheme != null && themeId.equals(currentTheme.getId());

        try {
            if (!alreadyOnTheme) {
                // Apply transiently so Eclipse does not persist the theme selection.
                // The IStartup hook re-applies it on every launch while the plugin is
                // installed; removing the plugin leaves no stale theme state behind.
                engine.setTheme(targetTheme, false);
                System.out.println("[AutoTheme] ThemeSwitchJob: switched to " + themeId);
            } else {
                System.out.println("[AutoTheme] ThemeSwitchJob: already on " + themeId + ", repainting.");
            }

            // Always force a full CSS re-application and layout pass on every open
            // shell. setTheme(false) skips the workbench's normal restore path, which
            // leaves some shells unpainted (visible as a pink/salmon background).
            // This also corrects any missed repaints from the early-startup sync.
            Display display2 = Display.getCurrent();
            if (display2 != null && !display2.isDisposed()) {
                for (org.eclipse.swt.widgets.Shell shell : display2.getShells()) {
                    if (shell != null && !shell.isDisposed()) {
                        engine.applyStyles(shell, true);
                        shell.layout(true, true);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AutoTheme] ThemeSwitchJob: failed to set theme: " + e.getMessage());
            e.printStackTrace();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "Failed to set theme: " + themeId, e);
        }

        return Status.OK_STATUS;
    }

    private IThemeEngine getThemeEngine(Display display) {
        try {
            BundleContext context = FrameworkUtil.getBundle(Activator.class).getBundleContext();
            if (context == null) {
                System.err.println("[AutoTheme] BundleContext is null");
                return null;
            }
            ServiceReference<IThemeManager> ref = context.getServiceReference(IThemeManager.class);
            if (ref == null) {
                System.err.println("[AutoTheme] IThemeManager service not found");
                return null;
            }
            IThemeManager manager = context.getService(ref);
            if (manager == null) {
                System.err.println("[AutoTheme] IThemeManager service instance is null");
                context.ungetService(ref);
                return null;
            }
            try {
                return manager.getEngineForDisplay(display);
            } finally {
                context.ungetService(ref);
            }
        } catch (Exception e) {
            System.err.println("[AutoTheme] Exception obtaining theme engine: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convenience method to schedule a theme switch.
     *
     * @param themeId the target theme ID
     */
    public static void switchToTheme(String themeId) {
        new ThemeSwitchJob(themeId).schedule();
    }
}
