package com.xmu.lxq.aiad.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.util.ToastUtil;

/**
 * Created by asus1 on 2017/12/26.
 */
public class ProductTypeActivity extends Activity{

    RadioButton radioButton0;
    RadioButton radioButton1;
    RadioButton radioButton2;
    RadioButton radioButton3;
    RadioButton radioButton4;
    RadioButton radioButton5;
    RadioButton radioButton6;

    LinearLayout linearLayout,linearLayout1,linearLayout2;
    RadioGroup rg0,rg1,rg2;

    String productType=null;//产品类型

    Button confirmButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_type);
        initView();
        initialize();
    }
    @Override
    public void onBackPressed(){
    }
    /**
     * initialView
     */
    private void initView(){
        linearLayout=(LinearLayout)findViewById(R.id.LinearLayout_Parent);
        rg0=(RadioGroup) linearLayout.getChildAt(1);
        linearLayout1=(LinearLayout)linearLayout.getChildAt(3);
        rg1=(RadioGroup) linearLayout1.getChildAt(0);
        linearLayout2=(LinearLayout)linearLayout.getChildAt(4);
        rg2=(RadioGroup) linearLayout2.getChildAt(0);

        radioButton0=(RadioButton)rg0.getChildAt(0);
        radioButton1=(RadioButton)rg1.getChildAt(0);
        radioButton2=(RadioButton)rg1.getChildAt(1);
        radioButton3=(RadioButton)rg1.getChildAt(2);

        radioButton4=(RadioButton)rg2.getChildAt(0);
        radioButton5=(RadioButton)rg2.getChildAt(1);
        radioButton6=(RadioButton)rg2.getChildAt(2);

        rg0.setOnCheckedChangeListener(listener0);
        rg1.setOnCheckedChangeListener(listener1);
        rg2.setOnCheckedChangeListener(listener2);
        confirmButton=(Button)findViewById(R.id.buttonConfirm1);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //要先判断是否选择了类型
                if(rg0.getCheckedRadioButtonId()==-1 &&
                        rg1.getCheckedRadioButtonId()==-1 &&
                        rg2.getCheckedRadioButtonId()==-1){
                    ToastUtil.getInstance(ProductTypeActivity.this).showToast("还未选择类型！");
                }else{
                    Intent intent=new Intent(ProductTypeActivity.this,TimeStyleActivity.class);
                    startActivity(intent);
                }

            }
        });
    }

    private RadioGroup.OnCheckedChangeListener listener0=new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId!=-1){
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                final String choose=String.valueOf(radioButton.getText());
                productType=choose;
                rg1.setOnCheckedChangeListener(null);rg1.clearCheck();rg1.setOnCheckedChangeListener(listener2);
                rg2.setOnCheckedChangeListener(null);rg2.clearCheck();rg2.setOnCheckedChangeListener(listener2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.getInstance(ProductTypeActivity.this).showToast("已选择"+choose);
                    }
                });
            }

        }
    };
    private RadioGroup.OnCheckedChangeListener listener1=new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId!=-1){
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                final String choose=String.valueOf(radioButton.getText());
                productType=choose;
                rg0.setOnCheckedChangeListener(null);rg0.clearCheck();rg0.setOnCheckedChangeListener(listener2);
                rg2.setOnCheckedChangeListener(null);rg2.clearCheck();rg2.setOnCheckedChangeListener(listener2);
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       ToastUtil.getInstance(ProductTypeActivity.this).showToast("已选择"+choose);

                   }
               });
            }

        }
    };

    private RadioGroup.OnCheckedChangeListener listener2=new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId!=-1){
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                final String choose=String.valueOf(radioButton.getText());
                rg0.setOnCheckedChangeListener(null);rg0.clearCheck();rg0.setOnCheckedChangeListener(listener2);
                rg1.setOnCheckedChangeListener(null);rg1.clearCheck();rg1.setOnCheckedChangeListener(listener1);
                productType=choose;
                Logger.i(String.valueOf(radioButton.getText()));
                Handler h = new Handler(Looper.getMainLooper());
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       ToastUtil.getInstance(ProductTypeActivity.this).showToast( "已选择"+choose);

                   }
               });
            }

        }
    };
    /**
     * set Text of radioButtons
     */
    private void initialize(){
        Intent intent=getIntent();
        String str[]=new String[5];
        int type[]=new int[6]; //顺序为book，daily，cloth，food，makeup，electronic
        str[0]=intent.getStringExtra("type1");
        str[1]=intent.getStringExtra("type2");
        str[2]=intent.getStringExtra("type3");
        str[3]=intent.getStringExtra("type4");
        str[4]=intent.getStringExtra("type5");
        //并没有String type6=intent.getStringExtra("其他");
        int index=classifyProcess(str,type);  //返回最优类型

        //图片文字
        Drawable icon=getResources().getDrawable(R.drawable.icon_cloth);
        String icon_txt="其他";
        if(index==0){icon =getResources().getDrawable(R.drawable.icon_book);icon_txt="学习办公";}
        else if (index==1) {icon=getResources().getDrawable(R.drawable.icon_shoe);icon_txt="日常用品";}
        else if (index==2) {icon=getResources().getDrawable(R.drawable.icon_cloth);icon_txt="衣帽鞋包";}
        else if (index==3) {icon=getResources().getDrawable(R.drawable.icon_snack);icon_txt="食物小吃";}
        else if (index==4) {icon=getResources().getDrawable(R.drawable.icon_makeup);icon_txt="美妆饰品";}
        else if (index==5) {icon=getResources().getDrawable(R.drawable.icon_book);icon_txt="数码电子";}

        icon.setBounds(0, 0, 200, 200);//第一0是距左右边距离，第二0是距上下边距离，第三69长度,第四宽度
        radioButton0.setCompoundDrawables(null,icon, null, null);//只放上面
        radioButton0.setText(icon_txt);
    }

    private int classifyProcess(String str[],int type[]){
        for(int i=0;i<5;i++) {
            str[i] = (str[i].substring(str[i].indexOf("#") + 1));// 取标签
            if(str[i].equals("book"))type[0]++;                         // 投票
            else if(str[i].equals("daily"))type[1]++;
            else if(str[i].equals("cloth"))type[2]++;
            else if(str[i].equals("food"))type[3]++;
            else if(str[i].equals("makeup"))type[4]++;
            else if(str[i].equals("electronic"))type[5]++;
        }
        int max=type[0],index=0;                                 //取最大票数
        for(int i=0;i<type.length;i++) {
            Logger.i(type[i]+" ");
            if(type[i]>max)   // 判断最大值
            {max=type[i];
                index=i;}
        }
        Logger.i("!!!最大值是"+index);
        return index;
    }
}
