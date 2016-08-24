package com.cantv.media.center.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cantv.media.center.data.Media;
import com.cantv.media.center.ui.MediaGridItemView;
import com.cantv.media.center.ui.MediaItemView;
import com.cantv.media.center.ui.MediaListItemView;
import com.cantv.media.center.ui.MediaOrientation;
import com.cantv.media.center.utils.MediaUtils;

import java.util.ArrayList;
import java.util.List;

public class MediaListAdapter extends BaseAdapter {
    private List<Media> mMediaList = new ArrayList<Media>();
    private MediaOrientation mStyle = MediaOrientation.THUMBNAIL;
    private List<String> mUsbRootPaths;

    public MediaListAdapter(Context context, List<Media> medialist) {
        bindData(medialist);
    }

    public void bindData(List<Media> medialist) {
        mMediaList = medialist;
        mUsbRootPaths = MediaUtils.getCurrPathList();
        notifyDataSetChanged();
    }

    public void bindStyle(MediaOrientation style) {
        this.mStyle = style;
    }

    private MediaItemView getItemType(ViewGroup parent) {
        MediaItemView mediaItemView;
        switch (mStyle) {
            case LIST:
                MediaListItemView mediaListItemView = new MediaListItemView(parent.getContext());
                mediaListItemView.setUsbPaths(mUsbRootPaths);
                mediaItemView = mediaListItemView;
                break;
            case THUMBNAIL:
                mediaItemView = new MediaGridItemView(parent.getContext());
                break;
            default:
                return null;
        }
        return mediaItemView;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
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
        MediaItemView mediaItemView = getItemType(parent);

//        if (mediaItemView instanceof MediaListItemView){
//
//            List<String> usbPaths = ((MediaListItemView) ((MediaListItemView) mediaItemView)).getUsbPaths();
//            if (null==usbPaths){
//                ((MediaListItemView) mediaItemView).setUsbPaths(MediaUtils.getCurrPathList());
//            }
//        }
        mediaItemView.setMediaItem(getItem(position));

        return mediaItemView;
    }
}