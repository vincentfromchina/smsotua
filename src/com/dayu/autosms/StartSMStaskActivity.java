package com.dayu.autosms;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.text.Editable;
import android.text.StaticLayout;
import android.text.TextWatcher;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.dayu.autosms.c.DBHelper;
import com.dayu.autosms.c.GernatorSMSText;
import com.dayu.autosms.c.Getnowtime;
import com.dayu.autosms.m.SmsBase;
import com.dayu.autosms.m.SmsTask;
import com.dayu.autosms.m.SmsTaskQuery;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StartSMStaskActivity extends Activity
{
	final static String TAG = "autophone";
	PendingIntent paIntent;
	PendingIntent deliverPI;
    SmsManager smsManager;
    String send_target[];
    int    send_num = 0;
    static int    send_totalnum = 0;
    static int    send_success = 0;
    static int    send_fail = 0;
    static int    send_interval = 1;
    private static int progessperct = 0;
	private DBHelper sqldb;
	static private String mfilePath="";
	static int taskid;
	private Cursor m_Cursor;
	static boolean keep_going = false;
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
	static long filesize = 0;
	final static Object loadsmstask_lock = "导入数据共享锁";
	final static Object sendsmstask_lock = "发送进程共享锁";
    static TextView tv_sendstatus;
    static TextView tv_successorfail;
    BroadcastReceiver brc_smssendstatus;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_smstask);
		
		Bundle bundle = this.getIntent().getExtras();
		taskid = bundle.getInt("taskid");
		
		
		mProgressBar = (ProgressBar)findViewById(R.id.pbr);
		tv_sendstatus = (TextView)findViewById(R.id.tv_sendstatus);
		edt_sendinteval = (EditText)findViewById(R.id.edt_sendinteval);
		tv_successorfail = (TextView)findViewById(R.id.tv_successorfail);
		
		edt_sendinteval.setSelection(edt_sendinteval.getText().length()); //将光标设置在文本最后面
		
		
		edt_sendinteval.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				/*
				if (edt_sendinteval.getText().toString().equals(""))
				{
					edt_sendinteval.setText("1");
				}
				
				send_interval = Integer.valueOf(edt_sendinteval.getText().toString());
			    
				if (send_interval>120) {
					edt_sendinteval.setText("120");
				}else if (send_interval<1)
				{
					edt_sendinteval.setText("1");
				}
				
				edt_sendinteval.setSelection(edt_sendinteval.getText().length());
				*/
			}
		});
		
	
		
		sqldb = new DBHelper(StartSMStaskActivity.this, "smstask.db", null);
		m_SmsTask = new SmsTask();
		m_SmsTask.setTaskid(taskid);
		
		Log.e(TAG,"m_SmsTask.getTaskfail()"+ m_SmsTask.getTaskfail());
	   
		init_task();
		
	    btn_start = (Button)findViewById(R.id.btn_start);
		btn_start.setVisibility(send_isstart ? View.INVISIBLE : View.VISIBLE);
		btn_start.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if (edt_sendinteval.getText().toString().equals(""))
				{
					edt_sendinteval.setText("1");
				}
				
				send_interval = Integer.valueOf(edt_sendinteval.getText().toString());
			    
				if (send_interval>120) {
					edt_sendinteval.setText("120");
				}else if (send_interval<1)
				{
					edt_sendinteval.setText("1");
				}
				
				edt_sendinteval.setSelection(edt_sendinteval.getText().length());
				
				send_isstart = true;
				keep_going = true;
				progessperct = 0;
				SmsTaskQuery.init_sendlist();
			     send_target = new String[10];
			     send_num = 0;
			     send_success = 0;
			     send_fail = 0;
			     send_totalnum = 0;
			     progessperct = 0;

			     Log.e(TAG, "send_totalnum" + send_totalnum);
			     m_SmsTask.setTasktotal(0);
                 m_SmsTask.setTaskfail(0);
                 m_SmsTask.setTasksuccess(0);
                 Log.e(TAG,"m_SmsTask.getTaskfail()2:"+ m_SmsTask.getTaskfail());
			     
			      if (m_sendsms==null)
					{
						 m_sendsms = new send_sms();
					     m_sendsms.start();
					}else {
						Log.e(TAG, "m_sendsms is not null");
					}
			      
			    if (m_SmsTaskQuery==null)
				{
			    	  m_SmsTaskQuery = new SmsTaskQuery();
				}else {
					Log.e(TAG, "m_SmsTaskQuery is not null");
				}
			    if (send_isstart )
				   {
					   synchronized (sendsmstask_lock)
						  {
						    Log.e(TAG, "通知你解锁了："+Thread.currentThread().getId());
							sendsmstask_lock.notifyAll();
						  }
				   }
				
				//btn_sendstop.setVisibility(View.VISIBLE);
				btn_sendpause.setVisibility(View.VISIBLE);
				btn_start.setVisibility(View.INVISIBLE);
				
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
				   		
				   		Log.e("gushiriji", "m_loadsmstask.isAlive");
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
						Log.e("gushiriji", "m_loadsmstask not Alive");
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
						    Log.e(TAG, "通知你解锁了："+Thread.currentThread().getId());
							sendsmstask_lock.notifyAll();
						  }
					   btn_sendpause.setText("暂停");
				   }else
				   { 
					   btn_sendpause.setText("继续");
				   }	
				
			}
		});
		
		  String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
	      Intent sentIntent = new Intent(SENT_SMS_ACTION);  
	      paIntent = PendingIntent.getBroadcast(this, 0, sentIntent, 0); 
	      smsManager = SmsManager.getDefault();
	      
	      brc_smssendstatus = new BroadcastReceiver()
	    		  {

					@Override
					public void onReceive(Context context, Intent intent)
					{
						switch (getResultCode()) {  
		    	        case Activity.RESULT_OK:  
		    	        	Log.e(TAG,"发送成功"); 
		    	        	 send_success++;
		    	        	StringBuilder sb1 = new StringBuilder()
		    	        	    .append("发送成功：").append(send_success)
							    .append("\t\t发送失败：").append(send_fail);
		    	        	 
		    	        	 tv_successorfail.setText(sb1.toString());
		    	        break;  
		    	        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:  
		    	        	Log.e(TAG,"RESULT_ERROR_GENERIC_FAILURE");
		    	        	send_fail++;
		    	        	 sb1 = new StringBuilder()
			    	        	    .append("发送成功：").append(send_success)
								    .append("\t\t发送失败：").append(send_fail);
			    	        	 
			    	        	 tv_successorfail.setText(sb1.toString());
		    	        break;  
		    	        case SmsManager.RESULT_ERROR_RADIO_OFF:  
		    	        	Log.e(TAG,"RESULT_ERROR_RADIO_OFF");
		    	        	send_fail++;
		    	        	 sb1 = new StringBuilder()
			    	        	    .append("发送成功：").append(send_success)
								    .append("\t\t发送失败：").append(send_fail);
			    	        	 
			    	        	 tv_successorfail.setText(sb1.toString());
		    	        break;  
		    	        case SmsManager.RESULT_ERROR_NULL_PDU:  
		    	        	Log.e(TAG,"RESULT_ERROR_NULL_PDU");
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
	
	private void init_task()
	{
		m_Cursor =  sqldb.get_sendtask(taskid);
	    Log.e("autophone","m_Cursor.get_sendtask");
	    
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
		
	    EditText  edt_showresult = (EditText)findViewById(R.id.edt_showresult);
	    edt_showresult.setText(GernatorSMSText.getSMSresult(m_SmsTask.getPlatecontent()));
	    
		m_Cursor =  sqldb.get_sendinteval();
	    Log.e("autophone","m_Cursor.get_sendinteval");
	    
		while (m_Cursor.moveToNext())
		{
			send_interval = m_Cursor.getInt(m_Cursor.getColumnIndex("sendinteval"));
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
					send_totalnum = 0;
					long readbytes = 0;
					Log.e(TAG, "send_totalnum2" + send_totalnum);
					
					while(((tmp_str=buffd.readLine())!=null)&&mylife)
					{   
						Log.e(TAG,"load_file 线程号："+Thread.currentThread().getId());
						readbytes += tmp_str.getBytes().length+2;
						tmp_str = tmp_str.trim();
						if((tmp_str.length()>0)&&(tmp_str.length()==11)&&tmp_str.startsWith("1"))
						 {	
							send_target[send_num] = tmp_str;
							Log.e(TAG,"this num；"+ send_target[send_num]);
							send_num++;
						 }
						
                        Log.e(TAG,"readbytes is"+ String.valueOf(readbytes));
						
						progessperct =  Math.round((float)readbytes/filesize*100);
						
						Log.e(TAG, String.valueOf(progessperct)+"%");
						
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
								SmsBase t_smsbase = new SmsBase(send_target[i],GernatorSMSText.getSMSresult(sms_sendtext));
								SmsTaskQuery.insert_sendlist(t_smsbase);
							}
							
							synchronized (loadsmstask_lock) //暂停导入线程
							{
								try
								{
									 Log.e(TAG, "锁住了："+Thread.currentThread().getId());
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
					
					sqldb.update_smstask(m_SmsTask);
					
					send_isstart = false;
					keep_going = false;
					runOnUiThread(new  Runnable()
					{
						public void run()
						{
							//btn_sendstop.setVisibility(View.INVISIBLE);
					        btn_sendpause.setVisibility(View.INVISIBLE);
					        btn_start.setVisibility(View.VISIBLE);
					
						}
					});
		
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
	
	class send_sms extends Thread
	{

		@Override
		public void run()
		{
			Log.e(TAG,"send_sms 启动 线程号："+Thread.currentThread().getId());
			
			while (true)
			{
				if (!send_isstart)
				{
					synchronized (sendsmstask_lock)
					{
						try
						{
							Log.e(TAG, "锁住了："+Thread.currentThread().getId());
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
				   StartSMStaskActivity.send_totalnum++;
				   m_SmsTask.setTasktotal(StartSMStaskActivity.send_totalnum);
				  smsManager.sendTextMessage(t_SmsBase.getSms_sendphone(), null, t_SmsBase.getSms_sendtext(), paIntent, null);
				  Log.e(TAG, t_SmsBase.getSms_sendphone()+","+t_SmsBase.getSms_sendtext());
				  Log.e(TAG,"发送 线程号："+Thread.currentThread().getId());
				}else {
					if (SmsTaskQuery.query_sendlist_count()==0)
					{
					  if (send_isstart)
					  {
						  synchronized (loadsmstask_lock)
							{
								Log.e(TAG, "帮你解锁了："+Thread.currentThread().getId());
								loadsmstask_lock.notifyAll();
							}
					   }
					}else
					{
						Log.e(TAG, "the query not empty");
					}
				}
			   
			   runOnUiThread(new Runnable()
				{
					public void run()
					{
						mProgressBar.setProgress(progessperct);
						
						StringBuilder sb = new StringBuilder();
						sb.append("任务进度：").append(progessperct+"%")
						  .append("\t\t发送数量：").append(send_totalnum);
						 
						tv_sendstatus.setText(sb.toString());
					}
				});
			   
				try
				{
					sleep(send_interval*1000);
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

 public  void stop_sendtask()
 {
	 send_isstart = false;
	 keep_going = false;
	 
	 if (m_loadsmstask!=null)
	{
		 m_loadsmstask.mylife = false;
	}
     
	 synchronized (loadsmstask_lock)
		{
		 Log.e(TAG, "帮你解锁了："+Thread.currentThread().getId());
		 SmsTaskQuery.init_sendlist();
		 loadsmstask_lock.notifyAll();
		}

	Intent open_managertask = new Intent(StartSMStaskActivity.this, ManagertaskActivity.class);
	startActivity(open_managertask);
	
 }
	
	  @Override  
	    public boolean onKeyDown(int keyCode, KeyEvent event)  
	    {  
		  if (keep_going)
		 {
			  if (keyCode == KeyEvent.KEYCODE_BACK )  
		        {  
		            // 创建退出对话框  
		            AlertDialog isExit = new AlertDialog.Builder(this).create();  
		            // 设置对话框标题  
		            isExit.setTitle("短信群发王");  
		            // 设置对话框消息  
		            isExit.setMessage("发送任务正在执行，要退出吗？");  
		            // 添加选择按钮并注册监听  
		            isExit.setButton("朕确定", listener);  
		            isExit.setButton2("取消了", listener);  
		            // 显示对话框  
		            isExit.show();  
		  
		        }  
		}else {
			
	        if (keyCode == KeyEvent.KEYCODE_BACK )  
	        {  
	            // 创建退出对话框  
	            AlertDialog isExit = new AlertDialog.Builder(this).create();  
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
	    
	    @Override
		public boolean onCreateOptionsMenu(Menu menu)
		{
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.start_smstask, menu);
			return true;
		}

	@Override
	protected void onRestart()
	{
		Log.e(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume()
	{
		Log.e(TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		Log.e(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		Log.e(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		Log.e(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void recreate()
	{
		Log.e(TAG, "recreate");
		super.recreate();
	}
	
	

}
