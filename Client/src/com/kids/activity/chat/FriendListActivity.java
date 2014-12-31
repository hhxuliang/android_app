package com.kids.activity.chat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.kids.activity.imagescan.GroupAdapter;
import com.kids.activity.imagescan.ImageBean;
import com.kids.activity.imagescan.ShowImageActivity;
import com.kids.client.ClientInputThread;
import com.kids.client.ClientOutputThread;
import com.kids.client.MessageListener;
import com.kids.util.GroupFriend;
import com.kids.util.ImageProcess;
import com.kids.util.MessageDB;
import com.way.chat.common.util.MyDate;
import com.kids.util.SharePreferenceUtil;
import com.kids.util.UserDB;
import com.way.chat.activity.R;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.chat.common.util.Constants;

/**
 * 好友列表的Activity
 * 
 * @author way
 * 
 */
public class FriendListActivity extends MyActivity implements OnClickListener {

	private static final int PAGE1 = 0;// 页面1
	private static final int PAGE2 = 1;// 页面2
	private static final int PAGE3 = 2;// 页面3
	private List<GroupFriend> group;// 需要传递给适配器的数据
	private SharePreferenceUtil util;
	private UserDB userDB;// 保存好友列表数据库对象
	private MessageDB messageDB;// 消息数据库对象
	private MyListView myListView;// 好友列表自定义listView
	private MyExAdapter myExAdapter;// 好
	private ListView mRecentListView;// 最近会话的listView
	private int newNum = 0;
	private ViewPager mPager;
	private List<View> mListViews;// Tab页面
	private LinearLayout layout_body_activity;
	private ImageView img_recent_chat;// 最近会话
	private ImageView img_friend_list;// 好友列表
	private ImageView img_group_friend;// 群组
	private ImageView myHeadImage;// 头像
	private TextView myName;// 名字
	private ImageView cursor;// 标题背景图片
	private int currentIndex = PAGE2; // 默认选中第2个，可以动态的改变此参数值
	private int offset = 0;// 动画图片偏移量
	private int bmpW;// 动画图片宽度
	private List<User> list;
	private MenuInflater mi;// 菜单
	private MyApplication application;
	private int[] imgs = { R.drawable.icon, R.drawable.f1, R.drawable.f2,
			R.drawable.f3, R.drawable.f4, R.drawable.f5, R.drawable.f6,
			R.drawable.f7, R.drawable.f8, R.drawable.f9 };// 头像资源
	private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();
	private List<ImageBean> list_img = new ArrayList<ImageBean>();
	private final static int SCAN_OK = 1;
	private ProgressDialog mProgressDialog;
	private GroupAdapter adapter;
	private GridView mGroupGridView;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_OK:
				// 关闭进度条
				mProgressDialog.dismiss();

				adapter = new GroupAdapter(FriendListActivity.this,
						list_img = subGroupOfImage(mGruopMap), mGroupGridView);
				mGroupGridView.setAdapter(adapter);
				break;
			}
		}

	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.friend_list);
		application = (MyApplication) this.getApplicationContext();
		Button back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		initData();// 初始化数据
		initImageView();// 初始化动画
		initUI();// 初始化界面

	}

	@Override
	protected void onResume() {// 如果从后台恢复，服务被系统干掉，就重启一下服务
		// TODO Auto-generated method stub
		super.onResume();
		newNum = application.getRecentNum();// 从新获取一下全局变量

		application.getmRecentAdapter().notifyDataSetChanged();
		myExAdapter.notifyDataSetChanged();
		if (mGroupGridView != null && mGroupGridView.getAdapter() != null){
			((GroupAdapter) mGroupGridView.getAdapter()).resetList();			
		}

		getImages();
	}

	/**
	 * 初始化系统数据
	 */
	private void initData() {
		userDB = application.getUserDB();// 本地用户数据库
		messageDB = application.getMessageDB();// 本地消息数据库
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);

		if (list != null && list.size() > 0)
			list.clear();
		list = userDB.getUser("");
		initListViewData(list);
	}

	/**
	 * 处理服务器传递过来的用户数组数据，
	 * 
	 * @param list
	 *            从服务器获取的用户数组
	 */
	private void initListViewData(List<User> list) {
		group = new ArrayList<GroupFriend>();// 实例化

		GroupFriend g_info = null;
		for (User u : list) {
			boolean findG = false;
			for (GroupFriend g : group) {
				if (u.getGroup().equals(g.getGroupName())) {
					g_info = g;
					findG = true;
					break;
				}
			}
			if (findG == false) {
				List<User> child = new ArrayList<User>();// 装小组成员的list
				child.add(u);
				GroupFriend groupInfo = new GroupFriend(u.getGroup(), child);// 我们自定义的大组成员对象
				group.add(groupInfo);// 把自定义大组成员对象放入一个list中，传递给适配器

			} else {
				if (g_info != null)
					g_info.getGroupChild().add(u);
			}

		}

	}

	/**
	 * 增量添加好友数据，
	 * 
	 * @param list
	 * 
	 */
	private void updateListViewData(List<User> list) {
		initListViewData(list);
	}

	/**
	 * 初始化动画
	 */
	private void initImageView() {
		cursor = (ImageView) findViewById(R.id.tab2_bg);
		bmpW = BitmapFactory.decodeResource(getResources(),
				R.drawable.topbar_select).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		// System.out.println("屏幕宽度:" + screenW);
		offset = (screenW / 3 - bmpW) / 2;// 计算偏移量:屏幕宽度/3，平分为3分，如果是3个view的话，再减去图片宽度，因为图片居中，所以要得到两变剩下的空隙需要再除以2
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset * 3 + bmpW, 0);// 初始化位置
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}

	private void initUI() {
		mi = new MenuInflater(this);
		layout_body_activity = (LinearLayout) findViewById(R.id.bodylayout);

		img_recent_chat = (ImageView) findViewById(R.id.tab1);
		img_recent_chat.setOnClickListener(this);
		img_friend_list = (ImageView) findViewById(R.id.tab2);
		img_friend_list.setOnClickListener(this);
		img_group_friend = (ImageView) findViewById(R.id.tab3);
		img_group_friend.setOnClickListener(this);

		myHeadImage = (ImageView) findViewById(R.id.friend_list_myImg);
		myName = (TextView) findViewById(R.id.friend_list_myName);

		cursor = (ImageView) findViewById(R.id.tab2_bg);

		myHeadImage.setImageResource(imgs[list.get(0).getImg()]);
		myName.setText(list.get(0).getName());
		layout_body_activity.setFocusable(true);

		mPager = (ViewPager) findViewById(R.id.viewPager);
		mListViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View lay1 = inflater.inflate(R.layout.tab1, null);
		View lay2 = inflater.inflate(R.layout.tab2, null);
		View lay3 = inflater.inflate(R.layout.tab3, null);
		mListViews.add(lay2);
		mListViews.add(lay1);
		mListViews.add(lay3);
		mPager.setAdapter(new MyPagerAdapter(mListViews));
		mPager.setCurrentItem(PAGE2);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());

		// 下面是最近会话界面处理
		mRecentListView = (ListView) lay1.findViewById(R.id.tab1_listView);
		// mRecentAdapter = new RecentChatAdapter(FriendListActivity.this,
		// application.getmRecentList());// 从全局变量中获取最近聊天对象数组
		
		mRecentListView.setAdapter(application.getmRecentAdapter());// 先设置空对象，要么从数据库中读出
		

		// 下面是处理好友列表界面处理
		myListView = (MyListView) lay2.findViewById(R.id.tab2_listView);
		myExAdapter = new MyExAdapter(this, group);
		myListView.setAdapter(myExAdapter);
		myListView.setGroupIndicator(null);// 不设置大组指示器图标，因为我们自定义设置了
		myListView.setDivider(null);// 设置图片可拉伸的
		myListView.setFocusable(true);// 聚焦才可以下拉刷新
		myListView.setonRefreshListener(new MyRefreshListener());

		mGroupGridView = (GridView) lay3.findViewById(R.id.main_grid);
		mGroupGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<String> childList = mGruopMap.get(list_img.get(position)
						.getFolderName());

				Intent mIntent = new Intent(FriendListActivity.this,
						ShowImageActivity.class);
				mIntent.putStringArrayListExtra("data",
						(ArrayList<String>) childList);
				mIntent.putExtra("fun", "DELETE");
				startActivity(mIntent);

			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tab1:
			mPager.setCurrentItem(PAGE1);// 点击页面1
			break;
		case R.id.tab2:
			mPager.setCurrentItem(PAGE2);// 点击页面1
			break;
		case R.id.tab3:
			mPager.setCurrentItem(PAGE3);// 点击页面1
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mi.inflate(R.menu.friend_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	// 菜单选项添加事件处理
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.friend_menu_add:
			// Toast.makeText(getApplicationContext(), "亲！此功能暂未实现哦", 0).show();
			goNewFriend();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// requestCode用于区分业务
		// resultCode用于区分某种业务的执行情况
		if (1 == requestCode && RESULT_OK == resultCode) {
			List<User> result = (List<User>) data
					.getSerializableExtra("newfriends");
			userDB.addUser(result);
			updateListViewData(result);
			myExAdapter.notifyDataSetChanged();

			// Toast.makeText(this.getBaseContext(), result,
			// Toast.LENGTH_SHORT).show();
		} else {
			// Toast.makeText(this.getBaseContext(), "无返回值",
			// Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 进入登陆界面
	 */
	private void goNewFriend() {
		Intent intent = new Intent();
		intent.setClass(this, Newfriends.class);
		startActivityForResult(intent, 1);
		// finish();
	}

	public void updateUserState() {
		
		
	}

	@Override
	public void receiveMsg(TranObject msg) {// 重写父类的方法，处理消息
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case MESSAGE:
			application.getmRecentAdapter().notifyDataSetChanged();
			myExAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {// 捕获返回按键事件，进入后台运行
		// TODO Auto-generated method stub
		// 发送广播，通知服务，已进入后台运行
		// Intent i = new Intent();
		// i.setAction(Constants.BACKKEY_ACTION);
		// sendBroadcast(i);

		// util.setIsStart(true);// 设置后台运行标志，正在运行
		finish();// 再结束自己
	}

	// ViewPager页面切换监听
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			Animation animation = null;
			switch (arg0) {
			case PAGE1:// 切换到页卡1
				if (currentIndex == PAGE2) {// 如果之前显示的是页卡2
					animation = new TranslateAnimation(0, -one, 0, 0);
				} else if (currentIndex == PAGE3) {// 如果之前显示的是页卡3
					animation = new TranslateAnimation(one, -one, 0, 0);
				}
				break;
			case PAGE2:// 切换到页卡2
				if (currentIndex == PAGE1) {// 如果之前显示的是页卡1
					animation = new TranslateAnimation(-one, 0, 0, 0);
				} else if (currentIndex == PAGE3) {// 如果之前显示的是页卡3
					animation = new TranslateAnimation(one, 0, 0, 0);
				}
				break;
			case PAGE3:// 切换到页卡3
				if (currentIndex == PAGE1) {// 如果之前显示的是页卡1
					animation = new TranslateAnimation(-one, one, 0, 0);
				} else if (currentIndex == PAGE2) {// 如果之前显示的是页卡2
					animation = new TranslateAnimation(0, one, 0, 0);
				}
				break;
			default:
				break;
			}
			currentIndex = arg0;// 动画结束后，改变当前图片位置
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			cursor.startAnimation(animation);
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * 好友列表下拉刷新监听与实现，异步任务
	 * 
	 * @author way
	 * 
	 */
	public class MyRefreshListener implements MyListView.OnRefreshListener {

		@Override
		public void onRefresh() {
			AsyncTask<Void, Void, Void> tk = new AsyncTask<Void, Void, Void>() {
				List<User> list;

				protected Void doInBackground(Void... params) {
					// 从服务器重新获取好友列表
					if (application.isClientStart()) {
						ClientOutputThread out = application.getClient()
								.getClientOutputThread();
						TranObject o = new TranObject(TranObjectType.REFRESH);
						o.setFromUser(Integer.parseInt(util.getId()));
						out.setMsg(o);
						// 为了及时收到服务器发过来的消息，我这里直接通过监听收消息线程，获取好友列表，就不通过接收广播了
						ClientInputThread in = application.getClient()
								.getClientInputThread();
						in.setMessageListener(new MessageListener() {

							@Override
							public void Message(TranObject msg) {
								// TODO Auto-generated method stub
								if (msg != null
										&& msg.getType() == TranObjectType.REFRESH) {
									list = (List<User>) msg.getObject();
									if (list.size() > 0) {
										// System.out.println("Friend:" + list);
										initListViewData(list);
										myExAdapter.updata(group);
										userDB.updateUser(list);// 保存到数据库
									}
								}
							}
						});
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					myExAdapter.notifyDataSetChanged();
					myListView.onRefreshComplete();
					Toast.makeText(FriendListActivity.this, "刷新成功", 0).show();
				}

			};
			tk.execute();
		}
	}

	// ////////////////////////////////////show pic

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
	 */
	private void getImages() {
		// 显示进度条
		mProgressDialog = ProgressDialog.show(this, null, "正在加载...");

		new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<String> childpath = new ArrayList<String>();
				childpath.add(application.getDownloadPicPath());
				childpath.add(application.getMyUploadPicPath());
				mGruopMap.clear();
				for (String parentN : childpath) {
					ArrayList<String> pathstr = ImageProcess.ListFile(parentN);

					File file = new File(parentN);
					//String parentName = file.getName();
					for (String path : pathstr) {
						// 根据父路径名将图片放入到mGruopMap中
						String parentName = "其他";
						int id=0;
						if (path.lastIndexOf("/")>0 && path.lastIndexOf("_kids_")>0)
						{	
							parentName = path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("_kids_"));
							System.out.println("usid is :" + parentName);					
							try{
								id = Integer.parseInt(parentName);
								UserDB userDB = application.getUserDB();
								User user2 = userDB.selectInfo(id);// 通过id查询对应数据库该好友信息
								parentName = user2.getName();
							}catch(Exception e){
								parentName = "其他";
							}
						}
						if (!mGruopMap.containsKey(parentName)) {
							List<String> chileList = new ArrayList<String>();
							chileList.add(path);
							mGruopMap.put(parentName, chileList);
						} else {
							mGruopMap.get(parentName).add(path);
						}
					}
				}
				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(SCAN_OK);

			}
		}).start();

	}

	/**
	 * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中 所以需要遍历HashMap将数据组装成List
	 * 
	 * @param mGruopMap
	 * @return
	 */
	private List<ImageBean> subGroupOfImage(
			HashMap<String, List<String>> mGruopMap) {
		List<ImageBean> list = new ArrayList<ImageBean>();

		Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();
			ImageBean mImageBean = new ImageBean();
			String key = entry.getKey();
			List<String> value = entry.getValue();

			mImageBean.setFolderName(key);
			mImageBean.setImageCounts(value.size());
			mImageBean.setTopImagePath(value.get(0));// 获取该组的第一张图片

			list.add(mImageBean);
		}

		return list;

	}
}
