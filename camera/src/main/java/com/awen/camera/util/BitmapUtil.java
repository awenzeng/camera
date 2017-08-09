package com.awen.camera.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.widget.ImageView;

import com.awen.camera.CameraApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by AwenZeng on 2016/12/28.
 */

public class BitmapUtil {
    /**
     * 通过图片路径获取Bitmap
     *
     * @param imgPath
     * @return
     */
    public static Bitmap getBitmap(String imgPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        newOpts.inSampleSize = 1;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(imgPath, newOpts);
    }


    /**
     * 保存Bitmap到指定路径
     *
     * @param bitmap
     * @param outPath
     * @throws FileNotFoundException
     */
    public static void saveImage(Bitmap bitmap, String outPath) throws FileNotFoundException {
        FileOutputStream os = new FileOutputStream(outPath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
    }

    public static int getBitmapBytes(Bitmap bitmap){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){     //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){//API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }


    /**
     * 尺寸压缩,按指定分辨率压缩
     *
     * @param imgPath
     * @param pixelW
     * @param pixelH
     * @return
     */
    public static Bitmap compressByResolution(String imgPath, float pixelW, float pixelH) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 想要缩放的目标尺寸
        float height = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float width = pixelW;// 设置宽度为120f，可以明显看到图片缩小了

        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > height) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (w / height);
        } else if (w < h && h > width) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (h / width);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        return bitmap;
    }

    /**
     * 尺寸压缩Bitmap,按指定分辨率压缩(先对Bitmap进行质量压缩，然后再尺寸压缩)
     * 一般用于缩略图
     *
     * @param image
     * @param pixelW
     * @param pixelH
     * @return
     */
    public static Bitmap compressByResolution(Bitmap image, float pixelW, float pixelH) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        if (os.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, os);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float height = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float width = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > width) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (w / width);
        } else if (w < h && h > height) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (h / height);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        is = new ByteArrayInputStream(os.toByteArray());
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        return bitmap;
    }


    /**
     * 通过Bitmap生成缩略图，并保存图片（按分辨率压缩）
     *
     * @param image
     * @param outPath
     * @param pixelW
     * @param pixelH
     * @throws FileNotFoundException
     */
    public static void compressByResolution(Bitmap image, String outPath, float pixelW, float pixelH) throws FileNotFoundException {
        Bitmap bitmap = compressByResolution(image, pixelW, pixelH);
        saveImage(bitmap, outPath);
    }

    /**
     * 通过图片路径生成缩略图，并保存图片（按分辨率压缩）
     *
     * @param imgPath
     * @param outPath
     * @param pixelW
     * @param pixelH
     * @param needsDelete 是否删除原图
     * @throws FileNotFoundException
     */
    public static void compressByResolution(String imgPath, String outPath, float pixelW, float pixelH, boolean needsDelete) throws FileNotFoundException {
        Bitmap bitmap = compressByResolution(imgPath, pixelW, pixelH);
        saveImage(bitmap, outPath);
        if (needsDelete) {
            File file = new File(imgPath);
            if (file.exists()) {
                file.delete();
            }
        }

    }

    /**
     * 质量压缩Bitmap，指定保存路径，
     *
     * @param image
     * @param outPath
     * @param maxSize 压缩图片大小（Kb）
     * @throws IOException
     */
    public static void compressByQuality(Bitmap image, String outPath, int maxSize) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int options = 90;
        image.compress(Bitmap.CompressFormat.JPEG, options, os);
        while (os.toByteArray().length / 1024 > maxSize) {
            os.reset();
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, os);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(os.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        saveBitmap(bitmap,outPath);
    }

    /**
     * 质量压缩，指定保存路径，
     *
     * @param imgPath     图片路径
     * @param outPath     保存路径
     * @param maxSize     压缩图片大小（Kb）
     * @param needsDelete 是否删除原图片
     * @throws IOException
     */
    public static void compressByQuality(String imgPath, String outPath, int maxSize, boolean needsDelete) throws IOException {
        compressByQuality(getBitmap(imgPath), outPath, maxSize);
        if (needsDelete) {
            File file = new File(imgPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    /**
     * 获取旋转后图片
     *
     * @param path
     * @param bitmap
     * @return
     */
    public static Bitmap getAfterBitmap(String path, Bitmap bitmap) {
        int degree = getPhotoDegree(path);
        return rotateBitmap(bitmap, degree);
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int getPhotoDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度  0、 90°、180°、270°
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 保存缓存图片
     *
     * @param bitmap
     * @return
     * @throws IOException
     */
    public static String saveBitmap(Bitmap bitmap) throws IOException {
        File file = FileUtil.getSavePicPath(CameraApplication.getCommonLibApplication().getPackageName());
        String filePath = file.getAbsolutePath();
        FileOutputStream fOut = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
        fOut.flush();
        fOut.close();
        bitmap.recycle();
        return filePath;
    }

    /**
     * 保存缓存图片
     *
     * @param bitmap
     * @return
     * @throws IOException
     */
    public static String saveBitmap(Bitmap bitmap, String path) throws IOException {
        FileOutputStream fOut = new FileOutputStream(path);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        fOut.flush();
        fOut.close();
        bitmap.recycle();
        return path;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    /**
     * Drawable → Bitmap
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    /*
         * Bitmap → Drawable
		 */
    @SuppressWarnings("deprecation")
    public static Drawable bitmap2Drawable(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        BitmapDrawable bd = new BitmapDrawable(bm);
        bd.setTargetDensity(bm.getDensity());
        return new BitmapDrawable(bm);
    }

    /**
     * 回收ImageView图片资源
     * @param imageView
     */
    public static void recycleImgBg(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getBackground();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            imageView.setBackgroundDrawable(null);
        }
    }
}
