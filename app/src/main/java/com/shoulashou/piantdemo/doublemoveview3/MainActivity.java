package com.shoulashou.piantdemo.doublemoveview3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.shoulashou.piantdemo.R;

public class MainActivity extends AppCompatActivity {

    private Bitmap mSrcBitmap;
    private FrameLayout mFrameLayout;
    private MyView1 mDoubleMoveView;
    private int mTouchMode;
    private PointF mTouchCenterPt;//两指中点坐标
    private float mOldDist, mNewDist;
    private float mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti;
    private Button button1,button2,button3,button4,button5,button6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initView();
    }

    private void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.act_main_mainlayout);
        button1=findViewById(R.id.fy);
        button2=findViewById(R.id.hzx);
        button3=findViewById(R.id.hy);
        button4=findViewById(R.id.rb);
        button5=findViewById(R.id.jx);
        button6=findViewById(R.id.ty);
        mSrcBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.demobg).copy(Bitmap.Config.ARGB_8888, true);
        mDoubleMoveView = new MyView1(MainActivity.this, mSrcBitmap);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mFrameLayout.addView(mDoubleMoveView, params);

        mDoubleMoveView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchMode = 1;
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mTouchMode = 0;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if (mTouchMode == 1){
                            return false;
                        }
                        if (mTouchMode == 2) {
                            PointF ptf = getMid(event);
                            mNewDist = spacing(event);// 两点按下时的距离
                            float sc = mNewDist / mOldDist;
                            float transX = mDoubleMoveView.toTransX(ptf.x, mToucheCentreXOnGraffiti);
                            float transY = mDoubleMoveView.toTransY(ptf.y, mToucheCentreYOnGraffiti);
                            mDoubleMoveView.setTransScale(sc, transX, transY);
                        }
                        return true;
                    case MotionEvent.ACTION_POINTER_UP:
                        mTouchMode -= 1;
                        if (mTouchMode == 1) {
                            //mDoubleMoveView.PointertUp();
                        }
                        return true;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mTouchMode += 1;
                        if (mTouchMode == 2) {
                            mTouchCenterPt = getMid(event);
                            mOldDist = spacing(event);// 两点按下时的距离
                            mToucheCentreXOnGraffiti = mDoubleMoveView.screenToBitmapX(mTouchCenterPt.x);
                            mToucheCentreYOnGraffiti = mDoubleMoveView.screenToBitmapY(mTouchCenterPt.y);
                            mDoubleMoveView.saveCurrentScale();
                        }
                        return true;
                }
                return false;
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoubleMoveView.flag=0;
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoubleMoveView.flag=1;
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoubleMoveView.flag=2;
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoubleMoveView.cx();
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoubleMoveView.flag=5;
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoubleMoveView.flag=6;
            }
        });
    }

    /**
     * 计算两指间的距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);

    }

    /*取两指的中心点坐标*/
    private PointF getMid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }

}
