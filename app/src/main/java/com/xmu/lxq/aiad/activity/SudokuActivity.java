package com.xmu.lxq.aiad.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.service.MusicService;
import com.xmu.lxq.aiad.SudokuUtil.ActiveGrideView;
import com.xmu.lxq.aiad.SudokuUtil.DragBaseAdapter;
import com.xmu.lxq.aiad.application.AppContext;
import com.xmu.lxq.aiad.util.CustomDialogUtil;
import com.xmu.lxq.aiad.util.OkHttpUtil;
import com.xmu.lxq.aiad.util.PermissionUtil;
import com.xmu.lxq.aiad.util.ToastUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xmu.lxq.aiad.config.Config.deal_url;
import static com.xmu.lxq.aiad.config.Config.resourcesfiles_url;
import static com.xmu.lxq.aiad.config.Config.userfiles_url;

/**
 * Created by asus1 on 2017/12/18.
 */

public class SudokuActivity extends Activity {

    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SeekBar seekBar;
    private ImageButton start,edit,music;
    private ImageButton save;
    private ImageButton lefttop, leftbottom, righttop, rightbottom, black, white,cd1;
    private EditText word;
    private Button submit;
    private ActiveGrideView aGridview;
    public static List<HashMap<String, String>> list;
    private DragBaseAdapter adapter;
    private int position = 0;
    private LinearLayout editWordsBar;
    private HorizontalScrollView addMusicBar;
    private String path;
    private int temp;
    static CustomDialogUtil dialog=null;
    public static String[] img_text = {"u_1", "宫格1", "宫格2", "宫格3", "u_2", "宫格4",
            "宫格5", "宫格6", "u_3"};

    static String default_img = "/sdcard/1513955901.png";
    public static String[] imgs = {"hh", default_img, default_img, default_img, "hh", default_img, default_img, default_img, "hh"};

    public class Words {
        private String videoID;
        private String content;
        private String pos;
        private String color;
    }

    private Words[] words = new Words[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);

        bindServiceConnection();
        musicService = new MusicService();

        initView();
    }

    /**
     * initView and set Listener
     */
    private void initView() {

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        start = (ImageButton) findViewById(R.id.video_start);
        edit = (ImageButton) findViewById(R.id.video_edit);music = (ImageButton) findViewById(R.id.video_music);
        save = (ImageButton) findViewById(R.id.save_words);
        word = (EditText) findViewById(R.id.words);
        word.setEnabled(false);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        player = new MediaPlayer();
        surfaceView.getHolder().setKeepScreenOn(true);
        aGridview = (ActiveGrideView) findViewById(R.id.gridview);
        submit = (Button) findViewById(R.id.submit);
        editWordsBar = (LinearLayout) findViewById(R.id.edit_words_bar); addMusicBar = (HorizontalScrollView) findViewById(R.id.add_music_bar);
        rightbottom = (ImageButton) findViewById(R.id.rightbottom);
        leftbottom = (ImageButton) findViewById(R.id.leftbottom);
        righttop = (ImageButton) findViewById(R.id.righttop);
        lefttop = (ImageButton) findViewById(R.id.lefttop);
        black = (ImageButton) findViewById(R.id.black);
        white = (ImageButton) findViewById(R.id.white);
        cd1=(ImageButton) findViewById(R.id.cd1);
        musicService.animator = ObjectAnimator.ofFloat(cd1, "rotation", 0, 359);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean down = true;
                Logger.i("整合视频!");
                for (int i = 0; i < img_text.length; i++) {
                    if (imgs[i].equals(default_img) || imgs[i].equals("hh")) {
                        Handler h = new Handler(Looper.getMainLooper());
                        h.post(new Runnable() {
                            public void run() {
                                ToastUtil.getInstance(SudokuActivity.this).showToast("九宫格视频尚未完成！");
                            }
                        });
                        down = false;
                        break;
                    }
                }
                if (down) {
                    submitRecord();
                    submitUserVideos();
                    mThread.start();
                    Intent intent = new Intent(SudokuActivity.this, AddWordsActivity.class);
                    startActivity(intent);
                }

            }
        });
        aGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View item, int arg2,
                                    long position) {

                if (imgs[arg2].equals(default_img) || (doubleClick()&&(img_text[arg2].equals("u_1")||img_text[arg2].equals("u_2")||img_text[arg2].equals("u_3"))) || imgs[arg2].equals("hh")) {
                    if(voicePermission() && PermissionUtil.isHasAudioRecordPermission(SudokuActivity.this)){
                        Logger.i("录音权限已开启");
                        DragBaseAdapter dba = (DragBaseAdapter) parent.getAdapter();
                        Map.Entry entry = dba.loopItem(dba.get(), (int) position);
                        Intent intent = new Intent(SudokuActivity.this, RecordedActivity.class);
                        intent.putExtra("order", arg2 + "");
                        intent.putExtra("fileName", img_text[arg2] + "");
                        startActivityForResult(intent, 1);
                    }else {
                        ToastUtil.getInstance(SudokuActivity.this).showToast("需要开启录音权限！");
                        MediaRecorder recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                        recorder.setAudioChannels(1);
                        recorder.setAudioSamplingRate(8000);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        recorder.setOutputFile("");//这里给个假的地址,因为这段录音是无效的.
                        try {
                            recorder.prepare();
                            recorder.start();//要开始录音时,这里就会弹出提示框了,如果不给权限.我们有异常处理,而且下次想录音时 还是会有此提示.
                            recorder.stop();
                            recorder.release();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    path = getFileNameNoEx(imgs[arg2]) + ".mp4";
                    Bitmap bitmap = getVideoThumbnail(path);
                    surfaceView.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
                    seekBar.setProgress(0);
                    for (int i = 0; i < 9; i++) {
                        if (words[i].videoID.equals(img_text[arg2])) {
                            word.setText(words[i].content);
                            if (words[i].pos.equals("lt"))
                                setMargins(word, dip2px(20), dip2px(20), 0, 0);
                            else if (words[i].pos.equals("lb"))
                                setMargins(word, dip2px(20), dip2px(150), 0, 0);
                            else if (words[i].pos.equals("rt"))
                                setMargins(word, dip2px(200), dip2px(20), 0, 0);
                            else if (words[i].pos.equals("rb"))
                                setMargins(word, dip2px(200), dip2px(150), 0, 0);
                            if (words[i].color.equals("black")) word.setTextColor(Color.BLACK);
                            else if (words[i].color.equals("white")) word.setTextColor(Color.WHITE);
                            temp = i;
                        }
                    }
                }
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.setBackgroundDrawable(null);
                videoPlay(0, path);
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                word.setEnabled(true);
                word.requestFocus();
                editWordsBar.setVisibility(View.VISIBLE);
            }
        });
        music.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addMusicBar.setVisibility(View.VISIBLE);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                words[temp].content = word.getText().toString();
                word.setEnabled(false);
                editWordsBar.setVisibility(View.INVISIBLE);
            }
        });
        lefttop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setMargins(word, dip2px(20), dip2px(20), 0, 0);
                words[temp].pos = "lt";
            }
        });
        leftbottom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setMargins(word, dip2px(20), dip2px(150), 0, 0);
                words[temp].pos = "lb";
            }
        });
        righttop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setMargins(word, dip2px(200), dip2px(20), 0, 0);
                words[temp].pos = "rt";
            }
        });
        rightbottom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setMargins(word, dip2px(200), dip2px(150), 0, 0);
                words[temp].pos = "rb";
            }
        });
        black.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                word.setTextColor(Color.BLACK);
                words[temp].color = "black";
            }
        });
        white.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                word.setTextColor(Color.WHITE);
                words[temp].color = "white";
            }
        });
        cd1.setOnClickListener(new myOnClickListener());

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
            }


            public void surfaceCreated(SurfaceHolder holder) {

                start.setEnabled(true);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (player != null) {
                    position = player.getCurrentPosition();
                    stop();
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null) {
                    player.seekTo(seekBar.getProgress());
                }
            }
        });
        initialAD();
        initialData();
        adapter = new DragBaseAdapter(SudokuActivity.this, list);
        aGridview.setAdapter(adapter);
    }

    /**
            * MUSIC
     */
    private MusicService musicService;

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };

    private void bindServiceConnection() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }

    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {

            cd1.setOnClickListener(new myOnClickListener());
            //stop.setOnClickListener(new myOnClickListener());
            //quit.setOnClickListener(new myOnClickListener());

            handler.postDelayed(runnable, 100);
        }
    };

    @Override
    public void onPause(){
        super.onPause();
        if(isApplicationBroughtToBackground()) {
            musicService.isReturnTo = 1;
            Logger.e("b","后台中");
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        musicService.isReturnTo = 1;
    }

    /*@Override
    protected void onResume() {

        musicService.AnimatorAction();
        verifyStoragePermissions(this);

        if(musicService.mediaPlayer.isPlaying()) {
            stateText.setText("Playing");
        } else {
            if (musicService.which.equals("stop"))  {
                stateText.setText("Stop");
            } else if (musicService.which.equals("pause")){
                stateText.setText("Pause");
            }
        }
        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
        seekBar.setMax(musicService.mediaPlayer.getDuration());
        handler.post(runnable);
        super.onResume();
        Log.d("hint", "handler post runnable");
    }*/

    private class myOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.cd1:
                    //changePlay();
                    musicService.playOrPause();
                    break;
               /* case R.id.stopButton:
                    musicService.stop();
                    changeStop();
                    break;
                case R.id.quitButton:
                    quit();
                    break;*/
                default:
                    break;
            }
        }
    }

    /*private void changePlay() {

        if(musicService.mediaPlayer.isPlaying()){
            stateText.setText("Pause");
            isPlay.setText("PLAY");
            //animator.pause();
        } else {
            stateText.setText("Playing");
            isPlay.setText("PAUSE");

        }
    }

    private void changeStop() {
        stateText.setText("Stop");
        seekBar.setProgress(0);
        //animator.pause();
    }

    private void quit() {
        musicService.animator.end();
        handler.removeCallbacks(runnable);
        unbindService(sc);
        try {
            finish();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public void onStop() {
        super.onStop();
    }
    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }


    private  boolean voicePermission(){
        return (PackageManager.PERMISSION_GRANTED ==   ContextCompat.
                checkSelfPermission(SudokuActivity.this, android.Manifest.permission.RECORD_AUDIO));
    }

    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String videoPath = null;
        private String fileName=null;

        private BitmapWorkerTask(ImageView imageView) {
            // 使用WeakReference来确保ImageView可以被垃圾回收机制回收
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // 在后台解析图片.
        @Override
        protected Bitmap doInBackground(String... params) {
            videoPath = params[0];
            fileName=params[1];
            File file=new File(videoPath);
            Logger.e("test waitforwrite");
            waitForWriteCompleted(file);
            return getVideoThumbnail(videoPath);
        }

        // 一旦解析完成，先查看imageview是否已经被回收，没回收就设置bitmap上去.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                saveBitmap(bitmap, fileName);
                Logger.e("postexecute");
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
                initialData();
                adapter = new DragBaseAdapter(SudokuActivity.this, list);
                adapter.notifyDataSetChanged();
                list = new ArrayList<>(adapter.get());
                reInitial();
                aGridview.setAdapter(adapter);
                dialog.dismiss();
            }
        }
    }

    public void loadBitmap(String videoPath,String fileName, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        String []arr=new String[2];
        arr[0]=videoPath;
        arr[1]=fileName;
        task.execute(arr);
    }

    public int dip2px(float dpValue) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /**
     * 初始化6个模板视频
     */
    public void initialAD() {
        CustomDialogUtil.showDialog(this,"加载中");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                for (int i = 0; i <= 8; i++) {
                    if (i == 1 || i == 2 || i == 3 | i == 5 || i == 6 || i == 7) {
                        Logger.i("initialAD:" + resourcesfiles_url + "/" + ProgressActivity.videosName[count]);
                        Bitmap bitmap = getVideoThumbnail(resourcesfiles_url + "/" + ProgressActivity.videosName[count] + ".mp4");

                        saveBitmap(bitmap, ProgressActivity.videosName[count] + "");
                        imgs[i] = resourcesfiles_url + "/" + ProgressActivity.videosName[count] + ".png";
                        img_text[i] = ProgressActivity.videosName[count];
                        count++;
                    }
                }
                SudokuActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CustomDialogUtil.dismissDialog();
                        initialData();
                        adapter = new DragBaseAdapter(SudokuActivity.this, list);
                        aGridview.setAdapter(adapter);
                        for (int i = 0; i < 9; i++) {
                            words[i] = new Words();
                            words[i].content = "请输入文字";
                            words[i].videoID = img_text[i];
                            words[i].pos = "lt";
                            words[i].color = "black";
                            Logger.i(words[i].videoID);
                        }
                    }
                });
            }
        }).start();
    }


    public void initialData() {
        if (list != null) {
            if (list.size() > 0) list.clear();
        } else {
            list = new ArrayList<>();
        }
        for (int i = 0; i < img_text.length; i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put(img_text[i], imgs[i]);
            list.add(map);
        }
    }


    long[] mHits = new long[2];
    /**
     * 处理双击事件
     *
     * @return
     */
    private boolean doubleClick() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();//获取手机开机时间
        if (mHits[mHits.length - 1] - mHits[0] < 1000) {
            return true;
        }
        return false;
    }

    protected void videoPlay(final int msec, final String path) {
        start.setEnabled(false);//在播放时不允许再点击播放按钮
        if (player == null) {
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
        player.setDisplay(surfaceView.getHolder());//将音频与影像控件关联起来
        player.prepareAsync();
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                player.start();
                player.seekTo(msec);
                // 设置进度条的最大进度为视频流的最大播放时长
                seekBar.setMax(player.getDuration());
                // 开始线程，更新进度条的刻度
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (player != null) {
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
                stop();
            }
        });
    }

    private void stop() {
        if (player != null) {
            seekBar.setProgress(0);
            player.stop();
            player.reset();
            player.release();
            player = null;
            start.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(sc);
        super.onDestroy();
        stop();
    }

    public void submitRecord() {
        File file = new File(userfiles_url + "/", "DateRecording.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(img_text[0] + " ");
            bufferedWriter.write(img_text[1] + " ");
            bufferedWriter.write(img_text[2] + " ");
            bufferedWriter.write(img_text[3] + " ");
            bufferedWriter.write(img_text[4] + " ");
            bufferedWriter.write(img_text[5] + " ");
            bufferedWriter.write(img_text[6] + " ");
            bufferedWriter.write(img_text[7] + " ");
            bufferedWriter.write(img_text[8] + "\r\n");
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (words[j].videoID.equals(img_text[i])) {
                        bufferedWriter.write(words[j].content + " " + words[j].pos + " " + words[j].color + "\r\n");
                        break;
                    }
                }
            }
            bufferedWriter.close();
            outputStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void reInitial() {
        for (int i = 0; i < img_text.length; i++) {
            HashMap<String, String> map;
            map = list.get(i);
            for (String key : map.keySet()) {
                img_text[i] = key;
            }
            for (String value : map.values()) {
                imgs[i] = value;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (data == null) {
                    return;
                }
                int order = Integer.parseInt((data.getStringExtra("order")).trim());
                String fileName = data.getStringExtra("fileName");
                String absolutePath = data.getStringExtra("absolutePath");
                aGridview = (ActiveGrideView) findViewById(R.id.gridview);
                ImageView imageView = (ImageView) aGridview.getChildAt(order).findViewById(R.id.iv_item);
                imgs[order] = userfiles_url + "/" + fileName + ".png";
                loadBitmap(absolutePath,fileName,imageView);
                dialog = new CustomDialogUtil(this,R.style.CustomDialog);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
                break;
        }
    }

    private void waitForWriteCompleted(File file) {
        if (!file.exists())
            return;
        long old_length;
        do {
            old_length = file.length();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (old_length != file.length());
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file1 = new File(filePath);
        if (!file1.exists()) {
            Logger.e(filePath + "不存在");
            return null;
        } else {
            Logger.e(filePath + "存在");
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                File file = new File(filePath);
                mmr.setDataSource(file.getAbsolutePath());
                bitmap = mmr.getFrameAtTime(1000,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (bitmap == null) {
                    Logger.e("bitmap==null");
                }else{
                   Logger.e("bitmap!=null");
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                try {
                    mmr.release();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }


    }

    public void saveBitmap(Bitmap bitmap, String fileName) { // 将在屏幕上绘制的图形保存到SD卡
        File file;
        if ("u_1".equals(fileName) || "u_2".equals(fileName) || "u_3".equals(fileName)) {
            file = new File(userfiles_url + "/" + fileName + ".png");
        } else {
            file = new File(resourcesfiles_url + "/" + fileName + ".png");
        }
        if (!file.exists()) {
            try {
                boolean flag = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file); // 创建文件输出流（写文件）
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);// 将图片对象按PNG格式压缩（质量100%)，写入文件
            fos.flush(); // 刷新
            fos.close();// 关闭流
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    private void submitUserVideos() {
        String url = OkHttpUtil.base_url + "uploadVideo";
        int count = 1;
        while (count <= 3) {
            OkHttpUtil.doFile(url, userfiles_url + "/" + "u_" + count + ".mp4", "u_" + count++ + ".mp4", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    Logger.e("用户视频上传失败！");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    Logger.i("用户视频上传成功！");
                }
            });
        }

    }

    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            entrance();
        }
    });

    public void entrance() {
        String url = OkHttpUtil.base_url + "downloadVideo/" + AppContext.timeStamp;
        File file = new File(deal_url + "/" + AppContext.timeStamp);
        if (!file.exists())
            file.mkdirs();
        Logger.i(deal_url + "/" + AppContext.timeStamp + "/" + "merge1.mp4");
        OkHttpUtil.downFile(url, deal_url + "/" + AppContext.timeStamp + "/", "merge1.mp4", new OkHttpUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                Logger.i("merge1.mp4下载成功！");
            }

            @Override
            public void onDownloading(int progress) {

                Logger.e("merge1.mp4正在下载！");
            }

            @Override
            public void onDownloadFailed() {
                Logger.e("merge1.mp4下载失败！");
            }
        });
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }
}
