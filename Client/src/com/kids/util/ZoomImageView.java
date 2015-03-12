package com.kids.util;


import com.kids.activity.imagescan.NativeImageLoader;
import com.way.chat.activity.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

public class ZoomImageView {
	private PopupWindow popupWindow;
	private LinearLayout layout;
	
	
	
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	
	private int mode = NONE;
	private float oldDist;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF start = new PointF();
	private PointF mid = new PointF();
	public ZoomImageView(Context context, Bitmap bitmap) {
		//dp
		int imgWidth = bitmap.getWidth();
		int imgHeight = bitmap.getHeight();
		
		DisplayMetrics dm=new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width =dm.widthPixels;
        int height = dm.heightPixels;
        float widthTimes = (float)width / imgWidth;
		final ImageView imageView = new ImageView(context);
		LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		imageView.setLayoutParams(params0);
		imageView.setScaleType(ScaleType.MATRIX);
		imageView.setImageBitmap(bitmap);
		//dp to px
		//int translateY = (int) (dipToPx(context, height / 2- imgHeight * widthTimes / 2) ) ;
		int translateY = (int) (height / 2- imgHeight * widthTimes / 2 ) ;
		//����Ļ���Ϊ�Ŵ�������ֱ������ʾ������Ĭ��matrix
		matrix.setValues(  
                new float[]{  
                        1, 0, 0,  
                        0, 1, translateY / widthTimes,  
                        0, 0, 1/widthTimes});
		//matrix.postTranslate(0, (int)(translateY/1.5));
		imageView.setImageMatrix(matrix);
		layout = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(params);
		layout.setGravity(Gravity.CENTER_VERTICAL);
		layout.addView(imageView);
		popupWindow = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
		popupWindow.setAnimationStyle(R.style.AnimationPop);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOnDismissListener(new OnDismissListener(){
            @Override
            public void onDismiss() {
            	NativeImageLoader.getInstance("big").clearCache();
            }
        });
        layout.setFocusable(true);
		layout.setFocusableInTouchMode(true);
		layout.setBackgroundColor(Color.BLACK);
		layout.getBackground().setAlpha(200);
		layout.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK ){
					popupWindow.dismiss();
				}
				return false;
			}
		});
		layout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//ImageView view = (ImageView) v;
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					savedMatrix.set(matrix);
					start.set(event.getX(), event.getY());
					mode = DRAG;
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = spacing(event);
					if (oldDist > 10f) {
						savedMatrix.set(matrix);
						midPoint(mid, event);
						mode = ZOOM;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {
						matrix.set(savedMatrix);
						matrix.postTranslate(event.getX() - start.x, event.getY()
								- start.y);
					} else if (mode == ZOOM) {
						float newDist = spacing(event);
						if (newDist > 10f) {
							matrix.set(savedMatrix);
							float scale = newDist / oldDist;
							matrix.postScale(scale, scale, mid.x, mid.y);
						}
					}
					break;
				}

				imageView.setImageMatrix(matrix);
				return true;
			}
			
			});
	}
	
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	public void showZoomView(){
		popupWindow.showAtLocation(layout, 0, 0, 0);
	}
	/**
	* �����ֻ��ķֱ��ʴ� dp �ĵ�λ ת��Ϊ px(����)
	*/
	public static int dipToPx(Context context, float dpValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpValue, context.getResources().getDisplayMetrics());
	}

}
