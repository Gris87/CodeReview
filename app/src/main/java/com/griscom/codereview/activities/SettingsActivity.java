package com.griscom.codereview.activities;

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
 * Activity with settings
 */
public class SettingsActivity extends PreferenceActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = "SettingsActivity";



    /** {@inheritDoc} */
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
        if (isSimplePreferences())
        {
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
    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane()
    {
        return isXLargeTablet();
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private boolean isXLargeTablet()
    {
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if the device doesn't have newer APIs like {@link PreferenceFragment},
     * or the device doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private boolean isSimplePreferences()
    {
        return !isXLargeTablet();
    }

    /** {@inheritDoc} */
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        if (!isSimplePreferences())
        {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName)
    {
        return fragmentName.equals(PreferenceFragment.class.getName())
               ||
               fragmentName.equals(FileManagerPreferenceFragment.class.getName())
               ||
               fragmentName.equals(ColorsPreferenceFragment.class.getName())
               ||
               fragmentName.equals(EditorPreferenceFragment.class.getName());
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
