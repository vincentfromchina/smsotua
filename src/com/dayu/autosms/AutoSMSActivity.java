package com.dayu.autosms;

import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore.Images.Thumbnails;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;

import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsMessage;
import android.text.style.ReplacementSpan;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dayu.autosms.R;
import com.dayu.autosms.c.FolderFilePicker;
import com.dayu.autosms.c.FolderFilePicker.PickPathEvent;
import com.dayu.autosms.c.GernatorSMSText;
import com.dayu.autosms.c.DBHelper;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
  

public class AutoSMSActivity extends Activity
{

	final static String TAG = "autophone";
	String sendbuf = "send ok!";
	PendingIntent paIntent;
	PendingIntent deliverPI;
    SmsManager smsManager;
    EditText edt_phonenum;
    private String mfilePath="";
    String send_target[] = new String[10];
    int    send_num = 0;
    int    send_totalnum = 0;
	private DBHelper sqldb;
	static SmsTaskQuery m_SmsTaskQuery = null;
	static load_smstask m_loadsmstask = null;
	long filesize = 0;
	Object loadsmstask_lock = "共享锁";
    
    void jiaocheng()
    {
    	Intent excel = new Intent();		
		excel.setClass(AutoSMSActivity.this, WebActivity.class);
	    excel.putExtra("urls", "http://jsonok.jsp.fjjsp.net/gushiriji/gushiriji_wangye.jsp");
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

	      manager.listen(new MyPhoneListener(),PhoneStateListener.LISTEN_CALL_STATE);
	      String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
	      Intent sentIntent = new Intent(SENT_SMS_ACTION);  
	      paIntent = PendingIntent.getBroadcast(this, 0, sentIntent, 0); 
	      smsManager = SmsManager.getDefault();
	      
	      getApplicationContext().registerReceiver(new BroadcastReceiver() {  
	    	    @Override  
	    	    public void onReceive(Context _context, Intent _intent) {  
	    	        switch (getResultCode()) {  
	    	        case Activity.RESULT_OK:  
	    	        	Log.e(TAG,"发送成功"); 
	    	        break;  
	    	        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:  
	    	        	Log.e(TAG,"RESULT_ERROR_GENERIC_FAILURE");
	    	        break;  
	    	        case SmsManager.RESULT_ERROR_RADIO_OFF:  
	    	        	Log.e(TAG,"RESULT_ERROR_RADIO_OFF");
	    	        break;  
	    	        case SmsManager.RESULT_ERROR_NULL_PDU:  
	    	        	Log.e(TAG,"RESULT_ERROR_NULL_PDU");
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
				
				String test = "接下来的流程和普通短信一样，最终通过RILJ将短信发送出去，并且注册回调消息为EVENT_SEND_SMS_COMPLETE。"
						+ "也就是说，对于长短信而言，如果运营商不支持，那么就拆分为一个个普通短信然后逐条发送，如果运营商支持长短信，则会对每个分"
						+ "组短信添加SmsHeader的信息头，然后逐条发送。 所以当SMSDispatcher接收到EVENT_SEND_SMS_COMPLETE消息"
						+ "时，就说明，无论是普通短信或者长短信，都已经发送完毕。以上就是长短信的发送流程。";
			
				Log.e("TAG",String.valueOf(test.getBytes().length));
				
			//	smsManager.sendTextMessage("18620470826", null, test, paIntent, null); 
				
				ArrayList<String> divideContents = smsManager.divideMessage(test);   
		        
				ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(divideContents.size()); 
				
		        smsManager.sendMultipartTextMessage("13538948083", null, divideContents, null /*sentIntents*/, null); 
		        
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
	     
	     Button button1 = (Button)findViewById(R.id.button1);
	     button1.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				synchronized (loadsmstask_lock)
				{
					loadsmstask_lock.notify();
				}
								
			}
		});
	     
			
	     m_SmsTaskQuery = new SmsTaskQuery();
	     send_sms kk = new send_sms();
			kk.start();
			
	}
	
	void fenxi_file(File feFile)
	{
		
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
				Thread.sleep(15000);
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
					sleep(2000);
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
			File feFile = new File(mfilePath);
			Log.e(TAG, "open file  1");
			
			Log.e(TAG, "open file  2");
			if(feFile.canRead())
			{
				Log.e(TAG, "open file  3");
				FileReader frd = null;
				filesize = feFile.length();
				Log.e(TAG,"filesize is"+ String.valueOf(filesize));
				BufferedReader buffd = null;
				try
				{
					 frd = new FileReader(feFile);
					
					 buffd = new BufferedReader(frd);
					
					String tmp_str = "";
					       send_num = 0;
					long readbytes = 0;
			
					
					while((tmp_str=buffd.readLine())!=null)
					{   
						readbytes += tmp_str.getBytes().length+2;
						tmp_str = tmp_str.trim();
						if((tmp_str.length()>0)&&(tmp_str.length()==11)&&tmp_str.startsWith("1"))
						 {	
							send_target[send_num] = tmp_str;
							Log.e(TAG, send_target[send_num]);
							send_num++;
						 }
						
						
						if (send_num==10)
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
						Log.e(TAG,"readbytes is"+ String.valueOf(readbytes));
						Log.e(TAG, String.valueOf((float)readbytes/filesize*100)+"%");
						
					}
					
					
					
					Log.e(TAG, String.valueOf(send_num));
					
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
