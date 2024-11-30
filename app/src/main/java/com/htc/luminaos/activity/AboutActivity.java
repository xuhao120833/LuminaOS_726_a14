package com.htc.luminaos.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.htc.luminaos.MyApplication;
import com.htc.luminaos.R;
import com.htc.luminaos.databinding.ActivityAboutBinding;
import com.htc.luminaos.utils.AppUtils;
import com.htc.luminaos.utils.ClearMemoryUtils;
import com.htc.luminaos.utils.Contants;
import com.htc.luminaos.utils.DeviceUtils;
import com.htc.luminaos.utils.LogUtils;
import com.htc.luminaos.utils.ShareUtil;
import com.htc.luminaos.utils.ToastUtil;
import com.htc.luminaos.widget.UpgradeCheckFailDialog;
import com.htc.luminaos.widget.UpgradeCheckSuccessDialog;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends BaseActivity {

    private ActivityAboutBinding aboutBinding;

    private final long GBYTE = 1024 * 1024 * 1024;
    List<Integer> ENTER_FACTORY_REBOOT = new ArrayList<>();
    List<Integer> ENTER_FACTORY = new ArrayList<>();
    List<Integer> ENTER_MAC = new ArrayList<>();
    List<Integer> record_list = new ArrayList<>();
    boolean isRecord = false;
    boolean isDebug = false;
    int mPosition = 5;
    private UpgradeCheckFailDialog upgradeCheckFailDialog;

    private UpgradeCheckSuccessDialog upgradeCheckSuccessDialog;
    private SharedPreferences sp;
    private static String OTA_PACKAGE_FILE = "update.zip";
    private static String USB_ROOT = "/mnt/media_rw";
    private static String FLASH_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                if (msg.obj != null) {
                    String path = (String) msg.obj;
//                    startSystemUpdate(path);
                    showUpgradeCheckSuccessDialog(path);

                } else {
                    showUpgradeCheckFailDialog();
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aboutBinding = ActivityAboutBinding.inflate(LayoutInflater.from(this));
        setContentView(aboutBinding.getRoot());
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        if (isNetworkConnect()) {
            aboutBinding.rlWiredMac.setVisibility(View.VISIBLE);
        } else {
            aboutBinding.rlWiredMac.setVisibility(View.GONE);
        }
        super.onResume();
    }

    private boolean isNetworkConnect() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        return networkInfo != null && networkInfo.isConnected();
    }

    private void initView() {
        aboutBinding.rlDeviceModel.setOnClickListener(this);
        aboutBinding.rlUpdateFirmware.setOnClickListener(this);
        aboutBinding.rlOnlineUpdate.setOnClickListener(this);
        aboutBinding.rlDeviceModel.requestFocus();
        aboutBinding.rlDeviceModel.requestFocusFromTouch();

        aboutBinding.rlDeviceModel.setVisibility(MyApplication.config.deviceModel ? View.VISIBLE : View.GONE);
        aboutBinding.rlUiVersion.setVisibility(MyApplication.config.uiVersion ? View.VISIBLE : View.GONE);
        aboutBinding.rlAndroidVersion.setVisibility(MyApplication.config.androidVersion ? View.VISIBLE : View.GONE);
        aboutBinding.rlResolution.setVisibility(MyApplication.config.resolution ? View.VISIBLE : View.GONE);
        aboutBinding.rlMemory.setVisibility(MyApplication.config.memory ? View.VISIBLE : View.GONE);
        aboutBinding.rlStorage.setVisibility(MyApplication.config.storage ? View.VISIBLE : View.GONE);
        aboutBinding.rlWirelessMac.setVisibility(MyApplication.config.wlanMacAddress ? View.VISIBLE : View.GONE);
        aboutBinding.rlSerialNumber.setVisibility(MyApplication.config.serialNumber ? View.VISIBLE : View.GONE);
        aboutBinding.rlUpdateFirmware.setVisibility(MyApplication.config.updateFirmware ? View.VISIBLE : View.GONE);
        aboutBinding.rlOnlineUpdate.setVisibility(MyApplication.config.onlineUpdate ? View.VISIBLE : View.GONE);

        aboutBinding.rlDeviceModel.setOnHoverListener(this);
        aboutBinding.rlUpdateFirmware.setOnHoverListener(this);
        aboutBinding.rlOnlineUpdate.setOnHoverListener(this);
    }

    private void initData() {
        sp = ShareUtil.getInstans(this);
        isDebug = sp.getBoolean(Contants.KEY_DEVELOPER_MODE, false);
        aboutBinding.deviceModelTv.setText(SystemProperties.get("persist.sys.modelName", "Projecter"));//产品型号
        aboutBinding.uiVersionTv.setText(SystemProperties.get("ro.build.version.incremental"));//产品ui版本
        aboutBinding.androidVersionTv.setText(Build.VERSION.RELEASE);//android version
        aboutBinding.serialNumberTv.setText(SystemProperties.get("ro.serialno", "unknow"));
//        aboutBinding.serialNumberTv.setText(getProperty("ro.serialno","unknow"));
        getMemorySize();
        getStorageSize();
        aboutBinding.resolutionTv.setText(getResolution());
        aboutBinding.wirelessMacTv.setText(getWlanMacAddress());
        aboutBinding.wiredMacTv.setText(DeviceUtils.getEthMac());
        initQuickKey();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_device_model:
                if (!isDebug) {
                    if (mPosition == 0) {
                        sp.edit().putBoolean(Contants.KEY_DEVELOPER_MODE, true).apply();
                        isDebug = true;
                        mPosition = 5;
                        ToastUtil.showShortToast(this,
                                getString(R.string.developer_mode_on));
                    }
                    mPosition--;
                } else {
                    if (mPosition == 0) {
                        sp.edit().putBoolean(Contants.KEY_DEVELOPER_MODE, false).apply();
                        isDebug = false;
                        mPosition = 5;
                        ToastUtil.showShortToast(this,
                                getString(R.string.developer_mode_off));
                    }
                    mPosition--;
                }
                break;
            case R.id.rl_update_firmware:
                try {
                    goFindUpgradeFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.rl_online_update:
                AppUtils.startNewApp(this, "com.htc.htcotaupdate");
                break;
        }
        super.onClick(v);
    }

    private void goFindUpgradeFile() {
        showCheckingDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = findUpdateFile();
                Message message = handler.obtainMessage();
                message.what = 1;
                message.obj = path;
                handler.sendMessage(message);
            }
        }).start();
    }

    private void initQuickKey() {
        //进厂测先重启测试组合键
        ENTER_FACTORY_REBOOT.add(KeyEvent.KEYCODE_VOLUME_DOWN);
        ENTER_FACTORY_REBOOT.add(KeyEvent.KEYCODE_DPAD_UP);
        ENTER_FACTORY_REBOOT.add(KeyEvent.KEYCODE_DPAD_RIGHT);
        ENTER_FACTORY_REBOOT.add(KeyEvent.KEYCODE_DPAD_DOWN);
        ENTER_FACTORY_REBOOT.add(KeyEvent.KEYCODE_DPAD_LEFT);
        ENTER_FACTORY_REBOOT.add(KeyEvent.KEYCODE_VOLUME_DOWN);

        //进厂测组合键
        ENTER_FACTORY.add(KeyEvent.KEYCODE_VOLUME_UP);
        ENTER_FACTORY.add(KeyEvent.KEYCODE_DPAD_UP);
        ENTER_FACTORY.add(KeyEvent.KEYCODE_DPAD_RIGHT);
        ENTER_FACTORY.add(KeyEvent.KEYCODE_DPAD_DOWN);
        ENTER_FACTORY.add(KeyEvent.KEYCODE_DPAD_LEFT);
        ENTER_FACTORY.add(KeyEvent.KEYCODE_VOLUME_UP);

        //显示wifi，以太网，SN的二维码组合键
        ENTER_MAC.add(KeyEvent.KEYCODE_VOLUME_MUTE);
        ENTER_MAC.add(KeyEvent.KEYCODE_DPAD_UP);
        ENTER_MAC.add(KeyEvent.KEYCODE_DPAD_RIGHT);
        ENTER_MAC.add(KeyEvent.KEYCODE_DPAD_DOWN);
        ENTER_MAC.add(KeyEvent.KEYCODE_DPAD_LEFT);
        ENTER_MAC.add(KeyEvent.KEYCODE_VOLUME_MUTE);
    }


    private void getMemorySize() {
        String total_memory = "1GB";
        long memorySize = ClearMemoryUtils.getTotalMemorySize(this);
        memorySize = memorySize * MyApplication.config.memoryScale;
        try {
            if (memorySize > 8 * GBYTE)
                total_memory = "10GB";
            else if (memorySize > 6 * GBYTE)
                total_memory = "8GB";
            else if (memorySize > 4 * GBYTE)
                total_memory = "6GB";
            else if (memorySize > 2 * GBYTE)
                total_memory = "4GB";
            else if (memorySize > GBYTE)
                total_memory = "2GB";
        } catch (Exception e) {
            total_memory = "1GB";
        }

        aboutBinding.memoryTv.setText(total_memory + "/"
                + ClearMemoryUtils.formatFileSize(ClearMemoryUtils
                .getAvailableMemory(this) * MyApplication.config.memoryScale, false));
    }

    private void getStorageSize() {
        long totalSize = ClearMemoryUtils
                .getRomTotalSizeLong(this);
        totalSize = totalSize * MyApplication.config.storageScale;
        String total = "8 GB";
        try {
            if (totalSize > 64 * GBYTE)
                total = "128 GB";
            else if (totalSize > 32 * GBYTE)
                total = "64 GB";
            else if (totalSize > 16 * GBYTE)
                total = "32 GB";
            else if (totalSize > 8 * GBYTE)
                total = "16 GB";
            else if (totalSize > 2 * GBYTE)
                total = "8 GB";
            else
                total = "4 GB";

        } catch (Exception e) {
            // TODO: handle exception
            total = ClearMemoryUtils.getRomTotalSize(this);
        }
        /*aboutBinding.storageTv.setText(getString(R.string.memory_info,
                ClearMemoryUtils.getRomAvailableSize(this),total));*/

        aboutBinding.storageTv.setText(total + "/"
                + ClearMemoryUtils.getRomAvailableSize(this, MyApplication.config.storageScale));
    }

    private String getResolution() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return displayMetrics.widthPixels + " X " + displayMetrics.heightPixels;
    }


    private String getWlanMacAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiManager.isWifiEnabled() && wifiInfo != null) {
            return wifiInfo.getMacAddress();
        }
        return "00:00:00:00:00:00";
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            isRecord = true;
        }
        LogUtils.d("hzj", "onKeyDown " + keyCode);
        if (isRecord && event.getAction() == KeyEvent.ACTION_UP) {
            record_list.add(keyCode);
            if (record_list.size() >= 6) {
                LogUtils.d("hzj", "record_list " + record_list.toString());
                if (isEqual(record_list, ENTER_FACTORY_REBOOT)) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.hotack.hotackfeaturestest", "com.hotack.activity.TestActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("reboot_test", true);
                    startActivity(intent);
                } else if (isEqual(record_list, ENTER_FACTORY)) {
                    AppUtils.startNewApp(this, "com.hotack.hotackfeaturestest", "com.hotack.activity.TestActivity");
                } else if (isEqual(record_list, ENTER_MAC)) {
                    AppUtils.startNewApp(this, "com.hotack.writesn", "com.hotack.writesn.MainActivity");
                }
                isRecord = false;
                record_list.clear();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean isEqual(List<Integer> list1, List<Integer> list2) {
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i)))
                return false;
        }
        return true;
    }


    private String findUpdateFile() {
        String dataPath = FLASH_ROOT + "/" + OTA_PACKAGE_FILE;
        Log.d("findUpdateFile", " dataPath " + dataPath);
        if (new File(dataPath).exists())  //优先检查本地存储有没有 storage/emulated/0/update.zip
            return dataPath;
        File usbRoot = new File(USB_ROOT);//本地没有，再去检查mnt/media_rw下面是否挂载了U盘
        File[] pfiles = usbRoot.listFiles();//支持检测多个U盘
        if (pfiles == null) {
            return null;
        }
        for (File tmp : pfiles) {//findUpdateFile:  tmp F3DE-C571 1 /mnt/media_rw/F3DE-C571
            Log.d("findUpdateFile", " tmp " + tmp.getName()+" "+pfiles.length+" "+tmp.getAbsolutePath() );
            if (tmp.isDirectory()) {
                File[] subfiles = tmp.listFiles();
                if (subfiles == null) {
                    Log.d("findUpdateFile", " subfiles  null ");
//                        continue;//跳过当前目录，进入下一个循环
                }
                if (subfiles != null) {
                    for (File subtmp : subfiles) {

                        Log.d("findUpdateFile", " subtmp " + subtmp.getName());
                        if (subtmp.isDirectory()) {
                            File[] files = subtmp.listFiles(new FileFilter() {
                                @Override
                                public boolean accept(File arg0) {

                                    if (arg0.isDirectory()) {
                                        return false;
                                    }

                                    if (arg0.getName().equals(OTA_PACKAGE_FILE)) {

                                        return true;
                                    }
                                    return false;
                                }
                            });
                            if (files != null && files.length > 0) {

                                return files[0].getAbsolutePath();
                            }
                        } else {
                            if (subtmp.getName().equals(OTA_PACKAGE_FILE)) {

                                return subtmp.getAbsolutePath();
                            } else {
                                continue;
                            }
                            //continue;
                        }
                    }
                }
            }
//            else if (tmp.isFile()) {
//                if (tmp.getName().equals(OTA_PACKAGE_FILE)) {
//
//                    return tmp.getAbsolutePath();
//                } else {
//                    continue;
//                }
//            }
        }


        return null;
    }

    private void startSystemUpdate(String path) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.softwinner.update", "com.softwinner.update.ui.AbUpdate"));
        Bundle bundle = new Bundle();
        bundle.putString("update_path", path);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 拷贝文件
     */
    ProgressDialog progressDialog;

    private void showCheckingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.checking));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void showUpgradeCheckFailDialog() {
        if (upgradeCheckFailDialog == null) {
            upgradeCheckFailDialog = new UpgradeCheckFailDialog(AboutActivity.this);
            upgradeCheckFailDialog.setOnClickCallBack(new UpgradeCheckFailDialog.OnClickCallBack() {
                @Override
                public void onRetry() {
                    goFindUpgradeFile();
                }
            });
        }

        if (!upgradeCheckFailDialog.isShowing())
            upgradeCheckFailDialog.show();
    }

    private void showUpgradeCheckSuccessDialog(String path) {
        if (upgradeCheckSuccessDialog == null) {
            upgradeCheckSuccessDialog = new UpgradeCheckSuccessDialog(AboutActivity.this);
            upgradeCheckSuccessDialog.setOnClickCallBack(new UpgradeCheckSuccessDialog.OnClickCallBack() {
                @Override
                public void upgrade() {
                    startSystemUpdate(path);
                }
            });
        }

        if (!upgradeCheckSuccessDialog.isShowing())
            upgradeCheckSuccessDialog.show();
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }
}