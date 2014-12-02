package com.example.imagescan;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.imagescan.ChildAdapter.ViewHolder;
import com.way.chat.activity.CameraProActivity;
import com.way.chat.activity.MyActivity;
import com.way.chat.activity.MyApplication;
import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import com.way.util.ImageProcess;
import com.zoom.ZoomImageView;

public class ShowImageActivity extends MyActivity {
	private GridView mGridView;
	private List<String> list;
	private ChildAdapter adapter;
	private MenuInflater mi;// 菜单
	ArrayList<String> deleteList = new ArrayList();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_image_activity);

		mGridView = (GridView) findViewById(R.id.child_grid);
		list = getIntent().getStringArrayListExtra("data");

		adapter = new ChildAdapter(this, list, mGridView);
		mGridView.setAdapter(adapter);
		mi = new MenuInflater(this);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				ViewHolder viewHolder = (ViewHolder)arg1.getTag();
				String picpath = (String) viewHolder.mImageView.getTag();
				if (ImageProcess.checkFileType(picpath) == ImageProcess.FileType.IMAGE) {

					
					if (picpath != null) {
						Bitmap bitmap = ImageProcess.GetBitmapByPath(
								ShowImageActivity.this, picpath,
								MyApplication.mWindowHeight,
								MyApplication.mWindowWidth, 1.3);
						if (bitmap != null) {
							int degree = ImageProcess.getBitmapDegree(picpath);
							if (degree != 0)
								bitmap = ImageProcess.rotateBitmapByDegree(
										bitmap, degree);
							ZoomImageView zoom = new ZoomImageView(ShowImageActivity.this,
									bitmap);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		mi.inflate(R.menu.deleteimages, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

	}

	@Override
	// 菜单选项添加事件处理
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.deletefiles:
			// Toast.makeText(getApplicationContext(), "亲！此功能暂未实现哦", 0).show();
			ArrayList<String> pathl = adapter.getSelectItems();
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
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
