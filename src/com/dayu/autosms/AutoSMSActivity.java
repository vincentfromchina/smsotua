package com.dayu.autosms;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore.Images.Thumbnails;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;

import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsMessage;
import android.text.style.ReplacementSpan;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.dayu.autosms.R;
import com.dayu.autosms.c.FolderFilePicker;
import com.dayu.autosms.c.FolderFilePicker.PickPathEvent;
import com.dayu.autosms.m.SmsBase;
import com.dayu.autosms.m.SmsTask;
import com.dayu.autosms.m.SmsTaskQuery;
import com.dayu.autosms.c.GernatorSMSText;
import com.dayu.autosms.c.Getnowtime;
import com.dayu.autosms.dummy.ThreeDES;
import com.dayu.autosms.c.DBHelper;

import android.R.integer;
import android.R.mipmap;
import android.R.string;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;
  

public class AutoSMSActivity extends Activity
{

	final static String TAG = "autophone";
	String sendbuf = "send ok!";
	PendingIntent paIntent;
	PendingIntent deliverPI;
    SmsManager smsManager;
    EditText edt_phonenum;
    static private String mfilePath="",owner="";
   
	private DBHelper sqldb;
	
	static long filesize = 0;
	
    static public Boolean isdebug = true;
    static public ProgressBar mProgressBar;
	HttpURLConnection urlConn = null;  
	static boolean cancelupdate = false;
	private static int progessperct = 0;
	private static final int DOWNLOAD_ING = 37, DOWNLOAD_FINISH = 39;
	private static String mSavepath = "", apkurl = "", apkname = "", apkversion = "";
    
    void jiaocheng()
    {
    	Intent excel = new Intent();		
		excel.setClass(AutoSMSActivity.this, WebActivity.class);
	    excel.putExtra("urls", "http://jsonok.jsp.fjjsp.net/autosms/jiaocheng.jsp");
		startActivity(excel);
    }
    
    public void pickFile(View v) {
		FolderFilePicker picker = new FolderFilePicker(this,
				new PickPathEvent() {

					@Override
					public void onPickEvent(String resultPath) {
						mfilePath = resultPath;
						if (mfilePath==null)
						{
							Toast.makeText(AutoSMSActivity.this, "请选择文件",
									Toast.LENGTH_LONG).show();
						}else{
						
						TextView tv1 = (TextView)findViewById(R.id.textView1);
						tv1.setText("文件："+mfilePath+",请点击分析文件");
						
						}
					}
				}, "txt","xls","TXT","XLS");  //不定长参数使用方法
		picker.show();
	}
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		 JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
         JPushInterface.init(this);     		// 初始化 JPush
         
         sqldb = new DBHelper(getBaseContext(),"smstask.db", null) ;
	     
		 Cursor c = null;
		 c = sqldb.query_count();
		 if (c!=null)	
		  {
			 c.moveToFirst();
			 int count = c.getInt(0);
			 c.close();
			 if (count==0)
			 {
				 Toast.makeText(getBaseContext(), "无数据，请先导入文件", Toast.LENGTH_LONG).show();
				
			 }
		  }
         
		 
         
	      manager.listen(new MyPhoneListener(),PhoneStateListener.LISTEN_CALL_STATE);
	   
	      
	      edt_phonenum = (EditText)findViewById(R.id.edt_phonenum);
	      
	      Button btn_openfile = (Button)findViewById(R.id.btn_openfile);
	      btn_openfile.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				pickFile(v);
				
			}
		});
	      
	     Button btn_call = (Button)findViewById(R.id.btn_call);
	     btn_call.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
				String phonenum = edt_phonenum.getText().toString();
				if(edt_phonenum.getText().toString().equals(""))
				{
					Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+10011));  
	                startActivity(intent);  
				}else
				{
					
					Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+phonenum));  
	                startActivity(intent); 
	               
				}
				
			}
		});
	     
	     Button btn_sms = (Button)findViewById(R.id.btn_sms);
	     btn_sms.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				/*
				String test = "接下来的流程和普通短信一样，最终通过RILJ将短信发送出去，并且注册回调消息为EVENT_SEND_SMS_COMPLETE。"
						+ "也就是说，对于长短信而言，如果运营商不支持，那么就拆分为一个个普通短信然后逐条发送，如果运营商支持长短信，则会对每个分"
						+ "组短信添加SmsHeader的信息头，然后逐条发送。 所以当SMSDispatcher接收到EVENT_SEND_SMS_COMPLETE消息"
						+ "时，就说明，无论是普通短信或者长短信，都已经发送完毕。以上就是长短信的发送流程。";
			    */
				
				String test = "联通收到吗？";
				Log.e("TAG",String.valueOf(test.getBytes().length));
				
				smsManager.sendTextMessage("13538948083", null, test, paIntent, null); 
				
				ArrayList<String> divideContents = smsManager.divideMessage(test);   
		        
				ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(divideContents.size()); 
				
		    //    smsManager.sendMultipartTextMessage("13538948083", null, divideContents, null sentIntents, null); 
		        
				/*
				if (send_num>0)
				   {
					  send_sms mt_sms = new send_sms();
						mt_sms.start();
				   }
				*/
			}
		});
	     
	     Button btn_sendpk = (Button)findViewById(R.id.btn_sendpk);
	     btn_sendpk.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
	     
	     Button btn_fenxifile = (Button)findViewById(R.id.btn_fenxifile);
	     btn_fenxifile.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
				
			}
		});
	     
	     Button btn_content_set = (Button)findViewById(R.id.btn_content_set);
	     btn_content_set.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent itopen = new Intent();
				itopen.setClass(AutoSMSActivity.this, ContentSetActivity.class);
				startActivity(itopen);
			}
		});
	     
	     final Button btn_startorpuase = (Button)findViewById(R.id.btn_startorpuase);
	     btn_startorpuase.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
		   }
	 });
	     
	     Button btn_managecontentplate = (Button)findViewById(R.id.btn_managecontentplate);
	     btn_managecontentplate.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent mIntent = new Intent();
				mIntent.setClass(AutoSMSActivity.this, ManagercontentplateActivity.class);
				startActivity(mIntent);
			}
		});
	     
	     Button btn_cpdb = (Button)findViewById(R.id.btn_cpdb);
	     btn_cpdb.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				copydbfile();				
			}
		});
	     
	     Button btn_jiaocheng = (Button)findViewById(R.id.btn_jiaocheng);
	     btn_jiaocheng.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				jiaocheng();
				
			}
		});
	     
	     Button btn_managertask = (Button)findViewById(R.id.btn_managertask);
	     btn_managertask.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent mIntent = new Intent();
				mIntent.setClass(AutoSMSActivity.this, ManagertaskActivity.class);
				startActivity(mIntent);
				
			}
		});
	     
	     Button btn_openaddtask = (Button)findViewById(R.id.btn_openaddtask);
	     btn_openaddtask.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent mIntent = new Intent();
				mIntent.setClass(AutoSMSActivity.this, AddsmstaskActivity.class);
				startActivity(mIntent);
				
			}
		});
			
	//检查软件版本
	//	checkupdate ck = new checkupdate();
	//	ck.start();
			
	}
	
	public static String getMD5(String val)
    {  
	        MessageDigest md5;
			try
			{
				md5 = MessageDigest.getInstance("MD5");
				 md5.update(val.getBytes());  
			        byte[] m = md5.digest();//加密  
			        StringBuffer sb = new StringBuffer();  
			         for(int i = 0; i < m.length; i ++){  
			          sb.append(m[i]);  
			         }  
			         return sb.toString();  
			} catch (NoSuchAlgorithmException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
	       return "1";
	 }  
	
	void copydbfile()
	{
		String dbfilepath = getApplication().getDatabasePath("smstask.db").toString();
		sqldb.copyDataBaseToSD(dbfilepath);
	}
	
	public class checkupdate extends Thread
	 {

		@Override
		public void run()
		{
			 String resultData=""; 
			 
			 try
			{
				Thread.sleep(12000);
			} catch (InterruptedException e2)
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			  if ( HttpURLConnection_update()==200)
				{
					try
					{
						InputStreamReader in;
						in = new InputStreamReader(urlConn.getInputStream());
						BufferedReader buffer = new BufferedReader(in);
						String inputLine = null;
						while (((inputLine = buffer.readLine()) != null))
						{
							resultData += inputLine;
						}
					} catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					if (AutoSMSActivity.isdebug)
						Log.e("gps", "resultData:" + resultData);

					JSONObject mJsonObject;
					try
					{
						mJsonObject = new JSONObject(resultData);
						 apkname = mJsonObject.getString("apkname");
						 apkurl = mJsonObject.getString("apkurl");
						 apkversion = mJsonObject.getString("apkversion");
						 
						 try
						{
							int cunversion = getApplicationContext().getPackageManager().getPackageInfo(AutoSMSActivity.this.getPackageName(), 0).versionCode;
							if (cunversion < Integer.valueOf(apkversion))
							{
								shownoticedialog();
							}
						} catch (NameNotFoundException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			super.run();
		}
		
	 }
	 
	 private void shownoticedialog()
	 {
		 final Builder adAlertDialog = new Builder(AutoSMSActivity.this);
		 adAlertDialog.setMessage("当前软件版本太旧，需要更新");
		 adAlertDialog.setTitle("软件更新");
		 adAlertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				showdownloaddialog();
			}
		});
		 
		 runOnUiThread( new Runnable()
		{
			public void run()
			{
				 Dialog noticedialog = adAlertDialog.create();
				 noticedialog.show();
			}
		});
		
	 }
	 
	 private void showdownloaddialog()
	 {
		 Builder adAlertDialog = new Builder(AutoSMSActivity.this);
		 final LayoutInflater mInflater  = LayoutInflater.from(getApplicationContext());
		View v = mInflater.inflate(R.layout.updatedialog, null);
		mProgressBar = (ProgressBar) v.findViewById(R.id.update_progressBar);
		
		 adAlertDialog.setView(v);
		 adAlertDialog.setTitle("下载进度");
		 adAlertDialog.setPositiveButton("取消", new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				cancelupdate = true;
			}
		});
		 
		 Dialog downloaddialog = adAlertDialog.create();
		 downloaddialog.show();
		 
		 Downloadapk mdDownloadapk = new Downloadapk();
		 mdDownloadapk.setcontext(downloaddialog);
		 mdDownloadapk.start();
	 }
		 
 private class Downloadapk extends Thread
	  {
			 Dialog dialog;
			 public void setcontext(Dialog dialog)
			 {
				 this.dialog = dialog;
			 }
			 
			@Override
			public void run()
			{
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					String sdpath = Environment.getExternalStorageDirectory()+"/";
					mSavepath = sdpath +"download/";
					if (isdebug) Log.e("gps", mSavepath);
					
					try
					{
						URL downurl = new URL(apkurl);
						if (isdebug) Log.e("gps", downurl.toString());
						HttpURLConnection conn = (HttpURLConnection) downurl.openConnection();
						conn.connect();
						int apklength = conn.getContentLength();
						InputStream is = conn.getInputStream();
						
						File file = new File(mSavepath);
						if (!file.exists())
						{
							file.mkdir();
						}
						
						File apkfile = new File(apkname);
						FileOutputStream fos = new FileOutputStream(mSavepath+apkfile);
						int count = 0;
						byte buf[] = new byte[1024];
						do
						{
							int numred = is.read(buf);
							count += numred;
							progessperct =(int) (((float)count/apklength)*100);
							mHandler.sendEmptyMessage(DOWNLOAD_ING);
							if(numred<=0)
							{
								mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
								dialog.dismiss();
								break;
							}
							
							fos.write(buf,0,numred);
							
						} while (!cancelupdate);
						if (fos!=null) fos.close();
						if (is!=null) is.close();
						if (conn!=null) conn = null;
						
					} catch (MalformedURLException e)
					{
						Toast.makeText(AutoSMSActivity.this, "下载失败", Toast.LENGTH_LONG).show();
						dialog.dismiss();
						e.printStackTrace();
					} catch (IOException e)
					{
						Toast.makeText(AutoSMSActivity.this, "下载失败", Toast.LENGTH_LONG).show();
						dialog.dismiss();
						e.printStackTrace();
					}
				}
					
					super.run();
			}
	}

	private void updateprogessbar(int process)
		{
			mProgressBar.setProgress(process);
		}
		/**
			 * 比较传进来的时间与实际时间的大小，如果当前时间比参数大，返回true，小则返回false
			 * @param time
			 * @return boolean
			 */
	public boolean bijiaotime(String time1,String time2) //比较当前系统时间与传进来的时间大小
			{
				   
				   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				    
				   Date dat1,dat2 = null;
				try
				{
					dat1 = df.parse(time1);
					dat2 = df.parse(time2);
					
					if ((dat1.getTime()-dat2.getTime())>0)
					   {
						   return true;
					   }
					   else {
						  return false;
					   }
				} catch (ParseException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				return false;
  }
			
   private int HttpURLConnection_update()
			 {  
				 int recode = 0;
			        try{  
			            //通过openConnection 连接  
			            URL url = new java.net.URL(getResources().getString(R.string.url)+"/autosms/updateversion.html");  
			            urlConn=(HttpURLConnection)url.openConnection();  
			            //设置输入和输出流   
			            urlConn.setDoOutput(true);  
			            urlConn.setDoInput(true);  
			              
			            urlConn.setRequestMethod("POST");  
			            urlConn.setUseCaches(false);  
			            urlConn.setReadTimeout(3000);
			            urlConn.setConnectTimeout(3000);
			            // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的    
			            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");    
			            // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，  
			            // 要注意的是connection.getOutputStream会隐含的进行connect。    
			            urlConn.connect();  
			            //DataOutputStream流  
			            DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());  
			            //要上传的参数  
			            String content = "owner=" + URLEncoder.encode(owner, "GBK");   
			            //将要上传的内容写入流中  
			            out.writeBytes(content);     
			            //刷新、关闭  
			            out.flush();  
			            out.close();     

			            recode = urlConn.getResponseCode();
			            
			            if (AutoSMSActivity.isdebug) Log.e("gps", String.valueOf(recode));
			        }catch(Exception e){  
			            
			            e.printStackTrace();  
			        }  
			        
			        return recode;
  }  		
 
	private Handler mHandler = new Handler()
	  {

		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg!=null)
			{
				switch (msg.what)
				{
			   /*	
				case BINDPHONE:
					setserial();
					break;
				case REFRESHGPS:
					updatemap();
					break;
				case doplay:
					doplay(msg.arg1);
					break;
				*/
				case DOWNLOAD_ING:
					updateprogessbar(AutoSMSActivity.progessperct);
					break;
				case DOWNLOAD_FINISH:
					installapk();
					break;
				default:
					break;
				}
			}
		}

	   };
	   
    private void installapk()
		{
			File apkfile = new File(mSavepath,apkname);
			if (!apkfile.exists())
			{
				return;
			}
			Intent ins = new Intent(Intent.ACTION_VIEW);
			ins.setDataAndType(Uri.parse("file://"+apkfile), "application/vnd.android.package-archive");
			startActivity(ins);
 }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 class MyPhoneListener extends PhoneStateListener{

	      /**

	       * 当电话状态改变了将会执行该方法

	       */

	      @Override

	      public void onCallStateChanged(int state, String incomingNumber) {

	         Log.i(TAG,"incomingNumber:"+incomingNumber);

	         switch(state) {

	         case TelephonyManager.CALL_STATE_IDLE:

	            Log.i(TAG,"CALL_STATE_IDLE");

	            break;

	         case TelephonyManager.CALL_STATE_OFFHOOK:

	            Log.i(TAG,"CALL_STATE_OFFHOOK");

	            break;

	         case TelephonyManager.CALL_STATE_RINGING:

	            Log.i(TAG,"CALL_STATE_RINGING");
	            /*
	            try {
	                Method method = Class.forName("android.os.ServiceManager")
	                        .getMethod("getService", String.class);
	               
	                IBinder binder = (IBinder) method.invoke(null, new Object[]{TELEPHONY_SERVICE});
	               ITelephony telephony = ITelephony.Stub.asInterface(binder);
	                telephony.answerRingingCall();               
	            } catch (NoSuchMethodException e) {
	                Log.d("Sandy", "", e);
	            } catch (ClassNotFoundException e) {
	                Log.d("Sandy", "", e);
	            }catch (Exception e) {
	                Log.d("Sandy", "", e);
	                try{
	                    Log.e("Sandy", "for version 4.1 or larger");
	                    Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
	                    KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
	                    intent.putExtra("android.intent.extra.KEY_EVENT",keyEvent);

	                    sendOrderedBroadcast(intent,"android.permission.CALL_PRIVILEGED");
	                } catch (Exception e2) {
	                    Log.d("Sandy", "", e2);
	                    Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
	                               KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
	                               meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT,keyEvent);
	                               sendOrderedBroadcast(meidaButtonIntent, null);
	                }
	            }
	            */
	            break;

	         }

	      }

	   }
	/*
	BroadcastReceiver br1 = new BroadcastReceiver()
	{	
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String phoneNumber = intent.getStringExtra(  
					TelephonyManager.EXTRA_INCOMING_NUMBER);  
					        TelephonyManager telephony = (TelephonyManager)context.getSystemService(  
					Context.TELEPHONY_SERVICE);  
					        int state = telephony.getCallState();  
					        
					        switch(state){  
					        case TelephonyManager.CALL_STATE_RINGING:  
					            Log.i(TAG, "[Broadcast]等待接电话="+phoneNumber);  
					             
					            break;  
					        case TelephonyManager.CALL_STATE_IDLE:  
					            Log.i(TAG, "[Broadcast]电话挂断="+phoneNumber);  
					            break;  
					        case TelephonyManager.CALL_STATE_OFFHOOK:  
					            Log.i(TAG, "[Broadcast]通话中="+phoneNumber);  
					            break;  
					        } 
		}
		
	};
   */
}
