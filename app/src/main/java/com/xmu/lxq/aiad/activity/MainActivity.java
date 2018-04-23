package com.xmu.lxq.aiad.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.xmu.lxq.aiad.BuildConfig;
import com.xmu.lxq.aiad.R;
import com.xmu.lxq.aiad.service.AppContext;
import com.xmu.lxq.aiad.util.NetworkDetector;
import com.xmu.lxq.aiad.util.OkHttpUtil;
import com.xmu.lxq.aiad.util.ToastUtil;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.xmu.lxq.aiad.config.Config.directorysUrl;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static ProgressDialog dialog = null;
    private int FLAG_DISMISS = 1;//关闭dialog的标志
    private boolean flag = true;//跳出循环的标志

    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    private ImageView picture;
    private TextView textView_account;
    private TextView notlogin;
    private Button button_to_login;
    private ImageView icon_image;
    private Uri imageUri;
    private Toolbar toolbar;
    private Button submitPic_button;

    public static final String IMAGE_FILE_NAME_TEMP = "faceImage_temp.jpg";
    private File cropFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME_TEMP);
    private Uri imageCropUri = Uri.fromFile(cropFile);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.more);

        submitPic_button = findViewById(R.id.button3);
        submitPic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //要先判断照片是否存在
                if(picture.getDrawable()==null ){
                    ToastUtil.getInstance(MainActivity.this).showToast("照片不存在！");
                }else if(!NetworkDetector.detectNetwork(MainActivity.this)){
                    ToastUtil.getInstance(MainActivity.this).showToast("网络未连接！");
                }else{
                    uploadPic();
                    showDialog();
                    mThread.start();
                }

            }
        });
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        //toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);
        notlogin = (TextView) view.findViewById(R.id.notlogin);
        icon_image = view.findViewById(R.id.icon_image);
        icon_image.setVisibility(View.INVISIBLE);
        AppContext AppContext = new AppContext();

        if (AppContext.isLogin) {
            icon_image.setVisibility(View.VISIBLE);
            String path = "/sdcard/AIAD/personal/icon.jpg";
            try {
                File file = new File(path);
                if (file.exists()) {
                    icon_image.setImageBitmap(getDiskBitmap(path));

                    //Glide.with(this).load(path).bitmapTransform(new CropCircleTransformation(this)).into(icon_image);
                } else {
                    Resources res = MainActivity.this.getResources();
                    Bitmap icon = BitmapFactory.decodeResource(res, R.drawable.album_color);
                    icon_image.setImageBitmap(icon);
                    //Glide.with(this).load(R.drawable.ic_launcher_background).bitmapTransform(new CropCircleTransformation(this)).into(icon_image);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }


        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String telephone = bundle.getString("telephone");
            if (telephone != null) {
                textView_account = (TextView) view.findViewById(R.id.account_text);
                textView_account.setText("手机号:" + telephone);
                textView_account.setVisibility(View.VISIBLE);
                button_to_login = (Button) view.findViewById(R.id.button1);
                button_to_login.setVisibility(View.INVISIBLE);
                notlogin.setVisibility(View.INVISIBLE);
            }
        }

        Button takePhoto = (Button) findViewById(R.id.take_photo);
        Button chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
        picture = (ImageView) findViewById(R.id.picture);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("进入拍照！");
                /*//测试用
                Intent intent1=new Intent(MainActivity.this,ProgressActivity.class);
                startActivity(intent1);
                //测试用*/
                Intent openCameraIntent = new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE);
                imageUri = Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), "image.jpg"));
                // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(openCameraIntent, TAKE_PICTURE);
            }
        });
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("进入相册！");
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });

        initialDirectory();
        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());*/
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    /**
     * onResume
     */
    @Override
    public void onRestart() {
        super.onRestart();
        AppContext AppContext = new AppContext();
        if (AppContext.isLogin) {
            icon_image.setVisibility(View.VISIBLE);
            String path = "/sdcard/AIAD/personal/" + "icon.jpg";
            try {
                File file = new File(path);
                if (file.exists()) {
                    icon_image.setImageBitmap(getDiskBitmap(path));

                    // Glide.with(this).load(path).bitmapTransform(new CropCircleTransformation(this)).into(icon_image);
                } else {
                    Resources res = MainActivity.this.getResources();
                    Bitmap icon = BitmapFactory.decodeResource(res, R.drawable.album_color);
                    icon_image.setImageBitmap(icon);
                    //Glide.with(this).load(R.drawable.ic_launcher_background).bitmapTransform(new CropCircleTransformation(this)).into(icon_image);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void initialDirectory() {
        for (int i = 0; i < directorysUrl.length; i++) {
            File file = new File(directorysUrl[i]);
            if (!file.exists()) {
                boolean bool = file.mkdirs();
                Logger.e("初始化需要的文件夹" + directorysUrl[i] + ",成功（true）失败（false）：" + bool);
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    /**
     * 从本地获取图片
     *
     * @param pathString 文件路径
     * @return 图片
     */
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


    /**
     * onNavigationItemSelected
     *
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.personal_info) {

            AppContext AppContext = new AppContext();
            if (AppContext.isLogin) {
                Intent intent = new Intent(MainActivity.this, PersonalInfo.class);
                startActivity(intent);
            } else {
                ToastUtil.getInstance(MainActivity.this).showToast("请先登录！");
            }

        } else if (id == R.id.nav_gallery) {

            AppContext AppContext = new AppContext();
            if (AppContext.isLogin) {

            } else {
                ToastUtil.getInstance(MainActivity.this).showToast("请先登录！");
            }
        } else if (id == R.id.nav_slideshow) {

            AppContext AppContext = new AppContext();
            if (AppContext.isLogin) {

            } else {
                ToastUtil.getInstance(MainActivity.this).showToast("请先登录！");
            }
        } else if (id == R.id.nav_manage) {
            AppContext AppContext = new AppContext();
            if (AppContext.isLogin) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            } else {
               /* ToastUtil.getInstance(MainActivity.this).showToast("请先登录！");*/
            }
        } else if (id == R.id.nav_share) {
            AppContext AppContext = new AppContext();
            if (AppContext.isLogin) {

            } else {
                ToastUtil.getInstance(MainActivity.this).showToast("请先登录！");
            }
        } else if (id == R.id.logout) {
            AppContext AppContext = new AppContext();
            AppContext.setIsLogin(true);
            //SharePreferenceUtil sharePreferenceUtil=new SharePreferenceUtil(MainActivity.this);
            if (AppContext.isLogin) {
                Logger.i("退出账号！");
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                View view = navigationView.getHeaderView(0);
                textView_account = (TextView) view.findViewById(R.id.account_text);
                textView_account.setVisibility(View.INVISIBLE);

                button_to_login = (Button) view.findViewById(R.id.button1);
                button_to_login.setText("登录");
                button_to_login.setVisibility(View.VISIBLE);
                icon_image.setVisibility(View.INVISIBLE);
                notlogin.setVisibility(View.VISIBLE);
                // sharePreferenceUtil.setStateLogout();
                AppContext.setIsLogin(false);
            } else {
                ToastUtil.getInstance(MainActivity.this).showToast("并未登录！");
            }


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * button1_click
     *
     * @param view
     */
    public void button1_click(View view) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * icon_image_click
     *
     * @param view
     */
    public void icon_image_click(View view) {
        AppContext AppContext = new AppContext();
        if (AppContext.isLogin) {
            Intent intent = new Intent(MainActivity.this, IconActivity.class);
            startActivity(intent);
        } else {
            ToastUtil.getInstance(MainActivity.this).showToast("请先登录！");
        }
    }

    /**
     * openAlbum
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PICTURE);
    }

    /**
     * onRequestPermissionsResult
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    ToastUtil.getInstance(MainActivity.this).showToast("没有权限！");
                }
                break;
            default:
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Logger.e("The uri is not exist.");
        }
        imageUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 100);
        intent.putExtra("aspectY", 100);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 450);
        intent.putExtra("outputY", 450);
        //intent.putExtra("return-data", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CROP_SMALL_PICTURE:
                    setImageToView(data);
                    break;
                case TAKE_PICTURE:
                    startPhotoZoom(imageUri);
                    break;
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param data
     */
    protected void setImageToView(Intent data) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageCropUri));
            picture.setImageBitmap(bitmap);
            saveBitmapAsPNG(bitmap);
            //bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * saveBitmapAsPNG
     *
     * @param bitmap
     */
    private void saveBitmapAsPNG(Bitmap bitmap) {
        File file = new File("/sdcard/product.jpg");
        try {
            BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bout);
            bout.flush();
            bout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 进度条
     */
    private void showDialog() {
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置水平进度条
        dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.setTitle("识别中");
        dialog.setMax(100);
        dialog.setMessage("请等待");
        dialog.show();
    }

    /**
     * 子线程控制dialog存在时间
     */
    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (flag) {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(1000); //每隔1s进行判断
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    });

    /**
     * 关闭dialog
     */
    public void dismiss() {
        dialog.dismiss();
        flag = false;
    }

    /**
     * uploadPic
     */
    private void uploadPic() {
        File img = new File("/sdcard/product.jpg");
        String url = OkHttpUtil.base_url + "uploadProductImage";
        OkHttpUtil.doFile(url, "/sdcard/product.jpg", img.getName(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.i("error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String returnCode = jsonObject.getString("code");
                        if ("200".equals(returnCode)) {
                            jsonObject = jsonObject.getJSONObject("detail");
                            String type[] = new String[5];
                            type[0] = jsonObject.getString("type1");
                            type[1] = jsonObject.getString("type2");
                            type[2] = jsonObject.getString("type3");
                            type[3] = jsonObject.getString("type4");
                            type[4] = jsonObject.getString("type5");
                            Message msg = mHandler.obtainMessage();
                            msg.what = FLAG_DISMISS;
                            Intent intent = new Intent(MainActivity.this, ProductTypeActivity.class);
                            intent.putExtra("type1", type[0]);
                            intent.putExtra("type2", type[1]);
                            intent.putExtra("type3", type[2]);
                            intent.putExtra("type4", type[3]);
                            intent.putExtra("type5", type[4]);
                            startActivity(intent);
                        } else {
                            Logger.i("error");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Logger.i("error");
                }

            }
        });
    }

    /**
     * 接收子线程传回的信息
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == FLAG_DISMISS) {
                dismiss();
            }
        }
    };

   /* @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)){
            //如果是document类型的uri，则通过document id 处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" +id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            //如果是content类型的uri则使用普通的方式处理
            imagePath = getImagePath(uri,null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的URI则直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath);//根据图片路径显示图片
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }*/
   /* private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过URI和selection来获取真是的图片路径
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }*/


    //此方法作废
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            //将照片显示出来
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(getApplicationContext(), "failed to get image!", Toast.LENGTH_SHORT).show();
        }
    }

    //因换UI缘故，此方法作废
    public void showNextDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("上传图片");
        builder.setMessage("下一步？");
        builder.setCancelable(false);
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /*picture.setDrawingCacheEnabled(true);
                Bitmap bitmap=picture.getDrawingCache();
                picture.setDrawingCacheEnabled(false);*/
                uploadPic();

            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //创建对话框
        AlertDialog dialog = builder.create();
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

        lp.y = 10; // 新位置Y坐标
        lp.width = 20; // 宽度
        lp.height = 20; // 高度
        lp.alpha = 0.7f; // 透明度
        dialogWindow.setAttributes(lp);

        dialog.show();
    }
}
