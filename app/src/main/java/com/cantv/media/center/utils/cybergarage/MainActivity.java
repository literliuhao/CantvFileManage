package com.cantv.media.center.utils.cybergarage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.cantv.media.center.utils.cybergarage.ScanSambaTask.IScanFileListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnItemClickListener {
    private Button mAddBtn = null;
    private ListView mListview = null;
    private ArrayAdapter<FileItem> mAdapter = null;
    private ArrayList<FileItem> mData = new ArrayList<>();
    private ScanSambaTask mScanSambaTask = null;
    FileServer mFileServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);

        mFileServer = new FileServer();
        mFileServer.start();

        init();
    }

    private void init() {
        mAdapter = new ArrayAdapter<FileItem>(this, android.R.layout.simple_list_item_1, mData);
        mListview.setAdapter(mAdapter);
        mListview.setOnItemClickListener(this);

        mAddBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipValue;
                ipValue = "zhuhl:123456789@192.168.88.111";
                mAdapter.add(new FileItem(ipValue, "smb://" + ipValue + "/", false));
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem fileItem = mData.get(position);

        if (fileItem.isFile()) {
            String path = fileItem.getPath();
            String httpReq = "http://" + mFileServer.getBindIP() + ":" + mFileServer.getHTTPPort() + "/smb=";
            if (path.endsWith(".mp3")) {
                path = path.substring(6);
                try {
                    path = URLEncoder.encode(path, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String url = "http://" + httpReq + "/smb=" + path;
                Log.e("", "url: " + url);
                Intent it = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(url);
                it.setDataAndType(uri, "audio/*");
                startActivity(it);
            } else if (path.endsWith(".mp4")) {
                path = path.substring(6);
                try {
                    path = URLEncoder.encode(path, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String url = httpReq + path;
                Log.e("", "url: " + url);
                Intent it = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(url);
                it.setDataAndType(uri, "video/mp4");
                startActivity(it);
            }

        } else {
            searchFile(fileItem.getPath());
        }

    }

    private void searchFile(String path) {
        if (mScanSambaTask == null || mScanSambaTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
            mScanSambaTask = new ScanSambaTask(true, new IScanFileListener() {

                @Override
                public void onSuccess(ArrayList<SmbFile> list) {
                    if (list == null || list.isEmpty()) {
                        Toast.makeText(MainActivity.this, "����ʧ���ˣ������� ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mAdapter.clear();
                    for (SmbFile f : list) {
                        String filePath = f.getPath();
                        String fileName = f.getName();
                        try {
                            boolean isFile = f.isFile();
                            Log.d("", "fileName: " + fileName + " " + filePath + " isFile: " + isFile);
                            mAdapter.add(new FileItem(fileName, filePath, isFile));
                        } catch (SmbException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onLoginFailed() {

                }

                @Override
                public void onException(Throwable ta) {

                }

            });
            mScanSambaTask.execute(path);
        }
    }

    @Override
    protected void onDestroy() {
        mFileServer.release();
        super.onDestroy();
    }

}