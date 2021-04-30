package com.leory.transform.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * @Description: UI变换基类
 * @Author: leory
 * @Time: 2021/4/30
 */
public class BaseCoreView extends BaseTransformView {
    protected static int BUTTON_SIZE;
    protected ShapeEx m_viewport; //显示视图
    protected ShapeEx m_target;//目标视图
    protected PaintFlagsDrawFilter temp_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected Paint temp_paint = new Paint();
    protected Matrix temp_matrix = new Matrix();
    protected float[] temp_dst = new float[8];
    protected float[] temp_src = new float[8];
    protected Path temp_path = new Path();
    protected ArrayList<ShapeEx> m_pendantArr = new ArrayList<>();//装饰物列表
    protected int m_pendantCurSel = -1; //装饰当前选中index
    protected ShapeEx m_pendant;//当前选中装饰
    protected boolean m_isTouch = false;
    protected boolean m_isOddCtrl = false; //单手操作
    protected float temp_showCX;//临时中心点
    protected float temp_showCY;

    public BaseCoreView(Context context) {
        this(context, null);
    }

    public BaseCoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCoreView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        BUTTON_SIZE = dpToPx(24);
    }

    /**
     * dp转为px
     */
    protected int dpToPx(float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics()) + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.setDrawFilter(temp_filter);
        //画装饰
        int len = m_pendantArr.size();
        for (int i = 0; i < len; i++) {
            DrawItem(canvas, m_pendantArr.get(i));
        }

        //画选中框和按钮
        if (m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()) {
            ShapeEx temp = m_pendantArr.get(m_pendantCurSel);
            //画选中框
            DrawRect(canvas, temp);

            //显示单手旋转放大按钮
            if (!m_isTouch) {
                DrawButtons(canvas, temp);
            }
        }

        canvas.restore();
    }

    protected void DrawItem(Canvas canvas, ShapeEx item) {
        if (item != null && item.m_bmp != null) {
            temp_paint.reset();
            temp_paint.setAntiAlias(true);
            temp_paint.setFilterBitmap(true);
            GetShowMatrix(temp_matrix, item);
            canvas.drawBitmap(item.m_bmp, temp_matrix, temp_paint);
        }
    }

    protected void DrawRect(Canvas canvas, ShapeEx item) {
        temp_dst = GetShapePoints(item, ShapeRectType.RECT_SELECT);
        temp_path.reset();
        temp_path.moveTo(temp_dst[0], temp_dst[1]);
        temp_path.lineTo(temp_dst[2], temp_dst[3]);
        temp_path.lineTo(temp_dst[4], temp_dst[5]);
        temp_path.lineTo(temp_dst[6], temp_dst[7]);
        temp_path.close();

        temp_paint.reset();
        temp_paint.setStrokeCap(Paint.Cap.SQUARE);
        temp_paint.setStrokeJoin(Paint.Join.MITER);
        temp_paint.setAntiAlias(true);
        temp_paint.setStyle(Paint.Style.STROKE);
        if (m_isTouch) {
            temp_paint.setColor(0x7FFFFFFF);
        } else {
            temp_paint.setColor(0xFFFFFFFF);
        }
        temp_paint.setStrokeWidth(dpToPx(1));
        canvas.drawPath(temp_path, temp_paint);
    }

    protected void DrawButtons(Canvas canvas, ShapeEx item) {

    }

    /**
     * 更新目标视图大小
     */
    protected void updateTargetShape(int w, int h) {
        m_target = new ShapeEx();
        m_target.m_w = w;
        m_target.m_h = h;
        m_target.m_centerX = m_target.m_w / 2;
        m_target.m_centerY = m_target.m_h / 2;
        m_target.m_x = m_origin.m_centerX - m_target.m_centerX;
        m_target.m_y = m_origin.m_centerY - m_target.m_centerY;
        float scale1 = (float) m_origin.m_w / (float) m_target.m_w;
        float scale2 = (float) m_origin.m_h / (float) m_target.m_h;
        m_target.m_scaleY = m_target.m_scaleX = (scale1 > scale2) ? scale2 : scale1;

        m_viewport = new ShapeEx();
        m_viewport.m_w = (int) (m_target.m_w * m_target.m_scaleX);
        m_viewport.m_h = (int) (m_target.m_h * m_target.m_scaleY);
        m_viewport.m_centerX = m_viewport.m_w / 2;
        m_viewport.m_centerY = m_viewport.m_h / 2;
        m_viewport.m_x = m_origin.m_centerX - m_viewport.m_centerX;
        m_viewport.m_y = m_origin.m_centerY - m_viewport.m_centerY;
    }

    /**
     * 异步更新UI
     */
    protected void updateUI() {
        postInvalidate();
    }

    /**
     * 外框矩形
     *
     * @param item
     * @return
     */
    public float[] GetShapePoints(ShapeEx item, ShapeRectType type) {
        float[] out = new float[8];
        GetShowMatrix(item.m_matrix, item);
        temp_matrix.set(item.m_matrix);
        temp_src[0] = 0;//左上点
        temp_src[1] = 0;
        temp_src[2] = item.m_w;//右上点
        temp_src[3] = 0;
        temp_src[4] = item.m_w;//右下点
        temp_src[5] = item.m_h;
        temp_src[6] = 0;//左下点
        temp_src[7] = item.m_h;
        if (type == ShapeRectType.RECT_SELECT || type == ShapeRectType.BUTTON) {//加上边距
            float DEF_GAP = BUTTON_SIZE / 2;
            DEF_GAP = (float) Math.sqrt(DEF_GAP * DEF_GAP / 2);
            if (type == ShapeRectType.BUTTON) {
                DEF_GAP += dpToPx(4);
            }
            DEF_GAP = DEF_GAP / item.m_scaleX;
            temp_src[0] = -DEF_GAP;//左上点
            temp_src[1] = -DEF_GAP;
            temp_src[2] = item.m_w + DEF_GAP;//右上点
            temp_src[3] = -DEF_GAP;
            temp_src[4] = item.m_w + DEF_GAP;//右下点
            temp_src[5] = item.m_h + DEF_GAP;
            temp_src[6] = -DEF_GAP;//左下点
            temp_src[7] = item.m_h + DEF_GAP;
        }
        temp_matrix.mapPoints(out, temp_src);
        return out;
    }

    @Override
    protected void oddDown(MotionEvent event) {
        m_isTouch = true;
        if (m_pendantCurSel >= 0) {
            if (m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()) {
                m_pendant = m_pendantArr.get(m_pendantCurSel);
            }
            if (m_pendant != null) {
                Init_M_Data(m_pendant, m_downX, m_downY);
                //更新界面
                updateUI();
            }
        }
    }

    @Override
    protected void oddMove(MotionEvent event) {
        if (m_isTouch && m_pendant != null) {
            if (m_isOddCtrl) {
                //使用临时中心点
                Run_RZ(m_pendant, temp_showCX, temp_showCY, event.getX(), event.getY());
            } else {
                Run_M(m_pendant, event.getX(), event.getY());
            }
            //更新界面
            updateUI();
        }
    }

    @Override
    protected void oddUp(MotionEvent event) {

    }

    @Override
    protected void evenDown(MotionEvent event) {

    }

    @Override
    protected void evenMove(MotionEvent event) {

    }

    @Override
    protected void evenUp(MotionEvent event) {

    }


    public enum ShapeRectType {//矩形类型
        CLING,//紧贴
        RECT_SELECT,//选中框
        BUTTON//按键
    }
}
