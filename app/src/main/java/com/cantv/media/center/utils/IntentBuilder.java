package com.cantv.media.center.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.cantv.media.center.data.Media;

import java.io.File;
import java.util.ArrayList;

public class IntentBuilder {

    public static void viewFile(final Context context, final String filePath) {
        String type = getMimeType(filePath);
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            try {
                /**设置intent的file与MimeType */
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "系统不支持该格式文件", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "系统不支持该格式文件", Toast.LENGTH_SHORT).show();
        }

    }

    public static Intent buildSendFile(ArrayList<Media> files) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        String mimeType = "*/*";
        for (Media file : files) {
            if (file.isDir) continue;

            File fileIn = new File(file.mUri);
            mimeType = getMimeType(file.mName);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        if (uris.size() == 0) return null;

        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE : android.content.Intent.ACTION_SEND);

        if (multiple) {
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }

        return intent;
    }

    private static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1) return "*/*";

        String ext = filePath.substring(dotPosition + 1, filePath.length()).toLowerCase();
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }

        return mimeType != null ? mimeType : "*/*";
    }
}
