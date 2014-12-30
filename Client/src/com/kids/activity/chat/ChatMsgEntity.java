package com.kids.activity.chat;

/**
 * 一个聊天消息的JavaBean
 * 
 * @author way
 * 
 */
public class ChatMsgEntity {
	private String name;// 消息来自
	private String date;// 消息日期
	private String message;// 消息内容
	private int img;
	private boolean isComMeg = true;// 是否为收到的消息
	private int msgtype=0;
	private String pic_path;
	private int sendSta;
	private int readSta;
	private String datekey;
	private String serverdatekey;
	private int msgid;

	public String getServerdatekey() {
		return serverdatekey;
	}

	public void setServerdatekey(String serverdatekey) {
		this.serverdatekey = serverdatekey;
	}

	public int getMsgid() {
		return msgid;
	}

	public void setMsgid(int msgid) {
		this.msgid = msgid;
	}

	public String getDatekey() {
		return datekey;
	}

	public void setDatekey(String datekey) {
		this.datekey = datekey;
	}

	public int getReadSta() {
		return readSta;
	}

	public void setReadSta(int readSta) {
		this.readSta = readSta;
	}

	public int getSendSta() {
		return sendSta;
	}

	public void setSendSta(int sendSta) {
		this.sendSta = sendSta;
	}

	public ChatMsgEntity() {

	}

	public ChatMsgEntity(String name, String date, String text, int img,
			boolean isComMsg, int type, String picPath) {
		super();
		this.name = name;
		this.date = date;
		this.message = text;
		this.img = img;
		this.isComMeg = isComMsg;
		this.msgtype = type;
		this.pic_path = picPath;
		this.readSta = 1;
	}

	public int getmsgtype() {
		return msgtype;
	}

	public void setmsgtype(int ispic) {
		msgtype=ispic;
	}

	public String getPicPath() {
		return pic_path;
	}

	public void setPicPath(String path) {
		this.pic_path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean getMsgType() {
		return isComMeg;
	}

	public void setMsgType(boolean isComMsg) {
		isComMeg = isComMsg;
	}

	public int getImg() {
		return img;
	}

	public void setImg(int img) {
		this.img = img;
	}
}
