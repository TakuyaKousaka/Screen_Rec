package com.takuya.screenrecorder.encoder;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.takuya.screenrecorder.Const;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Mp4toGIFConverter {
    private Uri videoUri;
    private Context context;
    private long maxDur = 5000;
    private MediaMetadataRetriever mediaMetadataRetriever;

    public Mp4toGIFConverter(Context context) {
        this();
        this.context = context;
    }

    private Mp4toGIFConverter() {
        mediaMetadataRetriever = new MediaMetadataRetriever();
    }

    public void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
    }

    public void convertToGif(){
        //MediaMetadataRetriever tRetriever = new MediaMetadataRetriever();

        try{
            mediaMetadataRetriever.setDataSource(context, videoUri);

            //extract duration in millisecond
            String DURATION = mediaMetadataRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION);
            maxDur = (long)(Double.parseDouble(DURATION));

            Log.d(Const.TAG, "max dur is" + maxDur);

            TaskSaveGIF myTaskSaveGIF = new TaskSaveGIF();
            myTaskSaveGIF.execute();
        }catch(RuntimeException e){
            e.printStackTrace();
            Toast.makeText(context,
                    "Something Wrong!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public class TaskSaveGIF extends AsyncTask<Void, Integer, String> {
        ProgressDialog dialog = new ProgressDialog(context);

        private String getGifFIleName(){
            String Filename = videoUri.getLastPathSegment();
            return Filename.replace("mp4", "gif");
        }



        @Override
        protected String doInBackground(Void... params) {
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File outFile = new File(extStorageDirectory + File.separator + Const.APPDIR, getGifFIleName());
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
                bos.write(genGIF());
                bos.flush();
                bos.close();


                return(outFile.getAbsolutePath() + " Saved");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle("Please wait. Saving GIF");
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMax(100);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context,
                    result,
                    Toast.LENGTH_LONG).show();
            dialog.cancel();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //bar.setProgress(values[0]);
            //updateFrame();
            dialog.setProgress(values[0]);

            Log.d(Const.TAG, "Gif save progress: " + values[0]);
        }

        private byte[] genGIF(){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            GifEncoder animatedGifEncoder = new GifEncoder();
            animatedGifEncoder.setDelay(1000);
            animatedGifEncoder.setRepeat(0);
            animatedGifEncoder.setQuality(15);
            //animatedGifEncoder.setSize(0,0);
            animatedGifEncoder.setFrameRate(20.0f);

            Bitmap bmFrame;
            animatedGifEncoder.start(bos);
            for(int i=0; i<100; i+=10){
                long frameTime = maxDur * i/100;
                Log.d(Const.TAG, "GIF GETTING FRAME AT: " + frameTime + "ms");
                bmFrame = mediaMetadataRetriever.getFrameAtTime(frameTime);
                animatedGifEncoder.addFrame(bmFrame);
                publishProgress(i);
            }

            //last from at end
            bmFrame = mediaMetadataRetriever.getFrameAtTime(maxDur);
            animatedGifEncoder.addFrame(bmFrame);
            publishProgress(100);

            animatedGifEncoder.finish();
            return bos.toByteArray();
        }
    }
}
