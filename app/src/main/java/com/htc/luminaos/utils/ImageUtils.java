package com.htc.luminaos.utils;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageUtils {

    private static String TAG = "ImageUtils";

    /**
     * 计算inSampleSize值，对图片进行缩放处理
     *
     * @param options   用于获取原图的长宽
     * @return 返回计算后的inSampleSize值
     */
    public static int calculateInSampleSize(BitmapFactory.Options options) {
        // 原图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        // 获取设备的屏幕宽度
        float widthPixels = (float) Resources.getSystem().getDisplayMetrics().widthPixels;
        // 计算比例，避免除法结果过小
        float scaleFactor = widthPixels / 1920f;
        float x =135f*scaleFactor;
        float y = 207f*scaleFactor;
        float inSampleSize = 1.0f;
        if (height > x || width > y) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // 计算inSampleSize值
            while ((halfHeight / inSampleSize) >= x && (halfWidth / inSampleSize) >= y) {
                inSampleSize *= 2;
            }
        }
        Log.d(TAG, " 图片缩略图 calculateInSampleSize options.inSampleSize " + options.inSampleSize + " inSampleSize " +inSampleSize);
        return (int)inSampleSize;
    }
}
