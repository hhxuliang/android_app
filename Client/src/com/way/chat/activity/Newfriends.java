package com.way.chat.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.way.chat.activity.MyAdapter.ViewHolder;
import com.way.chat.common.bean.AddNewFriendMsg;
import com.way.chat.common.bean.TextMessage;
import com.way.chat.common.bean.User;
import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.tran.bean.TranObjectType;
import com.way.chat.common.util.Constants;
import com.way.client.Client;
import com.way.client.ClientInputThread;
import com.way.client.ClientOutputThread;
import com.way.client.MessageListener;
import com.way.util.DialogFactory;
import com.way.util.Encode;
import com.way.util.GroupFriend;
import com.way.util.MessageDB;
import com.way.util.MyDate;
import com.way.util.SharePreferenceUtil;
import com.way.util.UserDB;

/**
 * 显示所有用户列表的Activity
 * 
 * @author way
 * 
 */
public class Newfriends extends MyActivity implements OnClickListener {
	private ListView mAllUserLV;
	private MyApplication application;
	List<User> newFriendlist = new ArrayList<User>();  
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.newfriend);
		mAllUserLV = (ListView) findViewById(R.id.listViewAllUsers);
		application = (MyApplication) this.getApplicationContext();
		  
		mAllUserLV.setItemsCanFocus(false);    
		mAllUserLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);    
		
		mAllUserLV.setOnItemClickListener(new OnItemClickListener(){    
            @Override    
            public void onItemClick(AdapterView<?> parent, View view,    
                    int position, long id) {    
                ViewHolder vHollder = (ViewHolder) view.getTag();    
 //在每次获取点击的item时将对于的checkbox状态改变，同时修改map的值。    
                vHollder.cBox.toggle();    
                MyAdapter.isSelected.put(position, vHollder.cBox.isChecked());    
            }    
        });
    	
		Button btn=(Button)findViewById(R.id.button_ok);
        btn.setOnClickListener(new OnClickListener(){
        	
		@Override
		public void onClick(View v) {
			for(int i=0;i<mAllUserLV.getCount();i++){    
                if(MyAdapter.isSelected.get(i)){    
                    ViewHolder vHollder = (ViewHolder) mAllUserLV.getChildAt(i).getTag();    
                    newFriendlist.add(new User(Integer.parseInt((String)vHollder.id.getText()),(String)vHollder.title.getText()));    
                }    
            } 
			if(newFriendlist.size()>0)
			{
				addFriends();
			}else
			{
				Toast.makeText(Newfriends.this,
						"请选择您想添加的好友!", 0)
						.show();
			}
			
		}});
        
        Button btn1=(Button)findViewById(R.id.button_cancel);
        btn1.setOnClickListener(new OnClickListener(){
 
		@Override
		public void onClick(View v) {
			finish();
		}});
		getAllUser();
	}
	
	@Override
	// 依据自己需求处理父类广播接收者收取到的消息
	public void getMessage(TranObject msg) {
		if (msg != null) {
			//System.out.println("ADDFRIENDS:" + msg);
			switch (msg.getType()) {
			case ALLUSERS:// OK
				List<User> list = (List<User>) msg.getObject();
				if (list!=null && list.size() > 0) {
					
					MyAdapter adapter=new MyAdapter(this,list);  
					mAllUserLV.setAdapter(adapter);  
					
					if (mDialog.isShowing())
						mDialog.dismiss();
				} else {
					DialogFactory.ToastDialog(Newfriends.this, "QQ添加好友",
							"亲！添加好友失败哦");
					if (mDialog.isShowing())
						mDialog.dismiss();
				}
				break;
			case ISOK:
			case ISERROR:
				if (mDialog.isShowing())
					mDialog.dismiss();
				SharePreferenceUtil util = new SharePreferenceUtil(
						Newfriends.this, Constants.SAVE_USER);
				TextMessage tm = (TextMessage) msg.getObject();
				String message = tm.getMessage();
				Toast.makeText(Newfriends.this,
						"您有新的消息来自：" + msg.getFromUser() + ":" + message, 0)
						.show();

				if (msg.getType()==TranObjectType.ISOK) {
					Intent intent = new Intent(); 
					intent.putExtra("newfriends",(Serializable)newFriendlist); 
					this.setResult(RESULT_OK, intent); // 设置结果数据  
					finish();
				} 
				
				break;
				
			default:
				break;
			}
		}
	}
	
	private List getData(List<User> list)
	{   
		List data = new ArrayList(); 
		for (User u : list) {
			data.add(u.getName());  
		}
		return data;   
	}
	/**
	 * 获取所有用户信息
	 */
	private void getAllUser() {
		showRequestDialog();
		// 通过Socket获取信息
		Client client = application.getClient();
		ClientOutputThread out = client.getClientOutputThread();
		TranObject<User> o = new TranObject<User>(TranObjectType.ALLUSERS);
		SharePreferenceUtil util = new SharePreferenceUtil(
				Newfriends.this, Constants.SAVE_USER);
		User u = new User();
		u.setId(Integer.parseInt(util.getId()));
		o.setObject(u);
		out.setMsg(o);
	}
	/**
	 * 添加新朋友
	 */
	private void addFriends() {
		showRequestDialog();
		// 通过Socket获取信息
		Client client = application.getClient();
		ClientOutputThread out = client.getClientOutputThread();
		TranObject<AddNewFriendMsg> o = new TranObject<AddNewFriendMsg>(TranObjectType.ADDFRIEND);
		SharePreferenceUtil util = new SharePreferenceUtil(
				Newfriends.this, Constants.SAVE_USER);
		AddNewFriendMsg u = new AddNewFriendMsg();
		u.setUserID(util.getId());
		for(User s:newFriendlist)
			u.addNewFriendID(""+s.getId());
		o.setObject(u);
		out.setMsg(o);
	}
	private Dialog mDialog = null;

	private void showRequestDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = DialogFactory.creatRequestDialog(this, "正在添加好友...");
		mDialog.show();
	}
	
	@Override
	public void onClick(View v) {
		
	}

}
