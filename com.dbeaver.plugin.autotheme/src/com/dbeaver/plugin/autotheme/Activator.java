package com.dbeaver.plugin.autotheme;

import com.dbeaver.plugin.autotheme.preferences.AutoThemePreferences;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends Plugin {

    public static final String PLUGIN_ID = "com.dbeaver.plugin.autotheme";

    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        AutoThemePreferences.initializeDefaults();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        ThemeMonitor monitor = ThemeMonitor.getInstance();
        if (monitor != null) {
            monitor.dispose();
        }
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }
}
