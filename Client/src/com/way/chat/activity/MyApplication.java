package com.way.chat.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.util.Constants;
import com.way.client.Client;
import com.way.util.MessageDB;
import com.way.util.MyDate;
import com.way.util.SharePreferenceUtil;
import com.way.util.UserDB;

import android.app.Application;
import android.app.NotificationManager;
import android.util.DisplayMetrics;

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
	private ArrayList<String> offlinemsslist;
	private HashMap<String, String> mNeedRefresh = new HashMap<String, String>();
	public static int mWindowHeight = 0;
	public static int mWindowWidth = 0;
	private boolean IsLogin = false;

	public boolean isIsLogin() {
		return IsLogin;
	}

	public void setIsLogin(boolean isLogin) {
		IsLogin = isLogin;
	}

	@Override
	public void onCreate() {
		SharePreferenceUtil util = new SharePreferenceUtil(this,
				Constants.SAVE_USER);
		System.out.println(util.getIp() + " " + util.getPort());
		client = new Client(util.getIp(), util.getPort());// 从配置文件中读ip和地址
		userDB = new UserDB(MyApplication.this);// 本地用户数据库
		messageDB = new MessageDB(MyApplication.this);
		initRencentAdap();
		super.onCreate();
	}

	private void initRencentAdap() {
		mRecentList = new LinkedList<RecentChatEntity>();
		List<User> list = userDB.getUser();
		for (User u : list) {
			List<ChatMsgEntity> lt = messageDB.getMsg(u.getId(), "", 1);
			if (lt.size() == 1) {
				ChatMsgEntity msg = lt.get(0);
				RecentChatEntity entity = new RecentChatEntity(u.getId(),
						u.getImg(), 0, u.getName(), msg.getDate(),
						msg.getMessage());
				mRecentList.add(entity);
			}
		}
		mRecentAdapter = new RecentChatAdapter(getApplicationContext(),
				mRecentList);
	}

	public HashMap<String, String> getNeedRefreshMap() {
		return mNeedRefresh;
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

	public ArrayList<String> getOffLineList() {
		return offlinemsslist;
	}

	public void setOffLineList(ArrayList<String> l) {
		offlinemsslist = l;
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

	}
}
