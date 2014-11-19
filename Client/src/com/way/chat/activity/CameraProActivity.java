package com.way.chat.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import com.way.chat.common.util.Constants;
import com.way.util.DialogFactory;
import com.way.util.SharePreferenceUtil;
import com.yzi.util.UploadUtil;
import com.yzi.util.UploadUtil.OnUploadProcessListener;
import com.zoom.ZoomImageView;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
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
	private static String requestURL = "";
	private Button selectButton,back;
	private EditText progressText;
	private ProgressBar progressBar;
	public static String picPath = null;
	private String pic_path_save = null;
	private ProgressDialog progressDialog;
	private Uri photoUri;
	private Context ct = null;
	//使用照相机拍照获取图片
	public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
	//使用相册中的图片
	public static final int SELECT_PIC_BY_PICK_PHOTO = 2;
	private MyApplication application;
	private SimpleAdapter saMenuItem;
	private ArrayList<HashMap<String, Object>> meumList;
	private int pic_NO = 0;
	private int total_pic=0;
	private int upload_ok_pic=0;
	ArrayList<String> ap;
	ArrayList<String> alp;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.camer);
      progressBar = (ProgressBar) findViewById(R.id.progressBar1);
      progressText = (EditText) findViewById(R.id.progressText);
      selectButton = (Button) findViewById(R.id.uploadImage);
      back=(Button) findViewById(R.id.back);
      back.setOnClickListener(this);
      selectButton.setOnClickListener(this);
      progressDialog = new ProgressDialog(this);
      application = (MyApplication) this.getApplicationContext();
      picPath = application.getCameraPath() + "/upload";
      pic_path_save = application.getPicPath();
      //picPath = "/mnt/sdcard/children/camerapicpath/upload";
      requestURL = "http://" + Constants.SERVER_IP + ":8080" +  "/Server/UploadFile";
      ap=new ArrayList<String>();
	  alp=new ArrayList<String>();
	  ct = this;
	  progressBar.setVisibility(View.GONE);
	  progressText.setVisibility(View.GONE);
      init_pic_grid();
  }
  
  private void init_pic_grid()
  {
	    GridView gridview = (GridView) findViewById(R.id.GridView_upload_pic); 
		meumList = new ArrayList<HashMap<String, Object>>(); 
		
		saMenuItem = new SimpleAdapter(this, 
		  meumList, //数据源 
		  R.layout.upload_pic_item, //xml实现 
		  new String[]{"ItemImage","ItemText"}, //对应map的Key 
		  new int[]{R.id.ItemImage,R.id.ItemText});  //对应R的Id 
		
		  saMenuItem.setViewBinder(new ViewBinder(){    
		    
	          public boolean setViewValue(View view, Object data,     
                  String textRepresentation) {     
                  //判断是否为我们要处理的对象      
                  if(view instanceof ImageView && data instanceof Bitmap){     
	                  ImageView iv = (ImageView) view;     
	                  iv.setImageBitmap((Bitmap) data); 
	                  
	                  return true;     
	                }else     
	                return false;     
	              }     
          });   
		//添加Item到网格中 
		gridview.setAdapter(saMenuItem); 
		gridview.setOnItemClickListener(new OnItemClickListener() { 
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) { 
					System.out.println("click index:"+arg2); 
					HashMap<String, Object> item=(HashMap<String, Object>) arg0.getItemAtPosition(arg2);  
					if(((String)item.get("ItemText")).equals(pic_NO+""))
						takePhoto(picPath + (String)item.get("ItemText") + ".jpg");
					else
					{

						ImageView image = (ImageView)arg1.findViewById(R.id.ItemImage);
						image.setDrawingCacheEnabled(true);
      	                Bitmap bitmap = image.getDrawingCache();
  	                    ZoomImageView zoom = new ZoomImageView(ct, bitmap);
  		                zoom.showZoomView();
      	            }
				} 
			}	 
		); 
		 
		HashMap<String, Object> map = new HashMap<String, Object>(); 
		map.put("ItemImage", R.drawable.camera_pic); 
		map.put("ItemText", "" + pic_NO); 
		map.put("ItemPath", ""); 
		meumList.add(map); 
	
		saMenuItem.notifyDataSetChanged();
  }
  
 
  private void takePhoto(String picpathstr) {
		//执行拍照前，应该先判断SD卡是否存在
		
		String SDState = Environment.getExternalStorageState();
		if(SDState.equals(Environment.MEDIA_MOUNTED))
		{
			File file = new File(picpathstr);  
			
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
		case R.id.uploadImage:
			total_pic=this.meumList.size();
			if(total_pic>1 && total_pic<=7)
			{				
				ap.clear();
				alp.clear();
				this.upload_ok_pic=0;
				progressText.setText("0/"+(total_pic-1));
				progressBar.setVisibility(View.VISIBLE);
				progressText.setVisibility(View.VISIBLE);
				handler.sendEmptyMessage(TO_UPLOAD_FILE);
			}else if( total_pic > 7)
			{
				DialogFactory.ToastDialog(this, "发送照片",
						"一次最多只能发送6张照片！请删除部分照片后发送！");
			}
			else
			{
				Toast.makeText(this, "上传的文件路径出错", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.back:
			finish();
			break;
		}
	}
	@Override
	public void onBackPressed() {// 捕获返回按键事件，进入后台运行
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
		}else if(requestCode == SELECT_PIC_BY_TACK_PHOTO)
		{
			meumList.remove(meumList.size()-1);
			SimpleDateFormat    sDateFormat    =   new    SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");       
			String    date_str    =    sDateFormat.format(new    java.util.Date()); 
			HashMap<String, Object> map = new HashMap<String, Object>(); 
			map.put("ItemImage", BitmapFactory.decodeFile(picPath + pic_NO + ".jpg")); 
			map.put("ItemText", "" + pic_NO); 
			map.put("ItemPath", this.pic_path_save+"/"+date_str+".jpg"); 
			pic_NO++;
			meumList.add(map);
			
			map = new HashMap<String, Object>(); 
			map.put("ItemImage",  R.drawable.camera_pic); 
			map.put("ItemText", "" + pic_NO); 
			map.put("ItemPath", ""); 
			meumList.add(map);
			
			saMenuItem.notifyDataSetChanged();
			
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
		String picstr=(String)((HashMap<String, Object>)this.meumList.get(0)).get("ItemText");
		picstr=picPath + picstr + ".jpg";
		uploadUtil.uploadFile(picstr ,fileKey, requestURL,params);
	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int  next=0;
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
				if(msg.arg1==UploadUtil.UPLOAD_SUCCESS_CODE)
				{
					
					next=onUploadOK(true,msg.obj+"");
					
				}else
				{
					next=onUploadOK(false,"");
				}
				
				break;
			default:
				break;
			}
			super.handleMessage(msg);
			if(msg.what==UPLOAD_FILE_DONE && msg.arg1==UploadUtil.UPLOAD_SUCCESS_CODE)
			{
				if(next==1)
				{	
					Intent intent = new Intent(); 					
					intent.putStringArrayListExtra("pic_path", ap);
					intent.putStringArrayListExtra("pic_local_path", alp);
					System.out.println(msg.obj+"");
					setResult(RESULT_OK, intent); // 设置结果数据  
					Toast.makeText(getApplicationContext(), "发送完成！", 0).show();
					finish();
				}else if(next>1)
				{
					handler.sendEmptyMessage(TO_UPLOAD_FILE);
				}
					
			}
		}
		
	};
	public void copyFile(String oldPath, String newPath) {   
       try {   
           int bytesum = 0;   
           int byteread = 0;   
           File oldfile = new File(oldPath);   
           if (oldfile.exists()) { //文件存在时   
               InputStream inStream = new FileInputStream(oldPath); //读入原文件   
               FileOutputStream fs = new FileOutputStream(newPath);   
               byte[] buffer = new byte[1444];   
               int length;   
               while ( (byteread = inStream.read(buffer)) != -1) {   
                   bytesum += byteread; //字节数 文件大小   
                   System.out.println(bytesum);   
                   fs.write(buffer, 0, byteread);   
               }   
               inStream.close();   
           }   
       }   
       catch (Exception e) {   
           System.out.println("复制单个文件操作出错");   
           e.printStackTrace();   
  
       }   
  
   }
	public int onUploadOK(boolean statu,String url_path) {
		this.upload_ok_pic++;
		if(statu)
		{
			String uploadmsg=  upload_ok_pic +"/" + (this.total_pic-1);
			progressText.setText(uploadmsg);
			String picoldstr=(String)((HashMap<String, Object>)this.meumList.get(0)).get("ItemText");
			picoldstr=picPath + picoldstr + ".jpg";
			String picnewstr=(String)((HashMap<String, Object>)this.meumList.get(0)).get("ItemPath");
			copyFile(picoldstr,picnewstr);
			alp.add(picnewstr);
			ap.add(url_path);
		}
		else
		{
			Toast.makeText(getApplicationContext(), "失败上传"+ upload_ok_pic +"/" + (this.total_pic-1), 0).show();
		}
		this.meumList.remove(0);
		saMenuItem.notifyDataSetChanged();
		return meumList.size();
	}
	 
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
		super.onResume();
	}
	
}