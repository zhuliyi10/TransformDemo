package com.leory.transform.transform_view;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.annotation.NonNull;

/**
 * 约定顺序：翻转-平移-放大-旋转
 *
 * @author POCO
 */
public class ShapeEx implements Cloneable {
    public float MAX_SCALE = 10f;
    public float MIN_SCALE = 0.1f;
    public int m_w;
    public int m_h;
    public float m_centerX;
    public float m_centerY;
    public float m_x = 0;
    public float m_y = 0;
    public float m_degree = 0f;
    public float m_scaleX = 1f;
    public float m_scaleY = 1f;
    public Flip m_flip = Flip.NONE;
    public transient Bitmap m_bmp = null;
    public Object m_ex;
    public transient Matrix m_matrix = new Matrix();

    public void SetScaleXY(float scaleX, float scaleY) {
        if (scaleX > MAX_SCALE) {
            m_scaleX = MAX_SCALE;
        } else if (scaleX < MIN_SCALE) {
            m_scaleX = MIN_SCALE;
        } else {
            m_scaleX = scaleX;
        }

        if (scaleY > MAX_SCALE) {
            m_scaleY = MAX_SCALE;
        } else if (scaleY < MIN_SCALE) {
            m_scaleY = MIN_SCALE;
        } else {
            m_scaleY = scaleY;
        }
    }


    @NonNull
    @Override
    protected ShapeEx clone() {
        try {
            return (ShapeEx) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public enum Flip {
        NONE(0),
        HORIZONTAL(1),
        VERTICAL(2),
        ;

        private final int m_value;

        Flip(int value) {
            m_value = value;
        }

        public int GetValue() {
            return m_value;
        }
    }
}
