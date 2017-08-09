package com.awen.camera.util;

import android.text.TextUtils;
import android.widget.Toast;

import com.awen.camera.CameraApplication;


/**
 * Toast工具累
 * Created by AwenZeng on 2016/12/2.
 */

public class ToastUtil {

    private Toast toast;

    private static ToastUtil instance;

    public static ToastUtil getInstance(){
        if(instance == null){
            synchronized (ToastUtil.class){
                if(instance == null){
                    instance = new ToastUtil();
                }
            }
        }
        return instance;
    }


    public void toast(String content){
        if(!TextUtils.isEmpty(content)){
            showShort(content);
        }
    }


    /**
     * 短时间显示Toast
     * @param message
     */
    public void showShort(CharSequence message) {
        if (null == toast) {
            toast = Toast.makeText(CameraApplication.getCommonLibContext(), message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }


    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public void showLong(CharSequence message) {
        if (null == toast) {
            toast = Toast.makeText(CameraApplication.getCommonLibContext(), message, Toast.LENGTH_LONG);
        } else {
            toast.setText(message);
        }
        toast.show();
    }


    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public void show(int message, int duration) {
        if (null == toast) {
            toast = Toast.makeText(CameraApplication.getCommonLibContext(), message, duration);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    public void hideToast() {
        if (null != toast) {
            toast.cancel();
        }
    }

}
