package com.cantv.media.center.ui.player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cantv.media.R;
import com.cantv.media.center.activity.VideoPlayActicity;

public class PlayerController extends RelativeLayout {

	private static final int CHANG_PROGRESS = 0;
	private static final int CHANG_PLAYIMAGE = 1;
	private static final int CHANG_VISIBLE = 2;
	private static final int STORE_DURATION = 3;
	private static final int CONTINUE_PLAY = 4;
	private static final int CHANG_SRT = 5;
	private static final int SEEK_DURATION = 6;
	private long mDuration;
	private boolean isHasDefinition;
	private boolean isFirstEnter = true;
	private boolean isShowTip = false;
	private boolean isSrtExist;

	private Context mContext;
	private PlayerProgresBar mProgressBar;
	private ImageView mPlayImage;
	private TextView mTitle;
	private TextView mTime, mDefinitionTv;
	private SimpleDateFormat format;
	private PlayerCtrlBarContext mCtrlBarContext;
	private PlayerCtrlBarListener mCtrlBarListener;
	private CoverFlowViewListener mCoverFlowViewListener;
	private TextView mTip, mContinueText;
	private ImageView mTipImage;
	private LinearLayout mContinuePlay;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case CHANG_PROGRESS:
				mProgressBar.setProgress(mCtrlBarContext.getPlayerCurPosition());
				handler.sendEmptyMessageDelayed(PlayerController.CHANG_PROGRESS, 1000);
				break;

			case CHANG_PLAYIMAGE:
				if (mCtrlBarContext.isPlayerPaused()) {
					mPlayImage.setBackgroundResource(R.drawable.play_play);
				} else {
					mPlayImage.setBackgroundResource(R.drawable.play_stop);
				}
				break;

			case CHANG_VISIBLE:

				if (mCtrlBarContext.isPlayerPaused()) {
					showPause(false);
				} else {
					setVisibility(INVISIBLE);
				}
				break;

			case STORE_DURATION:

				((VideoPlayActicity) mContext).storeDuration();
				handler.sendEmptyMessageDelayed(STORE_DURATION, 60 * 1000);

				break;

			case CONTINUE_PLAY:

				isShowTip = false;
				mContinuePlay.setVisibility(INVISIBLE);

				break;

			case CHANG_SRT:
				((VideoPlayActicity) mContext).setSrts(mCtrlBarContext.getPlayerCurPosition());
				handler.sendEmptyMessageDelayed(PlayerController.CHANG_SRT, 1000);
				break;
				
			case SEEK_DURATION:
				
				seekToDuration((int) mProgressBar.getCurrProgress()+1000);
				
				break;

			default:
				break;
			}

		}

	};

	public interface PlayerCtrlBarContext {
		String getPlayerTitle();

		int getPlayerDuration();

		int getPlayerCurPosition();

		boolean isPlayerPaused();

		String getDefinition();
	}

	public interface PlayerCtrlBarListener {

		void onPlayerPlayOrPause();

		void onPlaySeekTo(int duration, OnSeekCompleteListener listener);

	}

	public interface CoverFlowViewListener {
		boolean scrollToNext(OnCompletionListener listener);

		boolean scrollPre(OnCompletionListener listener);
	}

	public PlayerController(Context context) {
		super(context);
		initView(context);
	}

	public PlayerController(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PlayerController(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	private void initView(Context context) {
		this.mContext = context;
		inflate(context, R.layout.player_controller, this);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setWillNotDraw(false);
		setDrawingCacheEnabled(false);

		mProgressBar = (PlayerProgresBar) findViewById(R.id.pb_progress);
		mTime = (TextView) findViewById(R.id.tv_time);
		mPlayImage = (ImageView) findViewById(R.id.iv_play);
		mTitle = (TextView) findViewById(R.id.tv_name);
		mPlayImage.setFocusable(false);
		mDefinitionTv = (TextView) findViewById(R.id.tv_definiton);
		mTip = (TextView) findViewById(R.id.tv_menu);
		mTipImage = (ImageView) findViewById(R.id.iv_menu);
		mContinueText = (TextView) findViewById(R.id.tv_continue_play);
		mContinuePlay = (LinearLayout) findViewById(R.id.rl_continue);

		TextPaint tp = mContinueText.getPaint();
		tp.setFakeBoldText(true);
	}

	public void setPlayerCtrlBarListener(PlayerCtrlBarListener listener) {
		mCtrlBarListener = listener;
	}

	public void setPlayerControllerBarContext(PlayerCtrlBarContext context) {
		mCtrlBarContext = context;
	}

	public void setPlayerCoverFlowViewListener(CoverFlowViewListener listener) {
		mCoverFlowViewListener = listener;
	}

	public void setPlayDuration() {

		isHasDefinition = TextUtils.isEmpty(mCtrlBarContext.getDefinition());
		
		if (!isHasDefinition) {
			mDefinitionTv.setText(mCtrlBarContext.getDefinition());
			mDefinitionTv.setVisibility(View.VISIBLE);
		} else {
			mDefinitionTv.setVisibility(View.INVISIBLE);
		}

		if (isFirstEnter) {
			isFirstEnter = false;
		} else {
			mProgressBar.initProgress();
		}
		mDuration = mCtrlBarContext.getPlayerDuration();
		mProgressBar.setDuration(mCtrlBarContext.getPlayerDuration());
		handler.removeMessages(PlayerController.CHANG_PROGRESS);
		handler.sendEmptyMessage(PlayerController.CHANG_PROGRESS);
		handler.sendEmptyMessage(PlayerController.CHANG_PLAYIMAGE);
		handler.sendEmptyMessageDelayed(PlayerController.CHANG_VISIBLE, 5000);
		mTitle.setText(mCtrlBarContext.getPlayerTitle());
		setVisibility(VISIBLE);
		mTitle.bringToFront();
		mTime.bringToFront();
		// 设置当前时间
		refreshTime();
		//如果有字幕，开始获取字幕
		isSrtExist = ((VideoPlayActicity) mContext).isSrtExist();
		if(isSrtExist){
			handler.sendEmptyMessageDelayed(PlayerController.CHANG_SRT, 2000);
		}
		handler.sendEmptyMessage(STORE_DURATION);
	}

	@SuppressLint("SimpleDateFormat")
	public void showContinuePaly(int position) {
		seekToDuration(position);

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		String continueTime = formatter.format(position);

		mContinuePlay.setVisibility(VISIBLE);
		mContinueText.setText("从" + continueTime + "开始，继续为您播放");
		isShowTip = true;
		handler.sendEmptyMessageDelayed(CONTINUE_PLAY, 2000);
	}

	@SuppressLint("SimpleDateFormat")
	public void refreshTime() {
		if (format == null) {
			format = new SimpleDateFormat("HH:mm");
		}
		String time = format.format(new Date(System.currentTimeMillis()));
		mTime.setText(time);
	}

	public void onKeyUpEvent(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_LEFT:
			handler.sendEmptyMessageDelayed(SEEK_DURATION, 500);
			break;
		default:
			break;
		}
	}

	public void onKeyDownEvent(int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_ENTER:
			if (mCtrlBarContext.isPlayerPaused()) {
				mPlayImage.setBackgroundResource(R.drawable.play_stop);
			} else {
				mPlayImage.setBackgroundResource(R.drawable.play_play);
			}
			mCtrlBarListener.onPlayerPlayOrPause();
			showController();
			break;

		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			handler.removeMessages(CHANG_PROGRESS);
			handler.removeMessages(CHANG_PLAYIMAGE);
			handler.removeMessages(SEEK_DURATION);
			mProgressBar.onKeyDown(keyCode, event);
			toggleSeekImgvi(keyCode);
			showController();
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			showController();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			showController();
			if (isShowTip) {
				seekToDuration(0);
				((VideoPlayActicity) mContext).initSrts();
				isShowTip = false;
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			boolean isPre = mCoverFlowViewListener.scrollPre(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
				}
			});

			if (isPre) {
				handler.removeMessages(PlayerController.CHANG_PROGRESS);
				showController();
			}

			break;

		case KeyEvent.KEYCODE_MEDIA_NEXT:

			boolean isNext = mCoverFlowViewListener.scrollToNext(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
				}
			});

			if (isNext) {
				handler.removeMessages(PlayerController.CHANG_PROGRESS);
				showController();
			}

			break;
		case KeyEvent.KEYCODE_MENU:
			setVisibility(INVISIBLE);
		default:
			break;
		}

	}

	public void seekToDuration(int duration) {
		handler.removeMessages(PlayerController.CHANG_PROGRESS, null);
		handler.removeMessages(PlayerController.CHANG_SRT, null);
		mCtrlBarListener.onPlaySeekTo(duration, new OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer arg0) {
				handler.sendEmptyMessageDelayed(PlayerController.CHANG_PLAYIMAGE, 100);
				handler.sendEmptyMessage(PlayerController.CHANG_PROGRESS);
				if(isSrtExist){
					handler.sendEmptyMessage(PlayerController.CHANG_SRT);
				}
			}
		});
	}

	public void removeAllMessage() {
		handler.removeCallbacksAndMessages(null);
	}

	private void showPause(boolean isVisible) {
		if (isVisible) {
			mTitle.setVisibility(VISIBLE);
			mTime.setVisibility(VISIBLE);
			mTitle.bringToFront();
			mTime.bringToFront();
			mProgressBar.setVisibility(VISIBLE);
			mTip.setVisibility(VISIBLE);
			mTipImage.setVisibility(VISIBLE);
			if (!isHasDefinition) {
				mDefinitionTv.setVisibility(VISIBLE);
			}

		} else {
			mTitle.setVisibility(INVISIBLE);
			mDefinitionTv.setVisibility(INVISIBLE);
			mTime.setVisibility(INVISIBLE);
			mProgressBar.setVisibility(INVISIBLE);
			mTip.setVisibility(INVISIBLE);
			mTipImage.setVisibility(INVISIBLE);
		}

	};

	private void toggleSeekImgvi(int keyCode) {
		if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode) {
			mPlayImage.setBackgroundResource(R.drawable.play_kt);
		} else if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
			mPlayImage.setBackgroundResource(R.drawable.play_kj);
		}
	}

	public void showController() {
		handler.removeMessages(PlayerController.CHANG_VISIBLE);
		handler.sendEmptyMessageDelayed(PlayerController.CHANG_VISIBLE, 5000);
		setVisibility(VISIBLE);
		showPause(true);
	};

	public void setFullProgress() {
		mProgressBar.setProgress(mDuration);
	}

	public void onBackPressed(VideoPlayActicity context) {
		if (VISIBLE == getVisibility()) {
			if (INVISIBLE == mProgressBar.getVisibility() && mCtrlBarContext.isPlayerPaused()) {
				mPlayImage.setVisibility(INVISIBLE);
				context.finish();
			} else {
				setVisibility(INVISIBLE);
			}
		} else {
			context.finish();
		}
	}

}
