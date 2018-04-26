package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
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

import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.application.AppContext;

import java.io.File;

/**
 * Created by asus1 on 2017/12/18.
 */

public class PersonalInfo extends Activity {

    private ImageView avatar;
    private ImageButton edit_nickname,edit_gender;
    private LinearLayout personalInfo, nickname,gender;
    private TextView text_nickname,text_account,text_gender;
    private EditText editText_nickname;
    private Button save_nickname,male,female;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        avatar = (ImageView) findViewById(R.id.avatar);
        edit_nickname = (ImageButton) findViewById(R.id.edit_nickname);edit_gender=(ImageButton) findViewById(R.id.edit_gender);
        personalInfo = (LinearLayout) findViewById(R.id.personalInfo);
        nickname = (LinearLayout) findViewById(R.id.nickname);
        gender = (LinearLayout) findViewById(R.id.gender);
        text_nickname = (TextView) findViewById(R.id.text_nickname);text_account=(TextView) findViewById(R.id.text_account);
        text_gender=(TextView) findViewById(R.id.text_gender);
        editText_nickname = (EditText) findViewById(R.id.editText_nickname);
        save_nickname = (Button) findViewById(R.id.save_nickname);
        male = (Button) findViewById(R.id.male);female = (Button) findViewById(R.id.female);
        if (AppContext.isLogin) {
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
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String telephone = bundle.getString("telephone");
            if (telephone != null) {
                text_account.setText(telephone);
            }
        }

        edit_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfo.setVisibility(View.INVISIBLE);
                nickname.setVisibility(View.VISIBLE);
                if (text_nickname.getText().toString() != "") {
                    editText_nickname.setText(text_nickname.getText().toString());
                }
            }
        });
        save_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_nickname.setText(editText_nickname.getText().toString());
                personalInfo.setVisibility(View.VISIBLE);
                nickname.setVisibility(View.INVISIBLE);
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
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfo.setVisibility(View.VISIBLE);
                gender.setVisibility(View.INVISIBLE);
                text_gender.setText("女");
            }
        });
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalInfo.this, IconActivity.class);
                startActivity(intent);
            }
        });
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


