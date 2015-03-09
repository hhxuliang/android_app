package com.kids.activity.imagescan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;

import com.kids.activity.imagescan.MyImageView.OnMeasureListener;
import com.kids.activity.imagescan.NativeImageLoader.NativeImageCallBack;
import com.kids.util.ImageProcess;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;

public class ChildAdapter extends BaseAdapter {
	private Point mPoint = new Point(0, 0);// 用来封装ImageView的宽和高的对象
	/**
	 * 用来存储图片的选中情况
	 */
	private HashMap<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
	private GridView mGridView;
	private List<String> list;
	protected LayoutInflater mInflater;

	public ChildAdapter(Context context, List<String> list, GridView mGridView) {
		this.list = list;
		this.mGridView = mGridView;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		String path = list.get(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.grid_child_item, null);
			viewHolder = new ViewHolder();
			viewHolder.mImageView = (MyImageView) convertView
					.findViewById(R.id.child_image);
			viewHolder.mCheckBox = (CheckBox) convertView
					.findViewById(R.id.child_checkbox);
			viewHolder.tvVideo= (ImageView) convertView
					.findViewById(R.id.ItemImage_video);
			viewHolder.tvVideo.setVisibility(View.GONE);
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
		viewHolder.mImageView.setTag(path);
		viewHolder.mCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// 如果是未选中的CheckBox,则添加动画
						if (!mSelectMap.containsKey(position)
								|| !mSelectMap.get(position)) {
							addAnimation(viewHolder.mCheckBox);
						}
						mSelectMap.put(position, isChecked);
					}
				});

		viewHolder.mCheckBox
				.setChecked(mSelectMap.containsKey(position) ? mSelectMap
						.get(position) : false);
		Bitmap bitmap = null;
		if (ImageProcess.checkFileType(path) == ImageProcess.FileType.IMAGE) {
			// 利用NativeImageLoader类加载本地图片
			bitmap = NativeImageLoader.getInstance("small").loadNativeImage(path,
					new Point(150, 150), new NativeImageCallBack() {

						@Override
						public void onImageLoader(Bitmap bitmap, String path) {
							ImageView mImageView = (ImageView) mGridView
									.findViewWithTag(path);
							if (bitmap != null && mImageView != null) {
								mImageView.setImageBitmap(bitmap);
							}
						}
					});
		} else if (ImageProcess.checkFileType(path) == ImageProcess.FileType.VIDEO) {
			bitmap = ThumbnailUtils.createVideoThumbnail(path,
					Thumbnails.MINI_KIND);
			viewHolder.tvVideo.setVisibility(View.GONE);
		}
		if (bitmap != null) {
			viewHolder.mImageView.setImageBitmap(bitmap);
		} else {
			viewHolder.mImageView
					.setImageResource(R.drawable.friends_sends_pictures_no);
		}

		return convertView;
	}

	/**
	 * 给CheckBox加点击动画，利用开源库nineoldandroids设置动画
	 * 
	 * @param view
	 */
	private void addAnimation(View view) {
		float[] vaules = new float[] { 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f,
				1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f };
		AnimatorSet set = new AnimatorSet();
		set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
				ObjectAnimator.ofFloat(view, "scaleY", vaules));
		set.setDuration(150);
		set.start();
	}

	/**
	 * 获取选中的Item的position
	 * 
	 * @return
	 */
	public ArrayList<String> getSelectItems() {
		ArrayList<String> lists = new ArrayList<String>();
		for (Iterator<Map.Entry<Integer, Boolean>> it = mSelectMap.entrySet()
				.iterator(); it.hasNext();) {
			Map.Entry<Integer, Boolean> entry = it.next();
			if (entry.getValue()) {
				lists.add(list.get(entry.getKey()));
			}
		}

		return lists;
	}

	public void clearSelectItems() {
		mSelectMap.clear();
	}

	public static class ViewHolder {
		public MyImageView mImageView;
		public CheckBox mCheckBox;
		public ImageView tvVideo;
	}

}
