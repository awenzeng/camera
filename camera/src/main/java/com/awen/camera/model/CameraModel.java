package com.awen.camera.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.awen.camera.util.BitmapUtil;
import com.awen.camera.util.FileUtil;
import com.awen.camera.widget.CameraSurfaceView;


/**
 * Created by AwenZeng on 2017/2/17.
 */

public class CameraModel {
    private final String TAG = this.getClass().getName();
    private Context mContext;
    private CameraSurfaceView mCameraSurfaceView;
    private Camera mCamera;
    /**
     * 闪光灯是否打开中
     */
    private boolean isOpenFlash = false;


    public CameraModel(Context context) {
        mContext = context;
    }


    public CameraModel(Context context, CameraSurfaceView cameraSurfaceView) {
        mContext = context;
        mCameraSurfaceView = cameraSurfaceView;
    }

    /**
     * 设置闪光灯
     *
     * @param isOpen
     */
    public void changeFlashMode(boolean isOpen, Camera mCamera, int cameraId) {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) { // 后摄像头才有闪光灯
            Camera.Parameters parameters = mCamera.getParameters();
            PackageManager pm = mContext.getPackageManager();
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            for (FeatureInfo f : features) {
                if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) { // 判断设备是否支持闪光灯
                    if (isOpen) {
                        isOpenFlash = true;
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH); // 开闪光灯

                    } else {
                        isOpenFlash = false;
                        parameters
                                .setFlashMode(Camera.Parameters.FLASH_MODE_OFF); // 关闪光灯

                    }
                }
            }
            mCamera.setParameters(parameters);
        }
    }

    public String handlePhoto(byte[] data, int cameraId) {
        String filePath = FileUtil.saveFile(data, "/DCIM");
        if (!TextUtils.isEmpty(filePath)) {
            int degree = BitmapUtil.getPhotoDegree(filePath);
            Log.i(TAG, degree + "");
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            Bitmap tBitmap = null;
            try {
                Log.i(TAG, "保存图片大小："+"width = " + bitmap.getWidth() + "   ------ height = " + bitmap.getHeight());
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    switch (degree) {
                        case 0:
                            tBitmap = BitmapUtil.rotateBitmap(bitmap, 90);
                            filePath = BitmapUtil.saveBitmap(tBitmap == null ? bitmap : tBitmap, filePath);
                            break;
                        case 90:
                            tBitmap = BitmapUtil.rotateBitmap(bitmap, 180);
                            filePath = BitmapUtil.saveBitmap(tBitmap == null ? bitmap : tBitmap, filePath);
                            break;
                        case 180:
                            tBitmap = BitmapUtil.rotateBitmap(bitmap, 270);
                            filePath = BitmapUtil.saveBitmap(tBitmap == null ? bitmap : tBitmap, filePath);
                            break;
                        case 270:
                            tBitmap = BitmapUtil.rotateBitmap(bitmap, 360);
                            filePath = BitmapUtil.saveBitmap(tBitmap == null ? bitmap : tBitmap, filePath);
                            break;
                    }
                } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    switch (degree) {
                        case 0:
                            tBitmap = BitmapUtil.rotateBitmap(bitmap, 270);
                            filePath = BitmapUtil.saveBitmap(tBitmap == null ? bitmap : tBitmap, filePath);
                            break;
                        case 90:
                            tBitmap = BitmapUtil.rotateBitmap(bitmap, 180);
                            filePath = BitmapUtil.saveBitmap(tBitmap == null ? bitmap : tBitmap, filePath);
                            break;
                        case 180:
                            tBitmap = BitmapUtil.rotateBitmap(bitmap, 90);
                            filePath = BitmapUtil.saveBitmap(tBitmap == null ? bitmap : tBitmap, filePath);
                            break;
                        case 270:
                            tBitmap = BitmapUtil.rotateBitmap(bitmap, 360);
                            filePath = BitmapUtil.saveBitmap(tBitmap == null ? bitmap : tBitmap, filePath);
                            break;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                // 重新拍照
                return "";
            } finally {
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                if (tBitmap != null) {
                    tBitmap.recycle();
                    tBitmap = null;
                }
                ScannerByReceiver(mContext, filePath);//图库扫描
            }

            return filePath;
        }
        return null;
    }

    /**
     * Receiver扫描更新图库图片
     **/
    private static void ScannerByReceiver(Context context, String path) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + path)));
    }

}
