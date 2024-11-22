package com.htc.luminaos.adapter;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.htc.luminaos.R;
import com.htc.luminaos.utils.Contants;
import com.htc.luminaos.utils.ShareUtil;
import com.htc.luminaos.widget.FocusKeepRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Author:
 * Date:
 * Description:
 */
public class WallPaperAdapter extends RecyclerView.Adapter<WallPaperAdapter.MyViewHolder> implements View.OnFocusChangeListener, View.OnHoverListener {

    Context mContext;
    ArrayList<Drawable> drawables;
    WallPaperOnCallBack wallPaperOnCallBack;
    File[] files;
    boolean isLocal = true;
    private ExecutorService executorService = Executors.newFixedThreadPool(8);
    Handler handler;

    FocusKeepRecyclerView focusKeepRecyclerView;

    public static int selectpostion = -1;
    private static String TAG = "WallPaperAdapter";

    private LruCache<String, Bitmap> imageCache;

    private static LruCache<Integer, BitmapDrawable> drawableCache;

    private Map<Integer, Future<?>> taskMap = new ConcurrentHashMap<>();

    public WallPaperAdapter(Context mContext, ArrayList<Drawable> drawables, Handler handler, FocusKeepRecyclerView focusKeepRecyclerView) {
        this.mContext = mContext;
        this.drawables = drawables;
        this.handler = handler;
        this.focusKeepRecyclerView = focusKeepRecyclerView;
        selectpostion = readShared();
        initCache();
    }

    public WallPaperAdapter(Context mContext, File[] files, Handler handler) {
        this.mContext = mContext;
        this.files = files;
        isLocal = false;
//        this.threadExecutor = threadExecutor;
        this.handler = handler;
        selectpostion = readShared();
    }

    public void setWallPaperOnCallBack(WallPaperOnCallBack wallPaperOnCallBack) {
        this.wallPaperOnCallBack = wallPaperOnCallBack;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.d(TAG," 执行onCreateViewHolder "+i);
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wallpaper_custom_item, null));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") final int i) {
        selectpostion = readShared();
        if (i == selectpostion) {
            myViewHolder.check.setVisibility(View.VISIBLE);
            myViewHolder.check.setImageResource(R.drawable.check_correct);
        } else if (selectpostion == -1) {//配置了默认背景，首次进入背景切换页
            SharedPreferences sharedPreferences = ShareUtil.getInstans(mContext);
            String defaultbg = sharedPreferences.getString(Contants.DefaultBg, "1");
            int number = Integer.parseInt(defaultbg);
            if (number - 1 == i) {
                myViewHolder.check.setVisibility(View.VISIBLE);
                myViewHolder.check.setImageResource(R.drawable.check_correct);
            }
            writeShared(number - 1);
        } else {
            myViewHolder.check.setVisibility(View.GONE);
        }
//        myViewHolder.rl_item.setOnFocusChangeListener(this);
        if (i < drawables.size()) {
            loadAndSetBackground(i,myViewHolder);
        } else {
            myViewHolder.icon_card.setCardBackgroundColor(Color.parseColor("#00000000"));
            myViewHolder.icon.setBackgroundResource(R.drawable.wallpaper_add);
        }
        myViewHolder.rl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //xuhao add
                    int position = myViewHolder.getAdapterPosition();
                    if (position < drawables.size()) {
                        if (selectpostion == position) {
//                            myViewHolder.check.setVisibility(View.GONE);
                        } else {
                            //写入数据库
                            writeShared(position);
                            notifyItemChanged(selectpostion);
                            selectpostion = readShared();
                            Log.d(TAG, " 图片背景选择 position" + selectpostion);
                            //xuhao
                            myViewHolder.check.setImageResource(R.drawable.check_correct);
                            myViewHolder.check.setVisibility(View.VISIBLE);
                            if (wallPaperOnCallBack != null) {
                                if (position < drawables.size())
                                    wallPaperOnCallBack.WallPaperLocalChange(drawables.get(position));
                            }
                        }
                    } else {
                        // 打开文件管理器选择图片
                        startExplorer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        myViewHolder.rl_item.setOnHoverListener(this);
    }

//    @Override
//    public void onViewRecycled(@NonNull MyViewHolder holder) {
//        super.onViewRecycled(holder);
//        int position = (int) holder.icon.getTag();
//        if (taskMap.containsKey(position)) {
//            taskMap.get(position).cancel(true);
//        }
//    }

    private void loadAndSetBackground(int i, MyViewHolder myViewHolder) {
        myViewHolder.icon.setBackgroundColor(Color.WHITE);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                BitmapDrawable cachedDrawable = drawableCache.get(i); // 尝试从缓存中获取
                if (cachedDrawable == null) {
                    // 缓存中没有，需要重新加载和压缩
                    BitmapDrawable d = null;
                    if (drawables.get(i) != null) {
                        d = (BitmapDrawable) drawables.get(i);
                    }
                    Bitmap bitmap = drawableToBitamp(d);
                    Bitmap bp = compressBitmap(bitmap);
                    BitmapDrawable finalD = new BitmapDrawable(bp);

                    // 加入缓存
                    drawableCache.put(i, finalD);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myViewHolder.icon.setBackground(finalD);
                        }
                    });
                }else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myViewHolder.icon.setBackground(cachedDrawable);
                        }
                    });
                }
            }
        });
    }

    private Bitmap compressImageFromFile(String srcPath) {

        BitmapFactory.Options newOpts = new BitmapFactory.Options();

        newOpts.inJustDecodeBounds = true;//只读边,不读内容

        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);


        newOpts.inJustDecodeBounds = false;

        int w = newOpts.outWidth;

        int h = newOpts.outHeight;

        float hh = 400f;//

        float ww = 300f;//

        int be = 1;

        if (w > h && w > ww) {

            be = (int) (newOpts.outWidth / ww);

        } else if (w < h && h > hh) {

            be = (int) (newOpts.outHeight / hh);

        }

        if (be <= 0)

            be = 1;

        newOpts.inSampleSize = be;//设置采样率


        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;//该模式是默认的,可不设

        newOpts.inPurgeable = true;// 同时设置才会有效

        newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收


        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        //      return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩

        //其实是无效的,大家尽管尝试

        return bitmap;

    }

    private Bitmap compressBitmap(Bitmap srcBitmap) {
        if (srcBitmap == null) {
            return null; // 检查传入的 Bitmap 是否为 null
        }

        // 创建一个空 Bitmap，设置为目标大小
        int targetWidth = 300; // 目标宽度
        int targetHeight = 400; // 目标高度

        // 计算压缩比例
        float widthRatio = (float) srcBitmap.getWidth() / targetWidth;
        float heightRatio = (float) srcBitmap.getHeight() / targetHeight;
        float finalRatio = Math.max(widthRatio, heightRatio);

        // 确保 finalRatio 至少为 1
        if (finalRatio <= 1) {
            return srcBitmap; // 如果原始图片小于目标大小，则返回原始图片
        }

        // 计算压缩后的大小
        int newWidth = Math.round(srcBitmap.getWidth() / finalRatio);
        int newHeight = Math.round(srcBitmap.getHeight() / finalRatio);

        // 创建目标 Bitmap
        Bitmap compressedBitmap = Bitmap.createScaledBitmap(srcBitmap, newWidth, newHeight, true);

        return compressedBitmap;
    }


    @Override
    public int getItemCount() {
        if (isLocal) return drawables.size() + 1;
        else return files.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView icon_card;
        ImageView icon;
        TextView status;
        FrameLayout rl_item;

        ImageView check;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon_card = itemView.findViewById(R.id.icon_card);
            icon = itemView.findViewById(R.id.icon);
            check = itemView.findViewById(R.id.check);
            status = itemView.findViewById(R.id.status);
            rl_item = itemView.findViewById(R.id.rl_item);
        }
    }


    public interface WallPaperOnCallBack {
        void WallPaperLocalChange(Drawable drawable);

        void WallPaperUsbChange(File file);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
//        AnimationSet animationSet = new AnimationSet(true);
//        v.bringToFront();
//
//        if (hasFocus) {
//            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.10f,
//                    1.0f, 1.10f, Animation.RELATIVE_TO_SELF, 0.5f,
//                    Animation.RELATIVE_TO_SELF, 0.5f);
//            scaleAnimation.setDuration(150);
//            animationSet.addAnimation(scaleAnimation);
//            animationSet.setFillAfter(true);
//            v.startAnimation(animationSet);
//        } else {
//            ScaleAnimation scaleAnimation = new ScaleAnimation(1.10f, 1.0f,
//                    1.10f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
//                    Animation.RELATIVE_TO_SELF, 0.5f);
//            scaleAnimation.setDuration(150);
//            animationSet.addAnimation(scaleAnimation);
//            scaleAnimation.setFillAfter(true);
//            v.startAnimation(animationSet);
//        }
    }


    public void writeShared(int postion) {
        SharedPreferences sharedPreferences = ShareUtil.getInstans(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Contants.SelectWallpaperLocal, postion);
        editor.apply();
    }

    private int readShared() {
        SharedPreferences sharedPreferences = ShareUtil.getInstans(mContext);
        return sharedPreferences.getInt(Contants.SelectWallpaperLocal, -1); // -1 是默认值，当没有找到该键时返回
    }

    // 初始化缓存
    public void initCache() {
        // 初始化缓存，设置最大缓存大小为当前可用内存的1/8
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        if(drawableCache == null) {
            drawableCache = new LruCache<Integer, BitmapDrawable>(cacheSize) {
                @Override
                protected int sizeOf(Integer key, BitmapDrawable value) {
                    // 缓存大小以KB为单位
                    return value.getBitmap().getByteCount() / 1024;
                }
            };
        }
    }

    public Bitmap drawableToBitamp(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void startExplorer() {
        // 定义目标应用的包名
        String packageName = "com.hisilicon.explorer";
        // 检查系统中是否安装了这个应用
        PackageManager packageManager = mContext.getPackageManager();
        try {
            // 尝试获取该包名的信息，如果找不到则会抛出异常
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            // 如果应用已安装，创建 Intent
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setComponent(new ComponentName(packageName, "com.hisilicon.explorer.activity.MainExplorerActivity"));
            intent.setAction(Intent.ACTION_MAIN);

            // 创建一个 Bundle 并添加数据
            Bundle bundle = new Bundle();
            bundle.putBoolean("wallpaper", true);  // 传递布尔值
            // 将 Bundle 添加到 Intent 中
            intent.putExtras(bundle);

            // 启动应用
            mContext.startActivity(intent);

        } catch (PackageManager.NameNotFoundException e) {
            // 如果没有安装这个应用，处理异常
            Log.d(TAG, "应用未安装");
            Toast.makeText(mContext, "未找到com.hisilicon.explorer应用", Toast.LENGTH_SHORT).show();
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
