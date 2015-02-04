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
import java.util.Map;

import com.kids.util.SoundMeter;
import com.kids.activity.imagescan.MultiSelImageActivity;
import com.kids.client.Client;
import com.kids.client.ClientInputThread;
import com.kids.client.ClientOutputThread;
import com.kids.client.MessageListener;
import com.kids.client.Client.ClientThread;
import com.kids.util.ImageProcess;
import com.kids.util.MessageDB;
import com.kids.util.UploadUtil;
import com.kids.util.UploadUtil.OnUploadProcessListener;
import com.way.chat.common.util.MyDate;
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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 聊天Activity
 * 
 * @author way
 */
public class ChatActivity extends MyActivity implements OnClickListener,OnUploadProcessListener {
	// 去上传文件
	protected static final int TO_UPLOAD_FILE = 1;
	// 上传文件响应
	protected static final int UPLOAD_FILE_DONE = 2;
	// 选择文件
	public static final int TO_SELECT_PHOTO = 3;
	// 上传初始化
	private static final int UPLOAD_INIT_PROCESS = 4;
	// 上传中
	private static final int UPLOAD_IN_PROCESS = 5;
	private Button mBtnSend;// 发送btn
	private Button mBtnBack;// 返回btn
	private Button mBtnSendPic;// 返回btn
	private EditText mEditTextContent;
	private TextView mFriendName;
	private MyNormalListView mListView;
	private ChatMsgViewAdapter mAdapter;// 消息视图的Adapter
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组
	private ArrayList<GridView> grids;
	private TextView mBtnRcd;
	private User user;
	private MessageDB messageDB;
	private MyApplication application;
	private boolean alreadycreate;
	private Client client;
	private Bitmap bitmap_zoom=null;
	private boolean btn_vocie = false;
	private Handler mHandler = new Handler();
	private SoundMeter mSensor;
	private View rcChat_popup;
	private boolean isShosrt = false;
	private int flag = 1;
	private ImageView img1, sc_img1,emotion;
	private LinearLayout del_re;
	private String voiceName;
	private LinearLayout voice_rcd_hint_loading, voice_rcd_hint_rcding,
	voice_rcd_hint_tooshort;
	private long startVoiceT, endVoiceT;
	private ImageView chatting_mode_btn, volume;
	private EditText progressText;
	private ProgressBar progressBar;
	private ProgressDialog progressDialog;
	private GridView gView1;
	private GridView gView2;
	private GridView gView3;
	private int[] expressionImages;
	private String[] expressionImageNames;
	private int[] expressionImages1;
	private String[] expressionImageNames1;
	private int[] expressionImages2;
	private String[] expressionImageNames2;
	private ViewPager viewPager;
	private ImageView page0;
	private ImageView page1;
	private ImageView page2;
	private LinearLayout page_select;
	private ImageView biaoqingBtn;
	private ImageView biaoqingfocuseBtn;
	private Handler handler_send_file = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int next = 0;
			switch (msg.what) {
			case TO_UPLOAD_FILE:
				toUploadFile();
				break;
			case UPLOAD_INIT_PROCESS:
				//progressBar.setMax(msg.arg1);
				break;
			case UPLOAD_IN_PROCESS:
				//progressBar.setProgress(msg.arg1);
				break;
			case UPLOAD_FILE_DONE:
				if (msg.arg1 == UploadUtil.UPLOAD_SUCCESS_CODE) {

					next = onUploadOK(true, msg.obj + "");

				} else {
					next = onUploadOK(false, "");
				}

				break;
			default:
				break;
			}
			super.handleMessage(msg);
			
		}

	};
	public void onUploadDone(int responseCode, String message) {
		progressDialog.dismiss();
		//progressBar.setVisibility(View.GONE);
		//progressText.setVisibility(View.GONE);
		Message msg = Message.obtain();
		msg.what = UPLOAD_FILE_DONE;
		msg.arg1 = responseCode;
		msg.obj = message;
		handler_send_file.sendMessage(msg);
	}

	public int onUploadOK(boolean statu, String url_path) {
		if (statu) {
			ChatMsgEntity entity = application.send(url_path, 2, application.getSendVoicePath()+"/"
					+ voiceName, user);
			if (entity != null) {
				mDataArrays.add(entity);
				mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变

				mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项
			}
			
		} else {
			Toast.makeText(getApplicationContext(),
					"失败上传音频失败", 0)
					.show();
		}
		return 0;
	}

	public void onUploadProcess(int uploadSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_IN_PROCESS;
		msg.arg1 = uploadSize;
		handler_send_file.sendMessage(msg);
	}

	public void initUpload(int fileSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_INIT_PROCESS;
		msg.arg1 = fileSize;
		handler_send_file.sendMessage(msg);
	}
	private void toUploadFile() {
		//progressBar.setVisibility(View.VISIBLE);
		//progressText.setVisibility(View.VISIBLE);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage("正在上传文件,请等待...");
		/*
		 * uploadImageResult.setText("正在上传中...");
		 * progressDialog.setMessage("正在请求服务器上传...");
		 * progressDialog.setTitle("信息");
		 * progressDialog.setIcon(drawable.ic_dialog_info);
		 * progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		 * progressDialog.setProgress(59);
		 * progressDialog.setIndeterminate(true);
		 */
		/*
		 * progressDialog.setButton("确认", new DialogInterface.OnClickListener(){
		 * public void onClick(DialogInterface dialog, int which) {
		 * dialog.cancel(); } });
		 */

		progressDialog.show();
		String fileKey = "amr";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		
		uploadUtil.setOnUploadProcessListener(this); // 设置监听器监听上传状态

		Map<String, String> params = new HashMap<String, String>();
		params.put("orderId", "111");
		String picstr = application.getSendVoicePath()+"/"
				+ voiceName;
		
		fileKey = picstr.substring(picstr.lastIndexOf("."));
		uploadUtil.uploadFile(picstr, fileKey, Constants.FILE_UPLOAD_URL, params);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.chat);
		application = (MyApplication) getApplicationContext();
		messageDB = application.getMessageDB();
		user = (User) getIntent().getSerializableExtra("user");
		
		client = application.getClient();
		initView();// 初始化view
		initData(20);// 初始化数据
		mAdapter = new ChatMsgViewAdapter(this, mDataArrays, user,mListView);
		mListView.setAdapter(mAdapter);
		mListView.setSelection(mAdapter.getCount() - 1);
		initViewPager();
		alreadycreate = false;

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
		application.getNotReadmsslist().remove(user.getId() + "");
		messageDB.updateReadsta(user.getId());
		mAdapter.notifyDataSetInvalidated();
	}

	/**
	 * 初始化view
	 */
	public void initView() {
		mListView = (MyNormalListView) findViewById(R.id.listview);
		mListView.setonRefreshListener(new MyRefreshListener());
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ImageView image = (ImageView) arg1
						.findViewById(R.id.imageView_chat_pic);
				String path = image.getContentDescription().toString();
				if (path != null && !path.equals("")) {
					Toast.makeText(
							ChatActivity.this,
							path, 0).show();
					String prefix = path.substring(path.lastIndexOf("."));
					if(prefix.equals(".amr")){
						//playMusic(path) ;
					}
					else if (prefix.equals(".mp4")) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse(path), "video/mp4");
						startActivity(intent);
					} else {
//						if(bitmap_zoom!=null && !bitmap_zoom.isRecycled()){
//							bitmap_zoom.recycle();
//							bitmap_zoom=null;
//						}
//						System.gc();
						bitmap_zoom = ImageProcess.GetBitmapByPath(
								ChatActivity.this, path,
								MyApplication.mWindowHeight,
								MyApplication.mWindowWidth, 0.8);
						if (bitmap_zoom != null) {
							int degree = ImageProcess.getBitmapDegree(path);
							if (degree != 0)
								bitmap_zoom = ImageProcess.rotateBitmapByDegree(
										bitmap_zoom, degree);

							ZoomImageView zoom = new ZoomImageView(
									ChatActivity.this, bitmap_zoom);
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
		chatting_mode_btn = (ImageView) this.findViewById(R.id.ivPopUp);
		mBtnRcd = (TextView) findViewById(R.id.btn_rcd);
		chatting_mode_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (btn_vocie) {
					mBtnRcd.setVisibility(View.GONE);
					mEditTextContent.setVisibility(View.VISIBLE);
					mBtnSend.setVisibility(View.VISIBLE);
					mBtnSendPic.setVisibility(View.VISIBLE);
					biaoqingBtn.setVisibility(View.VISIBLE);
					btn_vocie = false;
					chatting_mode_btn
							.setImageResource(R.drawable.chatting_setmode_msg_btn);

				} else {
					mBtnRcd.setVisibility(View.VISIBLE);
					mEditTextContent.setVisibility(View.GONE);
					mBtnSend.setVisibility(View.GONE);
					mBtnSendPic.setVisibility(View.GONE);
					biaoqingBtn.setVisibility(View.GONE);
					chatting_mode_btn
							.setImageResource(R.drawable.chatting_setmode_voice_btn);
					btn_vocie = true;
					
				}
			}
		});
		mBtnRcd.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				//��������¼�ư�ťʱ����falseִ�и���OnTouch
				return false;
			}
		});
		mSensor = new SoundMeter();
		voice_rcd_hint_rcding = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_rcding);
		voice_rcd_hint_loading = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_loading);
		voice_rcd_hint_tooshort = (LinearLayout) this
				.findViewById(R.id.voice_rcd_hint_tooshort);
		rcChat_popup = this.findViewById(R.id.rcChat_popup);
		del_re = (LinearLayout) this.findViewById(R.id.del_re);
		img1 = (ImageView) this.findViewById(R.id.img1);
		sc_img1 = (ImageView) this.findViewById(R.id.sc_img1);
		volume = (ImageView) this.findViewById(R.id.volume);
		
		progressDialog = new ProgressDialog(this);
		
		expressionImages = Expressions.expressionImgs;
		expressionImageNames = Expressions.expressionImgNames;
		expressionImages1 = Expressions.expressionImgs1;
		expressionImageNames1 = Expressions.expressionImgNames1;
		expressionImages2 = Expressions.expressionImgs2;
		expressionImageNames2 = Expressions.expressionImgNames2;
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		page_select = (LinearLayout) findViewById(R.id.page_select);
		page0 = (ImageView) findViewById(R.id.page0_select);
		page1 = (ImageView) findViewById(R.id.page1_select);
		page2 = (ImageView) findViewById(R.id.page2_select);
		biaoqingBtn = (ImageView) findViewById(R.id.chatting_biaoqing_btn);
		biaoqingBtn.setOnClickListener(this);
		biaoqingfocuseBtn = (ImageView) findViewById(R.id.chatting_biaoqing_focuse_btn);
		biaoqingfocuseBtn.setOnClickListener(this);

	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!Environment.getExternalStorageDirectory().exists()) {
			Toast.makeText(this, "No SDCard", Toast.LENGTH_LONG).show();
			return false;
		}

		if (btn_vocie) {
			System.out.println("1");
			int[] location = new int[2];
			mBtnRcd.getLocationInWindow(location); // ��ȡ�ڵ�ǰ�����ڵľ�����
			int btn_rc_Y = location[1];
			int btn_rc_X = location[0];
			int[] del_location = new int[2];
			del_re.getLocationInWindow(del_location);
			int del_Y = del_location[1];
			int del_x = del_location[0];
			if (event.getAction() == MotionEvent.ACTION_DOWN && flag == 1) {
				if (!Environment.getExternalStorageDirectory().exists()) {
					Toast.makeText(this, "No SDCard", Toast.LENGTH_LONG).show();
					return false;
				}
				System.out.println("2");
				if (event.getY() > btn_rc_Y && event.getX() > btn_rc_X) {//�ж����ư��µ�λ���Ƿ�������¼�ư�ť�ķ�Χ��
					System.out.println("3");
					mBtnRcd.setBackgroundResource(R.drawable.voice_rcd_btn_pressed);
					rcChat_popup.setVisibility(View.VISIBLE);
					voice_rcd_hint_loading.setVisibility(View.VISIBLE);
					voice_rcd_hint_rcding.setVisibility(View.GONE);
					voice_rcd_hint_tooshort.setVisibility(View.GONE);
					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (!isShosrt) {
								voice_rcd_hint_loading.setVisibility(View.GONE);
								voice_rcd_hint_rcding
										.setVisibility(View.VISIBLE);
							}
						}
					}, 300);
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);
					startVoiceT = System.currentTimeMillis();
					voiceName = startVoiceT + ".amr";
					start(application.getSendVoicePath()+"/"
							+ voiceName);
					flag = 2;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP && flag == 2) {//�ɿ�����ʱִ��¼�����
				System.out.println("4");
				mBtnRcd.setBackgroundResource(R.drawable.voice_rcd_btn_nor);
				if (event.getY() >= del_Y
						&& event.getY() <= del_Y + del_re.getHeight()
						&& event.getX() >= del_x
						&& event.getX() <= del_x + del_re.getWidth()) {
					rcChat_popup.setVisibility(View.GONE);
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);
					stop();
					flag = 1;
					File file = new File(application.getSendVoicePath()+"/"
									+ voiceName);
					if (file.exists()) {
						file.delete();
					}
				} else {

					voice_rcd_hint_rcding.setVisibility(View.GONE);
					stop();
					endVoiceT = System.currentTimeMillis();
					flag = 1;
					int time = (int) ((endVoiceT - startVoiceT) / 1000);
					if (time < 1) {
						isShosrt = true;
						voice_rcd_hint_loading.setVisibility(View.GONE);
						voice_rcd_hint_rcding.setVisibility(View.GONE);
						voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
						mHandler.postDelayed(new Runnable() {
							public void run() {
								voice_rcd_hint_tooshort
										.setVisibility(View.GONE);
								rcChat_popup.setVisibility(View.GONE);
								isShosrt = false;
							}
						}, 500);
						return false;
					}
					
					handler_send_file.sendEmptyMessage(TO_UPLOAD_FILE);
					rcChat_popup.setVisibility(View.GONE);

				}
			}
			if (event.getY() < btn_rc_Y) {//���ư��µ�λ�ò�������¼�ư�ť�ķ�Χ��
				System.out.println("5");
				Animation mLitteAnimation = AnimationUtils.loadAnimation(this,
						R.anim.cancel_rc);
				Animation mBigAnimation = AnimationUtils.loadAnimation(this,
						R.anim.cancel_rc2);
				img1.setVisibility(View.GONE);
				del_re.setVisibility(View.VISIBLE);
				del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg);
				if (event.getY() >= del_Y
						&& event.getY() <= del_Y + del_re.getHeight()
						&& event.getX() >= del_x
						&& event.getX() <= del_x + del_re.getWidth()) {
					del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg_focused);
					sc_img1.startAnimation(mLitteAnimation);
					sc_img1.startAnimation(mBigAnimation);
				}
			} else {

				img1.setVisibility(View.VISIBLE);
				del_re.setVisibility(View.GONE);
				del_re.setBackgroundResource(0);
			}
		}
		return super.onTouchEvent(event);
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
			datestr = " date >'" + datestr + "' ";
		}
		
		List<ChatMsgEntity> list = messageDB.getMsg(user.getId(),datestr , 20);
		List<ChatMsgEntity> mDataArrays_tmp = new ArrayList<ChatMsgEntity>();
		System.out.println("reflesh date " + list.size());
		if (list.size() > 0) {
			for (ChatMsgEntity entity : list) {
				if (entity.getName()!=null && entity.getName().equals("")) {
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
	private static final int POLL_INTERVAL = 300;
	private Runnable mPollTask = new Runnable() {
		public void run() {
			double amp = mSensor.getAmplitude();
			updateDisplay(amp);
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);

		}
	};
	private Runnable mSleepTask = new Runnable() {
		public void run() {
			stop();
		}
	};
	private void start(String name) {
		mSensor.start(name);
		mHandler.postDelayed(mPollTask, POLL_INTERVAL);
	}

	private void stop() {
		mHandler.removeCallbacks(mSleepTask);
		mHandler.removeCallbacks(mPollTask);
		mSensor.stop();
		volume.setImageResource(R.drawable.amp1);
	}
private void updateDisplay(double signalEMA) {
		
		switch ((int) signalEMA) {
		case 0:
		case 1:
			volume.setImageResource(R.drawable.amp1);
			break;
		case 2:
		case 3:
			volume.setImageResource(R.drawable.amp2);
			
			break;
		case 4:
		case 5:
			volume.setImageResource(R.drawable.amp3);
			break;
		case 6:
		case 7:
			volume.setImageResource(R.drawable.amp4);
			break;
		case 8:
		case 9:
			volume.setImageResource(R.drawable.amp5);
			break;
		case 10:
		case 11:
			volume.setImageResource(R.drawable.amp6);
			break;
		default:
			volume.setImageResource(R.drawable.amp7);
			break;
		}
	}
	/**
	 * 加载消息历史，从数据库中读出
	 */
	public void initData(int cou) {
		List<ChatMsgEntity> list = messageDB.getMsg(user.getId(), "", cou);
		if(mDataArrays.size()>0)
			mDataArrays.clear();
		if (list.size() > 0) {
			for (ChatMsgEntity entity : list) {
				if (entity.getName()!=null && entity.getName().equals("")) {
					entity.setName(user.getName());
				}
				if (entity.getImg() < 0) {
					entity.setImg(user.getImg());
				}
				mDataArrays.add(entity);
			}
			Collections.reverse(mDataArrays);
		}
		
	}
	private void initViewPager() {
		LayoutInflater inflater = LayoutInflater.from(this);
		grids = new ArrayList<GridView>();
		gView1 = (GridView) inflater.inflate(R.layout.grid1, null);
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		// Éú³É24¸ö±íÇé
		for (int i = 0; i < 24; i++) {
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("image", expressionImages[i]);
			listItems.add(listItem);
		}

		SimpleAdapter simpleAdapter = new SimpleAdapter(ChatActivity.this, listItems,
				R.layout.singleexpression, new String[] { "image" },
				new int[] { R.id.image });
		gView1.setAdapter(simpleAdapter);
		gView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bitmap bitmap = null;
				bitmap = BitmapFactory.decodeResource(getResources(),
						expressionImages[arg2 % expressionImages.length]);
				ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap);
				SpannableString spannableString = new SpannableString(
						expressionImageNames[arg2].substring(1,
								expressionImageNames[arg2].length() - 1));
				spannableString.setSpan(imageSpan, 0,
						expressionImageNames[arg2].length() - 2,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				// ±à¼­¿òÉèÖÃÊý¾Ý
				mEditTextContent.append(spannableString);
				System.out.println("editµÄÄÚÈÝ = " + spannableString);
			}
		});
		grids.add(gView1);

		gView2 = (GridView) inflater.inflate(R.layout.grid2, null);
		grids.add(gView2);

		gView3 = (GridView) inflater.inflate(R.layout.grid3, null);
		grids.add(gView3);
		System.out.println("GridViewµÄ³¤¶È = " + grids.size());

		// Ìî³äViewPagerµÄÊý¾ÝÊÊÅäÆ÷
		PagerAdapter mPagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return grids.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager) container).removeView(grids.get(position));
			}

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager) container).addView(grids.get(position));
				return grids.get(position);
			}
		};

		viewPager.setAdapter(mPagerAdapter);
		// viewPager.setAdapter();

		viewPager.setOnPageChangeListener(new GuidePageChangeListener());
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		application.getNotReadmsslist().remove(user.getId() + "");
		messageDB.updateReadsta(user.getId());
		UploadUtil uploadUtil = UploadUtil.getInstance();

		uploadUtil.setOnUploadProcessListener(null); // 设置监听器监听上传状态
		uploadUtil.shutdownAllThread();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chat_send:// 发送按钮点击事件
			String contString = mEditTextContent.getText().toString();
			send(contString, 0, "");
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
					.setItems(new String[] { "相机拍照/视频", "本地相册", "请假","会议通知" },
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
									case 2:
										intent = new Intent(ChatActivity.this,
												ActionActivity.class);
										intent.putExtra("user", user);
										intent.putExtra("subview", R.layout.sub_leave);
										startActivityForResult(intent, 1);
										break;
									case 3:
										intent = new Intent(ChatActivity.this,
												ActionActivity.class);
										intent.putExtra("user", user);
										intent.putExtra("subview", R.layout.sub_notify);
										startActivityForResult(intent, 1);
										break;
									}
									dialog.cancel();
								}
							}).setNegativeButton("取消", null).show();
			break;
		case R.id.chat_back:// 返回按钮点击事件
			finish();// 结束,实际开发中，可以返回主界面
			break;
		case R.id.chatting_biaoqing_btn:
			biaoqingBtn.setVisibility(biaoqingBtn.GONE);
			biaoqingfocuseBtn.setVisibility(biaoqingfocuseBtn.VISIBLE);
			viewPager.setVisibility(viewPager.VISIBLE);
			page_select.setVisibility(page_select.VISIBLE);
			
			break;
		case R.id.chatting_biaoqing_focuse_btn:
			biaoqingBtn.setVisibility(biaoqingBtn.VISIBLE);
			biaoqingfocuseBtn.setVisibility(biaoqingfocuseBtn.GONE);
			viewPager.setVisibility(viewPager.GONE);
			page_select.setVisibility(page_select.GONE);
			break;
		}
	}

	public void msgsendok(String key, String id) {
		for (ChatMsgEntity cme : mDataArrays) {
			if (key.equals(cme.getDatekey()) && id.equals(user.getId() + "")) {
				cme.setSendSta(1);
				mAdapter.notifyDataSetChanged();
				// break;
			}
		}

	}

	/**
	 * 发送消息
	 */
	private void send(String contString, int is_pic, String pic_path_local) {

		if (contString.length() > 0) {
			ChatMsgEntity entity = application.send(contString, is_pic,
					pic_path_local, user);
			if (entity != null) {
				mDataArrays.add(entity);
				mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变

				mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项
			}
		}
	}

	@Override
	public void receiveMsg(TranObject msg) {
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case MESSAGE:
			TextMessage tm = (TextMessage) msg.getObject();
			application.removeNeedRefresh(user.getId() + "");
			Receive_message(msg, tm.getmsgtype(), "");
			break;
		default:
			break;
		}
	}

	private void Receive_message(TranObject msg, int msgtype, String path_pic) {
		TextMessage tm = (TextMessage) msg.getObject();
		String message = tm.getMessage();
		ChatMsgEntity entity = new ChatMsgEntity(user.getName(),
				MyDate.getDateEN(), message, user.getImg(), true, msgtype,
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
				send(stringList.get(i), 1, stringList_local.get(i));
			}
		} else {

		}
	}
	
	public void head_xiaohei(View v) { // ±êÌâÀ¸ ·µ»Ø°´Å¥
	}

	// ** Ö¸ÒýÒ³Ãæ¸Ä¼àÌýÆ÷ */
	class GuidePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			System.out.println("Ò³Ãæ¹ö¶¯" + arg0);

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			System.out.println("»»Ò³ÁË" + arg0);
		}

		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:
				page0.setImageDrawable(getResources().getDrawable(
						R.drawable.page_focused));
				page1.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));

				break;
			case 1:
				page1.setImageDrawable(getResources().getDrawable(
						R.drawable.page_focused));
				page0.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				page2.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
				// Éú³É24¸ö±íÇé
				for (int i = 0; i < 24; i++) {
					Map<String, Object> listItem = new HashMap<String, Object>();
					listItem.put("image", expressionImages1[i]);
					listItems.add(listItem);
				}

				SimpleAdapter simpleAdapter = new SimpleAdapter(ChatActivity.this,
						listItems, R.layout.singleexpression,
						new String[] { "image" }, new int[] { R.id.image });
				gView2.setAdapter(simpleAdapter);
				gView2.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Bitmap bitmap = null;
						bitmap = BitmapFactory.decodeResource(getResources(),
								expressionImages1[arg2
										% expressionImages1.length]);
						ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap);
						SpannableString spannableString = new SpannableString(
								expressionImageNames1[arg2]
										.substring(1,
												expressionImageNames1[arg2]
														.length() - 1));
						spannableString.setSpan(imageSpan, 0,
								expressionImageNames1[arg2].length() - 2,
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						// ±à¼­¿òÉèÖÃÊý¾Ý
						mEditTextContent.append(spannableString);
						System.out.println("editµÄÄÚÈÝ = " + spannableString);
					}
				});
				break;
			case 2:
				page2.setImageDrawable(getResources().getDrawable(
						R.drawable.page_focused));
				page1.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				page0.setImageDrawable(getResources().getDrawable(
						R.drawable.page_unfocused));
				List<Map<String, Object>> listItems1 = new ArrayList<Map<String, Object>>();
				// Éú³É24¸ö±íÇé
				for (int i = 0; i < 24; i++) {
					Map<String, Object> listItem = new HashMap<String, Object>();
					listItem.put("image", expressionImages2[i]);
					listItems1.add(listItem);
				}

				SimpleAdapter simpleAdapter1 = new SimpleAdapter(ChatActivity.this,
						listItems1, R.layout.singleexpression,
						new String[] { "image" }, new int[] { R.id.image });
				gView3.setAdapter(simpleAdapter1);
				gView3.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Bitmap bitmap = null;
						bitmap = BitmapFactory.decodeResource(getResources(),
								expressionImages2[arg2
										% expressionImages2.length]);
						ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap);
						SpannableString spannableString = new SpannableString(
								expressionImageNames2[arg2]
										.substring(1,
												expressionImageNames2[arg2]
														.length() - 1));
						spannableString.setSpan(imageSpan, 0,
								expressionImageNames2[arg2].length() - 2,
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						// ±à¼­¿òÉèÖÃÊý¾Ý
						mEditTextContent.append(spannableString);
						System.out.println("editµÄÄÚÈÝ = " + spannableString);
					}
				});
				break;

			}
		}
	}
	public class MyRefreshListener implements MyNormalListView.OnRefreshListener {

		@Override
		public void onRefresh() {
			AsyncTask<Void, Void, Void> tk = new AsyncTask<Void, Void, Void>() {
				List<User> list;

				protected Void doInBackground(Void... params) {
					initData(mDataArrays.size()+20);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					mAdapter.notifyDataSetChanged();
					mListView.onRefreshComplete();
					Toast.makeText(ChatActivity.this, "刷新成功", 0).show();
				}

			};
			tk.execute();
		}
	}
}