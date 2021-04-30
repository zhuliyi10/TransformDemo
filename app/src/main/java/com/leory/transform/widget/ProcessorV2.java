package com.leory.transform.widget;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class ProcessorV2 {
    /*
     * 以下为旋转翻转图片
     */
    public static int CONVERT_FLIP_H = 0x0001;
    public static int CONVERT_FLIP_V = 0x0002;
    public static int CONVERT_ROTATE_90 = 0x0010;
    public static int CONVERT_ROTATE_180 = 0x0020;
    public static int CONVERT_ROTATE_270 = 0x0040;
    /*
     * 以下为冲印拼图
     */
    private static int Grid_s_imgW = 0;
    private static int Grid_s_imgH = 0;
    private static int Grid_s_max = 0;
    private static int Grid_s_index = 0;


    public static void Init() {
    }

    public static native int Step1CreateImage(int w, int h, int border);

    public static native int Step2AddColorBk(int argb);

    public static native int Step2AddBmpBk(Bitmap bk);

    public static native int Step3AddResImg(Bitmap img);

    public static native int Step4AddMiddleFrame(Bitmap frame);

    public static native int Step5AddTopFrame(Bitmap frame);

    public static native int Step6AddBottomFrame(Bitmap frame);

    public static int Step7SaveImageSafe(String path, int quality) {
        if (path != null && !path.equals("") && !path.endsWith(File.separator)) {
            {
                File file = new File(path).getParentFile();
                if (file != null) {
                    file.mkdirs();
                }
            }

            return Step7SaveImage(path, quality);
        }
        return 0;
    }

    private static native int Step7SaveImage(String path, int quality);

    public static native int Step8DestroyImage();

    public static native byte[] Base64Encode1(byte[] data, boolean urlSafe);

    /**
     * @param table 长度为65,最后一位为补全位
     * @param data
     * @return
     */
    public static native byte[] Base64Encode2(byte[] table, byte[] data);

    public static native byte[] Base64Decode1(byte[] data, boolean urlSafe);

    /**
     * @param table 长度为65,最后一位为补全位
     * @param data
     * @return
     */
    public static native byte[] Base64Decode2(byte[] table, byte[] data);

    public static native boolean IsSelectTarget(float[] matrixArr, float w, float h, float x, float y);

    public static int Grid_Step1CreateImageSafe(int w, int h, int numW, int numH, int border) {
        if (w > 0 && h > 0 && numW > 0 && numH > 0 && border >= 0) {
            Grid_s_imgW = w;
            Grid_s_imgH = h;
            Grid_s_max = numW * numH;
            Grid_s_index = 0;

            return Grid_Step1CreateImage(w, h, numW, numH, border);
        }

        return -1;
    }

    private static native int Grid_Step1CreateImage(int w, int h, int numW, int numH, int border);

    public static int Grid_Step2AddImageSafe(Bitmap img, Grid_Callback cb) {
        if (Grid_s_index < Grid_s_max) {
            Grid_s_index++;
            return Grid_Step2AddImage(cb.FixImage(img, Grid_s_imgW, Grid_s_imgH));
        } else {
            return -1;
        }
    }

    private static native int Grid_Step2AddImage(Bitmap img);

    public static int Grid_Step3SaveImageSafe(String path, int quality) {
        if (path != null && !path.equals("") && !path.endsWith(File.separator)) {
            {
                File file = new File(path).getParentFile();
                if (file != null) {
                    file.mkdirs();
                }
            }

            return Grid_Step3SaveImage(path, quality);
        }
        return -1;
    }

    private static native int Grid_Step3SaveImage(String path, int quality);

    public static native int Grid_Step4DestroyImage();

    public static native int Fill_Step1CreateImage(int w, int h);

    public static int Fill_Step1CreateImageSafe(String path) {
        if (path != null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            if (opts.outMimeType != null && !opts.outMimeType.equals("")) {
                if (opts.outMimeType.equals("image/jpeg") || opts.outMimeType.equals("image/png") || opts.outMimeType.equals("image/bmp")) {
                    return Fill_Step1CreateImage(path);
                }
            }
        }
        return -1;
    }

    private static native int Fill_Step1CreateImage(String path);

    public static native int Fill_Step2DrawImage(int x, int y, Bitmap bmp);

    public static int Fill_Step3SaveImageSafe(String path, int quality) {
        if (path != null && !path.equals("") && !path.endsWith(File.separator)) {
            {
                File file = new File(path).getParentFile();
                if (file != null) {
                    file.mkdirs();
                }
            }

            return Fill_Step3SaveImage(path, quality);
        }
        return -1;
    }

    private static native int Fill_Step3SaveImage(String path, int quality);

    public static native int Fill_Step4DestroyImage();

    public static int ConvertImageSafe(String path, int flag, String savePath) {
        if (new File(path).exists()) {
            if (savePath != null && !savePath.equals("") && !savePath.endsWith(File.separator)) {
                {
                    File file = new File(savePath).getParentFile();
                    if (file != null) {
                        file.mkdirs();
                    }
                }

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, opts);
                if (opts.outMimeType != null && !opts.outMimeType.equals("")) {
                    if (opts.outMimeType.equals("image/jpeg") || opts.outMimeType.equals("image/png") || opts.outMimeType.equals("image/bmp")) {
                        return ConvertImage(path, flag, savePath);
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 先做旋转后做翻转
     *
     * @param path
     * @param flag
     * @param savePath
     * @return
     */
    private static native int ConvertImage(String path, int flag, String savePath);

    public static interface Grid_Callback {
        public Bitmap FixImage(Bitmap bmp, int w, int h);
    }
}
