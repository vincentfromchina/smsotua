package com.dayu.autosms;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class SetActivity extends Activity
{
	final static String TAG = "autosms";
	private static String serialid = "";
	TextView showinfo ;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set);
		
		showinfo = (TextView)findViewById(R.id.showinfo);
		
		Button btn_active = (Button)findViewById(R.id.button1);
		btn_active.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent excel = new Intent();		
				excel.setClass(SetActivity.this, WebActivity.class);
			    excel.putExtra("urls",
"http://jsonok.jsp.fjjsp.net/jiaocheng/autosms/active.jsp");
	  //http://jsonok.jsp.fjjsp.net/jiaocheng/autosms/daili/active_xxxxxxx.jsp
				startActivity(excel);
			}
		});
		
		Button btn_zhuce = (Button)findViewById(R.id.button2);
		btn_zhuce.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				doreg m_doreg = new doreg();
				m_doreg.start();
			 }
		});
		
		Button bt3 = (Button)findViewById(R.id.button3);
		bt3.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}
	
	/*
	 public  boolean isNetworkAvailable()
	    {
	        Context context = this.getApplicationContext();
	        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
	        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        
	        Boolean isconnect = false;
	        if (connectivityManager == null)
	        {
	        	isconnect = false;
	        }
	        else
	        {
	        	  State wifiState = null;  
			        State mobileState = null;  
			        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
			        wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();  
			        mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();  
			        if (wifiState != null && mobileState != null  
			                && State.CONNECTED != wifiState  
			                && State.CONNECTED == mobileState) {  
			        	    Speeh.networkok = true;
			        	    isconnect = true;
			        	    if( Pushapplication.isdebug )Log.e("loghere", "networkok=true")  ;  
			           
			        } else if (wifiState != null && mobileState != null  
			                && State.CONNECTED != wifiState  
			                && State.CONNECTED != mobileState) {  
			        	 Speeh.networkok = false;
		        	     isconnect = false;
		        	     if( Pushapplication.isdebug )   Log.e("loghere", "networkok=false")  ; 
			          
			        } else if (wifiState != null && State.CONNECTED == wifiState) {  
			      
			        	 Speeh.networkok = true;
			        	     isconnect = true;
			        	     if( Pushapplication.isdebug )   Log.e("loghere", "networkok=true")  ; 
			        }  
	        }
	        return isconnect;
	    }
	    
	    */

	
	
	private class doreg  extends Thread
	{

		@Override
		public void run()
		{
			Register();
			super.run();
		}
		
	}
	
	private void Register()
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				showinfo.setText("正在连接服务器...");
			}
		});
		
	 	HttpClient mHttpClient = new DefaultHttpClient();
	 	String Imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
		 String uri = getResources().getString(R.string.url)+"/AutoSms_Reg";
		 if (AutoSMSActivity.isdebug) Log.e(TAG, Imei);
		    HttpPost httppost = new HttpPost(uri);   
		    List<NameValuePair> params = new ArrayList<NameValuePair>();
		     // 添加要传递的参数
		    NameValuePair pair1 = new BasicNameValuePair("serialno", Imei);
		    params.add(pair1);
		   
		    HttpEntity mHttpEntity;
		 			try
		 			{
		 				mHttpEntity = new UrlEncodedFormEntity(params, "gbk");
		 			
		 				httppost.setEntity(mHttpEntity); 
		 				if (AutoSMSActivity.isdebug) Log.e(TAG, "发送数据");
		 			} catch (UnsupportedEncodingException e1)
		 			{
		 				// TODO Auto-generated catch block
		 				if (AutoSMSActivity.isdebug) Log.e(TAG, "数据传递出错了");
		 				e1.printStackTrace();
		 			}
		 		    		
		 			mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 4000);	
		 		     
		 		    HttpResponse httpresponse = null;  
		 		    try
		 			{
		 				httpresponse = mHttpClient.execute(httppost);
		 				
		 			   if (httpresponse.getStatusLine().getStatusCode()==200)	
		 			   {
		 				  String response = EntityUtils.toString(httpresponse.getEntity(), "utf-8");
		 				 final TextView tv1 = (TextView)findViewById(R.id.textView1);
		 				 JSONObject mJsonObject = new JSONObject(response);
		 				if (AutoSMSActivity.isdebug) Log.e(TAG, response);
						try
						{
							String resp = mJsonObject.getString("resp");
							
							switch (resp)
							{
							case "1":
								serialid = mJsonObject.getString("serialid");
							//	Toast.makeText(SetActivity.this, "激活成功", Toast.LENGTH_LONG).show();
								runOnUiThread(new Runnable()
								{
									public void run()
									{
										showinfo.setText("");
										tv1.setText("本设备机器码：\n"+serialid);
									}
								});
								break;
							case "2":
								serialid = mJsonObject.getString("serialid");
							//	Toast.makeText(SetActivity.this, "本设备已存在", Toast.LENGTH_LONG).show();
								runOnUiThread(new Runnable()
								{
									public void run()
									{
										showinfo.setText("");
										tv1.setText("本设备机器码：\n"+serialid);
									}
								});
								break;
							case "4":
								serialid = mJsonObject.getString("serialid");
								//Toast.makeText(SetActivity.this, "失败", Toast.LENGTH_LONG).show();
								runOnUiThread(new Runnable()
								{
									public void run()
									{
										showinfo.setText("");
										tv1.setText("本设备不允许激活！请更换设备");
									}
								});
								break;
							default:
								runOnUiThread(new Runnable()
								{
									public void run()
									{
										tv1.setText("激活失败，请重试");
									}
								});
								break;
							} 
							
						} catch (JSONException e)
						{
							e.printStackTrace();
						}
					 		
						if (AutoSMSActivity.isdebug) Log.e(TAG,response);
		 			   }
		 			  if (AutoSMSActivity.isdebug) Log.e(TAG,"rescode:"+httpresponse.getStatusLine().getStatusCode());
		 				
		 				
		 			} catch (ClientProtocolException e1)
					{
						e1.printStackTrace();
					} catch (IOException e1)
					{
						e1.printStackTrace();
						
						ShowOnUI("无法连接服务器，请检查网络");
						
						if (AutoSMSActivity.isdebug) Log.e(TAG, "sockettimeout");
						
					} catch (JSONException e1)
					{
						e1.printStackTrace();
					} 
	      }        
	
	void ShowOnUI(final String s)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				showinfo.setText(s);
			}
		});
	}

}
