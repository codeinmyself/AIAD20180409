package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.BuildConfig;

import java.io.File;

import static com.xmu.lxq.aiad.config.Config.userfiles_url;


public class VideoActivity extends Activity {

    private static final int VIDEO_REQUEST_CODE = 1;

    private int order=1;

    private String fileName;
    private File file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        Intent intent=getIntent();
        order=(int)Long.parseLong((intent.getStringExtra("order")).trim());
        fileName=intent.getStringExtra("fileName").trim();
        openVideo();
    }


    /**
     * openVideo
     */
    public void openVideo() {
        Intent intent = new Intent();
        intent.setAction("android.media.action.VIDEO_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");

        file=new File(userfiles_url+"/"+fileName+".mp4");
        Uri value = Uri.fromFile(file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, value);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,2);
        startActivityForResult(intent,VIDEO_REQUEST_CODE);
    }


    /**
     * getTime
     * @return
     */
    public String getTime(){
        long time=System.currentTimeMillis()/1000;//获取系统时间的10位的时间戳
        return String.valueOf(time);
    }


    /**
     * onActivityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
          if (requestCode == VIDEO_REQUEST_CODE) {
              Glide.get(this).clearMemory();
               /* //打开视频
                Uri uri = data.getData();
                VideoView videoView = new VideoView(this);
                videoView.setMediaController(new MediaController(this));
                videoView.setVideoURI(uri);
                videoView.start();
                videoView.requestFocus();*/
                Intent intent=new Intent();
                intent.putExtra("fileName",fileName+"");
                intent.putExtra("parentPath",file.getParent()+"");
                intent.putExtra("absolutePath",file.getAbsolutePath()+"");
                intent.putExtra("order",order+"");
                this.setResult(1,intent);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}