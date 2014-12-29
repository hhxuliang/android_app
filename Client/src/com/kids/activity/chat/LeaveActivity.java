package com.kids.activity.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kids.util.DateTimePickDialogUtil;
import com.kids.util.DialogFactory;
import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import com.way.chat.common.bean.User;
import com.way.chat.common.util.MyDate;
/**
 * ʱ��ʰȡ������
 * 
 * @author wwj_748
 * 
 */
public class LeaveActivity extends MyActivity implements OnClickListener{
	/** Called when the activity is first created. */
	private EditText startDateTime;
	private EditText endDateTime;

	private String initStartDateTime=""; 
	private String initEndDateTime ="";
	private Button selectButton, back;
	private User user=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leave);
		selectButton = (Button) findViewById(R.id.uploadImage);
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		selectButton.setOnClickListener(this);
		initStartDateTime = MyDate.getDateCNShort() ; 
		initEndDateTime = MyDate.getDateCNShort(); 
		// ���������
		startDateTime = (EditText) findViewById(R.id.inputDate);
		endDateTime = (EditText) findViewById(R.id.inputDate2);

		startDateTime.setText(initStartDateTime);
		endDateTime.setText(initEndDateTime);
		user = (User) getIntent().getSerializableExtra("user");
		startDateTime.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
						LeaveActivity.this, initEndDateTime);
				dateTimePicKDialog.dateTimePicKDialog(startDateTime);

			}
		});

		endDateTime.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
						LeaveActivity.this, initEndDateTime);
				dateTimePicKDialog.dateTimePicKDialog(endDateTime);
			}
		});
	}
	
	public void onClick(View v) {
		String str="";
		str="开始时间:\n" + startDateTime.getText().toString() + "\n";
		str =  str + "结束时间:\n" + endDateTime.getText().toString();
		switch (v.getId()) {
		case R.id.uploadImage:
			application.send(str, false, "", user);
			finish();
			break;
		case R.id.back:
			finish();
			break;
		}
	}
}
