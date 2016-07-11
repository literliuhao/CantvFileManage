package com.cantv.media.center.ui.player;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cantv.media.R;
import com.cantv.media.center.activity.VideoPlayActicity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlayerController extends RelativeLayout {
	
	public static final int FULLPROGRESS = -2;
	private static final int CHANG_PROGRESS = 0;
	private static final int CHANG_PLAYIMAGE = 1;
	private static final int CHANG_VISIBLE = 2;
	private static final int STORE_DURATION = 3;

	private long mDuration;
	private boolean isHasDefinition;
	private boolean isFirstEnter = true;

	private Context mContext;
	private PlayerProgresBar mProgressBar;
	private ImageView mPlayImage;
	private TextView mTitle;
	private TextView mTime, mDefinitionTv;
	private SimpleDateFormat format;
	private PlayerCtrlBarContext mCtrlBarContext;
	private PlayerCtrlBarListener mCtrlBarListener;
	private CoverFlowViewListener mCoverFlowViewListener;
	private TextView mTip;
	private ImageView mTipImage;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case CHANG_PROGRESS:
				Log.e("sunyanlong","CHANG_PROGRESS:"+mCtrlBarContext.getPlayerCurPosition());
				mProgressBar.setProgress(mCtrlBarContext.getPlayerCurPosition());
				setCurrentTime();
				((VideoPlayActicity) mContext).setSrt(mCtrlBarContext.getPlayerCurPosition());
				handler.sendEmptyMessageDelayed(PlayerController.CHANG_PROGRESS, 500);
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
				
				((VideoPlayActicity)mContext).storeDuration();
			
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
		void onPlayNext();

		void onPlayPrev();

		void onPlayRewind();

		void onPlayForwad();

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
		}
		
		
		if(isFirstEnter){
			isFirstEnter = false;
		}else{
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
		// 设置当前时间
		setCurrentTime();

		handler.sendEmptyMessageDelayed(STORE_DURATION, 60*1000);

		
		initWardData();

	}

	@SuppressLint("SimpleDateFormat")
	private void setCurrentTime() {
		if (format == null) {
			format = new SimpleDateFormat("HH:mm");
		}
		String time = format.format(new Date(System.currentTimeMillis()));
		mTime.setText(time);
	}

	public void onKeyUp(int keyCode){
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (isLongClick()) {
				mProgressBar.cancelAnim();
				seekToDuration((int)mProgressBar.getCurrProgress());
			}
			mClickCount=0;
			mStepSize=DEFAULT_STEP_SIZE;	
			break;

		default:
			break;
		}
	}
	public void onKeyDown(int  keyCode) {
		Log.e("sunyanlong", "keyCode:" + keyCode);

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

			calculateStepSize();
			performBackwardEvent();
			showController();
			break;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			calculateStepSize();
			performForwardEvent();
			showController();
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			showController();
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			boolean isPre = mCoverFlowViewListener.scrollPre(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					// mPlayImage.setBackgroundResource(R.drawable.play_stop);
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
					// mPlayImage.setBackgroundResource(R.drawable.play_stop);
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
		handler.removeMessages(PlayerController.CHANG_PLAYIMAGE);
		Log.e("sunyanlong","seekto:"+duration);
		mCtrlBarListener.onPlaySeekTo(duration, new OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer arg0) {
				handler.sendEmptyMessage(PlayerController.CHANG_PLAYIMAGE);
				if (isLongClick()) {
					handler.removeMessages(PlayerController.CHANG_PLAYIMAGE);
				}
				handler.sendEmptyMessageDelayed(PlayerController.CHANG_PLAYIMAGE, 500);
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

	public void showController() {
		handler.removeMessages(PlayerController.CHANG_VISIBLE);
		handler.sendEmptyMessageDelayed(PlayerController.CHANG_VISIBLE, 5000);
		setVisibility(VISIBLE);
		showPause(true);
	};


	
	/**长按步长*/
	private int mStepSize;
	/**长按事件*/
	private static final int DELAY_MILLIS_CHECK_LONG_CLICK=150;
	/**默认在 DELAY_MILLIS_CHECK_LONG_CLICK 秒内点击超过两次为长按事件，步长为影片时长mStepSize*/
	private static final int CLICK_COUNT=2;
	/**默认步长*/
	private static int DEFAULT_STEP_SIZE;
	
	protected void initWardData() {
		mStepSize=(int)Math.ceil(mDuration*0.01);
		DEFAULT_STEP_SIZE=(int)Math.ceil(mDuration*0.01);
	}
	
	
	private void calculateStepSize(){	
		mClickCount++;
		postCheckLongClick();
		if (mClickCount>=15) {
			mClickCount=15;
		}
		if (mClickCount>CLICK_COUNT) {
			mStepSize=(int)Math.ceil(mDuration*mClickCount/1000);
		}else {
			mStepSize=DEFAULT_STEP_SIZE;
		}

	}
	
	private int mClickCount=0;
	
	private Runnable mCheckLongClickRunnable =new Runnable() {
		@Override
		public void run() {
			mClickCount=0;
		}
	};
	
	private void postCheckLongClick(){
		handler.removeCallbacks(mCheckLongClickRunnable);
		handler.postDelayed(mCheckLongClickRunnable,DELAY_MILLIS_CHECK_LONG_CLICK);
	}
	

	public void performForwardEvent(){
		if (mCtrlBarContext.isPlayerPaused()) {
			return;
		}
		int seekTo1;
		int position1 = mCtrlBarContext.getPlayerCurPosition();
		
		if (position1 + mStepSize < mDuration) {
			seekTo1 = (int) (position1 +mStepSize);
		} else {
			seekTo1 = (int) mDuration;
		}
		mPlayImage.setBackgroundResource(R.drawable.play_kj);
		seekToDuration(seekTo1);
	} 
	
	public void performBackwardEvent(){
		if (mCtrlBarContext.isPlayerPaused()) {
			return;
		}
		int seekTo;

		int position = mCtrlBarContext.getPlayerCurPosition();
		if (position > mStepSize) {
			seekTo = (int) (position - mStepSize);
		} else {
			seekTo = 0;
		}
		mPlayImage.setBackgroundResource(R.drawable.play_kt);
		seekToDuration(seekTo);
		
	}
	/**检测是长按时间*/
	public boolean isLongClick(){
		return mClickCount>CLICK_COUNT?true:false;
	}
	
	public void setFullProgress(){
		mProgressBar.setProgress(FULLPROGRESS);
	}
	
}
