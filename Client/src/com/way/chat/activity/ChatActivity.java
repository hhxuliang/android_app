package com.way.chat.activity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.way.chat.common.bean.CommonMsg;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.chat.common.util.Constants;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.client.Client.ClientThread;
import com.way.util.MessageDB;
import com.way.util.MyDate;
import com.way.util.SharePreferenceUtil;


import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 聊天Activity
 * 
 * @author way
 */
public class ChatActivity extends MyActivity implements OnClickListener {
	private Button mBtnSend;// 发送btn
	private Button mBtnBack;// 返回btn
	private Button mBtnSendPic;// 返回btn
	private EditText mEditTextContent;
	private TextView mFriendName;
	private ListView mListView;
	private ChatMsgViewAdapter mAdapter;// 消息视图的Adapter
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组
	private SharePreferenceUtil util;
	private User user;
	private MessageDB messageDB;
	private MyApplication application;
	private ClientOutputThread out;
	private boolean alreadycreate;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.chat);
		application = (MyApplication) getApplicationContext();
		messageDB = new MessageDB(this);
		user = (User) getIntent().getSerializableExtra("user");
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);
		Client client = application.getClient();
		out = client.getClientOutputThread();
		initView();// 初始化view
		initData();// 初始化数据
		alreadycreate=false;
		Runnable runnable = new Runnable(){
			public void run(){
				try {
					Thread.sleep(10000);//we need to wait 10s for friendActivity not receive the message, 
										//that will cause duplicate message save in DB
										//maybe other good solution
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (alreadycreate)
					getOffLineMess();		
			}
		};
		new Thread(runnable).start();
	}
	@Override
	protected void onResume() {// 如果从后台恢复，服务被系统干掉，就重启一下服务
		alreadycreate = true;
		super.onResume();
	}
	public void getOffLineMess() {
		/**/
		CommonMsg cm = new CommonMsg();
		cm.setarg1(user.getId()+"");
		cm.setarg2(util.getId());
		cm.setarg3(user.getIsCrowd()+"");
		TranObject<CommonMsg> msg2Object = new TranObject<CommonMsg>(
				TranObjectType.OFFLINEMESS);
		msg2Object.setObject(cm);
		out.setMsg(msg2Object);
		if(application.getOffLineList()!=null)
		{
			for(String s:application.getOffLineList())
		
			{
				if (s.equals(util.getId())){
					application.getOffLineList().remove(s);
					break;
				}
			}
		}
		
	}
	/**
	 * 初始化view
	 */
	public void initView() {
		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.chat_send);
		mBtnSend.setOnClickListener(this);
		mBtnBack = (Button) findViewById(R.id.chat_back);
		mBtnBack.setOnClickListener(this);
		mBtnSendPic = (Button) findViewById(R.id.pic_send);
		mBtnSendPic.setOnClickListener(this);
		mFriendName = (TextView) findViewById(R.id.chat_name);
		mFriendName.setText(util.getName());
		mEditTextContent = (EditText) findViewById(R.id.chat_editmessage);
	}

	/**
	 * 加载消息历史，从数据库中读出
	 */
	public void initData() {
		List<ChatMsgEntity> list = messageDB.getMsg(user.getId());
		if (list.size() > 0) {
			for (ChatMsgEntity entity : list) {
				if (entity.getName().equals("")) {
					entity.setName(user.getName());
				}
				if (entity.getImg() < 0) {
					entity.setImg(user.getImg());
				}
				mDataArrays.add(entity);
			}
			Collections.reverse(mDataArrays);
		}
		mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);
		mListView.setSelection(mAdapter.getCount() - 1);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		messageDB.close();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chat_send:// 发送按钮点击事件
			String contString = mEditTextContent.getText().toString();
			send(contString);
			mEditTextContent.setText("");// 清空编辑框数据
			break;
		case R.id.pic_send:// 发送按钮点击事件
			 Intent intent = new Intent(ChatActivity.this, CameraProActivity.class);  
             startActivityForResult(intent,1);
             //finish();  
			//send_pic();
			break;
		case R.id.chat_back:// 返回按钮点击事件
			finish();// 结束,实际开发中，可以返回主界面
			break;
		}
	}

	/**
	 * 发送消息
	 */
	private void send(String contString) {
		
		if (contString.length() > 0) {
			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setName(util.getName());
			entity.setDate(MyDate.getDateEN());
			entity.setMessage(contString);
			entity.setImg(util.getImg());
			entity.setMsgType(false);

			messageDB.saveMsg(user.getId(), entity);

			mDataArrays.add(entity);
			mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变
			
			mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项
			
			if (out != null) {
				TranObject<TextMessage> o = new TranObject<TextMessage>(
						TranObjectType.MESSAGE);
				TextMessage message = new TextMessage();
				message.setMessage(contString);
				o.setObject(message);
				o.setFromUser(Integer.parseInt(util.getId()));
				o.setToUser(user.getId());
				if(user.getIsCrowd()==1)
					o.setCrowd(user.getId());
				else
					o.setCrowd(0);
				out.setMsg(o);
			}
			// 下面是添加到最近会话列表的处理，在按发送键之后
			RecentChatEntity entity1 = new RecentChatEntity(user.getId(),
					user.getImg(), 0, user.getName(), MyDate.getDate(),
					contString);
			application.getmRecentList().remove(entity1);
			application.getmRecentList().addFirst(entity1);
			application.getmRecentAdapter().notifyDataSetChanged();
		}
	}

	@Override
	public void getMessage(TranObject msg) {
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case MESSAGE:
			TextMessage tm = (TextMessage) msg.getObject();
			String message = tm.getMessage();
			ChatMsgEntity entity = new ChatMsgEntity(user.getName(),
					MyDate.getDateEN(), message, user.getImg(), true);// 收到的消息
			System.out.println("herris ====>"+msg.getFromUser());
			System.out.println("herris ====>"+user.getId());
			if (msg.getFromUser() == user.getId() || msg.getFromUser() == 0 || msg.getCrowd() == user.getId()) {// 如果是正在聊天的好友的消息，或者是服务器的消息
				if(msg.getCrowd() == user.getId())
				{
					entity.setName(msg.getFromUserName());
					entity.setImg(msg.getFromImg());
				}
				messageDB.saveMsg(user.getId(), entity);

				mDataArrays.add(entity);
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(mListView.getCount() - 1);
				MediaPlayer.create(this, R.raw.msg).start();
			} else {
				messageDB.saveMsg(msg.getFromUser(), entity);// 保存到数据库
				Toast.makeText(ChatActivity.this,
						"您有新的消息来自：" + msg.getFromUser() + ":" + message, 0)
						.show();// 其他好友的消息，就先提示，并保存到数据库
				MediaPlayer.create(this, R.raw.msg).start();
			}
			break;
		case LOGIN:
			User loginUser = (User) msg.getObject();
			Toast.makeText(ChatActivity.this, loginUser.getId() + "上线了", 0)
					.show();
			MediaPlayer.create(this, R.raw.msg).start();
			break;
		case LOGOUT:
			User logoutUser = (User) msg.getObject();
			Toast.makeText(ChatActivity.this, logoutUser.getId() + "下线了", 0)
					.show();
			MediaPlayer.create(this, R.raw.msg).start();
			break;
		default:
			break;
		}
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{ 
		// requestCode用于区分业务  
		// resultCode用于区分某种业务的执行情况  
		if (1 == requestCode && RESULT_OK == resultCode) 
		{ 
			String result = data.getStringExtra("pic_path"); 
			send(result);
			
		} 
		else 
		{ 

		} 
	} 
}