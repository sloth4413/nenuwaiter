package com.example.nenuwaiter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Shoplist extends Activity {
	private TextView father;
	private TextView name;
	private TextView price;
	private TextView num;
	private ListView shoplist;
	
	private TextView title_bar;
	private TextView slide_info;
	private String slide_welcome;
	
	//用来存储推送短信的数据.
	private StringBuffer message = new StringBuffer();
	private String phonenumber = "13104409212";
	
	static final int Re_Dialog = 0;
	private JSONArray coupon_Money = new JSONArray();
	private JSONArray coupon_Id = new JSONArray();
	private String radioClickNum = "0";
	private int radioClickId = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.shoplist);
		initView();
		liststart();
		//传说中的代码，使的4.0可以请求网络
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
		
	}
	private void initView(){
		father = (TextView) findViewById(R.id.father);
		name = (TextView) findViewById(R.id.name);
		price = (TextView) findViewById(R.id.price);
		num = (TextView) findViewById(R.id.num);
		shoplist = (ListView) findViewById(R.id.shoplist);
		
		title_bar = (TextView) findViewById(R.id.title_bar_name);
		title_bar.setText("购物车");
		slide_info = (TextView) findViewById(R.id.slide_info);
		SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		slide_welcome = sharedPreferences.getString("name", "");
		slide_info.setText(slide_welcome);
		message.append(slide_welcome+' ');
		String phone = sharedPreferences.getString("phonenumber", "");
		String address = sharedPreferences.getString("address", "");
		message.append("电话:"+phone+"地址:"+address+"订购:");
	}
	private void liststart(){
		Delect adapter = new Delect(this,getdata(),R.layout.shop_list,
				new String[]{"id_vlu","father_vlu","name_vlu","price_vlu","num_vlu"},new int[]{R.id.id,R.id.father,R.id.name,R.id.price,R.id.num});
		shoplist.setAdapter(adapter);
	}
	private List<Map<String, Object>> getdata(){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		SQLiteDatabase db = openOrCreateDatabase("order.db", Context.MODE_PRIVATE, null);
		db.execSQL("create table if not exists orderlist(id INTEGER PRIMARY KEY AUTOINCREMENT,dishname VARCHAR,restaurant VARCHAR,price float,num SMALLINT)");
		Cursor c = db.rawQuery("select * from orderlist", new String[]{});
		c.moveToFirst();
		int len = c.getCount();
		for(int i= 0;!c.isAfterLast();i++){
			String id_vlu = c.getString(c.getColumnIndex("id"));
			String father_vlu = c.getString(c.getColumnIndex("restaurant"));
			String name_vlu = c.getString(c.getColumnIndex("dishname"));
			String price_vlu = c.getString(c.getColumnIndex("price"));
			String num_vlu = c.getString(c.getColumnIndex("num"));
			try{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id_vlu", id_vlu);
			map.put("father_vlu", father_vlu);
			map.put("name_vlu", name_vlu);
			map.put("price_vlu", price_vlu);
			map.put("num_vlu", num_vlu);
			
			list.add(map);
		    }catch(Exception e){
		    	
		    }
			c.moveToNext();
			
		}
		c.close();
		db.close();
		return list;
	}
	//下单.将数据存入新的表.
	public void onclick(View v){
		showDialog(Re_Dialog);
	}
	//滑动侧边栏,跳转
	public void jump1(View v){
		startActivity(new Intent(this, Mainlist.class));
	}
	public void jump2(View v){
//		startActivity(new Intent(this, Shoplist.class));
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
	private String sendMessage(String message,String phonenumber,float sendCount,int sendSum,int sendCoupon){
		HttpClient httpCilent;
		HttpResponse response;
		HttpEntity entity;
		HttpPost httpPost;
		String url = "http://nenuwaiter.sinaapp.com/sendmessage/";
		httpCilent = new DefaultHttpClient();
		try{
			httpPost = new HttpPost(url);
			JSONObject data = new JSONObject();
			data.put("message", message);
			data.put("phonenumber", phonenumber);
			data.put("sendCount", sendCount);
			data.put("sendSum", sendSum);
			data.put("sendCoupon", sendCoupon);
			SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
			String sendUserId = sharedPreferences.getString("userid", "");
			data.put("sendUserId",sendUserId);
			httpPost.setEntity(new StringEntity(data.toString(),HTTP.UTF_8));
			response = httpCilent.execute(httpPost);
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
	             return status;
			}else{
				return "102";
			}
		}catch(Exception e){
			return "101";
		}
		
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case Re_Dialog:
			//处理layout
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater =
				(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.coupon_select, null, false);
			//获取优惠券信息
			String url = "http://nenuwaiter.sinaapp.com/coupon/";
			HttpClient httpCilent;
			HttpResponse response;
			HttpPost httpPost;
			HttpEntity entity;
			JSONObject data = new JSONObject();
			SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
			String userId = sharedPreferences.getString("userid", "");
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
		            	coupon_Money = datas.getJSONArray("money");
		            	coupon_Id = datas.getJSONArray("couponId");
		            }else Toast.makeText(this, "网络出错，请重试", Toast.LENGTH_SHORT).show();
				}else Toast.makeText(this, "网络出错，请重试", Toast.LENGTH_SHORT).show();
			}catch(Exception e){
				Toast.makeText(this, "网络出错，请重试", Toast.LENGTH_SHORT).show();
			}
			RadioGroup radiogroup = (RadioGroup) layout.findViewById(R.id.coupon_select);
			int len = coupon_Money.length();
			for(int i=0;i<len;i++){
				RadioButton newButton = new RadioButton(Shoplist.this);
				try{
				String text = (i+1)+". 优惠券"+coupon_Money.getString(i)+"元";
				newButton.setText(text);
				newButton.setId(coupon_Id.getInt(i));
				}catch(Exception e){
					
				}
				radiogroup.addView(newButton);
			}
			//radiobutton监听处理
			radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				
				@Override
				public void onCheckedChanged(RadioGroup arg0, int arg1) {
					RadioButton clickRaido = (RadioButton)layout.findViewById(arg1);
					String clickStr = clickRaido.getText().toString();
					String clickId = clickStr.substring(0, 1);
					radioClickId = arg1;
					radioClickNum = clickId;
				}
				
			});
//			//处理layout
//			LayoutInflater inflater =
//				(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			final View layout = inflater.inflate(R.layout.coupon_select,(ViewGroup) findViewById(R.id.root2));
			
			builder.setView(layout);
			
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//处理下单
					int sumnum = 0;
					float sumprice = 0.0f;
					int numone = 0;
					float priceone = 0f;
					String nameone;
					SQLiteDatabase db = openOrCreateDatabase("order.db", Context.MODE_PRIVATE, null);
					Cursor c1 = db.rawQuery("select * from orderlist", new String[]{});
					c1.moveToFirst();
					for(int i= 0;!c1.isAfterLast();i++){
						numone = c1.getInt(c1.getColumnIndex("num"));
						priceone = c1.getFloat(c1.getColumnIndex("price"));
						//将名字.数量写入message;
						nameone = c1.getString(c1.getColumnIndex("dishname"));
						message.append(" "+nameone+"数量:"+numone+"份.");
						sumnum += numone;
						sumprice += numone*priceone;
						c1.moveToNext();
					}
					int selectCoupon = Integer.parseInt(radioClickNum);
					//判断是否有优惠券
					if (radioClickId == 0){
						message.append(sumprice+"元");
					}else{
					String selectCouponV  = new String();
					try {
						selectCouponV = coupon_Money.getString(selectCoupon-1);
					} catch (JSONException e) {
					}
					int selectCouponM = Integer.parseInt(selectCouponV);
					if (sumprice<=selectCouponM){
						message.append("0元["+selectCouponV+"元优惠]");
					}else message.append((sumprice-selectCouponM)+"元["+selectCouponV+"元优惠]");
					}
					message.append("【东师waiter】");
					String send = message.toString();
//					Toast.makeText(Shoplist.this, send, Toast.LENGTH_SHORT).show();
					c1.close();
					db.close();
					String status = sendMessage(send, phonenumber,sumprice,sumnum,radioClickId);
					if(status.equals("0")){
						Toast.makeText(Shoplist.this, "下单成功", Toast.LENGTH_LONG).show();
						SQLiteDatabase db2 = openOrCreateDatabase("order.db", Context.MODE_PRIVATE, null);
						db2.execSQL("delete from orderlist;");
						db2.execSQL("update sqlite_sequence set seq=0 where name='orderlist'");
						db2.close();
						startActivity(new Intent(Shoplist.this,Finallist.class));
					}else{
						Toast.makeText(Shoplist.this, "出错 "+status, Toast.LENGTH_LONG).show();
						try {
							Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
							field.setAccessible(true);
							field.set(dialog, false);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				}
			});
			builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					try {
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);
						field.set(dialog, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			AlertDialog passworddialog = builder.create();
			return passworddialog;
		}
		return null;
	}
	

}
