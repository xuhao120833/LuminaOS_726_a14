package com.htc.luminaos.activity;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.htc.luminaos.MyApplication;
import com.htc.luminaos.R;
import com.htc.luminaos.adapter.AppsAdapter;
import com.htc.luminaos.databinding.ActivityAppsBinding;
import com.htc.luminaos.entry.AppInfoBean;
import com.htc.luminaos.receiver.AppCallBack;
import com.htc.luminaos.receiver.AppReceiver;
import com.htc.luminaos.utils.AppUtils;
import com.htc.luminaos.widget.SpacesItemDecoration;

import java.util.List;

public class AppsActivity extends BaseActivity{

    private ActivityAppsBinding appsBinding;

    //app
    private IntentFilter appFilter=new IntentFilter();
    private AppReceiver appReceiver=null;

    private String TAG = "AppsActivity";

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what==1){
                List<AppInfoBean> infoBeans =(List<AppInfoBean>)  msg.obj;
                AppsAdapter appsAdapter = new AppsAdapter(AppsActivity.this,infoBeans,appsBinding.appsRv);
                appsBinding.appsRv.setAdapter(appsAdapter);
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appsBinding = ActivityAppsBinding.inflate(LayoutInflater.from(this));
        setContentView(appsBinding.getRoot());
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setLayout();
    }

    private void setLayout() {
        if (MyApplication.config.layout_select == 2 || MyApplication.config.layout_select == 3) {

            Typeface typeface = ResourcesCompat.getFont(this, R.font.arial);

            appsBinding.title.setTextColor(Color.BLACK);
            appsBinding.title.setTypeface(typeface,Typeface.BOLD);
            appsBinding.title.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.y_40));
            appsBinding.title.setLetterSpacing(0.05f);

        }
    }

    private void initView(){

        GridLayoutManager layoutManager = new GridLayoutManager(this,6);
        appsBinding.appsRv.setLayoutManager(layoutManager);
        appsBinding.appsRv.addItemDecoration(new SpacesItemDecoration(SpacesItemDecoration.px2dp(10),SpacesItemDecoration.px2dp(10),SpacesItemDecoration.px2dp(10),SpacesItemDecoration.px2dp(10)));
        //app
        appFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appFilter.addDataScheme("package");
        appReceiver=new AppReceiver(appCallBack);
        registerReceiver(appReceiver, appFilter);
    }

    private void initData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AppInfoBean> appInfoBeans = AppUtils.getApplicationMsg(AppsActivity.this);
                Message message = handler.obtainMessage();
                message.what=1;
                message.obj =appInfoBeans;
                handler.sendMessage(message);
            }
        }).start();
    }

    AppCallBack appCallBack = new AppCallBack() {
        @Override
        public void appChange(String packageName) {
            initData();
        }

        @Override
        public void appUnInstall(String packageName) {
            initData();
        }

        @Override
        public void appInstall(String packageName) {
            initData();
        }
    };


    @Override
    protected void onDestroy() {
        unregisterReceiver(appReceiver);
        super.onDestroy();
    }

}