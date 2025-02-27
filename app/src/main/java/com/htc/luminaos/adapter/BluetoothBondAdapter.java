package com.htc.luminaos.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.htc.luminaos.R;
import com.htc.luminaos.utils.ClsUtils;
import com.htc.luminaos.widget.DelpairDeviceDialog;
import com.htc.luminaos.widget.DisDeviceDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.htc.luminaos.activity.BluetoothActivity.a2dp;
import static com.htc.luminaos.activity.BluetoothActivity.connectDeviceFromA2DP;
import static com.htc.luminaos.activity.BluetoothActivity.connectKeyboard;
import static com.htc.luminaos.activity.BluetoothActivity.isKeyboardDevice;
import static com.htc.luminaos.activity.BluetoothActivity.mBluetoothProfile;

/**
 * Author:
 * Date:
 * Description:
 */
public class BluetoothBondAdapter extends RecyclerView.Adapter<BluetoothBondAdapter.MyViewHolder> implements View.OnHoverListener {

    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private Activity mContext;

    private Map<String, Integer> stateMap = new HashMap<String, Integer>();
    BluetoothDevice CurDevice = null;

    public BluetoothBondAdapter(List<BluetoothDevice> deviceList, Activity mContext){
        this.deviceList = deviceList;
        this.mContext = mContext;

    }

    public void updateList(List<BluetoothDevice> deviceList){
        this.deviceList = deviceList;
    }

    public void updateConnectMap(String address, int state){
        stateMap.put(address,state);
    }

    public void removeConnectMap(String address){
        if (stateMap.containsKey(address)){
            stateMap.remove(address);
        }
    }
    public void clearConnectMap(){
        stateMap.clear();
    }

    public void currentConnectDevice(BluetoothDevice device){
        this.CurDevice = device;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ble_item,null));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final BluetoothDevice device = deviceList.get(i);
        if (device.getName()==null || "".equals(device.getName())){
            myViewHolder.ble_name.setText(SystemProperties.get("persist.sys.connectBleName",""));
        }else {
            myViewHolder.ble_name.setText(device.getName());
        }

        if(device.getBluetoothClass()
                .getMajorDeviceClass()== BluetoothClass.Device.Major.PHONE){
            myViewHolder.ble_type.setImageResource(R.drawable.bluetooth);
        }else if(device.getBluetoothClass()
                .getMajorDeviceClass()== BluetoothClass.Device.Major.COMPUTER){
            myViewHolder.ble_type.setImageResource(R.drawable.bluetooth);
        } else if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO) {
            myViewHolder.ble_type.setImageResource(R.drawable.bluetooth);
        }else if(device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL){
            switch (device.getBluetoothClass().getDeviceClass()) {
                case BluetoothClass.Device.PERIPHERAL_KEYBOARD:
                case BluetoothClass.Device.PERIPHERAL_KEYBOARD_POINTING:
                    // viewHolder.pair_iv.setImageResource(R.drawable.ic_lockscreen_ime);
                    myViewHolder.ble_type.setImageResource(R.drawable.bluetooth);
                    break;
                case BluetoothClass.Device.PERIPHERAL_POINTING:
                    myViewHolder.ble_type
                            .setImageResource(R.drawable.bluetooth);
                    break;
                default:
                    myViewHolder.ble_type.setImageResource(R.drawable.bluetooth);
                    break;
            }
        }else{
            myViewHolder.ble_type.setImageResource(R.drawable.bluetooth);
        }

        //myViewHolder.ble_status.setVisibility(View.GONE);
        if (stateMap.containsKey(device.getAddress())) {
            int state = stateMap.get(device.getAddress());
            /**
             * 1：已连接 2：正在连接 3：取消连接
             */
            switch (state) {
                case 1:
                    myViewHolder.ble_status.setVisibility(View.VISIBLE);
                    myViewHolder.ble_status.setText(mContext.getString(R.string.connected));

                    break;

                case 2:
                    myViewHolder.ble_status.setVisibility(View.VISIBLE);
                    myViewHolder.ble_status.setText(mContext
                            .getString(R.string.connecting));
                    break;

                case 3:
                    myViewHolder.ble_status.setVisibility(View.VISIBLE);
                    myViewHolder.ble_status.setText(mContext
                            .getString(R.string.disconnecting));
                    break;
            }
        }else {
            myViewHolder.ble_status.setVisibility(View.VISIBLE);
            myViewHolder.ble_status.setText(R.string.paired);
        }
        myViewHolder.rl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stateMap.containsKey(device.getAddress())){

                    DelpairDeviceDialog delpairDeviceDialog = new DelpairDeviceDialog(v.getContext(),R.style.DialogTheme,null);
                    delpairDeviceDialog.setDevice_title_name(device.getName());
                    delpairDeviceDialog.setOnClickCallBack(new DelpairDeviceDialog.OnDelpairDeviceCallBack() {
                        @Override
                        public void onDelPairedClick() {
                            View view =  mContext.getCurrentFocus();
                            if (view!=null)
                                view.clearFocus();

                            delPairDevice(device);
                        }

                        @Override
                        public void onConnectClick() {
                            updateConnectMap(device.getAddress(),2);
                            notifyDataSetChanged();
                            if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO){
                                if (a2dp.getConnectionState(device)== BluetoothProfile.STATE_CONNECTED){
                                    a2dp.setActiveDevice(device);
                                    return;
                                }
                            }
                            if(isKeyboardDevice(device.getUuids())){
                                if (mBluetoothProfile != null
                                        && mBluetoothProfile.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED) {
                                    connectKeyboard(device);
                                }
                            }else {
                                connectDeviceFromA2DP(device);
                            }
                        }
                    });
                    delpairDeviceDialog.show();

                }else {
                    //已连接
                    DisDeviceDialog disDeviceDialog = new DisDeviceDialog(v.getContext(),R.style.DialogTheme,null);
                    disDeviceDialog.setDevice_title_name(device.getName());
                    disDeviceDialog.setOnClickCallBack(new DisDeviceDialog.OnDisDeviceCallBack() {
                        @Override
                        public void onEnterClick() {
//                            BluetoothAdapter.getDefaultAdapter().disconnectAllEnabledProfiles(device);

                            updateConnectMap(device.getAddress(),3);
                            notifyDataSetChanged();
                            /*if (mBluetoothProfile!=null)
                            mBluetoothProfile.disconnect(device);*/
                        }

                        @Override
                        public void onUnPairClick() {
                            View view =  mContext.getCurrentFocus();
                            if (view!=null)
                                view.clearFocus();

                            delPairDevice(device);
                        }
                    });
                    disDeviceDialog.show();

                }
            }
        });
        myViewHolder.rl_item.setOnHoverListener(this);
    }

    /**
     * 清楚已配对设备信息
     */
    public void delPairDevice(BluetoothDevice device) {
        if (device != null) {
            try {
                ClsUtils.unpairDevice(device);
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView ble_type;
        TextView ble_name;
        TextView ble_status;
        RelativeLayout rl_item;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ble_type = itemView.findViewById(R.id.ble_type);
            ble_name = itemView.findViewById(R.id.ble_name);
            ble_status = itemView.findViewById(R.id.ble_status);
            rl_item = itemView.findViewById(R.id.rl_item);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        int what = event.getAction();
        switch (what) {
            case MotionEvent.ACTION_HOVER_ENTER: // 鼠标进入view
                v.requestFocus();
                break;
            case MotionEvent.ACTION_HOVER_MOVE: // 鼠标在view上
                break;
            case MotionEvent.ACTION_HOVER_EXIT: // 鼠标离开view
                break;
        }
        return false;
    }
}
