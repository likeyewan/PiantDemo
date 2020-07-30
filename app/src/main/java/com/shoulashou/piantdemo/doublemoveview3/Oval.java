package com.shoulashou.piantdemo.doublemoveview3;

import android.graphics.RectF;

/**
 * Created by likeye on 2020/7/29 17:33.
 **/
public class Oval  {
    RectF rectF=new RectF();
    Pointw p1=new Pointw();
    Pointw p2=new Pointw();
    Pointw p3=new Pointw();
    Pointw p4=new Pointw();
    public RectF getRectF() {
        return rectF;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public Pointw getP1() {
        p1.setX(rectF.left);
        p1.setY(rectF.bottom+(rectF.top-rectF.bottom)/2);
        return p1;
    }
    public Pointw getP2() {
        p2.setX(rectF.left+(rectF.right-rectF.left)/2);
        p2.setY(rectF.top);
        return p2;
    }
    public Pointw getP3() {
        p3.setX(rectF.right);
        p3.setY(rectF.bottom+(rectF.top-rectF.bottom)/2);
        return p3;
    }
    public Pointw getP4() {
        p4.setX(rectF.left+(rectF.right-rectF.left)/2);
        p4.setY(rectF.bottom);
        return p4;
    }
}
