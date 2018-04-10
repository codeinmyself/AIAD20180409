package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.BuildConfig;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.SudokuUtil.ActiveGrideView;
import com.xmu.lxq.aiad.SudokuUtil.DragBaseAdapter;
import com.xmu.lxq.aiad.service.AppContext;
import com.xmu.lxq.aiad.util.OkHttpUtil;
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
    private ImageButton start;
    private Button submit;
    private ActiveGrideView aGridview;
    public static List<HashMap<String, String>> list;
    private DragBaseAdapter adapter;
    private int position = 0;

    private String path;
    public static String[] img_text = {"u_1", "宫格1", "宫格2", "宫格3", "u_2", "宫格4",
            "宫格5", "宫格6", "u_3"};

    static String default_img = "/sdcard/1513955901.png";
    public static String[] imgs = {"hh", default_img, default_img, default_img, "hh", default_img, default_img, default_img, "hh"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   makeActionOverflowMenuShown();
        setContentView(R.layout.activity_sudoku);
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        initView();
    }

    /**
     * initView and set Listener
     */
    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        start = (ImageButton) findViewById(R.id.video_start);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        player = new MediaPlayer();
        surfaceView.getHolder().setKeepScreenOn(true);
        aGridview = (ActiveGrideView) findViewById(R.id.gridview);
        submit=(Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean down=true;
                Logger.i( "整合视频!");
                for (int i = 0; i < img_text.length; i++) {
                    if (imgs[i].equals(default_img) || imgs[i].equals("hh")) {
                        Handler h = new Handler(Looper.getMainLooper());
                        h.post(new Runnable() {
                            public void run() {
                                ToastUtil.getInstance(SudokuActivity.this).showToast("九宫格视频尚未完成！");
                            }
                        });
                        down=false;
                        break;
                    }
                }
                if (down){
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

                if (imgs[arg2].equals(default_img) || doubleClick() || imgs[arg2].equals("hh")) {
                    DragBaseAdapter dba = (DragBaseAdapter) parent.getAdapter();
                    Map.Entry entry = dba.loopItem(dba.get(), (int) position);
                    Intent intent = new Intent(SudokuActivity.this, RecordedActivity.class);
                    intent.putExtra("order", arg2 + "");
                    intent.putExtra("fileName", img_text[arg2] + "");
                    startActivityForResult(intent, 1);
                } else {
                    path = getFileNameNoEx(imgs[arg2]) + ".mp4";
                    Bitmap bitmap=getVideoThumbnail(path);
                    //surfaceView.setBackgroundDrawable(null);
                    surfaceView.setBackgroundDrawable(new BitmapDrawable(getResources(),bitmap));
                    seekBar.setProgress(0);
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

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                start.setEnabled(true);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (player != null) {
                    position =player.getCurrentPosition();
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
                if(player!=null){
                    player.seekTo(seekBar.getProgress());
                }
            }
        });
        initialAD();
        initialData();
        adapter = new DragBaseAdapter(SudokuActivity.this, list);
        aGridview.setAdapter(adapter);
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data=null;

        public BitmapWorkerTask(ImageView imageView) {
            // 使用WeakReference来确保ImageView可以被垃圾回收机制回收
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // 在后台解析图片.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            return getVideoThumbnail(data);
        }

        // 一旦解析完成，先查看imageview是否已经被回收，没回收就设置bitmap上去.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                //saveBitmap(bitmap, fileName);
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

    }
    public void loadBitmap(String imagePath, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(imagePath);
    }

    /**
     * 初始化6个模板视频
     */
    public void initialAD() {
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
                        initialData();
                        adapter = new DragBaseAdapter(SudokuActivity.this, list);
                        aGridview.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }


    public void initialData() {
        if (list != null) {
            if (list.size() > 0) list.clear();
        } else {
            //list = new ArrayList<HashMap<String,Integer>>();
            list = new ArrayList<HashMap<String, String>>();
        }
        for (int i = 0; i < img_text.length; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(img_text[i], imgs[i]);
            list.add(map);
        }
    }


    private long[] mHits = new long[2];

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
                            while (player!=null){
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

    private void stop(){
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
            bufferedWriter.close();
            outputStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public  static void reInitial() {
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
                loadBitmap(absolutePath,imageView);
              /*  ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if(bitmap!=null){
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Glide.with(this).load(stream.toByteArray())
                            .asBitmap().into(imageView);
                }else{
                    Logger.e("bitmap为null!");
                }*/

                imgs[order]="/sdcard/"+fileName+".png";
                //Glide.with(this).load(bitmap).into(imageView);
                imgs[order] = userfiles_url + "/" + fileName + ".png";


                initialData();
                adapter = new DragBaseAdapter(this, list);
                adapter.notifyDataSetChanged();
                list = new ArrayList<>(adapter.get());
                reInitial();
                aGridview.setAdapter(adapter);
                break;
        }
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;

        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file1 = new File(filePath);
        if (!file1.exists()) {
            Logger.e(filePath+"不存在");
            return null;
        }else{
            Logger.e(filePath+"存在");
        }
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        //FFmpegMediaMetadataRetriever mmr = new  FFmpegMediaMetadataRetriever();
        try {
              mmr.setDataSource(filePath);

           /* retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(10);
            上面这样写会出错
            */
            /*File file=new File(filePath);
            retriever.setDataSource(file.getAbsolutePath());
            bitmap = retriever.getFrameAtTime();
            链接：http://blog.csdn.net/yaochangliang159/article/details/56841879
            */
            /*
            https://stackoverflow.com/questions/11459784/mediametadataretactivity-getframeattime-videoframe-is-a-null-pointer
             */
            //retriever.setDataSource(filePath);
            //new HashMap<String, String>()

           /* Uri uri=Uri.fromFile(new File(filePath));
            mmr.setDataSource(SudokuActivity.this,uri);
            bitmap = mmr.getFrameAtTime(10000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);*/
            bitmap=mmr.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
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


    public void saveBitmap(Bitmap bitmap, String fileName) { // 将在屏幕上绘制的图形保存到SD卡
        File file;
        if("u_1".equals(fileName)||"u_2".equals(fileName)||"u_3".equals(fileName)){
            file = new File(userfiles_url + "/" + fileName + ".png");
        }else{
            file = new File(resourcesfiles_url + "/" + fileName + ".png");
        }
        if (!file.exists()) {
            try {
                boolean flag=file.createNewFile();
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

                    Logger.e( "用户视频上传失败！");
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
        String url = OkHttpUtil.base_url + "downloadVideo/"+ AppContext.timeStamp;
        File file=new File(deal_url+"/"+AppContext.timeStamp);
        if(!file.exists())
            file.mkdirs();
        Logger.i(deal_url + "/"+ AppContext.timeStamp+"/"+"merge1.mp4");
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
