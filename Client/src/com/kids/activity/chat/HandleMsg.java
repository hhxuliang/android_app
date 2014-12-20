package com.kids.activity.chat;

import java.io.Serializable;

public class HandleMsg implements Serializable {
	public int mComefromUid;
	public String mUrl;
	public String mSavePath;

	public HandleMsg(String url, String savepath) {
		mUrl = url;
		mSavePath = savepath;
	}
	public HandleMsg(String url, int uid) {
		mUrl = url;
		mComefromUid = uid;
	}
}