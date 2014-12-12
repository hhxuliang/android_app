package com.kids.activity.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.kids.client.Client;
import com.kids.client.ClientOutputThread;
import com.kids.util.MessageDB;
import com.kids.util.MyDate;
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
	private ArrayList<String> offlinemsslist = new ArrayList<String>();
	private ArrayList<String> notReadmsslist = new ArrayList<String>();
	private HashMap<String, String> mNeedRefresh = new HashMap<String, String>();
	public static int mWindowHeight = 0;
	public static int mWindowWidth = 0;
	private boolean IsLogin = false;
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
			if(messageDB.GetMsgReadSta(u.getId()))
				notReadmsslist.add(u.getId()+"");
		}
		mRecentAdapter = new RecentChatAdapter(getApplicationContext(),
				mRecentList);
	}

	public HashMap<String, String> getNeedRefreshMap() {
		return mNeedRefresh;
	}
	public void updateDBbyMsgOk(String msg, int id) {
		messageDB.updateDBbyMsgOk(msg, id);
		Intent broadCast = new Intent();
		broadCast.setAction(Constants.ACTION);
		broadCast.putExtra("MSG", msg);
		broadCast.putExtra("SENDSTA", id+"");
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

	public ArrayList<String> getOffLineList() {
		return offlinemsslist;
	}

	public void setOffLineList(ArrayList<String> l) {
		offlinemsslist.addAll(l);
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
	public int send(String contString, boolean is_pic, String pic_path_local,User user) {
		ClientOutputThread out = client.getClientOutputThread();
		if (!isClientStart()  || out == null) {
			return -1;
		}
		SharePreferenceUtil util = new SharePreferenceUtil(getApplicationContext(),
				Constants.SAVE_USER);
		if (contString.length() > 0) {
			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setName(util.getName());
			entity.setDate(MyDate.getDateEN());
			entity.set_is_pic(is_pic);
			entity.setMessage(contString);
			entity.setImg(util.getImg());
			entity.setMsgType(false);
			entity.setPicPath(pic_path_local);
			entity.setSendSta(-1);
			messageDB.saveMsg(user.getId(), entity);
			addNeedRefresh(user.getId()+"");
			

			if (out != null) {
				TranObject<TextMessage> o = new TranObject<TextMessage>(
						TranObjectType.MESSAGE);
				TextMessage message = new TextMessage();
				message.setMessage(contString);
				message.set_is_pic(is_pic);
				o.setObject(message);
				o.setFromUser(Integer.parseInt(util.getId()));
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
					contString);
			getmRecentList().remove(entity1);
			getmRecentList().addFirst(entity1);
			getmRecentAdapter().notifyDataSetChanged();
		}
		return 0;
	}
	
	public int Resend(String contString, boolean is_pic, String pic_path_local,User user) {
		ClientOutputThread out = client.getClientOutputThread();
		if (!isClientStart()  || out == null) {
			return -1;
		}
		SharePreferenceUtil util = new SharePreferenceUtil(getApplicationContext(),
				Constants.SAVE_USER);
		if (contString.length() > 0) {
			if (out != null) {
				TranObject<TextMessage> o = new TranObject<TextMessage>(
						TranObjectType.MESSAGE);
				TextMessage message = new TextMessage();
				message.setMessage(contString);
				message.set_is_pic(is_pic);
				o.setObject(message);
				o.setFromUser(Integer.parseInt(util.getId()));
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
