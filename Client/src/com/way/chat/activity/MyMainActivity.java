package com.way.chat.activity;

import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.util.Constants;
import com.way.util.SharePreferenceUtil;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;

public class MyMainActivity extends MyActivity {
	/** Called when the activity is first created. */
	MyImageView message;
	MyImageView picture;
	private MenuInflater mi;// 菜单
	MyApplication application;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
						PullToRefreshSampleActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	public void onBackPressed() {// 捕获返回按键事件，进入后台运行
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mi.inflate(R.menu.mymainactivity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	// 菜单选项添加事件处理
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.exit:
			exitDialog(MyMainActivity.this, "提示", "亲！您真的要退出吗？");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 完全退出提示窗
	private void exitDialog(Context context, String title, String msg) {
		new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 关闭服务
						application.clossDB();
						if (application.isClientStart()) {
							Intent service = new Intent(MyMainActivity.this,
									GetMsgService.class);
							stopService(service);
						}
						close();// 父类关闭方法
					}
				}).setNegativeButton("取消", null).create().show();
	}

}