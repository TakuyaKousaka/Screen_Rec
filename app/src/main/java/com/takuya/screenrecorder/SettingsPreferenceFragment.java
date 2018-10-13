/*
 * Copyright (c) 2016-2017. Vijai Chandra Prasad R.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */

package com.takuya.screenrecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.takuya.screenrecorder.folderpicker.FolderChooser;
import com.takuya.screenrecorder.folderpicker.OnDirectorySelectedListerner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * <p>
 *     This fragment handles various settings of the recorder.
 * </p>
 *
 * @author Vijai Chandra Prasad .R
 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener
 * @see PermissionResultListener
 * @see OnDirectorySelectedListerner
 */
public class SettingsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
        , PermissionResultListener, OnDirectorySelectedListerner {

    /**
     * SharedPreferences object to read the persisted settings
     */
    SharedPreferences prefs;

    /**
     * ListPreference to choose the recording resolution
     */
    private ListPreference res;

    /**
     * CheckBoxPreference to manage audio recording via mic setting
     */
    private CheckBoxPreference recaudio;

    /**
     * FolderChooser object to choose the directory where the video has to be saved to.
     */
    private FolderChooser dirChooser;

    /**
     * MainActivity object
     */
    private MainActivity activity;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    /**
     * Initialize various listeners and settings preferences.
     *
     * @param savedInstanceState default savedInstance bundle sent by Android runtime
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        //init permission listener callback
        setPermissionListener();

        //Get Default save location from shared preference
        String defaultSaveLoc = (new File(Environment
                .getExternalStorageDirectory() + File.separator + Const.APPDIR)).getPath();

        //Get instances of all preferences
        prefs = getPreferenceScreen().getSharedPreferences();
        res = (ListPreference) findPreference(getString(R.string.res_key));
        ListPreference fps = (ListPreference) findPreference(getString(R.string.fps_key));
        ListPreference bitrate = (ListPreference) findPreference(getString(R.string.bitrate_key));
        recaudio = (CheckBoxPreference) findPreference(getString(R.string.audiorec_key));
        ListPreference filenameFormat = (ListPreference) findPreference(getString(R.string.filename_key));
        EditTextPreference filenamePrefix = (EditTextPreference) findPreference(getString(R.string.fileprefix_key));
        dirChooser = (FolderChooser) findPreference(getString(R.string.savelocation_key));
        //Set previously chosen directory as initial directory
        dirChooser.setCurrentDir(getValue(getString(R.string.savelocation_key), defaultSaveLoc));

        ListPreference orientation = (ListPreference) findPreference(getString(R.string.orientation_key));
        orientation.setSummary(orientation.getEntry());

        ListPreference theme = (ListPreference) findPreference(getString(R.string.preference_theme_key));
        theme.setSummary(theme.getEntry());

        //Set the summary of preferences dynamically with user choice or default if no user choice is made
        updateResolution(res);
        updateScreenAspectRatio();
        fps.setSummary(getValue(getString(R.string.fps_key), "30"));
        float bps = bitsToMb(Integer.parseInt(getValue(getString(R.string.bitrate_key), "7130317")));
        bitrate.setSummary(bps + " Mbps");
        dirChooser.setSummary(getValue(getString(R.string.savelocation_key), defaultSaveLoc));
        filenameFormat.setSummary(getFileSaveFormat());
        filenamePrefix.setSummary(getValue(getString(R.string.fileprefix_key), "recording"));

        //If record audio checkbox is checked, check for record audio permission
        if (recaudio.isChecked())
            requestAudioPermission();

        //set callback for directory change
        dirChooser.setOnDirectoryClickedListerner(this);
    }

    /**
     * Updates the summary of resolution settings preference
     *
     * @param pref object of the resolution ListPreference
     */
    private void updateResolution(ListPreference pref) {
        String resolution = getValue(getString(R.string.res_key), getNativeRes());
        if (resolution.toLowerCase().contains("x")) {
            resolution = getNativeRes();
            pref.setValue(resolution);
        }
        pref.setSummary(resolution + "P");
    }

    /**
     * Method to get the device's native resolution
     *
     * @return device resolution
     */
    private String getNativeRes() {
        DisplayMetrics metrics = getRealDisplayMetrics();
        return String.valueOf(getScreenWidth(metrics));
    }

    /**
     * Updates the available resolution based on aspect ratio
     */
    private void updateScreenAspectRatio() {
        CharSequence[] entriesValues = getResolutionEntriesValues();
        res.setEntries(getResolutionEntries(entriesValues));
        //res.setEntries(entriesValues);
        res.setEntryValues(entriesValues);
    }

    /**
     * Get resolutions based on the device's aspect ratio
     *
     * @return entries for the resolution
     */
    private CharSequence[] getResolutionEntriesValues() {

        ArrayList<String> entrieValues = buildEntries(R.array.resolutionValues);

        String[] entriesArray = new String[entrieValues.size()];
        return entrieValues.toArray(entriesArray);
    }

    private CharSequence[] getResolutionEntries(CharSequence[] entriesValues) {
        ArrayList<String> entries = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.resolutionsArray)));
        ArrayList<String> newEntries = new ArrayList<>();
        for (CharSequence values : entriesValues) {
            Log.d(Const.TAG, "res entries:" + values.toString());
            for (String entry : entries) {
                if (entry.contains(values))
                    newEntries.add(entry);
            }
            Log.d(Const.TAG, "res entries: split " + values.toString().split("P")[0] + " val: ");
        }
        Log.d(Const.TAG, "res entries" + newEntries.toString());
        String[] entriesArray = new String[newEntries.size()];
        return newEntries.toArray(entriesArray);
    }

    /**
     * Build resolutions from the arrays.
     *
     * @param resID resource ID for the resolution array
     * @return ArrayList of available resolutions
     */
    private ArrayList<String> buildEntries(int resID) {
        DisplayMetrics metrics = getRealDisplayMetrics();
        int deviceWidth = getScreenWidth(metrics);
        ArrayList<String> entries = new ArrayList<>(Arrays.asList(getResources().getStringArray(resID)));
        Iterator<String> entriesIterator = entries.iterator();
        while (entriesIterator.hasNext()) {
            String width = entriesIterator.next();
            if (deviceWidth < Integer.parseInt(width)) {
                entriesIterator.remove();
            }
        }
        if (!entries.contains("" + deviceWidth))
            entries.add("" + deviceWidth);
        return entries;
    }


    /**
     * Returns object of DisplayMetrics
     *
     * @return DisplayMetrics
     */
    private DisplayMetrics getRealDisplayMetrics(){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager window = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        window.getDefaultDisplay().getRealMetrics(metrics);
        return metrics;
    }


    /**
     * Get width of screen in pixels
     *
     * @return screen width
     */
    private int getScreenWidth(DisplayMetrics metrics) {
        return metrics.widthPixels;
    }

    /**
     * Get height of screen in pixels
     *
     * @return Screen height
     */
    private int getScreenHeight(DisplayMetrics metrics) {
        return metrics.heightPixels;
    }


    /**
     * Get aspect ratio of the screen
     */
    @Deprecated
    private Const.ASPECT_RATIO getAspectRatio() {
        float screen_width = getScreenWidth(getRealDisplayMetrics());
        float screen_height = getScreenHeight(getRealDisplayMetrics());
        float aspectRatio;
        if (screen_width > screen_height) {
            aspectRatio = screen_width / screen_height;
        } else {
            aspectRatio = screen_height / screen_width;
        }
        return Const.ASPECT_RATIO.valueOf(aspectRatio);
    }

    /**
     * Set permission listener in the {@link MainActivity} to handle permission results
     */
    private void setPermissionListener() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            activity = (MainActivity) getActivity();
            activity.setPermissionResultListener(this);
        }
    }

    /**
     * Get the persisted value for the preference from default sharedPreference
     *
     * @param key String represnting the sharedpreference key to fetch
     * @param defVal String Default value if the preference does not exist
     * @return String the persisted preference value or default if not found
     */
    private String getValue(String key, String defVal) {
        return prefs.getString(key, defVal);
    }

    /**
     * Method to convert bits per second to MB/s
     *
     * @param bps float bitsPerSecond
     * @return float
     */
    private float bitsToMb(float bps) {
        return bps / (1024 * 1024);
    }

    //Register for OnSharedPreferenceChangeListener when the fragment resumes
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    //Unregister for OnSharedPreferenceChangeListener when the fragment pauses
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //When user changes preferences, update the summary accordingly
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Preference pref = findPreference(s);
        if (pref == null) return;
        switch (pref.getTitleRes()) {
            case R.string.preference_resolution_title:
                updateResolution((ListPreference) pref);
                break;
            case R.string.preference_fps_title:
                String fps = String.valueOf(getValue(getString(R.string.fps_key), "30"));
                pref.setSummary(fps);
                break;
            case R.string.preference_bit_title:
                float bps = bitsToMb(Integer.parseInt(getValue(getString(R.string.bitrate_key), "7130317")));
                pref.setSummary(bps + " Mbps");
                if (bps > 12)
                    Toast.makeText(getActivity(), R.string.toast_message_bitrate_high_warning, Toast.LENGTH_SHORT).show();
                break;
            case R.string.preference_filename_format_title:
                pref.setSummary(getFileSaveFormat());
                break;
            case R.string.preference_audio_record_title:
                requestAudioPermission();
                break;
            case R.string.preference_filename_prefix_title:
                EditTextPreference etp = (EditTextPreference) pref;
                etp.setSummary(etp.getText());
                ListPreference filename = (ListPreference) findPreference(getString(R.string.filename_key));
                filename.setSummary(getFileSaveFormat());
                break;
            case R.string.preference_theme_title:
                activity.recreate();
                break;
            case R.string.preference_orientation_title:
                pref.setSummary(((ListPreference) pref).getEntry());
                break;
        }
    }

    /**
     * Method to concat file prefix with dateTime format
     */
    public String getFileSaveFormat() {
        String filename = prefs.getString(getString(R.string.filename_key), "yyyyMMdd_hhmmss");
        String prefix = prefs.getString(getString(R.string.fileprefix_key), "recording");
        return prefix + "_" + filename;
    }

    /**
     * Method to request android permission to record audio
     */
    public void requestAudioPermission() {
        if (activity != null) {
            activity.requestPermissionAudio();
        }
    }

    /**
     * Show snackbar with permission Intent when the user rejects write storage permission
     */
    private void showSnackbar() {
        Snackbar.make(getActivity().findViewById(R.id.fab), R.string.snackbar_storage_permission_message,
                Snackbar.LENGTH_INDEFINITE).setAction(R.string.snackbar_storage_permission_action_enable,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activity != null){
                            activity.requestPermissionStorage();
                        }
                    }
                }).show();
    }

    /**
     * Show a dialog when the permission to storage is denied by the user during startup
     */
    private void showPermissionDeniedDialog(){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.alert_permission_denied_title)
                .setMessage(R.string.alert_permission_denied_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (activity != null){
                            activity.requestPermissionStorage();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showSnackbar();
                    }
                })
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setCancelable(false)
                .create().show();
    }

    //Permission result callback to process the result of Marshmallow style permission request
    @Override
    public void onPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Const.EXTDIR_REQUEST_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                    Log.d(Const.TAG, "Storage permission denied. Requesting again");
                    dirChooser.setEnabled(false);
                    showPermissionDeniedDialog();
                } else if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    dirChooser.setEnabled(true);
                }
                return;
            case Const.AUDIO_REQUEST_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(Const.TAG, "Record audio permission granted.");
                    recaudio.setChecked(true);
                } else {
                    Log.d(Const.TAG, "Record audio permission denied");
                    recaudio.setChecked(false);
                }
                return;
            default:
                Log.d(Const.TAG, "Unknown permission request with request code: " + requestCode);
        }
    }

    @Override
    public void onDirectorySelected() {
        Log.d(Const.TAG, "In settings fragment");
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onDirectoryChanged();
        }
    }
}
