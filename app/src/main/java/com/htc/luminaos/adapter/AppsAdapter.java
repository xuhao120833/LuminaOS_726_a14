package com.htc.luminaos.adapter;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.htc.luminaos.R;
import com.htc.luminaos.entry.AppInfoBean;
import com.htc.luminaos.utils.AppUtils;
import com.htc.luminaos.utils.ScrollUtils;
import com.htc.luminaos.widget.AppDetailDialog;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Author:
 * Date:
 * Description:
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.MyViewHolder> implements View.OnKeyListener {

    Context mContext;
    RecyclerView recyclerView;
    List<AppInfoBean> infoBeans;
    private PackageManager mPm;
    private String TAG = "AppsAdapter";

    public AppsAdapter(Context mContext,List<AppInfoBean> infoBeans, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.infoBeans = infoBeans;
        this.recyclerView = recyclerView;
        this.mPm = mContext.getPackageManager();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.app_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        final AppInfoBean info = infoBeans.get(i);
        myViewHolder.icon.setImageDrawable(info.getAppicon());
        myViewHolder.name.setText(info.getAppname());
        myViewHolder.rl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.startNewApp(mContext,info.getApppackagename());
            }
        });

        myViewHolder.rl_item.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (recyclerView==null)
                    return;

                int position = ((RecyclerView) view.getParent()).getChildAdapterPosition(view);
                if(hasFocus&&position!=0){
                    int[] amount = ScrollUtils.getScrollAmount(recyclerView, view);//计算需要滑动的距离
                    recyclerView.smoothScrollBy(amount[0], amount[1]);
                }

                AnimationSet animationSet = new AnimationSet(true);
                view.bringToFront();
                if (hasFocus) {

                    ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.10f,
                            1.0f, 1.10f, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnimation.setDuration(150);
                    animationSet.addAnimation(scaleAnimation);
                    animationSet.setFillAfter(true);
                    view.startAnimation(animationSet);
                } else {
                    ScaleAnimation scaleAnimation = new ScaleAnimation(1.10f, 1.0f,
                            1.10f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    animationSet.addAnimation(scaleAnimation);
                    scaleAnimation.setDuration(150);
                    animationSet.setFillAfter(true);
                    view.startAnimation(animationSet);
                }
            }
        });

        myViewHolder.rl_item.setOnKeyListener(this);
    }

    @Override
    public int getItemCount() {
        return infoBeans.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_MENU && event.getAction()==KeyEvent.ACTION_DOWN){
            Log.d(TAG,"卸载收到MENU按键");
            int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
            final AppInfoBean info = infoBeans.get(position);

            boolean[] result = AppUtils.checkIfSystemAppAndCanUninstall(mContext, info.getApplicationInfo().packageName);
            if (result[0] && !result[1]) {
                new AlertDialog.Builder(mContext)
                        .setTitle(mContext.getString(R.string.hint)) // 对话框标题
                        .setMessage(mContext.getString(R.string.system_app_cannot_uninstalled)) // 对话框内容
                        .setPositiveButton(mContext.getString(R.string.enter), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // 点击“确定”按钮时，关闭对话框
                            }
                        })
                        .setCancelable(false) // 使对话框不能通过点击外部区域关闭
                        .show();
                return true;
            }

            AppDetailDialog detailDialog = new AppDetailDialog(mContext,R.style.DialogTheme);
            detailDialog.setData(info.getApplicationInfo());
            detailDialog.setOnClickCallBack(new AppDetailDialog.OnAppDetailCallBack() {
                @Override
                public void onClear_cache(String packageName) {
                    ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    activityManager.clearApplicationUserData(packageName,null);
                }

                @Override
                public void onUninstall(String packageName) {
                    try {
                        Intent intent=new Intent();
                        Uri uri = Uri.parse("package:"+packageName);
                        //获取删除包名的URI
                        intent.setAction(Intent.ACTION_DELETE);
                        //设置我们要执行的卸载动作
                        intent.setData(uri);
                        //设置获取到的
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            });
            detailDialog.show();

        }

        return false;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        RelativeLayout rl_item;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.app_name);
            rl_item = itemView.findViewById(R.id.rl_item);
            icon = itemView.findViewById(R.id.app_icon);
        }
    }
}
