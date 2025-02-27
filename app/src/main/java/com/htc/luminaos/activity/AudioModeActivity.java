package com.htc.luminaos.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.htc.luminaos.MyApplication;
import com.htc.luminaos.R;
import com.htc.luminaos.databinding.ActivityAudioModeBinding;
import com.htc.luminaos.databinding.ActivityPictureModeBinding;
import com.htc.luminaos.utils.ReflectUtil;
import com.softwinner.PQControl;
import com.softwinner.TvAudioControl;
import com.softwinner.tv.AwTvAudioManager;
import com.softwinner.tv.AwTvDisplayManager;
import com.softwinner.tv.common.AwTvAudioTypes;

public class AudioModeActivity extends BaseActivity implements View.OnKeyListener,View.OnClickListener {

    ActivityAudioModeBinding activityAudioModeBinding;

    private long cur_time = 0;

    private int sound_mode = 0;//当前声音模式下标
    private String[] soundMode_name ;
    TvAudioControl tvAudioControl;
    private int value_120hz = 50;
    private int value_200hz = 50;
    private int value_500hz = 50;
    private int value_1d2khz = 50;
    private int value_3khz = 50;
    private int value_7d5khz = 50;
    private int value_12khz = 50;
    private AwTvAudioManager mAwTvAudioManager = null;

    private static String TAG = "AudioModeActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityAudioModeBinding = ActivityAudioModeBinding.inflate(LayoutInflater.from(this));
        setContentView(activityAudioModeBinding.getRoot());
        initView();
        soundMode_name = getResources().getStringArray(R.array.soundMode_name);
        tvAudioControl = new TvAudioControl(this);
        mAwTvAudioManager = AwTvAudioManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    private void initView() {
        activityAudioModeBinding.rlAudioMode.setOnClickListener(this);
        activityAudioModeBinding.rlAudioMode.setOnKeyListener(this);
        activityAudioModeBinding.rlAudioMode.setOnHoverListener(this);
        activityAudioModeBinding.audioModeRight.setOnClickListener(this);
        activityAudioModeBinding.audioModeLeft.setOnClickListener(this);

        activityAudioModeBinding.rl120hz.setOnClickListener(this);
        activityAudioModeBinding.rl120hz.setOnKeyListener(this);
        activityAudioModeBinding.rl120hz.setOnHoverListener(this);
        activityAudioModeBinding.right120.setOnClickListener(this);
        activityAudioModeBinding.left120.setOnClickListener(this);

        activityAudioModeBinding.rl200hz.setOnClickListener(this);
        activityAudioModeBinding.rl200hz.setOnKeyListener(this);
        activityAudioModeBinding.rl200hz.setOnHoverListener(this);
        activityAudioModeBinding.right200.setOnClickListener(this);
        activityAudioModeBinding.left200.setOnClickListener(this);

        activityAudioModeBinding.rl500hz.setOnClickListener(this);
        activityAudioModeBinding.rl500hz.setOnKeyListener(this);
        activityAudioModeBinding.rl500hz.setOnHoverListener(this);
        activityAudioModeBinding.right500.setOnClickListener(this);
        activityAudioModeBinding.left500.setOnClickListener(this);

        activityAudioModeBinding.rl1d2khz.setOnClickListener(this);
        activityAudioModeBinding.rl1d2khz.setOnKeyListener(this);
        activityAudioModeBinding.rl1d2khz.setOnHoverListener(this);
        activityAudioModeBinding.right1d2k.setOnClickListener(this);
        activityAudioModeBinding.left1d2k.setOnClickListener(this);

        activityAudioModeBinding.rl3khz.setOnClickListener(this);
        activityAudioModeBinding.rl3khz.setOnKeyListener(this);
        activityAudioModeBinding.rl3khz.setOnHoverListener(this);
        activityAudioModeBinding.right3k.setOnClickListener(this);
        activityAudioModeBinding.left3k.setOnClickListener(this);

        activityAudioModeBinding.rl7d5khz.setOnClickListener(this);
        activityAudioModeBinding.rl7d5khz.setOnKeyListener(this);
        activityAudioModeBinding.rl7d5khz.setOnHoverListener(this);
        activityAudioModeBinding.right7d5k.setOnClickListener(this);
        activityAudioModeBinding.left7d5k.setOnClickListener(this);

        activityAudioModeBinding.rl12khz.setOnClickListener(this);
        activityAudioModeBinding.rl12khz.setOnKeyListener(this);
        activityAudioModeBinding.rl12khz.setOnHoverListener(this);
        activityAudioModeBinding.right12k.setOnClickListener(this);
        activityAudioModeBinding.left12k.setOnClickListener(this);

        activityAudioModeBinding.rl120hz.setVisibility(MyApplication.config.Menu120HZ?View.VISIBLE:View.GONE);
        activityAudioModeBinding.rl200hz.setVisibility(MyApplication.config.Menu200HZ?View.VISIBLE:View.GONE);
        activityAudioModeBinding.rl500hz.setVisibility(MyApplication.config.Menu500HZ?View.VISIBLE:View.GONE);
        activityAudioModeBinding.rl1d2khz.setVisibility(MyApplication.config.Menu1D2KHZ?View.VISIBLE:View.GONE);
        activityAudioModeBinding.rl3khz.setVisibility(MyApplication.config.Menu3KHZ?View.VISIBLE:View.GONE);
        activityAudioModeBinding.rl7d5khz.setVisibility(MyApplication.config.Menu7D5KHZ?View.VISIBLE:View.GONE);
        activityAudioModeBinding.rl12khz.setVisibility(MyApplication.config.Menu12KHZ?View.VISIBLE:View.GONE);

    }

    private void initData() {
        sound_mode = tvAudioControl.getAudioMode();
        activityAudioModeBinding.audioModeTv.setText(soundMode_name[sound_mode]);
        updateAllEQValue();
        updateAudioStatus();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rl_audio_mode) {
            if (sound_mode == soundMode_name.length - 1) {
                sound_mode = 0;
            } else {
                sound_mode += 1;
            }
            tvAudioControl.setAudioMode(sound_mode);
            activityAudioModeBinding.audioModeTv.setText(soundMode_name[sound_mode]);
            updateAllEQValue();
            updateAudioStatus();
        } else if (id == R.id.rl_120hz) {
            if (value_120hz == 100)
                return;
            value_120hz += 1;
            activityAudioModeBinding.tv120hz.setText(String.valueOf(value_120hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_1, value_120hz);
        } else if (id == R.id.right_120) {
            if (value_120hz == 100)
                return;
            value_120hz += 1;
            activityAudioModeBinding.tv120hz.setText(String.valueOf(value_120hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_1, value_120hz);
        } else if (id == R.id.left_120) {
            if (value_120hz == 0)
                return;
            value_120hz -= 1;
            activityAudioModeBinding.tv120hz.setText(String.valueOf(value_120hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_1, value_120hz);
        } else if (id == R.id.rl_200hz) {
            if (value_200hz == 100)
                return;
            value_200hz += 1;
            activityAudioModeBinding.tv200hz.setText(String.valueOf(value_200hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_2, value_200hz);
        } else if (id == R.id.right_200) {
            if (value_200hz == 100)
                return;
            value_200hz += 1;
            activityAudioModeBinding.tv200hz.setText(String.valueOf(value_200hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_2, value_200hz);
        } else if (id == R.id.left_200) {
            if (value_200hz == 0)
                return;
            value_200hz -= 1;
            activityAudioModeBinding.tv200hz.setText(String.valueOf(value_200hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_2, value_200hz);
        } else if (id == R.id.rl_500hz) {
            if (value_500hz == 100)
                return;
            value_500hz += 1;
            activityAudioModeBinding.tv500hz.setText(String.valueOf(value_500hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_3, value_500hz);
        } else if (id == R.id.right_500) {
            if (value_500hz == 100)
                return;
            value_500hz += 1;
            activityAudioModeBinding.tv500hz.setText(String.valueOf(value_500hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_3, value_500hz);
        } else if (id == R.id.left_500) {
            if (value_500hz == 0)
                return;
            value_500hz -= 1;
            activityAudioModeBinding.tv500hz.setText(String.valueOf(value_500hz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_3, value_500hz);
        } else if (id == R.id.rl_1d2khz) {
            if (value_1d2khz == 100)
                return;
            value_1d2khz += 1;
            activityAudioModeBinding.tv1d2khz.setText(String.valueOf(value_1d2khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_4, value_1d2khz);
        } else if (id == R.id.right_1d2k) {
            if (value_1d2khz == 100)
                return;
            value_1d2khz += 1;
            activityAudioModeBinding.tv1d2khz.setText(String.valueOf(value_1d2khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_4, value_1d2khz);
        } else if (id == R.id.left_1d2k) {
            if (value_1d2khz == 0)
                return;
            value_1d2khz -= 1;
            activityAudioModeBinding.tv1d2khz.setText(String.valueOf(value_1d2khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_4, value_1d2khz);
        } else if (id == R.id.rl_3khz) {
            if (value_3khz == 100)
                return;
            value_3khz += 1;
            activityAudioModeBinding.tv3khz.setText(String.valueOf(value_3khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_5, value_3khz);
        } else if (id == R.id.right_3k) {
            if (value_3khz == 100)
                return;
            value_3khz += 1;
            activityAudioModeBinding.tv3khz.setText(String.valueOf(value_3khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_5, value_3khz);
        } else if (id == R.id.left_3k) {
            if (value_3khz == 0)
                return;
            value_3khz -= 1;
            activityAudioModeBinding.tv3khz.setText(String.valueOf(value_3khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_5, value_3khz);
        } else if (id == R.id.rl_7d5khz) {
            if (value_7d5khz == 100)
                return;
            value_7d5khz += 1;
            activityAudioModeBinding.tv7d5khz.setText(String.valueOf(value_7d5khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_6, value_7d5khz);
        } else if (id == R.id.right_7d5k) {
            if (value_7d5khz == 100)
                return;
            value_7d5khz += 1;
            activityAudioModeBinding.tv7d5khz.setText(String.valueOf(value_7d5khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_6, value_7d5khz);
        } else if (id == R.id.left_7d5k) {
            if (value_7d5khz == 0)
                return;
            value_7d5khz -= 1;
            activityAudioModeBinding.tv7d5khz.setText(String.valueOf(value_7d5khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_6, value_7d5khz);
        } else if (id == R.id.rl_12khz) {
            if (value_12khz == 100)
                return;
            value_12khz += 1;
            activityAudioModeBinding.tv12khz.setText(String.valueOf(value_12khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_7, value_12khz);
        } else if (id == R.id.right_12k) {
            if (value_12khz == 100)
                return;
            value_12khz += 1;
            activityAudioModeBinding.tv12khz.setText(String.valueOf(value_12khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_7, value_12khz);
        } else if (id == R.id.left_12k) {
            if (value_12khz == 0)
                return;
            value_12khz -= 1;
            activityAudioModeBinding.tv12khz.setText(String.valueOf(value_12khz));
            mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_7, value_12khz);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((System.currentTimeMillis() - cur_time) < 100 && (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT)) {
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            cur_time = System.currentTimeMillis();

        if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT )) {
            return true;
        }
        AudioManager audioManager = (AudioManager) v.getContext().getSystemService(Context.AUDIO_SERVICE);
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
            int id = v.getId();
            if (id == R.id.rl_audio_mode) {
                if (sound_mode == 0) {
                    sound_mode = soundMode_name.length - 1;
                } else {
                    sound_mode -= 1;
                }
                tvAudioControl.setAudioMode(sound_mode);
                activityAudioModeBinding.audioModeTv.setText(soundMode_name[sound_mode]);
                updateAllEQValue();
                updateAudioStatus();
                if (audioManager != null) {
                    audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
                }
                audioManager = null;
            } else if (id == R.id.rl_120hz) {
                if (value_120hz == 0)
                    return false;
                value_120hz -= 1;
                activityAudioModeBinding.tv120hz.setText(String.valueOf(value_120hz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_1, value_120hz);
//                    break;
                return true;
            } else if (id == R.id.rl_200hz) {
                if (value_200hz == 0)
                    return false;
                value_200hz -= 1;
                activityAudioModeBinding.tv200hz.setText(String.valueOf(value_200hz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_2, value_200hz);
                //                    break;
                return true;
            } else if (id == R.id.rl_500hz) {
                if (value_500hz == 0)
                    return false;
                value_500hz -= 1;
                activityAudioModeBinding.tv500hz.setText(String.valueOf(value_500hz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_3, value_500hz);
                //                    break;
                return true;
            } else if (id == R.id.rl_1d2khz) {
                if (value_1d2khz == 0)
                    return false;
                value_1d2khz -= 1;
                activityAudioModeBinding.tv1d2khz.setText(String.valueOf(value_1d2khz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_4, value_1d2khz);
                //                    break;
                return true;
            } else if (id == R.id.rl_3khz) {
                if (value_3khz == 0)
                    return false;
                value_3khz -= 1;
                activityAudioModeBinding.tv3khz.setText(String.valueOf(value_3khz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_5, value_3khz);
                //                    break;
                return true;
            } else if (id == R.id.rl_7d5khz) {
                if (value_7d5khz == 0)
                    return false;
                value_7d5khz -= 1;
                activityAudioModeBinding.tv7d5khz.setText(String.valueOf(value_7d5khz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_6, value_7d5khz);
                //                    break;
                return true;
            } else if (id == R.id.rl_12khz) {
                if (value_12khz == 0)
                    return false;
                value_12khz -= 1;
                activityAudioModeBinding.tv12khz.setText(String.valueOf(value_12khz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_7, value_12khz);
                //                    break;
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
            int id = v.getId();
            if (id == R.id.rl_audio_mode) {
                if (sound_mode == soundMode_name.length - 1) {
                    sound_mode = 0;
                } else {
                    sound_mode += 1;
                }
                tvAudioControl.setAudioMode(sound_mode);
                activityAudioModeBinding.audioModeTv.setText(soundMode_name[sound_mode]);
                updateAllEQValue();
                updateAudioStatus();
                if (audioManager != null) {
                    audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
                }
                audioManager = null;
            } else if (id == R.id.rl_120hz) {
                if (value_120hz == 100)
                    return false;
                value_120hz += 1;
                activityAudioModeBinding.tv120hz.setText(String.valueOf(value_120hz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_1, value_120hz);
                //                    break;
                return true;
            } else if (id == R.id.rl_200hz) {
                if (value_200hz == 100)
                    return false;
                value_200hz += 1;
                activityAudioModeBinding.tv200hz.setText(String.valueOf(value_200hz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_2, value_200hz);
                //                    break;
                return true;
            } else if (id == R.id.rl_500hz) {
                if (value_500hz == 100)
                    return false;
                value_500hz += 1;
                activityAudioModeBinding.tv500hz.setText(String.valueOf(value_500hz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_3, value_500hz);
                //                    break;
                return true;
            } else if (id == R.id.rl_1d2khz) {
                if (value_1d2khz == 100)
                    return false;
                value_1d2khz += 1;
                activityAudioModeBinding.tv1d2khz.setText(String.valueOf(value_1d2khz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_4, value_1d2khz);
                //                    break;
                return true;
            } else if (id == R.id.rl_3khz) {
                if (value_3khz == 100)
                    return false;
                value_3khz += 1;
                activityAudioModeBinding.tv3khz.setText(String.valueOf(value_3khz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_5, value_3khz);
                //                    break;
                return true;
            } else if (id == R.id.rl_7d5khz) {
                if (value_7d5khz == 100)
                    return false;
                value_7d5khz += 1;
                activityAudioModeBinding.tv7d5khz.setText(String.valueOf(value_7d5khz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_6, value_7d5khz);
                //                    break;
                return true;
            } else if (id == R.id.rl_12khz) {
                if (value_12khz == 100)
                    return false;
                value_12khz += 1;
                activityAudioModeBinding.tv12khz.setText(String.valueOf(value_12khz));
                mAwTvAudioManager.setAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_7, value_12khz);
                //                    break;
                return true;
            }
        }

        return false;
    }

    private void updateAllEQValue(){
        value_120hz = mAwTvAudioManager.getAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_1);
        value_200hz = mAwTvAudioManager.getAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_2);
        value_500hz = mAwTvAudioManager.getAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_3);
        value_1d2khz = mAwTvAudioManager.getAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_4);
        value_3khz = mAwTvAudioManager.getAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_5);
        value_7d5khz = mAwTvAudioManager.getAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_6);
        value_12khz = mAwTvAudioManager.getAudioEqBand(AwTvAudioTypes.EnumGEQBand.E_AW_GEQ_BAND_7);
        activityAudioModeBinding.tv120hz.setText(""+value_120hz);
        activityAudioModeBinding.tv200hz.setText(""+value_200hz);
        activityAudioModeBinding.tv500hz.setText(""+value_500hz);
        activityAudioModeBinding.tv1d2khz.setText(""+value_1d2khz);
        activityAudioModeBinding.tv3khz.setText(""+value_3khz);
        activityAudioModeBinding.tv7d5khz.setText(""+value_7d5khz);
        activityAudioModeBinding.tv12khz.setText(""+value_12khz);
    }

    private void updateAudioStatus() {
        if(sound_mode == 4) {
            activityAudioModeBinding.scrollImage.setFocusable(true);
            activityAudioModeBinding.rl120hz.setEnabled(true);
            activityAudioModeBinding.right120.setEnabled(true);
            activityAudioModeBinding.left120.setEnabled(true);
            activityAudioModeBinding.rl200hz.setEnabled(true);
            activityAudioModeBinding.right200.setEnabled(true);
            activityAudioModeBinding.left200.setEnabled(true);
            activityAudioModeBinding.rl500hz.setEnabled(true);
            activityAudioModeBinding.right500.setEnabled(true);
            activityAudioModeBinding.left500.setEnabled(true);
            activityAudioModeBinding.rl1d2khz.setEnabled(true);
            activityAudioModeBinding.right1d2k.setEnabled(true);
            activityAudioModeBinding.left1d2k.setEnabled(true);
            activityAudioModeBinding.rl3khz.setEnabled(true);
            activityAudioModeBinding.right3k.setEnabled(true);
            activityAudioModeBinding.left3k.setEnabled(true);
            activityAudioModeBinding.rl7d5khz.setEnabled(true);
            activityAudioModeBinding.right7d5k.setEnabled(true);
            activityAudioModeBinding.left7d5k.setEnabled(true);
            activityAudioModeBinding.rl12khz.setEnabled(true);
            activityAudioModeBinding.right12k.setEnabled(true);
            activityAudioModeBinding.left12k.setEnabled(true);

            activityAudioModeBinding.rl120hz.setAlpha(1.0f);
            activityAudioModeBinding.rl200hz.setAlpha(1.0f);
            activityAudioModeBinding.rl500hz.setAlpha(1.0f);
            activityAudioModeBinding.rl1d2khz.setAlpha(1.0f);
            activityAudioModeBinding.rl3khz.setAlpha(1.0f);
            activityAudioModeBinding.rl7d5khz.setAlpha(1.0f);
            activityAudioModeBinding.rl12khz.setAlpha(1.0f);
        }else {
            activityAudioModeBinding.scrollImage.setFocusable(false);
            activityAudioModeBinding.rl120hz.setEnabled(false);
            activityAudioModeBinding.right120.setEnabled(false);
            activityAudioModeBinding.left120.setEnabled(false);
            activityAudioModeBinding.rl200hz.setEnabled(false);
            activityAudioModeBinding.right200.setEnabled(false);
            activityAudioModeBinding.left200.setEnabled(false);
            activityAudioModeBinding.rl500hz.setEnabled(false);
            activityAudioModeBinding.right500.setEnabled(false);
            activityAudioModeBinding.left500.setEnabled(false);
            activityAudioModeBinding.rl1d2khz.setEnabled(false);
            activityAudioModeBinding.right1d2k.setEnabled(false);
            activityAudioModeBinding.left1d2k.setEnabled(false);
            activityAudioModeBinding.rl3khz.setEnabled(false);
            activityAudioModeBinding.right3k.setEnabled(false);
            activityAudioModeBinding.left3k.setEnabled(false);
            activityAudioModeBinding.rl7d5khz.setEnabled(false);
            activityAudioModeBinding.right7d5k.setEnabled(false);
            activityAudioModeBinding.left7d5k.setEnabled(false);
            activityAudioModeBinding.rl12khz.setEnabled(false);
            activityAudioModeBinding.right12k.setEnabled(false);
            activityAudioModeBinding.left12k.setEnabled(false);

            activityAudioModeBinding.rl120hz.setAlpha(0.7f);
            activityAudioModeBinding.rl200hz.setAlpha(0.7f);
            activityAudioModeBinding.rl500hz.setAlpha(0.7f);
            activityAudioModeBinding.rl1d2khz.setAlpha(0.7f);
            activityAudioModeBinding.rl3khz.setAlpha(0.7f);
            activityAudioModeBinding.rl7d5khz.setAlpha(0.7f);
            activityAudioModeBinding.rl12khz.setAlpha(0.7f);
        }
    }

}
