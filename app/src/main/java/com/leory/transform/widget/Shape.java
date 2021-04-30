package com.leory.transform.widget;

import android.graphics.Bitmap;

public class Shape implements Cloneable {
    private static int SOLE_NUM = 0x0001;
    public int m_soleId; //当前类的唯一id(用于区分不同对象)
    public float m_x = 0;
    public float m_y = 0;
    public float m_degree = 0f;
    public float m_scaleX = 1f;
    public float m_scaleY = 1f;
    public Flip m_flip = Flip.NONE;
    public transient Bitmap m_bmp = null;

    public Shape() {
        m_soleId = GetSoleId();
    }

    protected int GetSoleId() {
        return ++SOLE_NUM;
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
