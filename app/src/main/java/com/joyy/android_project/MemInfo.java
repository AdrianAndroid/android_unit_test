package com.joyy.android_project;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Scanner;

/**
 * 查询系统信息的封装类
 *
 * @author hyatt
 */
public class MemInfo {
    private static final Boolean DEBUG = true;
    private static final String TAG = "MemInfo";

    /**
     * 从 /proc/meminfo 查询手机总内存、剩余内存
     *
     * @return 返回一个数组，第1个元素是总内存，单位字节；第2个元素是剩余内存，单位字节。
     */
    public static long[] getMemoryInfo() {
        long memTotal = 0;
        long memFree = 0;
        long memBuffer = 0;
        long memCache = 0;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/meminfo")));
            String line;
            while ((line = reader.readLine()) != null) {
                String s = line.trim();
                if (s.startsWith("MemTotal")) {
                    memTotal = parseNumber(s) * parseUnit(s);
                    continue;
                }
                if (s.startsWith("MemFree")) {
                    memFree = parseNumber(s) * parseUnit(s);
                    continue;
                }
                if (s.startsWith("Buffers")) {
                    memBuffer = parseNumber(s) * parseUnit(s);
                    continue;
                }
                if (s.startsWith("Cached")) {
                    memCache = parseNumber(s) * parseUnit(s);
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "getMemoryInfo: ", e);
                    }
                }
            }
        }
        return new long[]{memTotal, memFree + memBuffer + memCache};
    }

    /**
     * 查询手机机身存储剩余空间（/data 分区剩余空间）
     *
     * @return 单位字节
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 查询手机机身存储总空间（/data 分区大小）
     *
     * @return 单位字节
     */
    public static long getTotalInternalMemorySize() {
        return StorageDeviceUtils.getDataFileSpaceTotalSize();
    }

    private static long parseUnit(String s) {
        s = s.toUpperCase(Locale.US);
        if (s.endsWith("KB")) {
            return 1024;
        }
        if (s.endsWith("MB")) {
            return 1024 * 1024;
        }
        if (s.endsWith("GB")) {
            return 1024L * 1024 * 1024;
        }
        return 1;
    }

    private static long parseNumber(String s) {
        Scanner scanner = new Scanner(s).useDelimiter("[^0-9]+");
        return scanner.nextLong();
    }
}
