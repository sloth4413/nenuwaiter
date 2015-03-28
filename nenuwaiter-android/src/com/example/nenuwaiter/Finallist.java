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
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Finallist extends Activity {
	private ListView finallist;
	private TextView sumprice;
	private TextView sumnum;
	
	private TextView title_bar;
	private TextView slide_info;
	String slide_welcome;
	
	private String userId;
	String url = "http://nenuwaiter.sinaapp.com/order/";
	private HttpClient httpCilent;
	private HttpResponse response;
	private HttpPost httpPost;
	private HttpEntity entity;
	private JSONObject data = new JSONObject();
	private JSONArray order_count =  new JSONArray();
	private JSONArray order_num = new JSONArray();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.finallist);
		doit();
	}
	private void doit(){
		finallist = (ListView) findViewById(R.id.finallist);
		sumprice = (TextView) findViewById(R.id.sumprice);
		sumnum = (TextView) findViewById(R.id.sumnum);
		
		title_bar = (TextView) findViewById(R.id.title_bar_name);
		title_bar.setText("订单");
		slide_info = (TextView) findViewById(R.id.slide_info);
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		slide_welcome = sharedPreferences.getString("name", "");
		slide_info.setText(slide_welcome);
		
		userId = sharedPreferences.getString("userid", "");
		
		SimpleAdapter adapter = new SimpleAdapter(this,getdata(),R.layout.final_list,
				new String[]{"sumnum_vlu","sumprice_vlu"},new int[]{R.id.sumnum,R.id.sumprice});
		finallist.setAdapter(adapter);
	}
	private List<Map<String, Object>> getdata(){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		//封装用户ID数据称json
		try{
			data.put("userId", userId);
			
		}catch(JSONException e){
			
		}
		httpPost = new HttpPost(url);
		httpCilent = new DefaultHttpClient();
		try{
			httpPost.setEntity(new StringEntity(data.toString(),HTTP.UTF_8));
			response = httpCilent.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
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
	            	order_count = datas.getJSONArray("order_count");
	            	order_num = datas.getJSONArray("order_num");
	            }else Toast.makeText(this, "网络出错，请重试", Toast.LENGTH_SHORT).show();
			}else Toast.makeText(this, "网络出错，请重试", Toast.LENGTH_SHORT).show();
		}catch(Exception e){
			Toast.makeText(this, "网络出错，请重试", Toast.LENGTH_SHORT).show();
		}
		int len = order_count.length();
		for(int i= 0;i<len;i++){
			try{
			map = new HashMap<String, Object>();
			map.put("sumnum_vlu",order_num.getString(i));
			map.put("sumprice_vlu",order_count.getString(i));
			
			list.add(map);
		    }catch(Exception e){
		    	
		    }
		}
		return list;
	}
	//滑动侧边栏,跳转
	public void jump1(View v){
		startActivity(new Intent(Finallist.this, Mainlist.class));
	}
	public void jump2(View v){
		startActivity(new Intent(Finallist.this, Shoplist.class));
	}
	public void jump3(View v){
//		startActivity(new Intent(Finallist.this, Finallist.class));
	}
	public void jump4(View v){
		startActivity(new Intent(Finallist.this,Coupon.class));
	}
	public void search(View v){
		EditText searchname = (EditText) findViewById(R.id.search);
		String searchcontent = searchname.getText().toString();
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		Editor editor  = sharedPreferences.edit();
		editor.putString("search", searchcontent);
      	editor.commit();
		startActivity(new Intent(Finallist.this,Search.class));
		
	}
	

}
