package com.leory.transform.transform_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.leory.transform.R;

/**
 * @Description: 简单变换view
 * @Author: leory
 * @Time: 2021/4/30
 */
public class SimpleTransformView extends BaseCoreView {

    private float m_pendantCX;
    private float m_pendantCY;

    public SimpleTransformView(Context context) {
        this(context, null);
    }

    public SimpleTransformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleTransformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        setLeftTopBtn(R.mipmap.ic_pendant_del, action -> {
            if (action == MotionEvent.ACTION_UP) {
                deleteCurPendent();
            }
        });
        setRightBottomBtn(R.mipmap.ic_pendant_scale, action -> {
            if (action == MotionEvent.ACTION_DOWN) {
                m_pendantCX = m_pendant.m_centerX + m_pendant.m_x;
                m_pendantCY = m_pendant.m_centerY + m_pendant.m_y;
                Init_RZ_Data(m_pendant, m_pendantCX, m_pendantCY, m_downX, m_downY);
            } else if (action == MotionEvent.ACTION_MOVE) {
                Run_RZ(m_pendant, m_pendantCX, m_pendantCY, m_moveX, m_moveY);
            }
        });
    }


}
