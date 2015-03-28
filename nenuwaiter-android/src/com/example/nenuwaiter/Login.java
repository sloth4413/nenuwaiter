package com.example.nenuwaiter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/*约定：
使用SharedPreferences保存用户数据*/
public class Login extends Activity {
	private static final String url = "http://nenuwaiter.sinaapp.com/login/";
	private static final String url2 = "http://nenuwaiter.sinaapp.com/create/";
	private HttpClient httpCilent;
	private HttpResponse response;
	private HttpPost httpPost;
	private EditText userAccount;
	private EditText userPwd;
	private Button BtnLogin;
	private HttpEntity entity;
	static final int Re_Dialog = 0;
	private EditText newusername;
	private EditText newname;
	private EditText newaddress;
	private EditText newphonename;
	private EditText newpassword;
	private EditText newrepassword;
	//
	private TextView title_bar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		initView();
		title_bar.setText("登录");
		addListener();
		//传说中的代码，使的4.0可以请求网络
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());

		
	}
	private void initView(){
		userAccount = (EditText) findViewById(R.id.user);
		userPwd = (EditText) findViewById(R.id.password);
		BtnLogin = (Button) findViewById(R.id.button_ok);

		newusername = (EditText) findViewById(R.id.newusername);
		newname = (EditText) findViewById(R.id.newname);
		newaddress = (EditText) findViewById(R.id.newaddress);
		newphonename = (EditText) findViewById(R.id.newphonename);
		newpassword = (EditText) findViewById(R.id.newpassword);
		newrepassword = (EditText) findViewById(R.id.newrepassword);
		
		title_bar = (TextView) findViewById(R.id.title_bar_name);
	}
	private void addListener(){
		BtnLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String account = userAccount.getText().toString();
				String password = userPwd.getText().toString();
				login(account,password);
			}
		});
	}
	public void login(String account,String password){
		httpCilent = new DefaultHttpClient();
		try{
			httpPost = new HttpPost(url);
			JSONObject data = new JSONObject();
			try{
				data.put("username",account);
				data.put("password", password);
			}catch(JSONException e1){
				e1.printStackTrace();
			}
			httpPost.setEntity(new StringEntity(data.toString()));

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
//	             获取需求数据，name，phonenumber，address

	             if(status.equals("101")){
	            	 Toast.makeText(Login.this, "用户名不存在", Toast.LENGTH_SHORT).show();
	             }
	             else if(status.equals("102")){
	            	 Toast.makeText(Login.this, "密码错误", Toast.LENGTH_SHORT).show();
	             }
	             else if(status.equals("yes")){
		             String sname = datas.getString("name");
		             String sphonenumber = datas.getString("phonenumber");
		             String saddress = datas.getString("address");
		             String sid = datas.getString("id");
	            	 SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
	            	 Editor editor = sharedPreferences.edit();
	            	 editor.putString("name", sname);
	            	 editor.putString("phonenumber", sphonenumber);
	            	 editor.putString("address", saddress);
	            	 editor.putString("userid", sid);
	            	 editor.commit();
	            	 
	            	 
	            	 startActivity(new Intent(Login.this,Mainlist.class));
	         		 Login.this.finish();
	             }
	             else{
	            	 Toast.makeText(Login.this, "出错，请重试", Toast.LENGTH_SHORT).show();
	             }

	             

			 }
			 else{
				 Toast.makeText(Login.this, "出错，请重试", Toast.LENGTH_SHORT).show();
			 }
		}catch (Exception e) {
			Toast.makeText(Login.this, "网络出错，请重试", Toast.LENGTH_SHORT).show();
        }
		
		
	}
	//注册，对话框,
	public void onsetreButton(View v){
		showDialog(Re_Dialog);
	}
	@Override
	protected Dialog onCreateDialog(int id){
		switch(id){
		case Re_Dialog:
			LayoutInflater inflater =
			(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.re_djalog,(ViewGroup) findViewById(R.id.root));
//		final View textEntryView = inflater.inflate(R.layout.re_djalog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
//		builder.setTitle("PASSWORD");
		
			
		builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				// TODO Auto-generated method stub
				EditText anewusername = (EditText) layout.findViewById(R.id.newusername);
				EditText anewname = (EditText) layout.findViewById(R.id.newname);
				EditText anewaddress = (EditText) layout.findViewById(R.id.newaddress);
				EditText anewphonename = (EditText) layout.findViewById(R.id.newphonename);
				EditText anewpassword = (EditText) layout.findViewById(R.id.newpassword);
				EditText anewrepassword = (EditText) layout.findViewById(R.id.newrepassword);
				
				String newu = anewusername.getText().toString();
				String newn = anewname.getText().toString();
				String newh = anewphonename.getText().toString();
				String newa = anewaddress.getText().toString();
				String newp = anewpassword.getText().toString();
				String newrp = anewrepassword.getText().toString();
				Boolean flaginput = (newu==null||newu.equals("")||newn.equals("")||newn.equals("")||newh==null||newh.equals("")||newa==null||newa.equals("")||newp==null||newp.equals("")||newrp==null||newrp.equals(""));
				if(flaginput){
					Toast.makeText(Login.this, "输入不可为空", Toast.LENGTH_SHORT).show();
					try {
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);
						field.set(dialog, false);
					} catch (Exception e3) {
						e3.printStackTrace();
					}
				}

				else {if(newp.equals(newrp)){
					
					// 注册，网络请求
					httpCilent = new DefaultHttpClient();
					try{
						httpPost = new HttpPost(url2);
						JSONObject data = new JSONObject();
						try{
							data.put("username",newu);
							data.put("name",newn);
							data.put("phonenumber",newh);
							data.put("address",newa);
							data.put("password",newp);
						}catch(JSONException e1){
							e1.printStackTrace();
						}
						httpPost.setEntity(new StringEntity(data.toString(),HTTP.UTF_8));
//注册判断是否成功
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
				             
				             System.out.println("this is data"+datas.toString());
				             if(status.equals("a")){
									Toast.makeText(Login.this, "用户名已存在.", Toast.LENGTH_SHORT).show();
									try {
										Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
										field.setAccessible(true);
										field.set(dialog, false);
									} catch (Exception e3) {
										e3.printStackTrace();
									
				             }
				             }
				             
				             else if(status.equals("y")){
									Toast.makeText(Login.this, "注册成功", Toast.LENGTH_SHORT).show();
									try {
										Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
										field.setAccessible(true);
										field.set(dialog, true);
									} catch (Exception e3) {
										e3.printStackTrace();
									}
				             }else{
									Toast.makeText(Login.this, "出错，请重试", Toast.LENGTH_SHORT).show();
									try {
										Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
										field.setAccessible(true);
										field.set(dialog, false);
									} catch (Exception e3) {
										e3.printStackTrace();
									}
				             }
							
						}
						else{
							Toast.makeText(Login.this, "网络出错，请重试", Toast.LENGTH_SHORT).show();
							try {
								Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(dialog, false);
							} catch (Exception e3) {
								e3.printStackTrace();
							}
							
						}
						
						
					}catch(Exception e){
						
					}
				}
				else{

					Toast.makeText(Login.this, "两次输入密码不同", Toast.LENGTH_SHORT).show();
					try {
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);
						field.set(dialog, false);
					} catch (Exception e3) {
						e3.printStackTrace();
					}	
				}
				}
			}
		});
		builder.setNeutralButton("取消",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					field.setAccessible(true);
					field.set(dialog, true);
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			}
		});
		AlertDialog passworddialog = builder.create();
		return passworddialog;
		
		}
		return null;
	}

	

}
