package com.example.nenuwaiter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
//layout.search.xml.listview 自定义使用dish_list.xml.和dishlist共享
public class Search extends Activity {
	private String searchname;
	private ListView searchlist;
	//web请求
	private String url = "http://nenuwaiter.sinaapp.com/search/";
	private HttpClient httpCilent;
	private HttpResponse response;
	private HttpPost httppost;
	private HttpEntity entity;
	//json封装与解封
	private JSONObject data = new JSONObject();
	private JSONArray img;
	private JSONArray name;
	private JSONArray price;
	private JSONArray fatherid;
	//小细节的信息更改
	private TextView title_bar;
	private TextView slide_info;
	String slide_welcome;
	//图片缓存类实现
	private ImageMemoryCache imageMemoryCache;
	private ImageFileCache imageFileCache = new ImageFileCache();
	private ImageGetFromHttp imageGetFromHttp = new ImageGetFromHttp();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search);
		begin();
		//4.0访问网络
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
	}
	private void begin(){
		searchlist = (ListView) findViewById(R.id.searchlist);
		//有点冗余...
		title_bar = (TextView) findViewById(R.id.title_bar_name);
		slide_info = (TextView) findViewById(R.id.slide_info);
		title_bar.setText("搜索");
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		slide_welcome = sharedPreferences.getString("name", "");
		slide_info.setText(slide_welcome);
		//提交web端,进行搜索.
		//SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		searchname = sharedPreferences.getString("search","" );
		data = new JSONObject();
		try {
			data.put("search", searchname);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpCilent = new DefaultHttpClient();
		httppost = new HttpPost(url);
		try {
			httppost.setEntity(new StringEntity(data.toString(),HTTP.UTF_8));
			response = httpCilent.execute(httppost);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				entity = response.getEntity();
	            StringBuffer sb = new StringBuffer();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
	            String s = null;
	            while((s = reader.readLine()) != null){
	                   sb.append(s);
	            }
	            data = new JSONObject();
	            data = new JSONObject(sb.toString());
	            String status = data.getString("status");
	            if(status.equals("y")){
	            	img = data.getJSONArray("img");
	            	name = data.getJSONArray("name");
	            	price = data.getJSONArray("price");
	            	fatherid = data.getJSONArray("fatherid");
	            }else{Toast.makeText(Search.this, "网络出错102", Toast.LENGTH_LONG).show();}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(Search.this, "网络出错101", Toast.LENGTH_LONG).show();
		}
		//获取获取后,进行处理.
		SimpleAdapter adapter = new SimpleAdapter(this, getdata(), R.layout.search_list, new String[]{"img","name","price","fatherid"},new int []{R.id.img,R.id.name,R.id.price,R.id.fatherid}){
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
		searchlist.setAdapter(adapter);
		//监听list的点击
		searchlist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				ListView selectitem = (ListView) arg0;
				HashMap<String, Object> item =(HashMap<String, Object>) selectitem.getItemAtPosition(arg2);
				String dish_name = (String) item.get("name");
				String father_id = (String) item.get("fatherid");
	           	 SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
	           	 Editor editor = sharedPreferences.edit();
	           	 editor.putString("dish_name",dish_name);
	           	 editor.putString("fatherid", father_id);
	           	 editor.commit();
//	           	Toast.makeText(Mainlist.this,name,Toast.LENGTH_SHORT).show();
	           	startActivity(new Intent(Search.this,Onedish.class));
				
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
				map.put("price", "¥"+price.getString(i));
				map.put("fatherid",fatherid.getString(i));
			list.add(map);
			}catch(Exception e){
				
			}
		}
		return list;
		
	}
	private Bitmap getimg(String i){
		Bitmap bitmap = null;
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
	public void jump1(View v){
		startActivity(new Intent(Search.this, Mainlist.class));
	}
	public void jump2(View v){
		startActivity(new Intent(Search.this, Shoplist.class));
	}
	public void jump3(View v){
		startActivity(new Intent(Search.this, Finallist.class));
	}
	public void search(View v){
		EditText searchname = (EditText) findViewById(R.id.search);
		String searchcontent = searchname.getText().toString();
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		Editor editor  = sharedPreferences.edit();
		editor.putString("search", searchcontent);
      	editor.commit();
		startActivity(new Intent(Search.this,Search.class));
		
	}
	
}
