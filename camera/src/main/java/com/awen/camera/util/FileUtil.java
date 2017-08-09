package com.awen.camera.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.awen.camera.CameraApplication;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Created by AwenZeng on 2016/12/2.
 */

public class FileUtil {

    /**
     * APP文件保存主路径
     */
    private static String filePath;


    public static String getSaveFileDir(String childDriName){
        return getAppFileSaveRootDri()+"/"+childDriName;
    }

    /**
     * 获取或创建，APP的子文件夹
     * @return
     */
    public static File getOrCreateAppDir(String childDriName){
        String root_path = getAppFileSaveRootDri();
        if(root_path == null)
            return null;

        File dir = new File(root_path +"/" + childDriName);
        if (!dir.exists())
            dir.mkdirs();

        return dir;
    }

    /**
     * 获取APP 的根文件夹路径
     * @return
     */
    public static String getAppFileSaveRootDri(){
        if(TextUtils.isEmpty(filePath)){	// 如果无外置 SD卡， 优先获取内置SD卡
            filePath = getExternalStorageDirectory(CameraApplication.getCommonLibContext());
        }

        if(TextUtils.isEmpty(filePath)){	// 如果都没有，才使用机身内存
            filePath = getInternalFileDirectory(CameraApplication.getCommonLibContext());
        }

        return filePath;
    };

    /**
     * 获取机身储存
     *
     * @param context
     * @return
     */
    public static String getInternalFileDirectory(Context context) {
        String fileDirPath = null;
        File fileDir = context.getFilesDir();
        if (fileDir != null) {
            fileDirPath = fileDir.getPath();
        }
        return fileDirPath;
    }

    /**
     * 获取内置SD卡
     * @param context
     * @return 没有外部sd卡，则返回null
     */
    public static String getExternalStorageDirectory(Context context) {
        String fileDirPath = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!sdCardExist) { // 不存在返回空
            return null;
        }
        fileDirPath = Environment.getExternalStorageDirectory().getPath();

        return fileDirPath;

    }

    // 获取SD卡根目录
    private static String getSDRoot() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }



    /**
     * 根据文件路径删除文件
     */
    public static void deleterFile(String filePath){
        File file = new File(filePath);
        if(file != null && file.exists()){
            try {
                file.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除文件
     */
    public static void deleterFile(File file){
        if(file != null && file.exists()){
            try {
                file.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除指定目录下文件及目录
     * @param deleteThisPath
     * @param deleteThisPath
     * @return
     */
    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件夹大小
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 获取指定文件大小
     * @param
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file)
    {
        long size = 0;
        try{
            if (file.exists()){
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                size = fis.available();
            }
            else{
                file.createNewFile();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return size;
    }

    /**
     * 格式化单位
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size/1024;
        if(kiloByte < 1) {
            return size + "B";
        }
        double megaByte = kiloByte/1024;
        if(megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }
        double gigaByte = megaByte/1024;
        if(gigaByte < 1) {
            BigDecimal result2  = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }
        double teraBytes = gigaByte/1024;
        if(teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    /**
     * 系统相机图片保存路径
     */
    public static File getSavePicPath(String name) {
        File dir = FileUtil.getOrCreateAppDir(name);
        File file = new File(dir, MD5Util.hashKeyForDisk(UUID.randomUUID()+"") + ".jpg");
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }
    /**
     * @param fileData
     * @Description: (存储文件)
     */
    public static String saveFile(byte[] fileData, String name) {
        File file = getSavePicPath(name);
        ByteArrayInputStream is = new ByteArrayInputStream(fileData);
        OutputStream os = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while (-1 != (len = is.read(buffer))) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            closeIO(is, os);
        }

        return file.getAbsolutePath();
    }

    /**
     * @param closeables
     * @Description: (关闭流)
     */
    public static void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                throw new RuntimeException(FileUtil.class.getClass().getName(), e);
            }
        }
    }
}
