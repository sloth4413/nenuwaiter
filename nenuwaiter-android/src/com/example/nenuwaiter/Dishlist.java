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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Dishlist extends Activity {
	private String restaurant;
	private ListView dishlist;
	
	private HttpClient httpCilent;
	private HttpResponse response;
	private HttpPost httppost;
	private HttpEntity entity;
	private String url = "http://nenuwaiter.sinaapp.com/dishlist/";
	private String url1 = "http://nenuwaiter.sinaapp.com/updata/";
	private JSONArray img;
	private JSONArray name;
	private JSONArray price;
	private JSONArray dishlist_rating;
	//restaurant相关信息
	private TextView restaurant_phone;
	private TextView restaurant_address;
	private TextView restaurant_introduce;
	
	private TextView title_bar;
	//评分星星.
	private TextView ratingnum;
	private RatingBar dishrating;
	private Integer marknum;
	private float mark;
	private String fatherid;
	//图片缓存类实现
	private ImageMemoryCache imageMemoryCache;
	private ImageFileCache imageFileCache = new ImageFileCache();
	private ImageGetFromHttp imageGetFromHttp = new ImageGetFromHttp();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dishlist);
		initView();
		list();
		//传说中的代码，使的4.0可以请求网络
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());

	}
	private void initView(){
		//获取从mainlist监听下面获取的数据
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		restaurant = sharedPreferences.getString("restaurant", "");
		dishlist = (ListView) this.findViewById(R.id.dishlist);
		
		title_bar = (TextView) findViewById(R.id.title_bar_name);
		title_bar.setText(restaurant);
		
		restaurant_phone = (TextView) findViewById(R.id.restaurant_phone);
		restaurant_address = (TextView) findViewById(R.id.restaurant_address);
		restaurant_introduce = (TextView) findViewById(R.id.restaurant_introduce);
		
		dishrating = (RatingBar) findViewById(R.id.dish_ratingbar);
		ratingnum = (TextView) findViewById(R.id.dish_ratingnum);
		
	}
	private void list(){
		JSONObject data = new JSONObject();
		try{
			data.put("name",restaurant);
		}catch(JSONException e1){
			e1.printStackTrace();
		}
		httpCilent = new DefaultHttpClient();
		httppost = new HttpPost(url);
		try{
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
	            JSONObject datas = new JSONObject(sb.toString());
	            String status = datas.getString("status");
	            if(status.equals("y")){
	            	img = datas.getJSONArray("img");
	            	name = datas.getJSONArray("name");
	            	price = datas.getJSONArray("price");
	            	dishlist_rating = datas.getJSONArray("dishlist_rating");
	            	//评分相关
	            	mark = (float) datas.getDouble("res_mark");
	            	marknum = datas.getInt("res_marknum");
	            	dishrating.setRating(mark);
	            	String father_id = datas.getString("res_id");
	            	fatherid = father_id;
	            	String res_num = "评分人数："+marknum.toString();
	            	ratingnum.setText(res_num);
	            	String res_phone ="电话："+datas.getString("res_phone");
	            	restaurant_phone.setText(res_phone);
	            	String res_address = "地址："+datas.getString("res_address");
	            	restaurant_address.setText(res_address);
	            	String res_introduce = "简介："+datas.getString("res_introduce");
	            	restaurant_introduce.setText(res_introduce);
	            	//保存选中餐馆id.用于识别菜(解决两家菜馆有相同菜名的问题)
		           	 SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		           	 Editor editor = sharedPreferences.edit();
		           	 editor.putString("fatherid",father_id);
		           	 editor.commit();	            	
	            	
	            }
	            else{
	            	Toast.makeText(Dishlist.this, "网络出错，请重试，错误代码103", Toast.LENGTH_SHORT).show();
	            }
			}
			else{
				Toast.makeText(Dishlist.this, "网络出错，请重试，错误代码102", Toast.LENGTH_SHORT).show();
			}
			
		}catch(Exception e){
			Toast.makeText(Dishlist.this, "网络出错，请重试，错误码101", Toast.LENGTH_SHORT).show();
		}
		SimpleAdapter adapter = new SimpleAdapter(this, getdata(), R.layout.dish_list, 
				new String[]{"img","name","price","dishlist_rating"},new int []{R.id.img,R.id.name,R.id.price,R.id.dishlist_rating}){
			@Override
			public void setViewImage(final ImageView v, final String value) {
				// TODO Auto-generated method stub
				if(v.getId()==R.id.img){
					new Thread(new Runnable(){
						
						@Override
						public void run() {
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
//		adapter.setViewBinder(new MyViewBinder());
		adapter.setViewBinder(new MyBinder());
		dishlist.setAdapter(adapter);
		
		//监听.dishlist的点击.
		dishlist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ListView selectitem = (ListView) arg0;
				HashMap<String, Object> item =(HashMap<String, Object>) selectitem.getItemAtPosition(arg2);
				String dish_name = (String) item.get("name");
	           	 SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
	           	 Editor editor = sharedPreferences.edit();
	           	 editor.putString("dish_name",dish_name);
	           	 editor.commit();
//	           	Toast.makeText(Mainlist.this,name,Toast.LENGTH_SHORT).show();
	           	startActivity(new Intent(Dishlist.this,Onedish.class));
				
			}			
		});
		//定义RatingBar监听器.
		OnRatingBarChangeListener orbc = new OnRatingBarChangeListener() {
			
			@Override
			public void onRatingChanged(RatingBar arg0, float arg1, boolean arg2) {
				// TODO Auto-generated method stub
				switch (arg0.getId()) {
				case R.id.dish_ratingbar:
			            	dishrating.setIsIndicator(true);
					float score = arg0.getRating();
					float old = mark*marknum;
					marknum+=1;
					mark = (old+score)/marknum;
//					dishrating.setRating(mark);
			            	String res_num = "评分人数："+marknum.toString();
					ratingnum.setText(res_num);
					updata(fatherid,marknum,mark);
					break;
				}
			}
		};
		dishrating.setOnRatingBarChangeListener(orbc);
		//监听触摸事件.失败了.监听触摸效果很奇怪.还是要在ratingbarchange上做文章.
/*		dishrating.setOnTouchListener(new OnTouchListener() {
		    
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_UP){
	                     float touchPositionX = event.getX();
	                     float width =dishrating.getWidth();
	                     float starsf = (touchPositionX / width) * 5.0f;
	                     float old = mark*marknum;
	                     marknum+=1;
	                     mark = (old+starsf)/marknum;
	                     dishrating.setRating(mark);
	                     String res_num = "评分人数："+marknum.toString();
	                     ratingnum.setText(res_num);
	                     dishrating.setIsIndicator(true);
	                     updata(fatherid,marknum,mark);
	                     v.setPressed(false);
			}
	                if (event.getAction() == MotionEvent.ACTION_DOWN) {
	                    v.setPressed(true);
	                }

	                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
	                    v.setPressed(false);
	                }
			return true;
		    }
		});*/
		
		
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
				map.put("dishlist_rating", dishlist_rating.getString(i));
			list.add(map);
			}catch(Exception e){
				
			}
		}
		return list;
		
	}
	//获取互联网的图片
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
	//更新评分数据.
	private void updata(String fatherid,Integer marknum,float mark){
		JSONObject data = new JSONObject();
		try{
			data.put("father_id",fatherid);
			data.put("mark", mark);
			data.put("marknum",marknum);
		}catch(JSONException e1){
			e1.printStackTrace();
		}
		httpCilent = new DefaultHttpClient();
		httppost = new HttpPost(url1);
		try {
			httppost.setEntity(new StringEntity(data.toString(),HTTP.UTF_8));
			response = httpCilent.execute(httppost);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}


}
