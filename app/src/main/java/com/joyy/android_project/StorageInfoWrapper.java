package com.joyy.android_project;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bf on 2018/1/1.
 */

public class StorageInfoWrapper {
    public static final boolean AT_LEAST_KITKAT_WATCH = SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH;
    public static final boolean AT_LEAST_KITKAT = SDK_INT >= Build.VERSION_CODES.KITKAT;
    public static final boolean AT_LEAST_LOLLIPOP = SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final boolean AT_LEAST_LOLLIPOP_MR1 = SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    public static final boolean AT_LEAST_JELLY_BEAN_MR2 = SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    public static final boolean AT_LEAST_JELLY_BEAN_MR1 = SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    public static final boolean AT_LEAST_JELLY_BEAN = SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean AT_LEAST_M = SDK_INT >= M;
    public static final boolean AT_LEAST_N = SDK_INT >= Build.VERSION_CODES.N;
    public static final boolean DEBUG = true;
    private static final long DEFAULT_VALUE = -1;
    /**
     * 内核可访问的总内存。
     */
    private long memTotal = DEFAULT_VALUE;
    /**
     * 系统上的可用内存。
     */
    private long memAvail = DEFAULT_VALUE;
    /**
     * 内部存储总内存
     */
    private long internalStorageTotal = DEFAULT_VALUE;
    /**
     * 内部存可用内存
     */
    private long internalAvail = DEFAULT_VALUE;
    /**
     * 外部存储总内存
     */
    private long externalStorageTotal = DEFAULT_VALUE;
    /**
     * 外部存储可用内存
     */
    private long externalStorageAvail = DEFAULT_VALUE;

    public static StorageInfoWrapper obtain(Context context) {
        StorageInfoWrapper infoWrapper = new StorageInfoWrapper();

        // 下面使用do-while(false)结构, 是想达到前面一旦成功, 后面就不需要执行的效果
        do {
            //AT_LEAST_JELLY_BEAN
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);
                infoWrapper.memTotal = memoryInfo.totalMem;
                infoWrapper.memAvail = memoryInfo.availMem;
                break;
            }
            long[] memInfo = MemInfo.getMemoryInfo();
            infoWrapper.memTotal = memInfo[0];
            infoWrapper.memAvail = memInfo[1];
        } while (false);

        // 内部存储
        infoWrapper.internalStorageTotal = MemInfo.getTotalInternalMemorySize();
        infoWrapper.internalAvail = MemInfo.getAvailableInternalMemorySize();
        // 外部存储
        long[] externalStorageInfo = getExternalStorageInfo(context);
        if (externalStorageInfo != null) {
            infoWrapper.externalStorageAvail = externalStorageInfo[0];
            infoWrapper.externalStorageTotal = externalStorageInfo[1];
        }

        return infoWrapper;
    }

    /**
     * @return 当前设备当中可用的外部存储空间大小，第一个元素是可用存储空间大小，第二个元素是总的存储空间大小
     */
    public static long[] getExternalStorageInfo(Context context) {
        String externalStoragePath = ExternalStorage.getExternalStoragePath(context);
        if (!TextUtils.isEmpty(externalStoragePath)) {
            return getPathStorageInfo(externalStoragePath);
        }
        return new long[]{-1, -1};
    }

    public static class ExternalStorage {

        public static String getExternalStoragePath(Context context) {
            String externalStoragePath = null;

            //方案1
            List<DiskSpaceInfo> diskSpaceInfoList = getExternalStorageFromVolumeList(context);
            if (diskSpaceInfoList != null && !diskSpaceInfoList.isEmpty()) {
                for (DiskSpaceInfo d : diskSpaceInfoList) {
                    if (externalStoragePath == null) {
                        externalStoragePath = d.path;
                    }
                    if (d.type == DiskSpaceInfo.TYPE_EXTERNAL) {//外置
                        externalStoragePath = d.path;
                    }
                }
            } else {
                //方案2
                String external_storage = System.getenv("EXTERNAL_STORAGE");
                if (checkDirectoryValid(external_storage)) {
                    externalStoragePath = external_storage;
                }
                String secondary_storage = System.getenv("SECONDARY_STORAGE");
                if (!TextUtils.isEmpty(secondary_storage)) {
                    String[] split = secondary_storage.split(":");
                    if (split.length > 0) {
                        secondary_storage = split[0];
                        if (checkDirectoryValid(secondary_storage) &&
                                getStorageSize(secondary_storage) > 0 &&
                                !TextUtils.equals(external_storage, secondary_storage)) {
                            externalStoragePath = secondary_storage;
                        }
                    }
                }
                if (TextUtils.isEmpty(externalStoragePath)) {
                    //方案3
                    if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
                        externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    }
                }
            }

            return externalStoragePath;
        }

        static long getStorageSize(String path) {
            try {
                StatFs statFs = new StatFs(path);
                return (long) statFs.getBlockCount() * statFs.getBlockSize();
            } catch (Exception e) {
                if (DEBUG) {
//                    Log.e(TAG, "getStorageSize: ", e);
                }
                return 0;
            }
        }

        private static boolean checkDirectoryValid(String path) {
            File file = new File(path);
            return file.exists() && file.isDirectory();
        }

        /**
         * 从系统的存储服务中获取外置路径的接口
         */
        static List<DiskSpaceInfo> getExternalStorageFromVolumeList(Context context) {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (sm == null) {
                return null;
            }
            try {
                Method SGetVolumeList = sm.getClass().getMethod("getVolumeList");
                if (!SGetVolumeList.isAccessible()) {
                    SGetVolumeList.setAccessible(true);
                }
                Method methodGetVolumePaths = sm.getClass().getMethod("getVolumePaths");
                if (methodGetVolumePaths == null) {
                    return null;
                }
                if (!methodGetVolumePaths.isAccessible()) {
                    methodGetVolumePaths.setAccessible(true);
                }
                String[] volumePaths = (String[]) methodGetVolumePaths.invoke(sm);
                if (volumePaths == null || volumePaths.length == 0) {
                    if (DEBUG) {
//                        Log.v(TAG, "VolumePaths is empty");
                    }
                    return null;
                }

                Method methodGetVolumeState = sm.getClass().getMethod("getVolumeState", String.class);
                if (methodGetVolumeState == null) {
                    return null;
                }
                methodGetVolumeState.setAccessible(true);

                String externalStoragePath = null;
                if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
                    File directory = Environment.getExternalStorageDirectory();
                    if (directory != null) {
                        externalStoragePath = directory.getAbsolutePath();
                    }
                }

                List<DiskSpaceInfo> list = new ArrayList<DiskSpaceInfo>();
                for (String path : volumePaths) {
                    if (TextUtils.isEmpty(path)) {
                        continue;
                    }
                    try {
                        String state = (String) methodGetVolumeState.invoke(sm, new Object[]{path});
                        if (DEBUG) {
//                            Log.v(TAG, "state for " + path + " is " + state);
                        }
                        if (Environment.MEDIA_MOUNTED.equals(state)) {
                            int type;
                            if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                type = Environment.isExternalStorageRemovable(new File(path)) ?
                                        DiskSpaceInfo.TYPE_EXTERNAL : DiskSpaceInfo.TYPE_INTERNAL;
                            } else {
                                if (TextUtils.equals(path, externalStoragePath)) {
                                    type = DiskSpaceInfo.TYPE_INTERNAL;
                                } else {
                                    type = DiskSpaceInfo.TYPE_EXTERNAL;
                                }
                            }

                            DiskSpaceInfo item = new DiskSpaceInfo(path, type);
                            list.add(item);
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
//                            Log.e(TAG, "getExternalStorageFromVolumeList: ", e);
                        }
                    }
                }
                return list;
            } catch (Exception e) {
                if (DEBUG) {
//                    Log.e(TAG, "getExternalStorageFromVolumeList: ", e);
                }
            }
            return null;
        }
    }

    public static class DiskSpaceInfo {
        public static final int TYPE_INTERNAL = 0;//手机
        public static final int TYPE_EXTERNAL = 1;//sdcard
        public final int type;
        public final String path;

        DiskSpaceInfo(String path, int type) {
            this.path = path;
            this.type = type;
        }
    }

    /**
     * 获取指定路径的存储控制大小信息
     */
    public static long[] getPathStorageInfo(String path) {
        try {
            StatFs statFs = new StatFs(path);
            long blockSize = AT_LEAST_JELLY_BEAN_MR2 ? statFs.getBlockSizeLong() : statFs.getBlockSize();
            long availableBlocksCount = AT_LEAST_JELLY_BEAN_MR2 ? statFs.getAvailableBlocksLong() : statFs.getAvailableBlocks();
            long totalBlocksCount = AT_LEAST_JELLY_BEAN_MR2 ? statFs.getBlockCountLong() : statFs.getBlockCount();

            return new long[]{(blockSize * availableBlocksCount), (blockSize * totalBlocksCount)};
        } catch (Exception e) {
            if (DEBUG) {
                Log.e("TAG", "getPathStorageInfo: ", e);
            }
        }
        return null;
    }

    public long getMemTotal() {
        return memTotal;
    }

    public long getMemAvail() {
        return memAvail;
    }

    public long getInternalStorageTotal() {
        return internalStorageTotal;
    }

    public long getInternalAvail() {
        return internalAvail;
    }

    public long getExternalStorageAvail() {
        return externalStorageAvail;
    }

    public long getExternalStorageTotal() {
        return externalStorageTotal;
    }


}
