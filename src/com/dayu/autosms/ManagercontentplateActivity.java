package com.dayu.autosms;

import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dayu.autosms.c.DBHelper;
import com.dayu.autosms.c.GernatorSMSText;

import android.R.bool;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ManagercontentplateActivity extends Activity
{
    DBHelper sqldb ;
    final static String TAG = "autosms";
	private String contentplate;
	boolean rec_contentplate = false;
	EditText edt_contentplate;
	Cursor m_Cursor;
	private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
	private List<String> platename ;
	private List<Integer>  plateid;
	private List<String>  platecontent;
	 static SimpleDateFormat nowdate;
	 static SimpleDateFormat nowtime;
	 static Date date;
	 private  platelistadapter m_datasetadpter;
	 private static ListView lv_contentplate ;
	 private static EditText edt_showresult;
	 final int REQUEST_CODE = 307;
	 static String modplatename = "";
	 static int pressplateid ;
	 static boolean isfirstlongclick = true;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managercontentplate);
		
		sqldb = new DBHelper(ManagercontentplateActivity.this, "smstask.db", null);
		
		mInflater = LayoutInflater.from(ManagercontentplateActivity.this);
		
	    lv_contentplate = (ListView)findViewById(R.id.lv_contentplate);
	    
	    edt_showresult = (EditText)findViewById(R.id.edt_showresult);
	    
	    
		Button btn_addcontentplate = (Button)findViewById(R.id.btn_addcontentplate);
		
		btn_addcontentplate.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				show_newcontentplateDialog();
			}
		});
		
		/*
		platelist = new ArrayList<>();		
		plateid = new ArrayList<>();
		
	    m_Cursor =  sqldb.query_smscontentplate();
	    
		while (m_Cursor.moveToNext())
		{
			platelist.add(m_Cursor.getString(1));	
			plateid.add(Integer.valueOf(m_Cursor.getInt(0)));
		}
         */
		update_dataset();
		
		m_datasetadpter = new platelistadapter();
		
		lv_contentplate.setAdapter(m_datasetadpter);
		
		lv_contentplate.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				//点击后在标题上显示点击了第几行
				if (AutoSMSActivity.isdebug) Log.e(TAG,"你点击了第"+position +"id:"+id);
                
               if (isfirstlongclick)
			   {
            	   isfirstlongclick = !isfirstlongclick;
            	   Toast.makeText(ManagercontentplateActivity.this, "长按修改模板名字", Toast.LENGTH_LONG).show();    
			   }   
                
               edt_showresult.setText(GernatorSMSText.getSMSresult(platecontent.get(position)));
			}
			
		});
		
		lv_contentplate.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				pressplateid = plateid.get(position);
				if (AutoSMSActivity.isdebug) Log.e(TAG,"你长按了第"+position+"模板名"+platename.get(position));
				show_modplatenameDialog(platename.get(position));
			
				return false;
			}
			
		});
	}
	
	class platelistadapter extends BaseAdapter
	{
		int  nowclickid = 0;
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			 ViewHolder holder;
	            //观察convertView随ListView滚动情况
	     
	            if (convertView == null) {
	                     convertView = mInflater.inflate(R.layout.showcontentplate, parent, false);

	                     holder = new ViewHolder();
	                    /*得到各个控件的对象*/
	                    holder.contenplatename = (TextView) convertView.findViewById(R.id.tv_listcontentplate);
	                    holder.delete_plate = (Button) convertView.findViewById(R.id.btn_delplate);
	                    holder.mod_plate = (Button) convertView.findViewById(R.id.btn_modplate);
	                    
	                    convertView.setTag(holder);//绑定ViewHolder对象
	                    if (AutoSMSActivity.isdebug) Log.e(TAG, "getView " + position + " " + convertView);
	          }
	          else{
	                    holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
	                    if (AutoSMSActivity.isdebug) Log.e(TAG, "会执行");
	                  }
	            /*设置TextView显示的内容，即我们存放在动态数组中的数据*/
	            holder.contenplatename.setText((String)getItem(position));
	            holder.mod_plate.setTag(R.id.mod_key_first_tag,plateid.get(position));
                holder.mod_plate.setTag(R.id.mod_key_second_tag, position);
                holder.delete_plate.setTag(plateid.get(position));
                if (AutoSMSActivity.isdebug) Log.e(TAG, ""+position+"--"+plateid.get(position));
                
                holder.mod_plate.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                    	if (AutoSMSActivity.isdebug) Log.e(TAG, "你点击了修改按钮" + v.getTag(R.id.mod_key_first_tag));                                //打印Button的点击信息
                     rec_contentplate = false;
                 	Intent open_conentsetactivity = new Intent();
     				open_conentsetactivity.setClass(ManagercontentplateActivity.this, ContentSetActivity.class);
     				Bundle bundle = new Bundle();
     				bundle.putString("type", "mod");
     				bundle.putInt("plateid", (int)v.getTag(R.id.mod_key_first_tag));
     				bundle.putString("platecontent", platecontent.get((Integer)v.getTag(R.id.mod_key_second_tag)));
     				open_conentsetactivity.putExtras(bundle);

     				startActivityForResult(open_conentsetactivity, REQUEST_CODE, null);

     				if (AutoSMSActivity.isdebug) Log.e(TAG, "start update");
     				
                   }
                });
                
                holder.delete_plate.setOnClickListener(new OnClickListener()
				{
					
					@Override
					public void onClick(View v)
					{
						if (AutoSMSActivity.isdebug) Log.e(TAG, "你点击了删除按钮" + v.getTag()); 
						
						int contentplate_withtask = sqldb.query_contentplate_withtask((Integer)v.getTag());
						
						if (contentplate_withtask>0)
						{
							nowclickid = (int)v.getTag();
							AlertDialog isExit = new AlertDialog.Builder(ManagercontentplateActivity.this).create();  
				            // 设置对话框标题  
				            isExit.setTitle("短信群发王");  
				            // 设置对话框消息  
				            isExit.setMessage("是否将已关联此短信模板的发送任务一起删除？");  
				            // 添加选择按钮并注册监听  
				            isExit.setButton("确定", listener);  
				            isExit.setButton2("取消", listener);  
				            // 显示对话框  
				            isExit.show();
						}else
						{
							sqldb.delrec_smscontentplate((Integer)v.getTag());
							 update_dataset();
						     m_datasetadpter.notifyDataSetChanged();
						}
						
					}
				});
	            /*为Button添加点击事件*/
	            return convertView;
		}
		
		 /**监听对话框里面的button点击事件*/  
	    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
	    {  
	        public void onClick(DialogInterface dialog, int which)  
	        {  
	            switch (which)  
	            {  
	            case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序  
	            	
	            	sqldb.delrec_smscontentplate(nowclickid);
					 update_dataset();
				     m_datasetadpter.notifyDataSetChanged();
	                break;  
	            case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框  
	                break;  
	            default:  
	                break;  
	            }  
	        }  
	    };   
		
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
		    public Button   delete_plate;
		    public Button   mod_plate;
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
	
	public void show_newcontentplateDialog(){         
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        LayoutInflater inflater = getLayoutInflater();  
        final View layout = inflater.inflate(R.layout.newcontentplatedialog, null);//获取自定义布局  
        builder.setView(layout);  
    //    builder.setIcon(R.drawable.ic_launcher);//设置标题图标  
        builder.setTitle("编辑短信模板");//设置标题内容  
        //builder.setMessage("");//显示自定义布局内容  
        edt_contentplate = (EditText)layout.findViewById(R.id.edt_contentplatename);
          
        Button btn_editcontentplate = (Button)layout.findViewById(R.id.btn_editcontentplate);  
        btn_editcontentplate.setOnClickListener(new OnClickListener() {  
              
            @Override  
            public void onClick(View arg0) {  
                 
            	rec_contentplate = false;
            	Intent open_conentsetactivity = new Intent();
				open_conentsetactivity.setClass(ManagercontentplateActivity.this, ContentSetActivity.class);
				startActivityForResult(open_conentsetactivity, REQUEST_CODE, null);

            }  
        });       
        //确认按钮  
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
              
            @Override  
            public void onClick(DialogInterface arg0, int arg1) {  
                if (rec_contentplate)
				{
                	nowdate = new SimpleDateFormat("yyyy-M-d HH:mm");
       			    long UTCtime = System.currentTimeMillis();
       				date = new Date(UTCtime);
       				String tmp_time = nowdate.format(date);
       				
					sqldb.insert_smscontentplate(edt_contentplate.getText().toString(), contentplate,tmp_time);
					
					update_dataset();
			        m_datasetadpter.notifyDataSetChanged();
				} 
            }  
        });  
        //取消  
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {  
              
            @Override  
            public void onClick(DialogInterface arg0, int arg1) {             
            	arg0.dismiss();
            }  
        });  
        final AlertDialog dlg = builder.create();  
        dlg.show();  
     } 
	
	public void show_modplatenameDialog(String platename){         
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        LayoutInflater inflater = getLayoutInflater();  
        final View layout = inflater.inflate(R.layout.modcontentplatenamedialog, null);//获取自定义布局  
        builder.setView(layout);  
    //    builder.setIcon(R.drawable.ic_launcher);//设置标题图标  
        builder.setTitle("编辑短信模板名称");//设置标题内容  
        //builder.setMessage("");//显示自定义布局内容  
        final EditText edt_modplatename = (EditText)layout.findViewById(R.id.edt_modplatename);
        edt_modplatename.setText(platename);  
            
        //确认按钮  
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
              
            @Override  
            public void onClick(DialogInterface arg0, int arg1) {  
                
            	modplatename = edt_modplatename.getText().toString();

				sqldb.update_contentplatename(pressplateid, modplatename);

				update_dataset();
				m_datasetadpter.notifyDataSetChanged();
            }  
        });  
        //取消  
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {  
              
            @Override  
            public void onClick(DialogInterface arg0, int arg1) {             
            	arg0.dismiss();
            }  
        });  
        final AlertDialog dlg = builder.create();  
        dlg.show();  
     } 

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (AutoSMSActivity.isdebug) Log.e(TAG, "requestCode"+requestCode+"resultCode"+resultCode);
		if (requestCode==307) //请求编辑短信模板
		{
			if (resultCode==308) //返回短信模板
			{
				Bundle m_Bundle = data.getExtras();
			    contentplate = m_Bundle.getString("contentplate");
			    rec_contentplate = true;
			    if (AutoSMSActivity.isdebug) Log.e(TAG, contentplate);
			    edt_showresult.setText(GernatorSMSText.getSMSresult(contentplate));
			}
			if (resultCode==309)
			{					
				Bundle m_Bundle = data.getExtras();
			    contentplate = m_Bundle.getString("contentplate");
			    if (AutoSMSActivity.isdebug) Log.e(TAG, "back contentplate"+contentplate);
			    edt_showresult.setText(GernatorSMSText.getSMSresult(contentplate));
				sqldb.update_contentplate(m_Bundle.getInt("plateid"), contentplate);
				
				update_dataset();
				m_datasetadpter.notifyDataSetChanged();
			}
		}
		
	}
	
	

}
