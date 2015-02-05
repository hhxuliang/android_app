package com.kids.activity.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.kids.client.Client;
import com.kids.client.ClientOutputThread;
import com.kids.client.DownloadFile;
import com.kids.util.MessageDB;
import com.way.chat.activity.R;
import com.way.chat.common.util.MyDate;
import com.kids.util.ImageProcess;
import com.kids.util.SharePreferenceUtil;
import com.kids.util.UserDB;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.chat.common.util.Constants;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.Toast;

public class MyApplication extends Application {
	private Client client;// 客户端
	private boolean isClientStart;// 客户端连接是否启动
	private NotificationManager mNotificationManager;
	private int newMsgNum = 0;// 后台运行的消息
	private LinkedList<RecentChatEntity> mRecentList;
	private RecentChatAdapter mRecentAdapter;
	private int recentNum = 0;
	private UserDB userDB;
	private MessageDB messageDB;
	private String home_path;
	private String camera_path;
	private ArrayList<String> notReadmsslist = new ArrayList<String>();
	private HashMap<String, String> mNeedRefresh = new HashMap<String, String>();
	public static int mWindowHeight = 0;
	public static int mWindowWidth = 0;
	private boolean IsLogin = false;
	private DownloadFile downloadfile = null;
	public static final int DOWNLOADPIC_OK = 1;
	public static final int DOWNLOADPIC_FAULT = 2;
	private HashMap<String, Integer> mMap_Waiting_Download_Pic = new HashMap<String, Integer>();
	public static int[] imgs = { R.drawable.p0, R.drawable.p1, R.drawable.p2,
			R.drawable.p3, R.drawable.p4, R.drawable.p5, R.drawable.p6,
			R.drawable.p7, R.drawable.p8, R.drawable.p9 };
	public Handler handler_download_pic = new Handler() {
		public void handleMessage(Message msg) {
			HandleMsg hmsg = (HandleMsg) msg.obj;
			Integer ti = mMap_Waiting_Download_Pic.get(hmsg.mUrl);
			int uid = 0;
			if (ti != null)
				uid = ti.intValue();
			switch (msg.what) {
			case DOWNLOADPIC_OK:
				if (hmsg.mSavePath == null)
					break;

				if (ImageProcess.FileType.APK == ImageProcess
						.checkFileType(hmsg.mSavePath))
					installApk(hmsg.mSavePath);
				if (uid > 0) {
					hmsg.mComefromUid = uid;
					getMessageDB().updateMsg(hmsg.mComefromUid, hmsg.mSavePath,
							hmsg.mUrl);
					Intent broadCast = new Intent();
					broadCast.setAction(Constants.ACTION);
					broadCast.putExtra(Constants.PICUPDATE, hmsg);
					sendBroadcast(broadCast);// 把收到的消息已广播的形式发送出去
				} else
					System.out.println("获取图片消息对象失败!");
				break;
			case DOWNLOADPIC_FAULT:
				System.out.println("获取图片消息对象失败!");
				break;
			}
			mMap_Waiting_Download_Pic.remove(hmsg.mUrl);
		}
	};

	/**
	 * å®è£APKæä»¶
	 */
	private void installApk(String path) {
		File apkfile = new File(path);
		if (!apkfile.exists()) {
			return;
		}
		// // éè¿Intentå®è£APKæä»¶
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.fromFile(apkfile),
				"application/vnd.android.package-archive");
		startActivity(i);
	}

	public void startDownloadPic(String msg, int uid) {
		stardownloadpicthread();
		if (downloadfile != null) {
			downloadfile.startDownloadPic(msg, uid);
		}
	}

	public void stardownloadpicthread() {
		if (downloadfile == null
				|| (downloadfile != null && downloadfile.getStatu() == 2)) {
			downloadfile = new DownloadFile(this,
					this.mMap_Waiting_Download_Pic);

			downloadfile.start();
		}
	}

	public ArrayList<String> getNotReadmsslist() {
		return notReadmsslist;
	}

	public void setNotReadmsslist(ArrayList<String> notReadmsslist) {
		this.notReadmsslist.addAll(notReadmsslist);
	}

	public boolean isIsLogin() {
		return IsLogin;
	}

	public void setIsLogin(boolean isLogin) {
		IsLogin = isLogin;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initEnv();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext(), this);

		SharePreferenceUtil util = new SharePreferenceUtil(this,
				Constants.SAVE_USER);
		System.out.println(util.getIp() + " " + util.getPort());
		client = new Client(util.getIp(), util.getPort());// 从配置文件中读ip和地址
		userDB = new UserDB(MyApplication.this);// 本地用户数据库
		messageDB = new MessageDB(MyApplication.this);

		initRencentAdap();

	}

	public void initEnv() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// 创建一个文件夹对象，赋值为外部存储器的目录
			File sdcardDir = Environment.getExternalStorageDirectory();
			// 得到一个路径，内容是sdcard的文件夹路径和名字
			String path = sdcardDir.getPath() + "/children";
			setHomePath(path);

			File path1 = new File(path);
			if (!path1.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path1.mkdirs();
			}

			String path_camera = getCameraPath();
			File path_camera_f = new File(path_camera);
			if (!path_camera_f.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path_camera_f.mkdirs();
			}
			String path_pic = getMyUploadPicPath();
			File path_pic_f = new File(path_pic);
			if (!path_pic_f.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path_pic_f.mkdirs();
			}

			String path_pic_1 = getDownloadPicPath();
			File path_pic_f_1 = new File(path_pic_1);
			if (!path_pic_f_1.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path_pic_f_1.mkdirs();
			}

			path_pic_1 = getDownloadVoicePath();
			path_pic_f_1 = new File(path_pic_1);
			if (!path_pic_f_1.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path_pic_f_1.mkdirs();
			}
			path_pic_1 = getSendVoicePath();
			path_pic_f_1 = new File(path_pic_1);
			if (!path_pic_f_1.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path_pic_f_1.mkdirs();
			}
		} else {
			Toast.makeText(this, "内存卡不存在...", Toast.LENGTH_LONG).show();
			return;

		}

	}

	private void initRencentAdap() {
		mRecentList = new LinkedList<RecentChatEntity>();
		List<User> list = userDB.getUser("");
		for (User u : list) {
			List<ChatMsgEntity> lt = messageDB.getMsg(u.getId(), "", 1);
			if (lt.size() == 1) {
				ChatMsgEntity msg = lt.get(0);
				RecentChatEntity entity = new RecentChatEntity(u.getId(),
						u.getImg(), 0, u.getName(), msg.getDate(),
						msg.getMessage(), u.getIsCrowd() , msg.getmsgtype());
				mRecentList.add(entity);
			}
			if (messageDB.GetMsgReadSta(u.getId()))
				notReadmsslist.add(u.getId() + "");
		}
		mRecentAdapter = new RecentChatAdapter(getApplicationContext(),
				mRecentList);
	}

	public HashMap<String, String> getNeedRefreshMap() {
		return mNeedRefresh;
	}

	public void updateDBbyMsgOk(String datekey, String id) {
		messageDB.updateDBbyMsgOk(datekey, id);
		Intent broadCast = new Intent();
		broadCast.setAction(Constants.ACTION);
		broadCast.putExtra("MSGDATEKEY", datekey);
		broadCast.putExtra("SENDSTA", id + "");
		sendBroadcast(broadCast);// 把收到的消息已广播的形式发送出去
	}

	public boolean needRefresh(String uidstr) {
		if (mNeedRefresh.get(uidstr) == null) {
			return false;
		} else {
			removeNeedRefresh(uidstr);
			return true;
		}
	}

	public void addNeedRefresh(String uidstr) {
		System.out.println("and uidster      " + uidstr);
		if (mNeedRefresh.get(uidstr) == null) {
			System.out.println("and uidster" + uidstr);
			mNeedRefresh.put(uidstr, uidstr);
		}
	}

	public void removeNeedRefresh(String uidstr) {
		mNeedRefresh.remove(uidstr);
	}

	public String getHomePath() {
		return home_path;
	}

	public void setHomePath(String p) {
		home_path = p;
	}

	public String getCameraPath() {
		return home_path + "/camerapicpath";
	}

	public String getMyUploadPicPath() {
		return home_path + "/picpath/MyUpload";
	}

	public String getDownloadPicPath() {
		return home_path + "/picpath/ClassSharing";
	}

	public String getDownloadVoicePath() {
		return home_path + "/voicepath/download";
	}

	public String getSendVoicePath() {
		return home_path + "/voicepath/sendout";
	}

	public UserDB getUserDB() {
		return userDB;
	}

	public MessageDB getMessageDB() {
		return messageDB;
	}

	public Client getClient() {
		return client;
	}

	public boolean isClientStart() {
		return isClientStart;
	}

	public void setClientStart(boolean isClientStart) {
		this.isClientStart = isClientStart;
	}

	public NotificationManager getmNotificationManager() {
		return mNotificationManager;
	}

	public void setmNotificationManager(NotificationManager mNotificationManager) {
		this.mNotificationManager = mNotificationManager;
	}

	public int getNewMsgNum() {
		return newMsgNum;
	}

	public void setNewMsgNum(int newMsgNum) {
		this.newMsgNum = newMsgNum;
	}

	public LinkedList<RecentChatEntity> getmRecentList() {
		return mRecentList;
	}

	public void setmRecentList(LinkedList<RecentChatEntity> mRecentList) {
		this.mRecentList = mRecentList;
	}

	public RecentChatAdapter getmRecentAdapter() {
		return mRecentAdapter;
	}

	public void setmRecentAdapter(RecentChatAdapter mRecentAdapter) {
		this.mRecentAdapter = mRecentAdapter;
	}

	public int getRecentNum() {
		return recentNum;
	}

	public void setRecentNum(int recentNum) {
		this.recentNum = recentNum;
	}

	public void clossDB() {

		if (messageDB != null)
			messageDB.close();
		if (userDB != null)
			userDB.close();

	}

	public ChatMsgEntity send(String contString, int msgtype,
			String pic_path_local, User user) {
		ClientOutputThread out = client.getClientOutputThread();
		ChatMsgEntity entity = null;

		SharePreferenceUtil util = new SharePreferenceUtil(
				getApplicationContext(), Constants.SAVE_USER);
		if (contString.length() > 0) {
			entity = new ChatMsgEntity();
			entity.setName(util.getName());
			entity.setDate(MyDate.getDateEN());
			entity.setmsgtype(msgtype);
			entity.setMessage(contString);
			entity.setImg(util.getImg());
			entity.setMsgType(false);
			entity.setPicPath(pic_path_local);
			entity.setSendSta(-1);
			entity.setDatekey(MyDate.getDateMillis());
			messageDB.saveMsg(user.getId(), entity);
			addNeedRefresh(user.getId() + "");

			if (isClientStart() && out != null) {
				TranObject<TextMessage> o = new TranObject<TextMessage>(
						TranObjectType.MESSAGE);
				TextMessage message = new TextMessage();
				message.setMessage(contString);
				message.setmsgtype(msgtype);
				message.setDatekey(entity.getDatekey());
				o.setObject(message);
				o.setFromUser(Integer.parseInt(util.getId()));
				o.setFromUserName(util.getName());
				o.setFromImg(util.getImg());
				o.setToUser(user.getId());
				if (user.getIsCrowd() == 1)
					o.setCrowd(user.getId());
				else
					o.setCrowd(0);
				out.setMsg(o);
			}

			// 下面是添加到最近会话列表的处理，在按发送键之后
			RecentChatEntity entity1 = new RecentChatEntity(user.getId(),
					user.getImg(), 0, user.getName(), MyDate.getDate(),
					contString, user.getIsCrowd(),entity.getmsgtype());
			getmRecentList().remove(entity1);
			getmRecentList().addFirst(entity1);
			getmRecentAdapter().notifyDataSetChanged();
		}
		return entity;
	}

	public int Resend(String contString, int is_pic, String pic_path_local,
			User user, String datekey) {
		ClientOutputThread out = client.getClientOutputThread();
		if (!isClientStart() || out == null) {
			return -1;
		}
		SharePreferenceUtil util = new SharePreferenceUtil(
				getApplicationContext(), Constants.SAVE_USER);
		if (contString.length() > 0) {
			if (out != null) {
				TranObject<TextMessage> o = new TranObject<TextMessage>(
						TranObjectType.MESSAGE);
				TextMessage message = new TextMessage();
				message.setMessage(contString);
				message.setmsgtype(is_pic);
				message.setDatekey(datekey);
				o.setObject(message);
				o.setFromUser(Integer.parseInt(util.getId()));
				o.setFromUserName(util.getName());
				o.setFromImg(util.getImg());

				o.setToUser(user.getId());
				if (user.getIsCrowd() == 1)
					o.setCrowd(user.getId());
				else
					o.setCrowd(0);
				out.setMsg(o);
			}
		}
		return 0;
	}
}
