package com.shoulashou.piantdemo.doublemoveview3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: aaa
 * Date: 2016/12/5 17:31.
 */
public class DoubleMoveView3 extends View {
    private List<Pointw>list=new ArrayList<>();
    private List<Circle> circles=new ArrayList<>();
    private List<Line>lineList=new ArrayList<>();
    private List<Rect>rectList=new ArrayList<>();
    private Context mContext;
    float l;
    public int flag;
    private  int upflag=0;
    private Pointw pointw;
    private Pointw prepoint;
    private Bitmap mSrcBitmap;
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

    private Paint mPaint;
    private Bitmap mGraffitiBitmap; // 用绘制涂鸦的图片
    private Canvas mBitmapCanvas; // 用于绘制涂鸦的画布

    private Bitmap mCurrentBitmap; // 绘制当前线时用到的图片
    private Canvas mCurrentCanvas; // 当前绘制线的画布

    private int[] mWhiteBuffer;//保存白色图片内存，刷新时重新刷新图片
    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<MyDrawSinglePath> mPathStack = new CopyOnWriteArrayList<MyDrawSinglePath>();
    private CopyOnWriteArrayList<MyDrawSinglePath> pathStackBackup = new CopyOnWriteArrayList<MyDrawSinglePath>();
    private int mTouchMode; // 触摸模式，触点数量
    private float mTouchDownX, mTouchDownY, mLastTouchX, mLastTouchY, mTouchX, mTouchY;
    private MyDrawSinglePath mCurrPath; // 当前手写的路径

    private PorterDuffXfermode mPdXfermode; // 定义PorterDuffXfermode变量
    private Paint mPdXfPaint;// 绘图的混合模式
    private Paint mCurrentPaint;
    private Path mCanvasPath; //仅用于当前Path的绘制

    public DoubleMoveView3(Context context, Bitmap bitmap) {
        super(context);
        mContext = context;
        init(bitmap);
    }

    public DoubleMoveView3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init(Bitmap bitmap) {
        mSrcBitmap = bitmap;
        mBitmapWidth = mSrcBitmap.getWidth();
        mBitmapHeight = mSrcBitmap.getHeight();
        mMultiplyBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_8888);

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5);
        mGraffitiBitmap = getTransparentBitmap(mSrcBitmap);

        mCurrentPaint = new Paint();
        mCurrentPaint.setStyle(Paint.Style.STROKE);
        mCurrentPaint.setStrokeWidth(5);
        mCurrentPaint.setColor(Color.BLACK);
        mCurrentPaint.setAlpha(100);
        mCurrentPaint.setAntiAlias(true);
        mCurrentPaint.setStrokeJoin(Paint.Join.ROUND);
        mCurrentPaint.setStrokeCap(Paint.Cap.ROUND);
        mCurrentPaint.setXfermode(null);

        mCanvasPath = new Path();
        mCurrentBitmap = getTransparentBitmap(mSrcBitmap);

        //设置混合模式   （正片叠底）
        mPdXfermode = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);

        mPdXfPaint = new Paint();
        mPdXfPaint.setAntiAlias(true);
        mPdXfPaint.setFilterBitmap(true);

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

        initCanvas();
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
            //正片叠底混合模式
            initCurrentCanvas();
           if(pointw!=null){
               if(flag==1) {
                   mCurrentCanvas.drawLine(screenToBitmapX(prepoint.getX()), screenToBitmapY(prepoint.getY()), screenToBitmapX((mTouchX + mLastTouchX) / 2), screenToBitmapY((mTouchY + mLastTouchY) / 2), mCurrentPaint);
                   if(upflag==1) {
                       Line line = new Line();
                       Pointw p1 = new Pointw();
                       p1.setX(screenToBitmapX(prepoint.getX()));
                       p1.setY(screenToBitmapY(prepoint.getY()));
                       line.setP1(p1);
                       Pointw p2 = new Pointw();
                       p2.setX(screenToBitmapX((mTouchX + mLastTouchX) / 2));
                       p2.setY(screenToBitmapY((mTouchY + mLastTouchY) / 2));
                       line.setP2(p2);
                       lineList.add(line);
                   }
               }
               float xl=screenToBitmapX((mTouchX + mLastTouchX) / 2) - screenToBitmapX(prepoint.getX());
               float yl=screenToBitmapY((mTouchY + mLastTouchY) / 2) - screenToBitmapY(prepoint.getY());
               if(flag==2) {
                   Log.d("ss","flag="+flag);
                   l = (float) Math.sqrt(xl*xl+yl*yl);
                   Log.d("ss","x="+screenToBitmapX(prepoint.getX())+"y="+screenToBitmapY(prepoint.getY()));
                   mCurrentCanvas.drawCircle(screenToBitmapX(prepoint.getX()), screenToBitmapY(prepoint.getY()), l, mCurrentPaint);
                   if(upflag==1) {
                       Pointw p3 = new Pointw();
                       p3.setX(screenToBitmapX(prepoint.getX()));
                       p3.setY(screenToBitmapY(prepoint.getY()));
                       Circle circle = new Circle();
                       circle.setR(l);
                       circle.setPointw(p3);
                       circles.add(circle);
                   }
               }
           }

            //mCurrentCanvas.drawPath(mCanvasPath, mCurrentPaint);
            canvas.drawBitmap(mSrcBitmap, x, y, mPdXfPaint);
            mPdXfPaint.setXfermode(mPdXfermode);
            canvas.drawBitmap(mCurrentBitmap, x, y, mPdXfPaint);
            // 绘制涂鸦图片
            canvas.drawBitmap(mGraffitiBitmap, x, y, mPdXfPaint);
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
                upflag=0;
                prepoint=new Pointw();
                prepoint.setX(event.getX());
                prepoint.setY(event.getY());
                Log.i("aaa", "ACTION_DOWN");
                mTouchMode = 1;
                penTouchDown(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.i("aaa", "ACTION_UP");
                upflag=1;
                mTouchMode = 0;
                mCanvasPath.reset();
                if(circles.size()>0){
                    for(int i=0;i<circles.size();i++){
                        mBitmapCanvas.drawCircle(circles.get(i).pointw.getX(),circles.get(i).pointw.getY(),circles.get(i).getR(),mCurrentPaint);
                    }
                }
                list.clear();
                initCanvas();//添上这句防止重复绘制
                draw(mBitmapCanvas, mPathStack); // 保存到图片中
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                Log.i("aaa", "ACTION_MOVE");
                if (mTouchMode == 1 && !mIsMove) {
                    mLastTouchX = mTouchX;
                    mLastTouchY = mTouchY;
                    mTouchX = event.getX();
                    mTouchY = event.getY();
                    Pointw point=new Pointw();
                    point.setX(mTouchX);
                    point.setY(mTouchY);
                    pointw=new Pointw();
                    pointw.setY(mTouchY);
                    pointw.setX(mTouchX);
                    list.add(point);
                    mCurrPath.getPath().quadTo(screenToBitmapX(mLastTouchX), screenToBitmapY(mLastTouchY),
                            screenToBitmapX((mTouchX + mLastTouchX) / 2), screenToBitmapY((mTouchY + mLastTouchY) / 2));

                    mCanvasPath.quadTo(screenToBitmapX(mLastTouchX), screenToBitmapY(mLastTouchY),
                            screenToBitmapX((mTouchX + mLastTouchX) / 2), screenToBitmapY((mTouchY + mLastTouchY) / 2));
                    invalidate();
                }
                return true;
        }
        return true;
    }

    public void setTransScale(float scale, float dx, float dy) {
        mScale = scale;
        mTransX = dx;
        mTransY = dy;
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
        canvas.drawBitmap(mGraffitiBitmap, 0, 0, mPdXfPaint);
        mPdXfPaint.setXfermode(null);
    }

    /**
     * 在画笔的状态下第一个触点按下的情况
     */
    private void penTouchDown(float x, float y) {
        mIsMove = false;
        mTouchDownX = mTouchX = mLastTouchX = x;
        mTouchDownY = mTouchY = mLastTouchY = y;

        // 为了仅点击时也能出现绘图，模拟滑动一个像素点
        mTouchX++;
        mTouchY++;

        mCurrPath = new MyDrawSinglePath(Color.BLACK, 5, 100, true);
        mCurrPath.getPath().moveTo(screenToBitmapX(mTouchDownX), screenToBitmapY(mTouchDownY));
        mPathStack.add(mCurrPath);

        mCanvasPath.reset();
        mCanvasPath.moveTo(screenToBitmapX(mTouchDownX), screenToBitmapY(mTouchDownY));
        // 为了仅点击时也能出现绘图，必须移动path
        mCanvasPath.quadTo(screenToBitmapX(mLastTouchX), screenToBitmapY(mLastTouchY),
                screenToBitmapX((mTouchX + mLastTouchX) / 2), screenToBitmapY((mTouchY + mLastTouchY) / 2));
    }

    /**
     * 初始化当前画线的绘图
     */
    private void initCurrentCanvas() {
        mCurrentBitmap.setPixels(mWhiteBuffer, 0, mSrcBitmap.getWidth(), 0, 0, mSrcBitmap.getWidth(), mSrcBitmap.getHeight());
        mCurrentCanvas = new Canvas(mCurrentBitmap);
    }

    /**
     * 初始化涂鸦的绘图
     */
    private void initCanvas() {
        mGraffitiBitmap.setPixels(mWhiteBuffer, 0, mSrcBitmap.getWidth(), 0, 0, mSrcBitmap.getWidth(), mSrcBitmap.getHeight());
        mBitmapCanvas = new Canvas(mGraffitiBitmap);
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

    private void draw(Canvas canvas, CopyOnWriteArrayList<MyDrawSinglePath> pathStack) {
         //canvas.drawCircle(prepoint.getX(),prepoint.getY(),l,mCurrentPaint);
        // 还原堆栈中的记录的操作
        for (MyDrawSinglePath path : pathStack) {
           // canvas.drawPath(path.getPath(), path.getMyPen().getPenPaint());
        }
        for(Line line:lineList){
            canvas.drawLine(line.getP1().getX(),line.getP1().getY(),line.getP2().getX(),line.getP2().getY(),mCurrentPaint);
        }
        for(Circle circle:circles){
            canvas.drawCircle(circle.getPointw().getX(),circle.getPointw().getY(),circle.getR(),mCurrentPaint);
        }
    }

    //双指抬起时
    public void PointertUp() {

        if (!mCanvasPath.isEmpty()) {//单指画线过程中，出现双触点则停止画线
            mCanvasPath.reset();
            if (!mPathStack.isEmpty()) {
                mPathStack.remove(mPathStack.size() - 1);
            }
        }
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
}
