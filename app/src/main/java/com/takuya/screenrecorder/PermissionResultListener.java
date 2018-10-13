package com.takuya.screenrecorder;

//Interface for permission result callback
interface PermissionResultListener {
    void onPermissionResult(int requestCode, String permissions[], int[] grantResults);
}
