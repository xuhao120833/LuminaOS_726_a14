package com.htc.luminaos.utils;

import android.content.Context;
import android.os.Environment;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 存储工具
 * @author GaoFei
 *
 */
public class StorageUtils { 
    public static String TAG = "StorageUtils";
    
    /**
     * 获取内置存储路径
     * @return
     */
    public static String getFlashStoragePath(){
    	return Environment.getExternalStorageDirectory().getPath();
    }
    
    /**
     * 获取SD card路径
     * @param
     * @return
     */
    public static List<String> getSdCardPaths(Context context){
    	StorageManager storageManager = (StorageManager)(context.getSystemService(Context.STORAGE_SERVICE));
    	List<String> sdPaths = new ArrayList<String>();
    	int currentVersion = android.os.Build.VERSION.SDK_INT;
    	if(currentVersion >= 23){
    		  List<VolumeInfo> volumes = storageManager.getVolumes();
    	        for (VolumeInfo vol : volumes) {
    	            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
    	                DiskInfo disk = vol.getDisk();
    	                if (disk != null) {
    	                    if (disk.isSd()) {
    	                        sdPaths.add(vol.path);
    	                    }
    	                }
    	            }
    	        }
    	}else{
    		File sdCardFile = (File)(invokeStaticMethod("android.os.Environment","getSecondVolumeStorageDirectory",new Object[]{}));
    		if(sdCardFile != null)
    			sdPaths.add(sdCardFile.getPath());
    	}
      
		
		return sdPaths;
    
    }
    
    /**
     * 获取USB路径
     * @param context
     * @return
     */
    public static List<String> getUSBPaths(Context context){
    	StorageManager storageManager = (StorageManager)(context.getSystemService(Context.STORAGE_SERVICE));
    	List<String> usbPaths = new ArrayList<String>();
    	int currentVersion = android.os.Build.VERSION.SDK_INT;
    	if(currentVersion >= 23){
    		  List<VolumeInfo> volumes = storageManager.getVolumes();
    	        for (VolumeInfo vol : volumes) {
    	            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
    	                DiskInfo disk = vol.getDisk();
    	                if (disk != null) {
    	                    if (disk.isUsb()) {
    	                        usbPaths.add(vol.path);
    	                    }
    	                }
    	            }
    	        }
    	}else{
    		List<String> dfPaths = getAllDfPaths();
    		if(dfPaths != null && dfPaths.size() > 0){
    			for(String item : dfPaths){
    				if(item.contains("/mnt/usb"))
    					usbPaths.add(item);
    			}
    		}
    	}
      
		
		return usbPaths;
    
    }
    
    /**
     * 判断挂载路径是否为USB设备
     * @param context
     * @param path
     * @return
     */
    public static boolean isMountUsb(Context context, String path){
    	boolean isUsb = false;
    	List<String> usbPaths = getUSBPaths(context);
    	if(usbPaths != null && usbPaths.size() > 0 && usbPaths.contains(path))
    		isUsb = true;
    	return isUsb;
    }
    
    /**
     * 判断挂载路径是否为SD卡
     * @param context
     * @param path
     * @return
     */
    public static boolean isMountSdCard(Context context, String path){
    	boolean isSDCard = false;
    	List<String> sdCardPaths = getSdCardPaths(context);
    	if(sdCardPaths != null && sdCardPaths.size() > 0 && sdCardPaths.contains(path))
    		isSDCard = true;
    	return isSDCard;
    }
    
    /**
     * 判断是否已经挂载SD卡
     * @param context
     * @param
     * @return
     */
    public static boolean isMountSdCard(Context context){
    	boolean isSDCard = false;
    	List<String> sdCardPaths = getSdCardPaths(context);
    	if(sdCardPaths != null && sdCardPaths.size() > 0)
    		isSDCard = true;
    	return isSDCard;
    }
    
    /**
     * 通过执行df命令获取所有分区路径
     * @return
     */
    public static List<String> getAllDfPaths(){
    	List<String> paths = new ArrayList<String>();
    	List<String> dfStrs = execdfCommond();
    	if(dfStrs != null && dfStrs.size() > 0){
    		for(String item : dfStrs){
        		try{
        			paths.add(item.split("\\s+")[0]);
        		}catch(Exception e){
        			
        		}
        		
        	}
    	}
    	
    	return paths;
    } 
    
    /**执行df命令，获取磁盘分区的一些信息*/
	public static List<String> execdfCommond() {
		List<String> list = new ArrayList<String>();
		try {
			Process su = Runtime.getRuntime().exec("df\n");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(su.getInputStream()));
			try {
				String line = bufferedReader.readLine();
				while (!TextUtils.isEmpty(line)) {
					Log.i(TAG, "line:" + line);
					list.add(line);
					line = bufferedReader.readLine();
				}
			} catch (Exception e) {
				Log.i(TAG, "read:" + e);
			}

			try {
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
				Log.i(TAG, "close:" + e);
			}
		} catch (Exception e) {
			Log.i(TAG, "" + e);
		}

		return list;
	}

    
    /**
     * Invoke static method
     * @param cls the target class
     * @param methodName the name of method
     * @param arguments the value of arguments
     * @return the result of invoke method
      */
    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object... arguments) {
        try {
            Class<?>[] parameterTypes = null;
            if(arguments!=null){
                parameterTypes = new Class<?>[arguments.length];
                for(int i=0; i<arguments.length; i++){
                    parameterTypes[i] = arguments[i].getClass();
                }
            }
            Method method = cls.getMethod(methodName, parameterTypes);
            return method.invoke(null, arguments);
        }catch (Exception ex) {
            return null;
        }
    }

    public static Object invokeStaticMethod(String className, String methodName, Object... arguments) {
        try {
            Class<?> cls = Class.forName(className);
            return invokeStaticMethod(cls, methodName, arguments);
        }catch (Exception ex) {
            return null;
        }
    }

    public static Object invokeStaticMethod(String className, String methodName, Class<?>[] types , Object... arguments) {
        try {
            Class<?> cls = Class.forName(className);
            return invokeStaticMethod(cls, methodName, types, arguments);
        }catch (Exception ex) {
            return null;
        }
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Class<?>[] types, Object... arguments) {
        try {
            Method method = cls.getMethod(methodName, types);
            return method.invoke(null, arguments);
        }catch (Exception ex) {
            return null;
        }
    }

    
} 


 



 
