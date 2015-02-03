package com.kids.activity.chat;

import java.util.ArrayList;
import java.util.HashMap;

import com.kids.util.ImageProcess;
import com.kids.util.ZoomImageView;
import com.way.chat.activity.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
	ArrayList<HashMap<String, Object>> mDataList;
	private LayoutInflater mInflater;
	private Context mContext;
	private GridAdapter mAdapterThis;

	public GridAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
		mDataList = data;
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mAdapterThis = this;
	}

	public int getCount() {
		return mDataList.size();
	}

	public Object getItem(int position) {
		return mDataList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ItemViewTag viewTag;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.upload_pic_item, null);

			// construct an item tag
			viewTag = new ItemViewTag(
					(ImageView) convertView.findViewById(R.id.ItemImage),
					(TextView) convertView.findViewById(R.id.ItemText),
					(ImageView) convertView.findViewById(R.id.ItemImage_video));
			convertView.setTag(viewTag);
		} else {
			viewTag = (ItemViewTag) convertView.getTag();
		}

		// set name
		viewTag.mName.setText((String) mDataList.get(position).get("ItemText"));

		// set icon
		viewTag.mIcon.setImageBitmap((Bitmap) mDataList.get(position).get(
				"ItemImage"));
		// viewTag.mIcon.setLayoutParams(params);
		if (((String) mDataList.get(position).get("ItemPath")).equals("")) {
			((ImageView) convertView.findViewById(R.id.ItemImage_del))
					.setVisibility(View.GONE);
		} else {
			viewTag.mIcon.setContentDescription((String) mDataList
					.get(position).get("ItemActualPath"));
			ImageView iv = (ImageView) convertView
					.findViewById(R.id.ItemImage_del);
			iv.setVisibility(View.VISIBLE);
			iv.setContentDescription(position + "");
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mDataList.remove(Integer.parseInt(v.getContentDescription()
							.toString()));
					mAdapterThis.notifyDataSetChanged();
				}
			});
			String path = (String) mDataList.get(position)
					.get("ItemActualPath");
			if (ImageProcess.checkFileType(path) == ImageProcess.FileType.VIDEO) {
				viewTag.ItemImage_video.setVisibility(View.VISIBLE);
				
			} else
				viewTag.ItemImage_video.setVisibility(View.GONE);
		}
		return convertView;
	}

	class ItemViewTag {
		protected ImageView mIcon;
		protected TextView mName;
		protected ImageView ItemImage_video;

		/**
		 * The constructor to construct a navigation view tag
		 * 
		 * @param name
		 *            the name view of the item
		 * @param size
		 *            the size view of the item
		 * @param icon
		 *            the icon view of the item
		 */
		public ItemViewTag(ImageView icon, TextView name, ImageView v) {
			this.mName = name;
			this.mIcon = icon;
			this.ItemImage_video = v;
		}
	}

}