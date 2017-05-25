package com.dayu.autosms;

import android.os.Bundle;
import android.util.Log;

import com.dayu.autosms.c.DBHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ManagercontentplateActivity extends Activity
{
    DBHelper sqldb ;
	private String contentplate;
	boolean rec_contentplate = false;
	EditText edt_contentplate;
 
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managercontentplate);
		
		sqldb = new DBHelper(ManagercontentplateActivity.this, "smstask.db", null);
		
		
		Button btn_addcontentplate = (Button)findViewById(R.id.btn_addcontentplate);
		Button btn_modcontentplate = (Button)findViewById(R.id.btn_modcontentplate);
		Button btn_delcontentplate = (Button)findViewById(R.id.btn_delcontentplate);
		
		btn_addcontentplate.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				showDialog();
			}
		});
	}
	
	public void showDialog(){         
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        LayoutInflater inflater = getLayoutInflater();  
        final View layout = inflater.inflate(R.layout.newcontentplatedialog, null);//��ȡ�Զ��岼��  
        builder.setView(layout);  
        builder.setIcon(R.drawable.ic_launcher);//���ñ���ͼ��  
        builder.setTitle("�༭����ģ��");//���ñ�������  
        //builder.setMessage("");//��ʾ�Զ��岼������  
        edt_contentplate = (EditText)layout.findViewById(R.id.edt_contentplatename);
          
        Button btn_editcontentplate = (Button)layout.findViewById(R.id.btn_editcontentplate);  
        btn_editcontentplate.setOnClickListener(new OnClickListener() {  
              
            @Override  
            public void onClick(View arg0) {  
                 
            	rec_contentplate = false;
            	Intent open_conentsetactivity = new Intent();
				open_conentsetactivity.setClass(ManagercontentplateActivity.this, ContentSetActivity.class);
				final int REQUEST_CODE = 307;
				startActivityForResult(open_conentsetactivity, REQUEST_CODE, null);

            }  
        });       
        //ȷ�ϰ�ť  
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
              
            @Override  
            public void onClick(DialogInterface arg0, int arg1) {  
                if (rec_contentplate)
				{
                	
					sqldb.insert_smscontentplate(edt_contentplate.getText().toString(), contentplate);
				} 
            }  
        });  
        //ȡ��  
        builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {  
              
            @Override  
            public void onClick(DialogInterface arg0, int arg1) {             
            	arg0.dismiss();
            }  
        });  
        final AlertDialog dlg = builder.create();  
        dlg.show();  
     } 

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.managercontentplate, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode==307) //����༭����ģ��
		{
			if (resultCode==308) //���ض���ģ��
			{
				Bundle m_Bundle = data.getExtras();
			    contentplate = m_Bundle.getString("contentplate");
			    rec_contentplate = true;
				Log.e("autophone", contentplate);
			}
		}
		
	}
	
	

}
