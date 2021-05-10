package com.leory.transform;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.leory.transform.transform_view.BaseCoreView;
import com.leory.transform.transform_view.ShapeEx;

public class MainActivity extends AppCompatActivity {

    private BaseCoreView mCoreView;
    private TextView mTxtSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCoreView = findViewById(R.id.core_view);
        mTxtSave = findViewById(R.id.txt_save);
        mCoreView.post(() -> {
//            mCoreView.setNullTargetShape(100,100);
            mCoreView.setImgTargetShape(BitmapFactory.decodeResource(getResources(), R.mipmap.lindan));
            addPendant();
        });
        mTxtSave.setOnClickListener(v -> {
            Bitmap in = mCoreView.m_target.m_bmp;
            Bitmap inBitmap = Bitmap.createScaledBitmap(in, in.getWidth() * 5, in.getHeight() * 5, true);
            mCoreView.m_target.m_bmp = inBitmap;
            Bitmap outputBitmap = mCoreView.getOutputBitmap();
            mCoreView.m_target.m_bmp = in;
        });

    }

    private void addPendant() {
        ShapeEx item = new ShapeEx();
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.lindan);
        item.m_w = bmp.getWidth();
        item.m_h = bmp.getHeight();
        item.m_centerX = item.m_w / 2;
        item.m_centerY = item.m_h / 2;
        item.m_x = mCoreView.m_viewport.m_centerX - item.m_centerX + mCoreView.m_viewport.m_x;
        item.m_y = mCoreView.m_viewport.m_centerY - item.m_centerY + mCoreView.m_viewport.m_y;
        item.m_bmp = bmp;
        mCoreView.addPendant(item);
    }
}