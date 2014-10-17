package com.way.chat.activity;

import java.util.HashMap;
import java.util.Map;

import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import com.yzi.util.UploadUtil;
import com.yzi.util.UploadUtil.OnUploadProcessListener;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


//��Ҫ����ѡ���ļ����ϴ��ļ�����

public class CramerProActivity extends Activity implements OnClickListener,OnUploadProcessListener{
	private static final String TAG = "uploadImage";
	//ȥ�ϴ��ļ�
	protected static final int TO_UPLOAD_FILE = 1;  
	//�ϴ��ļ���Ӧ
	protected static final int UPLOAD_FILE_DONE = 2;  
	//ѡ���ļ�
	public static final int TO_SELECT_PHOTO = 3;
	//�ϴ���ʼ��
	private static final int UPLOAD_INIT_PROCESS = 4;
	//�ϴ���
	private static final int UPLOAD_IN_PROCESS = 5;
	//���������uri
	//private String requestURL ="http://10.0.0.143:8888/AndroidServer/servlet/HttpServlet";
	private static String requestURL = "http://10.0.0.147:8888/MyTest/p/file!upload";
	private Button selectButton,uploadButton,back;
	private ImageView imageView;
	private TextView uploadImageResult;
	static TextView txt;
	private ProgressBar progressBar;
	private ImageButton cramer;
	public static String picPath = null;
	private ProgressDialog progressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camer);
        
        uploadImageResult = (TextView) findViewById(R.id.uploadImageResult);
        txt=(TextView) findViewById(R.id.txt1);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        selectButton = (Button) findViewById(R.id.selectImage);
        uploadButton = (Button) findViewById(R.id.uploadImage);
        imageView = (ImageView) findViewById(R.id.imageView);
        cramer=(ImageButton) findViewById(R.id.camera);        
        back=(Button) findViewById(R.id.back);
        back.setOnClickListener(this);
        cramer.setOnClickListener(this);
        selectButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);                		
        progressDialog = new ProgressDialog(this);
        
    }
    
	 
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.selectImage:
			Intent intent = new Intent(this,SystemCatalogActivity.class);
			startActivity(intent);
			break;
		case R.id.uploadImage:
			if(picPath!=null)
			{
				handler.sendEmptyMessage(TO_UPLOAD_FILE);
			}else{
				Toast.makeText(this, "�ϴ����ļ�·������", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.camera:
			Intent intent1 = new Intent(this,SelectPicActivity.class);
			startActivityForResult(intent1, TO_SELECT_PHOTO);
			break;
		case R.id.back:
			/*AlertDialog.Builder builder = new Builder(CramerProActivity.this); 
			 builder.setIcon(android.R.drawable.ic_dialog_info);
		        builder.setMessage("ȷ��Ҫ�˳�?"); 
		        builder.setTitle("��ʾ"); 
		        builder.setPositiveButton("ȷ��", 
		                new android.content.DialogInterface.OnClickListener() { 
		                    public void onClick(DialogInterface dialog, int which) { 
		                        dialog.dismiss(); 
		                        CramerProActivity.this.finish(); 
		                    } 
		                }); 
		        builder.setNegativeButton("ȡ��", 
		                new android.content.DialogInterface.OnClickListener() { 
		                    public void onClick(DialogInterface dialog, int which) { 
		                        dialog.dismiss(); 
		                    } 
		                }); 
		        		builder.create().show();*/
			finish();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode==Activity.RESULT_OK && requestCode == TO_SELECT_PHOTO){
			//imageView����null, ��һ���ϴ��ɹ��󣬵ڶ�����ѡ���ϴ���ʱ��ᱨ��
			imageView.setImageBitmap(null);
			picPath = data.getStringExtra(SelectPicActivity.KEY_PHOTO_PATH);
			Log.i(TAG, "����ѡ���ͼƬ="+picPath);
			txt.setText("�ļ�·��"+picPath);
			Bitmap bm = BitmapFactory.decodeFile(picPath);
			imageView.setImageBitmap(bm);
	}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * �ϴ���������Ӧ�ص�
	 */
	 
	public void onUploadDone(int responseCode, String message) {
		progressDialog.dismiss();
		Message msg = Message.obtain();
		msg.what = UPLOAD_FILE_DONE;
		msg.arg1 = responseCode;
		msg.obj = message;
		handler.sendMessage(msg);
	}
	
	private void toUploadFile()
	{
		uploadImageResult.setText("�����ϴ���...");
		progressDialog.setMessage("�����ϴ��ļ�...");
		/*uploadImageResult.setText("�����ϴ���...");
		progressDialog.setMessage("��������������ϴ�...");
		progressDialog.setTitle("��Ϣ");
		progressDialog.setIcon(drawable.ic_dialog_info);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(59);
		progressDialog.setIndeterminate(true);*/
		/*progressDialog.setButton("ȷ��", new DialogInterface.OnClickListener(){  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.cancel();               
            }       
        }); */ 
		
		progressDialog.show();
		String fileKey = "img";
		UploadUtil uploadUtil = UploadUtil.getInstance();;
		uploadUtil.setOnUploadProcessListener(this);  //���ü����������ϴ�״̬
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderId", "111");
		uploadUtil.uploadFile( picPath,fileKey, requestURL,params);
	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TO_UPLOAD_FILE:
				toUploadFile();
				break;			
			case UPLOAD_INIT_PROCESS:
				progressBar.setMax(msg.arg1);
				break;
			case UPLOAD_IN_PROCESS:
				progressBar.setProgress(msg.arg1);
				break;
			case UPLOAD_FILE_DONE:
				String result = "��Ӧ�룺"+msg.arg1+"\n��Ӧ��Ϣ��"+msg.obj+"\n��ʱ��"+UploadUtil.getRequestTime()+"��";
				uploadImageResult.setText(result);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};

	 
	public void onUploadProcess(int uploadSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_IN_PROCESS;
		msg.arg1 = uploadSize;
		handler.sendMessage(msg);
	}
	 
	public void initUpload(int fileSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_INIT_PROCESS;
		msg.arg1 = fileSize;
		handler.sendMessage(msg );
	}
	
}