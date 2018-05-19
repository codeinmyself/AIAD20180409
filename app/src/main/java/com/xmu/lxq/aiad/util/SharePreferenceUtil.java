package com.xmu.lxq.aiad.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by HoHo on 2018/5/18.
 */

/**
 * 保存信息配置类
 *
 * @author admin
 */
public class SharePreferenceUtil {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharePreferenceUtil(Context context,String FILE_NAME) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * 存储
     */
    public void put(String key, Object object) {
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.commit();
    }

    /**
     * 获取保存的数据
     */
    public Object getSharedPreference(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return sharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return sharedPreferences.getString(key, null);
        }
    }

    /**
     * 移除某个key值已经对应的值
     */
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

    /**
     * 查询某个key是否存在
     */
    public Boolean contain(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * 返回所有的键值对
     */
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    /*public Long getTelephone() {
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
    }*/
}