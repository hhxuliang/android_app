package com.kids.activity.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
public class ActionActivity extends MyActivity implements OnClickListener {
	/** Called when the activity is first created. */
	private Button selectButton, back;
	private User user = null;
	private LinearLayout layout_body_activity = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.action);
		user = (User) getIntent().getSerializableExtra("user");
		int idview=getIntent().getIntExtra("subview", -1);
		if(idview==-1)
			return;
		selectButton = (Button) findViewById(R.id.uploadImage);
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		selectButton.setOnClickListener(this);
				View v = View.inflate(this, idview,null);
		((LinearLayout)findViewById(R.id.fatherlayout)).addView(v);
		layout_body_activity = (LinearLayout)findViewById(R.id.bodylayout);

		for (int i = 0; i < layout_body_activity.getChildCount(); i++) {
			if (layout_body_activity.getChildAt(i).getTag() != null
					&& "DateTime".equals(layout_body_activity.getChildAt(i)
							.getTag().toString())) {
				layout_body_activity.getChildAt(i)
						.setContentDescription(i + "");
				layout_body_activity.getChildAt(i).setOnClickListener(
						new OnClickListener() {
							public void onClick(View v) {

								DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
										ActionActivity.this, "");
								int i = Integer.parseInt(""
										+ v.getContentDescription());
								dateTimePicKDialog
										.dateTimePicKDialog((EditText) layout_body_activity
												.getChildAt(i));

							}
						});
			}
		}

		// startDateTime.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		//
		// DateTimePickDialogUtil dateTimePicKDialog = new
		// DateTimePickDialogUtil(
		// LeaveActivity.this, initEndDateTime);
		// dateTimePicKDialog.dateTimePicKDialog(startDateTime);
		//
		// }
		// });
		//
		// endDateTime.setOnClickListener(new OnClickListener() {
		//
		// public void onClick(View v) {
		// DateTimePickDialogUtil dateTimePicKDialog = new
		// DateTimePickDialogUtil(
		// LeaveActivity.this, initEndDateTime);
		// dateTimePicKDialog.dateTimePicKDialog(endDateTime);
		// }
		// });
	}

	private String toStr() {
		String str = "";

		for (int i = 0; i < layout_body_activity.getChildCount(); i++) {
			if(layout_body_activity.getChildAt(i) instanceof EditText)
				str = str
						+ ((EditText) layout_body_activity.getChildAt(i))
								.getText().toString() + "\n";
			else if (layout_body_activity.getChildAt(i) instanceof TextView)
				str = str
						+ ((TextView) layout_body_activity.getChildAt(i))
								.getText().toString() + "\n";
		}
		if(!str.equals(""))
			str=str.substring(0,str.length()-1);
		return str;
	}

	public void onClick(View v) {
		String str = toStr();
		switch (v.getId()) {
		case R.id.uploadImage:
			application.send(str, 3, "", user);
			finish();
			break;
		case R.id.back:
			finish();
			break;
		}
	}
}
