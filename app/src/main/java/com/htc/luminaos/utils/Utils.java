package com.htc.luminaos.utils;

import android.graphics.drawable.Drawable;

import com.htc.luminaos.R;
import com.htc.luminaos.entry.SpecialApps;
import com.htc.luminaos.settings.utils.T;

import java.util.ArrayList;

public class Utils {
    public static boolean hasfocus = false;

    public static boolean hasUsbDevice = false;

    //首页默认背景resId,无配置默认-1
    public static int mainBgResId = -1;

    public static int usbDevicesNumber = 0;

    //默认背景使用的ArrayList
    public static ArrayList<Drawable> drawables = new ArrayList<>();

    public static final int REQUEST_CODE_PICK_IMAGE = 1;

    //一个全局的特定IP APP信息
    public static SpecialApps specialApps = null;

    public static int[] drawablesId = {
            R.drawable.background_main,
            R.drawable.background_custom,
            R.drawable.background1,
            R.drawable.background5,
            R.drawable.background10,
            R.drawable.background11,
            R.drawable.background12,
            R.drawable.background13,
    };

}
