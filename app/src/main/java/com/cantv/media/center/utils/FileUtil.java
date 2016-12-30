package com.cantv.media.center.utils;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Audio;
import com.cantv.media.center.data.Image;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.Video;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * 获取File信息工具类
 * Created by yibh on 2016/6/28.
 */
public class FileUtil {
    //保存不需要显示文件的文件名
    private static List<String> uselessFileList = new ArrayList<>();

    static {
        String[] uselessStr = {"LOST.DIR", "System Volume Information"};
        for (String s : uselessStr) {
            uselessFileList.add(s);
        }
    }

    private static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";
    private static ArrayList<ContentProviderOperation> ops = new ArrayList<>();

    /**
     * 获取外存储SD卡路径
     */
    public static String getSdDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * Sd卡状态 true SD卡正常挂载
     */
    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 是否是隐藏文件,隐藏文件返回false
     */
    public static boolean isShowFile(File file) {
        if (file.isHidden()) {
            return false;
        }
        return !file.getName().startsWith(".");
    }

    /**
     * 是否是隐藏文件,隐藏文件返回false
     */
    public static boolean isShowFile(SmbFile file) {
        try {
            if (file.isHidden()) {
                return false;
            }
        } catch (SmbException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return !file.getName().startsWith(".");
    }

    /**
     * 根据路径获取文件名
     *
     * @param path
     * @return
     */
    public static String onPathToFileName(String path) {
        if (path.equals("/")) {
            return "/";
        }
        int index;
        index = path.lastIndexOf("/");
        if (index == -1 || !path.startsWith("/")) {
            try {
                throw new IllegalStateException("非标准路径" + path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return path.substring(index + 1);
    }

    /**
     * 是否是正常常见文件
     */
    public static boolean isNormalFile(String fileName) {
        return !fileName.equals(ANDROID_SECURE);
    }

    /**
     * @param file
     * @param filter
     * @param showOrHidden 是否显示隐藏文件
     * @return
     */
    public static Media getFileInfo(File file, FilenameFilter filter, boolean showOrHidden) {
        SourceType fileType = FileUtil.getFileType(file);
        // 下面分类的写法是为了显示缩略图
        Media fileBean;
        if (fileType == SourceType.MOIVE) {
            fileBean = new Video(fileType, file.getAbsolutePath());
        } else if (fileType == SourceType.MUSIC) {
            fileBean = new Audio(fileType, file.getAbsolutePath());
        } else if (fileType == SourceType.PICTURE) {
            fileBean = new Image(fileType, file.getAbsolutePath());
        } else {
            fileBean = new Media(fileType, file.getAbsolutePath());
        }
        fileBean.canRead = file.canRead();
        fileBean.canWrite = file.canWrite();
        fileBean.isHidden = file.isHidden();
        fileBean.mName = file.getName();
        fileBean.modifiedDate = file.lastModified();
        fileBean.isDir = file.isDirectory();
        fileBean.mUri = file.getPath();
        // 文件夹时计算出总的下一级文件/夹数量
        if (fileBean.isDir) { //层级可能过深,不建议计算文件夹容量大小
        } else {
            // 文件大小
            fileBean.fileSize = file.length();
        }
        return fileBean;
    }

    /**
     * @param file
     * @param filter
     * @param showOrHidden    是否显示隐藏文件
     * @param proxyPathPrefix
     * @return
     */
    public static Media getSmbFileInfo(SmbFile file, FilenameFilter filter, boolean showOrHidden, String proxyPathPrefix) {
        SourceType fileType = FileUtil.getSmbFileType(file);
        // 下面分类的写法是为了显示缩略图
        Media fileBean;
        if (fileType == SourceType.MOIVE) {
            fileBean = new Video(fileType, file.getPath());
        } else if (fileType == SourceType.MUSIC) {
            fileBean = new Audio(fileType, file.getPath());
        } else if (fileType == SourceType.PICTURE) {
            fileBean = new Image(fileType, file.getPath());
        } else {
            fileBean = new Media(fileType, file.getPath());
        }
        try {
            fileBean.canRead = file.canRead();
            fileBean.canWrite = file.canWrite();
            fileBean.isHidden = file.isHidden();
            fileBean.mName = file.getName();
            // 有些是没有时间的,就成了默认时间1970,所以重新设置了时间
            if (DateUtil.onDate2String(new Date(file.lastModified()), "yyyy.MM.dd").equals("1970.01.01")) {
                fileBean.modifiedDate = new Date().getTime() - 3600000;
            } else {
                fileBean.modifiedDate = file.lastModified();
            }
            fileBean.isDir = file.isDirectory();
            fileBean.mUri = file.getPath();
            fileBean.mTotalSize = file.getContentLength();
            fileBean.fileSize = file.getContentLength();
            fileBean.isSharing = true;
            fileBean.sharePath = proxyPathPrefix + URLEncoder.encode(fileBean.mUri.substring(6), "UTF-8");
            // fileBean.sharePath = "http://"+fileBean.mUri.split("@")[1];
        } catch (SmbException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 文件夹时计算出总的下一级文件/夹数量
        if (fileBean.isDir) {
        } else {
            // 文件大小
            try {
                fileBean.fileSize = file.length();
            } catch (SmbException e) {
                e.printStackTrace();
            }
        }
        return fileBean;
    }

    /**
     * 返回指定路径的文件/夹 列表
     *
     * @param path
     * @return
     */
    public static List<Media> getFileList(String path) {
        List<Media> tList = new ArrayList<>();
        if (null == path) {
            return tList;
        }
        try {
            File file = new File(path);
            if (!file.exists() || !file.isDirectory()) {
                return tList;
            }
            File[] listfiles = file.listFiles();
            if (listfiles == null) {
                return tList;
            }
            for (File childFile : listfiles) {
                // 是常见文件,并且是非隐藏文件
                if (FileUtil.isShowFile(childFile)) {
                    Media fileInfo = FileUtil.getFileInfo(childFile, null, false);
                    if (null != fileInfo && !uselessFileList.contains(fileInfo.mName)) {
                        // 当文件是图片类型,并且大于10k,才进行显示
                        if (fileInfo.mType == SourceType.PICTURE) {
                            if (fileInfo.fileSize > 1024 * 6) {
                                tList.add(fileInfo);
                            }
                        } else {
                            tList.add(fileInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tList;
    }

    /**
     * 返回指定路径的文件/夹 列表
     *
     * @param path
     * @param proxyPathPrefix
     * @return
     */
    public static List<Media> getSmbFileList(String path, String proxyPathPrefix) {
        List<Media> tList = new ArrayList<>();
        try {
            SmbFile file = new SmbFile(path);
            if (!file.exists() || !file.isDirectory()) {
                return tList;
            }
            SmbFile[] listfiles = file.listFiles();
            if (listfiles == null) {
                return tList;
            }
            for (SmbFile childFile : listfiles) {
                // 是常见文件,并且是非隐藏文件
                if (FileUtil.isShowFile(childFile)) {
                    Media fileInfo = FileUtil.getSmbFileInfo(childFile, null, false, proxyPathPrefix);
                    if (null != fileInfo && !uselessFileList.contains(fileInfo.mName) && (!fileInfo.mName.contains("$/"))) {
                        // 当文件是图片类型,并且大于10k,才进行显示
                        if (fileInfo.mType == SourceType.PICTURE) {
                            if (fileInfo.fileSize > 1024 * 6) {
                                tList.add(fileInfo);
                            }
                        } else {
                            tList.add(fileInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tList;
    }

    /**
     * 返回指定路径指定类型的文件/夹 列表
     *
     * @param path
     * @param type
     * @return
     */
    public static List<Media> getFileList(String path, boolean addFolder, SourceType... type) {
        List<Media> tList = new ArrayList<>();
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return tList;
        }
        File[] listfiles = file.listFiles();
        if (listfiles == null) {
            return tList;
        }
        for (File childFile : listfiles) {
            // 是常见文件,并且是非隐藏文件
            if (FileUtil.isShowFile(childFile)) {
                Media fileInfo = FileUtil.getFileInfo(childFile, null, false);
                if (null != fileInfo) {
                    // 是文件夹或这是指定类型的文件,就加入到集合中
                    SourceType sourceType = type[0];
                    if ((sourceType == fileInfo.mType) ||
                            // 过滤掉指定2个无用的文件夹
                            (addFolder && fileInfo.isDir && !uselessFileList.contains(fileInfo.mName))) {
                        // 当文件是图片类型,并且大于10k,才进行显示
                        if (fileInfo.mType == SourceType.PICTURE) {
                            if (fileInfo.fileSize > 1024 * 6) {
                                tList.add(fileInfo);
                            }
                        } else {
                            tList.add(fileInfo);
                        }
                    }
                }
            }
        }
        return tList;
    }

    /**
     * 返回指定路径指定类型的文件/夹 列表
     *
     * @param path
     * @param type
     * @return
     * @throws MalformedURLException
     * @throws SmbException
     */
    public static List<Media> getSmbFileList(String path, boolean addFolder, String proxyPathPrefix, SourceType... type) throws MalformedURLException, SmbException {
        List<Media> tList = new ArrayList<>();
        SmbFile file = new SmbFile(path);
        if (!file.exists() || !file.isDirectory()) {
            return tList;
        }
        SmbFile[] listfiles = file.listFiles();
        if (listfiles == null) {
            return tList;
        }
        for (SmbFile childFile : listfiles) {
            Media fileInfo = FileUtil.getSmbFileInfo(childFile, null, false, proxyPathPrefix);
            if (null != fileInfo) {
                // 是文件夹或这是指定类型的文件,就加入到集合中
                SourceType sourceType = type[0];
                if ((sourceType == fileInfo.mType) ||
                        // 过滤掉指定2个无卵用的文件夹
                        (addFolder && fileInfo.isDir && !uselessFileList.contains(fileInfo.mName))) {
                    // 当文件是图片类型,并且大于10k,才进行显示
                    if (fileInfo.mType == SourceType.PICTURE) {
                        if (fileInfo.fileSize > 1024 * 6) {
                            tList.add(fileInfo);
                        }
                    } else {
                        tList.add(fileInfo);
                    }
                }
            }
        }
        return tList;
    }

    public static final long SIZE_KB = 1024L;
    public static final long SIZE_MB = 1024 * 1024L;
    public static final long SIZE_GB = 1024L * 1024L * 1024L;

    /**
     * 计算文件大小
     */
    public static String convertStorage(long size) {

        if (size < SIZE_KB) {
            return size + "B";
        }

        if (size < SIZE_MB) {
            return Math.round(size * 100.0 / SIZE_KB) / 100.0 + "KB";
        }

        if (size < SIZE_GB) {
            return Math.round(size * 100.0 / SIZE_MB) / 100.0 + "MB";
        }

        return Math.round(size * 100.0 / SIZE_GB) / 100.0 + "G";

    }

    /**
     * 获取文件的绝对路径
     *
     * @return
     */
    public static String getFileAbsPath(String path, String name) {
        return path.equals("/") ? path + name : path + File.separator + name;
    }

    /**
     * 获取文件类型（后缀名）
     */
    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 获取文件类型
     *
     * @param file
     * @return
     */
    public static SourceType getFileType(File file) {
        if (file.isDirectory()) {
            return SourceType.FOLDER;
        }
        String extFromFilename = FileUtil.getExtFromFilename(file.getAbsolutePath());
        int fileType = FileCategoryHelper.getFileType(extFromFilename);
        switch (fileType) {
            case FileCategoryHelper.TYPE_MOIVE:
                return SourceType.MOIVE;
            case FileCategoryHelper.TYPE_MUSIC:
                return SourceType.MUSIC;
            case FileCategoryHelper.TYPE_PICTURE:
                return SourceType.PICTURE;
            case FileCategoryHelper.TYPE_APP:
                return SourceType.APP;
            case FileCategoryHelper.TYPE_UNKNOW:
                return SourceType.UNKNOW;
        }
        return null;
    }

    /**
     * 获取文件类型
     *
     * @param file
     * @return
     */
    public static SourceType getSmbFileType(SmbFile file) {
        try {
            if (file.isDirectory()) {
                return SourceType.FOLDER;
            }
        } catch (SmbException e) {
            e.printStackTrace();
            return SourceType.UNKNOW;
        }
        String extFromFilename = FileUtil.getExtFromFilename(file.getPath());
        int fileType = FileCategoryHelper.getFileType(extFromFilename);
        switch (fileType) {
            case FileCategoryHelper.TYPE_MOIVE:
                return SourceType.MOIVE;
            case FileCategoryHelper.TYPE_MUSIC:
                return SourceType.MUSIC;
            case FileCategoryHelper.TYPE_PICTURE:
                return SourceType.PICTURE;
            case FileCategoryHelper.TYPE_APP:
                return SourceType.APP;
            case FileCategoryHelper.TYPE_UNKNOW:
                return SourceType.UNKNOW;
        }
        return null;
    }

    /**
     * 获取apk图标
     *
     * @param context
     * @param apkPath
     * @return
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.w("apk图标", e.toString());
            }
        }
        return null;
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersionName(Context context) {
        String version = null;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context.getString(R.string.home_version) + version;
    }

    /**
     * 得到指定文件大小
     *
     * @param file
     * @return
     */
    public static long getFileSize(File file) {
        long size = 0;
        // File file = new File(filePath);
        // if (null == file) {
        // return size;
        // }
        if (file.isFile()) {
            return size += file.length();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    size = size + getFileSize(file1);
                }
            }
        }
        return size;
    }

    /**
     * 文件排序
     *
     * @param list
     * @param mode    如果传递的不是默认模式,就要进行判断是否保存模式成功,在成功后再进行排序,方便下次使用
     * @param isFirst 判断是否初次进入,初次进入会进行排序
     */
    public static boolean sortList(List list, int mode, boolean isFirst) {
        if (isFirst) {
            Collections.sort(list, new FileComparator());
            return true;
        } else {
            if (mode != SharedPreferenceUtil.getSortType()) {
                // Collections.sort(list, new FileComparator());
                boolean isOk = SharedPreferenceUtil.setSortType(mode);
                if (isOk) {
                    Collections.sort(list, new FileComparator());
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean deleteFile(Media f) {
        if (f == null) {
            return false;
        }
        File file = new File(f.mUri);
        boolean directory = file.isDirectory();
        if (directory) {
            for (File child : file.listFiles()) {
                if (FileUtil.isNormalFile(child.getAbsolutePath())) {
                    deleteFile(FileUtil.getFileInfo(child, null, true));
                }
            }
        }
        if (!f.isDir) {
            ops.add(ContentProviderOperation.newDelete(getMediaUriFromFilename(f.mName)).withSelection("_data = ?", new String[]{f.mUri}).build());
        }
        return file.delete();
    }

    /*
     * 从文件名获取Mediauri
     */
    public static Uri getMediaUriFromFilename(String filename) {
        // String extString = getExtFromFilename(filename);
        String volumeName = "external";
        int fileType = FileCategoryHelper.getFileType(FileUtil.getExtFromFilename(filename));
        Uri uri = null;
        if (fileType == FileCategoryHelper.TYPE_MUSIC) {
            uri = MediaStore.Audio.Media.getContentUri(volumeName);
        } else if (fileType == FileCategoryHelper.TYPE_PICTURE) {
            uri = MediaStore.Images.Media.getContentUri(volumeName);
        } else if (fileType == FileCategoryHelper.TYPE_MOIVE) {
            uri = MediaStore.Video.Media.getContentUri(volumeName);
        } else {
            uri = MediaStore.Files.getContentUri(volumeName);
        }
        return uri;
    }

    /*
     * 创建异步线程
     */
    public static void asnycExecute(Runnable r) {
        final Runnable _r = r;
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                synchronized (mCurFileNameList) {
                    _r.run();
                }
                if (!(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH)) {
                    try {
                        //TODO 5.0以下系统可能出错
                        MyApplication.mContext.getContentResolver().applyBatch(MediaStore.AUTHORITY, ops);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return null;
                    } catch (OperationApplicationException e) {
                        // TODO: handle exception
                        e.printStackTrace();
                        return null;
                    }
                }
                ops.clear();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static ArrayList<Media> mCurFileNameList = new ArrayList<>();

    /**
     * 删除文件,不太容易回调结果,暂时不用
     */
    public static boolean delete(Media media) {
        Log.w("路径", media.isSharing ? media.sharePath : media.mUri);
        copyFileList(media);
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for (Media f : mCurFileNameList) {
                    deleteFile(f);
                }
                clear();
            }
        });
        return true;
    }

    public static void copyFileList(Media file) {
        synchronized (mCurFileNameList) {
            mCurFileNameList.clear();
            mCurFileNameList.add(file);
        }
    }

    public static void clear() {
        synchronized (mCurFileNameList) {
            mCurFileNameList.clear();
        }
    }

    /**
     * 将media中的路径提取出来
     *
     * @param
     * @return
     */
    public static ArrayList<String> getMediaPathList(String path, SourceType sourceType) {
        ArrayList<String> tList = new ArrayList<>();
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return tList;
        }
        File[] listfiles = file.listFiles();
        if (listfiles == null) {
            return tList;
        }
        for (File childFile : listfiles) {
            // 是常见文件,并且是非隐藏文件
            if (FileUtil.isShowFile(childFile)) {

                Media fileInfo = FileUtil.getFileInfo(childFile, null, false);
                if (null != fileInfo) {
                    // 是文件夹或这是指定类型的文件,就加入到集合中
                    if (sourceType == fileInfo.mType) {
                        tList.add(fileInfo.mUri);
                    }
                }
            }
        }
        return tList;
    }

    /**
     * 从集合中取出指定类型的数据,组成新的集合
     *
     * @param fromList
     * @param sourceType
     * @return
     */
    public static ArrayList<String> getListFromList(List<Media> fromList, SourceType sourceType) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (Media media : fromList) {
            if (sourceType == media.mType) {
                arrayList.add(media.isSharing ? media.sharePath : media.mUri);
            }
        }
        return arrayList;
    }

    /**
     * 获取路径
     *
     * @param fromList
     * @return
     */
    public static ArrayList<String> getListFromList(List<Media> fromList) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (Media media : fromList) {
            arrayList.add(media.isSharing ? media.sharePath : media.mUri);
        }
        return arrayList;
    }

    /**
     * 从集合中取出指定类型的数据,组成新的Media集合
     *
     * @param fromList
     * @param sourceType
     * @return
     */
    public static ArrayList<Media> getMediaListFromList(List<Media> fromList, SourceType sourceType) {
        ArrayList<Media> arrayList = new ArrayList<>();
        for (Media media : fromList) {
            if (sourceType == media.mType) {
                arrayList.add(media);
            }
        }
        return arrayList;
    }

    /**
     * 找出与目标字符串相同的字符串在集合中的索引
     *
     * @param list
     * @param oldPath
     * @return
     */
    public static int getIndexFromList(ArrayList<String> list, String oldPath) {
        for (int i = 0; i < list.size(); i++) {
            if (oldPath.equals(list.get(i))) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 找出与目标字符串相同的字符串在集合中的索引
     *
     * @param list
     * @param oldPath
     * @return
     */
    public static int getMediaIndexFromList(ArrayList<Media> list, String oldPath) {
        for (int i = 0; i < list.size(); i++) {
            Media media = list.get(i);
            if (media.isSharing) {
                if (oldPath.equals(media.sharePath)) {
                    return i;
                }
            } else {
                if (oldPath.equals(media.mUri)) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 判断一个集合中是否包含另外一个集合的值
     *
     * @param firstList
     * @param twoList
     * @return
     */
    public static boolean isListConOtListValue(List<String> firstList, List<String> twoList) {
        if (null == firstList || null == twoList || firstList.size() == 0 || twoList.size() == 0) {
            return false;
        }
        if (firstList.size() > twoList.size()) {
            return listConCompare(firstList, twoList);

        } else {
            return listConCompare(twoList, firstList);
        }
    }

    /**
     * 判断一个集合中是否包含另外一个集合的值,请使用上面的方法
     *
     * @param firstList
     * @param twoList
     * @return
     */
    private static boolean listConCompare(List<String> firstList, List<String> twoList) {
        for (String st : twoList) {
            if (firstList.contains(st)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 数组转换成集合
     *
     * @param array
     * @return
     */
    public static List<String> arrayToList(String[] array) {
        ArrayList<String> strings = new ArrayList<>();
        if (null == array || array.length < 1) {
            return strings;
        }

        for (String anArray : array) {
            strings.add(anArray);
        }
        return strings;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}