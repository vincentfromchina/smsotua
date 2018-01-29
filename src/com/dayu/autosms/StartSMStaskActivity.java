package com.dayu.autosms;

import android.os.Bundle;

import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.json.JSONException;
import org.json.JSONObject;

import com.dayu.autosms.c.DBHelper;
import com.dayu.autosms.c.GernatorSMSText;
import com.dayu.autosms.c.Getnowtime;
import com.dayu.autosms.m.SmsBase;
import com.dayu.autosms.m.SmsTask;
import com.dayu.autosms.m.SmsTaskQuery;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class StartSMStaskActivity extends Activity
{
	final static String TAG = "autosms";
	PendingIntent paIntent;
	PendingIntent deliverPI;
    SmsManager smsManager;
  //  String send_target[];
    HashMap<Integer, String[]> send_target;
    int    send_num = 0;
    static int    send_totalnum = 0;
    static int    send_success = 0;
    static int    send_fail = 0;
    static int    send_interval = 3;
    private static int progessperct = 0;
	private DBHelper sqldb;
	static private String mfilePath="";
	static int taskid;
	private Cursor m_Cursor;
	static SmsTaskQuery m_SmsTaskQuery = null;
    SmsTask m_SmsTask = null;
	static send_sms m_sendsms = null;
    load_smstask m_loadsmstask = null;
	static ProgressBar mProgressBar;
	static EditText edt_sendinteval;
	static Button btn_sendpause;
	static Button btn_start;
	static Button btn_sendstop;
	static boolean send_isstart = false;
	static boolean load_finish = false;
	static boolean send_finish = false;
	static long filesize = 0;
	final static Object loadsmstask_lock = "导入数据共享锁";
	final static Object sendsmstask_lock = "发送进程共享锁";
    static TextView tv_sendstatus;
    static TextView tv_successorfail;
    static TextView tv_showactive;
    BroadcastReceiver brc_smssendstatus;
    String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
    Intent sentIntent = new Intent(SENT_SMS_ACTION); 
	static boolean teac = false;
	static byte[] signfromdb ;
	static EditText  edt_showresult;
	static String[] extend_info;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_smstask);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Bundle bundle = this.getIntent().getExtras();
		taskid = bundle.getInt("taskid");
		
		new Thread(r_sign).start();
		
		tv_showactive = (TextView)findViewById(R.id.tv_showactive);
		mProgressBar = (ProgressBar)findViewById(R.id.pbr);
		tv_sendstatus = (TextView)findViewById(R.id.tv_sendstatus);
		edt_sendinteval = (EditText)findViewById(R.id.edt_sendinteval);
		tv_successorfail = (TextView)findViewById(R.id.tv_successorfail);
		
		edt_sendinteval.setSelection(edt_sendinteval.getText().length()); //将光标设置在文本最后面
		edt_sendinteval.setEnabled(true);
		
		edt_sendinteval.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void afterTextChanged(Editable s)
			{
				edt_sendinteval.setSelection(edt_sendinteval.getText().length());
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// TODO Auto-generated method stub
				
			}
		});
		
	
		
		sqldb = new DBHelper(StartSMStaskActivity.this, "smstask.db", null);
		m_SmsTask = new SmsTask();
		m_SmsTask.setTaskid(taskid);
		
		if (AutoSMSActivity.isdebug) Log.e(TAG,"m_SmsTask.getTaskfail()"+ m_SmsTask.getTaskfail());
	   
		init_task();
		
	    btn_start = (Button)findViewById(R.id.btn_start);
		btn_start.setVisibility(send_isstart ? View.INVISIBLE : View.VISIBLE);
		btn_start.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				check_sendinteval();
				
				edt_sendinteval.setSelection(edt_sendinteval.getText().length());
				
				tv_successorfail.setText("发送成功：   发送失败：");
				
				send_isstart = true;
				load_finish = false;
				SmsTaskQuery.init_sendlist();
			//     send_target = new String[10];
				send_target = new HashMap<>();
			     send_num = 0;
			     send_success = 0;
			     send_fail = 0;
			     send_totalnum = 0;
			     progessperct = 0;

			     if (AutoSMSActivity.isdebug) Log.e(TAG, "send_totalnum" + send_totalnum);
			     m_SmsTask.setTasktotal(0);
                 m_SmsTask.setTaskfail(0);
                 m_SmsTask.setTasksuccess(0);
                 if (AutoSMSActivity.isdebug)  Log.e(TAG,"m_SmsTask.getTaskfail()2:"+ m_SmsTask.getTaskfail());
			     
			      if (m_sendsms==null)
					{
						 m_sendsms = new send_sms();
					     m_sendsms.start();
					}else {
						if (AutoSMSActivity.isdebug) Log.e(TAG, "m_sendsms is not null");
					}
			      
			    if (m_SmsTaskQuery==null)
				{
			    	  m_SmsTaskQuery = new SmsTaskQuery();
				}else {
					if (AutoSMSActivity.isdebug) Log.e(TAG, "m_SmsTaskQuery is not null");
				}
			    if (send_isstart )
				   {
					   synchronized (sendsmstask_lock)
						  {
						   if (AutoSMSActivity.isdebug) Log.e(TAG, "通知你解锁了："+Thread.currentThread().getId());
							sendsmstask_lock.notifyAll();
						  }
				   }
				
				//btn_sendstop.setVisibility(View.VISIBLE);
				btn_sendpause.setVisibility(View.VISIBLE);
				btn_start.setVisibility(View.INVISIBLE);
				edt_sendinteval.setEnabled(false);
				
                m_loadsmstask = new load_smstask();
				m_loadsmstask.start();
										
			}
		});
				
		
	    btn_sendstop = (Button)findViewById(R.id.btn_sendstop);
	//	btn_sendstop.setVisibility(send_isstart ? View.VISIBLE : View.INVISIBLE);
		btn_sendstop.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
			  if (m_loadsmstask!=null)
			   {
				  if (m_loadsmstask.isAlive())
					{
					  AlertDialog isExit = new AlertDialog.Builder(StartSMStaskActivity.this).create();  
			            // 设置对话框标题  
			            isExit.setTitle("短信群发王");  
			            // 设置对话框消息  
			            isExit.setMessage("发送任务正在执行，要退出吗？");  
			            // 添加选择按钮并注册监听  
			            isExit.setButton("朕确定", listener);  
			            isExit.setButton2("取消了", listener);  
			            // 显示对话框  
			            isExit.show();
				   		
			            if (AutoSMSActivity.isdebug) Log.e(TAG, "m_loadsmstask.isAlive");
					}else
					{
						 // 创建退出对话框  
			            AlertDialog isExit = new AlertDialog.Builder(StartSMStaskActivity.this).create();  
			            // 设置对话框标题  
			            isExit.setTitle("短信群发王");  
			            // 设置对话框消息  
			            isExit.setMessage("确定要退出吗？");  
			            // 添加选择按钮并注册监听  
			            isExit.setButton("朕确定", listener);  
			            isExit.setButton2("取消了", listener);  
			            // 显示对话框  
			            isExit.show();  
			            if (AutoSMSActivity.isdebug) Log.e(TAG, "m_loadsmstask not Alive");
					}
			   }else {
				   // 创建退出对话框  
		            AlertDialog isExit = new AlertDialog.Builder(StartSMStaskActivity.this).create();  
		            // 设置对话框标题  
		            isExit.setTitle("短信群发王");  
		            // 设置对话框消息  
		            isExit.setMessage("确定要退出吗？");  
		            // 添加选择按钮并注册监听  
		            isExit.setButton("朕确定", listener);  
		            isExit.setButton2("取消了", listener);  
		            // 显示对话框  
		            isExit.show();  
			   }	
			  
			}
		});
		
	    btn_sendpause = (Button)findViewById(R.id.btn_sendpause);
		btn_sendpause.setVisibility(send_isstart ? View.VISIBLE : View.INVISIBLE);
		btn_sendpause.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				send_isstart = !send_isstart;
				   if (send_isstart )
				   {
					   synchronized (sendsmstask_lock)
						  {
						   if (AutoSMSActivity.isdebug) Log.e(TAG, "通知你解锁了："+Thread.currentThread().getId());
							sendsmstask_lock.notifyAll();
						  }
					   btn_sendpause.setText("暂停");
				   }else
				   { 
					   btn_sendpause.setText("继续");
				   }	
				
			}
		});
		
		ImageView imgbtn_what_jiange = (ImageView)findViewById(R.id.imgbtn_what_jiange);
		
		imgbtn_what_jiange.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				AlertDialog msg = new AlertDialog.Builder(StartSMStaskActivity.this).create();
				
				msg.setTitle("关于发送间隔");  
				msg.setMessage("每条短信之间暂停的时间，如果您经常被移动运营商封号，请尝试增大发送间隔。"
						+ "如何确定被封号\n 1、失败数量越来越多；2、使用系统自带短信发送给被人不成功，多数"
						+ "是被封号了，解封时间不定，短的几小时，长的几天。");  
 
				msg.show();
				
			}
		});
		
		   
	      paIntent = PendingIntent.getBroadcast(this, 0, sentIntent, 0); 
	      smsManager = SmsManager.getDefault();
	      
	      brc_smssendstatus = new BroadcastReceiver()
	    		  {

					@Override
					public void onReceive(Context context, Intent intent)
					{
						switch (getResultCode()) {  
		    	        case Activity.RESULT_OK:  
		    	        	if (AutoSMSActivity.isdebug) Log.e(TAG,"发送成功"); 
		    	        	 send_success++;
		    	        	StringBuilder sb1 = new StringBuilder()
		    	        	    .append("发送成功：").append(send_success)
							    .append("\t\t发送失败：").append(send_fail);
		    	        	 
		    	        	 tv_successorfail.setText(sb1.toString());
		    	        break;  
		    	        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:  
		    	        	if (AutoSMSActivity.isdebug) Log.e(TAG,"RESULT_ERROR_GENERIC_FAILURE");
		    	        	send_fail++;
		    	        	 sb1 = new StringBuilder()
			    	        	    .append("发送成功：").append(send_success)
								    .append("\t\t发送失败：").append(send_fail);
			    	        	 
			    	        	 tv_successorfail.setText(sb1.toString());
		    	        break;  
		    	        case SmsManager.RESULT_ERROR_RADIO_OFF:  
		    	        	if (AutoSMSActivity.isdebug) Log.e(TAG,"RESULT_ERROR_RADIO_OFF");
		    	        	send_fail++;
		    	        	 sb1 = new StringBuilder()
			    	        	    .append("发送成功：").append(send_success)
								    .append("\t\t发送失败：").append(send_fail);
			    	        	 
			    	        	 tv_successorfail.setText(sb1.toString());
		    	        break;  
		    	        case SmsManager.RESULT_ERROR_NULL_PDU:  
		    	        	if (AutoSMSActivity.isdebug) Log.e(TAG,"RESULT_ERROR_NULL_PDU");
		    	        	send_fail++;
		    	        	 sb1 = new StringBuilder()
			    	        	    .append("发送成功：").append(send_success)
								    .append("\t\t发送失败：").append(send_fail);
			    	        	 
			    	        	 tv_successorfail.setText(sb1.toString());
		    	        break;  
		    	        } 
					}
	    	  
	    		  };
	    	  
	  getApplicationContext().registerReceiver(brc_smssendstatus, new IntentFilter(SENT_SMS_ACTION));		  
		
		
      /*
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
//                     abortBroadcast();
                 }
                 
            }            
        
        
      }, new IntentFilter(DELIVERED_SMS_ACTION)); 
      */
		
	}
	
	Runnable r_sign = new Runnable(){
	    @Override
	    public void run() {
	    	Sign();
	    }
	};
	
	private void init_task()
	{
		m_Cursor =  sqldb.get_sendtask(taskid);
		if (AutoSMSActivity.isdebug)  Log.e(TAG,"m_Cursor.get_sendtask");
	    
		while (m_Cursor.moveToNext())
		{
			m_SmsTask.setTaskfilepath(m_Cursor.getString(m_Cursor.getColumnIndex("taskfilepath")));
			m_SmsTask.setTaskfilename(m_Cursor.getString(m_Cursor.getColumnIndex("taskfilename")));
			m_SmsTask.setTaskname(m_Cursor.getString(m_Cursor.getColumnIndex("taskname")));
			m_SmsTask.setPlatename(m_Cursor.getString(m_Cursor.getColumnIndex("platename")));
			m_SmsTask.setPlatecontent(m_Cursor.getString(m_Cursor.getColumnIndex("platecontent")));
				
			mfilePath = m_SmsTask.getTaskfilepath() +"/"+ m_SmsTask.getTaskfilename();
		}
		
	    TextView  tv_sendinfo = (TextView)findViewById(R.id.tv_sendinfo);
	    tv_sendinfo.setText(tv_sendinfo.getText().toString()+"\t\t"+ m_SmsTask.getTaskname()+"\r\n文件路径："+mfilePath);
		
	    edt_showresult = (EditText)findViewById(R.id.edt_showresult);
	    edt_showresult.setText(GernatorSMSText.getSMSresult(m_SmsTask.getPlatecontent()));
	    
		m_Cursor =  sqldb.get_config();
		if (AutoSMSActivity.isdebug) Log.e(TAG,"m_Cursor.get_sendinteval");
	    
		while (m_Cursor.moveToNext())
		{
			send_interval = m_Cursor.getInt(m_Cursor.getColumnIndex("sendinteval"));
			edt_sendinteval.setText(String.valueOf(send_interval));
			signfromdb = m_Cursor.getBlob(m_Cursor.getColumnIndex("sign"));
			if (signfromdb != null)
			{
				if (AutoSMSActivity.isdebug) Log.e(TAG, "signfromdb:" + new String(signfromdb));
			}
			
		}
		
		
	}

	class load_smstask extends Thread
	{
		boolean mylife = true;

		@Override
		public void interrupt()
		{
			// TODO Auto-generated method stub
			super.interrupt();
		}


		@Override
		public void run()
		{
			
			File feFile = new File(mfilePath);
			
			if (AutoSMSActivity.isdebug) Log.e(TAG, "open file  AbsolutePath"+feFile.getAbsolutePath());
			
			if (AutoSMSActivity.isdebug) Log.e(TAG, "open file  name" +feFile.getName());
			if(feFile.canRead())
			{
				FileReader frd = null;
				filesize = feFile.length();
				if (AutoSMSActivity.isdebug) Log.e(TAG,"filesize is"+ String.valueOf(filesize));
				BufferedReader buffd = null;
				try
				{
					Getnowtime m_Getnowtime = new Getnowtime();
					m_SmsTask.setTaskStarttime(m_Getnowtime.getnowtime("yyyy-M-d HH:mm"));
					
					frd = new FileReader(feFile);
					
					 buffd = new BufferedReader(frd);
					
					String tmp_str = "";
					send_num = 0;
					send_totalnum = 0;
					long readbytes = 0;
					
					while(((tmp_str=buffd.readLine())!=null)&&mylife)
					{   
						if (AutoSMSActivity.isdebug) Log.e(TAG,"load_file 线程号："+Thread.currentThread().getId());
						readbytes += tmp_str.getBytes().length+2;
						tmp_str = tmp_str.trim();
						
					if	(tmp_str.codePointAt(0)==65279)  //utf8格式
					{
						tmp_str = tmp_str.substring(1, tmp_str.length());
						if((tmp_str.length()>0)&&tmp_str.startsWith("1"))
						 {	
							int sp_len = tmp_str.split(",").length;
							
							extend_info = new String[sp_len];
							extend_info = tmp_str.split(",");
							
							send_target.put((Integer)send_num, extend_info);
							
							//send_target[send_num] = tmp_str;
							send_num++;
						 }
					}else {
						if((tmp_str.length()>0)&&tmp_str.startsWith("1"))
						 {	
							int sp_len = tmp_str.split(",").length;
							
							extend_info = new String[sp_len];
							extend_info = tmp_str.split(",");
							
							send_target.put((Integer)send_num, extend_info);
							
							//send_target[send_num] = tmp_str;
							send_num++;
						 }
					}
						
						
						if (AutoSMSActivity.isdebug) Log.e(TAG,"readbytes is"+ String.valueOf(readbytes));
						
						progessperct =  Math.round((float)readbytes/filesize*100);
						
						if (AutoSMSActivity.isdebug) Log.e(TAG, String.valueOf(progessperct)+"%");
						
						if (progessperct<0)
						{
							progessperct = 0;
						}else if (progessperct>100)
						{
							progessperct = 100;
						}
						
						
						if (send_num==10||progessperct>=100)
						{
							
							for (int i = 0; i < send_num; i++)
							{
								String sms_sendtext = m_SmsTask.getPlatecontent();
								
								String[] tmp_sendinfo = send_target.get(i);
								
								switch (tmp_sendinfo.length)
								{
								case 1:
									SmsBase t_smsbase = new SmsBase(tmp_sendinfo[0],GernatorSMSText.getSMSresult(sms_sendtext));
									SmsTaskQuery.insert_sendlist(t_smsbase);
									break;
								case 2:
									 t_smsbase = new SmsBase(tmp_sendinfo[0],GernatorSMSText.getSMSresult(sms_sendtext,tmp_sendinfo[1],"",""));
									SmsTaskQuery.insert_sendlist(t_smsbase);
									break;
								case 3:
									 t_smsbase = new SmsBase(tmp_sendinfo[0],GernatorSMSText.getSMSresult(sms_sendtext,tmp_sendinfo[1],tmp_sendinfo[2],""));
									SmsTaskQuery.insert_sendlist(t_smsbase);
									break;
								case 4:
									 t_smsbase = new SmsBase(tmp_sendinfo[0],GernatorSMSText.getSMSresult(sms_sendtext,tmp_sendinfo[1],tmp_sendinfo[2],tmp_sendinfo[3]));
									SmsTaskQuery.insert_sendlist(t_smsbase);
									break;
								default:
								    t_smsbase = new SmsBase(tmp_sendinfo[0],GernatorSMSText.getSMSresult(sms_sendtext));
									SmsTaskQuery.insert_sendlist(t_smsbase);
									break;
								}
								
							}
							
							synchronized (sendsmstask_lock)
							{
								sendsmstask_lock.notifyAll();
							}
							
							if (send_num >= 10 && !teac)
							{
								mylife = false;
								
								if (AutoSMSActivity.isdebug)  Log.e(TAG,"signfromdbd"+ signfromdb.toString());
							  if (signfromdb != null)
							   {
								  runOnUiThread(new Runnable()
									{
										public void run()
										{
											Toast.makeText(StartSMStaskActivity.this, "无法连接网络", Toast.LENGTH_LONG).show();
										}
									});
							   }else{
									runOnUiThread(new Runnable()
									{
										public void run()
										{
											Toast.makeText(StartSMStaskActivity.this, "未激活设备，只能发送10条", Toast.LENGTH_LONG).show();
										}
									});
							   }
							}
							
							synchronized (loadsmstask_lock) //暂停导入线程
							{
								try
								{
									if (AutoSMSActivity.isdebug) Log.e(TAG, "锁住了："+Thread.currentThread().getId());
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
					
					while (SmsTaskQuery.query_sendlist_count()!=0)
					{
						; //等待队列清空然后更新数据库任务表		
					}
					
				    m_Getnowtime = new Getnowtime();
					m_SmsTask.setTaskEndtime(m_Getnowtime.getnowtime("yyyy-M-d HH:mm"));
					
					m_SmsTask.setTaskfail(send_fail);
					m_SmsTask.setTasksuccess(send_success);
					m_SmsTask.setTasktotal(send_totalnum);
					sqldb.update_smstask(m_SmsTask);
					
					synchronized (sendsmstask_lock)
					{
						load_finish  = true;
						sendsmstask_lock.notifyAll();
					}
					
		
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
				if (AutoSMSActivity.isdebug) Log.e(TAG, "读取文件出错");
				
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						Toast.makeText(StartSMStaskActivity.this, "读取文件异常，请删除任务后重新添加", Toast.LENGTH_LONG).show();
					}
				});
			}
		}
		
	}
	
	class send_sms extends Thread
	{

		@Override
		public void run()
		{
			if (AutoSMSActivity.isdebug) Log.e(TAG,"send_sms 启动 线程号："+Thread.currentThread().getId());
			
			while (true)
			{
				if (load_finish&&SmsTaskQuery.query_sendlist_count()==0)
				{
					runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							mProgressBar.setProgress(100);
							StringBuilder sb = new StringBuilder();
							sb.append("任务进度：").append("100%")
							  .append("\t\t发送数量：").append(send_totalnum);
							tv_sendstatus.setText(sb.toString());
							send_finish = true;
							
							//btn_sendstop.setVisibility(View.INVISIBLE);
					        btn_sendpause.setVisibility(View.INVISIBLE);
					        btn_start.setVisibility(View.VISIBLE);
					        edt_sendinteval.setEnabled(true);
					        send_isstart = false;
						}
					});
					
					synchronized (sendsmstask_lock)
					{
						try
						{
							if (AutoSMSActivity.isdebug) Log.e(TAG, "任务发送完毕锁住："+Thread.currentThread().getId());
							sendsmstask_lock.wait();
						} catch (InterruptedException e)
						{
							
							e.printStackTrace();
						}
					}
				}
				
				if (!send_isstart)
				{
					synchronized (sendsmstask_lock)
					{
						try
						{
							if (AutoSMSActivity.isdebug) Log.e(TAG, "暂停锁住了："+Thread.currentThread().getId());
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
				   final String send_text = t_SmsBase.getSms_sendtext();
				   
					  runOnUiThread(new Runnable()
					 {
						public void run()
						{
							edt_showresult.setText(send_text);
						}
					});
				   
				   
				   ArrayList<String> divideContents = smsManager.divideMessage(send_text); 
				
				   ArrayList<PendingIntent> PendingIntents = new ArrayList<PendingIntent>(divideContents.size());
				
				   int divideContentssize = divideContents.size();
				   
				   for (int i = 0; i < divideContentssize; i++)
				    {
					   PendingIntents.add(i, PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, 0));
				    }
				   
				   smsManager.sendMultipartTextMessage(t_SmsBase.getSms_sendphone(), null,divideContents , PendingIntents, null); 
				
                  //  smsManager.sendTextMessage(t_SmsBase.getSms_sendphone(), null, t_SmsBase.getSms_sendtext(), paIntent, null);
				   send_totalnum += divideContentssize ;
				   m_SmsTask.setTasktotal(send_totalnum);
				   
				   if (AutoSMSActivity.isdebug) Log.e(TAG, t_SmsBase.getSms_sendphone()+","+send_text);
				   if (AutoSMSActivity.isdebug) Log.e(TAG,"发送 线程号："+Thread.currentThread().getId());
				  
				   runOnUiThread(new Runnable()
					{
						public void run()
						{
							if (progessperct>=98)
							{
								mProgressBar.setProgress(98);
								StringBuilder sb = new StringBuilder();
								sb.append("任务进度：").append("98%")
								  .append("\t\t发送数量：").append(send_totalnum);
								tv_sendstatus.setText(sb.toString());
							}else {
								mProgressBar.setProgress(progessperct);
								StringBuilder sb = new StringBuilder();
								sb.append("任务进度：").append(progessperct+"%")
								  .append("\t\t发送数量：").append(send_totalnum);
								tv_sendstatus.setText(sb.toString());
							}
						}
					});

					try
					{
						sleep(send_interval*1000*divideContentssize);
					} catch (InterruptedException e)
					{
						
						e.printStackTrace();
					}
				}else {
					if (SmsTaskQuery.query_sendlist_count()==0)
					{
					  if (send_isstart)
					  {
						  synchronized (loadsmstask_lock)
							{
							  if (AutoSMSActivity.isdebug) Log.e(TAG, "解锁导入线程了："+Thread.currentThread().getId());
								loadsmstask_lock.notifyAll();
							}
						  
						  synchronized (sendsmstask_lock)
						   {
							  try
							{
								  if (AutoSMSActivity.isdebug)  Log.e(TAG, "等待队列导入锁住："+Thread.currentThread().getId());
								sendsmstask_lock.wait();
							} catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						  }
					   }
					}else
					{
						if (AutoSMSActivity.isdebug) Log.e(TAG, "the query not empty");
					}
				}
			}
		}
	}

 public  void stop_sendtask()
 {
	 send_isstart = false;
	 load_finish = false;
     send_finish = false;
	 
	 if (m_loadsmstask!=null)
	{
		 m_loadsmstask.mylife = false;
	}
     
	 synchronized (loadsmstask_lock)
		{
		 if (AutoSMSActivity.isdebug) Log.e(TAG, "帮你解锁了："+Thread.currentThread().getId());
		 SmsTaskQuery.init_sendlist();
		 loadsmstask_lock.notifyAll();
		}
	 
	 check_sendinteval();
		
	 sqldb.update_inteval(send_interval);

	Intent open_managertask = new Intent(StartSMStaskActivity.this, ManagertaskActivity.class);
	startActivity(open_managertask);
	
 }
  private void check_sendinteval()
    {
		  if (edt_sendinteval.getText().toString().equals(""))
			{
				edt_sendinteval.setText("2");
			}
			
			send_interval = Integer.valueOf(edt_sendinteval.getText().toString());
		    
			if (send_interval>120) {
				edt_sendinteval.setText("120");
				send_interval = 120;
			}else if (send_interval<1)
			{
				edt_sendinteval.setText("1");
				send_interval = 1;
			}
	
    }

	@Override  
	    public boolean onKeyDown(int keyCode, KeyEvent event)  
	    {  
		  if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				if (m_loadsmstask!=null)
				   {
					  if (m_loadsmstask.isAlive())
						{
						  AlertDialog isExit = new AlertDialog.Builder(StartSMStaskActivity.this).create();  
				            // 设置对话框标题  
				            isExit.setTitle("短信群发王");  
				            // 设置对话框消息  
				            isExit.setMessage("发送任务正在执行，要退出吗？");  
				            // 添加选择按钮并注册监听  
				            isExit.setButton("朕确定", listener);  
				            isExit.setButton2("取消了", listener);  
				            // 显示对话框  
				            isExit.show();
					   		
				            if (AutoSMSActivity.isdebug) Log.e(TAG, "m_loadsmstask.isAlive");
						}else
						{
							 // 创建退出对话框  
				            AlertDialog isExit = new AlertDialog.Builder(StartSMStaskActivity.this).create();  
				            // 设置对话框标题  
				            isExit.setTitle("短信群发王");  
				            // 设置对话框消息  
				            isExit.setMessage("确定要退出吗？");  
				            // 添加选择按钮并注册监听  
				            isExit.setButton("朕确定", listener);  
				            isExit.setButton2("取消了", listener);  
				            // 显示对话框  
				            isExit.show();  
				            if (AutoSMSActivity.isdebug) Log.e(TAG, "m_loadsmstask not Alive");
						}
				   }else {
					   // 创建退出对话框  
			            AlertDialog isExit = new AlertDialog.Builder(StartSMStaskActivity.this).create();  
			            // 设置对话框标题  
			            isExit.setTitle("短信群发王");  
			            // 设置对话框消息  
			            isExit.setMessage("确定要退出吗？");  
			            // 添加选择按钮并注册监听  
			            isExit.setButton("朕确定", listener);  
			            isExit.setButton2("取消了", listener);  
			            // 显示对话框  
			            isExit.show();  
				}
		    }
			          
			        return false;  
	    } 
	  
	  /**监听对话框里面的button点击事件*/  
	    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
	    {  
	        public void onClick(DialogInterface dialog, int which)  
	        {  
	            switch (which)  
	            {  
	            case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序  
	            	
	            	stop_sendtask();
	            	getApplicationContext().unregisterReceiver(brc_smssendstatus);
	                finish();  
	                break;  
	            case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框  
	                break;  
	            default:  
	                break;  
	            }  
	        }  
	    };    
	    
	    //加密
	    public static String getBase64(String str) {  
	        byte[] b = null;  
	        String s = null;  
	        try {  
	            b = str.getBytes("utf-8");  
	        } catch (UnsupportedEncodingException e) {  
	            e.printStackTrace();  
	        }  
	        if (b != null) {  
	        	
	            s = Base64.encodeToString(b, Base64.NO_WRAP);
	        }  
	        return s;  
	    }  
	  
	    // 解密  
	    public static String getFromBase64(String s) {  
	        byte[] b = null;  
	        String result = null;  
	        if (s != null) {  
	              
	            try {  
	                b = Base64.decode(s.getBytes(), Base64.DEFAULT);
	                result = new String(b, "UTF-8");  
	            } catch (Exception e) {  
	                e.printStackTrace();  
	            }  
	        }  
	        return result;  
	    }
	    
	   private void Sign()
		{
		 	HttpClient mHttpClient = new DefaultHttpClient();
		 	String Imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
			 String uri = getResources().getString(R.string.url)+"/AutoSms_Sign";
			 if (AutoSMSActivity.isdebug) Log.e(TAG, Imei);
			    HttpPost httppost = new HttpPost(uri);   
			    List<NameValuePair> params = new ArrayList<NameValuePair>();
			     // 添加要传递的参数
			    NameValuePair pair1 = new BasicNameValuePair("serialno", Base64.encodeToString(Imei.getBytes(), Base64.DEFAULT));
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
			 				 
			 				 JSONObject mJsonObject = new JSONObject(response);
			 				if (AutoSMSActivity.isdebug) Log.e(TAG,"rescode:"+httpresponse.getStatusLine().getStatusCode());
			 				if (AutoSMSActivity.isdebug) Log.e(TAG, response);
							try
							{
								String resp = mJsonObject.getString("resp");
								switch (resp)
								{
								case "0": //SIGN_OK
									String baseencode_serialid = mJsonObject.getString("baseencode_serialid");
									 if (AutoSMSActivity.isdebug) Log.e(TAG,"baseencode_serialid:"+ baseencode_serialid);
									String tp = getBase64(getMD5(Imei));
									 if (AutoSMSActivity.isdebug) Log.e(TAG, getMD5(Imei));
									 if (AutoSMSActivity.isdebug) Log.e(TAG, tp);
									if (baseencode_serialid.equals(tp))
									{
										if (AutoSMSActivity.isdebug) Log.e(TAG,"认证成功");
										
										runOnUiThread(new Runnable()
										{
											public void run()
											{
												tv_showactive.setTextColor(Color.GREEN);
												tv_showactive.setText("已激活");
											}
										});
										
										teac = true;
										sqldb.update_serial(baseencode_serialid.getBytes());
									}
									
									break;
								case "2": //SIGN_NOREG
									if (AutoSMSActivity.isdebug) Log.e(TAG,"认证失败");
									teac = false;
									break;
								
								default:
									
									break;
								} 
							} catch (JSONException e)
							{
								e.printStackTrace();
							}
						 		
			 			   }
			 				
			 			} catch (ClientProtocolException e1)
						{
							e1.printStackTrace();
						} catch (IOException e1)
						{
							e1.printStackTrace();
							if (AutoSMSActivity.isdebug) Log.e(TAG, "sockettimeout");
							
						} catch (JSONException e1)
						{
							e1.printStackTrace();
						} 
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

	/*
	@Override
	protected void onRestart()
	{
		if (AutoSMSActivity.isdebug) Log.e(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume()
	{
		if (AutoSMSActivity.isdebug) Log.e(TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		if (AutoSMSActivity.isdebug) Log.e(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		if (AutoSMSActivity.isdebug) Log.e(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		if (AutoSMSActivity.isdebug) Log.e(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void recreate()
	{
		if (AutoSMSActivity.isdebug) Log.e(TAG, "recreate");
		super.recreate();
	}
	
	*/

}
