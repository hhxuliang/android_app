package com.kids.activity.imagescan;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.kids.activity.imagescan.MyImageView.OnMeasureListener;
import com.kids.activity.imagescan.NativeImageLoader.NativeImageCallBack;
import com.kids.util.ImageProcess;

import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;

public class GroupAdapter extends BaseAdapter {
	private List<ImageBean> list;
	private Point mPoint = new Point(0, 0);// 用来封装ImageView的宽和高的对象
	private GridView mGridView;
	protected LayoutInflater mInflater;

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	public void resetList() {
		list.clear();
		this.notifyDataSetInvalidated();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public GroupAdapter(Context context, List<ImageBean> list,
			GridView mGridView) {
		this.list = list;
		this.mGridView = mGridView;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		ImageBean mImageBean = list.get(position);
		String path = mImageBean.getTopImagePath();
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.grid_group_item, null);
			viewHolder.mImageView = (MyImageView) convertView
					.findViewById(R.id.group_image);
			viewHolder.mTextViewTitle = (TextView) convertView
					.findViewById(R.id.group_title);
			viewHolder.mTextViewCounts = (TextView) convertView
					.findViewById(R.id.group_count);

			// 用来监听ImageView的宽和高
			viewHolder.mImageView.setOnMeasureListener(new OnMeasureListener() {

				@Override
				public void onMeasureSize(int width, int height) {
					mPoint.set(width, height);
				}
			});

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.mImageView
					.setImageResource(R.drawable.friends_sends_pictures_no);
		}

		viewHolder.mTextViewTitle.setText(mImageBean.getFolderName());
		viewHolder.mTextViewCounts.setText(Integer.toString(mImageBean
				.getImageCounts()));
		// 给ImageView设置路径Tag,这是异步加载图片的小技巧
		viewHolder.mImageView.setTag(path);

		// 利用NativeImageLoader类加载本地图片
		Bitmap bitmap=null;
		if(ImageProcess.checkFileType(path) == ImageProcess.FileType.IMAGE){
		bitmap = NativeImageLoader.getInstance("group").loadNativeImage(path,
				mPoint, new NativeImageCallBack() {

					@Override
					public void onImageLoader(Bitmap bitmap, String path) {
						ImageView mImageView = (ImageView) mGridView
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
		if (bitmap != null) {
			viewHolder.mImageView.setImageBitmap(bitmap);
		} else {
			viewHolder.mImageView
					.setImageResource(R.drawable.friends_sends_pictures_no);
		}

		return convertView;
	}

	public static class ViewHolder {
		public MyImageView mImageView;
		public TextView mTextViewTitle;
		public TextView mTextViewCounts;
	}

}
