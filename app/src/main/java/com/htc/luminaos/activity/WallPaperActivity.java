package com.htc.luminaos.activity;

import static com.htc.luminaos.utils.BlurImageView.MAX_BITMAP_SIZE;
import static com.htc.luminaos.utils.BlurImageView.narrowBitmap;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.htc.luminaos.MyApplication;
import com.htc.luminaos.R;
import com.htc.luminaos.adapter.WallPaperAdapter;
import com.htc.luminaos.databinding.ActivityWallpaperCustomBinding;
import com.htc.luminaos.utils.BlurImageView;
import com.htc.luminaos.utils.Contants;
import com.htc.luminaos.utils.DialogUtils;
import com.htc.luminaos.utils.LogUtils;
import com.htc.luminaos.utils.StorageUtils;
import com.htc.luminaos.utils.Utils;
import com.htc.luminaos.widget.FocusKeepRecyclerView;
import com.htc.luminaos.widget.SpacesItemDecoration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallPaperActivity extends BaseActivity {

    private ActivityWallpaperCustomBinding wallPaperBinding;
    private ArrayList<File> file_toArray = new ArrayList<>();

    ExecutorService singer = Executors.newSingleThreadExecutor();
    ExecutorService threadExecutor = Executors.newFixedThreadPool(5);

    private Dialog switchDialog = null;

    long curTime = 0;
    private static String TAG = "WallPaperActivity";

    private MyApplication myApplication;
    private Dialog loadingDialog;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Contants.PICTURE_NULL:
                    wallPaperBinding.folderResult.setBackgroundResource(R.drawable.folder_x);
                    break;
                case Contants.PICTURE_RESULT:
                    if (msg.obj != null) {
                        File[] files = (File[]) msg.obj;
                        WallPaperAdapter wallPaperAdapter = new WallPaperAdapter(WallPaperActivity.this, files, handler);
                        wallPaperAdapter.setWallPaperOnCallBack(onCallBack);
                        wallPaperBinding.wallpaperRv.setAdapter(wallPaperAdapter);
                    }
                    wallPaperBinding.folderResult.setVisibility(View.GONE);
                    wallPaperBinding.wallpaperRv.setVisibility(View.VISIBLE);
                    break;
                case Contants.PICTURE_FIND:
                    wallPaperBinding.folderResult.setVisibility(View.VISIBLE);
                    wallPaperBinding.folderResult.setBackgroundResource(R.drawable.folder);
                    wallPaperBinding.wallpaperRv.setVisibility(View.GONE);
                    break;
                case Contants.DISSMISS_DIALOG:
                    if (switchDialog != null && switchDialog.isShowing())
                        switchDialog.dismiss();

                    setWallPaper();
                    break;
                case Contants.RESET_CHECK:
                    int receivedPosition = msg.arg1;
                    FocusKeepRecyclerView.ViewHolder viewHolder = wallPaperBinding.wallpaperRv.findViewHolderForAdapterPosition(receivedPosition);
                    if (viewHolder != null) {
                        Log.d(TAG, " 图片背景选择 receivedPosition" + receivedPosition);
                        View itemView = viewHolder.itemView;
                        ImageView check = itemView.findViewById(R.id.check);
                        check.setImageResource(R.drawable.check_no);
                        check.setVisibility(View.GONE);
                    }
                    break;

            }

            return false;
        }
    });

    BroadcastReceiver mediaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d("xuhao", "aciton " + intent.getAction());
            if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())
                    || Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())
                    || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(intent.getAction())
                    || Intent.ACTION_MEDIA_REMOVED.equals(intent.getAction())) {
                if (System.currentTimeMillis() - curTime < 300)
                    return;

                curTime = System.currentTimeMillis();
                StorageVolume storage = (StorageVolume) intent.getParcelableExtra(
                        StorageVolume.EXTRA_STORAGE_VOLUME);
                String path = storage.getPath();
//                if (isExternalStoragePath(path)) {
//                    if (wallPaperBinding.usbItem.isSelected())
//                        loadUSB();
//                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, " 执行onCreate");
        super.onCreate(savedInstanceState);
        wallPaperBinding = ActivityWallpaperCustomBinding.inflate(LayoutInflater.from(this));
        setContentView(wallPaperBinding.getRoot());
        observeLiveData();
        initView();
        getPath();
        initData();
        initFocus();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
//        getPath();
//        wallPaperBinding.wallpaperRv.getAdapter().notifyDataSetChanged();
//        initFocus();
        Log.d(TAG, " 执行onResume");
    }

    private void observeLiveData() {
        Log.d(TAG, " 执行observeLiveData");
        myApplication = (MyApplication) getApplication();
        if (myApplication != null) {
//            MutableLiveData<Boolean> mutableLiveData = myApplication.getIsDataInitialized();
            Boolean isInitialized = myApplication.getIsDataInitialized().getValue();
            if (!isInitialized) {
                Log.d(TAG, " 背景资源还在加载中");
                //显示动画
                showLottieLoading();
                //监听LiveData
                myApplication.getIsDataInitialized().observe(this, isInitializedValue -> {
                    if (isInitializedValue != null && isInitializedValue) {
                        WallPaperAdapter adapter = (WallPaperAdapter) wallPaperBinding.wallpaperRv.getAdapter();
                        adapter.notifyDataSetChanged();
                        loadingDialog.dismiss();
                    }
                });

            } else if (isInitialized) {
                Log.d(TAG, " 背景资源已经加载完成");
            }

        }
    }

    private void initView() {
        Log.d(TAG, " initView");
//        wallPaperBinding.localItem.setOnClickListener(this);
//        wallPaperBinding.usbItem.setOnClickListener(this);
//        GridLayoutManager layoutManager = new GridLayoutManager(this,6);//原生是6列
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        wallPaperBinding.wallpaperRv.setLayoutManager(layoutManager);
        wallPaperBinding.wallpaperRv.addItemDecoration(new SpacesItemDecoration(SpacesItemDecoration.pxAdapter(22.5F), SpacesItemDecoration.pxAdapter(22.5F), SpacesItemDecoration.pxAdapter(10), SpacesItemDecoration.pxAdapter(10)));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        registerReceiver(mediaReceiver, intentFilter);
    }

    private void initData() {
//        wallPaperBinding.localItem.setSelected(true);
//        wallPaperBinding.usbItem.setSelected(false);
        loadLocal();
    }

    private void initFocus() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String path = bundle.getString("filePath");
            Log.d(TAG, " 接收到路径 " + path);
            if (path != null && !path.isEmpty()) {
                FocusKeepRecyclerView wallpaperRv = wallPaperBinding.wallpaperRv;
                RecyclerView.Adapter adapter = wallpaperRv.getAdapter();
                if (adapter != null) {
                    int position = adapter.getItemCount() - 2; // 倒数第二个项的位置
                    wallpaperRv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            // 确保 RecyclerView 已完成布局
                            Log.d(TAG, "RecyclerView 已完成布局");
                            // 获取目标项的 ViewHolder
                            RecyclerView.ViewHolder viewHolder = wallpaperRv.findViewHolderForAdapterPosition(position);
                            if (viewHolder != null) {//不需要滚动的
                                Log.d(TAG, "找到目标项，设置焦点");
                                viewHolder.itemView.requestFocus();
                                viewHolder.itemView.performClick();
                            } else {//需要滚动的
                                wallpaperRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                    @Override
                                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                        super.onScrollStateChanged(recyclerView, newState);
                                        // 检查滚动是否结束
                                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                            RecyclerView.ViewHolder scrolledViewHolder = wallpaperRv.findViewHolderForAdapterPosition(position);
                                            if (scrolledViewHolder != null) {
                                                Log.e(TAG, "滚动后切背景");
                                                scrolledViewHolder.itemView.requestFocus();
                                                scrolledViewHolder.itemView.performClick();
                                            } else {
                                                Log.e(TAG, "无法找到指定位置的 ViewHolder");
                                            }
                                            // 滚动完成后移除监听器，避免重复调用
                                            wallpaperRv.removeOnScrollListener(this);
                                        }
                                    }
                                });
                                wallpaperRv.smoothScrollToPosition(position);
                                Log.e(TAG, "目标项不可见，无法设置焦点");
                            }
                            // 移除监听器，避免多次调用
                            wallpaperRv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                }
            }
        }
    }

    private void loadLocal() {
        Log.d(TAG," 执行loadLocal");
        WallPaperAdapter wallPaperAdapter = new WallPaperAdapter(getApplicationContext(), Utils.drawables, handler, wallPaperBinding.wallpaperRv);
//        wallPaperAdapter.setHasStableIds(true);
        wallPaperAdapter.setWallPaperOnCallBack(onCallBack);
        wallPaperBinding.wallpaperRv.setAdapter(wallPaperAdapter);
        wallPaperBinding.wallpaperRv.setVisibility(View.VISIBLE);
        wallPaperBinding.folderResult.setVisibility(View.GONE);
    }

    public boolean isExternalStoragePath(String path) {
        if (path.equals("/storage/emulated/0")) {
            return false;
        }
        return true;
    }

    WallPaperAdapter.WallPaperOnCallBack onCallBack = new WallPaperAdapter.WallPaperOnCallBack() {
        @Override
        public void WallPaperLocalChange(Drawable drawable) {
            switchDialog = DialogUtils.createLoadingDialog(WallPaperActivity.this, getString(R.string.switch_wallpaper_tips));
            switchDialog.show();
            threadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    CopyDrawableToSd(drawable);
//                    CopyResIdToSd(BlurImageView.BoxBlurFilter(WallPaperActivity.this, resId));
//                    if (new File(Contants.WALLPAPER_MAIN).exists())
//                        MyApplication.mainDrawable = new BitmapDrawable(BitmapFactory.decodeFile(Contants.WALLPAPER_MAIN));
//                    if (new File(Contants.WALLPAPER_OTHER).exists())
//                        MyApplication.otherDrawable = new BitmapDrawable(BitmapFactory.decodeFile(Contants.WALLPAPER_OTHER));
//                    handler.sendEmptyMessage(Contants.DISSMISS_DIALOG);
                }
            });
        }

        @Override
        public void WallPaperUsbChange(File file) {
            switchDialog = DialogUtils.createLoadingDialog(WallPaperActivity.this, getString(R.string.switch_wallpaper_tips));
            switchDialog.show();
            threadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    CopyFileToSd(file);
                    Log.d(TAG, "执行CopyFileToSd前");
                    CopyFileToSd(BlurImageView.BoxBlurFilter(BitmapFactory.decodeFile(file.getAbsolutePath())));
                    Log.d(TAG, "执行CopyFileToSd后");
                    if (new File(Contants.WALLPAPER_MAIN).exists()) {
                        MyApplication.mainDrawable = new BitmapDrawable(BitmapFactory.decodeFile(Contants.WALLPAPER_MAIN));
                    }
                    if (new File(Contants.WALLPAPER_OTHER).exists())
                        MyApplication.otherDrawable = new BitmapDrawable(BitmapFactory.decodeFile(Contants.WALLPAPER_OTHER));
                    handler.sendEmptyMessage(Contants.DISSMISS_DIALOG);
                }
            });
        }
    };

    private void CopyResIdToSd(int resId) {
        File file = new File(Contants.WALLPAPER_DIR);
        if (!file.exists())
            file.mkdir();


        InputStream inputStream = getResources().openRawResource(resId);
        try {
            File file1 = new File(Contants.WALLPAPER_MAIN);
            if (file1.exists())
                file1.delete();

            FileOutputStream fileOutputStream = new FileOutputStream(file1);

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, bytesRead);
            }
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void CopyDrawableToSd(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        //判断图片大小，如果超过限制就做缩小处理
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width * height * 4 >= MAX_BITMAP_SIZE) {
            bitmap = narrowBitmap(bitmap);
        }
        //缩小完毕
        MyApplication.mainDrawable = new BitmapDrawable(bitmap);
        handler.sendEmptyMessage(Contants.DISSMISS_DIALOG);
        File dir = new File(Contants.WALLPAPER_DIR);
        if (!dir.exists()) dir.mkdirs();
        File file1 = new File(Contants.WALLPAPER_MAIN);
//        if (file1.exists()) file1.delete();
        try (FileOutputStream fileOutputStream = new FileOutputStream(file1)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream); // 可根据需要更改格式
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CopyResIdToSd(Bitmap bitmap) {
        File file1 = new File(Contants.WALLPAPER_DIR);
        if (!file1.exists())
            file1.mkdir();

        File file = new File(Contants.WALLPAPER_OTHER);//将要保存图片的路径
        if (file.exists())
            file.delete();
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CopyFileToSd(File file) {
        File file1 = new File(Contants.WALLPAPER_DIR);
        if (!file1.exists())
            file1.mkdir();

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //判断图片大小，如果超过限制就做缩小处理
        if (width * height * 4 >= MAX_BITMAP_SIZE) {
            bitmap = narrowBitmap(bitmap);
        }
        //缩小完毕
        try {
            File file2 = new File(Contants.WALLPAPER_MAIN);
            if (file2.exists())
                file2.delete();

            //现在的逻辑bitmap输出到文件
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file2));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
            //原来的逻辑文件输出到文件
//            FileInputStream fileInputStream = new FileInputStream(file);
//            FileOutputStream fileOutputStream = new FileOutputStream(file2);
//
//            byte[] buf = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = fileInputStream.read(buf)) != -1) {
//                fileOutputStream.write(buf, 0, bytesRead);
//            }
//            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CopyFileToSd(Bitmap bitmap) {
        File file1 = new File(Contants.WALLPAPER_DIR);
        if (!file1.exists())
            file1.mkdir();

        File file = new File(Contants.WALLPAPER_OTHER);//将要保存图片的路径
        if (file.exists())
            file.delete();
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUSB() {
        handler.sendEmptyMessage(Contants.PICTURE_FIND);
        List<String> listPaths = StorageUtils.getUSBPaths(this);
        file_toArray.clear();
        File[] fileList = null;
        if (listPaths.size() > 0) {
            for (int i = 0; i < listPaths.size(); i++) {
                File file = new File(listPaths.get(i));
                if (file.canRead()) {
                    file_toArray.addAll(Arrays.asList(file.listFiles(pictureFilter)));
                }
            }
            fileList = file_toArray.toArray(new File[0]);
            if (fileList.length == 0) {
                handler.sendEmptyMessage(Contants.PICTURE_NULL);
            } else {
                Message message = handler.obtainMessage();
                message.what = Contants.PICTURE_RESULT;
                message.obj = fileList;
                handler.sendMessage(message);
            }
        } else {
            handler.sendEmptyMessage(Contants.PICTURE_NULL);
        }
    }

    public FileFilter pictureFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            // TODO Auto-generated method stub
            // keep all needed files
            try {
                if (pathname.isDirectory()) {
                    /*filesNum++;
                    return true;*/
                    File[] files = pathname.listFiles(pictureFilter);
                    if (files != null && files.length > 0)
                        file_toArray.addAll(Arrays.asList(files));
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                return false;
            }

            String name = pathname.getAbsolutePath();
            if (isPictureFile(name)) {
                return true;
            }

            return false;
        }
    };


    public static boolean isPictureFile(String path) {
        try {
            String ext = path.substring(path.lastIndexOf(".") + 1);
            if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpeg")
                    || ext.equalsIgnoreCase("jpg")
                    || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("jfif")
                    || ext.equalsIgnoreCase("tiff") || ext.equalsIgnoreCase("webp")) {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.usb_item:
//                if (wallPaperBinding.usbItem.isSelected())
//                    break;
//
//                wallPaperBinding.localItem.setSelected(false);
//                wallPaperBinding.usbItem.setSelected(true);
//
//                singer.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadUSB();
//                    }
//                });
//                break;
//            case R.id.local_item:
//                if (wallPaperBinding.localItem.isSelected())
//                    break;
//
//                wallPaperBinding.localItem.setSelected(true);
//                wallPaperBinding.usbItem.setSelected(false);
//                loadLocal();
//                break;
        }
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mediaReceiver);

        if (!singer.isShutdown()) {
            singer.shutdown();
            singer.shutdownNow();
        }

        if (!threadExecutor.isShutdown()) {
            threadExecutor.shutdown();
            threadExecutor.shutdownNow();
        }
        super.onDestroy();
    }

    private void getPath() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String path = bundle.getString("filePath");
            Log.d(TAG, " 接收到路径 " + path);
            String copypath = copyFileToWallpaperFolder(path);
            Bitmap bitmap = BitmapFactory.decodeFile(copypath);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            Utils.drawables.add(drawable);
//            Utils.FILE_PATH = path;
//            Utils.drawables.add(-1);
        }else {
            Log.d(TAG, " getPath bundle == null ");
        }
    }

    public String copyFileToWallpaperFolder(String sourcePath) {
        // 目标文件夹路径
        String targetDirPath = Environment.getExternalStorageDirectory() + "/.mywallpaper/";
        File targetDir = new File(targetDirPath);

        // 检查并创建目标文件夹
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            Log.e(TAG, "无法创建目标文件夹: " + targetDirPath);
            return "-1";
        }
        Log.d(TAG, "目标文件夹已存在或创建成功: " + targetDirPath);

        // 创建目标文件对象（保持与源文件相同的文件名）
        File sourceFile = new File(sourcePath);
        File targetFile = new File(targetDir, sourceFile.getName());

        // 拷贝文件
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fis.close();
            fos.close();
            Log.d(TAG, "文件拷贝成功: " + targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "文件拷贝失败: " + e.getMessage());
            return "-1";
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void showLottieLoading() {
        // 创建 Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.wapper_load);
        loadingDialog.setCancelable(false); // 禁用点击外部关闭
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); // 透明背景
//        LottieAnimationView lottieLoadingView = loadingDialog.findViewById(R.id.loadingAnimation);
//        if (lottieLoadingView != null) {
//            lottieLoadingView.setSpeed(2.0f); // 设置为 2 倍速播放
//        }
        // 显示 Dialog
        loadingDialog.show();
    }

}