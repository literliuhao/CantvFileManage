package com.cantv.media.center.data;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.cantv.liteplayer.core.mp3agic.ID3v2;
import com.cantv.liteplayer.core.mp3agic.InvalidDataException;
import com.cantv.liteplayer.core.mp3agic.Mp3File;
import com.cantv.liteplayer.core.mp3agic.UnsupportedTagException;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.constants.MediaFormat;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.utils.LyricParser;

import java.io.File;
import java.io.IOException;

@SuppressLint("NewApi")
public class Audio extends Media {

    private String mSinger;
    private LyricInfo mLyric;

    public String getSinger() {
        if (mSinger == null) {

        }
        return mSinger;
    }

    public void setSinger(String singer) {
        this.mSinger = singer;
    }

    public LyricInfo getLyric() {
        return mLyric;
    }

    public void setLyric(LyricInfo lyric) {
        this.mLyric = lyric;
    }

    public Audio(SourceType type, String uri) {
        super(type, uri);
    }

    @Override
    public MediaFormat getMediaFormat() {
        return MediaFormat.AUDIO;
    }

    @Override
    public Bitmap getThumbnails() {
        return getImageThumbnail(800, 800);
    }

    @Override
    protected String parseMediaName() {
        // try {
        // AbstractID3v2Tag tag = new MP3File(new File(getUri())).getID3v2Tag();
        // String songName = tag.frameMap.get("TIT2").toString();
        // int start = songName.indexOf("\"");
        // int end = songName.lastIndexOf("\"");
        // return (start >=0 && end >=0) ?songName.substring(start + 1, end) :
        // songName;
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        return super.parseMediaName();
    }

    public Bitmap getImageThumbnail(int width, int height) {
        return null;
    }

    public static Bitmap getAudioPicture(String uri, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(uri);
            byte[] embedPic = retriever.getEmbeddedPicture();
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inDither = true;
            op.outWidth = width;
            op.outHeight = height;
            if (null != embedPic && embedPic.length > 0) {
                bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return bitmap;
    }

    public static String getAudioName(String uri) {
        String name = "";
        ContentResolver resolver = MyApplication.getContext().getContentResolver();
        Cursor cursor = null;
        try {
            //查询数据库，参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
            cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (path.equals(uri)) {
                        name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return name;
    }

    public static String getAudioSinger(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    }

    public static LyricInfo getAudioLyric(String uri) {
        if (!TextUtils.isEmpty(uri)) {
            try {
                Mp3File mp3File = new Mp3File(uri);
                if (mp3File.hasId3v2Tag()) {
//                    String[] lrcs = mp3File.getId3v2Tag().getLyrics().split("\\n");
                    ID3v2 id3v2Tag = mp3File.getId3v2Tag();
                    String lyrics1 = id3v2Tag.getLyrics();  //可能报空指针异常
                    if (!TextUtils.isEmpty(lyrics1)) {
                        return LyricParser.parseFromStream(lyrics1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedTagException e) {
                e.printStackTrace();
            } catch (InvalidDataException e) {
                e.printStackTrace();
            }
            return parsingLrc(uri);
        }
        return null;
    }

    private static LyricInfo parsingLrc(String uri) {
        String lyricUri = uri.substring(0, uri.lastIndexOf(".")) + "." + "lrc";
        File file = new File(lyricUri);
        if (file.exists() && file.isFile() && file.length() > 0) {
            return LyricParser.parseFromFile(file);
        }
        return null;
    }

    /**
     *
     * @param uri 歌词地址
     * @param isOutLrc  是否是外置歌词
     * @return
     */
    public static LyricInfo getInOrOutLrc(String uri, boolean isOutLrc) {
        if (!TextUtils.isEmpty(uri)) {
            if (isOutLrc) {
                return parsingLrc(uri);
            } else {
                try {
                    Mp3File mp3File = new Mp3File(uri);
                    if (mp3File.hasId3v2Tag()) {
//                    String[] lrcs = mp3File.getId3v2Tag().getLyrics().split("\\n");
                        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
                        String lyrics1 = id3v2Tag.getLyrics();  //可能报空指针异常
                        if (!TextUtils.isEmpty(lyrics1)) {
                            return LyricParser.parseFromStream(lyrics1);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedTagException e) {
                    e.printStackTrace();
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

}
