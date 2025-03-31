package com.htc.luminaos.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;

import com.htc.luminaos.R;
import com.htc.luminaos.databinding.ActivityDisplaySettingsBinding;
import com.htc.luminaos.utils.AddViewToScreen;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class DisplaySettingsActivity extends BaseActivity implements View.OnKeyListener {

    ActivityDisplaySettingsBinding displaySettingsBinding;
    private AddViewToScreen mavts = new AddViewToScreen();
    private Context mContext;
    private static String TAG = "DisplaySettingsActivity";
    public WindowManager.LayoutParams lp;
    //窗口背景高斯模糊程度
    private int mBackgroundBlurRadius;
    private int mBackgroundCornersRadius;
    // 根据窗口高斯模糊功能是否开启来为窗口设置不同的不透明度
    private final int mWindowBackgroundAlphaWithBlur = 170;
    private final int mWindowBackgroundAlphaNoBlur = 255;
    private Drawable mWindowBackgroundDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mavts.setContext(getApplicationContext());
        //1、初始化LayoutParams描述工具
        initLayoutParams();

        //2、初始化View对象
        initView();
    }

    private void initLayoutParams() {
        lp = new WindowManager.LayoutParams();
        lp.format = PixelFormat.RGBA_8888;

        lp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        lp.width = (int)getResources().getDimension(R.dimen.x_400);
        lp.height = (int)getResources().getDimension(R.dimen.y_640);
        lp.gravity = Gravity.START;
        lp.x = (int)getResources().getDimension(R.dimen.x_27);
    }

    private void initView(){
        displaySettingsBinding = ActivityDisplaySettingsBinding.inflate(LayoutInflater.from(this));
        displaySettingsBinding.title.setOnKeyListener(this);
        mavts.addView(displaySettingsBinding.main,lp);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT )) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && mavts!=null && event.getAction() == KeyEvent.ACTION_UP) {
            mavts.clearView(displaySettingsBinding.main);
            finish();
        }
        return false;
    }
}