package com.way.chat.activity;

import java.io.Serializable;

class HandleMsg implements Serializable {
	public int mComefromUid;
	public String mUrl;
	public String mSavePath;

	public HandleMsg(String url, String savepath) {
		mUrl = url;
		mSavePath = savepath;
	}
}