package com.kids.activity.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.kids.activity.imagescan.MultiSelImageActivity;
import com.kids.client.Client;
import com.kids.client.ClientOutputThread;
import com.kids.client.Client.ClientThread;
import com.kids.util.ImageProcess;
import com.kids.util.MessageDB;
import com.kids.util.MyDate;
import com.kids.util.SharePreferenceUtil;
import com.kids.util.ZoomImageView;
import com.way.chat.activity.R;
import com.way.chat.common.bean.CommonMsg;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.chat.common.util.Constants;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
		messageDB = application.getMessageDB();
		user = (User) getIntent().getSerializableExtra("user");
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);
		Client client = application.getClient();
		out = client.getClientOutputThread();
		initView();// 初始化view
		initData();// 初始化数据
		alreadycreate = false;
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					Thread.sleep(10000);// we need to wait 10s for
										// friendActivity not receive the
										// message,
										// that will cause duplicate message
										// save in DB
										// maybe other good solution
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (alreadycreate && out != null)
					getOffLineMess();
			}
		};
		new Thread(runnable).start();
	}

	@Override
	public void getPicUpdate(HandleMsg hm) {
		if (hm.mComefromUid == user.getId()) {
			for (int i = 0; i < mDataArrays.size(); i++) {
				ChatMsgEntity cme = mDataArrays.get(i);
				if (cme.getMessage().equals(hm.mUrl)) {
					cme.setPicPath(hm.mSavePath);
					mDataArrays.set(i, cme);
				}
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onResume() {// 如果从后台恢复，服务被系统干掉，就重启一下服务
		alreadycreate = true;
		super.onResume();
		if (application.needRefresh(user.getId() + ""))
			refreshData();

	}

	public void getOffLineMess() {
		/**/
		CommonMsg cm = new CommonMsg();
		cm.setarg1(user.getId() + "");
		cm.setarg2(util.getId());
		cm.setarg3(user.getIsCrowd() + "");
		TranObject<CommonMsg> msg2Object = new TranObject<CommonMsg>(
				TranObjectType.OFFLINEMESS);
		msg2Object.setObject(cm);
		out.setMsg(msg2Object);
		if (application.getOffLineList() != null) {
			for (String s : application.getOffLineList())

			{
				if (s.equals(util.getId())) {
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
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ImageView image = (ImageView) arg1
						.findViewById(R.id.imageView_chat_pic);
				String path = image.getContentDescription().toString();
				if (path != null && !path.equals("")) {
					System.out.println("path path   pppp" + path);
					String prefix = path.substring(path.lastIndexOf("."));
					if (prefix.equals(".mp4")) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse(path), "video/mp4");
						startActivity(intent);
					} else {
						Bitmap bitmap = ImageProcess.GetBitmapByPath(
								ChatActivity.this, path,
								MyApplication.mWindowHeight,
								MyApplication.mWindowWidth, 1);
						if (bitmap != null) {
							int degree = ImageProcess.getBitmapDegree(path);
							if (degree != 0)
								bitmap = ImageProcess.rotateBitmapByDegree(
										bitmap, degree);

							ZoomImageView zoom = new ZoomImageView(
									ChatActivity.this, bitmap);
							zoom.showZoomView();
						}
					}
				}

			}
		});
		mBtnSend = (Button) findViewById(R.id.chat_send);
		mBtnSend.setOnClickListener(this);
		mBtnBack = (Button) findViewById(R.id.chat_back);
		mBtnBack.setOnClickListener(this);
		mBtnSendPic = (Button) findViewById(R.id.pic_send);
		mBtnSendPic.setOnClickListener(this);
		mFriendName = (TextView) findViewById(R.id.chat_name);
		mFriendName.setText(user.getName());
		mEditTextContent = (EditText) findViewById(R.id.chat_editmessage);
	}

	/**
	 * 加载还没有显示的消息，从数据库中读出
	 */
	public void refreshData() {
		ChatMsgEntity cme = null;
		String datestr = "";
		if (mDataArrays.size() > 0) {
			cme = mDataArrays.get(mDataArrays.size() - 1);
			datestr = cme.getDate();
		}
		List<ChatMsgEntity> list = messageDB.getMsg(user.getId(), datestr, 10);
		List<ChatMsgEntity> mDataArrays_tmp = new ArrayList<ChatMsgEntity>();
		System.out.println("reflesh date " + list.size());
		if (list.size() > 0) {
			for (ChatMsgEntity entity : list) {
				if (entity.getName().equals("")) {
					entity.setName(user.getName());
				}
				if (entity.getImg() < 0) {
					entity.setImg(user.getImg());
				}
				mDataArrays_tmp.add(entity);
			}
			Collections.reverse(mDataArrays_tmp);
			mDataArrays.addAll(mDataArrays_tmp);

			mListView.setSelection(mAdapter.getCount() - 1);
			mAdapter.notifyDataSetChanged();
			mListView.post(new Runnable() {
				@Override
				public void run() {

					mListView.setSelection(mAdapter.getCount() - 1);

				}

			});
		}
	}

	/**
	 * 加载消息历史，从数据库中读出
	 */
	public void initData() {
		List<ChatMsgEntity> list = messageDB.getMsg(user.getId(), "", 10);
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
		mAdapter = new ChatMsgViewAdapter(this, mDataArrays, user);
		mListView.setAdapter(mAdapter);
		mListView.setSelection(mAdapter.getCount() - 1);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chat_send:// 发送按钮点击事件
			String contString = mEditTextContent.getText().toString();
			send(contString, false, "");
			mEditTextContent.setText("");// 清空编辑框数据
			break;
		case R.id.pic_send:// 发送按钮点击事件
			/*
			 * Intent intent = new Intent(ChatActivity.this,
			 * CameraProActivity.class); startActivityForResult(intent, 1);
			 */
			new AlertDialog.Builder(ChatActivity.this)
					.setTitle("发送")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setItems(new String[] { "相机拍照/视频", "本地相册", "请假事件" },
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = null;
									switch (which) {
									case 0:
										intent = new Intent(ChatActivity.this,
												CameraProActivity.class);
										intent.putExtra("user", user);
										startActivityForResult(intent, 1);

										break;
									case 1:
										intent = new Intent(ChatActivity.this,
												MultiSelImageActivity.class);
										intent.putExtra("user", user);
										startActivityForResult(intent, 1);
										break;
									case 3:
										break;
									}
									dialog.cancel();
								}
							}).setNegativeButton("取消", null).show();
			break;
		case R.id.chat_back:// 返回按钮点击事件
			finish();// 结束,实际开发中，可以返回主界面
			break;
		}
	}

	public void msgsendok(String msgs, String id) {
		for (ChatMsgEntity cme : mDataArrays) {
			if (cme.getMessage().equals(msgs) && id.equals(user.getId() + "")) {
				cme.setSendSta(1);
				mAdapter.notifyDataSetChanged();
				// break;
			}
		}

	}

	/**
	 * 发送消息
	 */
	private void send(String contString, boolean is_pic, String pic_path_local) {

		if (contString.length() > 0) {
			application.send(contString, is_pic, pic_path_local, user);
			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setName(util.getName());
			entity.setDate(MyDate.getDateEN());
			entity.set_is_pic(is_pic);
			entity.setMessage(contString);
			entity.setImg(util.getImg());
			entity.setMsgType(false);
			entity.setPicPath(pic_path_local);

			mDataArrays.add(entity);
			mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变

			mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项

		}
	}

	@Override
	public void receiveMsg(TranObject msg) {
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case MESSAGE:
			TextMessage tm = (TextMessage) msg.getObject();
			application.removeNeedRefresh(user.getId() + "");
			Receive_message(msg, tm.get_is_pic(), "");
			break;
		default:
			break;
		}
	}

	private void Receive_message(TranObject msg, boolean ispic, String path_pic) {
		TextMessage tm = (TextMessage) msg.getObject();
		String message = tm.getMessage();
		ChatMsgEntity entity = new ChatMsgEntity(user.getName(),
				MyDate.getDateEN(), message, user.getImg(), true, ispic,
				path_pic);// 收到的消息
		System.out.println("herris ====>" + msg.getFromUser());
		System.out.println("herris ====>" + user.getId());
		if (msg.getFromUser() == user.getId() || msg.getFromUser() == 0
				|| msg.getCrowd() == user.getId()) {// 如果是正在聊天的好友的消息，或者是服务器的消息
			if (msg.getCrowd() == user.getId()) {
				entity.setName(msg.getFromUserName());
				entity.setImg(msg.getFromImg());
			}

			mDataArrays.add(entity);
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(mListView.getCount() - 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// requestCode用于区分业务
		// resultCode用于区分某种业务的执行情况
		if (1 == requestCode && RESULT_OK == resultCode) {
			ArrayList<String> stringList = (ArrayList<String>) data
					.getStringArrayListExtra("pic_path");
			ArrayList<String> stringList_local = (ArrayList<String>) data
					.getStringArrayListExtra("pic_local_path");
			if (stringList.size() != stringList_local.size()) {
				Toast.makeText(
						ChatActivity.this,
						"Error upload path" + stringList.size() + "   "
								+ stringList_local.size(), 0).show();
			}
			for (int i = 0; i < stringList.size(); i++) {
				send(stringList.get(i), true, stringList_local.get(i));
			}
		} else {

		}
	}
}