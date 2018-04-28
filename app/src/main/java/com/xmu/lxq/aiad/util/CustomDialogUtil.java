package com.xmu.lxq.aiad.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.xmu.lxq.aiad.R;

/**
 * Created by lee on 2018/4/28.
 */

public class CustomDialogUtil extends ProgressDialog{
    private static String message="加载中";//默认提示信息
    private  static CustomDialogUtil customDialogUtil;
    private CustomDialogUtil(Context context)
    {
        super(context);
    }

    public CustomDialogUtil(Context context, int theme)
    {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_dialog);
        TextView textView=(TextView)findViewById(R.id.tv_load_dialog);
        textView.setText(message);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
    }

    public static void showDialog(Context context){
        customDialogUtil=new CustomDialogUtil(context);
        customDialogUtil.setCancelable(false);
        customDialogUtil.setCanceledOnTouchOutside(false);
        customDialogUtil.show();
    }
    public static void showDialog(Context context ,String msg){
        customDialogUtil=new CustomDialogUtil(context);
        customDialogUtil.setCancelable(false);
        customDialogUtil.setCanceledOnTouchOutside(false);
        message=msg;
        //Dialog的show()方法会调用其onCreate()方法,因此在oncreate方法中赋值textView
        customDialogUtil.show();
    }
    public  static void dismissDialog(){
        if(customDialogUtil!=null && customDialogUtil.isShowing()){
            customDialogUtil.dismiss();
            customDialogUtil=null;
        }
    }
}
