package com.takuya.screenrecorder;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;

public class TakuAboutCont extends PreferenceFragment {

    String Twitter = "https://twitter.com/Takuya_Kou";
    String Find = "findme";
    String ubw = "ubw";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);
        // Custom Button In Prefrence
        Preference findme = (Preference) findPreference(Find);
        findme.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            // Launch The Website
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse(Twitter);
                Intent twitintent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(twitintent);
                return true;
            }
        });

        Preference ubwdiag = (Preference) findPreference(ubw);
        ubwdiag.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder takudialog = new AlertDialog.Builder(getActivity());
                takudialog.setTitle(R.string.ubwtitle);
                takudialog.setMessage(TakuAboutCont.this.getString(R.string.ubwmain));
                //Empty code for quitting the dialog
                takudialog.setNegativeButton(R.string.closethis, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                takudialog.show();
                return true;
            }
        });
    }
}