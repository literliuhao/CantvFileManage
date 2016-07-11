package com.cantv.media.center.greendao;

import java.util.List;

import com.cantv.media.center.greendao.DaoMaster.DevOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DaoOpenHelper extends DevOpenHelper {
	/**
	 * @author long 数据库操作页 DaoMaster类中添加数据库Version
	 */
	public static final String TAG = "GreenDaoDBHelper";
	private static final String DB_NAME = "mediacenter.sqlite";
	private static DaoOpenHelper instance = null;
	private DaoMaster mDaoMaster;
	private DaoSession mDaoSession;
	private VideoPlayerDao mVideoPlayerDao;

	private DaoOpenHelper(Context ctx) {
		super(ctx, DB_NAME, null);
		mDaoMaster = getDaoMaster();
		mDaoSession = getDaoSession();
		mVideoPlayerDao = mDaoSession.getVideoPlayerDao();
	}

	public static DaoOpenHelper getInstance(Context ctx) {
		if (ctx == null) {
			return null;
		}

		if (instance == null) {
			synchronized (DaoOpenHelper.class) {
				if (instance == null) {
					instance = new DaoOpenHelper(ctx.getApplicationContext());
				}
			}

		}
		return instance;
	}

	private DaoMaster getDaoMaster() {
		if (mDaoMaster == null) {
			mDaoMaster = new DaoMaster(getWritableDatabase());
		}
		return mDaoMaster;
	}

	private DaoSession getDaoSession() {
		if (mDaoSession == null) {
			mDaoSession = getDaoMaster().newSession();
		}
		return mDaoSession;
	}

	public VideoPlayerDao getUserInfoDao() {
		return mVideoPlayerDao;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onUpgrade(db, oldVersion, newVersion);
	}

	/**
	 * 执行新增
	 **/
	public long execInsert(VideoPlayer info) {
		return mVideoPlayerDao.insert(info);
	}

	/**
	 * 执行更新(单条)
	 **/
	public void update(VideoPlayer info) {
		mVideoPlayerDao.update(info);
	}

	/**
	 * 查询用户
	 */
	public List<VideoPlayer> queryInfo(String name) {
		return mVideoPlayerDao.queryBuilder().where(VideoPlayerDao.Properties.Name.eq(name)).list();
		
	}
	
	/**
	 * 执行删除(单条)
	 **/
	public void deleteInfo(VideoPlayer info) {
		mVideoPlayerDao.delete(info);
	}

}
