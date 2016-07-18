package com.cantv.media.center.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class YMediaUtil {
    /**
     * 获取指定文件列表的全路径，如果对应文件存在则返回文件的路径，如果对应文件不存在，则路径为空
     * eg:
     * List<String> filePathList = FileUtils.getExistFiles(this,new String[]{"1.ptr","2.hst","3.dct"});
     * 如果1.ptr和3.dct存在，2.hst不存在则返回的结果是：
     * filePathList.get(0) = /mnt/sdcard/1.ptr
     * filePathList.get(1) = null
     * filePathList.get(2) = /mnt/sdcard/3.dct
     */
    public static List<String> getExistFiles(final Context context, final String[] fileNames, String path) {
        if (null == context || null == fileNames || fileNames.length == 0) {
            return null;
        }

        String filePath = null;
        String suffix = null;
        String linkType = null;
        List<String> filePathList = new ArrayList<String>();
        for (String fileName : fileNames) {
            suffix = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
            linkType = " LIKE '%" + suffix + "'";

            ContentResolver contentResolver = context.getContentResolver();
            if (null != contentResolver) {
                Uri uri = MediaStore.Files.getContentUri(path);
                // 为了效率起见不应当传入null,否则默认取出所有的字段。这里应当根据自己的需求来定，比如下面只需要路径、大小信息
                Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE}, MediaStore.Files.FileColumns.DATA + linkType, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                            if (filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length()).equals(fileName)) {
                                filePathList.add(filePath);
                                break;
                            }
                        } while (cursor.moveToNext());
                    }

                    if (!cursor.isClosed()) {
                        cursor.close();
                    }
                }
            }
        }

        return filePathList;
    }

    /**
     * 通知媒体库更新文件
     *
     * @param context
     * @param filePath 文件全路径
     */
    public static void scanFile(Context context, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

}
