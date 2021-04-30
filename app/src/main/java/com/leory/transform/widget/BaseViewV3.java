package com.leory.transform.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public abstract class BaseViewV3 extends View implements Cloneable {
    protected float m_downX;
    protected float m_downY;

    protected float m_downX1;
    protected float m_downY1;
    protected float m_downX2;
    protected float m_downY2;

    protected float m_gammaX; //移动
    protected float m_gammaY;
    protected float m_delta; //放大
    protected float m_beta; //旋转

    protected float m_oldX;
    protected float m_oldY;
    protected float m_oldScaleX;
    protected float m_oldScaleY;
    protected float m_oldDegree;

    protected boolean m_uiEnabled;
    protected float[] temp_dst2 = {0, 0};
    protected float[] temp_src2 = {0, 0};
    protected Matrix temp_old_matrix;

    public BaseViewV3(Context context) {
        super(context);

        Init();
    }

    public BaseViewV3(Context context, AttributeSet attrs) {
        super(context, attrs);

        Init();
    }

    public BaseViewV3(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Init();
    }

    protected void Init() {
        m_uiEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (m_uiEnabled) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE: {
                    if (event.getPointerCount() > 1) {
                        EvenMove(event);
                    } else {
                        OddMove(event);
                    }

                    break;
                }

                case MotionEvent.ACTION_DOWN: {
                    //System.out.println("ACTION_DOWN");
                    //System.out.println(event.getX() + "-" + event.getY());
                    m_downX = event.getX();
                    m_downY = event.getY();
                    OddDown(event);
                    break;
                }

                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_UP: {
                    //System.out.println("ACTION_UP");
                    //System.out.println(event.getX() + "-" + event.getY());
                    OddUp(event);
                    break;
                }

                case MotionEvent.ACTION_POINTER_DOWN: {
                    //System.out.println("ACTION_POINTER_DOWN");
                    //System.out.println(event.getX(event.getActionIndex()) + "-" + event.getY(event.getActionIndex()));
                    m_downX1 = event.getX(0);
                    m_downY1 = event.getY(0);
                    m_downX2 = event.getX(1);
                    m_downY2 = event.getY(1);
                    EvenDown(event);
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    //System.out.println("ACTION_POINTER_UP");
                    //System.out.println(event.getX(event.getActionIndex()) + "-" + event.getY(event.getActionIndex()));
                    EvenUp(event);
                    break;
                }
            }
        }
        return true;
    }

    public void SetUIEnabled(boolean state) {
        m_uiEnabled = state;
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
     * 初始化非等比例缩放
     *
     * @param x1
     * @param y1
     */
    protected void Init_NZ_Data(ShapeEx target, float x1, float y1) {
        Matrix matrix = new Matrix(); //不能包含反转参数
        matrix.postTranslate(target.m_x, target.m_y);
        matrix.postScale(target.m_scaleX, target.m_scaleY, target.m_x + target.m_centerX, target.m_y + target.m_centerY);
        matrix.postRotate(target.m_degree, target.m_x + target.m_centerX, target.m_y + target.m_centerY);
        temp_src2[0] = target.m_w;
        temp_src2[1] = 0;
        matrix.mapPoints(temp_dst2, temp_src2);

        m_gammaX = temp_dst2[0] - x1; //偏移量
        m_gammaY = temp_dst2[1] - y1;
        if (temp_old_matrix == null) {
            temp_old_matrix = new Matrix();
        }
        temp_old_matrix.reset();
        matrix.invert(temp_old_matrix);
        temp_old_matrix.postScale(target.m_scaleX, target.m_scaleY, target.m_centerX, target.m_centerY);
        m_oldScaleX = target.m_scaleX;
        m_oldScaleY = target.m_scaleY;
    }

    /**
     * 非等比例缩放
     *
     * @param x
     * @param y
     */
    protected void Run_NZ(ShapeEx target, float x, float y) {
        temp_src2[0] = x + m_gammaX;
        temp_src2[1] = y + m_gammaY;
        temp_old_matrix.mapPoints(temp_dst2, temp_src2);
        if (temp_dst2[0] < target.m_centerX) {
            temp_dst2[0] = target.m_centerX;
        }
        if (temp_dst2[1] > target.m_centerY) {
            temp_dst2[1] = target.m_centerY;
        }

        target.SetScaleXY((temp_dst2[0] - target.m_centerX) * 2f / (float) target.m_w, (target.m_centerY - temp_dst2[1]) * 2f / (float) target.m_h);
    }

    /**
     * 初始化非中心点等比例缩放移动
     * (适用于基础坐标系,不支持旋转)
     *
     * @param target
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Init_NCZM_Data(ShapeEx target, float x1, float y1, float x2, float y2) {
        Init_M_Data(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
        Init_Z_Data(target, x1, y1, x2, y2);
    }

    /**
     * 非中心点等比例缩放移动
     * (适用于基础坐标系,不支持旋转)
     *
     * @param target
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Run_NCZM(ShapeEx target, float x1, float y1, float x2, float y2) {
        float tempDist = ImageUtils.Spacing(x1 - x2, y1 - y2);
        float offsetX = 0;
        float offsetY = 0;
        if (tempDist > 10) {
            float scale = tempDist / m_delta;
            float scaleX = m_oldScaleX * scale;
            float scaleY = m_oldScaleY * scale;
            //判断是否超出限制
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

            //计算平移
            if (m_oldScaleX != 0) {
                scale = scaleX / m_oldScaleX;
            } else if (m_oldScaleY != 0) {
                scale = scaleY / m_oldScaleY;
            }
            offsetX = (m_oldX + target.m_centerX - m_gammaX) * (scale - 1f);
            offsetY = (m_oldY + target.m_centerY - m_gammaY) * (scale - 1f);
        }

        target.m_x = (x1 + x2) / 2f - m_gammaX + m_oldX + offsetX;
        target.m_y = (y1 + y2) / 2f - m_gammaY + m_oldY + offsetY;
    }

    /**
     * @param matrix in out
     * @param item   in
     */
    protected void GetShowMatrix(Matrix matrix, ShapeEx item) {
        matrix.reset();
        if (item.m_flip == Shape.Flip.VERTICAL) {
            float[] values = {1, 0, 0, 0, -1, item.m_h, 0, 0, 1};
            matrix.setValues(values);
        } else if (item.m_flip == Shape.Flip.HORIZONTAL) {
            float[] values = {-1, 0, item.m_w, 0, 1, 0, 0, 0, 1};
            matrix.setValues(values);
        }

        matrix.postTranslate(item.m_x, item.m_y);
        matrix.postScale(item.m_scaleX, item.m_scaleY, item.m_x + item.m_centerX, item.m_y + item.m_centerY);
        matrix.postRotate(item.m_degree, item.m_x + item.m_centerX, item.m_y + item.m_centerY);
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

    protected abstract void OddDown(MotionEvent event);

    protected abstract void OddMove(MotionEvent event);

    protected abstract void OddUp(MotionEvent event);

    protected abstract void EvenDown(MotionEvent event);

    protected abstract void EvenMove(MotionEvent event);

    protected abstract void EvenUp(MotionEvent event);
}
