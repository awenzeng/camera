package com.awen.camera.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.awen.camera.util.LogUtil;
import com.awen.camera.util.ScreenSizeUtil;
import com.awen.camera.util.ToastUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by AwenZeng on 2016/12/5.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {
    private static final String TAG = CameraSurfaceView.class.getName();

    private Context mContext;
    private SurfaceHolder holder;
    private Camera mCamera;
    /**
     * 当前摄像头id
     */
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private CameraOrientationDetector mCameraOrientation;

    private int mScreenWidth;
    private int mScreenHeight;
    /**
     * 默认照片的宽
     */
    public static final int DEFAULT_PHOTO_WIDTH = ScreenSizeUtil.getScreenHeight();
    /**
     * 默认照片高
     */
    public static final int DEFAULT_PHOTO_HEIGHT = ScreenSizeUtil.getScreenWidth();

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        holder = getHolder();//获得surfaceHolder引用
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置类型
        mScreenWidth = DEFAULT_PHOTO_WIDTH;
        mScreenHeight = DEFAULT_PHOTO_HEIGHT;
        mCameraOrientation = new CameraOrientationDetector(mContext, SensorManager.SENSOR_DELAY_NORMAL);
        if (mCameraOrientation.canDetectOrientation()) {
            mCameraOrientation.enable();
        } else {
            LogUtil.d(TAG, "Can't Detect Orientation");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();//开启相机
            } catch (RuntimeException e) {
                e.printStackTrace();
                LogUtil.d(TAG, "摄像头异常，请检查摄像头权限是否应许");
                ToastUtil.getInstance().toast("摄像头异常，请检查摄像头权限是否应许");
                return;
            }
            try {
                mCamera.setPreviewDisplay(holder);//摄像头画面显示在Surface上
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            setCameraParams(mScreenWidth, mScreenHeight);
            mCamera.startPreview();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera Camera) {
        if (success) {
            LogUtil.i(TAG, "onAutoFocus success=" + success);
        }
    }


    /**
     * 设置分辨率等参数
     *
     * @param width  宽
     * @param height 高
     */
    private void setCameraParams(int width, int height) {
        LogUtil.i(TAG, "setCameraParams  width=" + width + "  height=" + height);

        Camera.Parameters parameters = mCamera.getParameters();


        /*************************** 获取摄像头支持的PictureSize列表********************/
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        sort(pictureSizeList);//排序
        for (Camera.Size size : pictureSizeList) {
            LogUtil.i(TAG, "摄像头支持的分辨率：" + " size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size picSize = getBestSupportedSize(pictureSizeList, ((float) height / width));//从列表中选取合适的分辨率
        if (null == picSize) {
            picSize = parameters.getPictureSize();
        }

        LogUtil.e(TAG, "我们选择的摄像头分辨率：" + "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        parameters.setPictureSize(picSize.width, picSize.height);


        /*************************** 获取摄像头支持的PreviewSize列表********************/
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        sort(previewSizeList);
        for (Camera.Size size : previewSizeList) {
            LogUtil.i(TAG, "摄像支持可预览的分辨率：" + " size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getBestSupportedSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            LogUtil.e(TAG, "我们选择的预览分辨率：" + "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }


        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF); // 关闪光灯
        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }
        mCamera.setErrorCallback(new Camera.ErrorCallback() {

            @Override
            public void onError(int error, Camera camera) {
                String error_str;
                switch (error) {
                    case Camera.CAMERA_ERROR_SERVER_DIED: // 摄像头已损坏
                        error_str = "摄像头已损坏";
                        break;

                    case Camera.CAMERA_ERROR_UNKNOWN:
                        error_str = "摄像头异常，请检查摄像头权限是否应许";
                        break;

                    default:
                        error_str = "摄像头异常，请检查摄像头权限是否应许";
                        break;
                }
                ToastUtil.getInstance().toast(error_str);
                Log.i(TAG, error_str);
            }
        });

        // 横竖屏镜头自动调整
        if (mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(90);
        } else {
            mCamera.setDisplayOrientation(0);
        }
        parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP); // 预览格式
        parameters.setPictureFormat(PixelFormat.JPEG); // 相片格式为JPEG，默认为NV21
        mCamera.cancelAutoFocus();//自动对焦。
        mCamera.setParameters(parameters);

    }

    /**
     * 排序：从大到小
     *
     * @param list
     */
    private void sort(List<Camera.Size> list) {
        Collections.sort(list, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return o2.width - o1.width;
            }
        });
    }

    /**
     * 如包含默认尺寸，则选默认尺寸，如没有，则选最大的尺寸
     * 规则：在相同比例下，1.优先寻找长宽分辨率相同的->2.找长宽有一个相同的分辨率->3.找最大的分辨率
     *
     * @param sizes 尺寸集合
     * @return 返回合适的尺寸
     */
    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, float screenRatio) {
        Camera.Size largestSize = null;
        int largestArea = 0;
        for (Camera.Size size : sizes) {
            if ((float) size.height / (float) size.width == screenRatio) {
                if (size.width == DEFAULT_PHOTO_WIDTH && size.height == DEFAULT_PHOTO_HEIGHT) {
                    // 包含特定的尺寸，直接取该尺寸
                    largestSize = size;
                    break;
                } else if (size.height == DEFAULT_PHOTO_HEIGHT || size.width == DEFAULT_PHOTO_WIDTH) {
                    largestSize = size;
                    break;
                }
                int area = size.height + size.width;
                if (area > largestArea) {//找出最大的合适尺寸
                    largestArea = area;
                    largestSize = size;
                }
            } else if (size.height == DEFAULT_PHOTO_HEIGHT || size.width == DEFAULT_PHOTO_WIDTH) {
                largestSize = size;
                break;
            }
        }
        if (largestSize == null) {
            largestSize = sizes.get(sizes.size() - 1);
        }
        return largestSize;
    }

    /**
     * 拍照
     * @param callback
     */
    public void takePicture(Camera.PictureCallback callback) {
        if (mCamera != null) {
            int orientation = mCameraOrientation.getOrientation();
            Camera.Parameters cameraParameter = mCamera.getParameters();
            if (orientation == 90) {
                cameraParameter.setRotation(90);
                cameraParameter.set("rotation", 90);
            } else if (orientation == 180) {
                cameraParameter.setRotation(180);
                cameraParameter.set("rotation", 180);
            } else if (orientation == 270) {
                cameraParameter.setRotation(270);
                cameraParameter.set("rotation", 270);
            } else {
                cameraParameter.setRotation(0);
                cameraParameter.set("rotation", 0);
            }
            mCamera.setParameters(cameraParameter);
        }
        mCamera.takePicture(null, null, callback);
    }

    /**
     * 切换摄像头
     */
    public void changeCamera(int camera_id) {
        mCamera.stopPreview();
        mCamera.release();
        openCamera(camera_id);
        setCameraParams(mScreenWidth, mScreenHeight);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean openCamera(int camera_id) {
        LogUtil.i(TAG, "openCamera id = " + camera_id);
        try {
            mCamera = Camera.open(camera_id); // 打开摄像头
            cameraId = camera_id;

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.getInstance().toast("请先开启摄像头权限");
            LogUtil.i(TAG, "请先开启摄像头权限");
            return false;
        }

        return true;
    }

    public Camera getmCamera() {
        return mCamera;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setAutoFocus() {
        mCamera.autoFocus(this);
    }
}
