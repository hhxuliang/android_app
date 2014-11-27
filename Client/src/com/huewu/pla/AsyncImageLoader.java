package com.huewu.pla;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * 异步加载图片
 * 注意这里使用??SoftReference来缓存图片，允许 GC在需要的时??可以对缓存中的图片进行清理??
 * @author zhanggq
 *
 */
public class AsyncImageLoader {

	private HashMap<String, SoftReference<Bitmap>> imageCache;
	/**
	 * ImageLoader的实例。
	 */
	private static AsyncImageLoader mAsyncImageLoader;
	
    public AsyncImageLoader() {
    	// 获取应用程序最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 8;
        imageCache = new HashMap<String, SoftReference<Bitmap>>();
    }
 
    public static AsyncImageLoader getInstance(){
    	if(mAsyncImageLoader == null){
    		mAsyncImageLoader = new AsyncImageLoader();
    	}
    	return mAsyncImageLoader;
    }
    
    public void addBitmapToMemoryCache(String imageUrl , Bitmap bitmap){
    	if (!imageCache.containsKey(imageUrl)){
    		imageCache.put(imageUrl, new SoftReference<Bitmap>(bitmap));
    	}
    }
    
    public Bitmap getBitmapFromMemoryCache(String imageUrl){
    	 SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
    	 if(softReference == null){
    		return null; 
    	 }
         return softReference.get();
    }
    public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth) {
		// 源图片的宽度
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			// 计算出实际宽度和目标宽度的比率
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRatio;
		}
		return inSampleSize;
	}
    public static Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
}
