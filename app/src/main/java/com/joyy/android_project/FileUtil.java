package com.joyy.android_project;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;

/**
 * 一些常用的对文件的操作
 *
 * @author hyatt
 */
public class FileUtil {

    private static final Boolean DEBUG = true;
    private static final String TAG = "FileUtil";
    /**
     * 存储在存储空间上的根目录位置。<br>
     * 注意：路径末尾不允许添加'/',即'/apusapps/launcher/'是错误的<br>
     */
    private static final String LOCAL_BASE_FILE_PATH = StringCodeUtils.decodeString(new byte[]{-14, 22, 7, 87, 55, 22, 7, 7, 55, -14});

    private static FileOperator sFileOperatorImpl;

    public static void setFileOperatorImpl(FileOperator fileOperatorImpl) {
        if (fileOperatorImpl != null) {
            sFileOperatorImpl = fileOperatorImpl;
        }
    }

    public static void setFakeFileOperatorImpl() {
        if (DEBUG) {
            setFileOperatorImpl(new FileOperator() {
                @Override
                public InputStream openLatestFile(Context context, String fileName) throws IOException {
                    return context.getAssets().open(fileName);
                }
            });
        }
    }

    @Deprecated
    public static boolean copyAssetToFile(Context c, String name, File target, boolean withSignature) {
        return copyAssetToFile(c, name, target);
    }

    /**
     * 将 assets 目录下的原始文件复制到指定位置
     *
     * @param context Context
     * @param name    assets 目录下的文件名
     * @param target  目标文件
     * @return 复制成功返回 true，出错则返回 false.
     */
    public static boolean copyAssetToFile(Context context, String name, File target) {
//        InputStream sourceStream = null;
//        OutputStream targetStream = null;
//
//        try {
//            sourceStream = context.getAssets().open(name);
//            targetStream = new FileOutputStream(target);
//            int copied = IOUtils.copy(sourceStream, targetStream);
//            if (DEBUG) {
//                Log.i(TAG, "Asset " + name + ": " + copied + " bytes copied.");
//            }
//            return true;
//        } catch (IOException e) {
//            if (DEBUG) {
//                Log.e(TAG, "copyAssetToFile", e);
//            }
//        } finally {
//            ResourceUtil.closeQuietly(sourceStream);
//            ResourceUtil.closeQuietly(targetStream);
//        }

        return false;
    }

    @Deprecated
    public static InputStream openLatestFile(Context context, String name) throws IOException {
        if (sFileOperatorImpl != null) {
            InputStream inputStream = sFileOperatorImpl.openLatestFile(context, name);
            // 这里为了保持兼容，原来此方法不会返回null，没文件时抛异常
            if (inputStream == null) {
                throw new FileNotFoundException(name + " not found");
            }
            return inputStream;
        } else {
            if (DEBUG) {
                Log.e(TAG, "openLatestFile: 调用此方法前，必须保证云控模块先初始化");
            }
        }
        return context.getAssets().open(name);
    }

    @Deprecated
    public static String openLatestFile2String(Context c, String name) {
        try {
            InputStream in = openLatestFile(c, name);
            return getFileText(in);
        } catch (IOException e) {
            if (DEBUG) {
                Log.d(TAG, "err", e);
            }
        }

        return null;
    }

    public static String getFileText(File file) {
//        FileInputStream fin = null;
//        try {
//            fin = new FileInputStream(file);
//            return getFileText(fin);
//        } catch (Exception e) {
//            if (DEBUG) {
//                Log.d(TAG, "err", e);
//            }
//        } finally {
//            IOUtils.closeQuietly(fin);
//        }

        return null;
    }

    private static String getFileText(InputStream in) {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(in);
            CharArrayWriter caw = new CharArrayWriter();
            int len;
            char[] buffer = new char[8 * 1024];
            while ((len = reader.read(buffer)) > 0) {
                caw.write(buffer, 0, len);
            }
            return caw.toString();
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "err", e);
            }
        } finally {
            closeQuietly(reader);
        }

        return null;
    }

    public static void closeQuietly(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception var2) {
                if (DEBUG) {
                    Log.e(TAG, "closeCursor: ", var2);
                }
            }
        }
    }
    /**
     * Copies the contents of an InputStream to an OutputStream using a copy buffer of 4KB. The contents of the
     * InputStream are read until the end of the stream is reached, but neither the source nor the destination are
     * closed. You must do this yourself outside of the method call. The number of bytes read/written is returned.
     *
     * @param source The source InputStream.
     * @param target The destination OutputStream.
     * @return The number of bytes read/written.
     */
    public static long copyStream(InputStream source, OutputStream target) throws IOException {
        final int BUF_SIZE = 4096;
        byte[] buffer = new byte[BUF_SIZE];
        int length = 0;
        long total = 0;
        while ((length = source.read(buffer)) > 0) {
            target.write(buffer, 0, length);
            total += length;
        }
        target.flush();
        return total;
    }

    /**
     * 生成 SD 卡（或者内部存储空间）路径的文件名。如果有多张SD卡，以遍历到到第一个状态为 {@link Environment#MEDIA_MOUNTED} 的 SD 卡为准
     */
    public static File getExternalStorageFile(Context c, String filename) {
        StorageManager storageManager = (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
        try {
            // getVolumeList 和 getVolumeState 等方法都是 @hide 的，所以需要用反射来调用
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", (Class[]) null);
            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
            Object[] storageVolumes = (Object[]) getVolumeList.invoke(storageManager, (Object[]) null);
            Object storageVolume = storageVolumes[0];
            if (c.getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.Q
                    || Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                Method getPath = storageVolume.getClass().getMethod("getPath", (Class[]) null);
                for (int i = 0; i < storageVolumes.length; i++) {
                    storageVolume = storageVolumes[i];
                    String path = (String) getPath.invoke(storageVolume, (Object[]) null);
                    if (Environment.MEDIA_MOUNTED.equals(getVolumeState.invoke(storageManager, path))) {
                        if (DEBUG) {
                            Log.d(TAG, "target or sdk version less than 30 : getExternalStorageFile: " + new File(path, filename).toString());
                        }
                        return new File(path, filename);
                    }
                }
            } else {
                Method getDirectory = storageVolume.getClass().getMethod("getDirectory", (Class[]) null);
                for (int i = 0; i < storageVolumes.length; i++) {
                    storageVolume = storageVolumes[i];
                    File path = (File) getDirectory.invoke(storageVolume, (Object[]) null);
                    if (path != null) {
                        if (DEBUG) {
                            Log.d(TAG, "target or sdk version more than 30 : getExternalStorageFile: " + new File(path, filename).toString());
                        }
                        return new File(path, filename);
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }

        return null;
    }

    /**
     * 查询当前手机上所有的状态为 {@link Environment#MEDIA_MOUNTED} SD 卡，包括内部存储和外部SD卡
     *
     * @return 返回所有 SD 卡的全路径的 ArrayList.
     */
    public static ArrayList<String> getInternalAndExternalStoragePath(Context c) {
        ArrayList<String> pathList = new ArrayList<String>();

        StorageManager storageManager = (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
        try {
            // getVolumeList 和 getVolumeState 等方法都是 @hide 的，所以需要用反射来调用
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", (Class[]) null);
            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
            Object[] storageVolumes = (Object[]) getVolumeList.invoke(storageManager, (Object[]) null);
            Object storageVolume = storageVolumes[0];
            if (c.getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.Q
                    || Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                Method getPath = storageVolume.getClass().getMethod("getPath", (Class[]) null);
                for (int i = 0; i < storageVolumes.length; i++) {
                    storageVolume = storageVolumes[i];
                    String path = (String) getPath.invoke(storageVolume, (Object[]) null);
                    String state = (String) getVolumeState.invoke(storageManager, path);
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        if (DEBUG) {
                            Log.d(TAG, "target or sdk version less than 30 : getExternalStorageFile: " + path);
                        }
                        pathList.add(path);
                    } else {
                        if (DEBUG) {
                            Log.i(TAG, path + " state: " + state);
                        }
                    }
                }
            } else {
                Method getDirectory = storageVolume.getClass().getMethod("getDirectory", (Class[]) null);
                for (int i = 0; i < storageVolumes.length; i++) {
                    storageVolume = storageVolumes[i];
                    File path = (File) getDirectory.invoke(storageVolume, (Object[]) null);
                    if (path != null) {
                        pathList.add(path.getAbsolutePath());
                        if (DEBUG) {
                            Log.d(TAG, "target or sdk version more than 30 : : getExternalStorageFile: " + path.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }

        return pathList;
    }

    /**
     * Deletes a directory recursively. Copies from org.apache.commons.io.FileUtils.
     *
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (directory.isDirectory()) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Cleans a directory without deleting it. Copies from org.apache.commons.io.FileUtils.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories. Copies from
     * org.apache.commons.io.FileUtils.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted. (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be {@code null}
     * @throws NullPointerException  if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException           in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            deleteFile(file);
        }
    }

    private static void deleteFile(File file) throws IOException {
        boolean filePresent = file.exists();
        if (!file.delete()) {
            if (!filePresent) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            String message = "Unable to delete file: " + file;
            throw new IOException(message);
        }
    }

    /**
     * 确认一个目录存在，如果不存在，则尝试创建此目录。
     *
     * @param path 目录的全路径名
     * @return 如果目录存在，则返回 true，如果无法创建此目录，则返回 false.
     */
    public static boolean makeSurePathExists(String path) {
        File file = new File(path);
        return makeSurePathExists(file);
    }

    /**
     * @see #makeSurePathExists(String)
     */
    public static boolean makeSurePathExists(File path) {
        if (path == null) {
            return false;
        }
        if (path.isDirectory()) {
            return true;
        }

        if (!path.exists()) {
            return path.mkdirs();
        } else {
            // 存在，但是上面的 isDirectory() 返回了 false，说明这是一个已经存在的文件，不是目录
            return false;
        }
    }

    /**
     * 由于保存在应用程序的 files 目录的数据在卸载之后就会丢失，另外该目录没有 root 过也无法在 adb 中访问，因此一些必要的数据我们保存在 SD 卡上，例如本地备份数据、调试数据等等。
     * 修改原因：原来直接把/apusapps/launcher/ 写死在该jar中，造成其他工程都把文件写入到了launcher文件夹中
     * 注意，所有的存储路径都是在apusapps根目录下，即'/apusapps/luancher' '/apusapps/notify' '/apusapps/booster'.所以建议filepath传递工程名称且都小写
     *
     * @param filepath 注意路径前后不应该有'/'.
     */
    public static String getExternalDataStoreDir(String filepath) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            StringBuilder builder = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
            builder.append(LOCAL_BASE_FILE_PATH);
            builder.append(filepath);
            String folderPath = builder.toString();
            if (makeSurePathExists(folderPath)) {
                return folderPath;
            }
        }
        return null;
    }

    /**
     * 返回存储在存储空间上的根目录位置。
     */
    public final static String getLocalBaseFilePath() {
        String res;
        if (TextUtils.isEmpty(LOCAL_BASE_FILE_PATH)) {
            res = new String(new byte[]{47, 97, 112, 117, 115, 97, 112, 112, 115, 47, 108, 97, 117, 110, 99, 104, 101, 114});
        } else {
            res = LOCAL_BASE_FILE_PATH;
        }
        return res;
    }

    /**
     * 在给定路径的后面附加文件或者目录。
     */
    public static String pathAppend(String path, String more) {
        StringBuffer buffer = new StringBuffer(path);
        if (!path.endsWith("/")) {
            buffer.append('/');
        }
        buffer.append(more);

        return buffer.toString();
    }

    /**
     * copy a file from srcFile to destFile, return true if succeed, return
     *
     * @param srcFile
     * @param destFile
     * @return false if fail
     */
    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        if (srcFile != null && srcFile.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(srcFile);
                result = copyToFile(in, destFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
        return result;
    }

    /**
     * Copy data from a source stream to destFile. Return true if succeed,
     * Return true if succeed, return false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        boolean result = false;
        OutputStream out = null;
        try {
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            } else if (destFile.exists()) {
                destFile.delete();
            }
            out = new FileOutputStream(destFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }

            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            if (out != null) {
                try {
                    out.flush();
                } catch (Exception e) {
                    result = false;
                }
                try {
                    out.close();
                } catch (Exception e) {
                    result = false;
                }
            }
        }
        if (!result) {
            destFile.delete();
        }
        return result;
    }

    public static void copyFileDir(File srcDir, File destDir, boolean forceDelete) throws IOException {
        if (!srcDir.exists()) {
            String message = srcDir + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!srcDir.isDirectory()) {
            String message = srcDir + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = srcDir.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }

        makeSurePathExists(destDir.getPath());

        for (File file : files) {
            String child = file.getName();
            if (file.isDirectory()) {
                copyFileDir(file, new File(destDir, child), forceDelete);
            } else {
                copyFile(file, new File(destDir, child));
                if (forceDelete) {
                    forceDelete(file);
                }
            }
        }
        if (forceDelete) {
            deleteFile(srcDir);
        }
    }

    public static String generateFakePrefix(Context c, String prefix) {
//        StringBuilder builder = new StringBuilder(prefix);
//        builder.append(PhoneId.getAndroidId(c));
//        String md5 = ConvertUtil.getMD5(builder.toString());
//        if (!TextUtils.isEmpty(md5)) {
//            if (DEBUG) {
//                Log.d(TAG, "md5 = " + md5);
//            }
//            return md5.substring(0, 4);
//        }
        return null;
    }

    public static String generateFakeDir(Context c, String path) {
//        StringBuilder builder = new StringBuilder(path);
//        builder.append(PhoneId.getAndroidId(c));
//        String md5 = ConvertUtil.getMD5(builder.toString());
//        if (DEBUG) {
//            Log.d(TAG, "md5 = " + md5);
//        }
//        if (!TextUtils.isEmpty(md5)) {
//            return regenerate(md5.substring(8, 24));
//        }
        return null;
    }

    private static String regenerate(String alph) {
        if (DEBUG) {
            Log.d(TAG, "alph = " + alph);
        }

        if (TextUtils.isEmpty(alph)) {
            return null;
        }
        if (TextUtils.isDigitsOnly(alph)) {
            if (DEBUG) {
                Log.d(TAG, "all digits");
            }
            return alph.substring(0, 7);
        }

        String alphabet = alph.replaceAll("[^(A-Za-z)]", "");
        if (DEBUG) {
            Log.d(TAG, "alphabet = " + alphabet);
        }
        String digit = alph.replaceAll("[(A-Za-z)]", "");
        if (DEBUG) {
            Log.d(TAG, "digit = " + digit);
        }

        if (alphabet.length() < 5) {
            if (DEBUG) {
                Log.d(TAG, "alphabet length < 5");
            }

            int subEndIndex = Math.min(4, digit.length());

            alphabet = alphabet + digit.substring(0, subEndIndex);
        }
        if (DEBUG) {
            Log.e(TAG, "final alphabet = " + alphabet);
        }
        return alphabet;
    }

    public static boolean saveObject2File(String path, Object content) {
        if (TextUtils.isEmpty(path)) {
            if (DEBUG) {
                Log.e(TAG, "path empty!");
            }
            return false;
        }
        return saveObject2File(content, new File(path));
    }

    /**
     * 保存序列化的数据到文件中，存放在file目录下
     */
    public static boolean saveObject2File(Object content, File file) {
        if (content == null) {
            if (DEBUG) {
                Log.e(TAG, "content empty!");
            }
            return false;
        }

        OutputStream out = null;
        try {
            try {
                out = new BufferedOutputStream(new FileOutputStream(file));
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(content);
                return true;
            } catch (FileNotFoundException e) {
                if (DEBUG) {
                    Log.d(TAG, "err", e);
                }
            } catch (IOException e) {
                if (DEBUG) {
                    Log.d(TAG, "err", e);
                }
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.d(TAG, "err", e);
                    }
                }
            }
        }

        return false;
    }

    public static Object readObjectFromFile(String path) {
        if (TextUtils.isEmpty(path)) {
            if (DEBUG) {
                Log.e(TAG, "path empty!");
            }
            return null;
        }
        return readObjectFromFile(new File(path));
    }

    public static Object readObjectFromFile(File file) {
        if (file == null || !file.exists()) {
            if (DEBUG) {
                Log.e(TAG, "no such file!");
            }
            return null;
        }

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            ObjectInputStream ois = new ObjectInputStream(is);
            Object obj = ois.readObject();
            return obj;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "err", e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.d(TAG, "err", e);
                    }
                }
            }
        }

        return null;
    }

    /**
     * DEBUG下读取SD卡根目录下测试属性
     */
    public static Properties getDebugAttributes() {
        if (!DEBUG) {
            return null;
        }
        return getDebugPropFile("cloud_config.prop");
    }

    public static Properties getDebugPropFile(String fileName) {
        if (!DEBUG) {
            return null;
        }
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        if (file.exists()) {
            Properties prop = new Properties();
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                prop.load(reader);
                Log.i(TAG, "getDebugPropFile: 使用" + file.getAbsolutePath() + ", " + prop);
                return prop;
            } catch (Exception e) {
                Log.e(TAG, "getDebugPropFile: 读取" + file.getAbsolutePath() + "失败");
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Log.i(TAG, "getDebugPropFile: " + file.getAbsolutePath() + "不存在");
        }
        return null;
    }

    public interface FileOperator {
        InputStream openLatestFile(Context context, String fileName) throws IOException;
    }
}
