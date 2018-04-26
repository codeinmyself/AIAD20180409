package com.xmu.lxq.aiad.application;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.BuildConfig;

import java.io.File;

import static com.xmu.lxq.aiad.config.Config.directorysUrl;

/**
 * Created by asus1 on 2017/12/21.
 */

public class AppContext extends Application{
    public  static boolean isLogin=false;
    private static Context mContext;
    public static int screenWidth;
    public static int screenHeight;
    public static String timeStamp;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //获取设备屏幕信息
        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
        //logger初始化
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        //文件初始化
        initialDirectory();
    }

    public boolean isLogin(){
        return isLogin;
    }
    public void setIsLogin(boolean flag){
        isLogin = flag;
    }
    public static Context getContext() {
        return mContext;
    }
    public void initialDirectory() {
        for (int i = 0; i < directorysUrl.length; i++) {
            File file = new File(directorysUrl[i]);
            if (!file.exists()) {
                boolean bool = file.mkdirs();
                Logger.e("初始化需要的文件夹" + directorysUrl[i] + ",成功（true）失败（false）：" + bool);
            }
        }
    }
}
