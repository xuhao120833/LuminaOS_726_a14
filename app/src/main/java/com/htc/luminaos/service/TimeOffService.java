package com.htc.luminaos.service;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.htc.luminaos.R;
import com.htc.luminaos.utils.Contants;
import com.htc.luminaos.utils.ShareUtil;
import com.htc.luminaos.widget.ShutDownDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class TimeOffService extends Service {
    String TAG = "TimeOffService";
    int offTime = -1;//OFF Time S
   Timer timer;
    SharedPreferences sharedPreferences;
    Handler handler = new Handler();

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (offTime==-1){
                stopSelf();
                return;
            }
            if (offTime<=10){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ShutDownDialog shutDownDialog = new ShutDownDialog(getBaseContext(), R.style.DialogTheme);
                        shutDownDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                stopSelf();
                            }
                        });
                        shutDownDialog.show();
                    }
                });
                sharedPreferences.edit().putInt(Contants.TimeOffTime,0).apply();
                sharedPreferences.edit().putBoolean(Contants.TimeOffStatus,false).apply();
                sharedPreferences.edit().putInt(Contants.TimeOffIndex,0).apply();
                if (timer!=null){
                    timer.cancel();
                }
                return;
            }
            offTime-=10;
            sharedPreferences.edit().putInt(Contants.TimeOffTime,offTime).apply();
        }
    };
    public TimeOffService() {
    }

    @Override
    public void onCreate() {
        timer = new Timer();
        timer.schedule(timerTask,10000,10000);//每10秒检查一次
        sharedPreferences = ShareUtil.getInstans(this);
        Log.d(TAG,"onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initData(intent);
        Log.d(TAG,"onStartCommand()");
        return START_STICKY;
    }

    private void initData(Intent intent){
        if (intent==null){
            //offTime=-1;
            if (sharedPreferences.getBoolean(Contants.TimeOffStatus,false))
                offTime = sharedPreferences.getInt(Contants.TimeOffTime,-1);
            return;
        }
        if (intent.hasExtra(Contants.TimeOffStatus)){
            if (!intent.getBooleanExtra(Contants.TimeOffStatus,true))
                stopSelf();
        }

        if (intent.hasExtra(Contants.TimeOffTime)){
            offTime = intent.getIntExtra(Contants.TimeOffTime,-1);
        }else {
            offTime = -1;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG,"stopService()");
        return super.stopService(name);
    }

    public static void execShell(String cmd){
    try {
      java.lang.Process p= java.lang.Runtime.getRuntime().exec(cmd);
      BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader br2=new BufferedReader(new InputStreamReader(p.getErrorStream()));
      String readLine=br.readLine();
      String readLine2=br2.readLine();
      if(br!=null){
        br.close();
      }
      p.destroy();
      p=null;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        if (timer!=null) {
            timer.cancel();
            timer=null;
        }
        super.onDestroy();
    }
}
