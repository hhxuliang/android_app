package com.kids.activity.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.kids.client.Client;
import com.kids.client.ClientOutputThread;
import com.kids.util.DialogFactory;
import com.kids.util.Encode;
import com.way.chat.activity.R;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;

public class RegisterActivity extends MyActivity implements OnClickListener {

	private Button mBtnRegister;
	private Button mRegBack;
	private EditText mEmailEt, mNameEt, mPasswdEt, mPasswdEt2;
	
	private MyApplication application;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.register);
		application = (MyApplication) this.getApplicationContext();
		initView();

	}

	public void initView() {
		mBtnRegister = (Button) findViewById(R.id.register_btn);
		mRegBack = (Button) findViewById(R.id.reg_back_btn);
		mBtnRegister.setOnClickListener(this);
		mRegBack.setOnClickListener(this);

		mEmailEt = (EditText) findViewById(R.id.reg_email);
		mNameEt = (EditText) findViewById(R.id.reg_name);
		mPasswdEt = (EditText) findViewById(R.id.reg_password);
		mPasswdEt2 = (EditText) findViewById(R.id.reg_password2);

	}

	private Dialog mDialog = null;

	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在注册中...");
		mDialog.show();
	}

	@Override
	public void onBackPressed() {// 捕获返回键
		toast(RegisterActivity.this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.register_btn:
			// showRequestDialog();
			estimate();
			break;
		case R.id.reg_back_btn:
			toast(RegisterActivity.this);
			break;
		default:
			break;
		}
	}

	private void toast(Context context) {
		new AlertDialog.Builder(context).setTitle("注册")
				.setMessage("亲！您真的不注册了吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton("取消", null).create().show();
	}

	private void estimate() {
		String email = mEmailEt.getText().toString();
		String name = mNameEt.getText().toString();
		String passwd = mPasswdEt.getText().toString();
		String passwd2 = mPasswdEt2.getText().toString();
		if (email.equals("") || name.equals("") || passwd.equals("")
				|| passwd2.equals("")) {
			DialogFactory.ToastDialog(RegisterActivity.this, "注册",
					"亲！带*项是不能为空的哦");
		} else {
			if (passwd.equals(passwd2)) {
				showRequestDialog();
				// 提交注册信息
				if (application.isClientStart()) {// 如果已连接上服务器
					Client client = application.getClient();
//					Client client = GetMsgService.client;
					ClientOutputThread out = client.getClientOutputThread();
					TranObject<User> o = new TranObject<User>(
							TranObjectType.REGISTER);
					User u = new User();
					u.setEmail(email);
					u.setName(name);
					u.setPassword(Encode.getEncode("MD5", passwd));
					o.setObject(u);
					out.setMsg(o);
				} else {
					if (mDialog.isShowing())
						mDialog.dismiss();
					DialogFactory.ToastDialog(this, "注册", "亲！服务器暂未开放哦");
				}

			} else {
				DialogFactory.ToastDialog(RegisterActivity.this, "注册",
						"亲！您两次输入的密码不同哦");
			}
		}
	}

	@Override
	public void getMessage(TranObject msg) {
		// TODO Auto-generated method stub
		switch (msg.getType()) {
		case REGISTER:
			User u = (User) msg.getObject();
			int id = u.getId();
			if (id > 0) {
				if (mDialog != null) {
					mDialog.dismiss();
					mDialog = null;
				}
				DialogFactory.ToastDialog(RegisterActivity.this, "注册",
						"亲！您的登录ID是：" + id);
				finish();
			} else {
				if (mDialog != null) {
					mDialog.dismiss();
					mDialog = null;
				}
				if (id == 0) 
				{
					DialogFactory.ToastDialog(RegisterActivity.this, "注册",
							"亲！很抱歉！注册失败！请联系我们!");
					finish();
				}
				if (id == -1) 
					DialogFactory.ToastDialog(RegisterActivity.this, "注册",
							"亲！很抱歉！您的邮箱已经被注册!请更改后重新注册!");
				if (id == -2) 
					DialogFactory.ToastDialog(RegisterActivity.this, "注册",
							"亲！很抱歉！您的昵称已经被注册!请更改后重新注册!");
			}
			break;

		default:
			break;
		}
	}
}
