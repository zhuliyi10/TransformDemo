package com.leory.transform.widget;

import android.content.Context;
import android.graphics.Matrix;

public abstract class RelativeView extends BaseViewV3 {
    public ShapeEx m_origin; //基础坐标系
    public ShapeEx m_viewport; //视图位置

    protected ShapeEx m_target;

    public RelativeView(Context context, int frW, int frH) {
        super(context);

        Init(frW, frH);
    }

    protected void Init(int frW, int frH) {
        m_origin = new ShapeEx();
        m_origin.m_w = frW;
        m_origin.m_h = frH;
        m_origin.m_centerX = m_origin.m_w / 2f;
        m_origin.m_centerY = m_origin.m_h / 2f;

        m_viewport = new ShapeEx();
        m_viewport.m_w = m_origin.m_w;
        m_viewport.m_h = m_origin.m_h;
        m_viewport.m_centerX = m_origin.m_centerX;
        m_viewport.m_centerY = m_origin.m_centerY;

        m_target = null;
    }

    @Override
    protected void Run_M(ShapeEx target, float x, float y) {
        target.m_x = (x - m_gammaX) / m_origin.m_scaleX + m_oldX;
        target.m_y = (y - m_gammaY) / m_origin.m_scaleY + m_oldY;
    }

    /**
     * 基础坐标系移动
     *
     * @param x
     * @param y
     */
    protected void Run_M2(ShapeEx target, float x, float y) {
        target.m_x = x - m_gammaX + m_oldX;
        target.m_y = y - m_gammaY + m_oldY;
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
     * 基础坐标系的放大移动
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void Run_ZM2(ShapeEx target, float x1, float y1, float x2, float y2) {
        Run_M2(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
        Run_Z(target, x1, y1, x2, y2);
    }

    @Override
    protected void Init_NZ_Data(ShapeEx target, float x1, float y1) {
        Matrix matrix = new Matrix(); //不能包含反转参数
        float[] src = {target.m_x + target.m_centerX, target.m_y + target.m_centerY};
        float[] dst = new float[2];
        GetShowPos(dst, src);
        matrix.postTranslate(dst[0] - target.m_centerX, dst[1] - target.m_centerY);
        matrix.postScale(target.m_scaleX * m_origin.m_scaleX, target.m_scaleY * m_origin.m_scaleY, dst[0], dst[1]);
        matrix.postRotate(target.m_degree, dst[0], dst[1]);
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
     * 逻辑坐标转换为真实的显示坐标
     *
     * @param dst 长度为2的倍数
     * @param src 长度为2的倍数
     */
    protected void GetShowPos(float[] dst, float[] src) {
        int len = src.length / 2 * 2;
        for (int i = 0; i < len; i += 2) {
            dst[i] = (src[i] - m_origin.m_centerX) * m_origin.m_scaleX + m_origin.m_x + m_origin.m_centerX;
            dst[i + 1] = (src[i + 1] - m_origin.m_centerY) * m_origin.m_scaleY + m_origin.m_y + m_origin.m_centerY;
        }
    }

    /**
     * 显示坐标转换为逻辑坐标
     *
     * @param dst
     * @param src
     */
    protected void GetLogicPos(float[] dst, float[] src) {
        int len = src.length / 2 * 2;
        for (int i = 0; i < len; i += 2) {
            dst[i] = (src[i] - m_origin.m_x - m_origin.m_centerX) / m_origin.m_scaleX + m_origin.m_centerX;
            dst[i + 1] = (src[i + 1] - m_origin.m_y - m_origin.m_centerY) / m_origin.m_scaleY + m_origin.m_centerY;
        }
    }

    @Override
    protected void GetShowMatrix(Matrix matrix, ShapeEx item) {
        float[] src = {item.m_x + item.m_centerX, item.m_y + item.m_centerY};
        float[] dst = new float[2];
        GetShowPos(dst, src);

        matrix.reset();
        if (item.m_flip == Shape.Flip.VERTICAL) {
            float[] values = {1, 0, 0, 0, -1, item.m_h, 0, 0, 1};
            matrix.setValues(values);
        } else if (item.m_flip == Shape.Flip.HORIZONTAL) {
            float[] values = {-1, 0, item.m_w, 0, 1, 0, 0, 0, 1};
            matrix.setValues(values);
        }

        matrix.postTranslate(dst[0] - item.m_centerX, dst[1] - item.m_centerY);
        matrix.postScale(item.m_scaleX * m_origin.m_scaleX, item.m_scaleY * m_origin.m_scaleY, dst[0], dst[1]);
        matrix.postRotate(item.m_degree, dst[0], dst[1]);
    }

    /**
     * 获取没基础坐标系的缩放(可有自身缩放)
     *
     * @param matrix
     * @param item
     */
    protected void GetShowMatrixNoScale(Matrix matrix, ShapeEx item) {
        float[] src = {item.m_x + item.m_centerX, item.m_y + item.m_centerY};
        float[] dst = new float[2];
        GetShowPos(dst, src);

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

    /**
     * 复制当前对象,主要用于多线程处理时的UI数据分离
     *
     * @return
     */
    public Object Clone() {
        RelativeView out = null;

        try {
            out = (RelativeView) this.clone();
            out.m_origin = (ShapeEx) this.m_origin.Clone();
            out.m_viewport = (ShapeEx) this.m_viewport.Clone();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return out;
    }
}
