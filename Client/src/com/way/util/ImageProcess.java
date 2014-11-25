package com.way.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import com.way.chat.activity.MyApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.view.WindowManager;

public class ImageProcess {
	public static Bitmap GetBitmapByPath(Context context, String pic_path,
			int winHeight, int winWidth, double myscale) {
		// Bitmap bitmap=BitmapFactory.decodeFile("/sdcard/a.jpg");
		// iv_bigimage.setImageBitmap(bitmap);
		try {
			File f = new File(pic_path);
			if (!f.exists()) {
				return null;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

		BitmapFactory.Options opts = new Options();
		// 不读取像素数组到内存中，仅读取图片的信息
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pic_path, opts);
		// 从Options中获取图片的分辨率
		int imageHeight = opts.outHeight;
		int imageWidth = opts.outWidth;

		// 获取屏幕的分辨率，getHeight()、getWidth已经被废弃掉了
		// 应该使用getSize()，但是这里为了向下兼容所以依然使用它们
		int windowHeight = (int) (winHeight * myscale);
		int windowWidth = (int) (winWidth * myscale);
		// 计算采样率
		int scaleX = imageWidth / windowWidth;
		int scale = 1;
		if (scaleX > 1) {
			scale = scaleX;
		}
		// 采样率依照最大的方向为准
		// if (scaleX > scaleY && scaleY >= 1) {
		// scale = scaleX;
		// }
		// if (scaleX < scaleY && scaleX >= 1) {
		// scale = scaleY;
		// }

		// false表示读取图片像素数组到内存中，依照设定的采样率
		opts.inJustDecodeBounds = false;
		// 采样率
		opts.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeFile(pic_path, opts);
		return bitmap;

	}

	public static Bitmap imageZoom(Bitmap bm, double maxSize) {
		// 图片允许最大空间 单位：KB
		Bitmap bitMap = bm;
		// 将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		// 将字节换成KB
		double mid = b.length / 1024;
		// 判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
		if (mid > maxSize) {
			// 获取bitmap大小 是允许最大大小的多少倍
			double i = mid / maxSize;
			// 开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍
			// （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
			bitMap = zoomImage(bitMap, bitMap.getWidth() / Math.sqrt(i),
					bitMap.getHeight() / Math.sqrt(i));
		}
		return bitMap;
	}

	/***
	 * 图片的缩放方法
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}

	/**
	 * 读取图片的旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return 图片的旋转角度
	 */
	public static int getBitmapDegree(String path) {
		int degree = 0;
		try {
			// 从指定路径下读取图片，并获取其EXIF信息
			ExifInterface exifInterface = new ExifInterface(path);
			// 获取图片的旋转信息
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 将图片按照某个角度进行旋转
	 * 
	 * @param bm
	 *            需要旋转的图片
	 * @param degree
	 *            旋转角度
	 * @return 旋转后的图片
	 */
	public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
		Bitmap returnBm = null;

		// 根据旋转角度，生成旋转矩阵
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
			returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		}
		if (returnBm == null) {
			returnBm = bm;
		}
		if (bm != returnBm) {
			bm.recycle();
		}
		return returnBm;
	}

}
