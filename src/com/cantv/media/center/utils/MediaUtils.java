package com.cantv.media.center.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cantv.media.R;
import com.cantv.media.center.activity.AudioPlayerActivity;
import com.cantv.media.center.activity.ImagePlayerActivity;
import com.cantv.media.center.activity.VideoPlayActicity;
import com.cantv.media.center.constants.FileCategory;
import com.cantv.media.center.constants.MediaFormat;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Audio;
import com.cantv.media.center.data.Media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Files.FileColumns;
import android.widget.Toast;

public class MediaUtils {
	private static final String TAG = "MediaUtils";

	// public static String getUsbRootPath() {
	// return "/storage/external_storage/udisk0/";
	// }
	private static List<String> usbList = new ArrayList<>();
	
	
	private static Map<String,Integer> mAduioIconMap=new HashMap<>();
    static {
        mAduioIconMap.put("ape",R.drawable.music_ape);
        mAduioIconMap.put("mp3",R.drawable.music_mp3);
        mAduioIconMap.put("ogg",R.drawable.music_ogg);
        mAduioIconMap.put("wma",R.drawable.music_wma);
        mAduioIconMap.put("wav",R.drawable.music_wav);
        mAduioIconMap.put("flac",R.drawable.music_flac);
    }
	

	public static String getLocalPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static List<String> getUsbRootPaths() {
		return usbList;
	}

	public static void addUsbRootPaths(String usbPath){
		if(!usbList.contains(usbPath)){
			usbList.add(usbPath);
		}
	}

	public static void removeUsbRootPaths(String usbPath){
		if(usbList.contains(usbPath)){
			usbList.remove(usbPath);
		}
	}

	public static boolean isExistUSB() {
//		usbList = runMount();
        if (usbList != null && usbList.size() > 0) {
            return true;
        }
        return false;
    }

    public static int getUSBNum() {
        if (usbList != null) {
            return usbList.size();
        }
        return 0;
    }

    public static String getInternalTotal() {
        Process mprocess;
        BufferedReader mreader;
        String temp;
        String total = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            mprocess = runtime.exec("df");
            mreader = new BufferedReader(new InputStreamReader(mprocess.getInputStream()));
            while ((temp = mreader.readLine()) != null) {
                if (temp.contains("/data")) {
                    total = temp.split("\\s+")[1];
                }
            }
            mreader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return total;
    }

    public static String getInternalFree() {
        Process mprocess;
        BufferedReader mreader;
        String temp;
        String total = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            mprocess = runtime.exec("df");
            mreader = new BufferedReader(new InputStreamReader(mprocess.getInputStream()));
            while ((temp = mreader.readLine()) != null) {
                if (temp.contains("/data")) {
                    total = temp.split("\\s+")[3];
                }
            }
            mreader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return total;
    }

    public static String getTotal(String path) {
        Process mprocess;
        BufferedReader mreader;
        String command = "df";
        String temp;
        String total = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            // command += path;
            mprocess = runtime.exec(command);

            mreader = new BufferedReader(new InputStreamReader(mprocess.getInputStream()));
            while ((temp = mreader.readLine()) != null) {
                if (temp.contains(path)) {
                    total = temp.split("\\s+")[1];
                }
            }
            mreader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return total;
    }

    public static String getFree(String path) {
        Process mprocess;
        BufferedReader mreader;
        String temp;
        String total = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            mprocess = runtime.exec("df");
            mreader = new BufferedReader(new InputStreamReader(mprocess.getInputStream()));
            while ((temp = mreader.readLine()) != null) {
                if (temp.contains(path)) {
                    total = temp.split("\\s+")[3];
                    break;
                }
            }
            mreader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return total;
    }

//	private static List<String> runMount() {
//		Set<String> set = new TreeSet<String>();
//		List<String> list = null;
//		Process mprocess;
//		BufferedReader mreader;
//		String temp;
//		Runtime runtime = Runtime.getRuntime();
//		try {
//			mprocess = runtime.exec("mount");
//			mreader = new BufferedReader(new InputStreamReader(mprocess.getInputStream()));
//			while ((temp = mreader.readLine()) != null) {
//				if (temp.contains("/dev/block/vold/8:")) {
// 					String usbpath = temp.split(" ")[1];
//					Log.i("mount","usbpath >>>>> " + usbpath);
//					set.add(usbpath);
//				}
//			}
//			mreader.close();
//			list = new ArrayList<String>(set);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//		}
//		return list;
//	}

    public static boolean isImage(String filename) {
        String[] str = {"jpg", "png", "jpeg", "bmp", "gif", "webp", "wbmp"};
        return isEqualType(filename, str);
    }

    public static boolean isVideo(String filename) {
        String[] str = {"mp4", "avi", "mkv", "mpg", "ts", "3gp", "flv", "mka", "mov", "webm", "m2ts", "vob", "mpeg", "f4v", "rmvb", "wmv", "rm"};
        return isEqualType(filename, str);
    }

    public static boolean isAudio(String filename) {
        String[] str = {"mp3", "aac", "wav", "ogg", "mid", "flac", "ape", "ac3", "wma", "m4a"};
        return isEqualType(filename, str);
    }

    public static boolean isApp(String filename) {
        String[] str = {"apk"};
        return isEqualType(filename, str);
    }

    public static boolean checkMediaSource(String uri, SourceType source) {
        if (uri.startsWith(".")) return false;
        if (isImage(uri)) return source == SourceType.PICTURE;
        if (isVideo(uri)) return source == SourceType.MOIVE;
        if (isAudio(uri)) return source == SourceType.MUSIC;
        if (isApp(uri)) {
            return source == SourceType.APP;
        } else {
            return source == SourceType.DEVICE;
        }
    }

    private static boolean isEqualType(String filename, String[] types) {
        String fileType = filename.substring(filename.lastIndexOf('.') + 1, filename.length());
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(fileType.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static void showMediaDetail(Context context, List<Media> medias, int showIndex) {
        Media curMedia = medias.get(showIndex);
        List<String> mediaFiles = new ArrayList<>();
        MediaFormat format = curMedia.getMediaFormat();
        for (Media each : medias) {
            if (each.isCollection() == false && each.getMediaFormat() == format) {
                mediaFiles.add(each.getUri());
            }
        }

        int curIndex = mediaFiles.indexOf(curMedia.getUri());
        Class mediaClass = ImagePlayerActivity.class;
        mediaClass = (format == MediaFormat.IMAGE) ? ImagePlayerActivity.class : mediaClass;
        mediaClass = (format == MediaFormat.AUDIO) ? AudioPlayerActivity.class : mediaClass;
        mediaClass = (format == MediaFormat.VIDEO) ? VideoPlayActicity.class : mediaClass;
        if (format == MediaFormat.APP) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse("file://" + mediaFiles.get(0).toString()), "application/vnd.android.package-archive");
            context.startActivity(intent);
        } else {
            Intent mIntent = new Intent(context, mediaClass);
            mIntent.putExtra("data_index", curIndex);
            mIntent.putStringArrayListExtra("data_list", (ArrayList<String>) mediaFiles);
            context.startActivity(mIntent);
        }
    }

    public static String getFileName(String filepath) {
        filepath = filepath.replace("\\040", " ");
        String filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length());
        return filename;
    }

    public static Bitmap loadBitmap(final Audio audio, final ImageCallBack imageCallBack) {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                Bitmap b = (Bitmap) msg.obj;
                imageCallBack.imageLoad(b);
            }
        };
        new Thread() {
            public void run() {
                Bitmap bitmap = audio.getImageThumbnail(800, 800);
                Message msg = handler.obtainMessage(0, bitmap);
                handler.sendMessage(msg);
            }

        }.start();
        return null;
    }

    public interface ImageCallBack {
        public void imageLoad(Bitmap bitmap);
    }

    public static boolean isEqualDevices(String sourcepath, String targetpath) {
        int index = targetpath.lastIndexOf("/");
        String tem = targetpath.substring(index + 1);
        if ((sourcepath.contains("usb_storage") || sourcepath.contains("usbotg")) && (sourcepath.contains(tem))) {
            return true;
        }
        if ((sourcepath.contains("external_sd") || sourcepath.contains("sdcard1")) && (sourcepath.contains(tem))) {
            return true;
        }
        return false;
    }

    public static String fileLength(long length) {
        if (length == 0) {
            return "0.00k";
        }
        final DecimalFormat decFormat = new DecimalFormat("0.##");
        float fileSize = (float) length / 1024;
        String sizestr = "";
        if (fileSize > 1024 * 1024) {
            fileSize = fileSize / (1024 * 1024);
            sizestr = decFormat.format(fileSize).toString() + "G";
        } else if (fileSize > 1024) {
            fileSize = fileSize / 1024;
            sizestr = decFormat.format(fileSize).toString() + "M";
        } else {
            sizestr = decFormat.format(fileSize).toString() + "K";
        }
        return sizestr;
    }

    public static String buildSelectionByCategory(FileCategory cat) {
        String selection = null;
        switch (cat) {
            case Music:
                selection = FileColumns.MIME_TYPE + " LIKE 'audio/%'";
                break;
            case Theme:
                selection = FileColumns.DATA + " LIKE '%.mtz'";
                break;
            case Apk:
                selection = FileColumns.DATA + " LIKE '%.apk'";
                break;
            default:
                selection = null;
        }
        return selection;
    }

    public static void openMedia(Context context, String path) {
        try {
            IntentBuilder.viewFile(context, path);
        } catch (Exception e) {
            Toast.makeText(context, "未安装相关应用", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 打开指定的媒体文件
     *
     * @param context
     * @param pathList
     * @param currIndex
     * @param sourceType
     */
   public static void openMediaActivity(Context context,ArrayList<String > pathList,int currIndex,SourceType sourceType){
	   openMediaActivity(context, pathList, currIndex, sourceType, true);
   }
    
    /**
     * 打开指定的媒体文件
     *
     * @param context
     * @param pathList
     * @param currIndex
     * @param sourceType
     */
   public static void openMediaActivity(Context context,ArrayList<String > pathList,int currIndex,SourceType sourceType, boolean isLocal){


       Intent intent = new Intent();
       Class currClass= null;
       if (sourceType==SourceType.MOIVE){
           currClass= VideoPlayActicity.class;
       }else if (sourceType==SourceType.MUSIC){
    	   currClass= AudioPlayerActivity.class;
       }else if (sourceType==SourceType.PICTURE){
           currClass= ImagePlayerActivity.class;
       }

       intent.setClass(context, currClass);
       intent.putStringArrayListExtra("data_list",pathList);
       intent.putExtra("data_index", currIndex);
       intent.putExtra("isLocal", isLocal);
       context.startActivity(intent);

   }
	
   
   /**
    * 根据Audio文件的拓展名,得到对应图标的id
    *
    * @param extensinName
    */
   public static int getAudioIconFromExtensionName(String extensinName) {
       return mAduioIconMap.get(extensinName);
   }
   

}
