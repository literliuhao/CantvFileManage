package com.cantv.media.center.utils;

import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;

import java.util.Comparator;
import java.util.Locale;

/**
 * Created by yibh on 2016/7/6. 文件排序
 */

public class FileComparator implements Comparator<Media> {
	public static final int SORT_TYPE_DATE_DOWN = 0;
	public static final int SORT_TYPE_SIZE_DOWN = 1;
	public static final int SORT_TYPE_NAME_UP = 2;
	public static final int SORT_TYPE_DATE_UP = 3;
	public static final int SORT_TYPE_SIZE_UP = 4;
	public static final int SORT_TYPE_NAME_DOWN = 5;
	public static final int SORT_TYPE_DEFAULT = SORT_TYPE_NAME_UP;

	// 每个方式对应两种模式
	public static final int SORT_MODE_UP = 0;
	public static final int SORT_MODE_DOWN = 1;

	private final int mSoryType;

	public FileComparator() {
		mSoryType = SharedPreferenceUtil.getSortType();
	}

	@Override
	public int compare(Media media1, Media media2) {
		switch (mSoryType) {
		case SORT_TYPE_DATE_UP:
			int i = (media1.modifiedDate - media2.modifiedDate) > 0 ? 1
					: (media1.modifiedDate - media2.modifiedDate) == 0 ? 0 : -1;
			return sort(media1, media2, i, SORT_MODE_UP);
		case SORT_TYPE_DATE_DOWN:
			int i1 = (media1.modifiedDate - media2.modifiedDate) > 0 ? 1
					: (media1.modifiedDate - media2.modifiedDate) == 0 ? 0 : -1;
			return sort(media1, media2, i1, SORT_MODE_DOWN);
		case SORT_TYPE_SIZE_UP:
			return sort(media1, media2,
					(int) (media1.fileSize - media2.fileSize), SORT_MODE_UP);
		case SORT_TYPE_SIZE_DOWN:
			return sort(media1, media2,
					(int) (media1.fileSize - media2.fileSize), SORT_MODE_DOWN);
		case SORT_TYPE_NAME_UP:
			return sort(media1, media2, media1.mName.toLowerCase(Locale.CHINA)
					.compareTo(media2.mName.toLowerCase(Locale.CHINA)),
					SORT_MODE_UP);
		case SORT_TYPE_NAME_DOWN:
			return sort(media1, media2, media1.mName.toLowerCase(Locale.CHINA)
					.compareTo(media2.mName.toLowerCase(Locale.CHINA)),
					SORT_MODE_DOWN);
		default:
			break;
		}
		return 0;
	}

	/**
	 * @param media1
	 * @param media2
	 * @param diffValue
	 *            这个值应该是Lv-Rv ,顺序不能乱
	 * @param sortMode
	 * @return
	 */
	private int sort(Media media1, Media media2, int diffValue, int sortMode) {
		if (media1.mType == SourceType.FOLDER) {
			if (media2.mType == SourceType.FOLDER) {
				return sortByMode(diffValue, sortMode);
			} else {
				return -1;
			}
		} else {
			if (media2.mType == SourceType.FOLDER) {
				return 1;
			} else {
				return sortByMode(diffValue, sortMode);
			}
		}
	}

	private int sortByMode(int diffValue, int sortMode) {
		return diffValue == 0 ? 0
				: sortMode == SORT_MODE_DOWN ? diffValue > 0 ? -1 : 1
						: diffValue > 0 ? 1 : -1;
	}

}
