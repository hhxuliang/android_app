package com.way.chat.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.chat.common.util.Constants;
import com.way.client.Client;
import com.way.client.ClientInputThread;
import com.way.client.ClientOutputThread;
import com.way.client.MessageListener;
import com.way.util.MessageDB;
import com.way.util.MyDate;
import com.way.util.SharePreferenceUtil;
import com.way.util.UserDB;
import com.way.util.ZipUtil;

/**
 * 收取消息服务
 * 
 * @author way
 * 
 */
public class GetMsgService extends Service {
	private static final int MSG = 0x001;
	private static final int DOWNLOADPIC_OK = 1;
	private static final int DOWNLOADPIC_FAULT = 2;

	private MyApplication application;
	private Client client;
	private NotificationManager mNotificationManager;
	private boolean isStart = false;// 是否与服务器连接上
	private Notification mNotification;
	private Context mContext = this;
	private SharePreferenceUtil util;
	private MessageDB messageDB;
	private HashMap<String, TranObject> mMap_Waiting_Download_Pic = new HashMap<String, TranObject>();

	private Handler handler_download_pic = new Handler() {
		public void handleMessage(Message msg) {
			HandleMsg hmsg = (HandleMsg) msg.obj;
			TranObject tobj = mMap_Waiting_Download_Pic.get(hmsg.mUrl);
			switch (msg.what) {
			case DOWNLOADPIC_OK:
				if (tobj != null) {
					hmsg.mComefromUid = tobj.getFromUser();
					messageDB.updateMsg(tobj.getFromUser(), hmsg.mSavePath,
							hmsg.mUrl);
					Intent broadCast = new Intent();
					broadCast.setAction(Constants.ACTION);
					broadCast.putExtra(Constants.PICUPDATE, hmsg);
					sendBroadcast(broadCast);// 把收到的消息已广播的形式发送出去
				} else
					System.out.println("获取图片消息对象失败!");
				break;
			case DOWNLOADPIC_FAULT:
				System.out.println("获取图片消息对象失败!应该再来一次！");
				break;
			}
			mMap_Waiting_Download_Pic.remove(hmsg.mUrl);
		}
	};

	class Download implements Runnable {
		private String mUrl = null;

		public Download(String p) {
			mUrl = p;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			try {
				URL url = new URL(mUrl);
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				con.setConnectTimeout(5000);
				con.setRequestMethod("GET");
				con.connect();
				String prefix = mUrl.substring(mUrl.lastIndexOf("."));
				if (prefix.substring(0, 4).equals(".mp4"))
					prefix = ".mp4";// i don't know why need this it is like
									// some network tranfer error,the prefix is
									// ".mp4_" if not this

				if (con.getResponseCode() == 200) {
					InputStream is = con.getInputStream();
					String savePath = application.getPicPath() + "/"
							+ MyDate.getDateForImageName() + prefix;
					FileOutputStream fos = new FileOutputStream(savePath);
					byte[] buffer = new byte[8192];
					int count = 0;
					while ((count = is.read(buffer)) != -1) {
						fos.write(buffer, 0, count);
					}
					fos.close();
					is.close();

					handler_download_pic.obtainMessage(DOWNLOADPIC_OK,
							new HandleMsg(mUrl, savePath)).sendToTarget();
				} else {
					handler_download_pic.obtainMessage(DOWNLOADPIC_FAULT,
							new HandleMsg(mUrl, "")).sendToTarget();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onCreate() {// 在onCreate方法里面注册广播接收者
		// TODO Auto-generated method stub
		super.onCreate();
		messageDB = new MessageDB(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BACKKEY_ACTION);
		registerReceiver(backKeyReceiver, filter);
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		application = (MyApplication) this.getApplicationContext();
		client = application.getClient();
		application.setmNotificationManager(mNotificationManager);
	}

	// 收到用户按返回键发出的广播，就显示通知栏
	private BroadcastReceiver backKeyReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Toast.makeText(context, "QQ进入后台运行", 0).show();
			setMsgNotification();
		}
	};
	// 用来更新通知栏消息的handler
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG:
				int newMsgNum = application.getNewMsgNum();// 从全局变量中获取
				newMsgNum++;// 每收到一次消息，自增一次
				application.setNewMsgNum(newMsgNum);// 再设置为全局变量
				TranObject<TextMessage> textObject = (TranObject<TextMessage>) msg
						.getData().getSerializable("msg");
				// System.out.println(textObject);
				if (textObject != null) {
					int form = textObject.getFromUser();// 消息从哪里来
					String content = textObject.getObject().getMessage();// 消息内容

					ChatMsgEntity entity = new ChatMsgEntity("",
							MyDate.getDateEN(), content, -1, true, false, "");// 收到的消息
					// messageDB.saveMsg(form, entity);// 保存到数据库

					// 更新通知栏
					int icon = R.drawable.notify_newmessage;
					CharSequence tickerText = form + ":" + content;
					long when = System.currentTimeMillis();
					mNotification = new Notification(icon, tickerText, when);

					mNotification.flags = Notification.FLAG_NO_CLEAR;
					// 设置默认声音
					mNotification.defaults |= Notification.DEFAULT_SOUND;
					// 设定震动(需加VIBRATE权限)
					mNotification.defaults |= Notification.DEFAULT_VIBRATE;
					mNotification.contentView = null;

					Intent intent = new Intent(mContext,
							FriendListActivity.class);
					PendingIntent contentIntent = PendingIntent.getActivity(
							mContext, 0, intent, 0);
					mNotification.setLatestEventInfo(mContext, util.getName()
							+ " (" + newMsgNum + "条新消息)", content,
							contentIntent);
				}
				mNotificationManager.notify(Constants.NOTIFY_ID, mNotification);// 通知一下才会生效哦
				break;

			default:
				break;
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		util = new SharePreferenceUtil(getApplicationContext(),
				Constants.SAVE_USER);
		isStart = client.start();
		application.setClientStart(isStart);
		System.out.println("client start:" + isStart);
		if (isStart) {
			ClientInputThread in = client.getClientInputThread();
			in.setMessageListener(new MessageListener() {

				@Override
				public void Message(TranObject msg) {
					System.out.println("GetMsgService:" + msg);
					System.out.println("GetMsgService:" + msg.getType());
					System.out.println("GetMsgService:" + msg.getFromUser());
					System.out.println("GetMsgService:" + util.getIsStart());
					preProcess(msg);
					if (util.getIsStart()) {// 如果 是在后台运行，就更新通知栏，否则就发送广播给Activity
						if (msg.getType() == TranObjectType.MESSAGE) {// 只处理文本消息类型
							// System.out.println("收到新消息");
							// 把消息对象发送到handler去处理
							Message message = handler.obtainMessage();
							message.what = MSG;
							message.getData().putSerializable("msg", msg);
							handler.sendMessage(message);
						}
					} else {
						Intent broadCast = new Intent();
						broadCast.setAction(Constants.ACTION);
						broadCast.putExtra(Constants.MSGKEY, msg);
						sendBroadcast(broadCast);// 把收到的消息已广播的形式发送出去
					}
				}
			});
		}
	}

	/*
	 * At here, we need preprocess the msg: 1.save the message into local db and
	 * update the adapter object 2.media reminder
	 */
	public void preProcess(TranObject msg) {
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case MESSAGE:
			TextMessage tm = (TextMessage) msg.getObject();
			String message = tm.getMessage();
			ChatMsgEntity entity = new ChatMsgEntity("", MyDate.getDateEN(),
					message, -1, true, tm.get_is_pic(), "");// 收到的消息
			/*
			 * if (msg.getCrowd() == user.getId()) {
			 * entity.setName(msg.getFromUserName());
			 * entity.setImg(msg.getFromImg()); }
			 */
			if (tm.get_is_pic()) {
				// new thread to download the picture to update the picpath in
				// local db
				mMap_Waiting_Download_Pic.put(tm.getMessage(), msg);
				new Thread(new Download(tm.getMessage())).start();
			}
			messageDB.saveMsg(msg.getFromUser(), entity);// 保存到数据库

			UserDB userDB = application.getUserDB();
			User user2 = userDB.selectInfo(msg.getFromUser());// 通过id查询对应数据库该好友信息
			RecentChatEntity entity2 = new RecentChatEntity(msg.getFromUser(),
					user2.getImg(), 0, user2.getName(), MyDate.getDate(),
					message);
			application.addNeedRefresh(msg.getFromUser() + "");
			application.getmRecentAdapter().remove(entity2);// 先移除该对象，目的是添加到首部
			application.getmRecentList().addFirst(entity2);// 再添加到首部

			MediaPlayer.create(this, R.raw.msg).start();// 声音提示
			break;
		case LOGIN:
			MediaPlayer.create(this, R.raw.msg).start();
			break;
		case LOGOUT:
			MediaPlayer.create(this, R.raw.msg).start();
			break;
		default:
			break;
		}
	}

	@Override
	// 在服务被摧毁时，做一些事情
	public void onDestroy() {
		super.onDestroy();
		if (messageDB != null)
			messageDB.close();
		unregisterReceiver(backKeyReceiver);
		mNotificationManager.cancel(Constants.NOTIFY_ID);
		// 给服务器发送下线消息
		if (isStart) {
			ClientOutputThread out = client.getClientOutputThread();
			TranObject<User> o = new TranObject<User>(TranObjectType.LOGOUT);
			User u = new User();
			u.setId(Integer.parseInt(util.getId()));
			o.setObject(u);
			out.setMsg(o);
			// 发送完之后，关闭client
			out.setStart(false);
			client.getClientInputThread().setStart(false);
		}
		// Intent intent = new Intent(this, GetMsgService.class);
		// startService(intent);
	}

	/**
	 * 创建通知
	 */
	private void setMsgNotification() {
		int icon = R.drawable.notify;
		CharSequence tickerText = "";
		long when = System.currentTimeMillis();
		mNotification = new Notification(icon, tickerText, when);

		// 放置在"正在运行"栏目中
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;

		RemoteViews contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.notify_view);
		contentView.setTextViewText(R.id.notify_name, util.getName());
		contentView.setTextViewText(R.id.notify_msg, "正在后台运行");
		contentView.setTextViewText(R.id.notify_time, MyDate.getDate());
		// 指定个性化视图
		mNotification.contentView = contentView;

		Intent intent = new Intent(this, MyMainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// 指定内容意图
		mNotification.contentIntent = contentIntent;
		mNotificationManager.notify(Constants.NOTIFY_ID, mNotification);
	}
}
