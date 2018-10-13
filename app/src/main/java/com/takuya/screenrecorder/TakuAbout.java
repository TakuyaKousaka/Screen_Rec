package com.takuya.screenrecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TakuAbout extends AppCompatActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new TakuAboutCont()).commit();
    }

}