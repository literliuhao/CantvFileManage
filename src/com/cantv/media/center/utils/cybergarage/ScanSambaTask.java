package com.cantv.media.center.utils.cybergarage;

import java.net.MalformedURLException;
import java.util.ArrayList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.os.AsyncTask;

public class ScanSambaTask extends AsyncTask<String, Void, ArrayList<SmbFile>> {
	
	public interface IScanFileListener {
		void callBack(ArrayList<SmbFile> list);
	}

	private IScanFileListener mScanFileListener;

	public ScanSambaTask(IScanFileListener iScanFileListener) {
		super();
		mScanFileListener = iScanFileListener;
	}

	@Override
	protected ArrayList<SmbFile> doInBackground(String... params) {

		SmbFile smbFile;
		SmbFile[] smbFileList;
		ArrayList<SmbFile> dirList = new ArrayList<SmbFile>();
		ArrayList<SmbFile> fileList = new ArrayList<SmbFile>();
		try {
			smbFile = new SmbFile(params[0]);
			smbFileList = smbFile.listFiles();
			for (SmbFile f : smbFileList) {
				if (f.isDirectory()) {
					dirList.add(f);
				} else {
					fileList.add(f);
				}
			}
			dirList.addAll(fileList);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SmbException e) {
			e.printStackTrace();
		}

		return dirList;
	}

	@Override
	protected void onPostExecute(ArrayList<SmbFile> result) {
		super.onPostExecute(result);
		mScanFileListener.callBack(result);
	}

}
