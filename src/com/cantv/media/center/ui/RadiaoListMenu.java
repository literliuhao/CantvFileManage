package com.cantv.media.center.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class RadiaoListMenu extends CustomListMenu {

	public RadiaoListMenu(Context context) {
		super(context);
	}
	
	@Override
	protected View getMenuItemView(String itemText, View convertView,
			ViewGroup parent) {
		MediaMenuListRadiaoItem mediaItemView = new MediaMenuListRadiaoItem(
				parent.getContext());
		if (convertView == null) {
			mediaItemView = new MediaMenuListRadiaoItem(parent.getContext());
		} else {
			mediaItemView = (MediaMenuListRadiaoItem) convertView;
		}
		mediaItemView.setText(itemText);
		return mediaItemView;
	}

}
