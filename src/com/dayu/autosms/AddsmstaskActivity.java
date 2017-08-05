package com.dayu.autosms;

import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.MalformedChallengeException;

import com.dayu.autosms.c.ContactPicker;
import com.dayu.autosms.c.ContactPicker.PickContactEvent;
import com.dayu.autosms.c.DBHelper;
import com.dayu.autosms.c.FolderFilePicker;
import com.dayu.autosms.c.FolderFilePicker.PickPathEvent;
import com.dayu.autosms.c.GernatorSMSText;
import com.dayu.autosms.c.Getnowtime;
import com.dayu.autosms.m.SmsTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class AddsmstaskActivity extends Activity
{
	final static String TAG = "autosms";
	private static EditText edt_showcontent;
	private static EditText edt_newtaskname;
	private static ListView lv_chooseplate;
	private static TextView tv_contentplatename;
	static private String mfilePath="";
	private String msavefilepath ;
	private static final String ROOT = Environment
			.getExternalStorageDirectory().toString()+"/autosms/";
	private static int contentplateid = 0;
	Cursor m_Cursor;
	private List<String> platename ;
	private List<Integer>  plateid;
	private List<String>  platecontent;
	private  platelistadapter m_datasetadpter;
	private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
	DBHelper sqldb ;
	static SmsTask m_SmsTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addsmstask);
		
		mInflater = LayoutInflater.from(AddsmstaskActivity.this);
		
		sqldb = new DBHelper(AddsmstaskActivity.this, "smstask.db", null);
		m_SmsTask = new SmsTask();
		
		edt_showcontent = (EditText)findViewById(R.id.edt_showresult);
		edt_newtaskname = (EditText)findViewById(R.id.edt_newtaskname);
		lv_chooseplate = (ListView)findViewById(R.id.lv_chooseplate);
		tv_contentplatename = (TextView)findViewById(R.id.tv_contentname);
		
		Button btn_addnewtask = (Button)findViewById(R.id.btn_addnewtask);
		final Button btn_selectsmsplate = (Button)findViewById(R.id.btn_selectsmsplate);
		Button btn_fromfile = (Button)findViewById(R.id.btn_fromfile);
		Button btn_fromcontart = (Button)findViewById(R.id.btn_fromcontart);
		
		
		OnClickListener listener = new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				switch (v.getId())
				{
				case R.id.btn_addnewtask:
					
					if (edt_newtaskname.getText().toString().equals(""))
					{
						Toast.makeText(AddsmstaskActivity.this, "请填写任务名", Toast.LENGTH_LONG).show();
						return;
					}
					
					if (tv_contentplatename.getText().toString().equals(""))
					{
						Toast.makeText(AddsmstaskActivity.this, "请选择模板", Toast.LENGTH_LONG).show();
						return;
					}
					
					if (mfilePath.equals(""))
					{
						Toast.makeText(AddsmstaskActivity.this, "请选文件", Toast.LENGTH_LONG).show();
						return;
					}
					
					m_SmsTask.setTaskname(edt_newtaskname.getText().toString());
					m_SmsTask.setTaskcontentplate(contentplateid);
					Getnowtime m_Getnowtime = new Getnowtime();
					m_SmsTask.setTaskStarttime(m_Getnowtime.getnowtime("yyyy-M-d HH:mm"));
					m_SmsTask.setTaskEndtime(m_Getnowtime.getnowtime("yyyy-M-d HH:mm"));
					
					sqldb.insert_smstask(m_SmsTask);
					
					Intent open_managertaskactivity = new Intent();
					open_managertaskactivity.setClass(AddsmstaskActivity.this, ManagertaskActivity.class);
     				startActivity(open_managertaskactivity);
					
					break;
                case R.id.btn_selectsmsplate:
                	switch(lv_chooseplate.getVisibility())
                	{
                	  case View.VISIBLE :
                		  lv_chooseplate.setVisibility(View.GONE);
                		  InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
                          imm.hideSoftInputFromInputMethod(btn_selectsmsplate.getWindowToken(), 0);
                     	 //	 imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); 
                		  break;
                	  case View.GONE :
                		  lv_chooseplate.setVisibility(View.VISIBLE);
                		   imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
                          imm.hideSoftInputFromInputMethod(btn_selectsmsplate.getWindowToken(), 0);
                     	 //	 imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); 
                		  break;
                	}
                	
					break;
                case R.id.btn_fromfile:
                	pickFile(v);
                	File feFile = new File(mfilePath);
                	m_SmsTask.setTaskfilename(feFile.getParent());
                	m_SmsTask.setTaskfilepath(feFile.getName());
                	
					break;
                case R.id.btn_fromcontart:
                	//readAllContacts();
                	pickContact(v);
					break;

				default:
					break;
				}
				
			}
		};
		
		
		btn_addnewtask.setOnClickListener(listener);
		btn_selectsmsplate.setOnClickListener(listener);
		btn_fromfile.setOnClickListener(listener);
		btn_fromcontart.setOnClickListener(listener);
		
		update_dataset();
		
		m_datasetadpter = new platelistadapter();
		
		lv_chooseplate.setAdapter(m_datasetadpter);
		
		lv_chooseplate.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				contentplateid =  plateid.get(position);
				if (AutoSMSActivity.isdebug) Log.e(TAG, "plateid:"+plateid.get(position));
				lv_chooseplate.setVisibility(View.GONE);
				tv_contentplatename.setText(platename.get(position));
				
				edt_showcontent.setText(GernatorSMSText.getSMSresult(platecontent.get(position)));
			}
			
		});
	}
	
	void copydbfile()
	{
		String dbfilepath = getApplication().getDatabasePath("smstask.db").toString();
		DBHelper.copyDataBaseToSD(dbfilepath);
	}
	
	class platelistadapter extends BaseAdapter
	{
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			 final ViewHolder holder;
	            //观察convertView随ListView滚动情况
	     
	            if (convertView == null) {
	                     convertView = mInflater.inflate(R.layout.listviewonetextview, parent, false);

	                     holder = new ViewHolder();
	                    /*得到各个控件的对象*/
	                    holder.contenplatename = (TextView) convertView.findViewById(R.id.tv_contentname);
	                   
	                    
	                    if (AutoSMSActivity.isdebug)  Log.e(TAG, ""+position+"--"+plateid.get(position));
	                    
	                    convertView.setTag(holder);//绑定ViewHolder对象
	                    
	                    if (AutoSMSActivity.isdebug) Log.v(TAG, "getView " + position + " " + convertView);
	          }
	          else{
	                    holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
	                    if (AutoSMSActivity.isdebug) Log.e(TAG, "会执行");
	                  }
	            /*设置TextView显示的内容，即我们存放在动态数组中的数据*/
	            holder.contenplatename.setText((String)getItem(position));
	            
	         
	            /*为Button添加点击事件*/
	            return convertView;
		}
		
		@Override
		public long getItemId(int position)
		{
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public Object getItem(int position)
		{
			if (position >= getCount() || position < 0) {
				return null;
			}
			return platename.get(position);
		}
		
		@Override
		public int getCount()
		{			
			//Log.e("autophone", "getCount "+m_Cursor.getCount());
			return platename.size();
		}
		
		class ViewHolder{
		    public TextView contenplatename;
		   
		    }
	};
	
	
	
	public void update_dataset()
	{
		platename = new ArrayList<>();		
		plateid = new ArrayList<>();
		platecontent = new ArrayList<>();
		
	    m_Cursor =  sqldb.query_smscontentplate();
	    if (AutoSMSActivity.isdebug) Log.e(TAG,"m_Cursor.getCount"+ m_Cursor.getCount());
	    
		while (m_Cursor.moveToNext())
		{
			platename.add(m_Cursor.getString(1));	
			plateid.add(Integer.valueOf(m_Cursor.getInt(0)));
			platecontent.add(m_Cursor.getString(2));
		}
	}
	
	public void pickFile(View v) {
		FolderFilePicker picker = new FolderFilePicker(this,
				new PickPathEvent() {

					@Override
					public void onPickEvent(String resultPath) {
						mfilePath = resultPath;
						if (mfilePath==null)
						{
							Toast.makeText(AddsmstaskActivity.this, "请选择文件",
									Toast.LENGTH_LONG).show();
						}else{
						
							File feFile = new File(mfilePath);
							if(feFile.canRead())
							{
								long filesize = feFile.length();
								TextView tv_taskinfo = (TextView)findViewById(R.id.tv_taskinfo);
								
								m_SmsTask.setTaskfilepath(feFile.getParent());
								m_SmsTask.setTaskfilename(feFile.getName());
								
								if (filesize>1024)
								{
									tv_taskinfo.setText("文件："+mfilePath+",文件大小: "+filesize/1024 +"KB");
									
								}else
								{
									tv_taskinfo.setText("文件："+mfilePath+",文件大小: "+filesize +"B");
								}
							}
						}
					}
				}, "txt","csv","TXT","CSV");  //不定长参数使用方法
		picker.show();
	}
	
	public void pickContact(View v)
	{
		Toast.makeText(AddsmstaskActivity.this, "正在导入联系人，请稍等", Toast.LENGTH_LONG).show();
		
		ContactPicker picker = new ContactPicker(this,
				new PickContactEvent() {

					@Override
					public void onPickEvent(List<String[]> contact) {
						
						
						Getnowtime mgGetnowtime = new Getnowtime();
						File feFile = null;
						FileWriter fwd = null;
						BufferedWriter bufwrd = null;
						try
						{
							createdir(ROOT);
							msavefilepath = ROOT+mgGetnowtime.getnowtime("yyyy-M-d_HH-mm")+".txt";
							if (AutoSMSActivity.isdebug) Log.e(TAG, msavefilepath);
							
							
							feFile = new File(msavefilepath);
							 fwd = new FileWriter(feFile);
							 bufwrd = new BufferedWriter(fwd);
						
							for (String[] onecontact : contact)
							{
								if (onecontact[2].equals("1") && (onecontact[1] != null))
								{
									if (AutoSMSActivity.isdebug) Log.e(TAG, onecontact[0] + onecontact[1]);
									String replacestr = onecontact[1].replace(" ", "");
									replacestr = replacestr.replace("-", "");
									replacestr = replacestr.replace("+", "");
									//bufwrd.write(replacestr);
									bufwrd.write(replacestr+","+onecontact[0]);
									bufwrd.newLine();
								}
							}
							
							showfiledialog(msavefilepath);
							
						}catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {
							try
							{
							     if (bufwrd != null) bufwrd.close();
							     if (fwd != null) fwd.close();
							     
							} catch (IOException e)
								{
									e.printStackTrace();
								}
							
						}
						
					}
				});  
		picker.show();
	}
	
	 private void showfiledialog(String filepath)
	 {
		 Log.e(TAG,"show filepath");
		 final Builder adAlertDialog = new Builder(AddsmstaskActivity.this);
		 adAlertDialog.setMessage("文件已生成，保存路径：\r\n"+filepath);
	//	 adAlertDialog.setTitle("软件更新");
		 adAlertDialog.setPositiveButton("好的，知道了", new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		 
		 adAlertDialog.show();
	 }
	
	public void createdir(String path)
	{

		File f = new File(path);

		if (!f.exists())
		{
			f.mkdirs();
		}

		/*
		File file = new File(f, fileName);
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 */
	}
	
	/*
     * 读取联系人的信息
     */
    public void readAllContacts() {
        Cursor contacts = this.getBaseContext().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, 
                 null, null, null, null);
        int contactIdIndex = 0;
        int nameIndex = 0;
        
        if(contacts.getCount() > 0) {
            contactIdIndex = contacts.getColumnIndex(ContactsContract.Contacts._ID);
            nameIndex = contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        }
        while(contacts.moveToNext()) {
            String contactId = contacts.getString(contactIdIndex);
            String name = contacts.getString(nameIndex);
            if (AutoSMSActivity.isdebug) Log.e(TAG, contactId);
            if (AutoSMSActivity.isdebug) Log.e(TAG, name);
            
            /*
             * 查找该联系人的phone信息
             */
            Cursor phones = this.getBaseContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                    null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
            int phoneIndex = 0;
            if(phones.getCount() > 0) {
                phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            }
            while(phones.moveToNext()) {
                String phoneNumber = phones.getString(phoneIndex);
                if (AutoSMSActivity.isdebug) Log.e(TAG, phoneNumber);
            }
            
            if (phones!=null)
			{
            	phones.close();
			}
            
            
        }
        
        if (contacts!=null)
		{
        	contacts.close();
		}
    }
    

}
