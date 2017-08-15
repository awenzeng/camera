package com.awen.camerademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.awen.camera.model.PermissionsModel;
import com.awen.camera.util.BitmapUtil;
import com.awen.camera.view.TakePhotoActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getName();

    private Button cameraBtn;
    private ImageView showTakePhotoImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraBtn = (Button) findViewById(R.id.cameraBtn);
        showTakePhotoImg = (ImageView) findViewById(R.id.showTakePhotoImg);
        cameraBtn.setOnClickListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TakePhotoActivity.REQUEST_CAPTRUE_CODE: {
                    String path = data.getStringExtra(TakePhotoActivity.RESULT_PHOTO_PATH);
                    showTakePhotoImg.setImageBitmap(BitmapUtil.getBitmap(path));
                    Log.v(TAG, "图片地址：" + path);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        PermissionsModel permissionsModel = new PermissionsModel(this);
        permissionsModel.checkCameraPermission(new PermissionsModel.PermissionListener() {
            @Override
            public void onPermission(boolean isPermission) {
                if (isPermission) {
                    Intent intent = new Intent(MainActivity.this, TakePhotoActivity.class);
                    startActivityForResult(intent, TakePhotoActivity.REQUEST_CAPTRUE_CODE);
                }
            }
        });
    }
}
