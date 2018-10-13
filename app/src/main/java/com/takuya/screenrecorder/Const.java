package com.takuya.screenrecorder;

import java.util.HashMap;
import java.util.Map;

public class Const {

    public enum ASPECT_RATIO {
        AR16_9(1.7777778f), AR18_9(2f);

        private static Map<Float, ASPECT_RATIO> map = new HashMap<Float, ASPECT_RATIO>();

        static {
            for (ASPECT_RATIO aspectRatio : ASPECT_RATIO.values()) {
                map.put(aspectRatio.numVal, aspectRatio);
            }
        }

        private float numVal;

        ASPECT_RATIO(float numVal) {
            this.numVal = numVal;
        }

        public static ASPECT_RATIO valueOf(float val) {
            return map.get(val) == null ? AR16_9 : map.get(val);
        }
    }

    public static final int VIDEO_EDIT_REQUEST_CODE = 1004;
    public static final int VIDEO_EDIT_RESULT_CODE = 1005;
    public static final String TAG = "SCREENRECORDER_LOG";
    public static final String APPDIR = "screenrecorder";
    public static final String ALERT_EXTR_STORAGE_CB_KEY = "ext_dir_warn_donot_show_again";
    public static final String VIDEO_EDIT_URI_KEY = "edit_video";
    static final int EXTDIR_REQUEST_CODE = 1000;
    static final int AUDIO_REQUEST_CODE = 1001;
    static final int SYSTEM_WINDOWS_CODE = 1002;
    static final int SCREEN_RECORD_REQUEST_CODE = 1003;
    static final String SCREEN_RECORDING_START = "com.orpheusdroid.screenrecorder.services.action.startrecording";
    static final String SCREEN_RECORDING_PAUSE = "com.orpheusdroid.screenrecorder.services.action.pauserecording";
    static final String SCREEN_RECORDING_RESUME = "com.orpheusdroid.screenrecorder.services.action.resumerecording";
    static final String SCREEN_RECORDING_STOP = "com.orpheusdroid.screenrecorder.services.action.stoprecording";
    static final String SCREEN_RECORDER_VIDEOS_LIST_FRAGMENT_INTENT = "com.orpheusdroid.screenrecorder.SHOWVIDEOSLIST";
    static final int SCREEN_RECORDER_NOTIFICATION_ID = 5001;
    static final int SCREEN_RECORDER_SHARE_NOTIFICATION_ID = 5002;
    static final String RECORDER_INTENT_DATA = "recorder_intent_data";
    static final String RECORDER_INTENT_RESULT = "recorder_intent_result";
    static final String RECORDING_NOTIFICATION_CHANNEL_ID = "recording_notification_channel_id1";
    static final String SHARE_NOTIFICATION_CHANNEL_ID = "share_notification_channel_id1";
    static final String RECORDING_NOTIFICATION_CHANNEL_NAME = "Persistent notification shown when recording screen or when waiting for shake gesture";
    static final String SHARE_NOTIFICATION_CHANNEL_NAME = "Notification shown to share or edit the recorded video";
    static final String PREFS_LIGHT_THEME = "light_theme";
    static final String PREFS_DARK_THEME = "dark_theme";
    static final String PREFS_BLACK_THEME = "black_theme";

    public enum RecordingState {
        RECORDING, PAUSED, STOPPED
    }
}
