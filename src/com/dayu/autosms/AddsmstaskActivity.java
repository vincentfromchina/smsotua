package com.dayu.autosms;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import com.dayu.autosms.c.DBHelper;
import com.dayu.autosms.c.FolderFilePicker;
import com.dayu.autosms.c.FolderFilePicker.PickPathEvent;
import com.dayu.autosms.c.GernatorSMSText;
import com.dayu.autosms.c.Getnowtime;
import com.dayu.autosms.m.SmsTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
	private static int contentplateid = 0;
	Cursor m_Cursor;
	private List<String> platename ;
	private List<Integer>  plateid;
	private List<String>  platecontent;
	private  platelistadapter m_datasetadpter;
	private LayoutInflater mInflater;//�õ�һ��LayoutInfalter�����������벼��
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
						Toast.makeText(AddsmstaskActivity.this, "����д������", Toast.LENGTH_LONG).show();
						return;
					}
					
					if (tv_contentplatename.getText().toString().equals(""))
					{
						Toast.makeText(AddsmstaskActivity.this, "��ѡ��ģ��", Toast.LENGTH_LONG).show();
						return;
					}
					
					if (mfilePath.equals(""))
					{
						Toast.makeText(AddsmstaskActivity.this, "��ѡ�ļ�", Toast.LENGTH_LONG).show();
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
                	lv_chooseplate.setVisibility(View.VISIBLE);
                	 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
                     imm.hideSoftInputFromInputMethod(btn_selectsmsplate.getWindowToken(), 0);
                	 //	 imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); 
					break;
                case R.id.btn_fromfile:
                	pickFile(v);
                	File feFile = new File(mfilePath);
                	m_SmsTask.setTaskfilename(feFile.getParent());
                	m_SmsTask.setTaskfilepath(feFile.getName());
                	
					break;
                case R.id.btn_fromcontart:
                	readAllContacts();
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
	            //�۲�convertView��ListView�������
	     
	            if (convertView == null) {
	                     convertView = mInflater.inflate(R.layout.listviewonetextview, parent, false);

	                     holder = new ViewHolder();
	                    /*�õ������ؼ��Ķ���*/
	                    holder.contenplatename = (TextView) convertView.findViewById(R.id.tv_contentname);
	                   
	                    
	                    if (AutoSMSActivity.isdebug)  Log.e(TAG, ""+position+"--"+plateid.get(position));
	                    
	                    convertView.setTag(holder);//��ViewHolder����
	                    
	                    if (AutoSMSActivity.isdebug) Log.v(TAG, "getView " + position + " " + convertView);
	          }
	          else{
	                    holder = (ViewHolder)convertView.getTag();//ȡ��ViewHolder����
	                    if (AutoSMSActivity.isdebug) Log.e(TAG, "��ִ��");
	                  }
	            /*����TextView��ʾ�����ݣ������Ǵ���ڶ�̬�����е�����*/
	            holder.contenplatename.setText((String)getItem(position));
	            
	         
	            /*ΪButton��ӵ���¼�*/
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
							Toast.makeText(AddsmstaskActivity.this, "��ѡ���ļ�",
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
									tv_taskinfo.setText("�ļ���"+mfilePath+",�ļ���С: "+filesize/1024 +"KB");
									
								}else
								{
									tv_taskinfo.setText("�ļ���"+mfilePath+",�ļ���С: "+filesize +"B");
								}
							}
						}
					}
				}, "txt","csv","TXT","CSV");  //����������ʹ�÷���
		picker.show();
	}
	
	/*
     * ��ȡ��ϵ�˵���Ϣ
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
             * ���Ҹ���ϵ�˵�phone��Ϣ
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.addsmstask, menu);
		return true;
	}

}
