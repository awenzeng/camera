package com.awen.camera.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.awen.camera.CameraApplication;

public class ScreenSizeUtil {

    private static DisplayMetrics displayMetrics = getDisplayMetrics();


    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    public static int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    public static int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics);
    }

    public static int px2dp(int px) {
        float scale = displayMetrics.density;
        return (int) (px / scale + 0.5f);
    }

    public static int px2dp(float px) {
        float scale = displayMetrics.density;
        return (int) (px / scale + 0.5f);
    }


    public static int getScreenWidth() {
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight() {
        return displayMetrics.heightPixels;
    }

    public static int[] getScreenDispaly() {
        int[] temp = new int[2];
        temp[0] = displayMetrics.widthPixels;
        temp[1] = displayMetrics.heightPixels;
        return temp;
    }

    public static DisplayMetrics getDisplayMetrics() {
        WindowManager wm = (WindowManager) CameraApplication.getCommonLibContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }
}
