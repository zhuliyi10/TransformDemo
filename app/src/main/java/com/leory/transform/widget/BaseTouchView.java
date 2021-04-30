package com.leory.transform.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @Description: 处理touch 事件
 * @Author: leory
 * @Time: 2021/4/30
 */
public abstract class BaseTouchView extends View implements Cloneable {
    protected float m_downX;
    protected float m_downY;

    protected float m_downX1;
    protected float m_downY1;
    protected float m_downX2;
    protected float m_downY2;

    protected float m_gammaX; //移动
    protected float m_gammaY;

    protected boolean m_uiEnabled;


    public BaseTouchView(Context context) {
        this(context, null);
    }

    public BaseTouchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public BaseTouchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    protected void init() {
        m_uiEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (m_uiEnabled) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE: {
                    if (event.getPointerCount() > 1) {
                        evenMove(event);
                    } else {
                        oddMove(event);
                    }

                    break;
                }

                case MotionEvent.ACTION_DOWN: {
                    //System.out.println("ACTION_DOWN");
                    //System.out.println(event.getX() + "-" + event.getY());
                    m_downX = event.getX();
                    m_downY = event.getY();
                    oddDown(event);
                    break;
                }

                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_UP: {
                    //System.out.println("ACTION_UP");
                    //System.out.println(event.getX() + "-" + event.getY());
                    oddUp(event);
                    break;
                }

                case MotionEvent.ACTION_POINTER_DOWN: {
                    //System.out.println("ACTION_POINTER_DOWN");
                    //System.out.println(event.getX(event.getActionIndex()) + "-" + event.getY(event.getActionIndex()));
                    m_downX1 = event.getX(0);
                    m_downY1 = event.getY(0);
                    m_downX2 = event.getX(1);
                    m_downY2 = event.getY(1);
                    evenDown(event);
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    //System.out.println("ACTION_POINTER_UP");
                    //System.out.println(event.getX(event.getActionIndex()) + "-" + event.getY(event.getActionIndex()));
                    evenUp(event);
                    break;
                }
            }
        }
        return true;
    }

    public void setUIEnabled(boolean state) {
        m_uiEnabled = state;
    }


    protected abstract void oddDown(MotionEvent event);

    protected abstract void oddMove(MotionEvent event);

    protected abstract void oddUp(MotionEvent event);

    protected abstract void evenDown(MotionEvent event);

    protected abstract void evenMove(MotionEvent event);

    protected abstract void evenUp(MotionEvent event);
}
