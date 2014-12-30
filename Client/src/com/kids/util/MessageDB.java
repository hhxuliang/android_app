package com.kids.util;

import java.util.ArrayList;
import java.util.List;

import com.kids.activity.chat.ChatMsgEntity;
import com.way.chat.common.util.Constants;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MessageDB {
	private SQLiteDatabase db;

	public MessageDB(Context context) {
		db = context.openOrCreateDatabase(Constants.DBNAME,
				Context.MODE_PRIVATE, null);
	}

	public void createTable(int id) {
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ id
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, img TEXT,date TEXT,isCome TEXT,message TEXT,isPic TEXT,picPath TEXT,sendsta TEXT,readsta TEXT,datekey TEXT,msgid TEXT,serverdatekey TEXT)");
	}

	public void saveMsg(int id, ChatMsgEntity entity) {
		createTable(id);
		int isCome = 0;
		if (entity.getMsgType()) {// 如果是收到的消息，保存在数据库的值为1
			isCome = 1;
		}
		db.execSQL(
				"insert into _"
						+ id
						+ " (name,img,date,isCome,message,isPic,picPath,sendsta,readsta,datekey,msgid,serverdatekey) values(?,?,?,?,?,?,?,?,?,?,?,?)",
				new Object[] { entity.getName(), entity.getImg(),
						entity.getDate(), isCome, entity.getMessage(),
						entity.getmsgtype(), entity.getPicPath(),
						entity.getSendSta(), entity.getReadSta(),
						entity.getDatekey(), entity.getMsgid(),
						entity.getServerdatekey() });
	}

	public void updateMsg(int id, String setStr_path, String whereStr_msg) {
		createTable(id);
		db.execSQL("update _" + id + "  set picPath=? where message=?",
				new Object[] { setStr_path, whereStr_msg });
	}

	public void updateDBbyMsgOk(String key, String id) {
		createTable(Integer.parseInt(id));
		db.execSQL("update _" + id + "  set sendsta=? where datekey=?",
				new Object[] { 1, key });
	}

	public void updateReadsta(int id) {
		createTable(id);
		db.execSQL("update _" + id + "  set readsta=?", new Object[] { 0 });
	}

	public boolean GetMsgReadSta(int id) {
		createTable(id);
		Cursor c;
		c = db.rawQuery("select * from _" + id
				+ " where isCome='1' and  readsta='1' LIMIT 1", null);
		if (c.moveToNext()) {
			int iii = c.getInt(c.getColumnIndex("readsta"));
			c.close();
			return true;
		}
		c.close();
		return false;
	}

	public String getServerDatekeybyCrowd(int id) {
		createTable(id);
		Cursor c;
		c = db.rawQuery("SELECT * from _" + id
				+ " ORDER BY serverdatekey DESC LIMIT 1",
				null);
		String serverdatekey="";
		while (c.moveToNext()) {
			serverdatekey = c.getString(c.getColumnIndex("serverdatekey"));
		}
		c.close();
		return serverdatekey;
	}

	public List<ChatMsgEntity> getMsg(int id, String whereStr_time, int limit) {
		List<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
		createTable(id);
		Cursor c;
		if (whereStr_time.equals("")) {
			c = db.rawQuery("SELECT * from _" + id
					+ " ORDER BY _id DESC LIMIT " + limit, null);
		} else {
			c = db.rawQuery("SELECT * from _" + id
					+ " where " + whereStr_time + " ORDER BY _id DESC LIMIT " + limit,
					null);
		}

		while (c.moveToNext()) {
			System.out.println("get date");
			String name = c.getString(c.getColumnIndex("name"));
			int img = c.getInt(c.getColumnIndex("img"));
			String date = c.getString(c.getColumnIndex("date"));
			int isCome = c.getInt(c.getColumnIndex("isCome"));
			String message = c.getString(c.getColumnIndex("message"));
			int ispic = c.getInt(c.getColumnIndex("isPic"));
			String pic_path = c.getString(c.getColumnIndex("picPath"));
			int sends = c.getInt(c.getColumnIndex("sendsta"));
			String datekey = c.getString(c.getColumnIndex("datekey"));
			String serverdatekey = c.getString(c
					.getColumnIndex("serverdatekey"));
			boolean isComMsg = false;
			if (isCome == 1) {
				isComMsg = true;
			}
			ChatMsgEntity entity = new ChatMsgEntity(name, date, message, img,
					isComMsg, ispic, pic_path);
			entity.setPicPath(pic_path);
			entity.setSendSta(sends);
			entity.setDatekey(datekey);
			entity.setServerdatekey(serverdatekey);
			list.add(entity);
		}
		c.close();
		return list;
	}

	public void close() {
		if (db != null)
			db.close();
	}
}
