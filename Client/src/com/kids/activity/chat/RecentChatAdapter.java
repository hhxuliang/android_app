package com.kids.activity.chat;

import java.util.ArrayList;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kids.util.ExpressionUtil;
import com.way.chat.activity.R;
import com.way.chat.common.bean.User;

public class RecentChatAdapter extends BaseAdapter {
	private Context context;
	private LinkedList<RecentChatEntity> list;
	private MyApplication application;
	private LayoutInflater inflater;
	

	public RecentChatAdapter(Context context, LinkedList<RecentChatEntity> list) {
		// TODO Auto-generated constructor stub
		this.context = context;
		application = (MyApplication) context.getApplicationContext();
		this.list = list;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// 通过对象移除
	public void remove(RecentChatEntity entity) {
		list.remove(entity);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.recent_chat_item, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView
					.findViewById(R.id.recent_userhead);
			holder.icon_more = (ImageView) convertView
					.findViewById(R.id.imageView_item_more_msg);
			holder.name = (TextView) convertView.findViewById(R.id.recent_name);
			holder.date = (TextView) convertView.findViewById(R.id.recent_time);
			holder.msg = (TextView) convertView.findViewById(R.id.recent_msg);
			holder.count = (TextView) convertView
					.findViewById(R.id.recent_new_num);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final RecentChatEntity entity = list.get(position);
		holder.icon.setImageResource(MyApplication.imgs[entity.getImg()]);
		holder.name.setText(entity.getName());
		holder.name.setTextColor(Color.BLACK);
		holder.date.setText(entity.getTime());
		holder.date.setTextColor(Color.BLACK);
		
		String str = entity.getMsg(); // æ¶ˆæ�¯å…·ä½“å†…å®¹
		String zhengze = "f0[0-9]{2}|f10[0-7]"; // æ­£åˆ™è¡¨è¾¾å¼�ï¼Œç”¨æ�¥åˆ¤æ–­æ¶ˆæ�¯å†…æ˜¯å�¦æœ‰è¡¨æƒ…
		try {
			SpannableString spannableString = ExpressionUtil
					.getExpressionString(context, str, zhengze);
			holder.msg.setText(spannableString);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (entity.getMsgtype()==1)
			holder.msg.setText("[图片]");
		if (entity.getMsgtype()==2)
			holder.msg.setText("[语音]");
		
		holder.msg.setTextColor(Color.BLACK);
		if (entity.getCount() > 0) {
			holder.count.setText(entity.getCount() + "");
			holder.count.setTextColor(Color.BLACK);
		} else {
			holder.count.setVisibility(View.INVISIBLE);// 如果没有消息，就隐藏此view
		}
		// 点击事件
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 下面是切换到聊天界面处理
				User u = new User();
				u.setName(entity.getName());
				u.setId(entity.getId());
				u.setImg(entity.getImg());
				u.setIsCrowd(entity.getUsertype());
				Intent intent = new Intent(context, ChatActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("user", u);
				context.startActivity(intent);
				// Toast.makeText(Tab2.this, "开始聊天", 0).show();
				application.setRecentNum(0);

			}
		});
		
		holder.icon_more.setVisibility(View.INVISIBLE );
		if(application.getNotReadmsslist() !=null)
		{
			for(String s:application.getNotReadmsslist())
			{
				if (s.equals(entity.getId()+""))
				{
					holder.icon_more.setVisibility(View.VISIBLE );
				}
			}
		}
		return convertView;
	}

	static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView date;
		public TextView msg;
		public TextView count;
		public ImageView icon_more;
	}
}
