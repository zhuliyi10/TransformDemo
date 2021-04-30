//package com.leory.transform.widget;
//
//import android.animation.ValueAnimator;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.PorterDuffXfermode;
//import android.os.Handler;
//import android.os.Message;
//import android.view.HapticFeedbackConstants;
//import android.view.MotionEvent;
//
//import androidx.annotation.NonNull;
//
//import com.adnonstop.album.tool.UserTagMgr;
//import com.adnonstop.edit.widget.Text.Painter;
//import com.adnonstop.edit.widget.Text.ShapeEx5;
//import com.adnonstop.edit.widget.Text.TextInfo;
//import com.adnonstop.edit.widget.graffiti.TextShape;
//import com.adnonstop.edit.widget.lightEffect.ShapeEx2;
//import com.adnonstop.imgedit.data.OperateType;
//import com.adnonstop.imgedit.utils.EditTextUtils;
//import com.adnonstop.imgedit.utils.GraffitiUtils;
//import com.adnonstop.imgedit.utils.ImportImageUtils;
//import com.adnonstop.imgedit.utils.LightEffectUtils;
//import com.adnonstop.system.Tags;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import cn.poco.tianutils.CommonUtils;
//import cn.poco.tianutils.ShareData;
//
///**
// * @Description: 修图组合操作view
// * @Author: leory
// * @Time: 2021/1/15
// */
//public class CombinationEditView extends CoreViewV3 {
//    private LightEffectUtils mLightEffectUtils;
//    private EditTextUtils mEditTextUtils;
//    private GraffitiUtils mGraffitiUtils;
//    private ImportImageUtils mImportImageUtils;
//
//    private static final int MSG_UPDATE_UI = 1;//更新uI
//    private static final float SNAP_DEGREE = 3;//吸附角度
//    private static final float SNAP_DISTANCE = ShareData.DpToPx(5);//吸附距离
//    private static boolean mIsShowTextTips = false;//是否显示了文字提示
//    public int defDeleteRes = 0;    //删除按钮
//    protected ShapeEx mDeleteBtn;
//
//    public int defFlipRes = 0;//翻转按钮
//    private ShapeEx mFlipBtn;
//    public int defColorRes = 0;//颜色按钮
//    private ShapeEx mColorBtn;
//
//    private boolean m_hasMoved = false;
//    private boolean mIsEventUp = false;
//    private Handler mMainHandler = new Handler() {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            if (msg.what == MSG_UPDATE_UI) {
//                UpdateUI();
//            }
//        }
//    };
//
//    public void updateUIASYN() {
//        mMainHandler.sendEmptyMessage(MSG_UPDATE_UI);
//    }
//
//    public CombinationEditView(Context context, int frW, int frH) {
//        super(context, frW, frH);
//        CommonUtils.CancelViewGPU(this);
//        mLightEffectUtils = new LightEffectUtils(this);
//        mEditTextUtils = new EditTextUtils(this);
//        mGraffitiUtils = new GraffitiUtils(this);
//        mImportImageUtils = new ImportImageUtils(this);
//    }
//
//    @Override
//    public void InitData(CoreViewV3.ControlCallback cb) {
//        m_cb = cb;
//        m_rotationBtn = InitBtn(def_rotation_res);
//        mDeleteBtn = InitBtn(defDeleteRes);
//        mFlipBtn = InitBtn(defFlipRes);
//        mColorBtn = InitBtn(defColorRes);
//    }
//
//    @Override
//    protected void DrawToCanvas(Canvas canvas, int mode) {
//        canvas.save();
//        canvas.setDrawFilter(temp_filter);
//        //控制渲染矩形
//        ClipStage(canvas);
//        //画图片
//        DrawItem(canvas, m_img);
//        //画装饰
//        int len = m_pendantArr.size();
//        for (int i = 0; i < len; i++) {
//            ShapeEx item = m_pendantArr.get(i);
//            DrawItem(canvas, item);
//        }
//
//        //画选中框和按钮
//        if ((m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size())) {
//            ShapeEx temp = m_pendantArr.get(m_pendantCurSel);
//            if (temp instanceof ShapeEx5) {
//                //画文字选中框
//                ((ShapeEx5) temp).DrawRect(canvas);
//                drawTextTips(canvas, (ShapeEx5) temp);
//
//            }
//            //画选中框,按钮
//            if (!(temp instanceof ShapeEx2)) {
//                DrawRect(canvas, temp);//画选中框
//                if (!m_isTouch) {
//                    DrawButtons(canvas, temp);//画按键
//                }
//                if (m_isTouch) {
//                    drawTipLine(canvas, temp);//画辅助线
//                }
//            }
//
//
//        }
//
//        canvas.restore();
//    }
//
//    @Override
//    protected void DrawRect(Canvas canvas, ShapeEx item) {
//        temp_dst = GetShapeRealPoints(item, ShapeRectType.RECT_SELECT);
//        temp_path.reset();
//        temp_path.moveTo(temp_dst[0], temp_dst[1]);
//        temp_path.lineTo(temp_dst[2], temp_dst[3]);
//        temp_path.lineTo(temp_dst[4], temp_dst[5]);
//        temp_path.lineTo(temp_dst[6], temp_dst[7]);
//        temp_path.close();
//
//        temp_paint.reset();
//        temp_paint.setStrokeCap(Paint.Cap.SQUARE);
//        temp_paint.setStrokeJoin(Paint.Join.MITER);
//        temp_paint.setAntiAlias(true);
//        temp_paint.setStyle(Paint.Style.STROKE);
//        if (m_isTouch) {
//            temp_paint.setColor(0x7FFFFFFF);
//        } else {
//            temp_paint.setColor(0xFFFFFFFF);
//        }
//        temp_paint.setStrokeWidth(ShareData.DpToPx(1));
//        canvas.drawPath(temp_path, temp_paint);
//    }
//
//    @Override
//    protected void DrawItem(Canvas canvas, ShapeEx item) {
//        if (item != null) {
//            if (item instanceof ShapeEx5) {
//                drawText(canvas, (ShapeEx5) item);
//            } else if (item instanceof ShapeEx2) {
//                drawLightEffect(canvas, (ShapeEx2) item);
//            } else if (item instanceof TextShape) {
//                drawInputText(canvas, (TextShape) item);
//            } else {
//                super.DrawItem(canvas, item);
//            }
//        }
//    }
//
//    @Override
//    protected void EvenDown(MotionEvent event) {
//        m_hasMoved = true;
//        m_isTouch = true;
//        m_isOddCtrl = false;
//        if (m_target != null) {
//            Init_MRZ_Data(m_target, m_downX1, m_downY1, m_downX2, m_downY2);
//            UpdateUI();
//        }
//    }
//
//    @Override
//    protected void EvenUp(MotionEvent event) {
//        mIsEventUp = true;
//        super.EvenUp(event);
//        mIsEventUp = false;
//    }
//
//    @Override
//    protected void EvenMove(MotionEvent event) {
//        if (m_isTouch && m_target != null) {
//            Run_MRZ(m_target, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
//            UpdateUI();
//        }
//    }
//
//    @Override
//    protected void OddMove(MotionEvent event) {
//        float dx = Math.abs(event.getX() - m_downX);
//        float dy = Math.abs(event.getY() - m_downY);
//        if (dx > 20 || dy > 20) {
//            // 移动超过阈值，则表示移动了
//            m_hasMoved = true;
//        }
//        if (m_isTouch && m_target != null && m_operateMode == MODE_PENDANT) {
//            if (m_isOddCtrl) {
//                if (m_oddCtrlType == CTRL_R_Z) {
//                    //使用临时中心点
//                    Run_RZ(m_target, temp_showCX, temp_showCY, event.getX(), event.getY());
//                }
//            } else {
//                Run_M(m_target, event.getX(), event.getY());
//            }
//
//            //更新界面
//            UpdateUI();
//        }
//    }
//
//    @Override
//    protected void OddDown(MotionEvent event) {
//        m_isTouch = true;
//        m_hasMoved = false;
//        if (m_pendantCurSel >= 0) {
//            if (m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()) {
//                m_target = m_pendantArr.get(m_pendantCurSel);
//            }
//            //判断是否选中旋转放大按钮
//            if (m_rotationBtn != null && IsClickBtn(m_rotationBtn, m_downX, m_downY)) {
//                m_hasMoved = true;
//                m_isOddCtrl = true;
//                m_oddCtrlType = CTRL_R_Z;
//                if (m_target != null) {
//                    float[] src = {m_target.m_x + m_target.m_centerX, m_target.m_y + m_target.m_centerY};
//                    float[] dst = new float[2];
//                    GetShowPos(dst, src);
//                    temp_showCX = dst[0];
//                    temp_showCY = dst[1];
//                    Init_RZ_Data(m_target, temp_showCX, temp_showCY, m_downX, m_downY);
//                }
//                return;
//            }
//            if ((mFlipBtn != null && IsClickBtn(mFlipBtn, m_downX, m_downY))) {
//                m_isOddCtrl = true;
//                m_oddCtrlType = CTRL_NZ;
//                return;
//            }
//            if ((mColorBtn != null && IsClickBtn(mColorBtn, m_downX, m_downY))) {
//                m_isOddCtrl = true;
//                m_oddCtrlType = CTRL_NZ;
//                return;
//            }
//            if (m_target != null) {
//                Init_M_Data(m_target, m_downX, m_downY);
//                //更新界面
//                UpdateUI();
//            }
//        }
//        if (m_cb instanceof ControlCallback) {
//            ((ControlCallback) m_cb).TouchImage(true);
//        }
//    }
//
//
//    @Override
//    protected void OddUp(MotionEvent event) {
//        float upX = event.getX();
//        float upY = event.getY();
//        float dx = Math.abs(upX - m_downX);
//        float dy = Math.abs(upY - m_downY);
//        m_isTouch = false;
//        m_isOddCtrl = false;
//        if (!m_hasMoved) {
//            //判断是否选中删除按钮
//            if (mDeleteBtn != null && IsClickBtn(mDeleteBtn, m_downX, m_downY) && m_pendantCurSel != -1) {
//                m_hasMoved = true;
//                m_target = mDeleteBtn;
//
//                if (m_cb instanceof ControlCallback) {
//                    if (m_pendantCurSel < m_pendantArr.size()) {
//                        ((ControlCallback) m_cb).onDelete(m_pendantArr.get(m_pendantCurSel), m_pendantCurSel);
//                    }
//                    ((ControlCallback) m_cb).recordOperate(OperateType.DELETE_DECORATION);
//                }
//
//                return;
//            }
//            if (mFlipBtn != null && IsClickBtn(mFlipBtn, m_downX, m_downY) && m_pendantCurSel != -1) {
//                m_target = mFlipBtn;
//                if (m_cb instanceof ControlCallback) {
//                    ((ControlCallback) m_cb).flip();
//                }
//
//                return;
//            }
//            if (mColorBtn != null && IsClickBtn(mColorBtn, m_downX, m_downY) && m_pendantCurSel != -1) {
//                m_target = mColorBtn;
//                if (m_cb instanceof ControlCallback) {
//                    ((ControlCallback) m_cb).showColor();
//                }
//                return;
//            }
//
//            int index = GetSelectIndex(m_pendantArr, m_downX, m_downY);
//            boolean flag = false;//选中变化了
//            if (index != m_pendantCurSel) {
//                flag = true;
//            }
//            for (ShapeEx shapeEx : m_pendantArr) {
//                if (shapeEx instanceof ShapeEx5) {
//                    ((ShapeEx5) shapeEx).setIsSelect(false);
//                }
//            }
//            if (index >= 0) {
//                m_target = m_pendantArr.get(index);
//                m_pendantCurSel = index;
//                if (m_target instanceof ShapeEx5) {
//                    ShapeEx5 temp = ((ShapeEx5) m_target);
//                    temp.setIsSelect(true);
//                    if (!flag) {
//                        TextInfo.FontInfo font = temp.GetCurSelFont(upX, upY);
//                        if (font != null) {
//                            String text = font.m_showText;
//                            if (m_cb instanceof ControlCallback) {
//                                ((ControlCallback) m_cb).onTextClick(text, upY);
//                            }
//                        }
//                    }
//                }
//                if (m_target != null) {
//                    //通知主界面选中信息
//                    m_cb.SelectPendant(m_pendantCurSel);
//                    //更新界面
//                    UpdateUI();
//                }
//
//            } else {
//                if (m_pendantCurSel >= 0) {
//                    m_pendantCurSel = -1;
//
//                    m_target = null;
//                    //通知主界面选中信息
//                    m_cb.SelectPendant(m_pendantCurSel);
//                    //更新界面
//                    UpdateUI();
//                }
//            }
//            return;
//        }
//        //更新界面
//        UpdateUI();
//        if (!mIsEventUp) {//单指up
//            if (m_operateMode == MODE_PENDANT && (dx > 20 || dy > 20) && m_pendantCurSel != -1) {
//                if (m_cb instanceof ControlCallback) {
//                    ((ControlCallback) m_cb).recordOperate(OperateType.MOVE_DECORATION);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void SetImg(Object info, Bitmap bmp) {
//        super.SetImg(info, bmp);
//    }
//
//    @Override
//    protected void DrawButtons(Canvas canvas, ShapeEx item) {
//        if (item != null) {
//            //移动到正确位置
//            temp_src = GetShapeRealPoints(item, ShapeRectType.BUTTON);
//            if (item.m_flip == Shape.Flip.HORIZONTAL) {
//                temp_dst[0] = temp_src[2];
//                temp_dst[1] = temp_src[3];
//                temp_dst[2] = temp_src[0];
//                temp_dst[3] = temp_src[1];
//                temp_dst[4] = temp_src[6];
//                temp_dst[5] = temp_src[7];
//                temp_dst[6] = temp_src[4];
//                temp_dst[7] = temp_src[5];
//
//            } else {
//                temp_dst = temp_src;
//            }
//            //右下
//            drawBtn(canvas, m_rotationBtn, temp_dst[4], temp_dst[5]);
//            //左上
//            drawBtn(canvas, mDeleteBtn, temp_dst[0], temp_dst[1]);
//            //左下
//            if (item instanceof ShapeEx5) {
//                drawBtn(canvas, mColorBtn, temp_dst[2], temp_dst[3]);
//                if (!((ShapeEx5) item).isEditableText()) {
//                    drawBtn(canvas, mFlipBtn, temp_dst[6], temp_dst[7]);
//                }
//            }
//        }
//    }
//
//    private void drawBtn(Canvas canvas, ShapeEx btn, float x, float y) {
//        if (btn != null) {
//            temp_point_src[0] = x;
//            temp_point_src[1] = y;
//            GetLogicPos(temp_point_dst, temp_point_src);
//            btn.m_x = temp_point_dst[0] - btn.m_centerX;
//            btn.m_y = temp_point_dst[1] - btn.m_centerY;
//            temp_paint.reset();
//            temp_paint.setAntiAlias(true);
//            temp_paint.setFilterBitmap(true);
//            GetShowMatrixNoScale(temp_matrix, btn);
//            canvas.drawBitmap(btn.m_bmp, temp_matrix, temp_paint);
//        }
//    }
//
//    /**
//     * 虚线闪烁
//     *
//     * @param item
//     */
//    private void showTextDashLine(ShapeEx5 item) {
//        if (item.isEditableText()) {
//            Timer timer = new Timer();
//            final int[] time = {0};
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    item.setShowTextDashLine(time[0] % 2 == 1);
//                    updateUIASYN();
//                    if (time[0] == 3) {
//                        timer.cancel();
//                    }
//                    time[0]++;
//
//                }
//            }, 200, 500);
//        }
//    }
//
//    /**
//     * 可修改文字提示
//     *
//     * @param item
//     */
//    private void showTextDashTips(ShapeEx5 item) {
//        if (item.isEditableText()) {
//            if (!mIsShowTextTips) {
//                int count = UserTagMgr.GetTagIntValue(getContext(), Tags.SHOW_TEXT_TIPS_COUNT, 0);
//                if (count >= 3) return;
//                UserTagMgr.SetTagValue(getContext(), Tags.SHOW_TEXT_TIPS_COUNT, String.valueOf(count + 1));
//                mIsShowTextTips = true;
//                ValueAnimator animator = ValueAnimator.ofInt(255, 0).setDuration(300);
//                animator.addUpdateListener(animation -> {
//                    int value = (int) animation.getAnimatedValue();
//                    item.setTextDashTipsAlpha(value);
//                    updateUIASYN();
//                });
//                animator.setStartDelay(2000);
//                animator.start();
//                item.setTextDashTipsAlpha(255);
//            }
//        }
//
//    }
//
//    @Override
//    public int AddPendant2(ShapeEx item) {
//        int index = -1;
//        if (GetPendantIdleNum() > 0) {
//            if (item instanceof ShapeEx5) {//态度
//                showTextDashLine((ShapeEx5) item);
//                showTextDashTips((ShapeEx5) item);
//            }
//            if (item instanceof ShapeEx2 && m_pendantArr.size() > 0) { // 最底层是光效
//                if (m_pendantArr.get(0) instanceof ShapeEx2) {
//                    m_pendantArr.remove(0);
//                }
//                m_pendantArr.add(0, item);
//                index = 0;
//            } else {
//                m_pendantArr.add(item);
//                index = m_pendantArr.size() - 1;
//            }
//            SetSelPendant(index);
//            if (m_cb instanceof ControlCallback) {
//                ((ControlCallback) m_cb).recordOperate(OperateType.ADD_DECORATION);
//            }
//        }
//
//        return index;
//    }
//
//    @Override
//    public void SetSelPendant(int index) {
//        super.SetSelPendant(index);
//        if (index >= 0 && index < m_pendantArr.size()) {
//            m_target = m_pendantArr.get(index);
//        }
//    }
//
//    @Override
//    public ShapeEx DelPendant() {
//        ShapeEx out = null;
//        if (m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()) {
//            out = m_pendantArr.remove(m_pendantCurSel);
//            m_pendantCurSel = -1;
//        }
//        return out;
//    }
//
//    @Override
//    protected int GetSelectIndex(ArrayList<? extends ShapeEx> arr, float x, float y) {
//        int index = -1;
//        int len = arr.size();
//        List<Integer> selectList = new ArrayList<>();//选中的列表
//        for (int i = len - 1; i >= 0; i--) {
//            ShapeEx item = arr.get(i);
//            float[] values = new float[9];
//            GetShowMatrix(item.m_matrix, item);
//            item.m_matrix.getValues(values);
//            if (ProcessorV2.IsSelectTarget(values, item.m_w, item.m_h, x, y)) {
//                if (item instanceof ShapeEx2) {//只有光影才选中
//                    if (selectList.size() > 0) continue;
//                }
//                selectList.add(i);
//            }
//        }
//
//        if (selectList.size() > 0) {
//            index = selectList.get(0);//默认第一个选中
//            for (int i = 0; i < selectList.size() - 1; i++) {
//                if (selectList.get(i) == m_pendantCurSel) {
//                    index = selectList.get(i + 1);//当前选中的下一个选中
//                    break;
//                }
//            }
//        }
//        return index;
//    }
//
//
//    /**
//     * 绘制态度文字
//     *
//     * @param canvas
//     * @param item
//     */
//    private void drawText(Canvas canvas, ShapeEx5 item) {
//        canvas.save();
//        GetShowMatrix(item.m_matrix, item);
//        item.draw(canvas);
//        canvas.restore();
//    }
//
//    @Override
//    protected void Init_R_Data(ShapeEx target, float x1, float y1, float x2, float y2) {
//        if (x1 - x2 == 0) {
//            if (y2 > y1) {
//                m_beta = 90;
//            } else {
//                m_beta = -90;
//            }
//        } else if (y1 - y2 != 0) {
//            m_beta = (float) Math.toDegrees(Math.atan(((double) (y2 - y1)) / (x2 - x1)));
//            if (x1 > x2) {
//                m_beta += 180;
//            }
//        } else {
//            if (x2 > x1) {
//                m_beta = 0;
//            } else {
//                m_beta = 180;
//            }
//        }
////        if (target.m_flip == Shape.Flip.HORIZONTAL) {
////            m_beta = -m_beta;
////        }
//        m_oldDegree = target.m_degree;
//    }
//
//    @Override
//    protected void Run_R(ShapeEx target, float x1, float y1, float x2, float y2) {
//        float tempAngle;
//        if (x1 - x2 == 0) {
//            if (y2 > y1) {
//                tempAngle = 90;
//            } else {
//                tempAngle = -90;
//            }
//        } else if (y1 - y2 != 0) {
//            tempAngle = (float) Math.toDegrees(Math.atan(((double) (y2 - y1)) / (x2 - x1)));
//            if (x1 > x2) {
//                tempAngle += 180;
//            }
//
//        } else {
//            if (x2 > x1) {
//                tempAngle = 0;
//            } else {
//                tempAngle = 180;
//            }
//        }
////        if (target.m_flip == Shape.Flip.HORIZONTAL) {
////            tempAngle = -tempAngle;
////        }
//        float degree = m_oldDegree + tempAngle - m_beta;
//        float absDegree = degree % 360;
//        if (absDegree < 0) {
//            absDegree += 360;
//        }
//        if (absDegree < SNAP_DEGREE || 360 - absDegree < SNAP_DEGREE) {
//            if (target.m_degree != 0) {
//                vibrate();
//            }
//            target.m_degree = 0;
//            return;
//        }
//        if (Math.abs(absDegree - 180) < SNAP_DEGREE) {
//            if (Math.abs(target.m_degree) != 180) {
//                vibrate();
//            }
//            target.m_degree = 180;
//            return;
//        }
//        if (Math.abs(absDegree - 90) < SNAP_DEGREE) {
//            if (Math.abs(target.m_degree) != 90) {
//                vibrate();
//            }
//            target.m_degree = 90;
//            return;
//        }
//        if (Math.abs(absDegree - 270) < SNAP_DEGREE) {
//            if (Math.abs(target.m_degree) != 270) {
//                vibrate();
//            }
//            target.m_degree = 270;
//            return;
//        }
//        target.m_degree = degree;
//    }
//
//    @Override
//    protected void Run_M(ShapeEx target, float x, float y) {
//
//        int img_centerX = (int) (m_img.m_x + m_img.m_centerX);
//        int img_centerY = (int) (m_img.m_y + m_img.m_centerY);
//        float mx = (x - m_gammaX) / m_origin.m_scaleX + m_oldX;
//        float my = (y - m_gammaY) / m_origin.m_scaleY + m_oldY;
//        boolean isSnapX = false;
//        boolean isSnapY = false;
//        if (Math.abs(mx + target.m_centerX - img_centerX) < SNAP_DISTANCE) {
//            float targetX = img_centerX - target.m_centerX;
//            if (target.m_x != targetX) {
//                target.m_x = targetX;
//                vibrate();
//            }
//            isSnapX = true;
//        }
//        if (Math.abs(my + target.m_centerY - img_centerY) < SNAP_DISTANCE) {
//            float targetY = img_centerY - target.m_centerY;
//            if (target.m_y != targetY) {
//                target.m_y = targetY;
//                vibrate();
//            }
//            isSnapY = true;
//        }
//        if (!isSnapX) {
//            target.m_x = mx;
//        }
//        if (!isSnapY) {
//            target.m_y = my;
//        }
//
//    }
//
//    private void vibrate() {
////        VibrateUtils.vibrate(50);
//        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
//    }
//
//    /**
//     * 绘制输入文字
//     *
//     * @param canvas
//     * @param item
//     */
//    private void drawInputText(Canvas canvas, TextShape item) {
//        if (item != null && item.m_ex != null) {
//            getGraffitiUtils().drawText(canvas, item);
//        }
//    }
//
//    /**
//     * 绘制光影
//     *
//     * @param canvas
//     * @param item
//     */
//    private void drawLightEffect(Canvas canvas, ShapeEx2 item) {
//        if (item.m_bmp != null) {
//            temp_paint.reset();
//            temp_paint.setAntiAlias(true);
//            temp_paint.setFilterBitmap(true);
//            temp_paint.setAlpha((int) (item.m_alpha / 120f * 255));
//            PorterDuffXfermode temp_mode = new PorterDuffXfermode(item.m_mode);
//            temp_paint.setXfermode(temp_mode);
//            GetShowMatrix(temp_matrix, item);
//            canvas.drawBitmap(item.m_bmp, temp_matrix, temp_paint);
//        }
//    }
//
//    /**
//     * 画文字提示
//     */
//    private void drawTextTips(Canvas canvas, ShapeEx5 item) {
//        if (item.isEditableText()) {
//            temp_dst = GetShapeRealPoints(item, ShapeRectType.RECT_SELECT);
//            temp_path.reset();
//            temp_path.moveTo(temp_dst[0], temp_dst[1]);
//            temp_path.lineTo(temp_dst[2], temp_dst[3]);
//            temp_paint.reset();
//            temp_paint.setColor(Color.WHITE);
//            temp_paint.setTextSize(ShareData.DpToPx(11));
//            temp_paint.setTextAlign(Paint.Align.CENTER);
//            temp_paint.setAlpha(item.getTextTipsAlpha());
//            int shadowColor = Painter.SetColorAlpha(item.getTextTipsAlpha() / 2, 0xff000000);
//            temp_paint.setShadowLayer(3, 0, ShareData.DpToPx(1), shadowColor);
//            canvas.drawTextOnPath("点击虚线框修改文字", temp_path, 0, -ShareData.DpToPx(10), temp_paint);
//        }
//    }
//
//    /**
//     * 画提示线
//     */
//    private void drawTipLine(Canvas canvas, ShapeEx shape) {
//        temp_paint.reset();
//        temp_paint.setStrokeCap(Paint.Cap.SQUARE);
//        temp_paint.setStrokeJoin(Paint.Join.BEVEL);
//        temp_paint.setStyle(Paint.Style.STROKE);
//        temp_paint.setStrokeWidth(ShareData.DpToPx(1.5f));
//        temp_paint.setColor(0xFF196EFF);
//        temp_paint.setAntiAlias(true);
//        float[] pos = GetShapeRealPoints(m_img, ShapeRectType.CLING);
//        float img_centerX = (pos[0] + pos[4]) / 2;
//        float img_centerY = (pos[1] + pos[5]) / 2;
//        float[] pos1 = GetShapeRealPoints(shape, ShapeRectType.CLING);
//        float centerX = (pos1[0] + pos1[4]) / 2;
//        float centerY = (pos1[1] + pos1[5]) / 2;
//
//        if (Math.abs(centerX - img_centerX) < 1) {
//            //画竖线
//            drawLine(canvas, img_centerX, pos[1], img_centerX, pos[7], temp_paint);
//        }
//        if (Math.abs(centerY - img_centerY) < 1) {
//            //画横线
//            drawLine(canvas, pos[0], img_centerY, pos[2], img_centerY, temp_paint);
//        }
//
//    }
//
//    private void drawLine(Canvas canvas, float x1, float y1, float x2, float y2, Paint p) {
//        canvas.save();
//        canvas.drawLine(x1, y1, x2, y2, p);
//        canvas.restore();
//    }
//
//    /**
//     * 获取index的Pendant
//     *
//     * @param index
//     * @return
//     */
//    public ShapeEx GetPendantByIndex(int index) {
//        ShapeEx out = null;
//        if (index >= 0 && index < m_pendantArr.size()) {
//            out = m_pendantArr.get(index);
//        }
//
//        return out;
//    }
//
//    /**
//     * 获取当前选中的pendant
//     *
//     * @return
//     */
//    public ShapeEx getSelPendant() {
//        ShapeEx out = null;
//        if (m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()) {
//            out = m_pendantArr.get(m_pendantCurSel);
//        }
//        return out;
//    }
//
//
//    public LightEffectUtils getLightEffectUtils() {
//        return mLightEffectUtils;
//    }
//
//    public GraffitiUtils getGraffitiUtils() {
//        return mGraffitiUtils;
//    }
//
//    public EditTextUtils getEditTextUtils() {
//        return mEditTextUtils;
//    }
//
//    public ImportImageUtils getImportImageUtils() {
//        return mImportImageUtils;
//    }
//
//    public interface ControlCallback extends CoreViewV3.ControlCallback {
//        void TouchImage(boolean isTouch);
//
//        void recordOperate(OperateType type);//记录操作
//
//        void flip();//翻转
//
//        void showColor();//显示颜色
//
//        void onTextClick(String text, float y);
//
//        void onDelete(ShapeEx shape, int index);
//    }
//
//    protected ShapeEx InitBtn(int res) {
//        ShapeEx shape = null;
//        if (res != 0) {
//            shape = new ShapeEx();
//            Bitmap bmp = BitmapFactory.decodeResource(getResources(), res);
//            shape.m_bmp = bmp;
//            shape.m_w = ShareData.DpToPx(24);
//            shape.m_h = ShareData.DpToPx(24);
//            shape.m_centerX = (float) shape.m_w / 2f;
//            shape.m_centerY = (float) shape.m_h / 2f;
//        }
//        return shape;
//    }
//
//
//    /**
//     * 外框矩形
//     *
//     * @param item
//     * @return
//     */
//    public float[] GetShapeRealPoints(ShapeEx item, ShapeRectType type) {
//        float[] out = new float[8];
//        GetShowMatrix(item.m_matrix, item);
//        temp_matrix.set(item.m_matrix);
//        temp_src[0] = 0;//左上点
//        temp_src[1] = 0;
//        temp_src[2] = item.m_w;//右上点
//        temp_src[3] = 0;
//        temp_src[4] = item.m_w;//右下点
//        temp_src[5] = item.m_h;
//        temp_src[6] = 0;//左下点
//        temp_src[7] = item.m_h;
//        if (type == ShapeRectType.RECT_SELECT || type == ShapeRectType.BUTTON) {//加上边距
//            float DEF_GAP = ShareData.DpToPx(10);
//            if (mDeleteBtn != null) {
//                DEF_GAP = mDeleteBtn.m_centerX;
//                DEF_GAP = (float) Math.sqrt(DEF_GAP * DEF_GAP / 2);
//                if (type == ShapeRectType.BUTTON) {
//                    DEF_GAP += ShareData.DpToPx(4);
//                }
//                DEF_GAP = DEF_GAP / item.m_scaleX;
//            }
//
//            temp_src[0] = -DEF_GAP;//左上点
//            temp_src[1] = -DEF_GAP;
//            temp_src[2] = item.m_w + DEF_GAP;//右上点
//            temp_src[3] = -DEF_GAP;
//            temp_src[4] = item.m_w + DEF_GAP;//右下点
//            temp_src[5] = item.m_h + DEF_GAP;
//            temp_src[6] = -DEF_GAP;//左下点
//            temp_src[7] = item.m_h + DEF_GAP;
//        }
//        temp_matrix.mapPoints(out, temp_src);
//        return out;
//    }
//
//    @Override
//    public Bitmap GetOutputBmp() {
//
//        Bitmap tempBmp = m_img.m_bmp;
//        int outW = tempBmp.getWidth();
//        int outH = tempBmp.getHeight();
//        Bitmap outBmp = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(outBmp);
//        canvas.setDrawFilter(temp_filter);
//        canvas.drawColor(m_bkColor);
//        temp_paint.reset();
//        temp_paint.setAntiAlias(true);
//        temp_paint.setFilterBitmap(true);
//        canvas.drawBitmap(tempBmp, 0, 0, temp_paint);
//
//        ShapeEx backup = (ShapeEx) m_origin.Clone();
//        //设置输出位置
//        m_origin.m_scaleX = outW / (float) m_viewport.m_w / m_viewport.m_scaleX;
//        m_origin.m_scaleY = m_origin.m_scaleX;
//        m_origin.m_x = (int) outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
//        m_origin.m_y = (int) outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;
//
//        int len = m_pendantArr.size();
//        ShapeEx temp;
//        for (int i = 0; i < len; i++) {
//            temp = m_pendantArr.get(i);
//            if (temp instanceof ShapeEx5) {
//                GetShowMatrix(temp.m_matrix, temp);
//                ((ShapeEx5) temp).DrawChildren(canvas);
//            } else {
//                tempBmp = temp.m_bmp;
//                GetOutputMatrix(temp_matrix, temp, tempBmp);
//                temp_paint.reset();
//                temp_paint.setAntiAlias(true);
//                temp_paint.setFilterBitmap(true);
//                if (temp instanceof ShapeEx2) {
//                    temp_paint.setAlpha((int) (((ShapeEx2) temp).m_alpha / 120f * 255));
//                    PorterDuffXfermode temp_mode = new PorterDuffXfermode(((ShapeEx2) temp).m_mode);
//                    temp_paint.setXfermode(temp_mode);
//                }
//                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
//            }
//        }
//        m_origin.Set(backup);
//        return outBmp;
//    }
//
//    public enum ShapeRectType {//矩形类型
//        CLING,//紧贴
//        RECT_SELECT,//选中框
//        BUTTON//按键
//    }
//}
