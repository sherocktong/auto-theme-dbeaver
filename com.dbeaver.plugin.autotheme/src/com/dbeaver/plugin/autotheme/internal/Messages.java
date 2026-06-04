package com.dbeaver.plugin.autotheme.internal;

/**
 * Localized messages for the auto-theme plugin.
 * (For a production plugin these would load from a properties file.)
 */
public final class Messages {

    private Messages() {
        // utility class
    }

    public static final String pref_page_description =
            "Automatically switch DBeaver theme based on the OS system appearance.";
    public static final String pref_page_enabled_label =
            "Enable auto theme switching";
    public static final String pref_page_theme_group_label =
            "Theme Mappings";
    public static final String pref_page_light_theme_label =
            "Light theme:";
    public static final String pref_page_dark_theme_label =
            "Dark theme:";
    public static final String pref_page_interval_label =
            "Check interval (ms):";
}
