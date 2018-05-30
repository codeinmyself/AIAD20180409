package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;

import java.io.File;

import static com.xmu.lxq.aiad.config.Config.deal_url;

/**
 * Created by asus1 on 2017/12/26.
 */

public class ResultActivity extends Activity {

    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SeekBar seekBar;
    private ImageButton start;
    private boolean isPlaying;

    static ProgressDialog dialog = null;
    private int FLAG_DISMISS = 1;
    private boolean flag = true;//跳出循环的标志
    //static String path = deal_url+"/"+ AppContext.timeStamp+"/merge1.mp4";
    static String path = deal_url+ "/merge1.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_result);
        initView();
        Logger.i(path);
    }

    /**
     * initView
     */
    private void initView() {

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        start = (ImageButton) findViewById(R.id.video_start1);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        player = new MediaPlayer();
        surfaceView.getHolder().setKeepScreenOn(true);

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置水平进度条
        dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.setTitle("正在合成");
        dialog.setMax(100);
        dialog.setMessage("请等待");
        dialog.show();

        mThread.start();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.setBackgroundDrawable(null);
                videoPlay(0, path);
            }
        });
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {

            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
                if (player != null && player.isPlaying()) {
                                /*currentPosition = player.getCurrentPosition();*/
                    player.stop();
                    player.release();
                    player = null;
                }
            }
        });
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;

        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file1 = new File(filePath);
        if (!file1.exists()) {
            return null;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
    public void dismiss() {
        dialog.dismiss();

        Bitmap bitmap=getVideoThumbnail(path);

        surfaceView.setBackgroundDrawable(new BitmapDrawable(getResources(),bitmap));
        flag = false;
    }
    private Thread mThread=new Thread(new Runnable() {
        @Override
        public void run() {
            while(flag){
                try {
                    Thread.sleep(1000);
                    if(isExists()){
                        Message msg=mHandler.obtainMessage();
                        msg.what=FLAG_DISMISS;
                        mHandler.sendMessage(msg);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    });
    public boolean isExists(){
        File file=new File(path);
        return file.exists();
    }

    private Handler mHandler=new Handler (){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what==FLAG_DISMISS){
                dismiss();
            }
        }
    };

    /**
     * 视频播放
     * @param msec
     * @param path
     */
    protected void videoPlay(final int msec, final String path) {
        if (player == null) {
            player = new MediaPlayer();
        }
        if (player.isPlaying()) {
            player.stop();
            player.release();
            player = null;
            player = new MediaPlayer();
        }
        //设置音频流类型
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // 设置播放的视频源
        try {
            player.setDataSource(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 设置显示视频的SurfaceHolder
        player.setDisplay(surfaceView.getHolder());//这一步是关键，制定用于显示视频的SurfaceView对象（通过setDisplay（））
        player.prepareAsync();
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                player.start();
                // 按照初始位置播放
                player.seekTo(msec);
                // 设置进度条的最大进度为视频流的最大播放时长
                seekBar.setMax(player.getDuration());
                // 开始线程，更新进度条的刻度
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            isPlaying = true;
                            while (isPlaying) {
                                int current = player.getCurrentPosition();
                                seekBar.setProgress(current);

                                sleep(20);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                start.setEnabled(false);
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // 在播放完毕被回调
                start.setEnabled(true);
                ResultActivity.this.player.release();
                ResultActivity.this.player = null;
            }
        });
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }
}
