package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
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
 * Created by lee on 2018/5/10.
 */

public class ForgetPasswordActivity extends Activity{
    String verificationCode;
    private EditText telephoneText;
    private EditText verificationText;
    private Button verificationButton;
    private Button submitButton;
    private ImageButton cancelButton;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_forget_password);
        initView();
    }

    public void initView(){
        telephoneText=(EditText)findViewById(R.id.phone_number_et);
        verificationText=findViewById(R.id.verification_code_et);
        verificationButton=findViewById(R.id.verification_code_btn);
        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isTelephoneValid()){

                    verificationButton.setEnabled(false);
                    timer.start();

                    String telephone = telephoneText.getText().toString().trim();
                    String url= OkHttpUtil.base_url+"/getVerificationCode";
                    Map<String,String> m1 = new HashMap();
                    m1.put("telephone",telephone);
                    OkHttpUtil.doPost(url, m1, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Logger.e("error!");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            String temmResponse=response.body().string();
                            if(response.isSuccessful()){
                                Logger.i("success!");                            }
                            try {
                                Logger.i(temmResponse);
                                JSONObject jsonObject = new JSONObject(temmResponse);
                                String returnCode = jsonObject.getString("code");
                                if ("200".equals(returnCode)) {
                                    verificationCode=jsonObject.getString("detail");
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
        cancelButton=(ImageButton)findViewById(R.id.cancel_forgot);

        submitButton.setOnClickListener(m_register_Listener);
        cancelButton.setOnClickListener(m_register_Listener);
    }

    public final static String PHONE_PATTERN = "[1][34578]\\d{9}";
    public static boolean isMatchered(String patternStr, CharSequence input) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    View.OnClickListener m_register_Listener = new View.OnClickListener() {    //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.register_btn_sure:                       //确认按钮的监听事件
                    registerCheck();
                    break;
                case R.id.cancel_forgot:                     //取消按钮的监听事件,由注册界面返回登录界面
                    Intent intent_Register_to_Login = new Intent(ForgetPasswordActivity.this,LoginActivity.class) ;    //切换User Activity至Login Activity
                    startActivity(intent_Register_to_Login);
                    finish();
                    break;
            }
        }
    };
    /**
     * registerCheck
     */
    public void registerCheck() {                                //确认按钮的监听事件
        if (isTelephoneValid()&&verificationCheck()) {
            Intent intent=new Intent(this,SetPasswordActivity.class);
            intent.putExtra("telephone",telephoneText.getText().toString().trim());
            intent.putExtra("type","forgetPassword");
            startActivity(intent);
        }
    }
    public boolean verificationCheck(){
        String code_et=verificationText.getText().toString().trim();
        Logger.i(code_et);
        Logger.i(verificationCode);
        if(!verificationCode.equals(code_et)){
            ToastUtil.getInstance(this).showToast("验证码错误！");
            return false;
        }
        if(TextUtils.isEmpty(code_et)){
            ToastUtil.getInstance(this).showToast("验证码不能为空！");
            return false;
        }else{
            Logger.e("wrong");
            ToastUtil.getInstance(this).showToast("wrong");
            return false;
        }
    }
    /**
     * isUserNameAndPwdValid
     * @return
     */
    public boolean isTelephoneValid() {
        if (telephoneText.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.account_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(!isMatchered(PHONE_PATTERN,telephoneText.getText().toString().trim())){
            Toast.makeText(this, "号码格式不正确！",
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    final CountDownTimer timer = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            verificationButton.setText(millisUntilFinished/1000 + "秒后重置");
        }

        @Override
        public void onFinish() {
            verificationButton.setEnabled(true);
            verificationButton.setText("获取验证码");
        }
    };
    /**
     * onBackPressed
     */
    @Override
    public void onBackPressed() {
    }
}
