package com.example.nenuwaiter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageGetFromHttp {
    private static final String LOG_TAG = "ImageGetFromHttp";
    
    public Bitmap downloadBitmap(String url){
    	Bitmap bitmap = null;
		try{
			URL imgurl = new URL(url);
	        HttpURLConnection conn = (HttpURLConnection)imgurl.openConnection();
	        InputStream is = conn.getInputStream();
	       
	        BitmapFactory.Options opts = new BitmapFactory.Options();
	        opts.inSampleSize = 10;
	        
	//        bitmap = BitmapFactory.decodeStream(is,null,opts);
	        //修改.加载完整图片
	        bitmap = BitmapFactory.decodeStream(is);
		}catch(Exception e){
			Log.w(LOG_TAG, "Error  while retrieving bitmap from " + url);
		}
		return bitmap;
		
    }
}
