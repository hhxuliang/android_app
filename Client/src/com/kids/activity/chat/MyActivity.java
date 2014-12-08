package com.kids.activity.chat;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import com.kids.util.MessageDB;
import com.kids.util.MyDate;
import com.kids.util.SharePreferenceUtil;
import com.kids.util.UserDB;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.util.Constants;

/**
 * 自定义一个抽象的MyActivity类，每个Activity都继承他，实现消息的接收（优化性能，减少代码重复）
 * 
 * @author way
 * 
 */
public abstract class MyActivity extends Activity {
	/**
	 * 广播接收者，接收GetMsgService发送过来的消息
	 */
	private BroadcastReceiver MsgReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			TranObject msg = (TranObject) intent
					.getSerializableExtra(Constants.MSGKEY);
			HandleMsg hm = (HandleMsg) intent
					.getSerializableExtra(Constants.PICUPDATE);

			String msgs = (String) intent.getSerializableExtra("MSG");
			String id =(String)  intent.getSerializableExtra("SENDSTA");

			if (msg != null) {// 如果不是空，说明是消息广播
				System.out.println("MyActivity:" + msg.getFromUser());
				getMessage(msg);// 把收到的消息传递给子类
			} else if (hm != null) {
				getPicUpdate(hm);
			} else if (msgs != null && id!=null) {
				msgsendok(msgs,id);
			} else {
				unregisterReceiver(this);
				close();
			}

		}
	};
	public void msgsendok(String msgs,String id) {

	}
	public void getPicUpdate(HandleMsg hm) {

	}

	/**
	 * 抽象方法，用于子类处理消息，
	 * 
	 * @param msg
	 *            传递给子类的消息对象
	 */
	public void getMessage(TranObject msg) {// 重写父类的方法，处理消息
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case MESSAGE:
			TextMessage tm = (TextMessage) msg.getObject();
			String message = tm.getMessage();
			Toast.makeText(MyActivity.this,
					"亲！新消息哦 " + msg.getFromUser() + ":" + message, 0).show();// 提示用户
			receiveMsg(msg);
			break;
		case LOGIN:
			break;
		case LOGOUT:
			User logoutUser = (User) msg.getObject();
			Toast.makeText(MyActivity.this, "亲！" + logoutUser.getId() + "下线了哦",
					0).show();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {// 如果从后台恢复，服务被系统干掉，就重启一下服务
		super.onResume();
		// TODO Auto-generated method stub
		MyApplication application = (MyApplication) this
				.getApplicationContext();
		/*
		 * if (!application.isClientStart()) { Intent service = new Intent(this,
		 * GetMsgService.class); startService(service); }
		 */
		new SharePreferenceUtil(this, Constants.SAVE_USER).setIsStart(false);
		NotificationManager manager = application.getmNotificationManager();
		if (manager != null) {
			manager.cancel(Constants.NOTIFY_ID);
			application.setNewMsgNum(0);// 把消息数目置0
		}

	}

	public void receiveMsg(TranObject msg) {
	}

	/**
	 * 子类直接调用这个方法关闭应用
	 */
	public void close() {
		Intent i = new Intent();
		i.setAction(Constants.ACTION);
		sendBroadcast(i);
		finish();
		System.exit(0);
	}

	@Override
	public void onStart() {// 在start方法中注册广播接收者
		super.onStart();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION);
		registerReceiver(MsgReceiver, intentFilter);// 注册接受消息广播

	}

	@Override
	protected void onStop() {// 在stop方法中注销广播接收者
		super.onStop();
		unregisterReceiver(MsgReceiver);// 注销接受消息广播
	}
}
