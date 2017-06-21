package com.dayu.autosms;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

import com.dayu.autosms.ManagercontentplateActivity.platelistadapter;
import com.dayu.autosms.c.GernatorSMSText;

import android.R.color;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

public class ContentSetActivity extends Activity {

	final static String TAG = "autosms";
	public static String Imei = "";
	public static String serverip = "dayuinf.com";
	private static final int REG_OK = 1001,REG_EXITS = 1002,REG_FAIL = 1007,REG_BACKLIST = 1004;
	String serialid;
	String type = "";
	int    plateid;
	String platecontent;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_set);
        
        Bundle bundle = this.getIntent().getExtras();
        
        if (bundle!=null)
		{
        	if (bundle.containsKey("type"))
    		{
            	type = bundle.getString("type");
    		}
            if (bundle.containsKey("plateid"))
    		{
            	plateid = bundle.getInt("plateid");
    		}
            if (bundle.containsKey("platecontent"))
    		{
                platecontent = bundle.getString("platecontent"); 
    		}
                    
		}
        
        
        if (AutoSMSActivity.isdebug) Log.e(TAG, type + " " + plateid);
        
        
        final TextView txv_calculate = (TextView)findViewById(R.id.txv_calculate);
        
        final TextView txv_totalcount = (TextView)findViewById(R.id.txv_totalcount);
        
        final EditText edt_content = (EditText)findViewById(R.id.edt_content);
        
        final EditText edt_showresult = (EditText)findViewById(R.id.edt_showresult);
    //    final Showresult view_showcontent = (Showresult)findViewById(R.id.view_showcontent);
            
   //     view_showcontent.set_screenwidth(getApplicationContext().getResources().getDisplayMetrics().widthPixels); 
   //     view_showcontent.set_screenheight(getApplicationContext().getResources().getDisplayMetrics().heightPixels);
        
        edt_content.addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{				
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
			//	Log.e("TAG", String.valueOf(s));
				
				String teString = edt_content.getText().toString();
								
				teString = GernatorSMSText.getSMSresult(teString);
						
			//	Log.e(TAG, teString);
			//	view_showcontent.set_contentchange(teString);
				int conentlength = teString.getBytes().length;
			//	Log.e("TAG", String.valueOf(conentlength));
				txv_totalcount.setText(String.valueOf(conentlength));
				if (conentlength>=140)
				{
					txv_totalcount.setTextColor(Color.RED);
					
					int j = 1;
					while ((conentlength=conentlength-140)>0)
					{
						j++;						
					}
					txv_calculate.setText("/140 (共"+String.valueOf(j)+"条)");
				}else
				{
					txv_totalcount.setTextColor(Color.BLACK);
					txv_calculate.setText("/140 (共1条)");
				}
						
				edt_showresult.setText(teString);	
				
			}
		});
        
        
        Button btn_insertspc = (Button)findViewById(R.id.btn_insertspc);
        btn_insertspc.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
			    int index = edt_content.getSelectionStart();  
			    Editable editable = edt_content.getText();  
			    editable.insert(index, "{|@|}"); 
				
			}
		});
        
        Button btn_insertlittle_a = (Button)findViewById(R.id.btn_insertlitter_a);
        btn_insertlittle_a.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				int index = edt_content.getSelectionStart();  
			    Editable editable = edt_content.getText();  
			    editable.insert(index, "{|a|}"); 
				
			}
		});
        
        Button btn_insertbig_a = (Button)findViewById(R.id.btn_insertbig_a);
        btn_insertbig_a.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				int index = edt_content.getSelectionStart();  
			    Editable editable = edt_content.getText();  
			    editable.insert(index, "{|A|}"); 
				
			}
		});
        
    //    Button btn_insertnum = (Button)findViewById(R.id.btn_insertnum);
        
        Button btn_insertblank = (Button)findViewById(R.id.btn_insertblank);
        btn_insertblank.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				int index = edt_content.getSelectionStart();  
			    Editable editable = edt_content.getText();  
			    editable.insert(index, "{| |}"); 
				
			}
		});
        
        Button btn_insertdate = (Button)findViewById(R.id.btn_insertdate);
        btn_insertdate.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				int index = edt_content.getSelectionStart();  
			    Editable editable = edt_content.getText();  
			    editable.insert(index, "{|d|}"); 
				
			}
		});
        
        Button btn_inserttime = (Button)findViewById(R.id.btn_inserttime);
        btn_inserttime.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				int index = edt_content.getSelectionStart();  
			    Editable editable = edt_content.getText();  
			    editable.insert(index, "{|t|}"); 
				
			}
		});
        
        Button btn_insertnum = (Button)findViewById(R.id.btn_insertnum);
        btn_insertnum.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				int index = edt_content.getSelectionStart();  
			    Editable editable = edt_content.getText();  
			    editable.insert(index, "{|n|}"); 
				
			}
		});
        
        Button btn_unlock = (Button)findViewById(R.id.btn_unlock);
        btn_unlock.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				 
					   doreg mdoreg = new doreg();
				       mdoreg.start();
			
			}
		});
        
        Button btn_ok = (Button)findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent data = new Intent();
				if (type.equals("mod"))
			    {
					final int RESULT_MOD = 309;
					data.putExtra("plateid", plateid);
					data.putExtra("contentplate", getcontent());
					setResult(RESULT_MOD, data);
					finish();
			    }else
			    {
					final int RESULT_CODE = 308;
					data.putExtra("contentplate", getcontent());
					setResult(RESULT_CODE, data);
					finish();
			    }
			}
		});
 
        if (type.equals("mod"))
	    {
    	   edt_content.setText(platecontent);
	    }
        
    }
    
    private void Register()
	{
	 	HttpClient mHttpClient = new DefaultHttpClient();
	 	Imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
		 String uri = "http://"+serverip+"/AutoSms_Reg";
		 if( AutoSMSActivity.isdebug ) Log.e(TAG, Imei);
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
		 				if( AutoSMSActivity.isdebug )Log.e(TAG, "发送数据");
		 			} catch (UnsupportedEncodingException e1)
		 			{
		 				// TODO Auto-generated catch block
		 				if( AutoSMSActivity.isdebug )Log.e(TAG, "数据传递出错了");
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
		 				
		 				if( AutoSMSActivity.isdebug )Log.e(TAG, mJsonObject.toString());
						try
						{
							String resp = mJsonObject.getString("resp");
					
							switch (resp)
							{
							case "1":
								serialid = mJsonObject.getString("serialid");
								mHandler.sendEmptyMessage(REG_OK);
								break;
							case "2":
								serialid = mJsonObject.getString("serialid");
								mHandler.sendEmptyMessage(REG_EXITS);
								break;
							case "4":
								serialid = mJsonObject.getString("serialid");
								if (AutoSMSActivity.isdebug) Log.e(TAG, serialid);
								mHandler.sendEmptyMessage(REG_BACKLIST);
								break;
							default:
								mHandler.sendEmptyMessage(REG_FAIL);
								break;
							} 
						} catch (JSONException e)
						{
							e.printStackTrace();
						}
					 		
						if( AutoSMSActivity.isdebug )	Log.e(TAG,response);
		 			   }
		 			  if( AutoSMSActivity.isdebug ) Log.e(TAG,"rescode:"+httpresponse.getStatusLine().getStatusCode());
		 				
		 				
		 			} catch (ClientProtocolException e1)
					{
						e1.printStackTrace();
					} catch (IOException e1)
					{
						e1.printStackTrace();
						if( AutoSMSActivity.isdebug ) Log.e(TAG, "sockettimeout");
						
					} catch (JSONException e1)
					{
						e1.printStackTrace();
					}       
	

}

    class doreg  extends Thread
{

	@Override
	public void run()
	{
		Register();
		super.run();
	}
	
}
    
    private final Handler mHandler = new Handler() {
 		@Override
 	    public void handleMessage(android.os.Message msg) {
 	        super.handleMessage(msg);
 	        switch (msg.what) {
 	            
 	            case REG_OK:
 	            	Toast.makeText(ContentSetActivity.this, "激活成功", Toast.LENGTH_LONG).show();
 	            	
 	               break;
 	            case REG_EXITS:
 	            	Toast.makeText(ContentSetActivity.this, "本设备已存在", Toast.LENGTH_LONG).show();
 	            	
 		            break;
 	            case REG_BACKLIST:
 	            	Toast.makeText(ContentSetActivity.this, "本设备无效，请更换", Toast.LENGTH_LONG).show();
 	            	
 		            break;    
 	            case REG_FAIL:
 	            	Toast.makeText(ContentSetActivity.this, "激活失败，请重试", Toast.LENGTH_LONG).show();
 	            	
 	            	break;
 	        default:
 	        	if( AutoSMSActivity.isdebug ) Log.e(TAG, "Unhandled msg - " + msg.what);
 	        }
 	    }                                       
 	};
    
    public String getcontent()
    {
       EditText tv_edit = (EditText)findViewById(R.id.edt_content);
       if (AutoSMSActivity.isdebug) Log.e(TAG,"send contentplate"+ tv_edit.getText().toString());
       return tv_edit.getText().toString();
    }
    
}
