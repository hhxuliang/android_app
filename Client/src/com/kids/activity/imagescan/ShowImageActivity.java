package com.kids.activity.imagescan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.kids.activity.chat.CameraProActivity;
import com.kids.activity.chat.MyActivity;
import com.kids.activity.chat.MyApplication;
import com.kids.activity.imagescan.ChildAdapter.ViewHolder;
import com.kids.util.DialogFactory;
import com.kids.util.ImageProcess;
import com.kids.util.UploadUtil;
import com.kids.util.ZoomImageView;
import com.kids.util.UploadUtil.OnUploadProcessListener;
import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import com.way.chat.common.bean.User;
import com.way.chat.common.util.Constants;

public class ShowImageActivity extends MyActivity implements
		OnUploadProcessListener {
	private GridView mGridView;
	private List<String> list;
	private ChildAdapter adapter;
	private MenuInflater mi;// 菜单
	private String fun = null;
	ArrayList<String> deleteList = new ArrayList();
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
	private EditText progressText;
	private ProgressBar progressBar;
	private ProgressDialog progressDialog;
	private int upload_ok_pic = 0;
	private int total_pic = 0;
	private ArrayList<String> pathl = null;
	private ArrayList<String> ap;
	private ArrayList<String> alp;
	private MyApplication application;
	private User user = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_image_activity);
		user = (User) getIntent().getSerializableExtra("user");
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressText = (EditText) findViewById(R.id.progressText);
		progressBar.setVisibility(View.GONE);
		progressText.setVisibility(View.GONE);
		mGridView = (GridView) findViewById(R.id.child_grid);
		list = getIntent().getStringArrayListExtra("data");
		
		fun = getIntent().getStringExtra("fun");
		application = (MyApplication) getApplicationContext();

		Button back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		Button selectButton = (Button) findViewById(R.id.actionImage);
		if (fun.equals("DELETE")) {
			selectButton.setText("删除");
		} else if (fun.equals("SEND")) {
			selectButton.setText("发送");
			ap = new ArrayList<String>();
			alp = new ArrayList<String>();
		}

		selectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doaction();
			}
		});
		progressDialog = new ProgressDialog(this);
		adapter = new ChildAdapter(this, list, mGridView);
		mGridView.setAdapter(adapter);
		mi = new MenuInflater(this);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				ViewHolder viewHolder = (ViewHolder) arg1.getTag();
				String picpath = (String) viewHolder.mImageView.getTag();
				if (ImageProcess.checkFileType(picpath) == ImageProcess.FileType.IMAGE) {

					if (picpath != null) {
						Bitmap bitmap = ImageProcess.GetBitmapByPath(
								ShowImageActivity.this, picpath,
								MyApplication.mWindowHeight,
								MyApplication.mWindowWidth, 1);
						if (bitmap != null) {
							int degree = ImageProcess.getBitmapDegree(picpath);
							if (degree != 0)
								bitmap = ImageProcess.rotateBitmapByDegree(
										bitmap, degree);
							ZoomImageView zoom = new ZoomImageView(
									ShowImageActivity.this, bitmap);
							zoom.showZoomView();
						}
					}
				} else if (ImageProcess.checkFileType(picpath) == ImageProcess.FileType.VIDEO) {

					if (picpath != null) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse(picpath), "video/mp4");
						startActivity(intent);
					}
				}
			}
		});

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

	}

	public void doaction() {
		if (fun == null)
			return;
		pathl = adapter.getSelectItems();
		if (fun.equals("DELETE")) {
			int count = pathl.size();
			deleteList.addAll(pathl);
			for (String path : pathl) {
				File file = new File(path);
				if (file.exists() && file.isFile()) {
					file.delete();
					list.remove(path);
				}
			}
			if (count > 0) {
				adapter.clearSelectItems();
				adapter.notifyDataSetChanged();
				Toast.makeText(this, "删除成功" + count + "个文件!", Toast.LENGTH_LONG)
						.show();
			}
		} else if (fun.equals("SEND")) {
			total_pic = pathl.size();
			if (total_pic >= 1) {
				ap.clear();
				alp.clear();
				this.upload_ok_pic = 0;
				progressText.setText("0/" + (total_pic));
				progressBar.setVisibility(View.VISIBLE);
				progressText.setVisibility(View.VISIBLE);
				handler.sendEmptyMessage(TO_UPLOAD_FILE);
			}
		}
	}

	// ///////////////upload pic

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

		uploadUtil.setOnUploadProcessListener(this); // 设置监听器监听上传状态

		Map<String, String> params = new HashMap<String, String>();
		params.put("orderId", "111");
		String picstr = (String) pathl.get(0);
		fileKey = picstr.substring(picstr.lastIndexOf("."));
		uploadUtil.uploadFile(picstr, fileKey, Constants.FILE_UPLOAD_URL, params);
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
				if (next == 0) {
					/*
					 * Intent intent = new Intent();
					 * intent.putStringArrayListExtra("pic_path", ap);
					 * intent.putStringArrayListExtra("pic_local_path", alp);
					 * setResult(RESULT_OK, intent);
					 */// 设置结果数据
					progressBar.setVisibility(View.GONE);
					progressText.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(), "全部发送完成！", 0)
							.show();
					//finish();
				} else if (next >= 1) {
					handler.sendEmptyMessage(TO_UPLOAD_FILE);
				}

			}
		}

	};

	public int onUploadOK(boolean statu, String url_path) {
		this.upload_ok_pic++;
		String picnewstr = (String) pathl.get(0);
		if (statu) {
			String uploadmsg = upload_ok_pic + "/" + (this.total_pic );
			progressText.setText(uploadmsg);

			
			alp.add(picnewstr);
			ap.add(url_path);
			if (user != null) {
				if(application==null)
					System.out.println("application is null");
				if (null != application.send(url_path, true, picnewstr, user)){
					Toast.makeText(	getApplicationContext(),"成功上传" + picnewstr ,0).show();
				}else{
					Toast.makeText(	getApplicationContext(),"上传" + picnewstr + "失败" ,0).show();
				}
			}
		} else {
			Toast.makeText(	getApplicationContext(),"上传" + picnewstr + "失败" ,0).show();
		}
		pathl.remove(0);
		return pathl.size();
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
}
