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
    static String send_target[] = new String[10];
    static int    send_num = 0;
    static int    send_totalnum = 0;
	private DBHelper sqldb;
	static SmsTaskQuery m_SmsTaskQuery = null;
	static SmsTask m_SmsTask = null;
	static load_smstask m_loadsmstask = null;
	static long filesize = 0;
	final static Object loadsmstask_lock = "导入数据共享锁";
	final static Object sendsmstask_lock = "发送进程共享锁";
    static public Boolean isdebug = true;
    static public ProgressBar mProgressBar;
	HttpURLConnection urlConn = null;  
	static boolean cancelupdate = false;
    static boolean send_isstart = true;
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
         
	      manager.listen(new MyPhoneListener(),PhoneStateListener.LISTEN_CALL_STATE);
	      String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
	      Intent sentIntent = new Intent(SENT_SMS_ACTION);  
	      paIntent = PendingIntent.getBroadcast(this, 0, sentIntent, 0); 
	      smsManager = SmsManager.getDefault();
	      
	      final byte[] keyBytes = {
	        1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24
	      };
	      
	      final byte[] keyBytes2 = {
	  	        1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24
	  	      };
	      
	      String szSrc = "厉害了 is a 3DES test. 测试";  
	      
	       Log.e(TAG,"加密前的字符串:" + szSrc);  
	        
	        byte[] encoded = ThreeDES.encryptMode(keyBytes, szSrc.getBytes());  
	        Log.e(TAG,"加密后的字符串:" + new String(encoded));  
	  
	        byte[] srcBytes = ThreeDES.decryptMode(keyBytes2, encoded);  
	        Log.e(TAG,"解密后的字符串:" + (new String(srcBytes)));  
	      
	      getApplicationContext().registerReceiver(new BroadcastReceiver() {  
	    	    @Override  
	    	    public void onReceive(Context _context, Intent _intent) {  
	    	        switch (getResultCode()) {  
	    	        case Activity.RESULT_OK:  
	    	        	Log.e(TAG,"发送成功"); 
	    	        	 m_SmsTask.setTasksuccess(m_SmsTask.getTasksuccess()+1);
	    	        break;  
	    	        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:  
	    	        	Log.e(TAG,"RESULT_ERROR_GENERIC_FAILURE");
	    	        	m_SmsTask.setTaskfail(m_SmsTask.getTaskfail()+1);
	    	        break;  
	    	        case SmsManager.RESULT_ERROR_RADIO_OFF:  
	    	        	Log.e(TAG,"RESULT_ERROR_RADIO_OFF");
	    	        	m_SmsTask.setTaskfail(m_SmsTask.getTaskfail()+1);
	    	        break;  
	    	        case SmsManager.RESULT_ERROR_NULL_PDU:  
	    	        	Log.e(TAG,"RESULT_ERROR_NULL_PDU");
	    	        	m_SmsTask.setTaskfail(m_SmsTask.getTaskfail()+1);
	    	        break;  
	    	        }  
	    	    }  
	    	}, new IntentFilter(SENT_SMS_ACTION)); 
	      
	      //处理返回的接收状态   
	      String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";  
	      // create the deilverIntent parameter  
	      Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION).putExtra("smsid", "388");  
	       deliverPI = PendingIntent.getBroadcast(this, 0,  
	             deliverIntent, 0);  
	      getApplicationContext().registerReceiver(new BroadcastReceiver() {  
	         @Override  
	         public void onReceive(Context _context, Intent _intent) {  
	        	 Log.e(TAG,"对方接收状态返回");
	        	 switch (getResultCode())  
	             {  
	                 case  Activity.RESULT_OK:  
	                     Log.e(TAG ,  "RESULT_OK" );  
	                    
	                     break ;  
	                 case  Activity.RESULT_CANCELED:  
	                     Log.e(TAG ,  "RESULT_CANCELED" ); 
	                     
	                     break ;  
	             }   
	        	 
	        	 Bundle bundle = _intent.getExtras();
	             StringBuffer messageContent = new StringBuffer();
	             
	             if (bundle != null) {
	            	 byte recdata[] =(byte[]) bundle.get("pdu");
	            	 
	            	 for (int i = 0; i < recdata.length; i++)
					{
	            		 int a = recdata[i];
	            		 messageContent.append(Integer.toHexString(a));
	            		 messageContent.append(" ");
	            		 
					}
	            	 Log.e(TAG ,messageContent.toString());  
	            //	SmsMessage message1 = SmsMessage.createFromPdu(recdata);
	            	
	           // 	 Log.e(TAG,message.getDisplayMessageBody()+ message.getEmailFrom()+message.getEmailBody()+message.getMessageBody()+message.getOriginatingAddress());
	           
	            //	 Object[] pdus = (Object[]) bundle.get("pdu");
	                 
	                     SmsMessage message = SmsMessage.createFromPdu(recdata);
	                     String sender = message.getOriginatingAddress();
	                     Log.e(TAG,"sender: "+sender);
	                    
	                if ("10086".equals(sender) || "10010".equals(sender) ||
	                             "10001".equals(sender)) {
	                         messageContent.append(message.getMessageBody());
	                     }
	                 }
	                 if(!messageContent.toString().isEmpty()) {
	                     Log.e(TAG,"send message broadcast.");
	                     
	                     Log.e(TAG,messageContent.toString());

	                     Log.e(TAG, "send broadcast and abort");
//	                     abortBroadcast();
	                 }
	                 
	            }            
	        
	        
	      }, new IntentFilter(DELIVERED_SMS_ACTION));  
	      
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
				send_thread m_st = new send_thread();
				m_st.start();
			}
		});
	     
	     Button btn_fenxifile = (Button)findViewById(R.id.btn_fenxifile);
	     btn_fenxifile.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				m_loadsmstask = new load_smstask();
				m_loadsmstask.start();
				
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
				send_isstart = !send_isstart;
			   if (send_isstart )
			   {
				   synchronized (sendsmstask_lock)
					  {
						sendsmstask_lock.notifyAll();
					  }
				   btn_startorpuase.setText("暂停");
			   }else
			   { 
				   btn_startorpuase.setText("开始");
			   }				
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
			
	 //    m_SmsTaskQuery = new SmsTaskQuery();
	 //    send_sms m_sendsms = new send_sms();
	 //    m_sendsms.start();
			
		checkupdate ck = new checkupdate();
		ck.start();
			
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
	
	class unlock_sendsmsthread extends Thread
	{

		@Override
		public void run()
		{
		  while(true)	
		  {	if (m_SmsTaskQuery.query_sendlist_count()==0)
			{
				synchronized (loadsmstask_lock)
				{
					loadsmstask_lock.notify();
				}
			}else
			{
				Log.e(TAG, "the query not empty");
			}
			
			try
			{
				Thread.sleep(1500);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		}
		
	}
	
	
	class send_sms extends Thread
	{

		@Override
		public void run()
		{
			while (true)
			{
				if (!send_isstart)
				{
					synchronized (sendsmstask_lock)
					{
						try
						{
							sendsmstask_lock.wait();
						} catch (InterruptedException e)
						{
							
							e.printStackTrace();
						}
					}
				}
				
				SmsBase t_SmsBase = null;
				
				t_SmsBase = SmsTaskQuery.poll_sendlist();
				
			   if(t_SmsBase!=null)	
				{
				 //  smsManager.sendTextMessage(t_SmsBase.getSms_sendphone(), null, t_SmsBase.getSms_sendtext(), paIntent, null);
				  Log.e(TAG, t_SmsBase.getSms_sendphone()+","+t_SmsBase.getSms_sendtext());
				}else {
					if (m_SmsTaskQuery.query_sendlist_count()==0)
					{
						synchronized (loadsmstask_lock)
						{
							loadsmstask_lock.notify();
						}
					}else
					{
						Log.e(TAG, "the query not empty");
					}
				}
			   
				try
				{
					sleep(200);
				} catch (InterruptedException e)
				{
					
					e.printStackTrace();
				}
			
			}
		   /*
			for (int i = 0; i < send_num; i++)
			{
				String phonenum = edt_phonenum.getText().toString();
				
				String content = "this is from 宇宙，don't reply.";		
				smsManager.sendTextMessage(send_target[i], null, content, paIntent, null); 
			//	smsManager.sendDataMessage("18620470826", null, (short) 0, hah.getBytes(), paIntent, deliverPI); 
						
				try
				{
					sleep(2000);
				} catch (InterruptedException e)
				{
					
					e.printStackTrace();
				}
			
			}
			*/
			
		}
		 
	}
	
	class load_smstask extends Thread
	{

		@Override
		public void run()
		{
			m_SmsTask = new SmsTask();
			
			File feFile = new File(mfilePath);
			m_SmsTask.setTaskfilepath(feFile.getParent());
			m_SmsTask.setTaskfilename(feFile.getName());
			
			Log.e(TAG, "open file  AbsolutePath"+feFile.getAbsolutePath());
			
			Log.e(TAG, "open file  name" +feFile.getName());
			if(feFile.canRead())
			{
				Log.e(TAG, "open file  3");
				FileReader frd = null;
				filesize = feFile.length();
				Log.e(TAG,"filesize is"+ String.valueOf(filesize));
				BufferedReader buffd = null;
				try
				{
					Getnowtime m_Getnowtime = new Getnowtime();
					m_SmsTask.setTaskStarttime(m_Getnowtime.getnowtime("yyyy-M-d HH:mm"));
					
					frd = new FileReader(feFile);
					
					 buffd = new BufferedReader(frd);
					
					String tmp_str = "";
					send_num = 0;
					long readbytes = 0;
			
					
					while((tmp_str=buffd.readLine())!=null)
					{   
						if (!send_isstart)
						{
							synchronized (sendsmstask_lock)
							{
								try
								{
									sendsmstask_lock.wait();
								} catch (InterruptedException e)
								{
									
									e.printStackTrace();
								}
							}
						}
						readbytes += tmp_str.getBytes().length+2;
						tmp_str = tmp_str.trim();
						if((tmp_str.length()>0)&&(tmp_str.length()==11)&&tmp_str.startsWith("1"))
						 {	
							m_SmsTask.setTasktotal(send_num+1);
							send_target[send_num] = tmp_str;
							Log.e(TAG, send_target[send_num]);
							send_num++;
						 }
						
                        Log.e(TAG,"readbytes is"+ String.valueOf(readbytes));
						
						progessperct =  Math.round((float)readbytes/filesize*100);
						
						Log.e(TAG, String.valueOf(progessperct)+"%");
						
						if (send_num==10||progessperct>=100)
						{
							for (int i = 0; i < send_num; i++)
							{
								String sms_sendtext = "wello ni {|@|} 好东西 {|d|}";
								SmsBase t_smsbase = new SmsBase(send_target[i],GernatorSMSText.getSMSresult(sms_sendtext));
								m_SmsTaskQuery.insert_sendlist(t_smsbase);
							}
							
							synchronized (loadsmstask_lock)
							{
								try
								{
									loadsmstask_lock.wait();
								} catch (InterruptedException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							send_num=0;
							
						}
						
						
					}
					
					while (m_SmsTaskQuery.query_sendlist_count()!=0)
					{
						; //等待队列清空然后更新数据库任务表		
					}
					
				    m_Getnowtime = new Getnowtime();
					m_SmsTask.setTaskEndtime(m_Getnowtime.getnowtime("yyyy-M-d HH:mm"));
					
					sqldb.insert_smstask(m_SmsTask);
					

					
				} catch (FileNotFoundException e)
				{
					
					e.printStackTrace();
				} catch (IOException e)
				{
					
					e.printStackTrace();
				}finally {
					if (buffd!=null)
					{
						try
						{
							buffd.close();
						} catch (IOException e)
						{
							
							e.printStackTrace();
						}
					}
					
					if (frd!=null)
					{
						try
						{
							frd.close();
						} catch (IOException e)
						{
							
							e.printStackTrace();
						}
					}
				}
				
			}else
			{
				Log.e(TAG, "读取文件出错");
			}
		}
		
	}
	
	class send_thread extends Thread
	{

		@Override
		public void run()
		{
			Socket m_client = null;
			 m_client = new Socket();
		     InetSocketAddress isa1 = new InetSocketAddress("192.168.1.101", 8085);
		     OutputStream os1 = null;
		     
		     try
			{
				m_client.connect(isa1);
				
					Log.e(TAG,"conn ok");
					os1 = m_client.getOutputStream();
					
					if(os1!=null)
					 {							
						Log.e(TAG,"send pk start");
						os1.write(sendbuf.getBytes());		
					 }
				 }				
			 catch (IOException e)
			{
				Log.e(TAG,e.toString());
				e.printStackTrace();
			}finally {
				if (os1 != null)
				{
					try
					{
						os1.close();
					} catch (IOException e)
					{
						Log.e(TAG,e.toString());
						e.printStackTrace();
					}
				}
				try
				{
					m_client.close();
				} catch (IOException e)
				{
					Log.e(TAG,e.toString());
					e.printStackTrace();
				}
			}
			
		}
		
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
