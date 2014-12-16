package com.kids.util;

import com.kids.activity.chat.MyApplication;
import com.kids.client.Client;
import com.kids.client.ClientOutputThread;
import com.way.chat.common.bean.CommonMsg;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;

public class MyUtils {
	/**
	 * 提交账号密码信息到服务器
	 */
	public static void login(String accounts, String password,
			MyApplication application) {

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

	public static void sendCrowdofflineMsgReq(String uid, String crowdid,String where,
			MyApplication application) {

		if (uid.length() > 0 && crowdid.length() > 0
				&& application.isClientStart()) {
			Client client = application.getClient();
			ClientOutputThread out = client.getClientOutputThread();
			TranObject<CommonMsg> o = new TranObject<CommonMsg>(TranObjectType.CROWDOFFLINEMSG);
			CommonMsg u = new CommonMsg();
			u.setarg1(where);
			o.setFromUser(Integer.parseInt(uid));
			o.setCrowd(Integer.parseInt(crowdid));
			o.setObject(u);
			out.setMsg(o);
		}
	}

}
