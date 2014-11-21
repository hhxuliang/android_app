package com.way.chat.activity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.way.util.ImageProcess;
import com.zoom.ZoomImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 消息ListView的Adapter
 * 
 * @author way
 */
public class ChatMsgViewAdapter extends BaseAdapter {
	private int[] imgs = { R.drawable.icon, R.drawable.f1, R.drawable.f2,
			R.drawable.f3, R.drawable.f4, R.drawable.f5, R.drawable.f6,
			R.drawable.f7, R.drawable.f8, R.drawable.f9 };

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;// 收到对方的消息
		int IMVT_TO_MSG = 1;// 自己发送出去的消息
	}

	private Context mContext = null;
	private static final int ITEMCOUNT = 2;// 消息类型的总数
	private List<ChatMsgEntity> coll;// 消息对象数组
	private LayoutInflater mInflater;

	public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> coll) {
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		mContext = context;
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * 得到Item的类型，是对方发过来的消息，还是自己发送出去的
	 */
	public int getItemViewType(int position) {
		ChatMsgEntity entity = coll.get(position);

		if (entity.getMsgType()) {// 收到的消息
			return IMsgViewType.IMVT_COM_MSG;
		} else {// 自己发送的消息
			return IMsgViewType.IMVT_TO_MSG;
		}
	}

	/**
	 * Item类型的总数
	 */
	public int getViewTypeCount() {
		return ITEMCOUNT;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ChatMsgEntity entity = coll.get(position);
		boolean isComMsg = entity.getMsgType();

		ViewHolder viewHolder = null;
		if (convertView == null) {
			if (isComMsg) {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_left, null);
			} else {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_right, null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.tvPicture = (ImageView) convertView
					.findViewById(R.id.imageView_chat_pic);
			viewHolder.icon = (ImageView) convertView
					.findViewById(R.id.iv_userhead);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tvSendTime.setText(entity.getDate());
		viewHolder.tvUserName.setText(entity.getName());
		if (entity.get_is_pic()) {
			viewHolder.tvContent.setVisibility(View.GONE);
			viewHolder.tvPicture.setVisibility(View.VISIBLE);

			System.out.println("height is :" + MyApplication.mWindowHeight);
			System.out.println("width is :" + MyApplication.mWindowWidth);
			Bitmap bitmap = ImageProcess.GetBitmapByPath(mContext,
					entity.getPicPath(), MyApplication.mWindowHeight,
					MyApplication.mWindowWidth, 0.5);
			viewHolder.tvPicture.setImageBitmap(bitmap);

			viewHolder.tvPicture.setContentDescription(entity.getPicPath());
			viewHolder.tvPicture.invalidate();
			/*
			 * viewHolder.tvPicture.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) {
			 * v.setDrawingCacheEnabled(true); Bitmap bitmap =
			 * v.getDrawingCache(); if(mContext!=null){ ZoomImageView zoom = new
			 * ZoomImageView(mContext, bitmap); zoom.showZoomView(); } } });
			 */
			System.out.println(entity.getPicPath());

		} else {
			viewHolder.tvPicture.setVisibility(View.GONE);
			viewHolder.tvContent.setVisibility(View.VISIBLE);
			viewHolder.tvContent.setText(entity.getMessage());

		}
		viewHolder.icon.setImageResource(imgs[entity.getImg()]);
		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public ImageView tvPicture;
		public ImageView icon;
		public boolean isComMsg = true;
	}

}
