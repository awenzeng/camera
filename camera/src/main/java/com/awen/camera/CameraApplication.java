package com.awen.camera;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/8/2.
 */

public class CameraApplication extends Application {
    private static Application application;

    public static boolean DEBUG = true;

    public static void init(Application app,boolean debug){
        application = app;
        DEBUG = debug;
    }

    public static Context getCommonLibContext(){
        return application.getApplicationContext();
    }

    public static Application getCommonLibApplication(){
        return application;
    }
}
