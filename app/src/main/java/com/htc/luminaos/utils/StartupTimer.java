package com.htc.luminaos.utils;

import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public class StartupTimer {
    private static final Map<String, Long> timestamps = new LinkedHashMap<>();

    public static void mark(String label) {
        timestamps.put(label, SystemClock.elapsedRealtime());
    }

    public static void print(String tag) {
        Long last = null;
        Log.d(tag, "—— 启动耗时分析 ——");
        for (Map.Entry<String, Long> entry : timestamps.entrySet()) {
            if (last == null) {
                Log.d(tag, entry.getKey() + " at " + entry.getValue() + "ms");
            } else {
                Log.d(tag, entry.getKey() + " + " + (entry.getValue() - last) + " ms");
            }
            last = entry.getValue();
        }
        Log.d(tag, "—— 结束 ——");
    }

    public static void clear() {
        timestamps.clear();
    }
}
