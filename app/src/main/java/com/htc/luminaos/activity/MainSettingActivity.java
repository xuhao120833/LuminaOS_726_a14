package com.htc.luminaos.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.htc.luminaos.R;
import com.htc.luminaos.databinding.ActivityMainSettingBinding;
import com.htc.luminaos.databinding.ActivitySettingsCustomBinding;
import com.htc.luminaos.databinding.MainSettingsCustomBinding;
import com.htc.luminaos.databinding.SettingsCustomBinding;

public class MainSettingActivity extends BaseActivity {

    ActivityMainSettingBinding mainSettingBinding;


    SettingsCustomBinding settingsCustomBinding;

    MainSettingsCustomBinding mainSettingsCustomBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //定制逻辑
//        settingsCustomBinding = settingsCustomBinding.inflate(LayoutInflater.from(this));
//        setContentView(settingsCustomBinding.getRoot());
//        initViewCustom();

        //原生逻辑改进
        mainSettingsCustomBinding = MainSettingsCustomBinding.inflate(LayoutInflater.from(this));
        setContentView(mainSettingsCustomBinding.getRoot());
        initView();
        initData();

        //原生逻辑
//        mainSettingBinding = ActivityMainSettingBinding.inflate(LayoutInflater.from(this));
//        setContentView(mainSettingBinding.getRoot());
//        initView();
//        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(settingsCustomBinding != null) {
            mainSettingsCustomBinding.wifiTxt.setSelected(true);
            mainSettingsCustomBinding.btTxt.setSelected(true);
            mainSettingsCustomBinding.projectTxt.setSelected(true);
            mainSettingsCustomBinding.languageTxt.setSelected(true);
            mainSettingsCustomBinding.appsTxt.setSelected(true);
            mainSettingsCustomBinding.timeTxt.setSelected(true);
            mainSettingsCustomBinding.otherTxt.setSelected(true);
            mainSettingsCustomBinding.aboutTxt.setSelected(true);
        }
    }

    private void initViewCustom() {
        settingsCustomBinding.settingsWifi.setOnClickListener(this);
        settingsCustomBinding.settingsBt.setOnClickListener(this);
        settingsCustomBinding.settingsHot.setOnClickListener(this);
        settingsCustomBinding.settingsMode.setOnClickListener(this);
        settingsCustomBinding.settingsTrapezium.setOnClickListener(this);
        settingsCustomBinding.settingsZoom.setOnClickListener(this);
        settingsCustomBinding.settingsFocus.setOnClickListener(this);
        settingsCustomBinding.settingsSignal.setOnClickListener(this);
        settingsCustomBinding.settingsCast.setOnClickListener(this);
        settingsCustomBinding.settingsApp.setOnClickListener(this);
        settingsCustomBinding.settingsDate.setOnClickListener(this);
        settingsCustomBinding.settingsLanguage.setOnClickListener(this);
        settingsCustomBinding.settingsTypewriting.setOnClickListener(this);
        settingsCustomBinding.settingsVoice.setOnClickListener(this);
        settingsCustomBinding.settingsLight.setOnClickListener(this);
        settingsCustomBinding.settingsTemperature.setOnClickListener(this);
        settingsCustomBinding.settingsUpdate.setOnClickListener(this);
        settingsCustomBinding.settingsRecovery.setOnClickListener(this);
        settingsCustomBinding.settingsAbout.setOnClickListener(this);

        settingsCustomBinding.settingsWifi.setOnHoverListener(this);
        settingsCustomBinding.settingsBt.setOnHoverListener(this);
        settingsCustomBinding.settingsHot.setOnHoverListener(this);
        settingsCustomBinding.settingsMode.setOnHoverListener(this);
        settingsCustomBinding.settingsTrapezium.setOnHoverListener(this);
        settingsCustomBinding.settingsZoom.setOnHoverListener(this);
        settingsCustomBinding.settingsFocus.setOnHoverListener(this);
        settingsCustomBinding.settingsSignal.setOnHoverListener(this);
        settingsCustomBinding.settingsCast.setOnHoverListener(this);
        settingsCustomBinding.settingsApp.setOnHoverListener(this);
        settingsCustomBinding.settingsDate.setOnHoverListener(this);
        settingsCustomBinding.settingsLanguage.setOnHoverListener(this);
        settingsCustomBinding.settingsTypewriting.setOnHoverListener(this);
        settingsCustomBinding.settingsVoice.setOnHoverListener(this);
        settingsCustomBinding.settingsLight.setOnHoverListener(this);
        settingsCustomBinding.settingsTemperature.setOnHoverListener(this);
        settingsCustomBinding.settingsUpdate.setOnHoverListener(this);
        settingsCustomBinding.settingsRecovery.setOnHoverListener(this);
        settingsCustomBinding.settingsAbout.setOnHoverListener(this);
    }


    private void initView() {

        //原生逻辑改进UI
        mainSettingsCustomBinding.rlAbout.setOnClickListener(this);
        mainSettingsCustomBinding.rlAppsManager.setOnClickListener(this);
        mainSettingsCustomBinding.rlBluetooth.setOnClickListener(this);
        mainSettingsCustomBinding.rlDateTime.setOnClickListener(this);
        mainSettingsCustomBinding.rlLanguage.setOnClickListener(this);
        mainSettingsCustomBinding.rlOther.setOnClickListener(this);
        mainSettingsCustomBinding.rlProject.setOnClickListener(this);
        mainSettingsCustomBinding.rlWifi.setOnClickListener(this);

        mainSettingsCustomBinding.rlAbout.setOnHoverListener(this);
        mainSettingsCustomBinding.rlAppsManager.setOnHoverListener(this);
        mainSettingsCustomBinding.rlBluetooth.setOnHoverListener(this);
        mainSettingsCustomBinding.rlDateTime.setOnHoverListener(this);
        mainSettingsCustomBinding.rlLanguage.setOnHoverListener(this);
        mainSettingsCustomBinding.rlOther.setOnHoverListener(this);
        mainSettingsCustomBinding.rlProject.setOnHoverListener(this);
        mainSettingsCustomBinding.rlWifi.setOnHoverListener(this);

        mainSettingsCustomBinding.rlWifi.requestFocus();
        mainSettingsCustomBinding.rlWifi.requestFocusFromTouch();

        mainSettingsCustomBinding.wifiTxt.setSelected(true);
        mainSettingsCustomBinding.btTxt.setSelected(true);
        mainSettingsCustomBinding.projectTxt.setSelected(true);
        mainSettingsCustomBinding.languageTxt.setSelected(true);
        mainSettingsCustomBinding.appsTxt.setSelected(true);
        mainSettingsCustomBinding.timeTxt.setSelected(true);
        mainSettingsCustomBinding.otherTxt.setSelected(true);
        mainSettingsCustomBinding.aboutTxt.setSelected(true);

        //原生逻辑
//        mainSettingBinding.rlAbout.setOnClickListener(this);
//        mainSettingBinding.rlAppsManager.setOnClickListener(this);
//        mainSettingBinding.rlBluetooth.setOnClickListener(this);
//        mainSettingBinding.rlDateTime.setOnClickListener(this);
//        mainSettingBinding.rlLanguage.setOnClickListener(this);
//        mainSettingBinding.rlOther.setOnClickListener(this);
//        mainSettingBinding.rlProject.setOnClickListener(this);
//        mainSettingBinding.rlWifi.setOnClickListener(this);
//
//        mainSettingBinding.rlAbout.setOnHoverListener(this);
//        mainSettingBinding.rlAppsManager.setOnHoverListener(this);
//        mainSettingBinding.rlBluetooth.setOnHoverListener(this);
//        mainSettingBinding.rlDateTime.setOnHoverListener(this);
//        mainSettingBinding.rlLanguage.setOnHoverListener(this);
//        mainSettingBinding.rlOther.setOnHoverListener(this);
//        mainSettingBinding.rlProject.setOnHoverListener(this);
//        mainSettingBinding.rlWifi.setOnHoverListener(this);
//
//        mainSettingBinding.rlProject.requestFocus();
//        mainSettingBinding.rlProject.requestFocusFromTouch();

    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {

        //定制逻辑
//        switch (v.getId()) {
//            case R.id.settings_wifi:
//                startNewActivity(NetworkActivity.class);
//                break;
//            case R.id.settings_bt:
//                startNewActivity(BluetoothActivity.class);
//                break;
//            case R.id.settings_hot:
//                startNewActivity(HotspotActivity.class);
//                break;
//            case R.id.settings_mode:
//                startNewActivity(ProjectActivity.class);
//                break;
//            case R.id.settings_trapezium:
//                startNewActivity(CorrectionActivity.class);
//                break;
//            case R.id.settings_zoom:
//                startNewActivity(CorrectionActivity.class);
//                break;
//            case R.id.settings_focus:
//                startNewActivity(ProjectActivity.class);
//                break;
//            case R.id.settings_signal:
//                startSource("HDMI1");
//                break;
//            case R.id.settings_cast:
//                AppUtils.startNewApp(MainSettingActivity.this, "com.softwinner.miracastReceiver");
//                break;
//            case R.id.settings_app:
//                startNewActivity(AppsManagerActivity.class);
//                break;
//            case R.id.settings_date:
//                startNewActivity(DateTimeActivity.class);
//                break;
//            case R.id.settings_language:
//                startNewActivity(LanguageAndKeyboardActivity.class);
//                break;
//            case R.id.settings_typewriting:
//                startNewActivity(LanguageAndKeyboardActivity.class);
//                break;
//            case R.id.settings_voice:
////                startNewActivity(LanguageAndKeyboardActivity.class);
//                break;
//            case R.id.settings_light:
////                startNewActivity(LanguageAndKeyboardActivity.class);
//                break;
//            case R.id.settings_temperature:
////                startNewActivity(LanguageAndKeyboardActivity.class);
//                break;
//
//            case R.id.settings_update:
//                startNewActivity(AboutActivity.class);
//                break;
//
//            case R.id.settings_recovery:
//                startNewActivity(OtherSettingsActivity.class);
//                break;
//
//            case R.id.settings_about:
//                startNewActivity(AboutActivity.class);
//                break;
//        }

        //原生逻辑
        int id = v.getId();
        if (id == R.id.rl_wifi) {
            startNewActivity(NetworkActivity.class);
        } else if (id == R.id.rl_bluetooth) {
            startNewActivity(BluetoothActivity.class);
        } else if (id == R.id.rl_project) {
            startNewActivity(ProjectActivity.class);
        } else if (id == R.id.rl_apps_manager) {
            startNewActivity(AppsManagerActivity.class);
        } else if (id == R.id.rl_language) {
            startNewActivity(LanguageAndKeyboardActivity.class);
        } else if (id == R.id.rl_date_time) {
            startNewActivity(DateTimeActivity.class);
        } else if (id == R.id.rl_other) {
            startNewActivity(OtherSettingsActivity.class);
        } else if (id == R.id.rl_about) {
            startNewActivity(AboutActivity.class);
        }
    }

    private void startSource(String sourceName) {
        Intent intent_hdmi = new Intent();
        intent_hdmi.setComponent(new ComponentName("com.softwinner.awlivetv", "com.softwinner.awlivetv.MainActivity"));
        intent_hdmi.putExtra("input_source", sourceName);
        intent_hdmi.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent_hdmi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent_hdmi);
    }

}