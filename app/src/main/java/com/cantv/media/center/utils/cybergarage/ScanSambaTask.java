package com.cantv.media.center.utils.cybergarage;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Locale;

import android.os.AsyncTask;
import android.util.Log;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class ScanSambaTask extends AsyncTask<String, Void, ArrayList<SmbFile>> {

	private boolean scanFiles;
	private Throwable exp;
	private int errCode;
	
	public interface IScanFileListener {
		void onSuccess(ArrayList<SmbFile> list);

		void onLoginFailed();

		void onException(Throwable ta);
	}

	private IScanFileListener mScanFileListener;

	public ScanSambaTask(boolean scanFiles, IScanFileListener iScanFileListener) {
		super();
		this.scanFiles = scanFiles;
		mScanFileListener = iScanFileListener;
	}

	@Override
	protected ArrayList<SmbFile> doInBackground(String... params) {
		Log.i("", "scanSamba path : " + params[0]);
		ArrayList<SmbFile> dirList = new ArrayList<SmbFile>();
		SmbFile smbFile = null;
		try {
			smbFile = new SmbFile(params[0]);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			SmbFile[] smbFileList = smbFile.listFiles();
			if(scanFiles){
				ArrayList<SmbFile> fileList = new ArrayList<SmbFile>();
				for (SmbFile f : smbFileList) {
					if (f.isDirectory()) {
						dirList.add(f);
					} else {
						fileList.add(f);
					}
				}
				dirList.addAll(fileList);
			}
		} catch (SmbException e) {
			exp = e;
			errCode = e.getNtStatus();
			Log.w("", "SmbException : 0x" + Integer.toHexString(errCode).toUpperCase(Locale.getDefault()), e);
			return null;
		}
		return dirList;
	}

	@Override
	protected void onPostExecute(ArrayList<SmbFile> result) {
		if(result != null){
			mScanFileListener.onSuccess(result);
		}else{
			if(errCode == SmbException.NT_STATUS_LOGON_FAILURE){
				mScanFileListener.onLoginFailed();
			}else{
				mScanFileListener.onException(exp);
			}
		}
	}

}