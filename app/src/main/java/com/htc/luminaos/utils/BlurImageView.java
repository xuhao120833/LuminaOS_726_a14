package com.htc.luminaos.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Author:
 * Date:
 * Description:
 */
public class BlurImageView {
    /** 水平方向模糊度 */
    public static float HRADIUS = 10;
    /** 竖直方向模糊度 */
    public static float VRADIUS = 10;
    /** 模糊迭代度 */
    public static int ITERATIONS = 10;

    public static final int MAX_BITMAP_SIZE = 50 * 1024 * 1024; //原生系统的限制为100 * 1024 * 1024，这里改得更小一点

    private static String TAG = "BlurImageView";

    /**
     * 根据bitmap设置高斯模糊
     * @param bmp:bitmap参数
     * @return
     */
//    public static Bitmap BoxBlurFilter(Bitmap bmp) {
//        Log.d(TAG,"执行BoxBlurFilter前");
//        int width = bmp.getWidth();
//        int height = bmp.getHeight();
//
//        //判断图片大小，如果超过限制就做缩小处理
//        if(width*height*4>=MAX_BITMAP_SIZE) {
//            bmp=narrowBitmap(bmp);
//            width = bmp.getWidth();
//            height = bmp.getHeight();
//        }
//        //缩小完毕
//
//        Log.d(TAG,"执行BoxBlurFilter inPixels前");
//        int[] inPixels = new int[width * height];
//        Log.d(TAG,"执行BoxBlurFilter inPixels后");
//        Log.d(TAG,"执行BoxBlurFilter outPixels前");
//        int[] outPixels = new int[width * height];
//        Log.d(TAG,"执行BoxBlurFilter outPixels后");
//        Log.d(TAG,"执行BoxBlurFilter Bitmap.createBitmap前");
//        Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
//        Log.d(TAG,"执行BoxBlurFilter Bitmap.createBitmap后");
//        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
//        for (int i = 0; i < ITERATIONS; i++) {
//            blur(inPixels, outPixels, width, height, HRADIUS);
//            blur(outPixels, inPixels, height, width, VRADIUS);
//        }
//        blurFractional(inPixels, outPixels, width, height, HRADIUS);
//        blurFractional(outPixels, inPixels, height, width, VRADIUS);
//        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
//        return bitmap;
//    }

//    public static Bitmap narrowBitmap(Bitmap bitmap) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int inSampleSize = 1;
//        int max = 50 * 1024 * 1024;
//        while (width * height * 6 / inSampleSize > max){
//            inSampleSize *= 2;
//        }
//        if (inSampleSize > 1){
//            Matrix m2 = new Matrix();
//            m2.setScale(1 / (float)inSampleSize , 1 / (float)inSampleSize);
//            bitmap = Bitmap.createBitmap(bitmap,0,0,width,height,m2,false);
//        }
//        return bitmap;
//    }

    public static Bitmap narrowBitmap(Bitmap bitmap) {
        int maxWidth = 1920;  // 最大宽度限制
        int maxHeight = 1080; // 最大高度限制
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > maxWidth || height > maxHeight) {
            float scaleWidth = (float) maxWidth / width;
            float scaleHeight = (float) maxHeight / height;
            float scale = Math.min(scaleWidth, scaleHeight);

            // 使用硬件加速的 createScaledBitmap
            return Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
        }
        return bitmap;
    }

    /**
     * 根据ImageView设置高斯模糊
     * @param img:ImageView
     * @return
     */
    public static void BoxBlurFilter(ImageView img) {
        Bitmap bmp =((BitmapDrawable)img.getDrawable()).getBitmap();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < ITERATIONS; i++) {
            blur(inPixels, outPixels, width, height, HRADIUS);
            blur(outPixels, inPixels, height, width, VRADIUS);
        }
        blurFractional(inPixels, outPixels, width, height, HRADIUS);
        blurFractional(outPixels, inPixels, height, width, VRADIUS);
        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
        Drawable drawable = new BitmapDrawable(bitmap);
        img.setImageDrawable(drawable);
    }

    /**
     * 根据项目资源文件图片返回高斯模糊drawable
     * @param context:上下文
     * @param res:资源id
     */
//    public static Bitmap BoxBlurFilter(Context context, int res) {
//        Bitmap bmp= BitmapFactory.decodeResource(context.getResources(), res);
//        return BoxBlurFilter(bmp);
//    }

    /**
     * 根据drawable返回高斯模糊
     * @param drawable
     * @return
     */
//    public static Bitmap BoxBlurFilter(Drawable drawable) {
//        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
//        return BoxBlurFilter(bitmap);
//    }

    /**
     * 核心代码
     * @param in
     * @param out
     * @param width
     * @param height
     * @param radius
     */
    public static void blur(int[] in, int[] out, int width, int height,
                            float radius) {
        int widthMinus1 = width - 1;
        int r = (int) radius;
        int tableSize = 2 * r + 1;
        int divide[] = new int[256 * tableSize];

        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -r; i <= r; i++) {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++) {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
                        | (divide[tg] << 8) | divide[tb];

                int i1 = x + r + 1;
                if (i1 > widthMinus1)
                    i1 = widthMinus1;
                int i2 = x - r;
                if (i2 < 0)
                    i2 = 0;
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    public static void blurFractional(int[] in, int[] out, int width,
                                      int height, float radius) {
        radius -= (int) radius;
        float f = 1.0f / (1 + 2 * radius);
        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;

            out[outIndex] = in[0];
            outIndex += height;
            for (int x = 1; x < width - 1; x++) {
                int i = inIndex + x;
                int rgb1 = in[i - 1];
                int rgb2 = in[i];
                int rgb3 = in[i + 1];

                int a1 = (rgb1 >> 24) & 0xff;
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                int a2 = (rgb2 >> 24) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                int a3 = (rgb3 >> 24) & 0xff;
                int r3 = (rgb3 >> 16) & 0xff;
                int g3 = (rgb3 >> 8) & 0xff;
                int b3 = rgb3 & 0xff;
                a1 = a2 + (int) ((a1 + a3) * radius);
                r1 = r2 + (int) ((r1 + r3) * radius);
                g1 = g2 + (int) ((g1 + g3) * radius);
                b1 = b2 + (int) ((b1 + b3) * radius);
                a1 *= f;
                r1 *= f;
                g1 *= f;
                b1 *= f;
                out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
                outIndex += height;
            }
            out[outIndex] = in[width - 1];
            inIndex += width;
        }
    }

    public static int clamp(int x, int a, int b) {
        return (x < a) ? a : (x > b) ? b : x;
    }


}