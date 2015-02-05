package com.kids.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.kids.activity.chat.HandleMsg;
import com.kids.activity.chat.MyApplication;
import com.kids.util.ImageProcess;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.util.Constants;
import com.way.chat.common.util.MyDate;

public class DownloadFile extends Thread {
	private MyApplication application;
	private int statu = 0;
	private Vector object = new Vector();
	private HashMap<String, Integer> mMap_Waiting_Download_Pic = null;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String savePath = null;
		String mUrl = null;
		int mUid;
		ArrayList<HandleMsg> al = new ArrayList<HandleMsg>();
		this.statu = 1;
		while (true) {
			al.clear();
			synchronized (this) {
				if (object.size() == 0) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						this.statu = 2;
					}
				}

				Iterator i = object.iterator(); // Must be in synchronized
				// block
				while (i.hasNext()) {
					HandleMsg msg = (HandleMsg) i.next();
					al.add(msg);
				}
				object.clear();
			}
			for (HandleMsg hm : al) {
				mUrl = hm.mUrl;
				mUid = hm.mComefromUid;
				try {
					Thread.sleep(300);// so urgly for the sleep 300 because voice file is so small that download faster then the message coming,
										// this will cause message in chat not be update.
					URL url = new URL(mUrl);
					HttpURLConnection con = (HttpURLConnection) url
							.openConnection();
					con.setConnectTimeout(5000);
					con.setRequestMethod("GET");
					con.connect();
					String prefix = mUrl.substring(mUrl.lastIndexOf("."));
					if(ImageProcess.FileType.VOICE==ImageProcess.checkFileType(mUrl))
					{
						savePath = application.getDownloadVoicePath() + "/" + mUid
								+ "_kids_" + System.currentTimeMillis() + prefix;
					}
					else if(ImageProcess.FileType.APK==ImageProcess.checkFileType(mUrl))
						savePath = application.getDownloadPicPath() + "/../../" + mUid
						+ "_kids_" + MyDate.getDateMillis() + prefix;
					else
						savePath = application.getDownloadPicPath() + "/" + mUid
							+ "_kids_" + MyDate.getDateMillis() + prefix;
					if (con.getResponseCode() == 200) {
						InputStream is = con.getInputStream();

						FileOutputStream fos = new FileOutputStream(savePath);
						byte[] buffer = new byte[8192];
						int count = 0;
						while ((count = is.read(buffer)) != -1) {
							fos.write(buffer, 0, count);
						}
						fos.close();
						is.close();

						application.handler_download_pic.obtainMessage(
								MyApplication.DOWNLOADPIC_OK,
								new HandleMsg(mUrl, savePath)).sendToTarget();
					} else {
						application.handler_download_pic.obtainMessage(
								MyApplication.DOWNLOADPIC_FAULT,
								new HandleMsg(mUrl, "")).sendToTarget();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					application.handler_download_pic.obtainMessage(
							MyApplication.DOWNLOADPIC_FAULT,
							new HandleMsg(mUrl, "")).sendToTarget();
					this.statu = 2;
					if (savePath != null) {
						File file = new File(savePath);
						if (file.exists() && file.isFile()) {
							file.delete();
						}
					}
				}

			}
		}

	}

	public int getStatu() {
		return statu;
	}

	public void setStatu(int statu) {
		this.statu = statu;
	}

	public void startDownloadPic(String msg, int uid) {
		synchronized (this) {
			for (int i = 0; i < object.size(); i++) {
				if (((HandleMsg) object.get(i)).mUrl.equals(msg))
					return;
			}
			object.add(new HandleMsg(msg, uid));
			notify();
		}
		mMap_Waiting_Download_Pic.put(msg, uid);

	}

	public DownloadFile(MyApplication app, HashMap<String, Integer> map) {
		application = app;
		mMap_Waiting_Download_Pic = map;
	}

}
