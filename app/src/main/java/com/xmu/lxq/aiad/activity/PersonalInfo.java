package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.os.Bundle;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.BuildConfig;
import com.xmu.lxq.aiad.R;

/**
 * Created by asus1 on 2017/12/18.
 */

public class PersonalInfo extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
    }
}
