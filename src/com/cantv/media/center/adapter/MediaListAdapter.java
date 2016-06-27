package com.cantv.media.center.adapter;

import java.util.ArrayList;
import java.util.List;

import com.cantv.media.center.data.Media;
import com.cantv.media.center.ui.MediaItemView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MediaListAdapter extends BaseAdapter {
	private List<Media> mMediaList = new ArrayList<Media>();

	public MediaListAdapter(Context context, List<Media> medialist) {
		bindData(medialist);
	}

	public void bindData(List<Media> medialist) {
		mMediaList = medialist;
		notifyDataSetChanged();
	}

	public List<Media> getData() {
		return mMediaList;
	}

	@Override
	public int getCount() {
		return mMediaList == null ? 0 : mMediaList.size();
	}

	@Override
	public Media getItem(int position) {
		return mMediaList == null ? null : mMediaList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MediaItemView mediaItemView;
		mediaItemView = new MediaItemView(parent.getContext());
		mediaItemView.setMediaItem(getItem(position));
		return mediaItemView;
	}

}