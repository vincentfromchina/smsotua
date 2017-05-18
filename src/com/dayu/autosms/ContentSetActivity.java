package com.dayu.autosms;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.dayu.autosms.c.GernatorSMSText;

import android.R.color;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ContentSetActivity extends Activity {

	final String TAG = "autophone";

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_set);
        
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
    }
    
   


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.content_set, menu);
        return true;
    }
    
    public String getcontent()
    {
       EditText tv_edit = (EditText)findViewById(R.id.edt_content);
       return tv_edit.getText().toString();
    }
    
}
