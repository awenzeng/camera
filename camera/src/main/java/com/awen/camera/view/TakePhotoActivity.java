package com.awen.camera.view;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awen.camera.R;
import com.awen.camera.model.CameraModel;
import com.awen.camera.model.PermissionsModel;
import com.awen.camera.widget.CameraFocusView;
import com.awen.camera.widget.CameraSurfaceView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * 拍照界面
 * Created by AwenZeng on 2017/2/17.
 */

public class TakePhotoActivity extends Activity implements CameraFocusView.IAutoFocus {
    private CameraSurfaceView cameraSurfaceView;
    private CameraFocusView cameraFocusView;
    private ImageView openFlashImg;
    private ImageView cameraSwitchBtn;
    private ImageView closeBtn;
    private RelativeLayout cameraTopLayout;
    private ImageView cancelBtn;
    private ImageView takePhotoBtn;
    private ImageView okBtn;
    private CameraModel mCameraModel;
    private PermissionsModel mPermissionsModel;
    private byte[] photoData;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    public final static String RESULT_PHOTO_PATH = "photoPath";
    public static final int REQUEST_CAPTRUE_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_take_photo);
        init();
    }

    private void init(){
        mPermissionsModel = new PermissionsModel(this);
        mCameraModel = new CameraModel(this, cameraSurfaceView);
        //控件获取
        cameraSurfaceView = (CameraSurfaceView)findViewById(R.id.cameraSurfaceView);
        cameraFocusView = (CameraFocusView)findViewById(R.id.cameraFocusView);
        openFlashImg = (ImageView)findViewById(R.id.openFlashImg);
        cameraSwitchBtn = (ImageView)findViewById(R.id.cameraSwitchBtn);
        closeBtn = (ImageView)findViewById(R.id.closeBtn);
        cameraTopLayout = (RelativeLayout) findViewById(R.id.cameraTopLayout);
        cancelBtn = (ImageView)findViewById(R.id.cancelBtn);
        takePhotoBtn = (ImageView)findViewById(R.id.takePhotoBtn);
        okBtn = (ImageView)findViewById(R.id.okBtn);
        //控件处理
        cameraFocusView.setmIAutoFocus(this);
        openFlashImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFlash();
            }
        });
        cameraSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCamera();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rephotograph();
            }
        });
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhoto();
            }
        });
    }

    @Override
    public void autoFocus(float x, float y) {
        cameraSurfaceView.setAutoFocus((int)x,(int)y);
    }



    private void takePhoto() {
        cameraSurfaceView.takePicture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                if (data.length == 0)
                    return;
                cameraSurfaceView.getmCamera().stopPreview();// 关闭预览
                photoData = data;
                cancelBtn.setVisibility(View.VISIBLE);
                okBtn.setVisibility(View.VISIBLE);
                takePhotoBtn.setVisibility(View.GONE);
                cameraTopLayout.setVisibility(View.GONE);
                cameraFocusView.setVisibility(View.GONE);
                Animation leftAnim =  AnimationUtils.loadAnimation(TakePhotoActivity.this, R.anim.anim_slide_left);
                Animation rightAnim =  AnimationUtils.loadAnimation(TakePhotoActivity.this, R.anim.anim_slide_right);
                cancelBtn.setAnimation(leftAnim);
                okBtn.setAnimation(rightAnim);
            }
        });
    }

    private void rephotograph() {
        cancelBtn.setVisibility(View.GONE);
        okBtn.setVisibility(View.GONE);
        takePhotoBtn.setVisibility(View.VISIBLE);
        cameraTopLayout.setVisibility(View.VISIBLE);
        if(mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
            cameraFocusView.setVisibility(View.VISIBLE);
        }
        cameraSurfaceView.getmCamera().startPreview();// 开启预览
    }


    private void savePhoto() {
        mPermissionsModel.checkWriteSDCardPermission(new PermissionsModel.PermissionListener() {
            @Override
            public void onPermission(boolean isPermission) {
                if (isPermission) {
                    Observable.just(photoData)
                            .map(new Func1<byte[], String>() {
                                @Override
                                public String call(byte[] bytes) {
                                    String path = mCameraModel.handlePhoto(bytes, cameraSurfaceView.getCameraId());
                                    return path;
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String path) {
                                    onResult(path);
                                }
                            });
                }
            }
        });
    }

    private void openFlash() {
        if (!cameraSwitchBtn.isSelected()) { // 后摄像头才有闪光灯
            if (openFlashImg.isSelected()) { // 关灯
                openFlashImg.setSelected(false);
                mCameraModel.changeFlashMode(false, cameraSurfaceView.getmCamera(), cameraSurfaceView.getCameraId());
            } else { // 开灯
                openFlashImg.setSelected(true);
                mCameraModel.changeFlashMode(true, cameraSurfaceView.getmCamera(), cameraSurfaceView.getCameraId());
            }
        }
    }


    /**
     * 切换摄像头
     */
    private void changeCamera() {
        // 重新开启相应摄像头
        if (cameraSwitchBtn.isSelected()) { // 切后置摄像头
            cameraSwitchBtn.setSelected(false);
            openFlashImg.setVisibility(View.VISIBLE);
            cameraFocusView.setVisibility(View.VISIBLE);
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

        } else { // 切前置
            cameraSwitchBtn.setSelected(true);
            cameraFocusView.setVisibility(View.GONE);
            openFlashImg.setVisibility(View.INVISIBLE); // 前置摄像头隐藏闪光灯按钮
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        openFlashImg.setSelected(false);
        cameraSurfaceView.changeCamera(mCameraId);
    }

    public void onResult(String path) {
        setResult(RESULT_OK, new Intent().putExtra(RESULT_PHOTO_PATH, path));
        finish();
    }

}
