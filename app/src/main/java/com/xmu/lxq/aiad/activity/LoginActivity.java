package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.application.AppContext;
import com.xmu.lxq.aiad.util.OkHttpUtil;
import com.xmu.lxq.aiad.util.ToastUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by lxq on 2017/12/7.
 */

public class LoginActivity extends Activity{

    private EditText  et_telephone=null;
    private EditText  et_password=null;
    private Button login;
    private Button register;
    private Button tryout;
    //3次登录机会
    private int LOGIN_CHANCES = 3;
    //还剩几次登录机会的标志，初始值就是LOGIN_CHANCES
    private int counter = LOGIN_CHANCES;
    //多次认证失败时需要等待的时间
    private long WAIT_TIME = 30000L;
    long errorTime;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);
        initView();
    }


    /**
     * initView
     */
    public void initView(){
        et_telephone = (EditText)findViewById(R.id.editText1);
        et_password = (EditText)findViewById(R.id.editText2);
        login = (Button)findViewById(R.id.button1);
        register=(Button)findViewById(R.id.button2);
        tryout=(Button)findViewById(R.id.button);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                //输入错误时的时间,如果为空的话就取0L
                errorTime = sp.getLong("errorTime", 0L);
                long recentTime = System.currentTimeMillis();
                //如果当前时间与出错时间相差超过30s
                if(recentTime - errorTime > WAIT_TIME) {
                    if(matchLoginMsg(et_telephone.getText().toString().trim(),et_password.getText().toString().trim())) {
                        doLogin(et_telephone.getText().toString().trim(), et_password.getText().toString().trim());
                    }
                } else{
                    long remainTime=errorTime+WAIT_TIME-recentTime;
                    ToastUtil.getInstance(LoginActivity.this).showToast("登录界面锁定中，请等待！剩余"+remainTime/1000+"s");
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRegister();
            }
        });
        tryout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * toRegister
     */
    public void toRegister(){
        Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * matchLoginMsg
     * @param telephone
     * @param password
     * @return
     */
    public boolean matchLoginMsg(String telephone,String password){
        if("".equals(telephone)) {
            ToastUtil.getInstance(LoginActivity.this).showToast("账号不能为空");
            return false;
        }
        if("".equals(password)) {
            ToastUtil.getInstance(LoginActivity.this).showToast("密码不能为空");
            return false;
        }
        return true;
    }

    public final static String PHONE_PATTERN = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
    public static boolean isMatchered(String patternStr, CharSequence input) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    /**
     * doLogin
     * @param telephone
     * @param password
     */
    private void doLogin(final String telephone, String password) {
        // 使用Map封装请求参数
        HashMap<String, String> map = new HashMap<>();
        map.put("telephone", telephone);
        map.put("password", password);
        String url = OkHttpUtil.base_url + "login";
        try {
            OkHttpUtil.doPost(url, map, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.e("失败！");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String tempResponse =  response.body().string();
                    if(response.isSuccessful()){
                        Logger.e("成功！");
                    }
                    try{
                        Log.e("hh","jj"+tempResponse);
                        Logger.i(tempResponse);
                        JSONObject jsonObject=new JSONObject(tempResponse);
                        String returnCode=jsonObject.getString("code");
                        if("200".equals(returnCode)){
                            AppContext AppContext =new AppContext();
                            AppContext.setIsLogin(true);
                            /*setContentView(R.layout.activity_main);
                            TextView textView=(TextView)findViewById(R.id.account_text);
                            textView.setText(telephone);

                            Button button=(Button)findViewById(R.id.button1);
                            button.setVisibility(View.INVISIBLE);*/

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("telephone",telephone);
                            startActivity(intent);
                            finish();
                        }else{

                            if(counter==1){
                                counter=LOGIN_CHANCES;
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       et_password.setText("");
                                       ToastUtil.getInstance(LoginActivity.this).showToast("连续" + LOGIN_CHANCES + "次认证失败，请您" + WAIT_TIME / 1000 +"秒后再登陆！");
                                   }
                               });
                                errorTime = System.currentTimeMillis();
                                SharedPreferences sp1 = getSharedPreferences("data", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp1.edit();
                                editor.putLong("errorTime", errorTime);
                                editor.commit();
                            }else {
                                counter--;
                              runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      et_password.setText("");
                                      ToastUtil.getInstance(LoginActivity.this).showToast("用户名或密码错误，请重新输入!剩余"+counter+"机会");
                                  }
                              });
                            }


                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });  //POST方式
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
