package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.application.AppContext;
import com.xmu.lxq.aiad.util.OkHttpUtil;
import com.xmu.lxq.aiad.util.ToastUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xmu.lxq.aiad.config.Config.userfiles_url;

public class TimeStyleActivity extends Activity {

    RadioGroup radioGroup1;
    RadioGroup radioGroup2;
    RadioButton radioButton1;
    RadioButton radioButton2;
    RadioButton radioButton3;
    RadioButton radioButton4;
    RadioButton radioButton5;
    RadioButton radioButton6;
    Button next;
    String time, style;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_style);
        setTitle("风格时长选择");
        initView();
    }

    /**
     * initialView
     */
    private void initView(){
        radioGroup1=(RadioGroup)findViewById(R.id.radioGroup1);
        radioGroup2=(RadioGroup)findViewById(R.id.radioGroup2);

        radioButton1=(RadioButton)radioGroup1.getChildAt(0);
        radioButton2=(RadioButton)radioGroup1.getChildAt(1);
        radioButton3=(RadioButton)radioGroup1.getChildAt(2);

        radioButton4=(RadioButton)radioGroup2.getChildAt(0);
        radioButton5=(RadioButton)radioGroup2.getChildAt(1);
        radioButton6=(RadioButton)radioGroup2.getChildAt(2);

        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if(radioButton1.getId() == checkedId){
                    style=radioButton1.getText().toString();
                    ToastUtil.getInstance(TimeStyleActivity.this).showToast(style);
                }
                else if(radioButton2.getId() == checkedId){
                    style=radioButton2.getText().toString();
                    ToastUtil.getInstance(TimeStyleActivity.this).showToast(style);
                }
                else if(radioButton3.getId() == checkedId){
                    style=radioButton3.getText().toString();
                    ToastUtil.getInstance(TimeStyleActivity.this).showToast(style);
                }
            }
        });
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if(radioButton4.getId() == checkedId){
                    time=radioButton4.getText().toString();
                    ToastUtil.getInstance(TimeStyleActivity.this).showToast(time);
                }
                else if(radioButton5.getId() == checkedId){
                    time=radioButton5.getText().toString();
                    ToastUtil.getInstance(TimeStyleActivity.this).showToast(time);
                }
                else if(radioButton6.getId() == checkedId){
                    time=radioButton6.getText().toString();
                    ToastUtil.getInstance(TimeStyleActivity.this).showToast(time);
                }
            }
        });
        next=(Button)findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(radioGroup1.getCheckedRadioButtonId()==-1 ){
                    ToastUtil.getInstance(TimeStyleActivity.this).showToast("未选择风格！");
                }else if(radioGroup2.getCheckedRadioButtonId()==-1){
                    ToastUtil.getInstance(TimeStyleActivity.this).showToast("未选择时长！");
                }else{
                    writeStyleTimeToFile();
                }
            }
        });
    }

    private void writeStyleTimeToFile(){
        File file=new File(userfiles_url+"/","DateRecording.txt");
        try {
            boolean fileFlag=file.createNewFile(); // 创建文件
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try{
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter=new BufferedWriter(outputStreamWriter);
            AppContext.timeStamp=System.currentTimeMillis()+"";

            bufferedWriter.write(AppContext.timeStamp+"\r\n");
            bufferedWriter.write("light"+"\r\n");
            bufferedWriter.write(time+"\r\n");
            bufferedWriter.write(style+"\r\n");

            bufferedWriter.close();
            outputStreamWriter.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }


        String url=OkHttpUtil.base_url+"/getStyle";
        Map<String,String> m1 = new HashMap();
        m1.put("style",style);
        OkHttpUtil.doPost(url, m1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e("error!");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Logger.i("success!");
            }
        });
        Intent intent=new Intent(TimeStyleActivity.this,ProgressActivity.class);
        startActivity(intent);
    }
}
