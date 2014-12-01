package com.way.util;

import com.way.chat.activity.MyApplication;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.client.Client;
import com.way.client.ClientOutputThread;

public class MyUtils {
	/**
	 * 提交账号密码信息到服务器
	 */
	public static void login(String accounts, String password,MyApplication application) {

		if (accounts.length() > 0 && password.length() > 0
				&& application.isClientStart()) {
			Client client = application.getClient();
			ClientOutputThread out = client.getClientOutputThread();
			TranObject<User> o = new TranObject<User>(TranObjectType.LOGIN);
			User u = new User();
			u.setLoginAccount(accounts);
			u.setPassword(Encode.getEncode("MD5", password));
			o.setObject(u);
			out.setMsg(o);
		}
	}

}
