package com.way.chat.activity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
 private ArrayList<String> mNameList = new ArrayList<String>();
 private ArrayList<Drawable> mDrawableList = new ArrayList<Drawable>();
 private LayoutInflater mInflater;
 private Context mContext;
 LinearLayout.LayoutParams params;

 public GridAdapter(Context context, ArrayList<String> nameList, ArrayList<Drawable> drawableList) {
  mNameList = nameList;
  mDrawableList = drawableList;
  mContext = context;
  mInflater = LayoutInflater.from(context);
  
  params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
  params.gravity = Gravity.CENTER;
 }

 public int getCount() {
  return mNameList.size();
 }

 public Object getItem(int position) {
  return mNameList.get(position);
 }

 public long getItemId(int position) {
  return position;
 }

 public View getView(int position, View convertView, ViewGroup parent) {
  ItemViewTag viewTag;
  
  if (convertView == null)
  {
   convertView = mInflater.inflate(R.layout.upload_pic_item, null);
   
   // construct an item tag
   viewTag = new ItemViewTag((ImageView) convertView.findViewById(R.id.ItemImage), (TextView) convertView.findViewById(R.id.ItemText));
   convertView.setTag(viewTag);
  } else
  {
   viewTag = (ItemViewTag) convertView.getTag();
  }
  
  // set name
  viewTag.mName.setText(mNameList.get(position));
  
  // set icon
  viewTag.mIcon.setBackgroundDrawable(mDrawableList.get(position));
  viewTag.mIcon.setLayoutParams(params);
  return convertView;
 }
 
 class ItemViewTag
 {
  protected ImageView mIcon;
  protected TextView mName;
  
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
  public ItemViewTag(ImageView icon, TextView name)
  {
   this.mName = name;
   this.mIcon = icon;
  }
 }

}