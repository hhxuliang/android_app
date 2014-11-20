package com.way.chat.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
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

public class CameraProActivity extends Activity implements OnClickListener,
		OnUploadProcessListener {
	private static final String TAG = "uploadImage";
	// 去上传文件
	protected static final int TO_UPLOAD_FILE = 1;
	// 上传文件响应
	protected static final int UPLOAD_FILE_DONE = 2;
	// 选择文件
	public static final int TO_SELECT_PHOTO = 3;
	// 上传初始化
	private static final int UPLOAD_INIT_PROCESS = 4;
	// 上传中
	private static final int UPLOAD_IN_PROCESS = 5;
	// 使用照相机拍照获取图片
	public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
	// 使用相册中的图片
	public static final int SELECT_PIC_BY_PICK_PHOTO = 2;

	private static String requestURL = "";
	private Button selectButton, back;
	private EditText progressText;
	private ProgressBar progressBar;
	public static String picPath = null;
	private String pic_path_save = null;
	private ProgressDialog progressDialog;
	private Uri photoUri;
	private Context mContext = null;
	private MyApplication application;
	private GridAdapter mGridAdapter;
	private ArrayList<HashMap<String, Object>> mGridItemList;
	private int pic_NO = 0;
	private int total_pic = 0;
	private int upload_ok_pic = 0;
	public String selItemIndex;
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
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		selectButton.setOnClickListener(this);
		progressDialog = new ProgressDialog(this);
		application = (MyApplication) this.getApplicationContext();
		picPath = application.getCameraPath() + "/upload";
		pic_path_save = application.getPicPath();
		// picPath = "/mnt/sdcard/children/camerapicpath/upload";
		requestURL = "http://" + Constants.SERVER_IP + ":8080"
				+ "/Server/UploadFile";
		ap = new ArrayList<String>();
		alp = new ArrayList<String>();
		mContext = this;
		progressBar.setVisibility(View.GONE);
		progressText.setVisibility(View.GONE);
		pic_NO = 0;
		initGrid();
	}

	private void initGrid() {
		GridView gridview = (GridView) findViewById(R.id.GridView_upload_pic);
		mGridItemList = new ArrayList<HashMap<String, Object>>();
		mGridAdapter = new GridAdapter(this, mGridItemList);

		// 添加Item到网格中
		gridview.setAdapter(mGridAdapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				System.out.println("click index:" + arg2);
				HashMap<String, Object> item = (HashMap<String, Object>) arg0
						.getItemAtPosition(arg2);
				if (((String) item.get("ItemPath")).equals("")) {
					CameraProActivity.this.selItemIndex = (String) item
							.get("ItemText");
					Dialog alertDialog = new AlertDialog.Builder(mContext)
							.setTitle("选择照片来源")
							.setPositiveButton("相机",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											String itemIndex = null;
											itemIndex = CameraProActivity.this.selItemIndex;
											takePhoto(picPath + itemIndex
													+ ".jpg");
										}
									})
							.setNegativeButton("相册",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											pickPhoto();
										}
									}).create();
					alertDialog.show();
				}
				// takePhoto(picPath + (String) item.get("ItemText") + ".jpg");
				else {

					ImageView image = (ImageView) arg1
							.findViewById(R.id.ItemImage);
					FileInputStream fis=null;
					try {
						fis = new FileInputStream(image.getContentDescription().toString());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(fis!=null){
						Bitmap bitmap = BitmapFactory.decodeStream(fis);
						ZoomImageView zoom = new ZoomImageView(mContext, bitmap);
						zoom.showZoomView();
					}
				}
			}
		});

		HashMap<String, Object> map = new HashMap<String, Object>();
		Resources res = getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.camera_pic);
		map.put("ItemImage", bmp);
		map.put("ItemActualPath", "");
		map.put("ItemText", "" + pic_NO);
		map.put("ItemPath", "");
		mGridItemList.add(map);

		mGridAdapter.notifyDataSetChanged();
	}

	private void takePhoto(String picpathstr) {
		// 执行拍照前，应该先判断SD卡是否存在

		String SDState = Environment.getExternalStorageState();
		if (SDState.equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(picpathstr);

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// "android.media.action.IMAGE_CAPTURE"
			/***
			 * 需要说明一下，以下操作使用照相机拍照，拍照后的图片会存放在相册中的 这里使用的这种方式有一个好处就是获取的图片是拍照后的原图
			 * 如果不实用ContentValues存放照片路径的话，拍照后获取的图片为缩略图不清晰
			 */
			photoUri = Uri.fromFile(file);
			intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
			intent.putExtra("return-data", true);
			/** ----------------- */
			startActivityForResult(intent, SELECT_PIC_BY_TACK_PHOTO);
		} else {
			Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
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
			total_pic = this.mGridItemList.size();
			if (total_pic > 1 && total_pic <= 7) {
				ap.clear();
				alp.clear();
				this.upload_ok_pic = 0;
				progressText.setText("0/" + (total_pic - 1));
				progressBar.setVisibility(View.VISIBLE);
				progressText.setVisibility(View.VISIBLE);
				handler.sendEmptyMessage(TO_UPLOAD_FILE);
			} else if (total_pic > 7) {
				DialogFactory.ToastDialog(this, "发送照片",
						"一次最多只能发送6张照片！请删除部分照片后发送！");
			} else if (total_pic == 1) {
				Toast.makeText(this, "啥照片都没有!", Toast.LENGTH_LONG).show();
			} else {
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

		if (resultCode == Activity.RESULT_OK) {
			// picPath = photoUri.toString().replace("file:///", "/");
			doPhoto(requestCode, data);
			// imageView不设null, 第一次上传成功后，第二次在选择上传的时候会报错。
			// imageView.setImageBitmap(null);
			// picPath = data.getStringExtra(SelectPicActivity.KEY_PHOTO_PATH);
			// Log.i(TAG, "最终选择的图片="+picPath);
			// txt.setText("文件路径"+picPath);
			// Bitmap bm = BitmapFactory.decodeFile(picPath);
			// Bitmap bitmap = (Bitmap) data.getExtras().get("data");
			// imageView.setImageBitmap(bitmap);

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 选择图片后，获取图片的路径
	 * 
	 * @param requestCode
	 * @param data
	 */
	private void doPhoto(int requestCode, Intent data) {
		if (requestCode == SELECT_PIC_BY_PICK_PHOTO) // 从相册取图片，有些手机有异常情况，请注意
		{
			if (data == null) {
				Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
				return;
			}
			photoUri = data.getData();
			if (photoUri == null) {
				Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
				return;
			}

			String[] pojo = { MediaStore.Images.Media.DATA };

			System.out.println("the path is " + photoUri.toString());

			String tmp = getPath(mContext, photoUri);
			System.out.println("the path is " + tmp);
			if (tmp != null)
				newPicture(tmp);
		} else if (requestCode == SELECT_PIC_BY_TACK_PHOTO) {
			newPicture(picPath + pic_NO + ".jpg");

		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	public void newPicture(String path) {
		mGridItemList.remove(mGridItemList.size() - 1);
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy_MM_dd_hh_mm_ss_SSS");
		String date_str = sDateFormat.format(new java.util.Date());
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ItemImage", BitmapFactory.decodeFile(path));
		map.put("ItemActualPath", path);
		map.put("ItemText", "" + pic_NO);
		map.put("ItemPath", this.pic_path_save + "/" + date_str + ".jpg");
		pic_NO++;
		mGridItemList.add(map);

		map = new HashMap<String, Object>();
		Resources res = getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.camera_pic);
		map.put("ItemImage", bmp);
		map.put("ItemActualPath", "");
		map.put("ItemText", "" + pic_NO);
		map.put("ItemPath", "");
		mGridItemList.add(map);

		mGridAdapter.notifyDataSetChanged();
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

	private void toUploadFile() {

		progressDialog.setMessage("正在上传文件...");
		/*
		 * uploadImageResult.setText("正在上传中...");
		 * progressDialog.setMessage("正在请求服务器上传...");
		 * progressDialog.setTitle("信息");
		 * progressDialog.setIcon(drawable.ic_dialog_info);
		 * progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		 * progressDialog.setProgress(59);
		 * progressDialog.setIndeterminate(true);
		 */
		/*
		 * progressDialog.setButton("确认", new DialogInterface.OnClickListener(){
		 * public void onClick(DialogInterface dialog, int which) {
		 * dialog.cancel(); } });
		 */

		progressDialog.show();
		String fileKey = "img";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		;
		uploadUtil.setOnUploadProcessListener(this); // 设置监听器监听上传状态

		Map<String, String> params = new HashMap<String, String>();
		params.put("orderId", "111");
		String picstr = (String) ((HashMap<String, Object>) this.mGridItemList
				.get(0)).get("ItemActualPath");
		
		uploadUtil.uploadFile(picstr, fileKey, requestURL, params);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int next = 0;
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
				if (msg.arg1 == UploadUtil.UPLOAD_SUCCESS_CODE) {

					next = onUploadOK(true, msg.obj + "");

				} else {
					next = onUploadOK(false, "");
				}

				break;
			default:
				break;
			}
			super.handleMessage(msg);
			if (msg.what == UPLOAD_FILE_DONE
					&& msg.arg1 == UploadUtil.UPLOAD_SUCCESS_CODE) {
				if (next == 1) {
					Intent intent = new Intent();
					intent.putStringArrayListExtra("pic_path", ap);
					intent.putStringArrayListExtra("pic_local_path", alp);
					System.out.println(msg.obj + "");
					setResult(RESULT_OK, intent); // 设置结果数据
					Toast.makeText(getApplicationContext(), "发送完成！", 0).show();
					finish();
				} else if (next > 1) {
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
			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}

	public int onUploadOK(boolean statu, String url_path) {
		this.upload_ok_pic++;
		if (statu) {
			String uploadmsg = upload_ok_pic + "/" + (this.total_pic - 1);
			progressText.setText(uploadmsg);
			String picoldstr = (String) ((HashMap<String, Object>) this.mGridItemList
					.get(0)).get("ItemActualPath");
			String picnewstr = (String) ((HashMap<String, Object>) this.mGridItemList
					.get(0)).get("ItemPath");
			copyFile(picoldstr, picnewstr);
			alp.add(picnewstr);
			ap.add(url_path);
		} else {
			Toast.makeText(getApplicationContext(),
					"失败上传" + upload_ok_pic + "/" + (this.total_pic - 1), 0)
					.show();
		}
		this.mGridItemList.remove(0);
		mGridAdapter.notifyDataSetChanged();
		return mGridItemList.size();
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
		handler.sendMessage(msg);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}