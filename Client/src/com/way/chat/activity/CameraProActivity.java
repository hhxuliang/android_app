package com.way.chat.activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import com.way.chat.common.util.Constants;
import com.way.util.SharePreferenceUtil;
import com.yzi.util.UploadUtil;
import com.yzi.util.UploadUtil.OnUploadProcessListener;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



//主要用于选择文件和上传文件操作

public class CameraProActivity extends Activity implements OnClickListener,OnUploadProcessListener{
	private static final String TAG = "uploadImage";
	//去上传文件
	protected static final int TO_UPLOAD_FILE = 1;  
	//上传文件响应
	protected static final int UPLOAD_FILE_DONE = 2;  
	//选择文件
	public static final int TO_SELECT_PHOTO = 3;
	//上传初始化
	private static final int UPLOAD_INIT_PROCESS = 4;
	//上传中
	private static final int UPLOAD_IN_PROCESS = 5;
	//请求服务器uri
	//private String requestURL ="http://10.0.0.143:8888/AndroidServer/servlet/HttpServlet";
	private static String requestURL = "http://10.0.0.147:8888/MyTest/p/file!upload";
	private Button selectButton,uploadButton,back;
	private ImageView imageView;
	private TextView uploadImageResult;
	static TextView txt;
	private ProgressBar progressBar;
	private ImageButton cramer;
	private ImageButton imagefile;
	public static String picPath = null;
	private ProgressDialog progressDialog;
	private Uri photoUri;
	//使用照相机拍照获取图片
	public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
	//使用相册中的图片
	public static final int SELECT_PIC_BY_PICK_PHOTO = 2;
	private MyApplication application;
	
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
      imagefile=(ImageButton) findViewById(R.id.imagefile);   
      back=(Button) findViewById(R.id.back);
      back.setOnClickListener(this);
      imagefile.setOnClickListener(this);
      cramer.setOnClickListener(this);
      selectButton.setOnClickListener(this);
      uploadButton.setOnClickListener(this);                		
      progressDialog = new ProgressDialog(this);
      application = (MyApplication) this.getApplicationContext();
      picPath = application.getCameraPath() + "/upload.jpg";
      
  }
  
  private void takePhoto() {
		//执行拍照前，应该先判断SD卡是否存在
		
		String SDState = Environment.getExternalStorageState();
		if(SDState.equals(Environment.MEDIA_MOUNTED))
		{
			File file = new File(picPath);  
			
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//"android.media.action.IMAGE_CAPTURE"
			/***
			 * 需要说明一下，以下操作使用照相机拍照，拍照后的图片会存放在相册中的
			 * 这里使用的这种方式有一个好处就是获取的图片是拍照后的原图
			 * 如果不实用ContentValues存放照片路径的话，拍照后获取的图片为缩略图不清晰
			 */
			photoUri = Uri.fromFile(file);
			intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
			intent.putExtra("return-data", true);
			/**-----------------*/
			startActivityForResult(intent, SELECT_PIC_BY_TACK_PHOTO);
		}else{
			Toast.makeText(this,"内存卡不存在", Toast.LENGTH_LONG).show();
		}
	}
    /***
	 * 从相册中取图片
	 */
	private void pickPhoto() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);
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
				Toast.makeText(this, "上传的文件路径出错", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.camera:
			takePhoto();
			break;
		case R.id.imagefile:
			pickPhoto();
			break;
		case R.id.back:
			imageView.setImageBitmap(null);
			finish();
			break;
		}
	}
	@Override
	public void onBackPressed() {// 捕获返回按键事件，进入后台运行
		imageView.setImageBitmap(null);
		finish();// 再结束自己
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode==Activity.RESULT_OK ){
			//picPath = photoUri.toString().replace("file:///", "/");
			doPhoto(requestCode,data);
			//imageView不设null, 第一次上传成功后，第二次在选择上传的时候会报错。
			//imageView.setImageBitmap(null);
			//picPath = data.getStringExtra(SelectPicActivity.KEY_PHOTO_PATH);
			//Log.i(TAG, "最终选择的图片="+picPath);
			//txt.setText("文件路径"+picPath);
			//Bitmap bm = BitmapFactory.decodeFile(picPath);
			//Bitmap bitmap = (Bitmap) data.getExtras().get("data");
			//imageView.setImageBitmap(bitmap);
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 选择图片后，获取图片的路径
	 * @param requestCode
	 * @param data
	 */
	private void doPhoto(int requestCode,Intent data){
		if(requestCode == SELECT_PIC_BY_PICK_PHOTO )  //从相册取图片，有些手机有异常情况，请注意
		{
			if(data == null){
				Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
				return;
			}
			photoUri = data.getData();
			if(photoUri == null ){
				Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
				return;
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
		}
	}
	/**
	 * 上传服务器响应回调
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
		uploadImageResult.setText("正在上传中...");
		progressDialog.setMessage("正在上传文件...");
		/*uploadImageResult.setText("正在上传中...");
		progressDialog.setMessage("正在请求服务器上传...");
		progressDialog.setTitle("信息");
		progressDialog.setIcon(drawable.ic_dialog_info);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(59);
		progressDialog.setIndeterminate(true);*/
		/*progressDialog.setButton("确认", new DialogInterface.OnClickListener(){  
          public void onClick(DialogInterface dialog, int which) {  
              dialog.cancel();               
          }       
      }); */ 
		
		progressDialog.show();
		String fileKey = "img";
		UploadUtil uploadUtil = UploadUtil.getInstance();;
		uploadUtil.setOnUploadProcessListener(this);  //设置监听器监听上传状态
		
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
				String result = "响应码："+msg.arg1+"\n响应信息："+msg.obj+"\n耗时："+UploadUtil.getRequestTime()+"秒";
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
	
	@Override
	protected void onResume() {
		imageView.setImageBitmap(null);
		Bitmap bm = BitmapFactory.decodeFile(picPath);
		imageView.setImageBitmap(bm);
		super.onResume();
	}
	
}