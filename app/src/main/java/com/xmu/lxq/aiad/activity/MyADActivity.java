package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.SudokuUtil.DragBaseAdapter;
import com.xmu.lxq.aiad.util.CustomDialogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.xmu.lxq.aiad.config.Config.resourcesfiles_url;
import static com.xmu.lxq.aiad.config.Config.userfiles_url;

public class MyADActivity extends Activity {

    private ImageView img1,img2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ad);
        initView();
    }
    private void initView() {
        img1=(ImageView)findViewById(R.id.ad1);
        img2=(ImageView)findViewById(R.id.ad2);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ad1.mp4";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                String type = "video/*";
                Uri uri = Uri.parse(url);
                intent.setDataAndType(uri,type);
                startActivity(intent);
            }
        });
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ad2.mp4";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                String type = "video/*";
                Uri uri = Uri.parse(url);
                intent.setDataAndType(uri,type);
                startActivity(intent);
            }
        });
        img1.setImageBitmap(getVideoThumbnail(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ad1.mp4"));
        img2.setImageBitmap(getVideoThumbnail(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ad2.mp4"));
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
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                File file = new File(filePath);
                mmr.setDataSource(file.getAbsolutePath());
                bitmap = mmr.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (bitmap == null) {
                    Logger.e("bitmap==null");
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

}