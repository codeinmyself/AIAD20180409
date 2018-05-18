package com.xmu.lxq.aiad.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.xmu.lxq.aiad.application.AppContext;

import java.util.Date;

/**
 * Created by HoHo on 2018/5/18.
 */

public class SharePreferenceUtil {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public SharePreferenceUtil(Context context, String file) {
        sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public Long getTelephone() {
        return sp.getLong("telephone",0L);
    }

    public void setTelephone(Long telephone) {
        editor.putLong("telephone", telephone);
        editor.commit();
    }

    public String getPassword() {
        return sp.getString("password", "");
    }

    public void setPassword(String passwd) {
        editor.putString("password", passwd);
        editor.commit();
    }

    public String getEmail() {
        return sp.getString("email", "");
    }

    public void setEmail(String email) {
        editor.putString("email", email);
        editor.commit();
    }

    public String getNickname() {
        return sp.getString("nickname", "");
    }

    public void setNickname(String nickname) {
        editor.putString("nickname", nickname);
        editor.commit();
    }

    public String getAvatar() {
        return sp.getString("avatar", "");
    }

    public void setAvatar(String avatar) {
        editor.putString("avatar", avatar);
        editor.commit();
    }
    public String getGender() {
        return sp.getString("gender", "");
    }

    public void setGender(String gender) {
        editor.putString("gender", gender);
        editor.commit();
    }
}