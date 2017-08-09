package com.awen.camerademo;

import android.app.Application;

import com.awen.camera.CameraApplication;

/**
 * Created by Administrator on 2017/8/7.
 */

public class CameraDemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CameraApplication.init(this,true);
    }
}
