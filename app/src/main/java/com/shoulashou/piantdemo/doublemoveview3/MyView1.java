package com.shoulashou.piantdemo.doublemoveview3;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.lang.Math.sqrt;

/**
 * Created by likeye on 2020/7/28 17:12.
 **/
public class MyView1 extends View {
    private List<Pointw>list=new ArrayList<>();
    private List<Circle> circles=new ArrayList<>();
    private List<Line>lineList=new ArrayList<>();
    private List<Rect>rectList=new ArrayList<>();
    private List<Oval> ovalList=new ArrayList<>();
    private Context mContext;
    private boolean choose=false;
    private boolean move=false;
    private boolean clip2=false;
    float l;
    public int flag;
    private int chooseLine;
    private boolean isUp=false;
    private  int upflag=0;
    private Pointw pointw,p1,p2,pp1,pp2,pp3;
    private Pointw prepoint,pointwl;
    private RectF rectF;
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

    private int mTouchMode; // 触摸模式，触点数量
    private float mTouchDownX, mTouchDownY, mLastTouchX, mLastTouchY, mTouchX, mTouchY;

    private PorterDuffXfermode mPdXfermode; // 定义PorterDuffXfermode变量
    private Paint mPdXfPaint;// 绘图的混合模式
    private Paint mCurrentPaint;

    public MyView1(Context context, Bitmap bitmap) {
        super(context);
        mContext = context;
        init(bitmap);
    }
    public MyView1(Context context, AttributeSet attrs) {
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
        //画图中的画笔
        mCurrentPaint = new Paint();
        mCurrentPaint.setStyle(Paint.Style.STROKE);
        mCurrentPaint.setStrokeWidth(5);
        mCurrentPaint.setColor(Color.BLACK);
        mCurrentPaint.setAlpha(100);
        mCurrentPaint.setAntiAlias(true);
        mCurrentPaint.setStrokeJoin(Paint.Join.ROUND);
        mCurrentPaint.setStrokeCap(Paint.Cap.ROUND);
        mCurrentPaint.setXfermode(null);

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
                    pointw.setX(screenToBitmapX((mTouchX + mLastTouchX) / 2));
                    pointw.setY(screenToBitmapY((mTouchY + mLastTouchY) / 2));
                    mCurrentCanvas.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), mCurrentPaint);
                   // mCurrentCanvas.drawLine(p1.getX(),p1.getY(),p2.getX(),p2.getY(),mCurrentPaint);
                }
                float xl=p2.getX() - p1.getX();
                float yl=p2.getY() - p1.getY();
                if(flag==2) {
                    l = (float) sqrt(xl*xl+yl*yl);
                    mCurrentCanvas.drawCircle(p1.getX(), p1.getY(), l, mCurrentPaint);
                }
                if(flag==0&&choose&&move){
                    float xx=p2.getX()-p1.getX();
                    float yy=p2.getY()-p1.getY();
                    pp1=new Pointw();
                    pp1.setX(lineList.get(chooseLine-1).getP1().getX()+xx);
                    pp1.setY(lineList.get(chooseLine-1).getP1().getY()+yy);
                    pp2=new Pointw();
                    pp2.setX(lineList.get(chooseLine-1).getP2().getX()+xx);
                    pp2.setY(lineList.get(chooseLine-1).getP2().getY()+yy);
                    pp3=new Pointw();
                    pp3.setX(pp1.getX()+(pp2.getX()-pp1.getX())/2);
                    pp3.setY(pp1.getY()+(pp2.getY()-pp1.getY())/2);
                    pointw.setX(screenToBitmapX((mTouchX + mLastTouchX) / 2));
                    pointw.setY(screenToBitmapY((mTouchY + mLastTouchY) / 2));
                    mCurrentCanvas.drawLine(pp1.getX(), pp1.getY(), pp2.getX(), pp2.getY(), mCurrentPaint);
                    Paint paint=new Paint();
                    paint.setColor(Color.RED);
                    paint.setStrokeWidth(10);
                    mCurrentCanvas.drawCircle(pp1.getX(),pp1.getY(),10,paint);
                    mCurrentCanvas.drawCircle(pp2.getX(),pp2.getY(),10,paint);
                    mCurrentCanvas.drawCircle(pp3.getX(),pp3.getY(),10,paint);
                   // mCurrentCanvas.drawLine(pp3.getX(), pp3.getY(), 0, 0, paint);

                }
                if(flag==0&&choose&&clip2){
                    pp1=new Pointw();
                    pp1.setX(lineList.get(chooseLine-1).getP1().getX());
                    pp1.setY(lineList.get(chooseLine-1).getP1().getY());
                    mCurrentCanvas.drawLine(pp1.getX(), pp1.getY(), pointwl.getX(), pointwl.getY(), mCurrentPaint);
                }
                if(flag==5){
                    mCurrentCanvas.drawRect(p1.getX(), p1.getY(), p2.getX(), p2.getY(), mCurrentPaint);
                }
                if(flag==6){

                    mCurrentCanvas.drawOval(rectF, mCurrentPaint);
                }
            }
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
    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(flag==0) {
                    isUp=false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isUp) {
                               // if(!choose) {
                                    Pointw pp = new Pointw();
                                    pp.setY(event.getY());
                                    pp.setX(event.getX());
                                    longclick(pp);
                               // }
                                choose=true;
                                return;
                            }
                        }
                    }, 500);
                    if(choose) {
                       // Log.d("ss","x1="+event.getX()+"x2="+lineList.get(chooseLine - 1).getPmid().getX()+"y1="+event.getY()+"y2="+lineList.get(chooseLine - 1).getPmid().getY());
                        //Log.d("ss","y3="+lineList.get(chooseLine - 1).getP1().getY()+"y4="+lineList.get(chooseLine - 1).getP2().getY());
                        double d = lineSpace(screenToBitmapX(event.getX()), screenToBitmapY(event.getY()), lineList.get(chooseLine - 1).getPmid().getX(), lineList.get(chooseLine - 1).getPmid().getY());
                        double dp2 = lineSpace(screenToBitmapX(event.getX()), screenToBitmapY(event.getY()), lineList.get(chooseLine - 1).getP2().getX(), lineList.get(chooseLine - 1).getP2().getY());

                        Log.d("ss","d="+d);
                        if (d < 150) {
                            move = true;
                            Log.d("ss","cc1="+move);
                        }
                        if (dp2 < 150) {
                            pointwl=new Pointw();
                            pointwl.setX(screenToBitmapX(event.getX()));
                            pointwl.setY(screenToBitmapY(event.getY()));
                            clip2 = true;
                            Log.d("ss","cc1="+move);
                        }
                    }
                }
                p1=new Pointw();
                p2=new Pointw();
                upflag=0;
                prepoint=new Pointw();
                prepoint.setX(event.getX());
                prepoint.setY(event.getY());
                //Log.i("aaa", "ACTION_DOWN");
                mTouchMode = 1;
                penTouchDown(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_UP:

                isUp=true;
                upflag=1;
                if(upflag==1) {
                    if(flag==1) {
                        Line line = new Line();
                        Pointw pp1 = new Pointw();
                        pp1=p1;
                        // pp1.setX(screenToBitmapX(prepoint.getX()));
                        // pp1.setY(screenToBitmapY(prepoint.getY()));
                        line.setP1(pp1);
                        Pointw pp2 = new Pointw();
                        pp2=p2;
                        // p2.setX(screenToBitmapX((mTouchX + mLastTouchX) / 2));
                        // p2.setY(screenToBitmapY((mTouchY + mLastTouchY) / 2));
                        line.setP2(pp2);
                        Pointw pp3 = new Pointw();
                        pp3.setX(pp1.getX()+(pp2.getX()-pp1.getX())/2);
                        pp3.setY(pp1.getY()+(pp2.getY()-pp1.getY())/2);
                        line.setPmid(pp3);
                        lineList.add(line);
                    }
                    if(flag==2) {
                        Pointw p3 = new Pointw();
                        p3.setX(screenToBitmapX(prepoint.getX()));
                        p3.setY(screenToBitmapY(prepoint.getY()));
                        Circle circle = new Circle();
                        circle.setR(l);
                        circle.setPointw(p3);
                        circles.add(circle);
                    }
                    if (flag==0&&choose&&move){
                        lineList.get(chooseLine-1).setP1(pp1);
                        lineList.get(chooseLine-1).setP2(pp2);
                        Pointw pp3 = new Pointw();
                        pp3.setX(pp1.getX()+(pp2.getX()-pp1.getX())/2);
                        pp3.setY(pp1.getY()+(pp2.getY()-pp1.getY())/2);
                        lineList.get(chooseLine-1).setPmid(pp3);
                    }

                    if(flag==5) {
                        Rect rect = new Rect();
                        rect.setP1(p1);
                        rect.setP2(p2);
                        rectList.add(rect);
                    }
                    if(flag==6){
                        Oval oval=new Oval();
                        oval.setRectF(rectF);
                        ovalList.add(oval);
                    }
                }
                move=false;
                clip2=false;
                p1=new Pointw();
                p2=new Pointw();
                rectF=new RectF();
                mTouchMode = 0;

                list.clear();
                initCanvas();//添上这句防止重复绘制
                draw1(mBitmapCanvas); // 保存到图片中
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                //Log.i("aaa", "ACTION_MOVE");
                if (mTouchMode == 1 && !mIsMove) {
                    pp3=new Pointw();
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
                    pointwl=new Pointw();
                    pointwl.setX(screenToBitmapX(event.getX()));
                    pointwl.setY(screenToBitmapY(event.getY()));
                    list.add(point);
                    p1.setX(screenToBitmapX(prepoint.getX()));
                    p1.setY(screenToBitmapY(prepoint.getY()));
                    p2.setX(screenToBitmapX((mTouchX + mLastTouchX) / 2));
                    p2.setY(screenToBitmapY((mTouchY + mLastTouchY) / 2));
                    rectF=new RectF();
                    rectF.set(p1.getX(),p1.getY(), p2.getX(),p2.getY());
                    invalidate();
                }
                return true;
        }
        return true;
    }
    //缩放
    public void setTransScale(float scale, float dx, float dy) {
        mScale = scale;
        mTransX = dx;
        mTransY = dy;
        if (!mIsSaveArg) {
            invalidate();
        }
        mIsSaveArg = false;
    }
    //长按选中
    public void longclick(Pointw p){
        float x0=p.getX();
        float y0=p.getY();
        double min=1000;
        int k=0;
        for(Line line:lineList){
            k++;
            float x1=line.getP1().getX();
            float y1=line.getP1().getY();
            float x2=line.getP2().getX();
            float y2=line.getP2().getY();
            double d=pointToLine(x1,y1,x2,y2,x0,y0);
            if(d<min){
                min=d;
                chooseLine=k;
            }
        }
    }
    // 点到直线的最短距离的判断 点（x0,y0） 到由两点组成的线段（x1,y1） ,( x2,y2 )
    private double pointToLine(float x1, float y1, float x2, float y2, float x0,
                               float y0) {
        double space = 0;
        double a, b, c;
        a = lineSpace(x1, y1, x2, y2);// 线段的长度
        b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离
        c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离
        if (c <= 0.000001 || b <= 0.000001) {
            space = 0;
            return space;
        }
        if (a <= 0.000001) {
            space = b;
            return space;
        }
        if (c * c >= a * a + b * b) {
            space = b;
            return space;
        }
        if (b * b >= a * a + c * c) {
            space = c;
            return space;
        }
        double p = (a + b + c) / 2;// 半周长
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
        space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
        return space;
    }

    // 计算两点之间的距离
    private double lineSpace(float x1, float y1, float x2, float y2) {
        double lineLength = 0;
        lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)
                * (y1 - y2));
        return lineLength;
    }
    //保存当前缩放的倍数
    public void saveCurrentScale() {
        mCenterScale = mCenterScale * mScale;
        mCenterLeft = (mCenterLeft + mTransX) / mCenterScale;
        mCenterTop = (mCenterTop + mTransY) / mCenterScale;
        mIsSaveArg = true;
        saveMultiplyBitmap();
    }
    public void cx(){
        //invalidate();
        if(lineList.size()>0) {
            lineList.remove(lineList.size() - 1);
            initCanvas();//添上这句防止重复绘制
            draw1(mBitmapCanvas); // 保存到图片中
            invalidate();
        }
        //for(Line line:lineList){
           // mBitmapCanvas.drawLine(line.getP1().getX(),line.getP1().getY(),line.getP2().getX(),line.getP2().getY(),mCurrentPaint);
       // }
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

    private void draw1(Canvas canvas) {
        int k=0;
        // 还原堆栈中的记录的操作
        for(Line line:lineList){
            k++;
            canvas.drawLine(line.getP1().getX(),line.getP1().getY(),line.getP2().getX(),line.getP2().getY(),mCurrentPaint);
        }
        for(Circle circle:circles){
            canvas.drawCircle(circle.getPointw().getX(),circle.getPointw().getY(),circle.getR(),mCurrentPaint);
        }
        for(Rect rect:rectList){
            canvas.drawRect(rect.getP1().getX(),rect.getP1().getY(),rect.getP2().getX(),rect.getP2().getY(),mCurrentPaint);
        }
        for(Oval oval:ovalList){
            canvas.drawOval(oval.getRectF(),mCurrentPaint);
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

