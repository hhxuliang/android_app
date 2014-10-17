package com.way.chat.activity;

import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

//ѡ���ļ�������
public class SelectPicActivity extends Activity implements OnClickListener{

	//ʹ����������ջ�ȡͼƬ
	public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
	//ʹ������е�ͼƬ
	public static final int SELECT_PIC_BY_PICK_PHOTO = 2;
	//��Intent��ȡͼƬ·����KEY
	public static final String KEY_PHOTO_PATH = "photo_path";	
	private static final String TAG = "SelectPicActivity";	
	private LinearLayout dialogLayout;
	private Button takePhotoBtn,pickPhotoBtn,cancelBtn;

	/**��ȡ����ͼƬ·��*/
	private String picPath;
	private Intent lastIntent ;	
	private Uri photoUri;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_pic_layout);
		
		dialogLayout = (LinearLayout) findViewById(R.id.dialog_layout);
		dialogLayout.setOnClickListener(this);
		takePhotoBtn = (Button) findViewById(R.id.btn_take_photo);
		takePhotoBtn.setOnClickListener(this);
		pickPhotoBtn = (Button) findViewById(R.id.btn_pick_photo);
		pickPhotoBtn.setOnClickListener(this);
		cancelBtn = (Button) findViewById(R.id.btn_cancel);
		cancelBtn.setOnClickListener(this);	
		lastIntent = getIntent();
	}

	 
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_layout:
			finish();
			break;
		case R.id.btn_take_photo:
			takePhoto();
			break;
		case R.id.btn_pick_photo:
			pickPhoto();
			break;
		default:
			finish();
			break;
		}
	}

	/**
	 * ���ջ�ȡͼƬ
	 */
	private void takePhoto() {
		//ִ������ǰ��Ӧ�����ж�SD���Ƿ����
		String SDState = Environment.getExternalStorageState();
		if(SDState.equals(Environment.MEDIA_MOUNTED))
		{
			
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//"android.media.action.IMAGE_CAPTURE"
			/***
			 * ��Ҫ˵��һ�£����²���ʹ����������գ����պ��ͼƬ����������е�
			 * ����ʹ�õ����ַ�ʽ��һ���ô����ǻ�ȡ��ͼƬ�����պ��ԭͼ
			 * �����ʵ��ContentValues�����Ƭ·���Ļ������պ��ȡ��ͼƬΪ����ͼ������
			 */
			ContentValues values = new ContentValues();  
			photoUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
			/**-----------------*/
			startActivityForResult(intent, SELECT_PIC_BY_TACK_PHOTO);
		}else{
			Toast.makeText(this,"�ڴ濨������", Toast.LENGTH_LONG).show();
		}
	}
				
	
	
	/***
	 * �������ȡͼƬ
	 */
	private void pickPhoto() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return super.onTouchEvent(event);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK)
		{
			doPhoto(requestCode,data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * ѡ��ͼƬ�󣬻�ȡͼƬ��·��
	 * @param requestCode
	 * @param data
	 */
	private void doPhoto(int requestCode,Intent data){
		if(requestCode == SELECT_PIC_BY_PICK_PHOTO )  //�����ȡͼƬ����Щ�ֻ����쳣�������ע��
		{
			if(data == null){
				Toast.makeText(this, "ѡ��ͼƬ�ļ�����", Toast.LENGTH_LONG).show();
				return;
			}
			photoUri = data.getData();
			if(photoUri == null ){
				Toast.makeText(this, "ѡ��ͼƬ�ļ�����", Toast.LENGTH_LONG).show();
				return;
			}
		}
		String[] pojo = {MediaStore.Images.Media.DATA};
		Cursor cursor = managedQuery(photoUri, pojo, null, null,null);   
		if(cursor != null ){
			int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
			cursor.moveToFirst();
			picPath = cursor.getString(columnIndex);
			cursor.close();
		}
		Log.i(TAG, "imagePath = "+picPath);
		/*if(picPath != null && ( picPath.endsWith(".png") || picPath.endsWith(".PNG") ||picPath.endsWith(".jpg") ||picPath.endsWith(".JPG")  ))*/
		if(picPath !=null){
			lastIntent.putExtra(KEY_PHOTO_PATH, picPath);
			setResult(Activity.RESULT_OK, lastIntent);
			finish();
		}else{
			Toast.makeText(this, "ѡ���ļ�����ȷ!", Toast.LENGTH_LONG).show();
			
		}
	}
}
