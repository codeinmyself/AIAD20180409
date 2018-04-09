package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.BuildConfig;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.model.User;
import com.xmu.lxq.aiad.util.OkHttpUtil;
import com.xmu.lxq.aiad.util.ToastUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by asus1 on 2017/12/16.
 */

public class RegisterActivity extends Activity{

    String verificationCode;
    private EditText telephoneText;
    private EditText passwordText;

    private EditText verificationText;

    private Button verificationButton;
    private Button submitButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_register);
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        initView();
    }

    /**
     * initial view
     */
    public void initView(){
        telephoneText=(EditText)findViewById(R.id.phone_number_et);
        passwordText=(EditText)findViewById(R.id.pwd_et);

        verificationText=findViewById(R.id.verification_code_et);
        verificationButton=findViewById(R.id.verification_code_btn);
        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isUserNameAndPwdValid()){
                    String telephone = telephoneText.getText().toString().trim();
                    String url=OkHttpUtil.base_url+"/getVerificationCode";
                    Map<String,String> m1 = new HashMap();
                    m1.put("telephone",telephone);
                    OkHttpUtil.doPost(url, m1, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Logger.e("error!");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {


                            String tempResponse =  response.body().string();
                            if(response.isSuccessful()){
                                Logger.i("success!");                            }
                            try {
                                JSONObject jsonObject = new JSONObject(tempResponse);
                                String returnCode = jsonObject.getString("code");
                                if ("200".equals(returnCode)) {
                                    verificationCode=jsonObject.getJSONObject("detail").toString();
                                    Logger.i(verificationCode);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        submitButton=(Button)findViewById(R.id.register_btn_sure);
        cancelButton=(Button)findViewById(R.id.register_btn_cancel);

        submitButton.setOnClickListener(m_register_Listener);
        cancelButton.setOnClickListener(m_register_Listener);
    }
    View.OnClickListener m_register_Listener = new View.OnClickListener() {    //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.register_btn_sure:                       //确认按钮的监听事件
                    registerCheck();
                    break;
                case R.id.register_btn_cancel:                     //取消按钮的监听事件,由注册界面返回登录界面
                    Intent intent_Register_to_Login = new Intent(RegisterActivity.this,LoginActivity.class) ;    //切换User Activity至Login Activity
                    startActivity(intent_Register_to_Login);
                    finish();
                    break;
            }
        }
    };

    public final static String PHONE_PATTERN = "[1][34578]\\d{9}";

    public static boolean isMatchered(String patternStr, CharSequence input) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
    /**
     * registerCheck
     */
    public void registerCheck() {                                //确认按钮的监听事件
        if (isUserNameAndPwdValid()||verificationCheck()) {
            String telephone = telephoneText.getText().toString().trim();
            String userPwd = passwordText.getText().toString().trim();
            User mUser = new User(Long.parseLong(telephone), userPwd);
            doRegister(mUser);
        }
    }
    public boolean verificationCheck(){
        String code_et=verificationText.getText().toString().trim();
        if(!verificationCode.equals(code_et)){
            ToastUtil.getInstance(this).showToast("验证码错误！");
            return false;
        }
        return true;
    }


    /**
     * doRegister
     * @param user
     */
    private void doRegister(User user)
    {
        String url = OkHttpUtil.base_url + "register"; //POST方式
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
                                Logger.i("注册成功!"+returnCode);
                                Handler h = new Handler(Looper.getMainLooper());
                                h.post(new Runnable() {
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "注册成功!",Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }else{

                                Handler h = new Handler(Looper.getMainLooper());
                                h.post(new Runnable() {
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "注册失败!",Toast.LENGTH_SHORT).show();
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
     * isUserNameAndPwdValid
     * @return
     */
    public boolean isUserNameAndPwdValid() {
        if (telephoneText.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.account_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (passwordText.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.pwd_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(!isMatchered(PHONE_PATTERN,telephoneText.getText().toString().trim())){
            Toast.makeText(this, "号码格式不正确！",
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
    }
}
