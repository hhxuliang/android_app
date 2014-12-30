package com.kids.activity.chat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.kids.activity.imagescan.MyImageView;
import com.kids.activity.imagescan.NativeImageLoader;
import com.kids.activity.imagescan.MyImageView.OnMeasureListener;
import com.kids.activity.imagescan.NativeImageLoader.NativeImageCallBack;
import com.kids.util.ImageProcess;
import com.kids.util.ZoomImageView;
import com.way.chat.activity.R;
import com.way.chat.common.bean.User;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
	private Point mPoint = new Point(0, 0);// 用来封装ImageView的宽和高的对象
	private Context mContext = null;
	private static final int ITEMCOUNT = 2;// 消息类型的总数
	private List<ChatMsgEntity> coll;// 消息对象数组
	private LayoutInflater mInflater;
	private User user = null;
	private ListView mlistView;

	public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> coll, User u,ListView list) {
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		mContext = context;
		user = u;
		mlistView = list;
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
			viewHolder.tvPicture = (MyImageView) convertView
					.findViewById(R.id.imageView_chat_pic);
			viewHolder.icon = (ImageView) convertView
					.findViewById(R.id.iv_userhead);
			viewHolder.isComMsg = isComMsg;
			viewHolder.tvReflesh = (ImageView) convertView
					.findViewById(R.id.imageView_reflesh);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tvSendTime.setText(entity.getDate());
		viewHolder.tvUserName.setText(entity.getName());
		viewHolder.tvReflesh.setVisibility(View.GONE);
		// 用来监听ImageView的宽和高
		viewHolder.tvPicture.setOnMeasureListener(new OnMeasureListener() {

			@Override
			public void onMeasureSize(int width, int height) {
				mPoint.set(width, height);
			}
		});
		
		if (entity.getmsgtype()==1) {
			viewHolder.tvContent.setVisibility(View.GONE);
			viewHolder.tvPicture.setVisibility(View.VISIBLE);

			System.out.println("height is :" + MyApplication.mWindowHeight);
			System.out.println("width is :" + MyApplication.mWindowWidth);
			if (!entity.getPicPath().equals("")) {
				String path = entity.getPicPath();
				String prefix = path.substring(path.lastIndexOf("."));
				Bitmap bitmap = null;
				// 给ImageView设置路径Tag,这是异步加载图片的小技巧
				viewHolder.tvPicture.setTag(path);
				if (prefix.equals(".mp4")) {
					bitmap = ThumbnailUtils.createVideoThumbnail(path,
							Thumbnails.MINI_KIND);
				} else {
					bitmap = ImageProcess.GetBitmapByPath(mContext,
							entity.getPicPath(), MyApplication.mWindowHeight,
							MyApplication.mWindowWidth, 0.15);
					if (bitmap != null) {
						int degree = ImageProcess.getBitmapDegree(entity
								.getPicPath());
						if (degree != 0)
							bitmap = ImageProcess.rotateBitmapByDegree(bitmap,
									degree);
					}
					
					
					if(ImageProcess.checkFileType(path) == ImageProcess.FileType.IMAGE){
						bitmap = NativeImageLoader.getInstance().loadNativeImage(path,
								mPoint, new NativeImageCallBack() {

								@Override
								public void onImageLoader(Bitmap bitmap, String path) {
									ImageView mImageView = (ImageView) mlistView
											.findViewWithTag(path);
									if (bitmap != null && mImageView != null) {
										mImageView.setImageBitmap(bitmap);
									}
								}
							});
					}
					else if (ImageProcess.checkFileType(path) == ImageProcess.FileType.VIDEO) {
						bitmap = ThumbnailUtils.createVideoThumbnail(path,
								Thumbnails.MINI_KIND);
					}
				}
				if (bitmap != null) {
					viewHolder.tvPicture.setImageBitmap(bitmap);
				} else
					viewHolder.tvPicture
							.setImageResource(R.drawable.waitloadpic);
			} else {
				viewHolder.tvPicture.setImageResource(R.drawable.waitloadpic);
				if (isComMsg) {
					viewHolder.tvReflesh.setVisibility(View.VISIBLE);
					viewHolder.tvReflesh.setContentDescription(position + "");
					viewHolder.tvReflesh.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String p = v.getContentDescription().toString();
							if (p != null) {
								int position = Integer.parseInt(p);
								ChatMsgEntity entity = coll.get(position);
								if (GetMsgService.application != null) {
									GetMsgService.application.startDownloadPic(entity.getMessage(), user.getId());
									Toast.makeText(mContext,"开始下载图片，请等待......!", 0).show();
								} 
							}
						}
					});
				}
			}
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

		if (entity.getSendSta() != 1 && !isComMsg) {
			viewHolder.tvReflesh.setVisibility(View.VISIBLE);
			viewHolder.tvReflesh.setContentDescription(position + "");
			viewHolder.tvReflesh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String p = v.getContentDescription().toString();
					if (p != null) {
						int position = Integer.parseInt(p);
						ChatMsgEntity entity = coll.get(position);
						if (GetMsgService.application != null) {
							if (-1 == GetMsgService.application.Resend(
									entity.getMessage(), entity.getmsgtype(),
									entity.getPicPath(), user,
									entity.getDatekey())) {
								Toast.makeText(mContext, "网络连接异常", 0).show();
							}else
								Toast.makeText(mContext,"重新发送消息，请等待......!", 0).show();
						} else
							Toast.makeText(mContext, "服务异常，请重新启动", 0).show();

					}
				}
			});
		}
		viewHolder.icon.setImageResource(imgs[entity.getImg()]);
		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public MyImageView tvPicture;
		public ImageView icon;
		public ImageView tvReflesh;
		public boolean isComMsg = true;
	}

}
