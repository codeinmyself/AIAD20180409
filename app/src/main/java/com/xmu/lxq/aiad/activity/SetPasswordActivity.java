package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.model.User;
import com.xmu.lxq.aiad.util.OkHttpUtil;
import com.xmu.lxq.aiad.util.ToastUtil;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lee on 2018/5/10.
 */

public class SetPasswordActivity extends Activity {

    private Button submit_password_btn;
    private EditText reset_password_et;
    private EditText set_password_et;

    String type=null;//判断从哪个activity过来的
    String telephone=null;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_set_password);
        Intent intent=getIntent();
        Logger.i("here");
        telephone=intent.getStringExtra("telephone");
        type=intent.getStringExtra("type");
        initView();
    }
    public void initView(){
        submit_password_btn=(Button)findViewById(R.id.submit_password_btn);
        reset_password_et=(EditText)findViewById(R.id.reset_password_et);
        set_password_et=(EditText)findViewById(R.id.set_password_et);

        submit_password_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd=set_password_et.getText().toString().trim();
                String pwd_reset=reset_password_et.getText().toString().trim();
                if(TextUtils.isEmpty(pwd)||TextUtils.isEmpty(pwd_reset)){
                    ToastUtil.getInstance(SetPasswordActivity.this).showToast("密码或确认密码不能为空!");
                }else if(!pwd.equals(pwd_reset)){
                    ToastUtil.getInstance(SetPasswordActivity.this).showToast("两次输入不匹配！");
                }else if(pwd.equals(pwd_reset)){
                    User mUser = new User(Long.parseLong(telephone), pwd);
                    doRegisterOrUpdatePassword(mUser);
                    ToastUtil.getInstance(SetPasswordActivity.this).showToast("密码设置成功！");
                }else{
                    ToastUtil.getInstance(SetPasswordActivity.this).showToast("未知错误！");
                }
            }
        });
    }

    /**
     * doRegister
     * @param user
     */
    private void doRegisterOrUpdatePassword(User user) {
        String url;
        if(type.equals("register")){
            url = OkHttpUtil.base_url + "register";
        }else if(type.equals("forgetPassword")){
            url=OkHttpUtil.base_url + "updatePassword";
        }else{
            ToastUtil.getInstance(this).showToast("错啦");
            return;
        }
        try {
            // 发送请求
            OkHttpUtil.doPost(url,user, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.e("失败！");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String tempResponse =  response.body().string();
                    if(response.isSuccessful()){
                        Logger.i("成功！");
                        try{
                            Logger.i(tempResponse);
                            JSONObject jsonObject=new JSONObject(tempResponse);
                            String returnCode=jsonObject.getString("code");
                            if("200".equals(returnCode)){
                                Logger.i("注册/更改密码成功!"+returnCode);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(type.equals("register")){
                                            ToastUtil.getInstance(SetPasswordActivity.this).showToast("注册成功!");
                                        }else{
                                            ToastUtil.getInstance(SetPasswordActivity.this).showToast("更改密码成功!");
                                        }
                                    }
                                });
                                Intent intent = new Intent(SetPasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.getInstance(SetPasswordActivity.this).showToast("失败!");
                                    }
                                });
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        Logger.e("失败！");
                    }

                }
            });  //POST方式
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
    }
}
