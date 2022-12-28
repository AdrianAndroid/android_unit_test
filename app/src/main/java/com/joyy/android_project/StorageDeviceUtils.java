package com.joyy.android_project;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class StorageDeviceUtils {

    private static final Boolean DEBUG = true;
    private static final String TAG = "StorageDeviceUtils";

    /**
     * 获取路径下的总空间及可用空间大小 （单位byte）
     *
     * @return
     */
    public static StorageInfo getStorageDeviceInfo(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        StorageInfo storageDevice = new StorageInfo();

        try {
            StatFs stat = new StatFs(path);
            long blockSize = 0;
            long totalBlocks = 0;
            long availableBlocks = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                try {
                    blockSize = stat.getBlockSizeLong();
                    totalBlocks = stat.getBlockCountLong();
                    availableBlocks = stat.getAvailableBlocksLong();
                } catch (Throwable e) {
                    blockSize = stat.getBlockSize();
                    totalBlocks = stat.getBlockCount();
                    availableBlocks = stat.getAvailableBlocks();
                }
            } else {
                blockSize = stat.getBlockSize();
                totalBlocks = stat.getBlockCount();
                availableBlocks = stat.getAvailableBlocks();
            }

            storageDevice.path = path;
            storageDevice.totalSize = totalBlocks * blockSize;
            storageDevice.freeSize = availableBlocks * blockSize;
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }

        return storageDevice;
    }

    /**
     * 获取所有设备的存储信息，包括：手机存储空间、内置存储空间、外置存储空间
     * 注意：这里面有可能出现，手机存储空间和内置存储空间是同一片区域
     *
     * @param context
     * @return
     */
    public static List<StorageInfo> getAllStorageDeviceInfo(Context context) {
        if (DEBUG) {
            Log.i(TAG, "getAllStorageDeviceInfo **start**");
        }
        List<StorageInfo> res = new ArrayList<StorageInfo>(3);

        StorageInfo systemStorageDevice = null;//手机磁盘路径信息
        StorageInfo internalStorageDevice = null;//内置存储磁盘路径信息
        StorageInfo externalStorageDevice = null;//外置存储磁盘路径信息

        ArrayList<String> storagePathList = FileUtil.getInternalAndExternalStoragePath(context);
        if (storagePathList == null) {
            if (DEBUG) {
                Log.i(TAG, "getAllStorageDeviceInfo fail. 获取外部或者内置存储空间路径错误.");
            }
            return res;
        }

        String externalStorageDirectory = Environment.getExternalStorageDirectory().getPath();
        Method isExternalStorageRemovableMethod = null;
        boolean isExternalStorageRemovable = false;
        try {
            isExternalStorageRemovableMethod = Environment.class.getMethod("isExternalStorageRemovable");
            isExternalStorageRemovable = (Boolean) isExternalStorageRemovableMethod.invoke(Environment.class,
                    (Object[]) null);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }

        String internalStorageDevicePath = null;
        String externalStorageDevicePath = null;

        int pathSize = storagePathList.size();

        if (pathSize >= 2) {
            String path0 = storagePathList.get(0);
            String path1 = storagePathList.get(1);

            if (externalStorageDirectory.equals(path0) && isExternalStorageRemovable) {
                externalStorageDevicePath = path0;
                internalStorageDevicePath = path1;
            } else {
                externalStorageDevicePath = path1;
                internalStorageDevicePath = path0;
            }

        } else if (pathSize == 1) {
            String path0 = storagePathList.get(0);
            if (isExternalStorageRemovable) {
                externalStorageDevicePath = path0;
            } else {
                internalStorageDevicePath = path0;
            }
        }

        //系统存储
        String syspath = Environment.getDataDirectory().getPath();
        systemStorageDevice = getStorageDeviceInfo(syspath);
        systemStorageDevice.type = StorageInfo.StorageType.SYSTEM;
        if (DEBUG) {
            Log.d(TAG, "手机磁盘路径：" + syspath);
        }

        if (!TextUtils.isEmpty(internalStorageDevicePath)) {
            //内置存储卡
            internalStorageDevice = getStorageDeviceInfo(internalStorageDevicePath);
            internalStorageDevice.type = StorageInfo.StorageType.INTERNAL;
        }

        if (!TextUtils.isEmpty(externalStorageDevicePath)) {
            //外置存储卡
            externalStorageDevice = getStorageDeviceInfo(externalStorageDevicePath);
            externalStorageDevice.type = StorageInfo.StorageType.EXTERNAL;
        }

        if (DEBUG) {
            Log.d(TAG, "内置存储卡路径 = " + internalStorageDevicePath + ";外置存储卡路径 = " + externalStorageDevicePath);
        }

        if (systemStorageDevice != null) {
            res.add(systemStorageDevice);
        }
        if (internalStorageDevice != null) {
            res.add(internalStorageDevice);
        }
        if (externalStorageDevice != null) {
            res.add(externalStorageDevice);
        }

        if (DEBUG) {
            Log.d(TAG, "getAllStorageDeviceInfo=" + res);
            Log.i(TAG, "getAllStorageDeviceInfo **end**");
        }

        return res;
    }

    /**
     * 获取所有有效路径的磁盘信息。主要解决：
     * 1.sys磁盘空间和内置存储空间是同一片空间
     * 2.内置存储空间是系统磁盘空间中的一部分的问题
     *
     * @param context
     * @return
     */
    private static List<StorageInfo> getAllValidStorageDeviceInfo(Context context) {
        if (DEBUG) {
            Log.i(TAG, "getAllValidStorageDeviceInfo **start**");
        }
        List<StorageInfo> res = getAllStorageDeviceInfo(context);
        StorageInfo systemStorageDevice = null;//手机磁盘路径信息
        StorageInfo internalStorageDevice = null;//内置存储磁盘路径信息
        int internalStorageDeviceIndex = -1;
        for (int i = res.size() - 1; i >= 0; i--) {
            StorageInfo sinfo = res.get(i);
            switch (sinfo.type) {
                case SYSTEM:
                    systemStorageDevice = sinfo;
                    break;
                case INTERNAL:
                    internalStorageDevice = sinfo;
                    internalStorageDeviceIndex = i;
                    break;
                default:
                    break;
            }
        }

        //去重， 避免系统手机存储与内置存储设备重复
        if ((internalStorageDevice != null) && (systemStorageDevice != null)) {
            if ((internalStorageDevice.totalSize == systemStorageDevice.totalSize)
                    && (internalStorageDevice.freeSize == systemStorageDevice.freeSize)) {
                internalStorageDevice = null;
                res.remove(internalStorageDeviceIndex);
                if (DEBUG) {
                    Log.d(TAG, "内置存储空间和外置存储空间大小相同，说明手机存储与内置存储是同一片存储");
                }
            }
        }

        if (internalStorageDevice != null && (systemStorageDevice != null)) {
            if (internalStorageDevice.totalSize < systemStorageDevice.totalSize * 2) {
                internalStorageDevice = null;
                res.remove(internalStorageDeviceIndex);
                if (DEBUG) {
                    Log.d(TAG, "内置存储空间必须>=系统存储空间2倍，否则说明手机存储包含内置存储");
                }
            }
        }

        if (DEBUG) {
            Log.i(TAG, "getAllValidStorageDeviceInfo 处理后的结果：" + res);
            Log.i(TAG, "getAllValidStorageDeviceInfo **end**");
        }
        return res;
    }

    /**
     * 获取data文件系统的大小信息
     */
    public static long getDataFileSpaceTotalSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0;
        long totalBlocks = 0;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                try {
                    blockSize = stat.getBlockSizeLong();
                    totalBlocks = stat.getBlockCountLong();
                } catch (Throwable e) {
                    blockSize = stat.getBlockSize();
                    totalBlocks = stat.getBlockCount();
                }
            } else {
                blockSize = stat.getBlockSize();
                totalBlocks = stat.getBlockCount();
            }
        } catch (Throwable e) {
            //解决moto 手机，报
//            {STACK_TRACE=java.lang.NullPointerException: Attempt to invoke
//                interface method 'java.util.Iterator java.lang.Iterable.iterator()' on a null
//                object reference
//                at android.os.StatFs.isThirdpartyApp(StatFs.java:199)
//                at android.os.StatFs.getBlockCount(StatFs.java:100)
//                at org.interlaken.common.utils.MemInfo.getTotalInternalMemorySize('':4)
            if (DEBUG) {
                Log.e(TAG, "err", e);
            }
        }
        return totalBlocks * blockSize;
    }

    /**
     * 获取data文件系统剩余空间的大小信息
     */
    public static long getDataFileSpaceFreeSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    /**
     * 获取已使用的存储总空间（包含手机存储、内置存储和外值存储）
     */
    public static long getUsedAllStorageSize(Context context) {
        if (DEBUG) {
            Log.i(TAG, "getUsedAllStorageSize **start**");
        }
        List<StorageInfo> list = getAllValidStorageDeviceInfo(context);
        long totalStorage = 0;
        long freeStorage = 0;
        if (list != null && list.size() > 0) {
            for (StorageInfo storageDevice : list) {
                totalStorage += storageDevice.totalSize;
                freeStorage += storageDevice.freeSize;
            }
        }
        long usedStorage = totalStorage - freeStorage;
        usedStorage = usedStorage > 0 ? usedStorage : 0;
        if (DEBUG) {
            Log.v(TAG, "已使用的存储空间（包括手机存储、内置存储、外置存储）  getUsedAllStorageSize:" + usedStorage);
            Log.i(TAG, "getUsedAllStorageSize **end**");
        }
        return usedStorage;
    }

    /**
     * 获取手机的总共存储
     */
    public static long getTotalAllStorageSize(Context context) {
        if (DEBUG) {
            Log.i(TAG, "getTotalAllStorageSize **start**");
        }
        List<StorageInfo> list = getAllValidStorageDeviceInfo(context);
        long totalStorage = 0;
        if (list != null && list.size() > 0) {
            for (StorageInfo storageDevice : list) {
                totalStorage += storageDevice.totalSize;
            }
        }
        if (DEBUG) {
            Log.v(TAG, "总的存储空间（包括手机存储、内置存储、外置存储）  getTotalAllStorageSize:" + totalStorage);
            Log.i(TAG, "getTotalAllStorageSize **end**");
        }
        return totalStorage;
    }

    /**
     * 获取手机的所有剩余存储。
     */
    public static long getFreeAllStorageSize(Context context) {
        if (DEBUG) {
            Log.i(TAG, "getFreeAllStorageSize **start**");
        }
        List<StorageInfo> list = getAllValidStorageDeviceInfo(context);
        long freeStorage = 0;
        if (list != null && list.size() > 0) {
            for (StorageInfo storageDevice : list) {
                freeStorage += storageDevice.freeSize;
            }
        }
        if (DEBUG) {
            Log.v(TAG, "剩余的存储空间（包括手机存储、内置存储、外置存储）  getFreeAllStorageSize:" + freeStorage);
            Log.i(TAG, "getFreeAllStorageSize **end**");
        }
        return freeStorage;
    }

    /**
     * 获取手机存储使用的百分比
     *
     * @param context
     * @return
     */
    public static float getStorageUsedPercent(Context context) {
        if (DEBUG) {
            Log.i(TAG, "getStorageUsedPercent **start**");
        }
        List<StorageInfo> list = getAllValidStorageDeviceInfo(context);
        long totalStorage = 0;
        long freeStorage = 0;
        if (list != null && list.size() > 0) {
            for (StorageInfo storageDevice : list) {
                totalStorage += storageDevice.totalSize;
                freeStorage += storageDevice.freeSize;
            }
        }
        long usedStorage = totalStorage - freeStorage;
        usedStorage = usedStorage > 0 ? usedStorage : 0;
        float percent = (float) usedStorage / (float) totalStorage;
        if (DEBUG) {
            Log.d(TAG, "usedStorage=" + usedStorage + ";totalStorage=" + totalStorage);
            Log.v(TAG, "已使用的存储空间百分比（包括手机存储、内置存储、外置存储）  getStorageUsedPercent:" + percent);
            Log.i(TAG, "getStorageUsedPercent **end**");
        }
        return percent;
    }

    public static class StorageInfo {
        /**
         * 储存设备类型
         */
        public StorageType type;
        /**
         * 总大小
         */
        public long totalSize;
        /**
         * 剩余大小
         */
        public long freeSize;
        /**
         * 存储设备系统路径
         */
        public String path;

        @Override
        public String toString() {
            String res = super.toString();
            if (DEBUG) {
                StringBuilder builder = new StringBuilder();
                builder.append("{StorageInfo数据结构 type:");
                builder.append(type);
                builder.append(", totalSize:");
                builder.append(totalSize);
                builder.append(", freeSize:");
                builder.append(freeSize);
                builder.append(", path:");
                builder.append(path);
                builder.append("}");
                res = builder.toString();
            }
            return res;
        }

        /**
         * 存储设备类型：手机存储、内置存储卡、外置存储卡
         */
        public enum StorageType {
            SYSTEM, INTERNAL, EXTERNAL
        }
    }
}
