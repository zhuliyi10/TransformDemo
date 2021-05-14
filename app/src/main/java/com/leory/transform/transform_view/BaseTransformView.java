package com.leory.transform.transform_view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;

/**
 * @Description: 处理元素变换移动、旋转、缩放
 * @Author: leory
 * @Time: 2021/4/30
 */
public abstract class BaseTransformView extends BaseTouchView {
    private static float SNAP_DEGREE;//吸附角度
    private static float SNAP_DISTANCE;//吸附距离
    public ShapeEx m_origin; //基础坐标系
    public ShapeEx m_viewport; //显示视图
    public ShapeEx m_target;//目标视图
    protected float m_old_mx;
    protected float m_old_my;
    protected float m_beta; //旋转
    protected float m_delta; //放大
    protected float m_oldDegree;
    protected float m_oldScaleX;
    protected float m_oldScaleY;
    protected float m_gammaX; //记录移动的初始坐标x
    protected float m_gammaY;//记录移动的初始坐标x

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
    protected void init() {
        super.init();
        SNAP_DEGREE = 3;
        SNAP_DISTANCE = dpToPx(5);
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
        m_old_mx = target.m_x;
        m_old_my = target.m_y;
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
            if (y2 > y1) {
                m_beta = 90;
            } else {
                m_beta = -90;
            }
        } else if (y1 - y2 != 0) {
            m_beta = (float) Math.toDegrees(Math.atan(((double) (y2 - y1)) / (x2 - x1)));
            if (x1 > x2) {
                m_beta += 180;
            }
        } else {
            if (x2 > x1) {
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
        m_delta = spacing(x1 - x2, y1 - y2);
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
        int img_centerX = (int) (m_viewport.m_x + m_viewport.m_centerX);
        int img_centerY = (int) (m_viewport.m_y + m_viewport.m_centerY);
        float mx = (x - m_gammaX) + m_old_mx;
        float my = (y - m_gammaY) + m_old_my;
        boolean isSnapX = false;
        boolean isSnapY = false;
        if (Math.abs(mx + target.m_centerX - img_centerX) < SNAP_DISTANCE) {
            float targetX = img_centerX - target.m_centerX;
            if (target.m_x != targetX) {
                target.m_x = targetX;
                vibrate();
            }
            isSnapX = true;
        }
        if (Math.abs(my + target.m_centerY - img_centerY) < SNAP_DISTANCE) {
            float targetY = img_centerY - target.m_centerY;
            if (target.m_y != targetY) {
                target.m_y = targetY;
                vibrate();
            }
            isSnapY = true;
        }
        if (!isSnapX) {
            target.m_x = mx;
        }
        if (!isSnapY) {
            target.m_y = my;
        }
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
            if (y2 > y1) {
                tempAngle = 90;
            } else {
                tempAngle = -90;
            }
        } else if (y1 - y2 != 0) {
            tempAngle = (float) Math.toDegrees(Math.atan(((double) (y2 - y1)) / (x2 - x1)));
            if (x1 > x2) {
                tempAngle += 180;
            }

        } else {
            if (x2 > x1) {
                tempAngle = 0;
            } else {
                tempAngle = 180;
            }
        }
        float degree = m_oldDegree + tempAngle - m_beta;
        float absDegree = degree % 360;
        if (absDegree < 0) {
            absDegree += 360;
        }
        if (absDegree < SNAP_DEGREE || 360 - absDegree < SNAP_DEGREE) {
            if (target.m_degree != 0) {
                vibrate();
            }
            target.m_degree = 0;
            return;
        }
        if (Math.abs(absDegree - 180) < SNAP_DEGREE) {
            if (Math.abs(target.m_degree) != 180) {
                vibrate();
            }
            target.m_degree = 180;
            return;
        }
        if (Math.abs(absDegree - 90) < SNAP_DEGREE) {
            if (Math.abs(target.m_degree) != 90) {
                vibrate();
            }
            target.m_degree = 90;
            return;
        }
        if (Math.abs(absDegree - 270) < SNAP_DEGREE) {
            if (Math.abs(target.m_degree) != 270) {
                vibrate();
            }
            target.m_degree = 270;
            return;
        }
        target.m_degree = degree;
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
        float tempDist = spacing(x1 - x2, y1 - y2);
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

    /**
     * dp转为px
     */
    protected int dpToPx(float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics()) + 0.5f);
    }

    /**
     * 获取2点间的距离
     *
     * @param dx
     * @param dy
     * @return
     */
    protected float spacing(float dx, float dy) {
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 触感震动
     */
    private void vibrate() {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

}
