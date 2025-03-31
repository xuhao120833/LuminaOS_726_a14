package com.htc.luminaos.utils;

import android.util.Log;
import android.view.View;
import android.app.ActionBar;
import android.content.Context;
import android.view.WindowManager;

public class AddViewToScreen {
    public static WindowManager wm;
    private Context mcontext;
    private static String TAG = "AddViewToScreen";

    public void addView(View v, WindowManager.LayoutParams p) {
        // 添加一个view之前先尝试删除这个view, 避免重复添加
        Log.d(TAG, "v.isAttachedToWindow() " + String.valueOf(v.isAttachedToWindow()));
        if (v.isAttachedToWindow()) {
            // 视图已经添加到屏幕上
            try {
                v.clearFocus();
                wm.removeViewImmediate(v);
            } catch (Exception e) {
                Log.d(TAG, "addView : removeView has Error： " + e.getMessage());
            }
        }
        v.clearFocus();
        wm.addView(v, p);
    }

    public void setContext(Context context) {
        mcontext = context;
        wm = mcontext.getSystemService(WindowManager.class);
    }

    public void clearView(View v) {
        if (v != null) {
            Log.d(TAG, "clearView");
            try {
                wm.removeViewImmediate(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
