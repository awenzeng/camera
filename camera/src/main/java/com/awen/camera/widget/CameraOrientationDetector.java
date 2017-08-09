package com.awen.camera.widget;

import android.content.Context;
import android.view.OrientationEventListener;

/**
 * 方向变化监听器，监听传感器方向的改变
 * Created by AwenZeng on 2017/2/21.
 */

public class CameraOrientationDetector extends OrientationEventListener {
    int mOrientation;

    public CameraOrientationDetector(Context context, int rate) {
        super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        this.mOrientation = orientation;
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }
        //保证只返回四个方向,分别为0°、90°、180°和270°中的一个
        int newOrientation = ((orientation + 45) / 90 * 90) % 360;
        if (newOrientation != mOrientation) {
            mOrientation = newOrientation;
        }
    }

    public int getOrientation() {
        return mOrientation;
    }
}
