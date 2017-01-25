package com.cantv.liteplayer.core.subtitle;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class SubDecoder extends StDecoder {

    class VobSubHead {
        boolean bFirstPacket;
        int id;
        int iLang;
        int headLen;
        int packetLen1;
        int packetLen2;
        int dataLen;
    }

    class VobSub_Data_S {
        int frame_width;
        int frame_height;
        int sub_xsize;
        int sub_ysize;
        int[] f_offset = new int[2];
        int fAligned;
        boolean sub_disp;
        int[] sub_color_map = new int[4];
        int[] sub_alpha_bit = new int[4];
        int sub_start_time;
        int sub_end_time;
        int control_start;
        int[] sub_palette = new int[16];
    }

    private static int SUB_NK = 10 * 1024;
    private VobSub_Data_S vobsub_ds = new VobSub_Data_S();
    private Bitmap mVobBitmap = null;

    private int VobSubIndex = 0;
    private int VobSubStartTime = 0;
    private int VobSubEndTime = 0;
    private int VobSubFilePos = 0;

    private int langidx = 0;
    private int[] langLine = new int[21];
    private int langCount = 0;
    private int langLineCount = 0;

    public void SubLoadMutiLine_VOBSUB(long vobSubTitlePos, String subFilePath) {
        int off = 0, i = 0;
        RandomAccessFile randomFile = null;
        try {
            randomFile = new RandomAccessFile(subFilePath, "r");

            randomFile.seek(vobSubTitlePos);

            byte[] bytesReadAhead = new byte[SUB_NK];

            if (randomFile.read(bytesReadAhead) != -1) {
                off = AssemblePacket(bytesReadAhead);
                vobsub_getline(bytesReadAhead, off);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    private int AssemblePacket(byte[] bytesReadAhead) {
        int code, flag;
        int offset = 0;
        int sumLen = 0;

        int streamId = 0;
        int i, j, k;
        int packets = 1, iPackets = 0;
        int[] nextPackPos = new int[5];

        VobSubHead subhead;

        code = (bytesReadAhead[0x0e]) | (bytesReadAhead[0x0f] << 8) | (bytesReadAhead[0x10] << 16) | (bytesReadAhead[0x11] << 24);
        flag = (bytesReadAhead[0x00]) | (bytesReadAhead[0x01] << 8) | (bytesReadAhead[0x02] << 16) | (bytesReadAhead[0x03] << 24);

        if (flag != 0xba010000 || code != 0xbd010000 || ((bytesReadAhead[0x15] & 0x80) == 0) || (bytesReadAhead[0x17] & 0xf0) != 0x20
                || (bytesReadAhead[bytesReadAhead[0x16] + 0x17] & 0xe0) != 0x20
        /* || (buff[buff[0x16] + 0x17] & 0x1f) == iLang */) {
            return -1;
        }

        subhead = vobsub_GetHead(bytesReadAhead, 0);
        offset = subhead.headLen;

        if (subhead.packetLen1 < 0x7ec) {
            ;
        } else {
            streamId = subhead.id;
            i = 1;
            j = SUB_NK >> 11;
            while (i < j) {
                subhead = vobsub_GetHead(bytesReadAhead, i << 11);

                if (subhead.id != streamId) {
                    i++;
                    continue;
                }
                if (subhead.bFirstPacket == false) {
                    nextPackPos[packets] = i;
                    packets++;
                    if (subhead.packetLen1 < 0x7ec) {
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
        }

        for (iPackets = 1; iPackets < packets; iPackets++) {
            subhead = vobsub_GetHead(bytesReadAhead, (nextPackPos[iPackets]) << 11);

            for (k = 0; k < 2048 - subhead.headLen; k++) {
                bytesReadAhead[(iPackets << 11) - sumLen + k] = bytesReadAhead[(nextPackPos[iPackets] << 11) + subhead.headLen + k];
            }

            sumLen += subhead.headLen;
        }

        return offset;
    }

    private VobSubHead vobsub_GetHead(byte[] bytesReadAhead, int filepos) {
        int flag;
        int code;
        VobSubHead head = new VobSubHead();
        head.bFirstPacket = false;
        head.id = 0;
        head.iLang = 0;
        head.headLen = 0;
        head.packetLen1 = 0;
        head.packetLen2 = 0;
        head.dataLen = 0;

        head.headLen = bytesReadAhead[filepos + 0x16] + 0x18; // packet[0x16]:data
        // packet head

        head.packetLen1 = ((bytesReadAhead[filepos + 0x12] & 0xff) << 8) | (bytesReadAhead[filepos + 0x12 + 1] & 0xff);

        head.packetLen2 = ((bytesReadAhead[filepos + head.headLen] & 0xff) << 8) | (bytesReadAhead[filepos + head.headLen + 1] & 0xff);
        head.dataLen = ((bytesReadAhead[filepos + head.headLen + 2] & 0xff) << 8) | (bytesReadAhead[filepos + head.headLen + 3] & 0xff);

        head.id = (bytesReadAhead[filepos + (bytesReadAhead[filepos + 0x16] & 0xff) + 0x17] & 0xff);

        head.iLang = (head.id & 0x1f);

        if (0x80 == ((bytesReadAhead[filepos + 0x15] & 0xff) & 0x80)) {
            head.bFirstPacket = true;
        } else {
            head.bFirstPacket = false;
        }
        return head;
    }

    private void vobsub_getline(byte[] bytesReadAhead, int fileoffset) {
        int x = 0;
        int y = 0;
        int i, k, m, n, t, q, p, w;
        byte blankLine = 0;
        int pixelCounbt = 0;

        boolean isFirstLine = false;
        int lastY = 0;

        //可能存在数组越界异常
        if (vobsub_getcom(bytesReadAhead, fileoffset) == -1) {
            ;
        }

        if (vobsub_ds.sub_ysize > 2) {
            vobsub_ds.sub_ysize -= 2;
        }

        i = vobsub_ds.f_offset[1];

        vobsub_ds.fAligned = 0;

        int colorstp[] = guess_palette();
        int colors[] = new int[vobsub_ds.sub_xsize * vobsub_ds.sub_ysize];
        // byte[][] writebuffer = new
        // byte[vobsub_ds.sub_ysize][vobsub_ds.sub_xsize]; // 200
        // lines
        // byte[] oneLineBuf = new byte[vobsub_ds.sub_xsize];

        while (vobsub_ds.f_offset[0] < i && vobsub_ds.f_offset[1] / 2 < vobsub_ds.control_start && y < vobsub_ds.sub_ysize) {
            int len, color;
            int rle = 0;

            rle = GetNibble(bytesReadAhead, fileoffset);
            if (rle < 0x04) {
                rle = (rle << 4) | GetNibble(bytesReadAhead, fileoffset);
                if (rle < 0x10) {
                    rle = (rle << 4) | GetNibble(bytesReadAhead, fileoffset);
                    if (rle < 0x040) {
                        rle = (rle << 4) | GetNibble(bytesReadAhead, fileoffset);
                        if (rle < 0x0004) {
                            rle |= ((vobsub_ds.sub_xsize - x) << 2);
                            // rle |= ((vobsub_ds.sub_xsize - x + 1) << 2);
                            // rle |= ((vobsub_ds.sub_xsize - x) << 2);
                        } else if (rle < 0x100 && rle > 0x0f) {
                            continue;
                        }
                    }
                }
            }

            // color = 3 - (rle & 0x3);
            color = (rle & 0x3);
            len = rle >> 2;

            // jump the first line
            if ((len != vobsub_ds.sub_xsize) && (len != 0) && (!isFirstLine) == true) {
                isFirstLine = true;
                lastY = y;
            }

            if (x == 0 && len >= vobsub_ds.sub_xsize) {
                blankLine++;
                if (blankLine > 2) {
                    NextLine(); // jump the blank line
                    continue;
                }
            } else {
                blankLine = 0;
            }

            if (len > vobsub_ds.sub_xsize - x || len == 0)
                len = vobsub_ds.sub_xsize - x;// len = vobsub_ds.sub_xsize - x
            // +1;

            for (k = 0; k < len; k++) {
                if (k + x > (vobsub_ds.sub_xsize - 1)) {
                    break;
                }
                colors[vobsub_ds.sub_xsize * y + k + x] = colorstp[color];
                // oneLineBuf[k + x] = (byte) color;
                ++pixelCounbt;
            }

            x += len;
            if (x >= vobsub_ds.sub_xsize) {
                NextLine();
                x = 0;
                ++y;
            }

        }

        vobsub_ds.sub_ysize = y;

        if (vobsub_ds.sub_xsize <= 0) {
            return;
        }
        if (vobsub_ds.sub_ysize <= 0) {
            return;
        }

        mVobBitmap = Bitmap.createBitmap(colors, 0, vobsub_ds.sub_xsize, vobsub_ds.sub_xsize, vobsub_ds.sub_ysize, ARGB_8888);
    }

    private int[] guess_palette() {
        int subtitle_color = 0xffff00;
        int[] rgba_palette = new int[4];
        if (vobsub_ds.sub_palette != null && vobsub_ds.sub_palette.length > 0) {
            for (int i = 0; i < 4 && i < vobsub_ds.sub_palette.length; i++) {
                rgba_palette[i] = (vobsub_ds.sub_palette[vobsub_ds.sub_color_map[i]] & 0x00FFFFFF) | (vobsub_ds.sub_alpha_bit[i] * 17 << 24);
            }
            return rgba_palette;
        }
        int alpha[] = vobsub_ds.sub_alpha_bit;
        int colormap[] = vobsub_ds.sub_color_map;
        // this configuration (full range, lowest to highest) in tests
        // seemed most common, so assume this
        int level_map[][] = {{0xff}, {0x00, 0xff}, {0x00, 0x80, 0xff}, {0x00, 0x55, 0xaa, 0xff}};
        int color_used[] = new int[16];
        int nb_opaque_colors, i, level, j, r, g, b;
        nb_opaque_colors = 0;
        for (i = 0; i < 4; i++) {
            if (alpha[i] != 0 && color_used[colormap[i]] != 1) {
                color_used[colormap[i]] = 1;
                nb_opaque_colors++;
            }
        }
        if (nb_opaque_colors == 0)
            return rgba_palette;

        for (i = 0; i < color_used.length; i++) {
            color_used[i] = 0;
        }
        j = 0;
        for (i = 0; i < 4; i++) {
            if (alpha[i] != 0) {
                if (color_used[colormap[i]] != 1) {
                    level = level_map[nb_opaque_colors][j];
                    r = (((subtitle_color >> 16) & 0xff) * level) >> 8;
                    g = (((subtitle_color >> 8) & 0xff) * level) >> 8;
                    b = (((subtitle_color >> 0) & 0xff) * level) >> 8;
                    rgba_palette[i] = b | (g << 8) | (r << 16) | ((alpha[i] * 17) << 24);
                    color_used[colormap[i]] = (i + 1);
                    j++;
                } else {
                    rgba_palette[i] = (rgba_palette[color_used[colormap[i]] - 1] & 0x00ffffff) | ((alpha[i] * 17) << 24);
                }
            }
        }
        return rgba_palette;
    }

    private int vobsub_getcom(byte[] bytesReadAhead, int filepos) {
        int a, b;
        int date, type;
        int off;
        int start_off = 0;
        int next_off;
        int start_pts;
        int end_pts;
        int[] f_offset = new int[2];
        int control_start;
        int start_col = 0;
        int end_col = 0;
        int start_row = 0;
        int end_row = 0;
        int width = 0;
        int height = 0;

        control_start = ((bytesReadAhead[filepos + 2] & 0xff) << 8) | (bytesReadAhead[filepos + 2 + 1] & 0xff);
        next_off = control_start;
        vobsub_ds.control_start = control_start << 1;

        while (start_off != next_off) {
            start_off = next_off;
            if (filepos + start_off >= bytesReadAhead.length) {
                continue;
            }
            date = (((bytesReadAhead[filepos + start_off] & 0xff) << 8) | (bytesReadAhead[filepos + start_off + 1] & 0xff)); // *1024;
            next_off = ((bytesReadAhead[filepos + start_off + 2] & 0xff) << 8) | (bytesReadAhead[filepos + start_off + 3] & 0xff);
            off = start_off + 4;

            //这里这样写是有原因的
            int i = filepos + off++;
            int i1 = filepos + off++;
            if (i >= bytesReadAhead.length) {
                continue;
            }

            if (i1 >= bytesReadAhead.length) {
                continue;
            }

            for (type = bytesReadAhead[i] & 0xff; type != 0xff; type = bytesReadAhead[i1] & 0xff) {
                if (filepos + off >= bytesReadAhead.length) {
                    continue;
                }
                switch (type) {
                    case 0x0:
                        vobsub_ds.sub_disp = true;
                        break;
                    case 0x01:
                        vobsub_ds.sub_start_time = (date << 10) / 90;
                        vobsub_ds.sub_disp = true;
                        break;
                    case 0x02:
                        VobSubEndTime = (date << 10) / 90;// the time of end a
                        // subtitle
                        break;
                    case 0x03:
                        if (filepos + off + 1 >= bytesReadAhead.length) {
                            continue;
                        }
                        vobsub_ds.sub_color_map[3] = ((bytesReadAhead[filepos + off] & 0xff) >> 4) & 0x0f;
                        vobsub_ds.sub_color_map[2] = (bytesReadAhead[filepos + off] & 0xff) & 0x0f;
                        vobsub_ds.sub_color_map[1] = ((bytesReadAhead[filepos + off + 1] & 0xff) >> 4) & 0x0f;
                        vobsub_ds.sub_color_map[0] = (bytesReadAhead[filepos + off + 1] & 0xff) & 0x0f;
                        off += 0x02;
                        break;
                    case 0x04:
                        if (filepos + off + 1 >= bytesReadAhead.length) {
                            continue;
                        }
                        vobsub_ds.sub_alpha_bit[3] = ((bytesReadAhead[filepos + off] & 0xff) >> 4) & 0x0f;
                        vobsub_ds.sub_alpha_bit[2] = (bytesReadAhead[filepos + off] & 0xff) & 0x0f;
                        vobsub_ds.sub_alpha_bit[1] = ((bytesReadAhead[filepos + off + 1] & 0xff) >> 4) & 0x0f;
                        vobsub_ds.sub_alpha_bit[0] = (bytesReadAhead[filepos + off + 1] & 0xff) & 0x0f;
                        off += 0x02;
                        break;
                    case 0x05:
                    case 0x85:
                        if (filepos + off + 5 >= bytesReadAhead.length) {
                            continue;
                        }

                        a = ((bytesReadAhead[filepos + off] & 0xff) << 16) | ((bytesReadAhead[filepos + off + 1] & 0xff) << 8)
                                | ((bytesReadAhead[filepos + off + 2] & 0xff));
                        b = ((bytesReadAhead[filepos + off + 3] & 0xff) << 16) | ((bytesReadAhead[filepos + off + 4] & 0xff) << 8)
                                | ((bytesReadAhead[filepos + off + 5] & 0xff));
                        start_col = a >> 12;
                        end_col = a & 0xfff;
                        start_row = b >> 12;
                        end_row = b & 0xfff;

                        vobsub_ds.sub_xsize = (end_col < start_col) ? 0 : (end_col - start_col + 1);
                        vobsub_ds.sub_ysize = (end_row < start_row) ? 0 : (end_row - start_row);
                        off += 0x06;
                        break;
                    case 0x06:
                        if (filepos + off + 3 >= bytesReadAhead.length) {
                            continue;
                        }
                        vobsub_ds.f_offset[0] = ((bytesReadAhead[filepos + off] & 0xff) << 8) | (bytesReadAhead[filepos + off + 1]);
                        vobsub_ds.f_offset[1] = ((bytesReadAhead[filepos + off + 2] & 0xff) << 8) | ((bytesReadAhead[filepos + off + 3] & 0xff));
                        vobsub_ds.f_offset[0] = (vobsub_ds.f_offset[0]) << 1;
                        vobsub_ds.f_offset[1] = (vobsub_ds.f_offset[1]) << 1;
                        off += 0x04;
                        break;
                    case 0xff:
                        return 0;
                    default:
                        break;
                }
            }

        }
        return 0;
    }

    int GetNibble(byte[] bytesReadAhead, int filepos) {
        int nib;
        int nibblep = vobsub_ds.f_offset[vobsub_ds.fAligned];
        int pos = nibblep >> 1;

        if (pos >= vobsub_ds.control_start) {
            return 0;
        }

        if (filepos + pos >= bytesReadAhead.length) {
            return 0;
        }

        nib = bytesReadAhead[filepos + pos] & 0xff;

        if ((nibblep & 0x01) > 0) {
            nib &= 0xf; // low-order nibble
        } else {
            nib >>= 4; // high-order nibble
        }

        vobsub_ds.f_offset[vobsub_ds.fAligned]++;

        return nib;
    }

    private void NextLine() {
        if ((vobsub_ds.f_offset[vobsub_ds.fAligned] & 0x01) > 0) {
            vobsub_ds.f_offset[vobsub_ds.fAligned]++;
        }
        vobsub_ds.fAligned = (vobsub_ds.fAligned + 1) & 0x01;
    }

    class langStruct {
        int langIndex = 0;
        int langStartLine = 0;
        int langEndLine = 0;

        public langStruct(int langIndex, int langStartLine, int langEndLine) {
            super();
            this.langIndex = langIndex;
            this.langStartLine = langStartLine;
            this.langEndLine = langEndLine;
        }

        public int getLangIndex() {
            return langIndex;
        }

        public int getLangStartLine() {
            return langStartLine;
        }

        public int getLangEndLine() {
            return langEndLine;
        }

        public void setLangIndex(int langIndex) {
            this.langIndex = langIndex;
        }

        public void setLangStartLine(int langStartLine) {
            this.langStartLine = langStartLine;
        }

        public void setLangEndLine(int langEndLine) {
            this.langEndLine = langEndLine;
        }
    }

    // 00:00:28:728
    private int getSubTime(String str) {
        String[] timeArray = str.split(":");
        int hour = Integer.valueOf(timeArray[0]).intValue();
        int minute = Integer.valueOf(timeArray[1]).intValue();
        int second = Integer.valueOf(timeArray[2]).intValue();
        int millsecond = Integer.valueOf(timeArray[3]).intValue();

        return hour * 3600000 + minute * 60000 + second * 1000 + millsecond;
    }

    public void saveMyBitmap(Bitmap mBitmap, String filePath) throws IOException {
        File f = new File(filePath);
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean use_MicroDVD_SUB = false;

    @Override
    public Map<String, List<StContent>> decodeSubtitle(String subtitlePath, String encode) throws Exception {

        String subIdxPath = subtitlePath.substring(0, subtitlePath.lastIndexOf(".")) + ".idx";
        String encodeType = StUtil.getEncodeType(subIdxPath);
        BufferedReader bufferReaderSub = StUtil.getBufferReader(subIdxPath, encodeType);

        Map<String, List<StContent>> subTitleMap = new TreeMap<String, List<StContent>>();
        Map<String, langStruct> langMap = new TreeMap<String, langStruct>();
        StContent content = null;
        String subTitleLine = null;
        String langStr = null;
        String timeStr = null;
        int time = 0;
        int endTime = 0;
        String posStr = null;
        int pos = 0;
        int langIndexCount = 0;
        int langIndex = 0;
        int indexCount = 0;

        while ((subTitleLine = bufferReaderSub.readLine()) != null) {
            subTitleLine = subTitleLine.trim();

            if (use_MicroDVD_SUB) {
                String str[] = subTitleLine.split("\\}");
                content = new StContent();
                content.setSubtitleIndex(indexCount++);
                if (str.length == 3) {
                    String str0 = str[0].substring(str[0].indexOf("{") + 1);
                    String str1 = str[1].substring(str[1].indexOf("{") + 1);
                    String str2 = str[2];
                    langStr = "SUB" + indexCount;
                    subTitleMap.put(langStr, new ArrayList<StContent>());
                    time = (int) (Integer.valueOf(str0) * 1000 / 25);
                    endTime = (int) (Integer.valueOf(str1) * 1000 / 25);
                    content.setSubtitleStartTime(time);
                    content.setSubtitleEndTime(endTime);
                    content.setSubtitleLine(str2);
                    subTitleMap.get(langStr).add(content);
                }
            } else {
                if (subTitleLine.startsWith("id:")) {
                    langIndex = Integer.valueOf(subTitleLine.split(":")[subTitleLine.split(":").length - 1].trim());
                    if (langStr != null && langMap.get(langStr) != null) {
                        langMap.get(langStr).setLangEndLine(langIndex - 1);
                    }
                    langStr = subTitleLine.split(",")[0].split(":")[1].trim() + langIndex;
                    langMap.put(langStr, new langStruct(langIndexCount++, langIndex, Integer.MAX_VALUE));
                    subTitleMap.put(langStr, new ArrayList<StContent>());
                } else if (subTitleLine.startsWith("palette:")) {
                    subTitleLine = subTitleLine.substring("palette:".length());
                    String strPilt[] = subTitleLine.split(",");
                    for (int i = 0; i < 16 && i < strPilt.length; i++) {
                        vobsub_ds.sub_palette[i] = Integer.decode("0x00" + strPilt[i].trim());
                    }
                } else if (subTitleLine.startsWith("size:")) {
                    subTitleLine = subTitleLine.substring("size:".length());
                    String strPlit[] = subTitleLine.split("x");
                    if (strPlit != null && strPlit.length == 2) {
                        vobsub_ds.frame_width = Integer.parseInt(strPlit[0].trim());
                        vobsub_ds.frame_height = Integer.parseInt(strPlit[1].trim());
                    }

                } else if (subTitleLine.startsWith("timestamp:")) {
                    timeStr = subTitleLine.split(",")[0].trim().split(" ")[1].trim();
                    time = getSubTime(timeStr);
                    posStr = subTitleLine.split("filepos:")[1].trim();
                    pos = Integer.parseInt(posStr, 16);

                    content = new StContent();
                    content.setSubtitleIndex(indexCount++);
                    content.setmLanguageClass(langStr);
                    content.setmFilepos(pos);
                    content.setSubtitleStartTime(time);
                    content.setSubtitleEndTime(time + 5000/* VobSubEndTime */);// default
                    // delay
                    // 5000ms
                    content.setSubtitleLine(null);
                    List<StContent> contentList = subTitleMap.get(langStr);
                    if (contentList.size() > 0) {
                        StContent lastContent = contentList.get(contentList.size() - 1);
                        lastContent.setSubtitleEndTime(time - 1);
                    }
                    contentList.add(content);
                }
            }
        }
        super.closeBufferReader(bufferReaderSub);
        return subTitleMap;
    }

    public void decodePictureSubTitle(String subtitlePath, StContent subtitleContent, int screenWidth) {

        long time = System.currentTimeMillis();
        SubLoadMutiLine_VOBSUB(subtitleContent.getmFilepos(), subtitlePath);
        subtitleContent.setSubtitleEndTime(subtitleContent.getSubtitleStartTime() + VobSubEndTime);
        if (null == mVobBitmap) {
            return;
        }
        int frameWidth = vobsub_ds.frame_width;
        if (frameWidth > 0) {
            float scale = (screenWidth + frameWidth) * 1.5f / (2 * frameWidth);
            mVobBitmap = bitmapScale(mVobBitmap, scale);

        }
        Bitmap copyBt = mVobBitmap.copy(ARGB_8888, false);
        subtitleContent.setSubtitleBmp(copyBt);
        if (null != mVobBitmap) {
            mVobBitmap.recycle();
            mVobBitmap = null;
        }
    }

    private static Bitmap bitmapScale(Bitmap bitmap, float scale) {
        if (null == bitmap) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return resizeBmp;
    }
}
