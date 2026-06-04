# DBeaver Auto Theme Plugin

Automatically switches DBeaver theme based on the OS system appearance (light / dark mode).

## Problem

DBeaver has a "Follow system" theme option, but it only applies at startup. If you change your OS theme while DBeaver is running, you have to restart the application to see the change.

## Solution

This plugin polls the OS system theme and dynamically switches the Eclipse CSS theme via `IThemeEngine` whenever a change is detected — no restart required.

## Features

- **Real-time switching**: Detects OS light/dark changes while DBeaver is running
- **Customizable mappings**: Choose which DBeaver theme to use for light and dark modes
- **Configurable polling interval**: Adjust how frequently the OS theme is checked
- **Easy toggle**: Enable or disable auto-switching from preferences

## Installation

### From Update Site

1. Build the plugin (see [Building](#building) below)
2. In DBeaver, go to **Help → Install New Software → Add…**
3. Click **Local…** and select `com.dbeaver.plugin.autotheme.site/target/repository/`
4. Select **DBeaver Auto Theme Feature** and finish the installation
5. Restart DBeaver

### Manual

Copy the generated plugin JAR into DBeaver's `dropins` folder and restart.

## Usage

Once installed, the plugin starts monitoring automatically.

To configure:

1. Open **Window → Preferences → Auto Theme**
2. Toggle **Enable auto theme switching**
3. Select your preferred **Light theme** and **Dark theme**
4. Adjust the **Check interval** (default: 3000 ms)

## Default Theme Mappings

| OS Mode | DBeaver Theme ID |
|---------|-----------------|
| Light   | `org.eclipse.e4.ui.css.theme.e4_default` |
| Dark    | `org.eclipse.e4.ui.css.theme.e4_dark` |

## Building

Requires **Maven 3.6+** and **Java 17+**.

```bash
mvn clean verify
```

The p2 update site will be generated at:

```
com.dbeaver.plugin.autotheme.site/target/repository/
```

## Project Structure

```
.
├── pom.xml                                    # Parent POM (Tycho)
├── com.dbeaver.plugin.autotheme              # Plugin bundle
│   ├── META-INF/MANIFEST.MF
│   ├── plugin.xml
│   └── src/com/dbeaver/plugin/autotheme
│       ├── Activator.java                    # Bundle lifecycle
│       ├── ThemeMonitor.java                 # OS theme polling
│       ├── ThemeSwitchJob.java               # Dynamic theme switch
│       └── preferences
│           ├── AutoThemePreferences.java
│           └── AutoThemePreferencePage.java
├── com.dbeaver.plugin.autotheme.feature      # Eclipse feature
└── com.dbeaver.plugin.autotheme.site         # p2 update site
```

## How It Works

1. **Startup hook**: `ThemeMonitor` implements `IStartup` and begins when the workbench opens
2. **Polling loop**: `Display.timerExec()` checks `Display.isSystemDarkTheme()` every N milliseconds
3. **Theme switch**: When a change is detected, `ThemeSwitchJob` runs on the UI thread and calls `IThemeEngine.setTheme()`
4. **No native code**: Pure SWT / Eclipse APIs — works cross-platform without JNA

## Compatibility

| Platform | Detection Method | Notes |
|----------|-----------------|-------|
| macOS    | `Display.isSystemDarkTheme()` | Reliable |
| Windows  | `Display.isSystemDarkTheme()` | Detects system setting |
| Linux    | `Display.isSystemDarkTheme()` | Depends on GTK theme |

## License

EPL-2.0
