package com.leory.transform.transform_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: UI变换基类
 * 功能介绍
 * 1、画布绘制{@link #drawCanvas},包括背景{@link #drawBK}，目标图片{@link #drawTarget}，装饰物{@link #drawPendents},
 * 选中框{@link #drawRect},按键{@link #drawButtons},中线辅助线{@link #drawCenterTipLine}
 * 2、处理单指和双指事件
 * 3、在显示之前，要通过{@link #setImgTargetShape}或{@link #setNullTargetShape}设置目标显示的大小，会自动适配到屏幕
 * 4、通过{@link #addPendant}添加装饰物，通过{@link #deletePendent}删除装饰物
 * 5、根据具体的业务设置按键如{@link #setLeftTopBtn},并在回调中处理相关的逻辑，具体参考{@link SimpleTransformView}
 * 6、获取输出的bitmap{@link #getOutputBitmap}
 * @Author: leory
 * @Time: 2021/4/30
 */
public class BaseCoreView extends BaseTransformView {
    protected static int BUTTON_SIZE;//按键大小
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
        drawCanvas(canvas);
        canvas.restore();
    }

    /**
     * 画布绘制
     *
     * @param canvas
     */
    protected void drawCanvas(Canvas canvas) {
        //裁剪显示区域
        clipCanvas(canvas);

        //画背景
        drawBK(canvas);

        //绘制目标
        drawTarget(canvas);

        //画装饰
        drawPendents(canvas);

        //画选中框
        drawRect(canvas);

        //显示按键
        drawButtons(canvas);

        //画中线辅助线
        drawCenterTipLine(canvas);
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
     * 绘制目标图片
     *
     * @param canvas
     */
    protected void drawTarget(Canvas canvas) {
        if (m_target.m_bmp != null) {//如果是空目标就不绘制
            drawItem(canvas, m_target);
        }
    }

    /**
     * 画装饰物集合
     */
    protected void drawPendents(Canvas canvas) {
        int len = m_pendantArr.size();
        for (int i = 0; i < len; i++) {
            drawItem(canvas, m_pendantArr.get(i));
        }
    }

    /**
     * 画shapeEx
     *
     * @param canvas
     * @param item
     */
    protected void drawItem(Canvas canvas, ShapeEx item) {
        if (item != null && item.m_bmp != null) {
            getShowMatrix(temp_matrix, item);
            canvas.drawBitmap(item.m_bmp, temp_matrix, null);
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
        if (m_isTouch) return;
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

    /**
     * 画单个按键
     *
     * @param canvas
     * @param btn
     * @param x
     * @param y
     */
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
    private void drawCenterTipLine(Canvas canvas) {
        if (!m_isTouch) return;
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

    /**
     * 画线
     *
     * @param canvas
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param p
     */
    private void drawLine(Canvas canvas, float x1, float y1, float x2, float y2, Paint p) {
        canvas.drawLine(x1, y1, x2, y2, p);
    }

    /**
     * 设置图片目标，并生成显示的视图，此方法用于需要显示原图片
     *
     * @param bmp
     */
    public void setImgTargetShape(Bitmap bmp) {
        if (bmp == null) return;
        setTargetShape(bmp.getWidth(), bmp.getHeight(), bmp);
    }

    /**
     * 设置空目标，并生成显示的视图，此方法用于不用显示原图片的控件
     */
    public void setNullTargetShape(int w, int h) {
        setTargetShape(w, h, null);
    }

    /**
     * 设置目标shape，并生成显示的视图
     *
     * @param w   目标的宽
     * @param h   目标的高
     * @param bmp 目标的bitmap
     */
    private void setTargetShape(int w, int h, Bitmap bmp) {
        m_target = new ShapeEx();
        m_target.m_w = w;
        m_target.m_h = h;
        m_target.m_bmp = bmp;
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
     * 获取当前装饰物
     *
     * @return
     */
    public ShapeEx getCurPendent() {
        return m_pendant;
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
     * 删除当前装饰物
     *
     * @return
     */
    public int deleteCurPendent() {
        int index = -1;
        if (m_pendant != null) {
            m_pendantArr.remove(m_pendant);
            index = m_pendantCurSel;
            setSelPendant(index);
        }
        return index;
    }

    /**
     * 删除删除装饰物
     *
     * @param item
     * @return
     */
    public int deletePendent(ShapeEx item) {
        int index = -1;
        if (item == null) {
            return index;
        }
        for (int i = 0; i < m_pendantArr.size(); i++) {
            ShapeEx temp = m_pendantArr.get(i);
            if (item == temp) {
                index = i;
                deletePendentByIndex(index);
                break;
            }
        }

        return index;
    }

    /**
     * 通过index删除装饰物
     *
     * @param index
     * @return
     */
    public ShapeEx deletePendentByIndex(int index) {
        if (index < 0 || index >= m_pendantArr.size()) return null;
        return m_pendantArr.remove(index);

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
     * 如果输出的target是图片，则调用此方法前先设置m_target.m_bmp为高清原图，保存完之前改回之前的图片
     * 获取输出bitmap
     */
    public Bitmap getOutputBitmap() {
        int outW = m_viewport.m_w;
        int outH = m_viewport.m_h;
        float scale = 1f;
        if (m_target.m_bmp != null) {//是图片,则导出改成图片的宽高
            outW = m_target.m_bmp.getWidth();
            outH = m_target.m_bmp.getHeight();
            scale = 1f * outW / m_viewport.m_w;
        }
        Bitmap outBmp = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBmp);
        if (m_target.m_bmp != null) {
            canvas.drawBitmap(m_target.m_bmp, 0, 0, null);
        }
        canvas.scale(scale, scale);
        canvas.translate(-m_viewport.m_x, -m_viewport.m_y);
        //画装饰
        drawPendents(canvas);
        return outBmp;
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
    protected float[] getShapePoints(ShapeEx item, ShapeRectType type) {
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
    protected RectF getShapeRect(ShapeEx item, ShapeRectType type) {
        RectF src = new RectF();
        RectF dest = new RectF();
        src.set(0, 0, item.m_w, item.m_h);
        getShowMatrix(item.m_matrix, item);
        item.m_matrix.mapRect(dest, src);
        return dest;
    }

    /**
     * 获取变换的matrix
     *
     * @param matrix
     * @param item
     */
    protected void getShowMatrix(Matrix matrix, ShapeEx item) {
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

    /**
     * 判断 x,y是否在按键所在的矩形内
     *
     * @param shapeEx
     * @param x
     * @param y
     * @return
     */
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
