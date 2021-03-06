package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.util.OkHttpUtil;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xmu.lxq.aiad.config.Config.resourcesfiles_url;
import static com.xmu.lxq.aiad.config.Config.userfiles_url;

/**
 * Created by asus1 on 2017/12/27.
 */

public class ProgressActivity extends Activity{

    public static String[] videosName=new String[6];//记录videosName
    static int num=0;//在非主线程遍历videosName[]，必须为静态
    static SweetAlertDialog  dialog = null;
    private int FLAG_DISMISS = 5;//关闭dialog的标志

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_progress);
        getVideosName();
        initialView();
    }

    /**
     * 从服务器返回videosName的json串，并解析
     */
    public void getVideosName(){
        String url= OkHttpUtil.base_url+"getVideosName";
        OkHttpUtil.doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e("getVideosName():失败!");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String tempResponse = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(tempResponse);
                        String returnCode = jsonObject.getString("code");
                        if ("200".equals(returnCode)) {
                            jsonObject = jsonObject.getJSONObject("detail");

                            videosName[0] = jsonObject.getString("0");
                            videosName[1] = jsonObject.getString("1");
                            videosName[2] = jsonObject.getString("2");
                            videosName[3] = jsonObject.getString("3");
                            videosName[4] = jsonObject.getString("4");
                            videosName[5] = jsonObject.getString("5");

                            getTemplateVideo();
                            recordVideosName();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    Logger.e("wrong");
                }
            }
        });
    }

    /**
     * 记录videosName到.txt文件
     */
    public void recordVideosName(){
        File file=new File(userfiles_url+"/","DateRecording.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);//追加方式打开
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,"UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(videosName[0]+"*"+videosName[1]+"*"+videosName[2]+"*"+videosName[3]+"*"+videosName[4]+"*"+videosName[5]+"\r\n");
            bufferedWriter.write("u_1*u_2*u_3"+"\r\n");
            bufferedWriter.close();
            outputStreamWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 从服务器获取视频
     */
    public void getTemplateVideo(){
        try{
            for(int i=0;i<=5;i++){
                String url = OkHttpUtil.base_url + "downloadVideos/"+i;
                OkHttpUtil.downFile(url, resourcesfiles_url+"/", /*videosName[i]*/videosName[i] + ".mp4", new OkHttpUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess() {
                        Logger.i(videosName[num]+"^_^视频下载成功！");
                        Message msg=mHandler.obtainMessage();
                        msg.what=num;mHandler.sendMessage(msg);num++;
                        //if只会在最后执行
                        if(num==6){num=0;}
                    }

                    @Override
                    public void onDownloading(final int progress) {
                       // Log.e(TAG,"正在下载"+progress+"%");//多个文件不知道怎么表示下载进度，单个文件可以直接用
                    }

                    @Override
                    public void onDownloadFailed() {
                        Logger.e("-_-||下载失败！");
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 关闭dialog
     */
    public void dismiss() {
        dialog.dismiss();
    }

    /**
     * 初始化view
     */
    private void initialView(){
        dialog=new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("正在下载模板视频");
        dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.show();
    }


    /**
     * 接收子线程传回的信息
     */
    private Handler mHandler=new Handler (){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);

            dialog.setContentText("进度"+((int)(((double)(msg.what+1)/6)*100))+"%");
            if(msg.what==FLAG_DISMISS){
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                dismiss();
                Intent intent=new Intent(ProgressActivity.this,SudokuActivity.class);
                startActivity(intent);
            }
        }
    };
}
