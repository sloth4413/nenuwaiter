package com.example.nenuwaiter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Onedish extends Activity {
	private String fatherid;
	private String onedish;
	private String url = "http://nenuwaiter.sinaapp.com/onedish/";
	private String url2 = "http://nenuwaiter.sinaapp.com/updata2/";
	private String url3 = "http://nenuwaiter.sinaapp.com/updata3/";
	private String onedish_father;
	private String onedish_price;
	private String onedish_img;
	private String onedish_introduce;
	
	private HttpClient httpClient;
	private HttpPost httpPost;
	private HttpResponse response;
	private HttpEntity entity;
	
	private ImageView image;
	private TextView info;
	
	private TextView title_bar;
	private EditText numberpick;
	//评分
	private RatingBar onedish_rating;
	private float mark;
	private Integer marknum;
	private String id;
	
	//comment.评论
	private EditText newcomment;
	private ListView commentlist;
	private JSONArray commentcontent;
	//图片缓存类实现
	private ImageMemoryCache imageMemoryCache;
	private ImageFileCache imageFileCache = new ImageFileCache();
	private ImageGetFromHttp imageGetFromHttp = new ImageGetFromHttp();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.onedish);
		initView();
		doit();//进行网络访问获取相应的数据
		giveShow();
		//4.0访问网络
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());

	}
	private void initView(){
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		fatherid = sharedPreferences.getString("fatherid", "");
		onedish = sharedPreferences.getString("dish_name", "");
		image =(ImageView) findViewById(R.id.onedish_img);
		info = (TextView) findViewById(R.id.onedish_text);
		title_bar = (TextView) findViewById(R.id.title_bar_name);
		title_bar.setText(onedish);
		
		numberpick = (EditText) findViewById(R.id.numberpicker1);
		onedish_rating = (RatingBar) findViewById(R.id.onedish_rating);
		
		newcomment = (EditText) findViewById(R.id.new_comment);
		commentlist = (ListView) findViewById(R.id.commentlist);
	}
	private void doit(){
		JSONObject data = new JSONObject();
		try{
			data.put("fatherid",fatherid);
			data.put("dishname",onedish);
		}catch(JSONException e1){
			e1.printStackTrace();
		}
		httpClient = new DefaultHttpClient();
		httpPost = new HttpPost(url);
		try{
			httpPost.setEntity(new StringEntity(data.toString(),HTTP.UTF_8));
			response = httpClient.execute(httpPost);
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
	            	onedish_father = datas.getString("onedish_father");
	            	onedish_introduce = datas.getString("onedish_introduce");
	            	onedish_img = datas.getString("onedish_img");
	            	onedish_price = datas.getString("onedish_price");
	            	
	            	id = datas.getString("onedish_id");
	            	marknum = datas.getInt("onedish_marknum");
	            	mark = (float) datas.getDouble("onedish_mark");
	            	onedish_rating.setRating(mark);
	            	
	            	commentcontent = datas.getJSONArray("commentlist");
	            }
	            else{
					Toast.makeText(this,"网络出错,错误103",Toast.LENGTH_SHORT).show();
	            }
			}
			else{
				Toast.makeText(this,"网络出错,错误102",Toast.LENGTH_SHORT).show();
			}
		}catch(Exception e){
			Toast.makeText(this,"网络出错,错误101",Toast.LENGTH_SHORT).show();
		}
		
	}
	private void giveShow(){
		image.setImageBitmap(getimg(onedish_img));
		String html = "<p><span><font color=\"#177cb0\">餐馆：</span>"+onedish_father+"</p>"+
				"<p><span><font color=\"#177cb0\">价格：</span>￥"+onedish_price+"</p>"+
				"<p><span><font color=\"#177cb0\">介绍：</span>"+onedish_introduce+"</p>"+
				"<p>评分人数: "+marknum.toString()+"</p>";
		info.setText(Html.fromHtml(html));
		
		OnRatingBarChangeListener orbc = new OnRatingBarChangeListener() {
			
			@Override
			public void onRatingChanged(RatingBar arg0, float arg1, boolean arg2) {
				switch (arg0.getId()) {
				case R.id.onedish_rating:
			            	onedish_rating.setIsIndicator(true);
					float score = arg0.getRating();
					float old = mark*marknum;
					marknum+=1;
					mark = (old+score)/marknum;
					updata(id,marknum,mark);
					break;
				}
			}
		};
		onedish_rating.setOnRatingBarChangeListener(orbc);
		
		//显示评论.
		SimpleAdapter adapter = new SimpleAdapter(Onedish.this, getdata(), R.layout.comment_list,
				new String []{"comment",}, new int[]{R.id.comment,} );
		commentlist.setAdapter(adapter);
		setListViewHeightBasedOnChildren(commentlist);
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
	private List<Map<String, Object>> getdata(){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		int len = commentcontent.length();
		for(int i =0;i<len;i++){
			map = new HashMap<String, Object>();
			try {
				map.put("comment",commentcontent.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			list.add(map);
		}
		return list;
	}
	//getdata2用于刷新listview
	private List<Map<String, Object>> getdata2(String newsay){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		int len = commentcontent.length();
		for(int i =0;i<len;i++){
			map = new HashMap<String, Object>();
			try {
				map.put("comment",commentcontent.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			list.add(map);
		}
		map = new HashMap<String, Object>();
		map.put("comment",newsay);
		list.add(map);
		return list;
	}
	public void saveSql(View v){
		String num = numberpick.getText().toString();
		SQLiteDatabase db = openOrCreateDatabase("order.db", Context.MODE_PRIVATE, null);
		db.execSQL("create table if not exists orderlist(id INTEGER PRIMARY KEY AUTOINCREMENT,dishname VARCHAR,restaurant VARCHAR,price float,num SMALLINT)");
		String new_sql = "select count(*) as cnt,num from orderlist where dishname ='"+onedish+"'";
		Cursor x = db.rawQuery(new_sql, new String[]{});
		x.moveToFirst();
		if(x.getString(x.getColumnIndex("cnt")).equals("1")){
			int num_now = x.getInt(x.getColumnIndex("num"));
			int num_new = Integer.valueOf(num).intValue()+num_now;
			String sql_num = "update orderlist set num = "+num_new+" where dishname = '"+onedish+"'";
//			db.execSQL("update orderlist values(NULL,?,?,?,?)",new Object[]{onedish,onedish_father,onedish_price,num_now+num});
			db.execSQL(sql_num);
		}	
		else{
			db.execSQL("insert into orderlist values (NULL,?,?,?,?)",new Object[]{onedish,onedish_father,onedish_price,num});
		}
		x.close();
		Cursor c = db.rawQuery("select * from orderlist", new String[]{});
		c.moveToLast();
		Toast.makeText(Onedish.this, " 价格:"+c.getFloat(c.getColumnIndex("price"))+"订餐数:"+c.getInt(c.getColumnIndex("num")), Toast.LENGTH_SHORT).show();
		c.close();
		db.close();
	}
	public void gotoshoplist(View v){
		startActivity(new Intent(Onedish.this,Shoplist.class));
	}
	
	private void updata(String id,Integer marknum,float mark){
		JSONObject data = new JSONObject();
		try{
			data.put("id",id);
			data.put("mark", mark); 
			data.put("marknum",marknum);
		}catch(JSONException e1){
			e1.printStackTrace();
		}
		httpClient = new DefaultHttpClient();
		httpPost = new HttpPost(url2);
		try {
			httpPost.setEntity(new StringEntity(data.toString(),HTTP.UTF_8));
			response = httpClient.execute(httpPost);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void postcomment(View v){
		String postcontent = newcomment.getText().toString();
		JSONObject data = new JSONObject();
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		String userid = sharedPreferences.getString("userid", "");		
		try {
			data.put("dishid",id);
			data.put("postcontent", postcontent);
			data.put("userid", userid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		httpClient = new DefaultHttpClient();
		httpPost = new HttpPost(url3);
		//刷新Listview
		SimpleAdapter adapter = new SimpleAdapter(Onedish.this, getdata2(postcontent), R.layout.comment_list,
				new String []{"comment",}, new int[]{R.id.comment,} );
		commentlist.setAdapter(adapter);
		setListViewHeightBasedOnChildren(commentlist);
		
		try {
			httpPost.setEntity(new StringEntity(data.toString(),HTTP.UTF_8));
			response = httpClient.execute(httpPost);
			Toast.makeText(this,"提交评论", Toast.LENGTH_LONG).show();
			newcomment.setText("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	//设置listview的高度.
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
	

}
