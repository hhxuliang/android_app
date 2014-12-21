package com.kids.activity.chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.view.WindowManager;
import android.widget.Toast;

import com.kids.util.SharePreferenceUtil;
import com.way.chat.activity.R;
import com.way.chat.common.util.Constants;

/**
 * 欢迎界面
 * 
 * @author way
 */
public class WelcomeActivity extends Activity {
	private SharePreferenceUtil util;
	private Handler mHandler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		util = new SharePreferenceUtil(this, Constants.SAVE_USER);
		if (util.getisFirst()) {
			createShut();// 创建快捷方式
			moveSound();
		}
		initView();
		initEnv();
		
		// 获取Android屏幕的服务
		WindowManager wm = this.getWindowManager();
		// 获取屏幕的分辨率，getHeight()、getWidth已经被废弃掉了
		// 应该使用getSize()，但是这里为了向下兼容所以依然使用它们
		
		MyApplication.mWindowHeight = wm.getDefaultDisplay().getHeight();
		MyApplication.mWindowWidth = wm.getDefaultDisplay().getWidth();
		
	}

	public void initEnv() {
		MyApplication application = (MyApplication) this
				.getApplicationContext();
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// 创建一个文件夹对象，赋值为外部存储器的目录
			File sdcardDir = Environment.getExternalStorageDirectory();
			// 得到一个路径，内容是sdcard的文件夹路径和名字
			String path = sdcardDir.getPath() + "/children";
			application.setHomePath(path);
			
			File path1 = new File(path);
			if (!path1.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path1.mkdirs();
			}
			
			String path_camera = application.getCameraPath();
			File path_camera_f = new File(path_camera);
			if (!path_camera_f.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path_camera_f.mkdirs();
			}
			String path_pic = application.getMyUploadPicPath();
			File path_pic_f = new File(path_pic);
			if (!path_pic_f.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path_pic_f.mkdirs();
			}
			
			String path_pic_1 = application.getDownloadPicPath();
			File path_pic_f_1 = new File(path_pic_1);
			if (!path_pic_f_1.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path_pic_f_1.mkdirs();
			}
		} else {
			Toast.makeText(this, "内存卡不存在...", Toast.LENGTH_LONG).show();
			return;

		}

	}

	public void initView() {
		if (util.getIsStart()) {// 如果正在后台运行
			goFriendListActivity();
		} else {// 如果是首次运行
			mHandler = new Handler();
			mHandler.postDelayed(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					goLoginActivity();
				}
			}, 1000);
		}
	}

	/**
	 * 进入登陆界面
	 */
	public void goLoginActivity() {
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * 进入好友列表界面
	 */
	public void goFriendListActivity() {
		Intent i = new Intent(this, MyMainActivity.class);
		startActivity(i);
		util.setIsStart(false);
		finish();
	}

	/**
	 * 创建桌面快捷方式
	 */
	public void createShut() {
		// 创建添加快捷方式的Intent
		Intent addIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		addIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED| Intent.FLAG_ACTIVITY_NEW_TASK);
		String title = getResources().getString(R.string.app_name);
		// 加载快捷方式的图标
		Parcelable icon = Intent.ShortcutIconResource.fromContext(
				WelcomeActivity.this, R.drawable.icon);
		// 创建点击快捷方式后操作Intent,该处当点击创建的快捷方式后，再次启动该程序
		Intent myIntent = new Intent(WelcomeActivity.this,
				WelcomeActivity.class);
		// 设置快捷方式的标题
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		// 设置快捷方式的图标
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		// 设置快捷方式对应的Intent
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, myIntent);
		// 发送广播添加快捷方式
		sendBroadcast(addIntent);
		util.setIsFirst(false);
	}

	/**
	 * 复制原生资源文件“来消息声音”到应用目录下，
	 */
	public void moveSound() {
		InputStream is = getResources().openRawResource(R.raw.msg);
		File file = new File(getFilesDir(), "msg.mp3");
		try {
			OutputStream os = new FileOutputStream(file);
			int len = -1;
			byte[] buffer = new byte[1024];
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			// System.out.println("声音复制完毕");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断手机网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager mgr = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = mgr.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	private void toast(Context context) {
		new AlertDialog.Builder(context)
				.setTitle("温馨提示")
				.setMessage("亲！您的网络连接未打开哦")
				.setPositiveButton("前往打开",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(
										android.provider.Settings.ACTION_WIRELESS_SETTINGS);
								startActivity(intent);
							}
						}).setNegativeButton("取消", null).create().show();
	}
}