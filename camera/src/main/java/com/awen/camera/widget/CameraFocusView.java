package com.awen.camera.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.awen.camera.util.ScreenSizeUtil;


/**
 * Created by AwenZeng on 2016/12/5.
 */
public class CameraFocusView extends View {
    private int mScreenWidth;
    private int mScreenHeight;
    private Paint mPaint;
    private Point centerPoint;
    private int radius;
    private int lastValue;
    private ValueAnimator lineAnimator;
    private boolean isShow = false;
    private final int GREEN_RADIUS = 87;//变绿半径
    private final int DURATION_TIME = 1000;
    private final int TOP_CONTROL_HEIGHT = ScreenSizeUtil.dp2px(50);//顶部控制栏高度（包括状态栏）
    private final int BETTOM_CONTROL_HEIGHT = ScreenSizeUtil.dp2px(105);//底部控制区域高度

    private IAutoFocus mIAutoFocus;

    /**
     * 聚焦的回调接口
     */
    public interface IAutoFocus {
        void autoFocus(float x,float y);
    }

    public void setmIAutoFocus(IAutoFocus mIAutoFocus) {
        this.mIAutoFocus = mIAutoFocus;
    }

    public CameraFocusView(Context context) {
        this(context, null);
    }

    public CameraFocusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraFocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);// 抗锯齿
        mPaint.setDither(true);// 防抖动
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5);
        mScreenWidth = ScreenSizeUtil.getScreenWidth();
        mScreenHeight =ScreenSizeUtil.getScreenHeight();
        mPaint.setStyle(Paint.Style.STROKE);// 空心
        centerPoint = new Point(mScreenWidth / 2, mScreenHeight / 2);
        radius = (int) (mScreenWidth * 0.1);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isShow){
            if(radius == GREEN_RADIUS){
                mPaint.setColor(Color.GREEN);
            }
            if(centerPoint!=null){
                canvas.drawCircle(centerPoint.x, centerPoint.y, radius, mPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int x = (int) event.getX();
                int y = (int) event.getY();
                lastValue = 0;
                mPaint.setColor(Color.WHITE);
                radius = (int) (mScreenWidth * 0.1);
                centerPoint = null;
                if(y>TOP_CONTROL_HEIGHT&&y<ScreenSizeUtil.getScreenHeight()-BETTOM_CONTROL_HEIGHT){//状态栏和底部禁止点击获取焦点（显示体验不好）
                    centerPoint = new Point(x, y);
                    showAnimView();
                    //开始对焦
                    if (mIAutoFocus != null) {
                        mIAutoFocus.autoFocus(event.getX(),event.getY());
                    }
                }
                break;
        }
        return true;
    }


    private void showAnimView() {
        isShow = true;
        if (lineAnimator == null) {
            lineAnimator = ValueAnimator.ofInt(0, 20);
            lineAnimator.setDuration(DURATION_TIME);
            lineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int animationValue = (Integer) animation
                            .getAnimatedValue();
                    if(lastValue!=animationValue&&radius>=(int) ((mScreenWidth * 0.1)-20)){
                        radius = radius - animationValue;
                        lastValue = animationValue;
                    }
                    isShow = true;
                    invalidate();
                }
            });
            lineAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isShow = false;
                    lastValue = 0;
                    mPaint.setColor(Color.WHITE);
                    radius = (int) (mScreenWidth * 0.1);
                    invalidate();
                }
            });
        }else{
            lineAnimator.end();
            lineAnimator.cancel();
            lineAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            lineAnimator.start();
        }
    }
}
