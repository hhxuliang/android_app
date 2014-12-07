package com.way.chat.activity;

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
	private boolean is_pic = false;
	private String pic_path;
	private int sendSta;

	public int getSendSta() {
		return sendSta;
	}

	public void setSendSta(int sendSta) {
		this.sendSta = sendSta;
	}

	public ChatMsgEntity() {

	}

	public ChatMsgEntity(String name, String date, String text, int img,
			boolean isComMsg, boolean isPic, String picPath) {
		super();
		this.name = name;
		this.date = date;
		this.message = text;
		this.img = img;
		this.isComMeg = isComMsg;
		this.is_pic = isPic;
		this.pic_path = picPath;
	}

	public boolean get_is_pic() {
		return is_pic;
	}

	public void set_is_pic(boolean ispic) {
		this.is_pic = ispic;
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
