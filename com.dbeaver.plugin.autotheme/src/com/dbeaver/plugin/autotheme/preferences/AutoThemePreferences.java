package com.dbeaver.plugin.autotheme.preferences;

import com.dbeaver.plugin.autotheme.Activator;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Preference constants and accessors for the auto-theme plugin.
 */
public final class AutoThemePreferences {

    private static final IPreferenceStore STORE =
            new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);

    private static volatile boolean defaultsInitialized = false;

    public static final String PREF_ENABLED = "autotheme.enabled";
    public static final String PREF_LIGHT_THEME_ID = "autotheme.lightThemeId";
    public static final String PREF_DARK_THEME_ID = "autotheme.darkThemeId";
    public static final String PREF_POLL_INTERVAL = "autotheme.pollInterval";

    public static final String DEFAULT_LIGHT_THEME_ID = "org.eclipse.e4.ui.css.theme.e4_default";
    public static final String DEFAULT_DARK_THEME_ID = "org.eclipse.e4.ui.css.theme.e4_dark";
    public static final int DEFAULT_POLL_INTERVAL = 3000;

    private AutoThemePreferences() {
        // utility class
    }

    /**
     * Ensure defaults are set before any accessor is used.
     * This is safe to call repeatedly.
     */
    public static synchronized void ensureDefaults() {
        if (defaultsInitialized) {
            return;
        }
        initializeDefaults();
        defaultsInitialized = true;
    }

    public static IPreferenceStore getPreferenceStore() {
        return STORE;
    }

    public static void initializeDefaults() {
        STORE.setDefault(PREF_ENABLED, true);
        STORE.setDefault(PREF_LIGHT_THEME_ID, DEFAULT_LIGHT_THEME_ID);
        STORE.setDefault(PREF_DARK_THEME_ID, DEFAULT_DARK_THEME_ID);
        STORE.setDefault(PREF_POLL_INTERVAL, DEFAULT_POLL_INTERVAL);
    }

    public static boolean isEnabled() {
        ensureDefaults();
        return STORE.getBoolean(PREF_ENABLED);
    }

    public static void setEnabled(boolean enabled) {
        STORE.setValue(PREF_ENABLED, enabled);
    }

    public static String getLightThemeId() {
        ensureDefaults();
        return STORE.getString(PREF_LIGHT_THEME_ID);
    }

    public static void setLightThemeId(String themeId) {
        STORE.setValue(PREF_LIGHT_THEME_ID, themeId);
    }

    public static String getDarkThemeId() {
        ensureDefaults();
        return STORE.getString(PREF_DARK_THEME_ID);
    }

    public static void setDarkThemeId(String themeId) {
        STORE.setValue(PREF_DARK_THEME_ID, themeId);
    }

    public static int getPollInterval() {
        ensureDefaults();
        return STORE.getInt(PREF_POLL_INTERVAL);
    }

    public static void setPollInterval(int interval) {
        STORE.setValue(PREF_POLL_INTERVAL, interval);
    }

    public static void save() {
        if (STORE.needsSaving()) {
            try {
                ((ScopedPreferenceStore) STORE).save();
            } catch (java.io.IOException e) {
                System.err.println("[AutoTheme] Failed to save preferences: " + e.getMessage());
            }
        }
    }
}
