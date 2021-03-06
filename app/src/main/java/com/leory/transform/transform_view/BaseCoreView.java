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
 * 3、在视图显示之前，要通过{@link #setImgTargetShape}或{@link #setNullTargetShape}设置目标显示的大小，会自动适配到屏幕
 * 4、通过{@link #addPendant}添加装饰物，通过{@link #deletePendent}删除装饰物
 * 5、根据具体的业务设置按键如{@link #setLeftTopBtn},并在回调{@link #onButtonAction}中处理相关的逻辑
 * 6、通过调用{@link #setControlCallBack}回调事件
 * 7、获取输出的bitmap{@link #getOutputBitmap}
 * @Author: leory
 * @Time: 2021/4/30
 */
public class BaseCoreView extends BaseTransformView {
    protected static int BUTTON_SIZE;//按键大小
    protected Paint temp_paint = new Paint();
    protected float[] temp_dst = new float[8];
    protected float[] temp_src = new float[8];
    protected Path temp_path = new Path();
    protected RectF temp_rect = new RectF();
    protected List<ShapeEx> m_pendantArr = new ArrayList<>();//装饰物列表
    protected int m_pendantCurSel = -1; //装饰当前选中index
    protected ShapeEx m_pendant;//当前选中装饰
    protected boolean m_isTouch = false;
    private boolean m_hasMoved = false;
    private Integer mBgColor = null;
    protected ShapeEx mBtnLeftTop;//左上按键
    protected ShapeEx mBtnRightTop;//右上按键
    protected ShapeEx mBtnRightBottom;//右下按键
    protected ShapeEx mBtnLeftBottom;//左下按键
    protected ShapeEx mBtnDown;//down事件对应的按键
    protected ControlCallBack mCallBack;//回调事件
    protected float m_pendantCX;//选中装饰物中心点X坐标
    protected float m_pendantCY;//选中装饰物中心点Y坐标

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
        drawCanvas(canvas);
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

        //画选中装饰的相关UI
        drawSelectedPendantRelate(canvas);
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
        if (mBgColor != null) {
            canvas.drawColor(mBgColor);
        }
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
            getShowMatrix(item.m_matrix, item);
            canvas.drawBitmap(item.m_bmp, item.m_matrix, null);
        }
    }

    /**
     * 画选中装饰的相关UI
     *
     * @param canvas
     */
    protected void drawSelectedPendantRelate(Canvas canvas) {
        if (m_pendant == null) return;
        //画选中框
        drawRect(canvas);

        //显示按键
        drawButtons(canvas);

        //画中线辅助线
        drawCenterTipLine(canvas);
    }

    /**
     * 画边框
     *
     * @param canvas
     */
    protected void drawRect(Canvas canvas) {
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

    /**
     * 画单个按键
     *
     * @param canvas
     * @param btn
     * @param x
     * @param y
     */
    protected void drawBtn(Canvas canvas, ShapeEx btn, float x, float y) {
        if (btn != null) {
            boolean isShow = (boolean) btn.m_ex;
            if (isShow) {
                btn.m_x = x - btn.m_centerX;
                btn.m_y = y - btn.m_centerY;
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                getShowMatrix(btn.m_matrix, btn);
                canvas.drawBitmap(btn.m_bmp, btn.m_matrix, temp_paint);
            }
        }
    }

    /**
     * 画提示线
     */
    private void drawCenterTipLine(Canvas canvas) {
        if (!m_isTouch) return;
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
        updateUI();
    }

    /**
     * 设置目标shape，并生成显示的视图
     *
     * @param w   目标的宽
     * @param h   目标的高
     * @param bmp 目标的bitmap
     */
    private void setTargetShape(int w, int h, Bitmap bmp) {
        if (m_origin == null) {
            updateOrigin(getMeasuredWidth(), getMeasuredHeight());
        }
        ShapeEx lastViewPort = null;
        if (m_viewport != null) {
            lastViewPort = (ShapeEx) m_viewport.clone();
        }
        m_target = new ShapeEx();
        m_target.m_w = w;
        m_target.m_h = h;
        m_target.m_bmp = bmp;
        m_target.m_centerX = m_target.m_w / 2f;
        m_target.m_centerY = m_target.m_h / 2f;
        m_target.m_x = m_origin.m_centerX - m_target.m_centerX;
        m_target.m_y = m_origin.m_centerY - m_target.m_centerY;
        float scale1 = 1f * m_origin.m_w / m_target.m_w;
        float scale2 = 1f * m_origin.m_h / m_target.m_h;
        m_target.m_scaleY = m_target.m_scaleX = (scale1 > scale2) ? scale2 : scale1;

        if (m_viewport == null) {
            m_viewport = new ShapeEx();
        }
        m_viewport.m_w = (int) (m_target.m_w * m_target.m_scaleX + 0.5f);
        m_viewport.m_h = (int) (m_target.m_h * m_target.m_scaleY + 0.5f);
        m_viewport.m_centerX = m_viewport.m_w / 2f;
        m_viewport.m_centerY = m_viewport.m_h / 2f;
        m_viewport.m_x = m_origin.m_centerX - m_viewport.m_centerX;
        m_viewport.m_y = m_origin.m_centerY - m_viewport.m_centerY;
        onTargetShapeChange(lastViewPort, m_viewport);
    }

    /**
     * viewPort视图变化时调用，保证装饰物所在viewPort相对位置和大小的比例不变
     *
     * @param lastViewPort
     * @param nowViewPort
     */
    private void onTargetShapeChange(ShapeEx lastViewPort, ShapeEx nowViewPort) {
        if (lastViewPort == null || nowViewPort == null) return;
        for (ShapeEx item : m_pendantArr) {
            float centerX = (item.m_x + item.m_centerX - lastViewPort.m_x) / lastViewPort.m_w;
            float centerY = (item.m_y + item.m_centerY - lastViewPort.m_y) / lastViewPort.m_h;
            item.m_x = centerX * nowViewPort.m_w + nowViewPort.m_x - item.m_centerX;
            item.m_y = centerY * nowViewPort.m_h + nowViewPort.m_y - item.m_centerY;
        }
    }

    /**
     * 获取全部覆盖物
     *
     * @return
     */
    public List<ShapeEx> getAllPendants() {
        return m_pendantArr;
    }

    public void setAllPendant(List<ShapeEx> pendants) {
        m_pendantArr = pendants;
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
     * 根据index获取装饰
     *
     * @param index
     * @return
     */
    public ShapeEx getPendant(int index) {
        if (index < 0 || index >= m_pendantArr.size()) return null;
        return m_pendantArr.get(index);
    }

    /**
     * 获取 Pendant的index
     *
     * @param item
     * @return
     */
    public int getPendantIndex(ShapeEx item) {
        if (item == null) return -1;
        for (int i = 0; i < m_pendantArr.size(); i++) {
            if (item == m_pendantArr.get(i)) return i;
        }
        return -1;
    }

    /**
     * 是否是最底层
     *
     * @return
     */
    public boolean isBottomMost(ShapeEx item) {
        return getPendantIndex(item) == 0;
    }

    /**
     * 是否是最顶层
     *
     * @return
     */
    public boolean isTopMost(ShapeEx item) {
        return getPendantIndex(item) == getAllPendants().size() - 1;
    }

    /**
     * 添加装饰物
     *
     * @param item
     * @return
     */
    public int addPendant(ShapeEx item) {
        m_pendantArr.add(item);
        int index = m_pendantArr.size() - 1;
        setSelPendant(index);
        return index;
    }

    /**
     * 删除当前装饰物
     *
     * @return
     */
    public void deleteCurPendent() {
        if (m_pendant != null) {
            m_pendantArr.remove(m_pendant);
            setSelPendant(-1);
        }
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
        ShapeEx shapeEx = m_pendantArr.remove(index);
        setSelPendant(-1);
        return shapeEx;

    }

    /**
     * 清空装饰物
     */
    public void deleteAllPendant() {
        m_pendantArr.clear();
        setSelPendant(-1);
    }

    /**
     * 上一层
     */
    public void lastLayer() {
        if (m_pendant != null && m_pendantCurSel < getAllPendants().size() - 1) {
            if (getAllPendants().remove(m_pendant)) {
                m_pendantCurSel++;
                getAllPendants().add(m_pendantCurSel, m_pendant);
                setSelPendant(m_pendantCurSel);
            }
        }
    }

    /**
     * 下一层
     */
    public void nextLayer() {
        if (m_pendant != null && m_pendantCurSel > 0) {
            if (getAllPendants().remove(m_pendant)) {
                m_pendantCurSel--;
                getAllPendants().add(m_pendantCurSel, m_pendant);
                setSelPendant(m_pendantCurSel);
            }
        }
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
     * 获取目标最底部Y坐标
     *
     * @param shapeEx
     * @return
     */
    public float getBottomY(ShapeEx shapeEx) {
        temp_dst = getShapePoints(shapeEx, ShapeRectType.RECT_SELECT);
        return Math.max(Math.max(temp_dst[1], temp_dst[3]), Math.max(temp_dst[5], temp_dst[7]));
    }

    /**
     * 更新UI
     */
    public void updateUI() {
        postInvalidate();
    }

    /**
     * 调用之前如需改变target则调用{@link #setNullTargetShape}或{@link #setImgTargetShape}
     * 获取输出bitmap
     */
    public Bitmap getOutputBitmap() {
        int outW = m_target.m_w;
        int outH = m_target.m_h;
        float scale = 1f * outW / m_viewport.m_w;
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
     * 设置监听事件
     *
     * @param callBack
     */
    public void setControlCallBack(ControlCallBack callBack) {
        mCallBack = callBack;
    }

    /**
     * 设置左上按键
     *
     * @param btnRes
     */
    public void setLeftTopBtn(int btnRes) {
        mBtnLeftTop = getButtonShape(btnRes);
    }

    /**
     * 设置右上按键
     *
     * @param btnRes
     */
    public void setRightTopBtn(int btnRes) {
        mBtnRightTop = getButtonShape(btnRes);
    }

    /**
     * 设置右下按键
     *
     * @param btnRes
     */
    public void setRightBottomBtn(int btnRes) {
        mBtnRightBottom = getButtonShape(btnRes);
    }


    /**
     * 设置左下按键
     *
     * @param btnRes
     */
    public void setLeftBottomBtn(int btnRes) {
        mBtnLeftBottom = getButtonShape(btnRes);
    }

    /**
     * 获取按键shape
     *
     * @param res
     * @return
     */
    protected ShapeEx getButtonShape(int res) {
        ShapeEx shape = null;
        if (res != 0) {
            shape = new ShapeEx();
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), res);
            shape.m_bmp = bmp;
            BUTTON_SIZE = bmp.getHeight();
            shape.m_w = BUTTON_SIZE;
            shape.m_h = BUTTON_SIZE;
            shape.m_centerX = (float) shape.m_w / 2f;
            shape.m_centerY = (float) shape.m_h / 2f;
            shape.m_ex = true;//显示
        }
        return shape;
    }

    protected void setButtonShow(ShapeEx shapeBtn, boolean isShow) {
        if (shapeBtn != null) {
            shapeBtn.m_ex = isShow;
        }
    }

    /**
     * 获取矩形的各个坐标点
     *
     * @param item
     * @return
     */
    public float[] getShapePoints(ShapeEx item, ShapeRectType type) {
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
            DEF_GAP = DEF_GAP / item.getScale();
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
     * 获取变换的matrix
     *
     * @param matrix
     * @param item
     */
    public void getShowMatrix(Matrix matrix, ShapeEx item) {
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
        matrix.postScale(item.getScale(), item.getScale(), dst[0], dst[1]);
        matrix.postRotate(item.m_degree, dst[0], dst[1]);
    }

    /**
     * 获取选中的位置
     *
     * @param x
     * @param y
     * @return
     */
    protected int getSelectIndex(float x, float y) {
        int index = -1;
        List<Integer> selectList = new ArrayList<>();//选中的列表
        int len = m_pendantArr.size();
        for (int i = len - 1; i >= 0; i--) {
            ShapeEx item = m_pendantArr.get(i);
            if (isSelectTarget(item, x, y)) {
                selectList.add(i);
            }
        }
        if (selectList.size() > 0) {
            index = selectList.get(0);//默认第一个选中
            for (int i = 0; i < selectList.size() - 1; i++) {
                if (selectList.get(i) == m_pendantCurSel) {
                    index = selectList.get(i + 1);//当前选中的下一个选中
                    break;
                }
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
    protected boolean isSelectTarget(ShapeEx shapeEx, float x, float y) {
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
        boolean isShow = (boolean) shapeEx.m_ex;
        if (isShow && isContain(shapeEx, x, y)) {
            mBtnDown = shapeEx;
            return true;
        }
        return false;
    }

    /**
     * 判断点是否在多边形内
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

    /**
     * 选中时回调
     *
     * @param isNewSelect
     */
    protected void onPendantSelect(boolean isNewSelect) {

    }

    /**
     * 按键触摸时回调
     *
     * @param shapeBtn
     * @param action
     */
    protected void onButtonAction(ShapeEx shapeBtn, int action) {

    }

    @Override
    protected void oddDown(MotionEvent event) {
        m_isTouch = true;
        m_hasMoved = false;
        mBtnDown = null;
        if (m_pendant != null) {
            if (isSelectButton(mBtnLeftTop, m_downX, m_downY)) {
                onButtonAction(mBtnLeftTop, MotionEvent.ACTION_DOWN);
                return;
            }
            if (isSelectButton(mBtnRightTop, m_downX, m_downY)) {
                onButtonAction(mBtnRightTop, MotionEvent.ACTION_DOWN);
                return;
            }
            if (isSelectButton(mBtnRightBottom, m_downX, m_downY)) {
                onButtonAction(mBtnRightBottom, MotionEvent.ACTION_DOWN);
                return;
            }
            if (isSelectButton(mBtnLeftBottom, m_downX, m_downY)) {
                onButtonAction(mBtnLeftBottom, MotionEvent.ACTION_DOWN);
                return;
            }
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
        if (m_isTouch && m_hasMoved && m_pendant != null) {
            if (mBtnDown != null) {//按下了按键
                onButtonAction(mBtnDown, MotionEvent.ACTION_MOVE);
            } else {
                Run_M(m_pendant, m_moveX, m_moveY);
                if (mCallBack != null) {
                    mCallBack.onPendantMove(MotionEvent.ACTION_MOVE, event.getPointerCount() == 1);
                }

            }
            //更新界面
            updateUI();
        }
    }

    @Override
    protected void oddUp(MotionEvent event) {
        if (!m_isTouch) return;
        m_isTouch = false;
        if (!m_hasMoved) {
            if (mBtnDown != null) {
                onButtonAction(mBtnDown, MotionEvent.ACTION_UP);
                updateUI();
                return;
            }
            int lastIndex = m_pendantCurSel;
            int index = getSelectIndex(m_downX, m_downY);
            boolean isNewSelect = index != m_pendantCurSel;//上一次是否选中
            if (index >= 0) {
                m_pendant = m_pendantArr.get(index);
                m_pendantCurSel = index;
                onPendantSelect(isNewSelect);
            } else {
                if (m_pendantCurSel >= 0) {
                    m_pendantCurSel = -1;
                    m_pendant = null;
                }
            }
            if (mCallBack != null) {
                mCallBack.onPendantSelected(m_pendantCurSel, lastIndex);
            }
        } else {
            if (m_pendant != null) {
                if (mCallBack != null) {
                    mCallBack.onPendantMove(MotionEvent.ACTION_UP, event.getPointerCount() == 1);
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
            if (mCallBack != null) {
                mCallBack.onPendantMove(MotionEvent.ACTION_MOVE, event.getPointerCount() == 1);
            }
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

    public interface ControlCallBack {
        void onPendantSelected(int index, int lastIndex);//选中回调

        void onPendantMove(int action, boolean isMove);
    }
}
