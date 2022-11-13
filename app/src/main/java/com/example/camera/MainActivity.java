package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.ImageFormat;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    private SurfaceView camera_sf;
    private Button camera_btn;
    //安卓硬件相机
    private Camera mCamera;
    private SurfaceHolder mHolder;

    public static final String FILE_PATH = "filePath";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        methodRequiresTwoPermission();
        initViews();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    String PERMISSION_STORAGE_MSG = "请授予权限，否则影响部分使用功能";
    public static final int RC_CAMERA_AND_LOCATION = 123;
    String[] perms = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void methodRequiresTwoPermission() {

        if (EasyPermissions.hasPermissions(this, perms)) {
            // writeToSDcardFile("你好吗？？？？？？？？？？？");
            Toast.makeText(this, "权限Ok", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "no-------权限", Toast.LENGTH_LONG).show();
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, PERMISSION_STORAGE_MSG,
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mCamera) {
            mCamera = getCustomCamera();
            if (mHolder != null) {
                //开户相机预览
                //获取到相机参数

                Camera.Parameters parameters = mCamera.getParameters();
                //设置图片保存格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                //设置图片大小
                parameters.setPreviewSize(480, 720);
                //设置对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                previceCamera(mCamera, mHolder);
            }
        }
    }

    private Camera getCustomCamera() {
        if (null == mCamera) {
            //使用Camera的Open函数开机摄像头硬件

            mCamera = Camera.open(0);

            //Camera.open()方法说明：2.3以后支持多摄像头，所以开启前可以通过getNumberOfCameras先获取摄像头数目，
            // 再通过 getCameraInfo得到需要开启的摄像头id，然后传入Open函数开启摄像头，
            // 假如摄像头开启成功则返回一个Camera对象
        }
        return mCamera;
    }

    private void previceCamera(Camera camera, SurfaceHolder holder) {
        try {
            //摄像头设置SurfaceHolder对象，把摄像头与SurfaceHolder进行绑定
            camera.setPreviewDisplay(holder);
            //调整系统相机拍照角度
            camera.setDisplayOrientation(90);
            //调用相机预览功能
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        this.camera_sf = (SurfaceView) findViewById(R.id.camera_sf);
        this.camera_btn = (Button) findViewById(R.id.camera_btn);
        this.camera_btn.setOnClickListener(this);
        camera_sf.setOnClickListener(this);
        //获取SurfaceView的SurfaceHolder对象
        mHolder = camera_sf.getHolder();
        //实现SurfaceHolder.Callback接口
        mHolder.addCallback(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_sf://点击可以对焦
                if (null != mCamera)
                    mCamera.autoFocus(null);
                break;
            case R.id.camera_btn://点击进行拍照
                startTakephoto();
                break;
        }
    }

    private void startTakephoto() {

        //设置自动对焦
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            try {
                                dealWithCameraData(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    //保存拍照数据
    private void dealWithCameraData(byte[] data) throws IOException {
        FileOutputStream fos = null;
        //  String tempStr = Environment.getExternalStorageDirectory().getAbsolutePath();
        // String tempStr=this.getFilesDir().getAbsolutePath();
        //图片临时保存位置
        //  String fileName = tempStr + System.currentTimeMillis() + ".jpg";

        FileHelper fileHelper = new FileHelper(this);
        fileHelper.getquanxian();
        File fileName = fileHelper.createSDFile(System.currentTimeMillis() + ".jpg");


        //   File tempFile = new File(fileName);
        //  tempFile.createNewFile();
        try {
            Log.d("file:", fileName.getPath());
            fos = new FileOutputStream(fileName);
            Log.d("AAAAAAAAAAAAAAAAAAA", "DDDDDDDDDDDDDDDD");
            //保存图片数据
            fos.write(data);
            fos.close();
            Intent intent = new Intent(MainActivity.this, ShowResultActivity.class);
            intent.putExtra(FILE_PATH, fileName.getPath());
            startActivity(intent);
            finish();   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String writeToSDcardFile(String szOutText) {
        // 获取扩展SD卡设备状态
        String sDStateString = android.os.Environment.getExternalStorageState();

        File myFile = null;
        // 拥有可读可写权限
        if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {

            try {

                // 获取扩展存储设备的文件目录
                File SDFile = android.os.Environment
                        .getExternalStorageDirectory();

                // File destDir=new File("/sdcard/xmlfile");

                myFile = new File(SDFile.getAbsolutePath() + System.currentTimeMillis() + ".txt");

                Log.d("myFile:", myFile.getAbsolutePath());

                // 判断是否存在,不存在则创建
                if (!myFile.exists()) {
                    myFile.createNewFile();
                }
                Log.d("myFile---------------------------------:", myFile.getAbsolutePath());
                // 写数据
                // String szOutText = "Hello, World!";
                FileOutputStream outputStream = new FileOutputStream(myFile);
                outputStream.write(szOutText.getBytes());
                outputStream.close();

            } catch (Exception e) {
                // TODO: handle exception
            }// end of try

        }
        // end of func

        return myFile.toString();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        previceCamera(mCamera, holder);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        previceCamera(mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            //停止预览
            mCamera.stopPreview();
            //释放相机资源
            mCamera.release();
            mCamera = null;
        }
    }
}