package com.example.nenuwaiter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nenuwaiter.ImageGetFromHttp;
import com.example.nenuwaiter.ImageMemoryCache;
import com.example.nenuwaiter.ImageFileCache;

public class Mainlist extends Activity {
	//滑动什么的.
	private SlideMenu slideMenu;

	private HttpClient httpCilent;
	private HttpResponse response;
	private HttpGet httpget;
	private HttpEntity entity;
	private JSONArray name;
	private JSONArray img;
	private JSONArray phonenumber;
	private JSONArray main_rating;
	private ListView restaurantlist;
	
	private TextView title_bar;
	private TextView slide_info;
	String slide_welcome;
	String url = "http://nenuwaiter.sinaapp.com/restaurantlist/";
	//图片缓存类实现
	private ImageMemoryCache imageMemoryCache;
	private ImageFileCache imageFileCache = new ImageFileCache();
	private ImageGetFromHttp imageGetFromHttp = new ImageGetFromHttp();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mainlist);
		initView();
		addlist();
		
		//使4.0可以请求网络
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
		slideMenu = (SlideMenu) findViewById(R.id.slide_menu);
	}
	private void initView(){
		restaurantlist = (ListView) findViewById(R.id.restaurantlist);
		title_bar = (TextView) findViewById(R.id.title_bar_name);
		slide_info = (TextView) findViewById(R.id.slide_info);
		title_bar.setText("餐厅列表");
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		slide_welcome = sharedPreferences.getString("name", "");
		slide_info.setText(slide_welcome);
	}
	private void addlist(){
		//请求列表
		try{
		httpCilent = new DefaultHttpClient();
		httpget = new HttpGet(url);
		response = httpCilent.execute(httpget);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				entity = response.getEntity();
	            StringBuffer sb = new StringBuffer();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
	            String s = null;
	            while((s = reader.readLine()) != null){
	                   sb.append(s);
	            }
	            JSONObject datas = new JSONObject(sb.toString());
	            name = datas.getJSONArray("name");
	            img  = datas.getJSONArray("img");
	            phonenumber = datas.getJSONArray("phonenumber");
	            main_rating = datas.getJSONArray("main_star");
//	            String names = name.getString(1);
//	            Toast.makeText(Mainlist.this,names, Toast.LENGTH_SHORT).show();
	        }
			else{
				Toast.makeText(Mainlist.this,"网络出错", Toast.LENGTH_SHORT).show();
			}
		
		}catch(Exception e){
			Toast.makeText(Mainlist.this,"请求列表出错", Toast.LENGTH_SHORT).show();
			}
		
		SimpleAdapter adapter = new SimpleAdapter(this, getdata(), R.layout.restaurant_list,
				new String[]{"img","name","phonenumber","main_rating"},new int []{R.id.img,R.id.name,R.id.phonenumber,R.id.main_rating}){

					@Override
					public void setViewImage(final ImageView v, final String value) {
						// TODO Auto-generated method stub
						if(v.getId()==R.id.img){
							new Thread(new Runnable(){
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									try{
										final Bitmap bitmap= getimg(value);
                                        v.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(img!=null){
                                                v.setImageBitmap(bitmap);}
                                            }
                                        });
									}catch(Exception e){
										
									}
								}
								
							}).start();
						}
						else{
							super.setViewImage(v, value);
						}
					}
			
		};
		//除去setview.测试是否成功.---
//		adapter.setViewBinder(new MyViewBinder());
		adapter.setViewBinder(new MyBinder());
		restaurantlist.setAdapter(adapter);
//监听，item的选择
		restaurantlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				ListView selectitem = (ListView) arg0;
				HashMap<String, Object> item =(HashMap<String, Object>) selectitem.getItemAtPosition(arg2);
				String name = (String) item.get("name");
	           	 SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
	           	 Editor editor = sharedPreferences.edit();
	           	 editor.putString("restaurant", name);
	           	 editor.commit();
//	           	Toast.makeText(Mainlist.this,name,Toast.LENGTH_SHORT).show();
	           	startActivity(new Intent(Mainlist.this,Dishlist.class));
				
			}
			
		});
	
		
	}
	private List<Map<String, Object>> getdata(){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		int len = name.length();
		for(int i=0;i<len;i++){
			try{
				map = new HashMap<String, Object>();
				map.put("img", img.getString(i));
				map.put("name", name.getString(i));
				map.put("phonenumber", phonenumber.getString(i));
				map.put("main_rating",main_rating.getString(i));
			list.add(map);
			}catch(Exception e){
				
			}
		}
		return list;
		
	}
	//获取互联网的图片
	private Bitmap getimg(String i){
		Bitmap bitmap = null;
//		try{
//			URL imgurl = new URL(i);
//            HttpURLConnection conn = (HttpURLConnection)imgurl.openConnection();
//            InputStream is = conn.getInputStream();
//           
//            BitmapFactory.Options opts = new BitmapFactory.Options();
//            opts.inSampleSize = 10;
//            
////            bitmap = BitmapFactory.decodeStream(is,null,opts);
//            //修改.加载完整图片
//            bitmap = BitmapFactory.decodeStream(is);
//		}catch(Exception e){
//			Toast.makeText(Mainlist.this,"获取图片失败.", Toast.LENGTH_SHORT).show();
//		}
//		修改为缓存图片，以上为直接http获取，imagergetURL中使用以上获取。
		imageMemoryCache = new ImageMemoryCache(this.getApplicationContext());
		bitmap = imageMemoryCache.getBitmapFromCache(i);
		if(bitmap == null){
			bitmap = imageFileCache.getImage(i);
			if (bitmap == null){
				bitmap = imageGetFromHttp.downloadBitmap(i);
				if(bitmap != null){
					imageFileCache.saveBitmap(bitmap, i);
					imageMemoryCache.addBitmapToCache(i, bitmap);
				}
			}else imageMemoryCache.addBitmapToCache(i, bitmap);
		}
		return bitmap;
	}
	//滑动侧边栏,跳转
	public void jump1(View v){
//		startActivity(new Intent(this, Mainlist.class));
	}
	public void jump2(View v){
		startActivity(new Intent(this, Shoplist.class));
	}
	public void jump3(View v){
		startActivity(new Intent(this, Finallist.class));
	}
	public void jump4(View v){
		startActivity(new Intent(this,Coupon.class));
	}
	public void search(View v){
		EditText searchname = (EditText) findViewById(R.id.search);
		String searchcontent = searchname.getText().toString();
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		Editor editor  = sharedPreferences.edit();
		editor.putString("search", searchcontent);
      	editor.commit();
		startActivity(new Intent(this,Search.class));
		
	}
	
}
//viewbinder.加载bmp数据。
class MyViewBinder implements ViewBinder
{

    @Override
    public boolean setViewValue(View view, Object data,
            String textRepresentation) {
        if((view instanceof ImageView)&(data instanceof Bitmap))
        {
            ImageView iv = (ImageView)view;
            Bitmap bmp = (Bitmap)data;
            iv.setImageBitmap(bmp);
            return true;
        }
        return false;
    }
}
class MyBinder implements ViewBinder{
    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        if((view.getId() == R.id.main_rating)||(view.getId() == R.id.dishlist_rating)){
            String stringval = (String) data;
            float ratingValue = Float.parseFloat(stringval);
            RatingBar ratingBar = (RatingBar) view;
            ratingBar.setRating(ratingValue);
            return true;
        }
        return false;
    }
}