package com.leory.transform.transform_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: UI变换基类
 * @Author: leory
 * @Time: 2021/4/30
 */
public class BaseCoreView extends BaseTransformView {
    protected static int BUTTON_SIZE;
    protected PaintFlagsDrawFilter temp_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected Paint temp_paint = new Paint();
    protected Matrix temp_matrix = new Matrix();
    protected float[] temp_dst = new float[8];
    protected float[] temp_src = new float[8];
    protected Path temp_path = new Path();
    protected RectF temp_rect = new RectF();
    protected ArrayList<ShapeEx> m_pendantArr = new ArrayList<>();//装饰物列表
    protected int m_pendantCurSel = -1; //装饰当前选中index
    protected ShapeEx m_pendant;//当前选中装饰
    protected boolean m_isTouch = false;
    private boolean m_hasMoved = false;
    private int m_bkColor = 0xff000000;
    private ShapeEx mBtnLeftTop;//左上按键
    private ShapeEx mBtnRightTop;//右上按键
    private ShapeEx mBtnRightBottom;//右下按键
    private ShapeEx mBtnLeftBottom;//左下按键
    private ShapeEx mBtnDown;//down事件对应的按键

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


    @Override
    protected void onDraw(Canvas canvas) {
        if (m_viewport == null) return;
        canvas.save();
        //显示区域
        clipCanvas(canvas);

        //画背景
        drawBK(canvas);

        canvas.setDrawFilter(temp_filter);
        //画装饰
        int len = m_pendantArr.size();
        for (int i = 0; i < len; i++) {
            drawItem(canvas, m_pendantArr.get(i));
        }

        //画选中框
        drawRect(canvas);

        //显示按键
        if (!m_isTouch) {
            drawButtons(canvas);
        } else {
            drawTipLine(canvas);
        }

        canvas.restore();
    }

    /**
     * 画布裁剪
     *
     * @param canvas
     */
    protected void clipCanvas(Canvas canvas) {
        canvas.clipRect(m_viewport.m_x, m_viewport.m_y, m_viewport.m_x + m_viewport.m_w, m_viewport.m_y + m_viewport.m_h);
    }

    /**
     * 显示区域背景
     *
     * @param canvas
     */
    protected void drawBK(Canvas canvas) {
        canvas.drawColor(m_bkColor);
    }

    /**
     * 画装饰物
     *
     * @param canvas
     * @param item
     */
    protected void drawItem(Canvas canvas, ShapeEx item) {
        if (item != null && item.m_bmp != null) {
            temp_paint.reset();
            temp_paint.setAntiAlias(true);
            temp_paint.setFilterBitmap(true);
            getShowMatrix(temp_matrix, item);
            canvas.drawBitmap(item.m_bmp, temp_matrix, temp_paint);
        }
    }

    /**
     * 画边框
     *
     * @param canvas
     */
    protected void drawRect(Canvas canvas) {
        if (m_pendant == null) return;
        temp_dst = getShapePoints(m_pendant, ShapeRectType.RECT_SELECT);
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

    /**
     * 画按键
     *
     * @param canvas
     */
    protected void drawButtons(Canvas canvas) {
        if (m_pendant != null) {
            //移动到正确位置
            temp_src = getShapePoints(m_pendant, ShapeRectType.BUTTON);
            if (m_pendant.m_flip == ShapeEx.Flip.HORIZONTAL) {
                temp_dst[0] = temp_src[2];
                temp_dst[1] = temp_src[3];
                temp_dst[2] = temp_src[0];
                temp_dst[3] = temp_src[1];
                temp_dst[4] = temp_src[6];
                temp_dst[5] = temp_src[7];
                temp_dst[6] = temp_src[4];
                temp_dst[7] = temp_src[5];
            } else {
                temp_dst = temp_src;
            }
            //左上
            drawBtn(canvas, mBtnLeftTop, temp_dst[0], temp_dst[1]);
            //右上
            drawBtn(canvas, mBtnRightTop, temp_dst[2], temp_dst[3]);
            //右下
            drawBtn(canvas, mBtnRightBottom, temp_dst[4], temp_dst[5]);
            //左下
            drawBtn(canvas, mBtnLeftBottom, temp_dst[6], temp_dst[7]);
        }
    }

    private void drawBtn(Canvas canvas, ShapeEx btn, float x, float y) {
        if (btn != null) {
            btn.m_x = x - btn.m_centerX;
            btn.m_y = y - btn.m_centerY;
            temp_paint.reset();
            temp_paint.setAntiAlias(true);
            temp_paint.setFilterBitmap(true);
            getShowMatrix(btn.m_matrix, btn);
            canvas.drawBitmap(btn.m_bmp, btn.m_matrix, temp_paint);
        }
    }

    /**
     * 画提示线
     */
    private void drawTipLine(Canvas canvas) {
        if (m_pendant == null) return;
        temp_paint.reset();
        temp_paint.setStrokeWidth(dpToPx(1f));
        temp_paint.setColor(0xFF196EFF);
        temp_paint.setAntiAlias(true);
        float[] pos = getShapePoints(m_viewport, ShapeRectType.CLING);
        float img_centerX = (pos[0] + pos[4]) / 2;
        float img_centerY = (pos[1] + pos[5]) / 2;
        float[] pos1 = getShapePoints(m_pendant, ShapeRectType.CLING);
        float centerX = (pos1[0] + pos1[4]) / 2;
        float centerY = (pos1[1] + pos1[5]) / 2;

        if (Math.abs(centerX - img_centerX) < 1) {
            //画竖线
            drawLine(canvas, img_centerX, pos[1], img_centerX, pos[7], temp_paint);
        }
        if (Math.abs(centerY - img_centerY) < 1) {
            //画横线
            drawLine(canvas, pos[0], img_centerY, pos[2], img_centerY, temp_paint);
        }

    }

    private void drawLine(Canvas canvas, float x1, float y1, float x2, float y2, Paint p) {
        canvas.save();
        canvas.drawLine(x1, y1, x2, y2, p);
        canvas.restore();
    }

    /**
     * 更新目标视图大小
     */
    public void updateTargetShape(int w, int h) {
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
        updateUI();
    }

    /**
     * 添加装饰物
     *
     * @param item
     * @return
     */
    public int addPendant(ShapeEx item) {
        int index = -1;
        m_pendantArr.add(item);
        index = m_pendantArr.size() - 1;
        setSelPendant(index);
        return index;
    }

    /**
     * 选中装饰物
     *
     * @param index
     */
    public void setSelPendant(int index) {
        if (index >= 0 && index < m_pendantArr.size()) {
            m_pendantCurSel = index;
            m_pendant = m_pendantArr.get(index);
        } else {
            m_pendantCurSel = -1;
            m_pendant = null;
        }
        updateUI();
    }

    /**
     * 更新UI
     */
    public void updateUI() {
        postInvalidate();
    }

    /**
     * 设置左上按键
     *
     * @param btnRes
     */
    public void setLeftTopBtn(int btnRes, ButtonListener listener) {
        mBtnLeftTop = getButtonShape(btnRes, listener);
    }

    /**
     * 设置右上按键
     *
     * @param btnRes
     */
    public void setRightTopBtn(int btnRes, ButtonListener listener) {
        mBtnRightTop = getButtonShape(btnRes, listener);
    }

    /**
     * 设置右下按键
     *
     * @param btnRes
     */
    public void setRightBottomBtn(int btnRes, ButtonListener listener) {
        mBtnRightBottom = getButtonShape(btnRes, listener);
    }

    /**
     * 设置左下按键
     *
     * @param btnRes
     */
    public void setLeftBottomBtn(int btnRes, ButtonListener listener) {
        mBtnLeftBottom = getButtonShape(btnRes, listener);
    }

    /**
     * 获取按键shape
     *
     * @param res
     * @return
     */
    protected ShapeEx getButtonShape(int res, ButtonListener listener) {
        ShapeEx shape = null;
        if (res != 0) {
            shape = new ShapeEx();
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), res);
            shape.m_bmp = bmp;
            shape.m_w = BUTTON_SIZE;
            shape.m_h = BUTTON_SIZE;
            shape.m_centerX = (float) shape.m_w / 2f;
            shape.m_centerY = (float) shape.m_h / 2f;
            shape.m_ex = listener;
        }
        return shape;
    }

    /**
     * 获取矩形的各个坐标点
     *
     * @param item
     * @return
     */
    private float[] getShapePoints(ShapeEx item, ShapeRectType type) {
        float[] out = new float[8];
        getShowMatrix(item.m_matrix, item);
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
        item.m_matrix.mapPoints(out, temp_src);
        return out;
    }

    /**
     * 获取矩形的rect
     *
     * @param item
     * @param type
     * @return
     */
    private RectF getShapeRect(ShapeEx item, ShapeRectType type) {
        RectF src = new RectF();
        RectF dest = new RectF();
        src.set(0, 0, item.m_w, item.m_h);
        getShowMatrix(item.m_matrix, item);
        item.m_matrix.mapRect(dest, src);
        return dest;
    }

    private void getShowMatrix(Matrix matrix, ShapeEx item) {
        float[] dst = {item.m_x + item.m_centerX, item.m_y + item.m_centerY};
        matrix.reset();
        if (item.m_flip == ShapeEx.Flip.VERTICAL) {
            float[] values = {1, 0, 0, 0, -1, item.m_h, 0, 0, 1};
            matrix.setValues(values);
        } else if (item.m_flip == ShapeEx.Flip.HORIZONTAL) {
            float[] values = {-1, 0, item.m_w, 0, 1, 0, 0, 0, 1};
            matrix.setValues(values);
        }

        matrix.postTranslate(dst[0] - item.m_centerX, dst[1] - item.m_centerY);
        matrix.postScale(item.m_scaleX, item.m_scaleY, dst[0], dst[1]);
        matrix.postRotate(item.m_degree, dst[0], dst[1]);
    }

    /**
     * 获取选中的位置
     *
     * @param x
     * @param y
     * @return
     */
    private int getSelectIndex(float x, float y) {
        int index = -1;
        ShapeEx item;
        int len = m_pendantArr.size();
        for (int i = len - 1; i >= 0; i--) {
            item = m_pendantArr.get(i);
            if (isSelectTarget(item, x, y)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 判断 x,y是否在装饰物所在的矩形内
     *
     * @param shapeEx
     * @param x
     * @param y
     * @return
     */
    private boolean isSelectTarget(ShapeEx shapeEx, float x, float y) {
        if (shapeEx == null) return false;
        if (isContain(shapeEx, x, y)) {
            return true;
        }
        return false;
    }

    private boolean isSelectButton(ShapeEx shapeEx, float x, float y) {
        if (shapeEx == null) return false;
        if (isContain(shapeEx, x, y)) {
            mBtnDown = shapeEx;
            return true;
        }
        return false;
    }

    /**
     * 判断点是否在多边开内
     *
     * @param shapeEx
     * @param x
     * @param y
     * @return
     */
    private boolean isContain(ShapeEx shapeEx, float x, float y) {
        boolean result = false;
        int i = 0;
        float[] pos = getShapePoints(shapeEx, ShapeRectType.CLING);
        List<PointF> points = new ArrayList<>();
        points.add(new PointF(pos[0], pos[1]));
        points.add(new PointF(pos[2], pos[3]));
        points.add(new PointF(pos[4], pos[5]));
        points.add(new PointF(pos[6], pos[7]));
        for (int j = points.size() - 1; i < points.size(); j = i++) {
            if (points.get(i).y > y != points.get(j).y > y
                    && x < (points.get(j).x - points.get(i).x) * (y - points.get(i).y) / (points.get(j).y - points.get(i).y) + points.get(i).x) {
                result = !result;
            }
        }

        return result;
    }


    @Override
    protected void oddDown(MotionEvent event) {
        m_isTouch = true;
        m_hasMoved = false;
        if (m_pendant != null) {
            if (isSelectButton(mBtnLeftTop, m_downX, m_downY)) {
                ((ButtonListener) mBtnLeftTop.m_ex).onButtonAction(MotionEvent.ACTION_DOWN);
                return;
            }
            if (isSelectButton(mBtnRightTop, m_downX, m_downY)) {
                ((ButtonListener) mBtnRightTop.m_ex).onButtonAction(MotionEvent.ACTION_DOWN);
                return;
            }
            if (isSelectButton(mBtnRightBottom, m_downX, m_downY)) {
                ((ButtonListener) mBtnRightBottom.m_ex).onButtonAction(MotionEvent.ACTION_DOWN);
                return;
            }
            if (isSelectButton(mBtnLeftBottom, m_downX, m_downY)) {
                ((ButtonListener) mBtnLeftBottom.m_ex).onButtonAction(MotionEvent.ACTION_DOWN);
                return;
            }
            mBtnDown = null;
            Init_M_Data(m_pendant, m_downX, m_downY);
            //更新界面
            updateUI();
        }
    }

    @Override
    protected void oddMove(MotionEvent event) {
        float dx = Math.abs(m_moveX - m_downX);
        float dy = Math.abs(m_moveY - m_downY);
        if (dx > 20 || dy > 20) {
            // 移动超过阈值，则表示移动了
            m_hasMoved = true;
        }
        if (m_isTouch && m_pendant != null) {
            if (mBtnDown != null) {//按下了按键
                ((ButtonListener) mBtnDown.m_ex).onButtonAction(MotionEvent.ACTION_MOVE);
            } else {
                Run_M(m_pendant, m_moveX, m_moveY);
            }
            //更新界面
            updateUI();
        }
    }

    @Override
    protected void oddUp(MotionEvent event) {
        m_isTouch = false;
        if (!m_hasMoved) {
            if (mBtnDown != null) {
                ((ButtonListener) mBtnDown.m_ex).onButtonAction(MotionEvent.ACTION_UP);
                return;
            }
            int index = getSelectIndex(m_downX, m_downY);
            if (index >= 0) {
                m_pendant = m_pendantArr.get(index);
                m_pendantCurSel = index;
            } else {
                if (m_pendantCurSel >= 0) {
                    m_pendantCurSel = -1;
                    m_pendant = null;
                }
            }
        }

        //更新界面
        updateUI();
    }

    @Override
    protected void evenDown(MotionEvent event) {
        m_hasMoved = true;
        if (m_pendant != null) {
            Init_MRZ_Data(m_pendant, m_downX1, m_downY1, m_downX2, m_downY2);
            updateUI();
        }
    }

    @Override
    protected void evenMove(MotionEvent event) {
        if (m_pendant != null) {
            Run_MRZ(m_pendant, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
            updateUI();
        }
    }

    @Override
    protected void evenUp(MotionEvent event) {
        oddUp(event);
    }


    public enum ShapeRectType {//矩形类型
        CLING,//紧贴
        RECT_SELECT,//选中框
        BUTTON//按键
    }

    public interface ButtonListener {
        void onButtonAction(int action);
    }
}
