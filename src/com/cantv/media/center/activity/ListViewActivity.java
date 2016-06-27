package com.cantv.media.center.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.cantv.media.R;
import com.cantv.media.center.adapter.MediaListAdapter;
import com.cantv.media.center.data.Media;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListViewActivity extends Activity {
	private static String TAG = "ListViewActivity";
	private TextView mTitleTV;
	private ListView mContentLV;
	private List<Media> mMedia;
	private MediaListAdapter mListAdapter;
	
	private Stack<Integer> mPosStack = new Stack<Integer>();
	private Stack<List<Media>> mMediaStack = new Stack<List<Media>>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        
        mTitleTV = (TextView)findViewById(R.id.title_textview);
        mContentLV = (ListView)findViewById(R.id.listview);
        mListAdapter = new MediaListAdapter(this, new ArrayList<Media>());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mMedia = (ArrayList<Media>)extras.getSerializable("data");
            String type = extras.getString("type");
            if ("video".equalsIgnoreCase(type)) {
            	mTitleTV.setText(R.string.str_movie);
            } else if ("image".equalsIgnoreCase(type)) {
            	mTitleTV.setText(R.string.str_photo);
            } else if ("audio".equalsIgnoreCase(type)) {
            	mTitleTV.setText(R.string.str_music);
            }
            if (mMedia != null) {
            	mListAdapter.bindData(mMedia);
            	mContentLV.setAdapter(mListAdapter);
            }
        }
        mContentLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Media item = (Media) mListAdapter.getItem(position);
				if (item.isCollection()) {
					mPosStack.push(position);
					mMediaStack.push(mListAdapter.getData());
					mListAdapter.bindData(item.getSubMedias());
					mContentLV.setSelection(0);
				} else {
					//DevicesType devicesType = MediaUtils.getDevicesType(item.getUri());
					//mMediaUtils.showMediaDetail(getContext(), mListAdapter.getData(), position);
				}
			}
		});
    }

    @Override
	public void onBackPressed() {
    	if (!mPosStack.isEmpty() && !mMediaStack.isEmpty()) {
			mListAdapter.bindData(mMediaStack.pop());
			mContentLV.setSelection(mPosStack.pop());
			return;
		} 
		finish();
		super.onBackPressed();
	}

    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			default:
				break;
			}

		}
	};
}