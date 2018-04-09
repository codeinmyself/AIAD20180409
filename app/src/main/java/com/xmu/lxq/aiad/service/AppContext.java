package com.xmu.lxq.aiad.service;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by asus1 on 2017/12/21.
 */

public class AppContext extends Application{
    public  static boolean isLogin=false;

   /* private  AppContext instance;

    public  AppContext getInstance() {
        return instance;
    }*/
   /* @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }*/
    public boolean isLogin(){
        return isLogin;
    }
    public void setIsLogin(boolean flag){
        isLogin = flag;
    }

    private static Context mContext;
    public static int screenWidth;
    public static int screenHeight;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
    }

    public static Context getContext() {
        return mContext;
    }
    public static String timeStamp;
}
