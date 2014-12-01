package com.way.chat.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.chat.common.util.Constants;
import com.way.client.Client;
import com.way.client.ClientOutputThread;
import com.way.util.DialogFactory;
import com.way.util.Encode;
import com.way.util.MyUtils;
import com.way.util.SharePreferenceUtil;
import com.way.util.UserDB;

/**
 * 登录
 * 
 * @author way
 * 
 */
public class LoginActivity extends MyActivity implements OnClickListener {
	private Button mBtnRegister;
	private Button mBtnLogin;
	private EditText mAccounts, mPassword;
	private CheckBox mAutoSavePassword;
	private MyApplication application;

	private View mMoreView;// “更多登录选项”的view
	private ImageView mMoreImage;// “更多登录选项”的箭头图片
	private View mMoreMenuView;// “更多登录选项”中的内容view
	private MenuInflater mi;// 菜单
	private boolean mShowMenu = false;// “更多登录选项”的内容是否显示

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginpage);
		application = (MyApplication) this.getApplicationContext();
		initView();
		mi = new MenuInflater(this);
		if (!mAccounts.getText().toString().equals("")
				&& !mPassword.getText().toString().equals("")) {
			// submit();
			gotoMain();
		}
	}

	public void gotoMain() {
		Intent i = new Intent(LoginActivity.this, MyMainActivity.class);
		startActivity(i);
		finish();
	}

	@Override
	protected void onResume() {// 在onResume方法里面先判断网络是否可用，再启动服务,这样在打开网络连接之后返回当前Activity时，会重新启动服务联网，
		super.onResume();

	}

	public void initView() {
		mAutoSavePassword = (CheckBox) findViewById(R.id.auto_save_password);
		mMoreView = findViewById(R.id.more);
		mMoreMenuView = findViewById(R.id.moremenu);
		mMoreImage = (ImageView) findViewById(R.id.more_image);
		mMoreView.setOnClickListener(this);

		mBtnRegister = (Button) findViewById(R.id.regist_btn);
		mBtnRegister.setOnClickListener(this);

		mBtnLogin = (Button) findViewById(R.id.login_btn);
		mBtnLogin.setOnClickListener(this);

		mAccounts = (EditText) findViewById(R.id.lgoin_accounts);
		mPassword = (EditText) findViewById(R.id.login_password);
		if (mAutoSavePassword.isChecked()) {
			SharePreferenceUtil util = new SharePreferenceUtil(
					LoginActivity.this, Constants.SAVE_USER);
			mAccounts.setText(util.getName());
			mPassword.setText(util.getPasswd());

		}
	}

	/**
	 * “更多登录选项”内容的显示方法
	 * 
	 * @param bShow
	 *            是否显示
	 */
	public void showMoreView(boolean bShow) {
		if (bShow) {
			mMoreMenuView.setVisibility(View.GONE);
			mMoreImage.setImageResource(R.drawable.login_more_up);
			mShowMenu = true;
		} else {
			mMoreMenuView.setVisibility(View.VISIBLE);
			mMoreImage.setImageResource(R.drawable.login_more);
			mShowMenu = false;
		}
	}

	/**
	 * 处理点击事件
	 */
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.more:
			showMoreView(!mShowMenu);
			break;
		case R.id.regist_btn:
			goRegisterActivity();
			break;
		case R.id.login_btn:
			submit();
			break;
		default:
			break;
		}
	}

	/**
	 * 进入注册界面
	 */
	public void goRegisterActivity() {
		Intent intent = new Intent();
		intent.setClass(this, RegisterActivity.class);
		startActivity(intent);
	}

	/**
	 * 点击登录按钮后，弹出验证对话框
	 */
	private Dialog mDialog = null;

	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在验证账号...");
		mDialog.show();
	}

	/**
	 * 提交账号密码信息到服务器
	 */
	private void submit() {
		// 获取Android屏幕的服务
		WindowManager wm = (WindowManager) LoginActivity.this
				.getSystemService(LoginActivity.this.WINDOW_SERVICE);
		// 获取屏幕的分辨率，getHeight()、getWidth已经被废弃掉了
		// 应该使用getSize()，但是这里为了向下兼容所以依然使用它们
		MyApplication.mWindowHeight = wm.getDefaultDisplay().getHeight();
		MyApplication.mWindowWidth = wm.getDefaultDisplay().getWidth();
		System.out.println("height is :" + MyApplication.mWindowHeight);
		System.out.println("width is :" + MyApplication.mWindowWidth);

		String accounts = mAccounts.getText().toString();
		String password = mPassword.getText().toString();
		if (accounts.length() == 0 || password.length() == 0) {
			DialogFactory.ToastDialog(this, "登录", "亲！帐号或密码不能为空哦");
		} else {
			showRequestDialog();
			// 通过Socket验证信息
			if (application.isClientStart()) {
				MyUtils.login(accounts, password, application);
			} else {
				if (mDialog.isShowing())
					mDialog.dismiss();
				DialogFactory.ToastDialog(LoginActivity.this, "登录",
						"亲！服务器暂未开放哦");
			}
		}
	}

	@Override
	// 依据自己需求处理父类广播接收者收取到的消息
	public void getMessage(TranObject msg) {
		if (msg != null) {
			// System.out.println("Login:" + msg);
			switch (msg.getType()) {
			case LOGIN:// LoginActivity只处理登录的消息
				List<User> list = (List<User>) msg.getObject();
				if (list != null && list.size() > 0) {
					// 保存用户信息
					SharePreferenceUtil util = new SharePreferenceUtil(
							LoginActivity.this, Constants.SAVE_USER);
					util.setId(list.get(0).getId() + "");
					util.setPasswd(mPassword.getText().toString());
					util.setEmail(list.get(0).getEmail());
					util.setName(list.get(0).getName());
					util.setImg(list.get(0).getImg());
					gotoMain();

					if (mDialog.isShowing())
						mDialog.dismiss();
					
					Toast.makeText(getApplicationContext(), "登录成功", 0).show();
					if (list.get(0).getOffLineMessUser() != null
							&& list.get(0).getOffLineMessUser().size() > 0) {
						Toast.makeText(getApplicationContext(), "你有离线的消息，请查阅！",
								0).show();
						application.setOffLineList(list.get(0)
								.getOffLineMessUser());
					}

				} else {
					DialogFactory.ToastDialog(LoginActivity.this, "登录",
							"亲！您的帐号或密码错误哦");
					if (mDialog.isShowing())
						mDialog.dismiss();
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	// 添加菜单
	public boolean onCreateOptionsMenu(Menu menu) {
		mi.inflate(R.menu.login_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	// 菜单选项添加事件处理
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.login_menu_setting:
			setDialog();
			break;
		case R.id.login_menu_exit:
			exitDialog(LoginActivity.this, "提示", "亲！您真的要退出吗？");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {// 捕获返回按键
		exitDialog(LoginActivity.this, "提示", "亲！您真的要退出吗？");
	}

	/**
	 * 退出时的提示框
	 * 
	 * @param context
	 *            上下文对象
	 * @param title
	 *            标题
	 * @param msg
	 *            内容
	 */
	private void exitDialog(Context context, String title, String msg) {
		new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (application.isClientStart()) {// 如果连接还在，说明服务还在运行
							// 关闭服务
							Intent service = new Intent(LoginActivity.this,
									GetMsgService.class);
							stopService(service);
						}
						close();// 调用父类自定义的循环关闭方法
					}
				}).setNegativeButton("取消", null).create().show();
	}

	/**
	 * “设置”菜单选项的功能实现
	 */
	private void setDialog() {
		final View view = LayoutInflater.from(LoginActivity.this).inflate(
				R.layout.setting_view, null);
		new AlertDialog.Builder(LoginActivity.this).setTitle("设置服务器ip、port")
				.setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 把ip和port保存到文件中
						EditText ipEditText = (EditText) view
								.findViewById(R.id.setting_ip);
						EditText portEditText = (EditText) view
								.findViewById(R.id.setting_port);
						String ip = ipEditText.getText().toString();
						String port = portEditText.getText().toString();
						SharePreferenceUtil util = new SharePreferenceUtil(
								LoginActivity.this, Constants.IP_PORT);
						if (ip.length() > 0 && port.length() > 0) {
							util.setIp(ip);
							util.setPort(Integer.valueOf(port));
							Toast.makeText(getApplicationContext(),
									"亲！保存成功，重启生效哦", 0).show();
							finish();
						} else {
							Toast.makeText(getApplicationContext(),
									"亲！ip和port都不能为空哦", 0).show();
						}
					}
				}).setNegativeButton("取消", null).create().show();
	}

}
