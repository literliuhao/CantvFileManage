package com.cantv.media.center.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by yibh on 2017/3/13.
 * 拷贝/粘贴文件的工具类
 */

public class CopyPasUtils {

    /**
     * @param path
     * @return 是否复制成功
     */
    public static boolean copyFilePath(String path) {

        return false;
    }

    /**
     * @param oldPath  复制的文件路径
     * @param destPath 目标文件路径
     * @return 是否拷贝成功
     */
    public static boolean copyFile(String oldPath, String destPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            //TODO 复制的文件不存在
            return false;
        } else if (!oldFile.isFile()) {
            //// TODO: 不是文件
            return false;
        }

        File newFile = new File(destPath);
        if (newFile.exists()) {  //新文件已经存在
            //TODO  已经存在同名文件,可以提示是否覆盖
        } else { //新文件不存在
            if (!newFile.getParentFile().exists()) {  //父路径不存在,就新建
                if (!newFile.getParentFile().mkdirs()) {    //新建文件夹失败
                    return false;
                }
            }
        }

        //开始真正的复制文件
        int byteread = 0;
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(oldFile);
            out = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }

                if (in != null) {
                    in.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 拷贝文件
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean nioTransferCopy(String source, String target) {
        File oldFile = new File(source);
        if (!oldFile.exists()) {
            //TODO 复制的文件不存在
            return false;
        } else if (!oldFile.isFile()) {
            //// TODO: 不是文件
            return false;
        }

        File newFile = new File(target);
        if (newFile.exists()) {  //新文件已经存在
            //TODO  已经存在同名文件,可以提示是否覆盖
        } else { //新文件不存在
            if (!newFile.getParentFile().exists()) {  //父路径不存在,就新建
                if (!newFile.getParentFile().mkdirs()) {    //新建文件夹失败
                    return false;
                }
            }
        }

        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(oldFile);
            outStream = new FileOutputStream(newFile);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (in != null) {
                    in.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
                if (out != null) {
                    out.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 移动文件(reNameTo方法可能受文件系统(ntfs与fat32等不同的情况)的影响会造成失败)
     * 1.检测最上层路径,判断是否在同一个盘里,如果在同一个盘,使用reNameToe方法(经过测试,发现此方法失败)
     * 2.不在一个盘,优先使用先复制后删除的方式
     *
     * @param oldPath    将复制的文件整体路径
     * @param targetPath 待复制到的文件路径的上父路径(文件夹)
     * @return
     */
    public static boolean moveFile(String oldPath, String targetPath) {
//        int oldSep = oldPath.indexOf(File.separator);   //第一个"/"
//        int targetSep = targetPath.indexOf(File.separator);
//        String oldSub = oldPath.substring(0, oldSep);
//        String targetSub = targetPath.substring(0, targetSep);
//        String fileName = oldPath.substring(oldPath.lastIndexOf("/"));
        File oldFile = new File(oldPath);
//        if ((oldSep == targetSep) && (oldSub.equals(targetSub))) { //使用reNameTo方法
//            if (targetPath.endsWith("/")) {  //文件夹
//                targetPath = targetPath + oldFile.getName();
//            } else { //文件
//                targetPath = targetPath + File.separator + oldFile.getName();
//            }
//
//            File newFile = new File(targetPath);
//            boolean b = oldFile.renameTo(newFile);
//            return b;
//        } else {
        //使用复制->删除的方式

        boolean copySuccess = nioTransferCopy(oldPath, targetPath);
        Log.w("复制结果", copySuccess + "");
        if (copySuccess) {   //复制成功
            if (oldFile.exists() && oldFile.canWrite()) {
                if (oldFile.isFile()) {
                    boolean delete = oldFile.delete();
                    if (delete) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


}
