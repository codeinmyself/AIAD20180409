package com.xmu.lxq.aiad.service;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.LinearInterpolator;

/**
 * Created by xd on 2018/5/2.
 */

public class MusicService extends Service {
    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
    private String[] musicDir = new String[]{
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/MakeorBreak.mp3",
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/ALittleStory.mp3",
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/新的潮流.mp3",
           };
    private int musicIndex = 0;
    public static int isReturnTo = 0;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static ObjectAnimator animator;
    public MusicService() {
        initMediaPlayer();

    }

    public void initMediaPlayer() {
        try {
            //String file_path = "/storage/0123-4567/K.Will-Melt.mp3";
            //String file_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MakeorBreak.mp3";
            //String file_path = "/data/K.Will-Melt.mp3";
            mediaPlayer.setDataSource(musicDir[musicIndex]);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);  // 设置循环播放
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void nextMusic(int i) {
        if(mediaPlayer != null && musicIndex < 3) {
            mediaPlayer.stop();
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicDir[i]);
                //musicIndex++;
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
                /*mediaPlayer.seekTo(0);
                mediaPlayer.start();*/
            } catch (Exception e) {
                Log.d("hint", "can't jump next music");
                e.printStackTrace();
            }
        }
    }

   public  void AnimatorAction() {
        if (mediaPlayer.isPlaying()) {
            animator.setDuration(5000);
            animator.setInterpolator(new LinearInterpolator()); // 均速旋转
            animator.setRepeatCount(ValueAnimator.INFINITE); // 无限循环
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.start();
        }
    }
    private int flag = 0;
    public static String which = "";
    public void playOrPause() {
        flag++;
        if (flag >= 1000) flag = 2;

        which = "pause";

        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            animator.cancel();
        } else {
            mediaPlayer.start();

            if ((flag == 1) || (isReturnTo == 1)) {
                animator.setDuration(5000);
                animator.setInterpolator(new LinearInterpolator()); // 均速旋转
                animator.setRepeatCount(ValueAnimator.INFINITE); // 无限循环
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.start();
            } else {
               // animator.resume();
                animator.start();
            }
        }
    }
    public void stop() {
        which = "stop";
        animator.cancel();
        if(mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }
    /**
     * onBind 是 Service 的虚方法，因此我们不得不实现它。
     * 返回 null，表示客服端不能建立到此服务的连接。
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
