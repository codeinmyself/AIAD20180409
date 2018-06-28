package com.xmu.lxq.aiad.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.util.OkHttpUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xmu.lxq.aiad.config.Config.userfiles_url;

public class AddWordsActivity extends AppCompatActivity {

    EditText productName, adMessage;
    Button next;
    TextView num1, num2;
    int numLimit1 = 10, numLimit2 = 20;
    String name, message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_words);
        setTitle("广告词添加");
        initView();
    }

    /**
     * initialView
     */
    private void initView() {
        productName = (EditText) findViewById(R.id.productName);
        adMessage = (EditText) findViewById(R.id.adMessage);
        num1 = (TextView) findViewById(R.id.num1);
        num1.setText("0/" + numLimit1);
        num2 = (TextView) findViewById(R.id.num2);
        num2.setText("0/" + numLimit2);
        //字数统计和限制
        productName.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                temp = s;
            }

            public void afterTextChanged(Editable s) {
                int number = s.length();
                num1.setText(number + "/" + numLimit1);
                selectionStart = productName.getSelectionStart();
                selectionEnd = productName.getSelectionEnd();
                if (temp.length() > numLimit1) {
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    productName.setText(s);
                    productName.setSelection(tempSelection);//设置光标在最后
                }
            }
        });
        adMessage.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                temp = s;
            }

            public void afterTextChanged(Editable s) {
                int number = s.length();
                num2.setText(number + "/" + numLimit2);
                selectionStart = productName.getSelectionStart();
                selectionEnd = productName.getSelectionEnd();
                if (temp.length() > numLimit2) {
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    productName.setText(s);
                    productName.setSelection(tempSelection);//设置光标在最后
                }
            }
        });

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeWordsToFile();
            }
        });


    }

    private void writeWordsToFile() {
        name = productName.getText().toString();
        message = adMessage.getText().toString();
        File file = new File(userfiles_url + "/", "DateRecording.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,"UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(name + "\r\n");
            bufferedWriter.write(message + "\r\n");
            bufferedWriter.close();
            outputStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = OkHttpUtil.base_url + "uploadTxt";
        OkHttpUtil.doFile(url, userfiles_url + "/" + "DateRecording.txt", "DateRecording.txt", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Logger.e("DateRecording.txt上传失败！");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Logger.i("DateRecording.txt上传成功！");
                Intent intent = new Intent(AddWordsActivity.this, ResultActivity.class);
                startActivity(intent);
            }
        });

    }
}
