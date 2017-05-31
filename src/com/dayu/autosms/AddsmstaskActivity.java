package com.dayu.autosms;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.io.FileReader;

import com.dayu.autosms.c.FolderFilePicker;
import com.dayu.autosms.c.FolderFilePicker.PickPathEvent;

import android.app.Activity;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddsmstaskActivity extends Activity
{
	private static final String TAG = "autophone";
	private static EditText edt_showcontent;
	static private String mfilePath="";
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addsmstask);
		
		edt_showcontent = (EditText)findViewById(R.id.edt_showresult);
		
		Button btn_addnewtask = (Button)findViewById(R.id.btn_addnewtask);
		Button btn_selectsmsplate = (Button)findViewById(R.id.btn_selectsmsplate);
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
					
					break;
                case R.id.btn_selectsmsplate:
					
					break;
                case R.id.btn_fromfile:
                	pickFile(v);
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
            Log.e(TAG, contactId);
            Log.e(TAG, name);
            
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
                Log.e(TAG, phoneNumber);
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
