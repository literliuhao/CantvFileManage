package com.cantv.media.center.ui.dialog;

public class DialogActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_weixin_top_right);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		finish();
		return true;
	}

	public void tip(View view)
	{
		Toast.makeText(this, "点击弹出框外部关闭窗口~", Toast.LENGTH_SHORT).show();
	}
}