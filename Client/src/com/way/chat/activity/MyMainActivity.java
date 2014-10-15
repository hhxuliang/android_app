package com.way.chat.activity;

import com.way.chat.common.tran.bean.TranObject;
import com.way.chat.common.util.Constants;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

public class MyMainActivity extends Activity {
    /** Called when the activity is first created. */
	MyImageView joke;
	TranObject msg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_home);
        
        joke=(MyImageView) findViewById(R.id.c_joke);
        msg = (TranObject) getIntent().getSerializableExtra(Constants.MSGKEY);// 从intent中取出消息对象	
        joke.setOnClickIntent(new MyImageView.OnViewClick() {
			@Override
			public void onClick() {
				Intent i = new Intent(MyMainActivity.this,
						FriendListActivity.class);
				i.putExtra(Constants.MSGKEY, msg);
				startActivity(i);
			}
		});
    }
}