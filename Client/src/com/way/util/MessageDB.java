package com.way.util;

import java.util.ArrayList;
import java.util.List;

import com.way.chat.activity.ChatMsgEntity;
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

	public void saveMsg(int id, ChatMsgEntity entity) {
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ id
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, img TEXT,date TEXT,isCome TEXT,message TEXT,isPic TEXT,picPath TEXT,sendsta TEXT)");
		int isCome = 0;
		if (entity.getMsgType()) {// 如果是收到的消息，保存在数据库的值为1
			isCome = 1;
		}
		db.execSQL(
				"insert into _"
						+ id
						+ " (name,img,date,isCome,message,isPic,picPath,sendsta) values(?,?,?,?,?,?,?,?)",
				new Object[] { entity.getName(), entity.getImg(),
						entity.getDate(), isCome, entity.getMessage(),
						entity.get_is_pic(), entity.getPicPath(),entity.getSendSta() });
	}

	public void updateMsg(int id, String setStr_path, String whereStr_msg) {
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ id
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, img TEXT,date TEXT,isCome TEXT,message TEXT,isPic TEXT,picPath TEXT,sendsta TEXT)");
		db.execSQL("update _" + id + "  set picPath=? where message=?",
				new Object[] { setStr_path, whereStr_msg });
	}

	public void updateDBbyMsgOk(String msg, int id) {
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ id
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, img TEXT,date TEXT,isCome TEXT,message TEXT,isPic TEXT,picPath TEXT,sendsta TEXT)");
		db.execSQL("update _" + id + "  set sendsta=? where message=?",
				new Object[] { 1,msg });
	}

	public List<ChatMsgEntity> getMsg(int id, String whereStr_time, int limit) {
		List<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ id
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, img TEXT,date TEXT,isCome TEXT,message TEXT,isPic TEXT,picPath TEXT,sendsta TEXT)");
		Cursor c;
		if (whereStr_time.equals("")) {
			c = db.rawQuery("SELECT * from _" + id
					+ " ORDER BY _id DESC LIMIT " + limit, null);
		} else {
			c = db.rawQuery("SELECT * from _" + id
					+ " where date > ? ORDER BY _id DESC LIMIT " + limit,
					new String[] { whereStr_time });
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
			int sends =c.getInt(c.getColumnIndex("sendsta"));
			boolean isComMsg = false;
			if (isCome == 1) {
				isComMsg = true;
			}
			boolean picOrNot = false;
			if (ispic >= 1)
				picOrNot = true;
			else
				picOrNot = false;
			ChatMsgEntity entity = new ChatMsgEntity(name, date, message, img,
					isComMsg, picOrNot, pic_path);
			entity.setPicPath(pic_path);
			entity.setSendSta(sends);
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
