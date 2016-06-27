package com.cantv.media.center.activity;

import com.cantv.media.R;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.ui.MediaGridView;
import com.cantv.media.center.utils.MediaUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

public class GridViewActivity extends Activity {
	private static String TAG = "GridViewActivity";
	private FrameLayout mContentView;
	private TextView mTitleTV;
	private MediaGridView mGridView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        mTitleTV = (TextView)findViewById(R.id.title_textview);
        mContentView = (FrameLayout) findViewById(R.id.content);
        
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String type = null;
        if (uri != null) {
        	type = uri.getHost();
        } else {
        	type = intent.getStringExtra("type");
        }
        if ("video".equalsIgnoreCase(type)) {
         	mTitleTV.setText(R.string.str_movie);
         	mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(), SourceType.MOIVE);
        } else if ("image".equalsIgnoreCase(type)) {
           	mTitleTV.setText(R.string.str_photo);
           	mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(), SourceType.PICTURE);
        } else if ("audio".equalsIgnoreCase(type)) {
           	mTitleTV.setText(R.string.str_music);
           	mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(), SourceType.MUSIC);
        } else {
        	mTitleTV.setText(R.string.str_file);
			mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(), SourceType.DEVICE);
		}
        mGridView.show();
        mGridView.setNumColumns(5);
        mContentView.addView(mGridView);
	}

	@Override
	public void onBackPressed() {
		MediaGridView childGridView = (MediaGridView)mContentView.getFocusedChild();
		if (childGridView.onBack() == false) {
			finish();
		} else {
			return;
		}		
	}
}