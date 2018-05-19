package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.application.AppContext;
import com.xmu.lxq.aiad.model.User;
import com.xmu.lxq.aiad.util.CustomDialogUtil;
import com.xmu.lxq.aiad.util.OkHttpUtil;
import com.xmu.lxq.aiad.util.SharePreferenceUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by asus1 on 2017/12/18.
 */

public class PersonalInfo extends Activity {

    private ImageView avatar;
    private ImageButton edit_nickname,edit_gender,edit_mail;
    private LinearLayout personalInfo, nickname,gender,mail;
    private TextView text_nickname,text_gender,text_mail,text_telephone;
    private EditText editText_nickname,editText_mail;
    private Button save_nickname,male,female,save_mail;

    private SharePreferenceUtil sharePreferenceUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        avatar = (ImageView) findViewById(R.id.avatar);
        text_telephone=(TextView)findViewById(R.id.text_telephone);
        edit_nickname = (ImageButton) findViewById(R.id.edit_nickname);edit_gender=(ImageButton) findViewById(R.id.edit_gender);edit_mail=(ImageButton) findViewById(R.id.edit_mail);
        personalInfo = (LinearLayout) findViewById(R.id.personalInfo);
        nickname = (LinearLayout) findViewById(R.id.nickname);mail=(LinearLayout) findViewById(R.id.mail);
        gender = (LinearLayout) findViewById(R.id.gender);
        text_nickname = (TextView) findViewById(R.id.text_nickname);text_mail=(TextView) findViewById(R.id.editText_mail);text_gender=(TextView) findViewById(R.id.text_gender);
        editText_nickname = (EditText) findViewById(R.id.editText_nickname);editText_mail = (EditText) findViewById(R.id.editText_mail);
        save_nickname = (Button) findViewById(R.id.save_nickname); save_mail = (Button) findViewById(R.id.save_mail);
        male = (Button) findViewById(R.id.male);female = (Button) findViewById(R.id.female);

        if (AppContext.isLogin) {
            String path = "/sdcard/AIAD/personal/" + "icon.jpg";
            try {
                File file = new File(path);
                if (file.exists()) {
                    avatar.setImageBitmap(getDiskBitmap(path));

                } else {
                    Resources res = PersonalInfo.this.getResources();
                    Bitmap icon = BitmapFactory.decodeResource(res, R.drawable.album_color);
                    avatar.setImageBitmap(icon);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        sharePreferenceUtil=new SharePreferenceUtil(PersonalInfo.this,"personalInfo");
        text_telephone.setText(sharePreferenceUtil.getSharedPreference("telephone","").toString().trim());
        text_mail.setText(sharePreferenceUtil.getSharedPreference("email","").toString().trim());
        text_nickname.setText(sharePreferenceUtil.getSharedPreference("nickname","").toString().trim());
        text_gender.setText(sharePreferenceUtil.getSharedPreference("gender","").toString().trim());

        edit_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfo.setVisibility(View.INVISIBLE);
                nickname.setVisibility(View.VISIBLE);
                if (!text_nickname.getText().toString().equals("")) {
                    editText_nickname.setText(text_nickname.getText().toString());
                    editText_nickname.setSelection(text_nickname.getText().toString().length());//将光标移至文字末尾
                }
            }
        });
        edit_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfo.setVisibility(View.INVISIBLE);
                mail.setVisibility(View.VISIBLE);
                if (!text_mail.getText().toString().equals("")) {
                    editText_mail.setText(text_mail.getText().toString());
                    editText_mail.setSelection(text_mail.getText().toString().length());//将光标移至文字末尾

                }
            }
        });
        save_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname1=editText_nickname.getText().toString();
                text_nickname.setText(nickname1);
                sharePreferenceUtil.put("nickname",nickname1);
                personalInfo.setVisibility(View.VISIBLE);
                nickname.setVisibility(View.INVISIBLE);
            }
        });
        save_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail1=editText_mail.getText().toString();
                text_mail.setText(mail1);
                sharePreferenceUtil.put("email",mail1);
                personalInfo.setVisibility(View.VISIBLE);
                mail.setVisibility(View.INVISIBLE);
            }
        });
        edit_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfo.setVisibility(View.INVISIBLE);
                gender.setVisibility(View.VISIBLE);
            }
        });
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfo.setVisibility(View.VISIBLE);
                gender.setVisibility(View.INVISIBLE);
                text_gender.setText("男");
                sharePreferenceUtil.put("gender","男");
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfo.setVisibility(View.VISIBLE);
                gender.setVisibility(View.INVISIBLE);
                text_gender.setText("女");
                sharePreferenceUtil.put("gender","女");
            }
        });
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalInfo.this, IconActivity.class);
                startActivity(intent);
            }
        });
        //getPersonalInfo();
    }

    public void getPersonalInfo(){
        CustomDialogUtil.showDialog(this,"加载中");
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        long telephone = sp.getLong("telephone", 0L);
        String url= OkHttpUtil.base_url+"getUserByTelephone";
        HashMap<String, String> map = new HashMap<>();
        map.put("telephone", telephone+"");
        OkHttpUtil.doPost(url, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e("wrong");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String tempResponse = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        Logger.i(tempResponse);
                        Gson gson=new Gson();
                        final User user=gson.fromJson(tempResponse,User.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(user.getNickname()!=null){
                                    text_nickname.setText(user.getNickname());
                                }
                                if(user.getEmail()!=null){
                                    text_mail.setText(user.getEmail());
                                }
                                //...未写完
                            }
                        });
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
     * onResume
     */
    @Override
    public void onRestart() {
        super.onRestart();
        if (AppContext.isLogin) {
            avatar.setVisibility(View.VISIBLE);
            String path = "/sdcard/AIAD/personal/" + "icon.jpg";
            try {
                File file = new File(path);
                if (file.exists()) {
                    avatar.setImageBitmap(getDiskBitmap(path));
                    // Glide.with(this).load(path).bitmapTransform(new CropCircleTransformation(this)).into(icon_image);
                } else {
                    Resources res = PersonalInfo.this.getResources();
                    Bitmap icon = BitmapFactory.decodeResource(res, R.drawable.album_color);
                    avatar.setImageBitmap(icon);
                    //Glide.with(this).load(R.drawable.ic_launcher_background).bitmapTransform(new CropCircleTransformation(this)).into(icon_image);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            text_telephone.setText(sharePreferenceUtil.getSharedPreference("telephone","").toString().trim());
            text_mail.setText(sharePreferenceUtil.getSharedPreference("email","").toString().trim());
            text_nickname.setText(sharePreferenceUtil.getSharedPreference("nickname","").toString().trim());
            text_gender.setText(sharePreferenceUtil.getSharedPreference("gender","").toString().trim());
        }

    }

    public Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return bitmap;
    }
}


