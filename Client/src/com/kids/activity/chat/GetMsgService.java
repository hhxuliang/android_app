package com.kids.activity.chat;

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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.kids.client.Client;
import com.kids.client.ClientInputThread;
import com.kids.client.ClientOutputThread;
import com.kids.client.MessageListener;
import com.kids.util.DialogFactory;
import com.kids.util.Encode;
import com.kids.util.MessageDB;
import com.way.chat.common.util.MyDate;
import com.kids.util.MyUtils;
import com.kids.util.SharePreferenceUtil;
import com.kids.util.UploadUtil;
import com.kids.util.UserDB;
import com.kids.util.ZipUtil;
import com.way.chat.activity.R;
import com.way.chat.common.bean.CommonMsg;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.chat.common.util.Constants;

/**
 * 收取消息服务
 * 
 * @author way
 * 
 */
public class GetMsgService extends Service {
	private static final int MSG = 0x001;

	public static MyApplication application;
	private Client client;
	private NotificationManager mNotificationManager;
	private boolean isStart = false;// 是否与服务器连接上
	private Notification mNotification;
	private Context mContext = this;
	private SharePreferenceUtil util;
	private MessageDB messageDB;

	private final Timer timer = new Timer();
	private TimerTask task;
	private int mHeartBeat = 0;

	public static String getMyName() {
		return "com.kids.activity.chat.GetMsgService";

	}

	@Override
	public void onCreate() {// 在onCreate方法里面注册广播接收者
		// TODO Auto-generated method stub
		super.onCreate();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BACKKEY_ACTION);
		registerReceiver(backKeyReceiver, filter);
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		application = (MyApplication) this.getApplicationContext();
		messageDB = application.getMessageDB();
		client = application.getClient();
		application.setmNotificationManager(mNotificationManager);
		util = new SharePreferenceUtil(getApplicationContext(),
				Constants.SAVE_USER);
		final IntentFilter homeFilter = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

		registerReceiver(homePressReceiver, homeFilter);

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
							MyDate.getDateEN(), content, -1, true, 0, "");// 收到的消息
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

					Intent intent = new Intent(mContext, MyMainActivity.class);
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
		task = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mHeartBeat == 2 && client.getClientOutputThread().isStart()
						&& client.getClientInputThread().isStart()) {
					isStart = true;
					application.setClientStart(isStart);
					client.sendHeartBeat();
					mHeartBeat = 1;
				} else {
					application.setClientStart(false);
					client.stopNet();
					start_client_socket();
				}

			}
		};
		timer.schedule(task, 500, 15000);
	}

	public void start_client_socket() {

		isStart = client.start();
		
		if (isStart) {
			mHeartBeat = 2;
			ClientInputThread in = client.getClientInputThread();
			in.setMessageListener(new MessageListener() {

				@Override
				public void Message(TranObject msg) {
					preProcess(msg);
					if (util.getIsStart()) {// 如果 是在后台运行，就更新通知栏，否则就发送广播给Activity
						if (msg.getType() == TranObjectType.MESSAGE) {// 只处理文本消息类型
							// System.out.println("收到新消息");
							// 把消息对象发送到handler去处理
							// Toast.makeText(context, "", 0).show();
							setMsgNotification();
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
			application.setClientStart(isStart);
			MyUtils.login(util.getName(), util.getPasswd(), application);

		}
	}

	/*
	 * At here, we need preprocess the msg: 1.save the message into local db and
	 * update the adapter object 2.media reminder
	 */
	public void preProcess(TranObject msg) {
		// TODO Auto-generated method stub
		UserDB userDB = application.getUserDB();
		switch (msg.getType()) {
		case MESSAGE:
			TextMessage tm = (TextMessage) msg.getObject();
			int uid = 0;
			if (msg.getCrowd() > 0)
				uid = msg.getCrowd();
			else
				uid = msg.getFromUser();
			CommonMsg cmg = new CommonMsg();
			cmg.setarg1(tm.getDatekey());
			TranObject<CommonMsg> ack = new TranObject<CommonMsg>(
					TranObjectType.ACKMSG);
			if(util.getId().length()>0)
				ack.setFromUser(Integer.parseInt(util.getId()));
			ack.setObject(cmg);

			if (client != null && application.isClientStart()
					&& client.getClientOutputThread().isStart()) {
				ClientOutputThread out = client.getClientOutputThread();
				out.setMsg(ack);
			}

			String message = tm.getMessage();
			ChatMsgEntity entity = new ChatMsgEntity(msg.getFromUserName(),
					MyDate.getDateEN(), message, msg.getFromImg(), true,
					tm.getmsgtype(), "");// 收到的消息
			entity.setMsgid(tm.getMessageid());
			entity.setDatekey(tm.getDatekey());
			entity.setServerdatekey(tm.getServerdatekey());

			if (msg.getCrowd() > 0) {
				entity.setName(msg.getFromUserName());
				entity.setImg(msg.getFromImg());
			}

			if (tm.getmsgtype()==1 || tm.getmsgtype()==2) {
				// new thread to download the picture to update the picpath in
				// local db
				application.startDownloadPic(tm.getMessage(), uid);
			}

			messageDB.saveMsg(uid, entity);// 保存到数据库

			User user2 = userDB.selectInfo(uid);// 通过id查询对应数据库该好友信息
			RecentChatEntity entity2 = new RecentChatEntity(uid,
					user2.getImg(), 0, user2.getName(), MyDate.getDate(),
					message, user2.getIsCrowd(),tm.getmsgtype());
			if (!application.getNotReadmsslist().contains(uid + ""))
				application.getNotReadmsslist().add(uid + "");
			application.addNeedRefresh(uid + "");
			application.getmRecentAdapter().remove(entity2);// 先移除该对象，目的是添加到首部
			application.getmRecentList().addFirst(entity2);// 再添加到首部

			MediaPlayer.create(this, R.raw.msg).start();// 声音提示
			break;
		case LOGIN:
			List<User> list = (List<User>) msg.getObject();
			if (list != null && list.size() > 0) {
				// 保存用户信息
				application.getUserDB().updateUser(list);
				application.setIsLogin(true);
			}

			for (String s : userDB.getCrowdid()) {
				String where = messageDB.getServerDatekeybyCrowd(Integer
						.parseInt(s));
				MyUtils.sendCrowdofflineMsgReq(util.getId(), s, where,
						application);
			}

			CommonMsg cmg_v = new CommonMsg();
			cmg_v.setarg1(Constants.VERSION);
			cmg_v.setarg2(Constants.VERSION);
			cmg_v.setarg3(Constants.VERSION);
			TranObject<CommonMsg> version = new TranObject<CommonMsg>(
					TranObjectType.VERSION);
			if (!util.getId().equals("")) {
				version.setFromUser(Integer.parseInt(util.getId()));
				version.setObject(cmg_v);
				if (client != null && application.isClientStart()
						&& client.getClientOutputThread().isStart()) {
					ClientOutputThread out = client.getClientOutputThread();
					out.setMsg(version);
				}
			}
			// send out the error msg to server

			UploadUtil uploadUtil = UploadUtil.getInstance();
			File[] files = new File(application.getHomePath() + "/log/")
					.listFiles();
			if (files != null)
				for (File file : files) {
					if (file.isFile()) {
						String picstr = file.getPath();
						String fileKey = picstr.substring(picstr
								.lastIndexOf("."));
						if (fileKey.equals(".errorlog"))
							uploadUtil.uploadFile(picstr, fileKey,
									Constants.FILE_UPLOAD_URL, null);

					}
				}

			break;
		case LOGOUT:
			// MediaPlayer.create(this, R.raw.msg).start();
			break;
		case HEARTBEAT:
			this.mHeartBeat = 2;
			break;
		case ACKMSG:
			CommonMsg cm = (CommonMsg) msg.getObject();
			if ((cm.getarg1() != null && cm.getarg1().length() > 0)
					|| (cm.getarg2() != null && cm.getarg2().length() > 0))
				application.updateDBbyMsgOk(cm.getarg1(), cm.getarg2());
			break;

		default:
			break;
		}
	}

	@Override
	// 在服务被摧毁时，做一些事情
	public void onDestroy() {
		super.onDestroy();
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
		timer.cancel();
		timer.purge();
		// Intent intent = new Intent(this, GetMsgService.class);
		// startService(intent);
		if (homePressReceiver != null) {

			try {

				unregisterReceiver(homePressReceiver);

			} catch (Exception e) {

				e.printStackTrace();

			}

		}

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

	private final BroadcastReceiver homePressReceiver = new BroadcastReceiver() {

		final String SYSTEM_DIALOG_REASON_KEY = "reason";

		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {

				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

				if (reason != null
						&& reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {

					util.setIsStart(true);// 设置后台运行标志，正在运行

				}

			}

		}

	};

}
