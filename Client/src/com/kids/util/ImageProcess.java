package com.kids.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kids.activity.chat.FriendListActivity;
import com.kids.activity.chat.MyApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.WindowManager;

public class ImageProcess {
	public enum FileType {
		IMAGE, VIDEO, APK,VOICE ,UNKNOW
	};

	/**
	 * 删除单个文件
	 * 
	 * @param filePath
	 *            被删除文件的文件名
	 * @return 文件删除成功返回true，否则返回false
	 */
	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			file.delete();
		}
	}

	public static Bitmap GetBitmapByPath(Context context, String pic_path,
			int winHeight, int winWidth, double myscale) {
		// Bitmap bitmap=BitmapFactory.decodeFile("/sdcard/a.jpg");
		// iv_bigimage.setImageBitmap(bitmap);
		if (winWidth == 0 || winHeight == 0)
			return null;
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
		if (windowWidth == 0 || windowHeight == 0)
			return null;
		// 计算采样率
		int scaleX = imageWidth / windowWidth;
		int scaleY = imageHeight / windowHeight;
		int scale = 1;
		if (scaleX > 1) {
			scale = scaleX;
		}
		if (scaleY > scaleX) {
			scale = scaleY;
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
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
				bm = null;
				System.gc();
			}
		}
		return returnBm;
	}

	public static ArrayList<String> ListFile(String path) {

		File file = new File(path);
		File[] f = file.listFiles();
		ArrayList<String> Path = new ArrayList<String>();
		if (f != null) {
			for (int i = 0; i < f.length; i++) {
				if (f[i].isFile())
					Path.add(f[i].getPath());
			}
		}
		return Path;

	}

	// 检查扩展名，得到图片格式的文件
	public static FileType checkFileType(String fName) {
		FileType isImageFile = ImageProcess.FileType.UNKNOW;
		// 获取扩展名
		String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
				fName.length()).toLowerCase();
		if (FileEnd.equals("jpg") || FileEnd.equals("gif")
				|| FileEnd.equals("png") || FileEnd.equals("jpeg")
				|| FileEnd.equals("bmp")) {
			isImageFile = ImageProcess.FileType.IMAGE;
		} else if (FileEnd.equals("mp4")) {
			isImageFile = ImageProcess.FileType.VIDEO;
		} else if (FileEnd.equals("apk")) {
			isImageFile = ImageProcess.FileType.APK;
		}else if (FileEnd.equals("amr")) {
			isImageFile = ImageProcess.FileType.VOICE;
		}
		return isImageFile;
	}

	private void backcode() {
		// Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		// ContentResolver mContentResolver = FriendListActivity.this
		// .getContentResolver();
		//
		// // 只查询jpeg和png的图片
		// Cursor mCursor = mContentResolver.query(mImageUri, null,
		// MediaStore.Images.Media.MIME_TYPE + "=? or "
		// + MediaStore.Images.Media.MIME_TYPE + "=? or "
		// + MediaStore.Images.Media.MIME_TYPE + "=?",
		// new String[] { "image/jpeg", "image/png", "image/jpg" },
		// MediaStore.Images.Media.DATE_MODIFIED);
		//
		// while (mCursor.moveToNext()) {
		// // 获取图片的路径
		// String path = mCursor.getString(mCursor
		// .getColumnIndex(MediaStore.Images.Media.DATA));
		//
		// // 获取该图片的父路径名
		// String parentName = new File(path).getParentFile().getName();
		//
		// // 根据父路径名将图片放入到mGruopMap中
		// if (!mGruopMap.containsKey(parentName)) {
		// List<String> chileList = new ArrayList<String>();
		// chileList.add(path);
		// mGruopMap.put(parentName, chileList);
		// } else {
		// mGruopMap.get(parentName).add(path);
		// }
		// }
		//
		// mCursor.close();
		//
		// // 通知Handler扫描图片完成
		// mHandler.sendEmptyMessage(SCAN_OK);
	}

	public static ByteArrayOutputStream getSmallBitmap(String filePath) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, 480, 800);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		Bitmap bm1 = BitmapFactory.decodeFile(filePath, options);
		if (bm1 == null) {
			return null;
		}
		int degree = readPictureDegree(filePath);
		Bitmap bm = rotateBitmapByDegree(bm1, degree);
		ByteArrayOutputStream baos = null;
		baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 30, baos);

		if(bm != null && !bm.isRecycled()){
			bm.recycle();
			bm = null;
			System.gc();
        }
		return baos;

	}

	private static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
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

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}

		return inSampleSize;
	}

}
