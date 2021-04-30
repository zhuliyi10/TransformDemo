package com.leory.transform.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * @Description: 处理元素变换移动、旋转、放大
 * @Author: leory
 * @Time: 2021/4/30
 */
public abstract class BaseTransformView extends BaseTouchView {
    public ShapeEx m_origin; //基础坐标系
    protected float m_oldX;
    protected float m_oldY;
    protected float m_beta; //旋转
    protected float m_delta; //放大
    protected float m_oldDegree;
    protected float m_oldScaleX;
    protected float m_oldScaleY;

    public BaseTransformView(Context context) {
        this(context, null);
    }

    public BaseTransformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseTransformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        updateOrigin(width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 更新控件大小
     *
     * @param width
     * @param height
     */
    private void updateOrigin(int width, int height) {
        if (m_origin == null) {
            m_origin = new ShapeEx();
        }
        m_origin.m_w = width;
        m_origin.m_h = height;
        m_origin.m_centerX = m_origin.m_w / 2f;
        m_origin.m_centerY = m_origin.m_h / 2f;
    }


    /**
     * 初始化移动
     *
     * @param x
     * @param y
     */
    protected void Init_M_Data(ShapeEx target, float x, float y) {
        m_gammaX = x;
        m_gammaY = y;
        m_oldX = target.m_x;
        m_oldY = target.m_y;
    }

    /**
     * 初始化旋转
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Init_R_Data(ShapeEx target, float x1, float y1, float x2, float y2) {
        if (x1 - x2 == 0) {
            if (y1 >= y2) {
                m_beta = 90;
            } else {
                m_beta = -90;
            }
        } else if (y1 - y2 != 0) {
            m_beta = (float) Math.toDegrees(Math.atan(((double) (y1 - y2)) / (x1 - x2)));
            if (x1 < x2) {
                m_beta += 180;
            }
        } else {
            if (x1 >= x2) {
                m_beta = 0;
            } else {
                m_beta = 180;
            }
        }
        m_oldDegree = target.m_degree;
    }

    /**
     * 初始化缩放
     * (引起误差的原因是按钮不是在交点的上,放大的时候空隙也放大了)
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Init_Z_Data(ShapeEx target, float x1, float y1, float x2, float y2) {
        m_delta = ImageUtils.Spacing(x1 - x2, y1 - y2);
        m_oldScaleX = target.m_scaleX;
        m_oldScaleY = target.m_scaleY;
    }

    /**
     * 初始化旋转放大
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Init_RZ_Data(ShapeEx target, float x1, float y1, float x2, float y2) {
        Init_R_Data(target, x1, y1, x2, y2);
        Init_Z_Data(target, x1, y1, x2, y2);
    }

    /**
     * 初始化放大移动
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Init_ZM_Data(ShapeEx target, float x1, float y1, float x2, float y2) {
        Init_M_Data(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
        Init_Z_Data(target, x1, y1, x2, y2);
    }

    /**
     * 初始化移动缩放
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Init_MZ_Data(ShapeEx target, float x1, float y1, float x2, float y2) {
        Init_M_Data(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
        Init_Z_Data(target, x1, y1, x2, y2);
    }

    /**
     * 初始化移动旋转放大
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Init_MRZ_Data(ShapeEx target, float x1, float y1, float x2, float y2) {
        Init_M_Data(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
        Init_R_Data(target, x1, y1, x2, y2);
        Init_Z_Data(target, x1, y1, x2, y2);
    }

    /**
     * 子元件移动
     *
     * @param x
     * @param y
     */
    protected void Run_M(ShapeEx target, float x, float y) {
        target.m_x = (x - m_gammaX) + m_oldX;
        target.m_y = (y - m_gammaY) + m_oldY;
    }

    /**
     * 旋转
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Run_R(ShapeEx target, float x1, float y1, float x2, float y2) {
        float tempAngle;
        if (x1 - x2 == 0) {
            if (y1 >= y2) {
                tempAngle = 90;
            } else {
                tempAngle = -90;
            }
        } else if (y1 - y2 != 0) {
            tempAngle = (float) Math.toDegrees(Math.atan(((double) (y1 - y2)) / (x1 - x2)));
            if (x1 < x2) {
                tempAngle += 180;
            }
        } else {
            if (x1 >= x2) {
                tempAngle = 0;
            } else {
                tempAngle = 180;
            }
        }
        target.m_degree = m_oldDegree + tempAngle - m_beta;
    }

    /**
     * 缩放
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Run_Z(ShapeEx target, float x1, float y1, float x2, float y2) {
        float tempDist = ImageUtils.Spacing(x1 - x2, y1 - y2);
        if (tempDist > 10) {
            float scale = tempDist / m_delta;
            float scaleX = m_oldScaleX * scale;
            float scaleY = m_oldScaleY * scale;
            if (scaleX > target.MAX_SCALE) {
                scaleX = target.MAX_SCALE;
                scaleY = scaleX / m_oldScaleX * m_oldScaleY;
            }
            if (scaleY > target.MAX_SCALE) {
                scaleY = target.MAX_SCALE;
                scaleX = scaleY / m_oldScaleY * m_oldScaleX;
            }
            if (scaleX < target.MIN_SCALE) {
                scaleX = target.MIN_SCALE;
                scaleY = scaleX / m_oldScaleX * m_oldScaleY;
            }
            if (scaleY < target.MIN_SCALE) {
                scaleY = target.MIN_SCALE;
                scaleX = scaleY / m_oldScaleY * m_oldScaleX;
            }
            target.SetScaleXY(scaleX, scaleY);
        }
    }

    /**
     * 旋转放大
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Run_RZ(ShapeEx target, float x1, float y1, float x2, float y2) {
        Run_R(target, x1, y1, x2, y2);
        Run_Z(target, x1, y1, x2, y2);
    }

    /**
     * 子元件的移动缩放
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Run_MZ(ShapeEx target, float x1, float y1, float x2, float y2) {
        Run_M(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
        Run_Z(target, x1, y1, x2, y2);
    }

    /**
     * 子元件的移动旋转放大
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Run_MRZ(ShapeEx target, float x1, float y1, float x2, float y2) {
        Run_M(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
        Run_R(target, x1, y1, x2, y2);
        Run_Z(target, x1, y1, x2, y2);
    }


    protected void GetShowMatrix(Matrix matrix, ShapeEx item) {
        float[] dst = {item.m_x + item.m_centerX, item.m_y + item.m_centerY};
        matrix.reset();
        if (item.m_flip == Shape.Flip.VERTICAL) {
            float[] values = {1, 0, 0, 0, -1, item.m_h, 0, 0, 1};
            matrix.setValues(values);
        } else if (item.m_flip == Shape.Flip.HORIZONTAL) {
            float[] values = {-1, 0, item.m_w, 0, 1, 0, 0, 0, 1};
            matrix.setValues(values);
        }

        matrix.postTranslate(dst[0] - item.m_centerX, dst[1] - item.m_centerY);
        matrix.postScale(item.m_scaleX, item.m_scaleY, dst[0], dst[1]);
        matrix.postRotate(item.m_degree, dst[0], dst[1]);
    }

    protected int GetSelectIndex(ArrayList<? extends ShapeEx> arr, float x, float y) {
        int index = -1;

        ShapeEx item;
        float[] values = new float[9];

        int len = arr.size();
        Matrix matrix = new Matrix();
        for (int i = len - 1; i >= 0; i--) {
            item = arr.get(i);
            GetShowMatrix(matrix, item);
            matrix.getValues(values);

            if (ProcessorV2.IsSelectTarget(values, item.m_w, item.m_h, x, y)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
