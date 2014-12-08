package com.kids.activity.chat;



import com.kids.activity.calendar.ClassCalenderActivity;
import com.kids.util.SharePreferenceUtil;
import com.way.chat.activity.R;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.util.Constants;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

public class MyMainActivity extends MyActivity {
	/** Called when the activity is first created. */
	MyImageView message;
	MyImageView picture;
	private MenuInflater mi;// 菜单
	MyApplication application;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉状态栏
		setContentView(R.layout.main_home);
		mi = new MenuInflater(this);
		message = (MyImageView) findViewById(R.id.c_joke);
		picture = (MyImageView) findViewById(R.id.c_constellation);
		application = (MyApplication) this.getApplicationContext();
		message.setOnClickIntent(new MyImageView.OnViewClick() {
			@Override
			public void onClick() {
				Intent i = new Intent(MyMainActivity.this,
						FriendListActivity.class);
				startActivity(i);
			}
		});
		picture.setOnClickIntent(new MyImageView.OnViewClick() {
			@Override
			public void onClick() {
				Intent i = new Intent(MyMainActivity.this,
						ClassCalenderActivity.class);
				startActivity(i);
			}
		});
	}
	@Override
	protected void onResume() {// 如果从后台恢复，服务被系统干掉，就重启一下服务
		// TODO Auto-generated method stub
		super.onResume();
	}
	@Override
	public void onBackPressed() {// 捕获返回按键事件，进入后台运行
		Dialog alertDialog = new AlertDialog.Builder(MyMainActivity.this)
				.setTitle("选择")
				.setPositiveButton("退出", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						exitsys();
					}
				}).setNeutralButton("注销",
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO Auto-generated method stub
						SharePreferenceUtil util = new SharePreferenceUtil(
								MyMainActivity.this, Constants.SAVE_USER);
						util.setPasswd("");
						util.setName("");
						exitsys();
					}
				})
				.setNegativeButton("后台运行",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								backRun();
							}
						}).create();
		alertDialog.show();
	}

	public void backRun() {
		// TODO Auto-generated method stub
		// 发送广播，通知服务，已进入后台运行
		Intent i = new Intent();
		i.setAction(Constants.BACKKEY_ACTION);
		sendBroadcast(i);
		SharePreferenceUtil util = new SharePreferenceUtil(this,
				Constants.SAVE_USER);
		util.setIsStart(true);// 设置后台运行标志，正在运行
		finish();// 再结束自己
	}

	private void exitsys() {
		// 关闭服务
		application.clossDB();
		if (application.isClientStart()) {
			Intent service = new Intent(MyMainActivity.this,
					GetMsgService.class);
			stopService(service);
		}
		close();// 父类关闭方法
	}

}