package com.leory.transform.transform_view;

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
    protected float m_moveX;
    protected float m_moveY;

    protected float m_downX1;
    protected float m_downY1;
    protected float m_downX2;
    protected float m_downY2;

    protected float m_gammaX; //记录移动的初始坐标x
    protected float m_gammaY;//记录移动的初始坐标x

    private boolean m_uiEnabled;


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
                        m_moveX = event.getX();
                        m_moveY = event.getY();
                        oddMove(event);
                    }

                    break;
                }

                case MotionEvent.ACTION_DOWN: {
                    m_downX = event.getX();
                    m_downY = event.getY();
                    oddDown(event);
                    break;
                }

                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_UP: {
                    oddUp(event);
                    break;
                }

                case MotionEvent.ACTION_POINTER_DOWN: {
                    m_downX1 = event.getX(0);
                    m_downY1 = event.getY(0);
                    m_downX2 = event.getX(1);
                    m_downY2 = event.getY(1);
                    evenDown(event);
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
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
