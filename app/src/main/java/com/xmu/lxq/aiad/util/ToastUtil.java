package com.xmu.lxq.aiad.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by HoHo on 2018/3/20.
 */

public class ToastUtil {
    private Toast mToast;
    private static ToastUtil mToastUtils;
    private ToastUtil(Context context) {
        mToast = Toast.makeText(context.getApplicationContext(), null, Toast.LENGTH_SHORT);
    }

    public static synchronized ToastUtil getInstance(Context context) {
        if (null == mToastUtils) {
            mToastUtils = new ToastUtil(context);
        }
        return mToastUtils;
    }

    /**
     * 显示toast
     *
     * @param toastMsg
     */
    public void showToast(int toastMsg) {
        mToast.setText(toastMsg);
        mToast.show();
    }
    /**
     * 显示toast
     *
     * @param toastMsg
     */
    public void showToast(String toastMsg) {
        mToast.setText(toastMsg);
        mToast.show();
    }
    /**
     * 取消toast，在activity的destory方法中调用
     */
    public void destroy() {
        if (null != mToast) {
            mToast.cancel();
            mToast = null;
        }
        mToastUtils = null;
    }
}
