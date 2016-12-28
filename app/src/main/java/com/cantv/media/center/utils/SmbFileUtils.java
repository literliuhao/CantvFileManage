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
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Audio;
import com.cantv.media.center.data.Image;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.Video;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by yibh on 2016/6/28.
 */

public class SmbFileUtils {

    private static ArrayList<ContentProviderOperation> ops = new ArrayList<>();

    //保存不需要显示文件的文件名
    private static List<String> uselessFileList = new ArrayList<>();

    static {
        String[] uselessStr = {"LOST.DIR", "System Volume Information"};
        for (String s : uselessStr) {
            uselessFileList.add(s);
        }
    }

    /**
     * 是否是隐藏文件,隐藏文件返回false
     *
     * @throws SmbException
     */
    public static boolean isShowFile(SmbFile file) throws SmbException {

        if (file.isHidden()) {
            return false;
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
        int index = 0;
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
     * @param file
     * @param filter
     * @param showOrHidden 是否显示隐藏文件
     * @return
     * @throws SmbException
     */
    public static Media getFileInfo(SmbFile file, FilenameFilter filter, boolean showOrHidden) throws SmbException {

        SourceType fileType = SmbFileUtils.getFileType(file);

        // 下面分类的写法是为了显示缩略图
        Media fileBean = null;
        if (fileType == SourceType.MOIVE) {
            fileBean = new Video(fileType, file.getPath());
        } else if (fileType == SourceType.MUSIC) {
            fileBean = new Audio(fileType, file.getPath());
        } else if (fileType == SourceType.PICTURE) {
            fileBean = new Image(fileType, file.getPath());
        } else {
            fileBean = new Media(fileType, file.getPath());
        }

        String path = file.getPath();
        // File file1 = new File(path);
        fileBean.canRead = file.canRead();
        fileBean.canWrite = file.canWrite();
        fileBean.isHidden = file.isHidden();
        fileBean.mName = file.getName();
        fileBean.modifiedDate = file.lastModified();
        fileBean.isDir = file.isDirectory();
        fileBean.mUri = file.getPath();

        // 文件夹时计算出总的下一级文件/夹数量
        if (fileBean.isDir) {

        } else {
            // 文件大小
            fileBean.fileSize = file.length();
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
                if (SmbFileUtils.isShowFile(childFile)) {
                    Media fileInfo = SmbFileUtils.getFileInfo(childFile, null, false);

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
     * 返回指定路径指定类型的文件/夹 列表
     *
     * @param path
     * @param type
     * @return
     * @throws SmbException
     * @throws MalformedURLException
     */
    public static List<Media> getFileList(String path, boolean addFolder, SourceType... type) throws SmbException, MalformedURLException {
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

            // 是常见文件,并且是非隐藏文件
            if (SmbFileUtils.isShowFile(childFile)) {
                Media fileInfo = SmbFileUtils.getFileInfo(childFile, null, false);

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

        }
        return tList;
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
            return filename.substring(dotPosition + 1);
        }
        return "";
    }

    /**
     * 获取文件类型
     *
     * @param file
     * @return
     */
    public static SourceType getFileType(SmbFile file) {

//		if (file.isDirectory()) {
//			return SourceType.FOLDER;
//		}

//		String extFromFilename = SmbFileUtils.getExtFromFilename(file.getAbsolutePath());
//		int fileType = FileCategoryHelper.getFileType(extFromFilename);
//		switch (fileType) {
//		case FileCategoryHelper.TYPE_MOIVE:
//			return SourceType.MOIVE;
//
//		case FileCategoryHelper.TYPE_MUSIC:
//			return SourceType.MUSIC;
//
//		case FileCategoryHelper.TYPE_PICTURE:
//			return SourceType.PICTURE;
//
//		case FileCategoryHelper.TYPE_APP:
//			return SourceType.APP;
//
//		case FileCategoryHelper.TYPE_UNKNOW:
//			return SourceType.UNKNOW;
//
//		}

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

    protected static void deleteFile(Media f) throws SmbException, MalformedURLException {
        if (f == null)
            return;

        SmbFile file = new SmbFile(f.mUri);
        boolean directory = file.isDirectory();
        if (directory) {
            for (SmbFile child : file.listFiles()) {
                deleteFile(SmbFileUtils.getFileInfo(child, null, true));
            }
        }

        if (!f.isDir) {
            ops.add(ContentProviderOperation.newDelete(getMediaUriFromFilename(f.mName))
                    .withSelection("_data = ?", new String[]{f.mUri}).build());
        }

        file.delete();
    }

    /*
     * 从文件名获取Mediauri
     */
    public static Uri getMediaUriFromFilename(String filename) {
        // String extString = getExtFromFilename(filename);
        String volumeName = "external";
        int fileType = FileCategoryHelper.getFileType(SmbFileUtils.getExtFromFilename(filename));

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
    private static void asnycExecute(Runnable r) {
        final Runnable _r = r;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                synchronized (mCurFileNameList) {
                    _r.run();
                }

                try {
                    MyApplication.mContext.getContentResolver().applyBatch(MediaStore.AUTHORITY, ops);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                }

                // if (moperationListener != null) {
                // moperationListener.onFinish();
                // }
                ops.clear();
                return null;
            }
        }.execute();
    }

    private static ArrayList<Media> mCurFileNameList = new ArrayList<>();

    /**
     * 删除文件
     */
    public static boolean delete(Media media) {
        copyFileList(media);
        asnycExecute(new Runnable() {

            @Override
            public void run() {
                for (Media f : mCurFileNameList) {
                    try {
                        deleteFile(f);
                    } catch (SmbException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

                clear();
            }
        });
        return true;
    }

    private static void copyFileList(Media file) {
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
     * @throws MalformedURLException
     * @throws SmbException
     */
    public static ArrayList<String> getMediaPathList(String path, SourceType sourceType) throws MalformedURLException, SmbException {
        ArrayList<String> tList = new ArrayList<>();

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
            if (SmbFileUtils.isShowFile(childFile)) {
                Media fileInfo = SmbFileUtils.getFileInfo(childFile, null, false);

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
                arrayList.add(media.mUri);
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
}
