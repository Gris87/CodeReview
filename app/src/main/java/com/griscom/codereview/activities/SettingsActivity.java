package com.griscom.codereview.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationPreferences;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
{
    private static final String TAG = "SettingsActivity";

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    /**
     * Force to use multi pan
     */
    private static final boolean ALWAYS_MULTIPAN = false;



    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen()
    {
        if (!isSimplePreferences(this))
        {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Change preference file name
        PreferenceManager prefManager = getPreferenceManager();
        prefManager.setSharedPreferencesName(ApplicationPreferences.FILE_NAME);
        prefManager.setSharedPreferencesMode(MODE_PRIVATE);

        // Create empty PreferenceScreen
        setPreferenceScreen(prefManager.createPreferenceScreen(this));

        // Add 'file manager' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_file_manager);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_file_manager);

        // Add 'colors' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_colors);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_colors);

        // Add 'editor' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_editor);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_editor);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane()
    {
        return (ALWAYS_MULTIPAN || isLargeTablet(this)) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context)
    {
        return ALWAYS_SIMPLE_PREFS
               ||
               (
                !ALWAYS_MULTIPAN
                &&
                !isLargeTablet(context)
               );
    }

    /** {@inheritDoc} */
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        if (!isSimplePreferences(this))
        {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * This fragment shows file manager preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class FileManagerPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            // Change preference file name
            PreferenceManager prefManager = getPreferenceManager();
            prefManager.setSharedPreferencesName(ApplicationPreferences.FILE_NAME);
            prefManager.setSharedPreferencesMode(MODE_PRIVATE);

            addPreferencesFromResource(R.xml.pref_file_manager);
        }
    }

    /**
     * This fragment shows colors preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class ColorsPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            // Change preference file name
            PreferenceManager prefManager = getPreferenceManager();
            prefManager.setSharedPreferencesName(ApplicationPreferences.FILE_NAME);
            prefManager.setSharedPreferencesMode(MODE_PRIVATE);

            addPreferencesFromResource(R.xml.pref_colors);
        }
    }

    /**
     * This fragment shows editor preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class EditorPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            // Change preference file name
            PreferenceManager prefManager = getPreferenceManager();
            prefManager.setSharedPreferencesName(ApplicationPreferences.FILE_NAME);
            prefManager.setSharedPreferencesMode(MODE_PRIVATE);

            addPreferencesFromResource(R.xml.pref_editor);
        }
    }
}
