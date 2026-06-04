package com.dbeaver.plugin.autotheme.preferences;

import com.dbeaver.plugin.autotheme.Activator;
import com.dbeaver.plugin.autotheme.internal.Messages;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Preference page for configuring auto-theme behavior.
 */
public class AutoThemePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor enabledEditor;
    private Combo lightThemeCombo;
    private Combo darkThemeCombo;
    private IntegerFieldEditor intervalEditor;
    private Composite intervalComposite;

    private List<ITheme> availableThemes = new ArrayList<>();

    public AutoThemePreferencePage() {
        setPreferenceStore(AutoThemePreferences.getPreferenceStore());
        setDescription(Messages.pref_page_description);
    }

    @Override
    public void init(IWorkbench workbench) {
        // no-op
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Enabled checkbox
        Composite enabledComposite = new Composite(composite, SWT.NONE);
        enabledComposite.setLayout(new GridLayout(1, false));
        enabledEditor = new BooleanFieldEditor(
                AutoThemePreferences.PREF_ENABLED,
                Messages.pref_page_enabled_label,
                enabledComposite);
        enabledEditor.setPage(this);
        enabledEditor.load();
        enabledEditor.setPropertyChangeListener(event -> updateEnablement());

        // Theme mappings group
        Group themeGroup = new Group(composite, SWT.NONE);
        themeGroup.setText(Messages.pref_page_theme_group_label);
        themeGroup.setLayout(new GridLayout(2, false));
        themeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loadAvailableThemes();

        new Label(themeGroup, SWT.NONE).setText(Messages.pref_page_light_theme_label);
        lightThemeCombo = new Combo(themeGroup, SWT.READ_ONLY);
        lightThemeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fillThemeCombo(lightThemeCombo);
        lightThemeCombo.setText(findLabelForId(AutoThemePreferences.getLightThemeId()));

        new Label(themeGroup, SWT.NONE).setText(Messages.pref_page_dark_theme_label);
        darkThemeCombo = new Combo(themeGroup, SWT.READ_ONLY);
        darkThemeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fillThemeCombo(darkThemeCombo);
        darkThemeCombo.setText(findLabelForId(AutoThemePreferences.getDarkThemeId()));

        // Polling interval
        intervalComposite = new Composite(composite, SWT.NONE);
        intervalComposite.setLayout(new GridLayout(2, false));
        intervalComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        intervalEditor = new IntegerFieldEditor(
                AutoThemePreferences.PREF_POLL_INTERVAL,
                Messages.pref_page_interval_label,
                intervalComposite);
        intervalEditor.setPage(this);
        intervalEditor.setValidRange(500, 60000);
        intervalEditor.load();

        updateEnablement();
        return composite;
    }

    private void loadAvailableThemes() {
        Display display = Display.getCurrent();
        if (display == null || display.isDisposed()) {
            return;
        }

        BundleContext context = FrameworkUtil.getBundle(Activator.class).getBundleContext();
        if (context == null) {
            return;
        }

        ServiceReference<IThemeManager> ref = context.getServiceReference(IThemeManager.class);
        if (ref == null) {
            return;
        }

        try {
            IThemeManager manager = context.getService(ref);
            if (manager == null) {
                return;
            }
            IThemeEngine engine = manager.getEngineForDisplay(display);
            if (engine != null) {
                for (ITheme theme : engine.getThemes()) {
                    availableThemes.add(theme);
                }
            }
        } finally {
            context.ungetService(ref);
        }
    }

    private void fillThemeCombo(Combo combo) {
        for (ITheme theme : availableThemes) {
            combo.add(theme.getLabel());
        }
        if (combo.getItemCount() == 0) {
            combo.add(AutoThemePreferences.DEFAULT_LIGHT_THEME_ID);
            combo.add(AutoThemePreferences.DEFAULT_DARK_THEME_ID);
        }
    }

    private String findLabelForId(String themeId) {
        for (ITheme theme : availableThemes) {
            if (themeId.equals(theme.getId())) {
                return theme.getLabel();
            }
        }
        return themeId;
    }

    private String findIdForLabel(String label) {
        for (ITheme theme : availableThemes) {
            if (label.equals(theme.getLabel())) {
                return theme.getId();
            }
        }
        return label;
    }

    private void updateEnablement() {
        boolean enabled = enabledEditor.getBooleanValue();
        lightThemeCombo.setEnabled(enabled);
        darkThemeCombo.setEnabled(enabled);
        intervalEditor.setEnabled(enabled, intervalComposite);
    }

    @Override
    public boolean performOk() {
        enabledEditor.store();
        AutoThemePreferences.setLightThemeId(findIdForLabel(lightThemeCombo.getText()));
        AutoThemePreferences.setDarkThemeId(findIdForLabel(darkThemeCombo.getText()));
        intervalEditor.store();
        AutoThemePreferences.save();
        return true;
    }

    @Override
    protected void performDefaults() {
        enabledEditor.loadDefault();
        lightThemeCombo.setText(findLabelForId(AutoThemePreferences.DEFAULT_LIGHT_THEME_ID));
        darkThemeCombo.setText(findLabelForId(AutoThemePreferences.DEFAULT_DARK_THEME_ID));
        intervalEditor.loadDefault();
        updateEnablement();
    }
}
