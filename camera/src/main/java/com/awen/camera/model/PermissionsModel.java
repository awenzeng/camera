package com.awen.camera.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;

/**
 * 权限检测模块
 * Created by AwenZeng on 2017/3/3.
 */

public class PermissionsModel{
    private RxPermissions rxPermissions;
    private String packageName = "";
    private Context mContext;

    public interface PermissionListener {
        void onPermission(boolean isPermission);
    }


    public PermissionsModel(Context context) {
        mContext = context;
        rxPermissions = new RxPermissions((Activity) mContext);
        packageName = mContext.getPackageName();
    }


    public void checkCameraPermission(final PermissionListener listener) {
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (!aBoolean) {
                    gotoPermissionSetting();
                }
                if (listener != null) {
                    listener.onPermission(aBoolean);
                }
            }
        });
    }


    public void checkWriteSDCardPermission(final PermissionListener listener) {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (!aBoolean) {
                    gotoPermissionSetting();
                }
                if (listener != null) {
                    listener.onPermission(aBoolean);
                }
            }
        });
    }

    /**
     * 跳转到权限设置界面
     */
    private void gotoPermissionSetting() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", mContext.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", mContext.getPackageName());
        }
        mContext.startActivity(intent);
    }


}
