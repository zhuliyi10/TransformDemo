package com.leory.transform.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


/**
 * 支持部分GPU加速
 *
 * @author pocouser
 */
public class CoreViewV3 extends RelativeView {
    public static final int MODE_ALL = 0x0001;
    public static final int MODE_IMAGE = 0x0002;
    public static final int MODE_FRAME = 0x0004;
    public static final int MODE_PENDANT = 0x0008;
    public static final int CTRL_R_Z = 0x0001;
    public static final int CTRL_NZ = 0x0002;
    public static final int LAYOUT_MODE_NONE = 0;
    public static final int LAYOUT_MODE_MATCH_PARENT = 0x0001; //铺满父控件
    public static final int LAYOUT_MODE_WRAP_TOP = 0X0002; //太大缩放填满父控件,比父控件小不放大,上居中
    public int def_rotation_res = 0;
    public int def_zoom_res = 0;
    public float def_limit_sacle = 0.25f; //图片移出显示区的限制
    public int def_pendant_max_num = GetPendantMaxNum();
    public float def_img_max_scale = 2f; //img_size / view_size 最大比例
    public float def_img_min_scale = 0.3f; //img_size / view_size 最小比例
    public float def_pendant_max_scale = 1.5f; //pendant_size / view_size 最大比例
    public float def_pendant_min_scale = 0.05f; //pendant_size / view_size 最小比例
    public int m_bkColor = 0;
    public ShapeEx m_bk = null;
    public ShapeEx m_img = null;
    public ShapeEx m_frame = null;
    public ArrayList<ShapeEx> m_pendantArr = new ArrayList<ShapeEx>();
    protected int m_layoutMode = LAYOUT_MODE_NONE;
    protected int m_pendantCurSel = -1; //装饰当前选中index

    protected boolean m_isTouch = false;
    protected boolean m_isOddCtrl = false; //单手操作
    protected int m_oddCtrlType; //单手操作类型
    protected Bitmap m_drawBuffer;
    protected boolean m_drawable = false; //控制是否可画
    protected boolean m_invalidate = true; //控制缓存是否有效
    protected int m_operateMode = MODE_IMAGE;
    protected ControlCallback m_cb;

    protected ShapeEx m_rotationBtn;
    protected ShapeEx m_nzoomBtn; //只能固定在右上角
    protected boolean m_hasRotationBtn = true;
    protected boolean m_hasZoomBtn = true;
    protected PaintFlagsDrawFilter temp_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected Paint temp_paint = new Paint();
    protected Matrix temp_matrix = new Matrix();
    protected float[] temp_dst = new float[8];
    protected float[] temp_src = new float[8];
    protected Path temp_path = new Path();
    //protected PorterDuffXfermode temp_xfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    protected float[] temp_point_src = {0, 0};
    protected float[] temp_point_dst = {0, 0};
    protected float temp_showCX;
    protected float temp_showCY;

    public CoreViewV3(Context context, int frW, int frH) {
        super(context, frW, frH);
    }

    public void InitData(ControlCallback cb) {
        m_cb = cb;

        Bitmap bmp;
        if (def_rotation_res != 0) {
            m_rotationBtn = new ShapeEx();
            bmp = BitmapFactory.decodeResource(getResources(), def_rotation_res);
            m_rotationBtn.m_bmp = bmp;
            m_rotationBtn.m_w = bmp.getWidth();
            m_rotationBtn.m_h = bmp.getHeight();
            m_rotationBtn.m_centerX = m_rotationBtn.m_w / 2f;
            m_rotationBtn.m_centerY = m_rotationBtn.m_h / 2f;
        }

        if (def_zoom_res != 0) {
            m_nzoomBtn = new ShapeEx();
            bmp = BitmapFactory.decodeResource(getResources(), def_zoom_res);
            m_nzoomBtn.m_bmp = bmp;
            m_nzoomBtn.m_w = bmp.getWidth();
            m_nzoomBtn.m_h = bmp.getHeight();
            m_nzoomBtn.m_centerX = (float) m_nzoomBtn.m_w / 2f;
            m_nzoomBtn.m_centerY = (float) m_nzoomBtn.m_h / 2f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (m_drawable && m_viewport.m_w > 0 && m_viewport.m_h > 0) {
            if (m_isTouch) {
                DrawToCanvas(canvas, m_operateMode);
            } else {
                if (m_drawBuffer == null && m_origin.m_w > 0 && m_origin.m_h > 0) {
                    m_drawBuffer = Bitmap.createBitmap(m_origin.m_w, m_origin.m_h, Config.ARGB_8888);
                }

                if (m_drawBuffer != null) {
                    if (m_invalidate) {
                        Canvas tempCanvas = new Canvas(m_drawBuffer);
                        tempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        DrawToCanvas(tempCanvas, m_operateMode);
                        m_invalidate = false;
                    }

                    canvas.save();
                    //canvas.setDrawFilter(temp_filter);
                    temp_paint.reset();
                    //temp_paint.setAntiAlias(true);
                    //temp_paint.setFilterBitmap(true);
                    canvas.drawBitmap(m_drawBuffer, 0, 0, temp_paint);

                    canvas.restore();
                }
            }
        }
    }

    protected void DrawToCanvas(Canvas canvas, int mode) {
        canvas.save();

        canvas.setDrawFilter(temp_filter);

        //控制渲染矩形
        ClipStage(canvas);

        //画背景
        DrawBK(canvas, m_bk, m_bkColor);

        //画图片
        DrawItem(canvas, m_img);

        //画边框
        DrawItem(canvas, m_frame);

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

    protected void ClipStage(Canvas canvas) {
        GetShowMatrix(temp_matrix, m_viewport);
        temp_src[0] = 0;
        temp_src[1] = 0;
        temp_src[2] = m_viewport.m_w;
        temp_src[3] = 0;
        temp_src[4] = m_viewport.m_w;
        temp_src[5] = m_viewport.m_h;
        temp_src[6] = 0;
        temp_src[7] = m_viewport.m_h;
        temp_matrix.mapPoints(temp_dst, temp_src);
        //temp_paint.reset();
        //temp_paint.setStyle(Paint.Style.FILL);
        //temp_paint.setAntiAlias(true);
        //temp_paint.setFilterBitmap(true);
        //temp_paint.setColor(0xFFFFFFFF);
        //temp_paint.setXfermode(temp_xfermode);
        if (temp_dst[0] < 0) {
            temp_dst[0] = 0;
        } else {
            if (temp_dst[0] != (int) temp_dst[0]) {
                temp_dst[0] += 0.5f; //浮点误差补偿
            }
        }
        if (temp_dst[1] < 0) {
            temp_dst[1] = 0;
        } else {
            if (temp_dst[1] != (int) temp_dst[1]) {
                temp_dst[1] += 0.5f; //浮点误差补偿
            }
        }
        temp_dst[4] = (int) temp_dst[4];
        temp_dst[5] = (int) temp_dst[5];
        if (temp_dst[4] > m_origin.m_w) {
            temp_dst[4] = m_origin.m_w;
        }
        if (temp_dst[5] > m_origin.m_h) {
            temp_dst[5] = m_origin.m_h;
        }
        //System.out.println(temp_dst[0] + "," + temp_dst[1] + "," + temp_dst[4] + "," + temp_dst[5]);
        canvas.clipRect(temp_dst[0], temp_dst[1], temp_dst[4], temp_dst[5]);
    }

    protected void DrawBK(Canvas canvas, ShapeEx bk, int color) {
        if (bk != null) {
            temp_point_src[0] = m_viewport.m_x + m_viewport.m_centerX;
            temp_point_src[1] = m_viewport.m_y + m_viewport.m_centerY;
            GetShowPos(temp_point_dst, temp_point_src);
            float w2 = m_viewport.m_centerX * m_viewport.m_scaleX * m_origin.m_scaleX;
            float h2 = m_viewport.m_centerY * m_viewport.m_scaleY * m_origin.m_scaleY;
            float x = temp_point_dst[0] - w2;
            float y = temp_point_dst[1] - h2;
            float left = x;
            if (left < 0) {
                left = 0;
            }
            float top = y;
            if (top < 0) {
                top = 0;
            }
            float right = temp_point_dst[0] + w2;
            if (right > m_origin.m_w) {
                right = m_origin.m_w;
            }
            float bottom = temp_point_dst[1] + h2;
            if (bottom > m_origin.m_h) {
                bottom = m_origin.m_h;
            }
            if (left < m_origin.m_w && top < m_origin.m_h && right > 0 && bottom > 0) {
                canvas.save();
                canvas.translate(x, y);

                if (bk.m_bmp != null) {
                    temp_paint.reset();
                    temp_paint.setAntiAlias(true);
                    temp_paint.setFilterBitmap(true);
                    BitmapShader shader = new BitmapShader(bk.m_bmp, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
                    temp_paint.setShader(shader);
                    canvas.drawRect(left - x, top - y, right - x, bottom - y, temp_paint);
                }

                canvas.restore();
            }
        } else if (color != 0) {
            canvas.drawColor(color);
        }
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
        if (item != null) {
            GetShowMatrix(temp_matrix, item);

            //画选中框
            temp_src[0] = 0;
            temp_src[1] = 0;
            temp_src[2] = item.m_w;
            temp_src[3] = 0;
            temp_src[4] = item.m_w;
            temp_src[5] = item.m_h;
            temp_src[6] = 0;
            temp_src[7] = item.m_h;
            temp_matrix.mapPoints(temp_dst, temp_src);
            temp_paint.reset();
            temp_paint.setStyle(Style.STROKE);
            temp_paint.setColor(0xA0FFFFFF);
            temp_paint.setStrokeCap(Paint.Cap.SQUARE);
            temp_paint.setStrokeJoin(Paint.Join.MITER);
            temp_paint.setStrokeWidth(2);
            canvas.drawLine(temp_dst[0], temp_dst[1], temp_dst[2], temp_dst[3], temp_paint);
            canvas.drawLine(temp_dst[2], temp_dst[3], temp_dst[4], temp_dst[5], temp_paint);
            canvas.drawLine(temp_dst[4], temp_dst[5], temp_dst[6], temp_dst[7], temp_paint);
            canvas.drawLine(temp_dst[6], temp_dst[7], temp_dst[0], temp_dst[1], temp_paint);
            //GPU兼容问题
            //temp_path.reset();
            //temp_path.moveTo(temp_dst[0], temp_dst[1]);
            //temp_path.lineTo(temp_dst[2], temp_dst[3]);
            //temp_path.lineTo(temp_dst[4], temp_dst[5]);
            //temp_path.lineTo(temp_dst[6], temp_dst[7]);
            //temp_path.close();
            //canvas.drawPath(temp_path, temp_paint);
        }
    }

    protected void DrawButtons(Canvas canvas, ShapeEx item) {
        if (item != null) {
            //移动到正确位置
            temp_matrix.reset();
            temp_point_src[0] = item.m_x + item.m_centerX;
            temp_point_src[1] = item.m_y + item.m_centerY;
            float[] cxy = new float[2];
            GetShowPos(cxy, temp_point_src);
            temp_matrix.postTranslate(cxy[0] - item.m_centerX, cxy[1] - item.m_centerY);
            temp_matrix.postScale(item.m_scaleX * m_origin.m_scaleX, item.m_scaleY * m_origin.m_scaleY, cxy[0], cxy[1]);
            temp_src[0] = 0;
            temp_src[1] = 0;
            temp_src[2] = item.m_w;
            temp_src[3] = 0;
            temp_src[4] = item.m_w;
            temp_src[5] = item.m_h;
            temp_src[6] = 0;
            temp_src[7] = item.m_h;
            temp_matrix.mapPoints(temp_dst, temp_src);

            if (m_rotationBtn != null && m_hasRotationBtn) {
                //计算按钮的位置
                float[] dst = new float[8];
                temp_src[0] = temp_dst[0] - m_rotationBtn.m_centerX;
                temp_src[1] = temp_dst[1] - m_rotationBtn.m_centerY;
                temp_src[2] = temp_dst[2] + m_rotationBtn.m_centerX;
                temp_src[3] = temp_dst[3] - m_rotationBtn.m_centerY;
                temp_src[4] = temp_dst[4] + m_rotationBtn.m_centerX;
                temp_src[5] = temp_dst[5] + m_rotationBtn.m_centerY;
                temp_src[6] = temp_dst[6] - m_rotationBtn.m_centerX;
                temp_src[7] = temp_dst[7] + m_rotationBtn.m_centerY;
                Matrix matrix = new Matrix();
                matrix.postRotate(item.m_degree, cxy[0], cxy[1]);
                matrix.mapPoints(dst, temp_src);

                //测试用
                //temp_path.reset();
                //temp_path.moveTo(dst[0], dst[1]);
                //temp_path.lineTo(dst[2], dst[3]);
                //temp_path.lineTo(dst[4], dst[5]);
                //temp_path.lineTo(dst[6], dst[7]);
                //temp_path.close();
                //temp_paint.setColor(0xFF00FF00);
                //canvas.drawPath(temp_path, temp_paint);

                float[] dst2 = new float[8];
                temp_src[0] = 0;
                temp_src[1] = 0;
                temp_src[2] = m_viewport.m_w;
                temp_src[3] = 0;
                temp_src[4] = m_viewport.m_w;
                temp_src[5] = m_viewport.m_h;
                temp_src[6] = 0;
                temp_src[7] = m_viewport.m_h;
                GetShowMatrix(matrix, m_viewport);
                matrix.mapPoints(dst2, temp_src);

                if (dst2[0] < m_origin.m_w && dst2[1] < m_origin.m_h && dst2[4] > 0 && dst2[5] > 0) {
                    //边界 左上角(a,b) 右下角(c,d)
                    float a = ((dst2[0] < 0) ? 0 : dst2[0]);
                    float b = ((dst2[1] < 0) ? 0 : dst2[1]);
                    float c = ((dst2[4] > m_origin.m_w) ? m_origin.m_w : dst2[4]);
                    float d = ((dst2[5] > m_origin.m_h) ? m_origin.m_h : dst2[5]);
                    if (c - a > m_rotationBtn.m_w) {
                        a += m_rotationBtn.m_centerX;
                        c -= m_rotationBtn.m_centerX;
                    }
                    if (d - b > m_rotationBtn.m_h) {
                        b += m_rotationBtn.m_centerY;
                        d -= m_rotationBtn.m_centerY;
                    }

                    //测试用
                    //temp_path.reset();
                    //temp_path.moveTo(a, b);
                    //temp_path.lineTo(c, b);
                    //temp_path.lineTo(c, d);
                    //temp_path.lineTo(a, d);
                    //temp_path.close();
                    //temp_paint.setColor(0xFF0000FF);
                    //canvas.drawPath(temp_path, temp_paint);

                    float p0x = (a + c) / 2f;
                    float p0y = (b + d) / 2f;

                    //(dst[4], dst[5])右下角坐标
                    if (dst[4] > a && dst[4] < c && dst[5] > b && dst[5] < d) {
                        temp_point_src[0] = dst[4];
                        temp_point_src[1] = dst[5];
                    }
                    //(dst[6], dst[7])左下角坐标
                    else if (dst[6] > a && dst[6] < c && dst[7] > b && dst[7] < d) {
                        temp_point_src[0] = dst[6];
                        temp_point_src[1] = dst[7];
                    }
                    //(dst[0], dst[1])左上角坐标
                    else if (dst[0] > a && dst[0] < c && dst[1] > b && dst[1] < d) {
                        temp_point_src[0] = dst[0];
                        temp_point_src[1] = dst[1];
                    }
                    //(dst[2], dst[3])右上角坐标,注意和其他按钮冲突
                    else if ((m_nzoomBtn == null || !m_hasZoomBtn) && dst[2] > a && dst[2] < c && dst[3] > b && dst[3] < d) {
                        temp_point_src[0] = dst[2];
                        temp_point_src[1] = dst[3];
                    } else {
                        float d1 = ImageUtils.Spacing(p0x - dst[0], p0y - dst[1]);
                        float d2 = 0;
                        if (m_nzoomBtn != null && m_hasZoomBtn) {
                            d2 = 999999f; //极大值
                        } else {
                            d2 = ImageUtils.Spacing(p0x - dst[2], p0y - dst[3]);
                        }
                        float d3 = ImageUtils.Spacing(p0x - dst[4], p0y - dst[5]);
                        float d4 = ImageUtils.Spacing(p0x - dst[6], p0y - dst[7]);

                        float min = Math.min(Math.min(Math.min(d1, d2), d3), d4);
                        if (min == d3) {
                            temp_point_src[0] = dst[4];
                            temp_point_src[1] = dst[5];
                        } else if (min == d2) {
                            temp_point_src[0] = dst[2];
                            temp_point_src[1] = dst[3];
                        } else if (min == d4) {
                            temp_point_src[0] = dst[6];
                            temp_point_src[1] = dst[7];
                        } else {
                            temp_point_src[0] = dst[0];
                            temp_point_src[1] = dst[1];
                        }
                    }

                    GetLogicPos(temp_point_dst, temp_point_src);
                    m_rotationBtn.m_x = temp_point_dst[0] - m_rotationBtn.m_centerX;
                    m_rotationBtn.m_y = temp_point_dst[1] - m_rotationBtn.m_centerY;

                    temp_paint.reset();
                    temp_paint.setAntiAlias(true);
                    temp_paint.setFilterBitmap(true);
                    GetShowMatrixNoScale(temp_matrix, m_rotationBtn);
                    canvas.drawBitmap(m_rotationBtn.m_bmp, temp_matrix, temp_paint);
                }
            }

            //上下顺序不能调换
            if (m_nzoomBtn != null && m_hasZoomBtn) {
                temp_src[0] = temp_dst[0] - m_nzoomBtn.m_centerX;
                temp_src[1] = temp_dst[1] - m_nzoomBtn.m_centerY;
                temp_src[2] = temp_dst[2] + m_nzoomBtn.m_centerX;
                temp_src[3] = temp_dst[3] - m_nzoomBtn.m_centerY;
                temp_src[4] = temp_dst[4] + m_nzoomBtn.m_centerX;
                temp_src[5] = temp_dst[5] + m_nzoomBtn.m_centerY;
                temp_src[6] = temp_dst[6] - m_nzoomBtn.m_centerX;
                temp_src[7] = temp_dst[7] + m_nzoomBtn.m_centerY;

                temp_matrix.reset();
                temp_matrix.postRotate(item.m_degree, cxy[0], cxy[1]);
                temp_matrix.mapPoints(temp_dst, temp_src);

                //测试用
                //temp_path.reset();
                //temp_path.moveTo(temp_dst[0], temp_dst[1]);
                //temp_path.lineTo(temp_dst[2], temp_dst[3]);
                //temp_path.lineTo(temp_dst[4], temp_dst[5]);
                //temp_path.lineTo(temp_dst[6], temp_dst[7]);
                //temp_path.close();
                //temp_paint.setColor(0xFFFF0000);
                //canvas.drawPath(temp_path, temp_paint);

                temp_point_src[0] = temp_dst[2];
                temp_point_src[1] = temp_dst[3];
                GetLogicPos(temp_point_dst, temp_point_src);
                m_nzoomBtn.m_x = temp_point_dst[0] - m_nzoomBtn.m_centerX;
                m_nzoomBtn.m_y = temp_point_dst[1] - m_nzoomBtn.m_centerY;

                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                GetShowMatrixNoScale(temp_matrix, m_nzoomBtn);
                canvas.drawBitmap(m_nzoomBtn.m_bmp, temp_matrix, temp_paint);
            }
        }
    }

    public int GetPendantMaxNum() {
        long mem = Runtime.getRuntime().maxMemory() / 1048576;
        int max;
        if (mem >= 64) {
            max = 36;
        } else if (mem >= 32) {
            max = 24;
        } else if (mem >= 24) {
            max = 16;
        } else {
            max = 12;
        }

        return max;
    }

    public int GetPendantIdleNum() {
        return def_pendant_max_num - m_pendantArr.size();
    }

    @Override
    protected void OddDown(MotionEvent event) {
        m_isTouch = true;

        switch (m_operateMode) {
            case MODE_ALL:
                m_target = m_origin;
                Init_M_Data(m_target, m_downX, m_downY);
                break;

            case MODE_FRAME:
                m_target = m_img;
                Init_M_Data(m_target, m_downX, m_downY);
                break;

            case MODE_PENDANT: {
                if (m_pendantCurSel >= 0) {
                    //判断是否选中旋转放大按钮
                    if (m_rotationBtn != null && IsClickBtn(m_rotationBtn, m_downX, m_downY)) {
                        m_target = m_pendantArr.get(m_pendantCurSel);
                        m_isOddCtrl = true;
                        m_oddCtrlType = CTRL_R_Z;
                        float[] src = {m_target.m_x + m_target.m_centerX, m_target.m_y + m_target.m_centerY};
                        float[] dst = new float[2];
                        GetShowPos(dst, src);
                        temp_showCX = dst[0];
                        temp_showCY = dst[1];
                        Init_RZ_Data(m_target, dst[0], dst[1], m_downX, m_downY);

                        return;
                    }

                    //判断是否选中非比例缩放按钮
                    if (m_nzoomBtn != null && IsClickBtn(m_nzoomBtn, m_downX, m_downY)) {
                        m_target = m_pendantArr.get(m_pendantCurSel);
                        m_isOddCtrl = true;
                        m_oddCtrlType = CTRL_NZ;
                        Init_NZ_Data(m_target, m_downX, m_downY);

                        return;
                    }
                }

                int index = GetSelectIndex(m_pendantArr, m_downX, m_downY);
                if (index >= 0) {
                    m_target = m_pendantArr.get(index);
                    m_pendantArr.remove(index);
                    m_pendantArr.add(m_target);
                    m_pendantCurSel = m_pendantArr.size() - 1;
                    m_isOddCtrl = false;
                    Init_M_Data(m_target, m_downX, m_downY);
                    //通知主界面选中信息
                    m_cb.SelectPendant(m_pendantCurSel);

                    //更新界面
                    this.invalidate();
                } else {
                    if (m_pendantCurSel >= 0) {
                        m_pendantCurSel = -1;
                        //通知主界面选中信息
                        m_cb.SelectPendant(m_pendantCurSel);

                        //更新界面
                        this.invalidate();
                    }
                    m_isOddCtrl = false;
                    m_target = null;
                }
                break;
            }

            case MODE_IMAGE:
            default:
                m_target = null;
                break;
        }
    }

    @Override
    protected void OddMove(MotionEvent event) {
        if (m_isTouch && m_target != null) {
            switch (m_operateMode) {
                case MODE_ALL:
                    Run_M2(m_target, event.getX(), event.getY());
                    //更新界面
                    this.invalidate();
                    break;

                case MODE_FRAME:
                    Run_M(m_target, event.getX(), event.getY());
                    //更新界面
                    this.invalidate();
                    break;

                case MODE_PENDANT: {
                    if (m_isOddCtrl) {
                        if (m_oddCtrlType == CTRL_R_Z) {
                            //使用临时中心点
                            Run_RZ(m_target, temp_showCX, temp_showCY, event.getX(), event.getY());
                        } else if (m_oddCtrlType == CTRL_NZ) {
                            Run_NZ(m_target, event.getX(), event.getY());
                        }
                    } else {
                        Run_M(m_target, event.getX(), event.getY());
                    }
                    //更新界面
                    this.invalidate();
                    break;
                }

                case MODE_IMAGE:
                default:
                    break;
            }
        }
    }

    @Override
    protected void OddUp(MotionEvent event) {
        //判断主图片位置，并且移动到合适位置
        if (m_isTouch) {
            switch (m_operateMode) {
                case MODE_ALL:
                    break;

                case MODE_FRAME:
                    if (m_img != null && m_img == m_target) {
                        float limit;
                        if (m_viewport.m_w > m_viewport.m_h) {
                            limit = def_limit_sacle * m_viewport.m_w * m_viewport.m_scaleX;
                        } else {
                            limit = def_limit_sacle * m_viewport.m_h * m_viewport.m_scaleY;
                        }
                        float w2 = m_viewport.m_centerX * m_viewport.m_scaleX;
                        float h2 = m_viewport.m_centerY * m_viewport.m_scaleY;
                        float left = m_viewport.m_x + m_viewport.m_centerX - w2;
                        float top = m_viewport.m_y + m_viewport.m_centerY - h2;
                        float right = m_viewport.m_x + m_viewport.m_centerX + w2;
                        float bottom = m_viewport.m_y + m_viewport.m_centerY + h2;
                        float imgw2 = m_img.m_centerX * m_img.m_scaleX;
                        float imgh2 = m_img.m_centerY * m_img.m_scaleY;

                        if (imgw2 > limit) {
                            left -= imgw2 - limit;
                            right += imgw2 - limit;
                        }

                        if (imgh2 > limit) {
                            top -= imgh2 - limit;
                            bottom += imgh2 - limit;
                        }

                        float cx = m_img.m_x + m_img.m_centerX;
                        float cy = m_img.m_y + m_img.m_centerY;

                        if (cx < left) {
                            m_img.m_x = left - m_img.m_centerX;
                        } else if (cx > right) {
                            m_img.m_x = right - m_img.m_centerX;
                        }
                        if (cy < top) {
                            m_img.m_y = top - m_img.m_centerY;
                        } else if (cy > bottom) {
                            m_img.m_y = bottom - m_img.m_centerY;
                        }
                    }
                    break;

                case MODE_PENDANT:
                case MODE_IMAGE:
                default:
                    break;
            }
        }

        m_isTouch = false;
        m_isOddCtrl = false;
        m_target = null;

        UpdateUI();
    }

    @Override
    protected void EvenDown(MotionEvent event) {
        m_isTouch = true;
        m_isOddCtrl = false;

        switch (m_operateMode) {
            case MODE_ALL:
                m_target = m_origin;
                Init_ZM_Data(m_target, m_downX1, m_downY1, m_downX2, m_downY2);
                break;

            case MODE_FRAME:
                m_target = m_img;
                Init_MRZ_Data(m_target, m_downX1, m_downY1, m_downX2, m_downY2);
                break;

            case MODE_PENDANT: {
                int index = GetSelectIndex(m_pendantArr, (m_downX1 + m_downX2) / 2f, (m_downY1 + m_downY2) / 2f);
                if (index >= 0) {
                    m_target = m_pendantArr.get(index);
                    m_pendantArr.remove(index);
                    m_pendantArr.add(m_target);
                    m_pendantCurSel = m_pendantArr.size() - 1;
                    Init_MRZ_Data(m_target, m_downX1, m_downY1, m_downX2, m_downY2);
                    //通知主界面选中信息
                    m_cb.SelectPendant(m_pendantCurSel);
                    //更新界面
                    this.invalidate();
                } else {
                    if (m_pendantCurSel >= 0) {
                        if (m_pendantCurSel >= 0) {
                            m_pendantCurSel = -1;
                            //通知主界面选中信息
                            m_cb.SelectPendant(m_pendantCurSel);
                            //更新界面
                            this.invalidate();
                        }
                        m_target = null;
                    }
                }
                break;
            }

            case MODE_IMAGE:
            default:
                m_target = null;
                break;
        }
    }

    @Override
    protected void EvenMove(MotionEvent event) {
        if (m_isTouch && m_target != null) {
            switch (m_operateMode) {
                case MODE_ALL:
                    Run_ZM2(m_target, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    //更新界面
                    this.invalidate();
                    break;

                case MODE_FRAME:
                case MODE_PENDANT:
                    Run_MRZ(m_target, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    //更新界面
                    this.invalidate();
                    break;

                case MODE_IMAGE:
                default:
                    break;
            }
        }
    }

    @Override
    protected void EvenUp(MotionEvent event) {
        OddUp(event);
    }

    protected void GetOutputMatrix(Matrix matrix, ShapeEx item, Bitmap bmp) {
        float[] src = {item.m_x + item.m_centerX, item.m_y + item.m_centerY};
        float[] dst = new float[2];
        GetShowPos(dst, src);

        matrix.reset();
        if (item.m_flip == Shape.Flip.VERTICAL) {
            float[] values = {1, 0, 0, 0, -1, bmp.getHeight(), 0, 0, 1};
            matrix.setValues(values);
        } else if (item.m_flip == Shape.Flip.HORIZONTAL) {
            float[] values = {-1, 0, bmp.getWidth(), 0, 1, 0, 0, 0, 1};
            matrix.setValues(values);
        }

        matrix.postTranslate(dst[0] - bmp.getWidth() / 2f, dst[1] - bmp.getHeight() / 2f);
        matrix.postScale(m_origin.m_scaleX * item.m_scaleX * item.m_w / (float) bmp.getWidth(), m_origin.m_scaleY * item.m_scaleY * item.m_h / (float) bmp.getHeight(), dst[0], dst[1]);
        matrix.postRotate(item.m_degree, dst[0], dst[1]);
    }

    protected boolean IsClickBtn(ShapeEx item, float x, float y) {
        boolean out = false;

        float[] values = new float[9];
        Matrix matrix = new Matrix();
        GetShowMatrixNoScale(matrix, item);
        matrix.getValues(values);
        if (ProcessorV2.IsSelectTarget(values, item.m_w, item.m_h, x, y)) {
            out = true;
        }

        return out;
    }

    /**
     * 此函数为重新创建一个指定size的bitmap,可以先清掉显示缓存再创建
     *
     * @param size
     * @return
     */
    public Bitmap GetOutputBmp(int size) {
        float whscale = (float) m_viewport.m_w / (float) m_viewport.m_h;
        float outW = size;
        float outH = outW / whscale;
        if (outH > size) {
            outH = size;
            outW = outH * whscale;
        }
        ShapeEx backup = (ShapeEx) m_origin.Clone();

        //设置输出位置
        m_origin.m_scaleX = outW / (float) m_viewport.m_w / m_viewport.m_scaleX;
        m_origin.m_scaleY = m_origin.m_scaleX;
        m_origin.m_x = (int) outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
        m_origin.m_y = (int) outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;

        Bitmap outBmp = Bitmap.createBitmap((int) outW, (int) outH, Config.ARGB_8888);
        Canvas canvas = new Canvas(outBmp);
        canvas.setDrawFilter(temp_filter);

        Bitmap tempBmp;
        canvas.drawColor(m_bkColor);
        if (m_bk != null) {
            tempBmp = m_cb.MakeOutputBK(m_bk.m_ex, (int) outW, (int) outH);
            if (tempBmp != null) {
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                BitmapShader shader = new BitmapShader(tempBmp, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
                temp_paint.setShader(shader);
                canvas.drawRect(0, 0, outW, outH, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        float tempW;
        float tempH;
        if (m_img != null) {
            tempW = m_origin.m_scaleX * m_img.m_scaleX * m_img.m_w;
            tempH = m_origin.m_scaleY * m_img.m_scaleY * m_img.m_h;
            tempBmp = m_cb.MakeOutputImg(m_img.m_ex, (int) (tempW + 0.5), (int) (tempH + 0.5));
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, m_img, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        if (m_frame != null) {
            tempW = m_origin.m_scaleX * m_frame.m_scaleX * m_frame.m_w;
            tempH = m_origin.m_scaleY * m_frame.m_scaleY * m_frame.m_h;
            tempBmp = m_cb.MakeOutputFrame(m_frame.m_ex, (int) (tempW + 0.5), (int) (tempH + 0.5));
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, m_frame, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        int len = m_pendantArr.size();
        ShapeEx temp;
        for (int i = 0; i < len; i++) {
            temp = m_pendantArr.get(i);
            tempW = m_origin.m_scaleX * temp.m_scaleX * temp.m_w;
            tempH = m_origin.m_scaleY * temp.m_scaleY * temp.m_h;
            tempBmp = m_cb.MakeOutputPendant(temp.m_ex, (int) (tempW + 0.5), (int) (tempH + 0.5));
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, temp, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        m_origin.Set(backup);

        return outBmp;
    }

    /**
     * 按照img最合适的尺寸输出,主要是img小于边框的情况<br/>
     * 此函数为重新创建一个指定size的bitmap,可以先清掉显示缓存再创建
     *
     * @param size
     * @return
     */
    public Bitmap GetOutputBmp2(int size) {
        float whscale = (float) m_viewport.m_w / (float) m_viewport.m_h;
        float outW = size;
        float outH = outW / whscale;
        if (outH > size) {
            outH = size;
            outW = outH * whscale;
        }
        ShapeEx backup = (ShapeEx) m_origin.Clone();

        //设置输出位置
        m_origin.m_scaleX = outW / (float) m_viewport.m_w / m_viewport.m_scaleX;
        m_origin.m_scaleY = m_origin.m_scaleX;
        m_origin.m_x = (int) outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
        m_origin.m_y = (int) outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;

        float tempW;
        float tempH;
        Bitmap imgBmp = null;
        if (m_img != null) {
            tempW = m_origin.m_scaleX * m_img.m_scaleX * m_img.m_w;
            tempH = m_origin.m_scaleY * m_img.m_scaleY * m_img.m_h;
            imgBmp = m_cb.MakeOutputImg(m_img.m_ex, (int) (tempW + 0.5), (int) (tempH + 0.5));
            if (imgBmp != null && imgBmp.getWidth() > 0 && imgBmp.getHeight() > 0) {
                if (Math.max(tempW, tempH) > Math.max(imgBmp.getWidth(), imgBmp.getHeight())) {
                    //修正(图片小于输出size)
                    m_origin.m_scaleX = (float) imgBmp.getWidth() / (m_img.m_scaleX * m_img.m_w);
                    m_origin.m_scaleY = m_origin.m_scaleX;
                    outW = m_origin.m_scaleX * (float) m_viewport.m_w * m_viewport.m_scaleX;
                    outH = outW / whscale;
                    m_origin.m_x = (int) outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
                    m_origin.m_y = (int) outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;
                }
            } else {
                imgBmp = null;
            }
        }

        Bitmap outBmp = Bitmap.createBitmap((int) outW, (int) outH, Config.ARGB_8888);
        Canvas canvas = new Canvas(outBmp);
        canvas.setDrawFilter(temp_filter);

        Bitmap tempBmp;
        canvas.drawColor(m_bkColor);
        if (m_bk != null) {
            temp_paint.reset();
            temp_paint.setAntiAlias(true);
            temp_paint.setFilterBitmap(true);
            tempBmp = m_cb.MakeOutputBK(m_bk.m_ex, (int) outW, (int) outH);
            if (tempBmp != null) {
                BitmapShader shader = new BitmapShader(tempBmp, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
                temp_paint.setShader(shader);
                canvas.drawRect(0, 0, outW, outH, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        if (m_img != null && imgBmp != null) {
            tempBmp = imgBmp;
            imgBmp = null;
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, m_img, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        if (m_frame != null) {
            tempW = m_origin.m_scaleX * m_frame.m_scaleX * m_frame.m_w;
            tempH = m_origin.m_scaleY * m_frame.m_scaleY * m_frame.m_h;
            tempBmp = m_cb.MakeOutputFrame(m_frame.m_ex, (int) (tempW + 0.5), (int) (tempH + 0.5));
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, m_frame, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        int len = m_pendantArr.size();
        ShapeEx temp;
        for (int i = 0; i < len; i++) {
            temp = m_pendantArr.get(i);
            tempW = m_origin.m_scaleX * temp.m_scaleX * temp.m_w;
            tempH = m_origin.m_scaleY * temp.m_scaleY * temp.m_h;
            tempBmp = m_cb.MakeOutputPendant(temp.m_ex, (int) (tempW + 0.5), (int) (tempH + 0.5));
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, temp, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        m_origin.Set(backup);

        return outBmp;
    }

    /**
     * 直接用显示的图片输出,不能清理显示缓存
     *
     * @return
     */
    public Bitmap GetOutputBmp() {
        int size = m_img.m_w > m_img.m_h ? m_img.m_w : m_img.m_h;

        float whscale = (float) m_viewport.m_w / (float) m_viewport.m_h;
        float outW = size;
        float outH = outW / whscale;
        if (outH > size) {
            outH = size;
            outW = outH * whscale;
        }
        ShapeEx backup = (ShapeEx) m_origin.Clone();

        //设置输出位置
        m_origin.m_scaleX = outW / (float) m_viewport.m_w / m_viewport.m_scaleX;
        m_origin.m_scaleY = m_origin.m_scaleX;
        m_origin.m_x = (int) outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
        m_origin.m_y = (int) outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;

        Bitmap outBmp = Bitmap.createBitmap((int) outW, (int) outH, Config.ARGB_8888);
        Canvas canvas = new Canvas(outBmp);
        canvas.setDrawFilter(temp_filter);

        Bitmap tempBmp;
        canvas.drawColor(m_bkColor);
        if (m_bk != null) {
            temp_paint.reset();
            temp_paint.setAntiAlias(true);
            temp_paint.setFilterBitmap(true);
            tempBmp = m_bk.m_bmp;
            if (tempBmp != null) {
                BitmapShader shader = new BitmapShader(tempBmp, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
                temp_paint.setShader(shader);
                canvas.drawRect(0, 0, outW, outH, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        if (m_img != null) {
            tempBmp = m_img.m_bmp;
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, m_img, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        if (m_frame != null) {
            tempBmp = m_frame.m_bmp;
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, m_frame, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        int len = m_pendantArr.size();
        ShapeEx temp;
        for (int i = 0; i < len; i++) {
            temp = m_pendantArr.get(i);
            tempBmp = temp.m_bmp;
            if (tempBmp != null) {
                GetOutputMatrix(temp_matrix, temp, tempBmp);
                temp_paint.reset();
                temp_paint.setAntiAlias(true);
                temp_paint.setFilterBitmap(true);
                canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
                tempBmp.recycle();
                tempBmp = null;
            }
        }

        m_origin.Set(backup);

        return outBmp;
    }

    /**
     * @param info
     * @param bmp  可以为null,为null时调用默认回调生成图片
     * @return index值, 失败-1
     */
    public int AddPendant(Object info, Bitmap bmp) {
        if (GetPendantIdleNum() > 0) {
            ShapeEx item = new ShapeEx();
            if (bmp != null) {
                item.m_bmp = bmp;
            } else {
                item.m_bmp = m_cb.MakeShowPendant(info, m_origin.m_w, m_origin.m_h);
            }
            if (item.m_bmp != null) {
                item.m_w = item.m_bmp.getWidth();
                item.m_h = item.m_bmp.getHeight();
                item.m_centerX = (float) item.m_w / 2f;
                item.m_centerY = (float) item.m_h / 2f;
                item.m_x = (float) m_origin.m_w / 2f - item.m_centerX;
                item.m_y = (float) m_origin.m_h / 2f - item.m_centerY;
                item.m_ex = info;

                //控制缩放比例
                item.DEF_SCALE = item.m_scaleX;
                {
                    float scale1 = (float) m_origin.m_w * def_pendant_max_scale / (float) item.m_w;
                    float scale2 = (float) m_origin.m_h * def_pendant_max_scale / (float) item.m_h;
                    item.MAX_SCALE = (scale1 > scale2) ? scale2 : scale1;

                    scale1 = (float) m_origin.m_w * def_pendant_min_scale / (float) item.m_w;
                    scale2 = (float) m_origin.m_h * def_pendant_min_scale / (float) item.m_h;
                    item.MIN_SCALE = (scale1 > scale2) ? scale2 : scale1;
                }

                m_pendantArr.add(item);

                return m_pendantArr.size() - 1;
            }
        }

        return -1;
    }

    /**
     * @param item
     * @return index值, 失败-1
     */
    public int AddPendant2(ShapeEx item) {
        if (GetPendantIdleNum() > 0) {
            m_pendantArr.add(item);

            return m_pendantArr.size() - 1;
        }

        return -1;
    }

    public void SetSelPendant(int index) {
        if (index >= 0 && index < m_pendantArr.size()) {
            m_pendantCurSel = index;
        } else {
            m_pendantCurSel = -1;
        }

        m_isOddCtrl = false;
        m_isTouch = false;
        m_target = null;

        UpdateUI();
    }

    public int GetSelPendant() {
        return m_pendantCurSel;
    }

    public void UpdateUI() {
        UpdateUI(getWidth(), getHeight());
    }

    protected void UpdateUI(int w, int h) {
        m_invalidate = true;
        this.invalidate();

        if (m_origin != null && m_viewport != null) {
            switch (m_layoutMode) {
                case LAYOUT_MODE_MATCH_PARENT: {
                    float showW = m_origin.m_scaleX * m_viewport.m_w * m_viewport.m_scaleX;
                    float showH = m_origin.m_scaleY * m_viewport.m_h * m_viewport.m_scaleY;
                    if (showW > 0 && showH > 0) {
                        float s1 = w / showW;
                        float s2 = h / showH;
                        float s = s1 < s2 ? s1 : s2;
                        m_origin.m_x = (w - m_origin.m_w) / 2f;
                        m_origin.m_y = (h - m_origin.m_h) / 2f;
                        m_origin.m_scaleX *= s;
                        m_origin.m_scaleY = m_origin.m_scaleX;
                    }
                    break;
                }

                case LAYOUT_MODE_WRAP_TOP: {
                    //待续...
                    break;
                }

                default:
                    break;
            }
        }
    }

    public void UpdateViewport() {
        if (m_frame != null) {
            m_viewport.m_w = m_frame.m_w;
            m_viewport.m_h = m_frame.m_h;
            m_viewport.m_centerX = m_frame.m_centerX;
            m_viewport.m_centerY = m_frame.m_centerY;
            m_viewport.m_x = (m_origin.m_w - m_viewport.m_w) / 2f;
            m_viewport.m_y = (m_origin.m_h - m_viewport.m_h) / 2f;
            {
                float scale1 = (float) m_origin.m_w / (float) m_viewport.m_w;
                float scale2 = (float) m_origin.m_h / (float) m_viewport.m_h;
                m_viewport.m_scaleX = (scale1 > scale2) ? scale2 : scale1;
                m_viewport.m_scaleY = m_viewport.m_scaleX;
            }
        } else if (m_img != null) {
            m_viewport.m_w = m_img.m_w;
            m_viewport.m_h = m_img.m_h;
            m_viewport.m_centerX = m_img.m_centerX;
            m_viewport.m_centerY = m_img.m_centerY;
            m_viewport.m_x = (m_origin.m_w - m_viewport.m_w) / 2f;
            m_viewport.m_y = (m_origin.m_h - m_viewport.m_h) / 2f;
            {
                float scale1 = (float) m_origin.m_w / (float) m_viewport.m_w;
                float scale2 = (float) m_origin.m_h / (float) m_viewport.m_h;
                m_viewport.m_scaleX = (scale1 > scale2) ? scale2 : scale1;
                m_viewport.m_scaleY = m_viewport.m_scaleX;
            }
        } else {
            m_viewport.m_w = m_origin.m_w;
            m_viewport.m_h = m_origin.m_h;
            m_viewport.m_centerX = m_origin.m_centerX;
            m_viewport.m_centerY = m_origin.m_centerY;
            m_viewport.m_x = 0;
            m_viewport.m_y = 0;
            m_viewport.m_scaleX = 1;
            m_viewport.m_scaleY = 1;
        }
    }

    public void SetBkColor(int color) {
        m_bkColor = color;
        m_bk = null;
    }

    /**
     * @param info
     * @param bmp  可以为null,为null时调用默认回调生成图片
     */
    public void SetBkBmp(Object info, Bitmap bmp) {
        m_bkColor = 0;
        m_bk = new ShapeEx();
        if (bmp != null) {
            m_bk.m_bmp = bmp;
        } else {
            if (m_viewport.m_w > 0 && m_viewport.m_h > 0) {
                m_bk.m_bmp = m_cb.MakeShowBK(info, (int) Math.ceil((float) m_viewport.m_w * m_viewport.m_scaleX), (int) Math.ceil(m_viewport.m_h * m_viewport.m_scaleY));
            } else {
                m_bk.m_bmp = m_cb.MakeShowBK(info, m_origin.m_w, m_origin.m_h);
            }
        }
        m_bk.m_ex = info;
    }

    public void SetOperateMode(int mode) {
        m_operateMode = mode;
        m_pendantCurSel = -1;
        m_cb.SelectPendant(m_pendantCurSel);

        //重置基坐标
        m_origin.m_x = 0;
        m_origin.m_y = 0;
        m_origin.m_scaleX = 1;
        m_origin.m_scaleY = 1;
    }

    public void SetLayoutMode(int mode) {
        m_layoutMode = mode;
    }

    /**
     * @param info
     * @param bmp  可以为null,为null时调用默认回调生成图片
     */
    public void SetImg(Object info, Bitmap bmp) {
        m_img = new ShapeEx();
        if (bmp != null) {
            m_img.m_bmp = bmp;
        } else {
            m_img.m_bmp = m_cb.MakeShowImg(info, m_origin.m_w, m_origin.m_h);
        }
        m_img.m_w = m_img.m_bmp.getWidth();
        m_img.m_h = m_img.m_bmp.getHeight();
        m_img.m_centerX = (float) m_img.m_w / 2f;
        m_img.m_centerY = (float) m_img.m_h / 2f;
        m_img.m_x = m_origin.m_centerX - m_img.m_centerX;
        m_img.m_y = m_origin.m_centerY - m_img.m_centerY;
        {
            float scale1 = (float) m_origin.m_w / (float) m_img.m_w;
            float scale2 = (float) m_origin.m_h / (float) m_img.m_h;
            m_img.m_scaleX = (scale1 > scale2) ? scale2 : scale1;
            m_img.m_scaleY = m_img.m_scaleX;
        }
        m_img.m_ex = info;

        //控制缩放比例
        m_img.DEF_SCALE = m_img.m_scaleX;
        {
            float scale1 = (float) m_origin.m_w * def_img_max_scale / (float) m_img.m_w;
            float scale2 = (float) m_origin.m_h * def_img_max_scale / (float) m_img.m_h;
            m_img.MAX_SCALE = (scale1 > scale2) ? scale2 : scale1;

            scale1 = (float) m_origin.m_w * def_img_min_scale / (float) m_img.m_w;
            scale2 = (float) m_origin.m_h * def_img_min_scale / (float) m_img.m_h;
            m_img.MIN_SCALE = (scale1 > scale2) ? scale2 : scale1;
        }

        UpdateViewport();
    }

    public void SetImg2(ShapeEx item) {
        m_img = item;

        UpdateViewport();
    }

    /**
     * @param info
     * @param bmp  可以为null,为null时调用默认回调生成图片
     */
    public void SetFrame(Object info, Bitmap bmp) {
        m_frame = new ShapeEx();
        if (bmp != null) {
            m_frame.m_bmp = bmp;
        } else {
            m_frame.m_bmp = m_cb.MakeShowFrame(info, m_origin.m_w, m_origin.m_h);
        }
        m_frame.m_w = m_frame.m_bmp.getWidth();
        m_frame.m_h = m_frame.m_bmp.getHeight();
        m_frame.m_centerX = (float) m_frame.m_w / 2f;
        m_frame.m_centerY = (float) m_frame.m_h / 2f;
        m_frame.m_x = (float) m_origin.m_w / 2f - m_frame.m_centerX;
        m_frame.m_y = (float) m_origin.m_h / 2f - m_frame.m_centerY;
        {
            float scale1 = (float) m_origin.m_w / (float) m_frame.m_w;
            float scale2 = (float) m_origin.m_h / (float) m_frame.m_h;
            m_frame.m_scaleX = (scale1 > scale2) ? scale2 : scale1;
            m_frame.m_scaleY = m_frame.m_scaleX;
        }
        m_frame.m_ex = info;

        UpdateViewport();
    }

    public void SetFrame2(ShapeEx item) {
        m_frame = item;

        UpdateViewport();
    }

    public ShapeEx GetCurrentSelPendantItem() {
        ShapeEx out = null;
        if (m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()) {
            out = m_pendantArr.get(m_pendantCurSel);
        }

        return out;
    }

    public ShapeEx DelPendant() {
        ShapeEx out = null;

        if (m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()) {
            out = m_pendantArr.remove(m_pendantCurSel);

            m_pendantCurSel = m_pendantArr.size() - 1;
            m_cb.SelectPendant(m_pendantCurSel);
        }

        return out;
    }

    public void DelAllPendant() {
        if (m_pendantArr != null) {
            if (m_pendantArr.size() > 0) {
                m_pendantArr.clear();

                m_pendantCurSel = -1;
                m_cb.SelectPendant(m_pendantCurSel);
            }
        }
    }

    public void ReleaseMem() {
        ClearViewBuffer();

//		if(m_img != null && m_img.m_bmp != null)
//		{
//			m_img.m_bmp.recycle();
//			m_img.m_bmp = null;
//		}

        if (m_frame != null && m_frame.m_bmp != null) {
            m_frame.m_bmp.recycle();
            m_frame.m_bmp = null;
        }

        int len = m_pendantArr.size();
        ShapeEx temp;
        for (int i = 0; i < len; i++) {
            temp = m_pendantArr.get(i);
            if (temp.m_bmp != null) {
                temp.m_bmp.recycle();
                temp.m_bmp = null;
            }
        }
    }

    public void CreateViewBuffer() {
        ClearViewBuffer();

        m_invalidate = true;
        m_drawable = true;
    }

    public void ClearViewBuffer() {
        if (m_drawBuffer != null) {
            m_drawBuffer.recycle();
            m_drawBuffer = null;
        }
        m_drawable = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = View.MeasureSpec.getSize(widthMeasureSpec);
        int h = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(w, h);

        UpdateUI(w, h);
    }

    public static interface ControlCallback {
        public Bitmap MakeShowImg(Object info, int frW, int frH);

        public Bitmap MakeOutputImg(Object info, int outW, int outH);

        public Bitmap MakeShowFrame(Object info, int frW, int frH);

        public Bitmap MakeOutputFrame(Object info, int outW, int outH);

        public Bitmap MakeShowBK(Object info, int frW, int frH);

        public Bitmap MakeOutputBK(Object info, int outW, int outH);

        public Bitmap MakeShowPendant(Object info, int frW, int frH);

        public Bitmap MakeOutputPendant(Object info, int outW, int outH);

        public void SelectPendant(int index);
    }
}
