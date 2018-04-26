package com.xmu.lxq.aiad.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.application.AppContext;
import com.xmu.lxq.aiad.camera.SensorControler;
import com.xmu.lxq.aiad.gpufilter.SlideGpuFilterGroup;
import com.xmu.lxq.aiad.gpufilter.helper.MagicFilterType;
import com.xmu.lxq.aiad.util.ToastUtil;
import com.xmu.lxq.aiad.widget.CameraView;
import com.xmu.lxq.aiad.widget.CircularProgressView;
import com.xmu.lxq.aiad.widget.FocusImageView;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.xmu.lxq.aiad.config.Config.userfiles_url;

/**
 * desc 视频录制
 * 主要包括 音视频录制、断点续录、对焦等功能
 */

public class RecordedActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener, SensorControler.CameraFocusListener, SlideGpuFilterGroup.OnFilterChangeListener {

    private CameraView mCameraView;
    private CircularProgressView mCapture;
    private FocusImageView mFocus;
    private ImageView mBeautyBtn;
    private ImageView mFilterBtn;
    private ImageView mCameraChange;
    private static final int maxTime = 2000;//最长录制2s
    private boolean pausing = false;
    private boolean recordFlag = false;//是否正在录制
    private long timeStep = 50;//进度条刷新的时间
    long timeCount = 0;//用于记录录制时间
    private boolean autoPausing = false;
    ExecutorService executorService;
    private SensorControler mSensorControler;
    private String fileName;
    private File file;
    private int order=1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorde);
        Intent intent=getIntent();
        order=(int)Long.parseLong((intent.getStringExtra("order")).trim());
        fileName=intent.getStringExtra("fileName").trim();

        file=new File(userfiles_url+"/"+fileName+".mp4");
        executorService = Executors.newSingleThreadExecutor();
        mSensorControler = SensorControler.getInstance();
        mSensorControler.setCameraFocusListener(this);
        initView();
    }

    private void initView() {
        mCameraView = (CameraView) findViewById(R.id.camera_view);
        mCapture = (CircularProgressView) findViewById(R.id.mCapture);
        mFocus = (FocusImageView) findViewById(R.id.focusImageView);
        mBeautyBtn = (ImageView) findViewById(R.id.btn_camera_beauty);
        mFilterBtn = (ImageView) findViewById(R.id.btn_camera_filter);
        mCameraChange = (ImageView) findViewById(R.id.btn_camera_switch);
        mBeautyBtn.setOnClickListener(this);
        mCameraView.setOnTouchListener(this);
        mCameraView.setOnFilterChangeListener(this);
        mCameraChange.setOnClickListener(this);
        mCapture.setTotal(maxTime);
        mCapture.setOnClickListener(this);
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mCameraView.onTouch(event);
        if (mCameraView.getCameraId() == 1) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                float sRawX = event.getRawX();
                float sRawY = event.getRawY();
                float rawY = sRawY * AppContext.screenWidth / AppContext.screenHeight;
                float temp = sRawX;
                float rawX = rawY;
                rawY = (AppContext.screenWidth - temp) * AppContext.screenHeight / AppContext.screenWidth;

                Point point = new Point((int) rawX, (int) rawY);
                mCameraView.onFocus(point, callback);
                mFocus.startFocus(new Point((int) sRawX, (int) sRawY));
        }
        return true;
    }
    Camera.AutoFocusCallback callback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                mFocus.onFocusSuccess();
            } else {
                //聚焦失败显示的图片
                mFocus.onFocusFailed();

            }
        }
    };
    @Override
    public void onFocus() {
        if (mCameraView.getCameraId() == 1) {
            return;
        }
        Point point = new Point(AppContext.screenWidth / 2, AppContext.screenHeight / 2);
        mCameraView.onFocus(point, callback);
    }
    @Override
    public void onBackPressed() {
        if (recordFlag) {
            recordFlag = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
        ToastUtil.getInstance(this).showToast(R.string.change_filter);
        if (recordFlag && autoPausing) {
            mCameraView.resume(true);
            autoPausing = false;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (recordFlag && !pausing) {
            mCameraView.pause(true);
            autoPausing = true;
        }
        mCameraView.onPause();
    }
    @Override
    public void onFilterChange(final MagicFilterType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == MagicFilterType.正常){
                    ToastUtil.getInstance(RecordedActivity.this).showToast("当前没有设置滤镜--"+type);
                }else {
                    ToastUtil.getInstance(RecordedActivity.this).showToast("当前滤镜切换为--"+type);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_camera_switch:
                mCameraView.switchCamera();
                if (mCameraView.getCameraId() == 1){
                    //前置摄像头 使用美颜
                    mCameraView.changeBeautyLevel(3);
                }else {
                    //后置摄像头不使用美颜
                    mCameraView.changeBeautyLevel(0);
                }
                break;
            case R.id.mCapture:
                if (!recordFlag) {
                    executorService.execute(recordRunnable);
                } else if (!pausing) {
                    mCameraView.pause(false);
                    pausing = true;
                } else {
                    mCameraView.resume(false);
                    pausing = false;
                }
                break;
            case R.id.btn_camera_beauty:
                new AlertDialog.Builder(RecordedActivity.this)
                        .setSingleChoiceItems(new String[]{"关闭", "1", "2", "3", "4", "5"}, mCameraView.getBeautyLevel(),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        mCameraView.changeBeautyLevel(which);
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton("取消", null)
                        .show();
                break;
        }
    }
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            recordFlag = true;
            pausing = false;
            autoPausing = false;
            timeCount = 0;

            file=new File(userfiles_url+"/"+fileName+".mp4");
            //如果存在文件先删除（有方法覆盖吗？）
            if(file.exists()) file.delete();
            String savePath = userfiles_url+"/"+fileName+".mp4";

            try {
                mCameraView.setSavePath(savePath);
                mCameraView.startRecord();
                while (timeCount <= maxTime && recordFlag) {
                    if (pausing || autoPausing) {
                        continue;
                    }
                    mCapture.setProcess((int) timeCount);
                    Thread.sleep(timeStep);
                    timeCount += timeStep;
                }
                recordFlag = false;
                mCameraView.stopRecord();
                if (timeCount < 2000) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.getInstance(RecordedActivity.this).showToast( "录像时间太短");
                        }
                    });
                } else {
                    recordComplete(savePath);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    /**
     * getTime
     * @return
     */
    public String getTime(){
        long time=System.currentTimeMillis()/1000;//获取系统时间的10位的时间戳
        return String.valueOf(time);
    }



    private void recordComplete(final String path) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCapture.setProcess(0);
                ToastUtil.getInstance(RecordedActivity.this).showToast( "文件保存路径：" + path);

                Glide.get(RecordedActivity.this).clearMemory();

                Uri uri = Uri.fromFile(file);
                VideoView videoView = new VideoView(RecordedActivity.this);
                videoView.setMediaController(new MediaController(RecordedActivity.this));
                videoView.setVideoURI(uri);
                videoView.start();
                videoView.requestFocus();

                Intent intent=new Intent();
                intent.putExtra("fileName",fileName+"");
                intent.putExtra("parentPath",file.getParent()+"");
                intent.putExtra("absolutePath",file.getAbsolutePath()+"");
                intent.putExtra("order",order+"");

                mCameraView.onDestroy();
                RecordedActivity.this.setResult(1,intent);
                finish();
            }
        });
    }

    @Override
    public void onDestroy(){
        mCameraView.onDestroy();
        super.onDestroy();
    }

}
