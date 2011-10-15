package com.zrd.zr.letuwb;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.zrd.zr.pnj.SecureURL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class AsyncImageLoader extends AsyncTask<Object, Object, Bitmap> {
	private Context mContext;
	private Integer mResIdBadImage;
	private ImageView mImage;
	private ProgressBar mProgress = null;
	
	public AsyncImageLoader(Context context,
		Integer resIdImageView, Integer resIdBadImage) {
		super();
		mContext = context;
		mImage = (ImageView) ((Activity)mContext).findViewById(resIdImageView);
		mResIdBadImage = resIdBadImage;
	}
	
	public AsyncImageLoader(Context context,
		ImageView image, Integer resIdBadImage) {
		super();
		mContext = context;
		mImage = image;
		mResIdBadImage = resIdBadImage;
	}
	
	public AsyncImageLoader(Context context,
		ImageView image, Integer resIdBadImage, ProgressBar progress) {
		this(context, image, resIdBadImage);
		mProgress = progress;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		if (mProgress != null) {
			mProgress.setVisibility(ProgressBar.VISIBLE);
		}
		super.onPreExecute();
	}

	@Override
	protected Bitmap doInBackground(Object... params) {
		System.gc();
		System.runFinalization();
		System.gc();
		
		// TODO Auto-generated method stub
		
		if (params.length != 1) return null;
		
		URL url = (URL) params[0];
		
		SecureURL su = new SecureURL();
		/*
		 * see if local cached.
		 * it uses value of MD5 the address of URL for the cached image filename
		 */
		String pathCacheImg = AsyncSaver.getSdcardDir()
			+ EntranceActivity.PATH_CACHE;
		String fileCacheImg = su.phpMd5(url.toString());
		Bitmap bmp;
		int probe = AsyncSaver.probeFile(pathCacheImg, fileCacheImg);
		if (probe == -2) {//means cache image exists
			/*
			 * directly load the cached image here
			 */
			bmp = BitmapFactory.decodeFile(pathCacheImg + fileCacheImg);
	    	return bmp == null ? null : bmp;
		} else {
			/*
			 * load the INTERNET image here
			 */
			URLConnection conn = su.getConnection(url);
			InputStream is;
			
			if (conn == null) return null;
			try {
				is = conn.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("DEBUGTAG", "Remtoe Image Exception", e);
				e.printStackTrace();
				return null;
			}
			
			if (is == null) return null;
			BufferedInputStream bis = new BufferedInputStream(is, 8192);
			bmp = BitmapFactory.decodeStream(bis);
			if (bmp == null) return null;
			try {
				bis.close();
				is.close();
				bis = null;
				is = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("DEBUGTAG", "Remtoe Image Exception", e);
				e.printStackTrace();
				bis = null;
				is = null;
				return null;
			}
			if (probe == 1) {//means could deal with the cache
				/*
				 * cache the image file here
				 */
				AsyncSaver saver = new AsyncSaver(mContext, bmp);
				saver.saveImage(
					AsyncSaver.getSilentFile(pathCacheImg, fileCacheImg)
				);
			}
			return bmp;
		}
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		// TODO Auto-generated method stub
		if (mImage != null) {
			if (result != null) {
				mImage.setImageBitmap(result);
			}
			else {
				mImage.setImageResource(mResIdBadImage);
			}
		}
		if (mProgress != null) {
			mProgress.setVisibility(ProgressBar.GONE);
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}
	
	@Override
    protected void onCancelled() {
		super.onCancelled();
    }
}