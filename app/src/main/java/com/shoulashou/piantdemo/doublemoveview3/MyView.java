package com.shoulashou.piantdemo.doublemoveview3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by likeye on 2020/7/28 13:49.
 **/
public class MyView extends View {
    Pointw pointw,prepoint;
    public int flag;
    private Bitmap mSrcBitmap;
    private Canvas mCurrentCanvas; // 当前绘制线的画布
    private Bitmap mCurrentBitmap; // 绘制当前线时用到的图片
    private int[] mWhiteBuffer;//保存白色图片内存，刷新时重新刷新图片

    private Bitmap mMultiplyBitmap = null;//混合之后的图片,双指缩放移动的时候,单独移动这张混合后图片,提高用户体验
    public boolean mIsMove;//是否双指拖动图片中ing
    private int mBitmapWidth, mBitmapHeight;//图片的长度和高度
    private float mCenterLeft, mCenterTop;//图片居中时左上角的坐标
    private int mCenterHeight, mCenterWidth; // 图片适应屏幕时的大小
    private float mCenterScale;//画布居中时的比例
    private int mViewWidth, mViewHeight;//当前View的长度和宽度
    private float mTransX = 0, mTransY = 0; // 偏移量，图片真实偏移量为　mCentreTranX + mTransX
    private float mScale = 1.0f; // 缩放倍数, 图片真实的缩放倍数为 mPrivateScale * mScale
    private boolean mIsSaveArg = false;//保存参数使用
    private PorterDuffXfermode mPdXfermode; // 定义PorterDuffXfermode变量
    private Paint mPdXfPaint,mPaint;// 绘图的混合模式
    public MyView(Context context, Bitmap bitmap) {
        super(context);
        init(bitmap);
    }
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private void init(Bitmap bitmap) {
        mSrcBitmap = bitmap;
        mBitmapWidth = mSrcBitmap.getWidth();
        mBitmapHeight = mSrcBitmap.getHeight();
        mCurrentBitmap = getTransparentBitmap(mSrcBitmap);
        mMultiplyBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_8888);
        //设置混合模式   （正片叠底）
        mPdXfermode = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
        mPdXfPaint = new Paint();
        mPdXfPaint.setAntiAlias(true);
        mPdXfPaint.setFilterBitmap(true);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5);
        mIsMove = false;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        float nw = mBitmapWidth * 1f / mViewWidth;
        float nh = mBitmapHeight * 1f / mViewHeight;
        if (nw > nh) {
            mCenterScale = 1 / nw;
            mCenterWidth = mViewWidth;
            mCenterHeight = (int) (mBitmapHeight * mCenterScale);
        } else {
            mCenterScale = 1 / nh;
            mCenterWidth = (int) (mBitmapWidth * mCenterScale);
            mCenterHeight = mViewHeight;
        }
        // 使图片居中
        mCenterLeft = (mViewWidth - mCenterWidth) / 2f;
        mCenterTop = (mViewHeight - mCenterHeight) / 2f;
        initCurrentCanvas();
        mIsMove = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scale = mCenterScale * mScale;
        float x = (mCenterLeft + mTransX) / scale;
        float y = (mCenterTop + mTransY) / scale;
        canvas.scale(scale, scale);
        if (!mIsMove) {
            initCurrentCanvas();
            if(prepoint!=null){
                mCurrentCanvas.drawLine(prepoint.getX(),prepoint.getY(),pointw.getX(),pointw.getY(),mPaint);
            }
            //正片叠底混合模式
            canvas.drawBitmap(mSrcBitmap, x, y, mPdXfPaint);
            mPdXfPaint.setXfermode(mPdXfermode);
            canvas.drawBitmap(mCurrentBitmap, x, y, mPdXfPaint);
            mPdXfPaint.setXfermode(null);
        } else {
            //只显示原始图片
            canvas.drawBitmap(mMultiplyBitmap, x, y, null);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                prepoint=new Pointw();
                prepoint.setX(event.getX());
                prepoint.setY(event.getY());
                Log.d("ff","flag="+flag);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mCurrentCanvas.drawLine(prepoint.getX(),prepoint.getY(),pointw.getX(),pointw.getY(),mPaint);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                pointw=new Pointw();
                pointw.setX(event.getX());
                pointw.setY(event.getY());
                return true;
        }
        return true;
    }
    public void setTransScale(float scale, float dx, float dy) {
        mScale = scale;
        mTransX = dx;
        mTransY = dy;
        Log.d("ss","scale="+mScale+"x="+mTransX+"y="+mTransY);
        if (!mIsSaveArg) {
            invalidate();
        }
        mIsSaveArg = false;
    }
    public void saveCurrentScale() {
        mCenterScale = mCenterScale * mScale;
        mCenterLeft = (mCenterLeft + mTransX) / mCenterScale;
        mCenterTop = (mCenterTop + mTransY) / mCenterScale;
        mIsSaveArg = true;
        saveMultiplyBitmap();
    }

    //双指移动的时候,生成混合之后的图片
    private void saveMultiplyBitmap() {
        mIsMove = true;
        Canvas canvas = new Canvas(mMultiplyBitmap);
        canvas.drawBitmap(mSrcBitmap, 0, 0, mPdXfPaint);
        mPdXfPaint.setXfermode(mPdXfermode);
        // 绘制涂鸦图片
        mPdXfPaint.setXfermode(null);
    }
    /**
     * 将触摸的屏幕坐标转换成实际图片中的坐标
     */
    public float screenToBitmapX(float touchX) {
        return (touchX - mCenterLeft - mTransX) / (mCenterScale * mScale);
    }
    public float screenToBitmapY(float touchY) {
        return (touchY - mCenterTop - mTransY) / (mCenterScale * mScale);
    }
    //通过触点的坐标和实际图片中的坐标,得到当前图片的起始点坐标
    public final float toTransX(float touchX, float graffitiX) {
        return -graffitiX * (mCenterScale * mScale) + touchX - mCenterLeft;
    }
    public final float toTransY(float touchY, float graffitiY) {
        return -graffitiY * (mCenterScale * mScale) + touchY - mCenterTop;
    }

    /**
     * 初始化当前画线的绘图
     */
    private void initCurrentCanvas() {
        mCurrentBitmap.setPixels(mWhiteBuffer, 0, mSrcBitmap.getWidth(), 0, 0, mSrcBitmap.getWidth(), mSrcBitmap.getHeight());
        mCurrentCanvas = new Canvas(mCurrentBitmap);
    }
    /**
     * 创建一个图片,透明度为255(不透明), 底色为白色 ,目的是为了使用正片叠底
     * @param sourceImg
     * @return
     */
    public Bitmap getTransparentBitmap(Bitmap sourceImg) {
        mWhiteBuffer = new int[sourceImg.getWidth() * sourceImg.getHeight()];
        Arrays.fill(mWhiteBuffer, 0xFFFFFFFF);
        sourceImg = Bitmap.createBitmap(mWhiteBuffer, sourceImg.getWidth(), sourceImg.getHeight(), Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true);
        return sourceImg;
    }
}
