package com.dayu.autosms;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.dayu.autosms.ManagercontentplateActivity.platelistadapter;
import com.dayu.autosms.ManagercontentplateActivity.platelistadapter.ViewHolder;
import com.dayu.autosms.c.DBHelper;
import com.dayu.autosms.c.GernatorSMSText;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ManagertaskActivity extends Activity
{
	DBHelper sqldb ;
	Cursor m_Cursor;
	private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局
	private List<String> taskname ;
	private List<Integer>  taskid;
	private List<String>  taskinfo;
	private  platelistadapter m_datasetadpter;
	 private static ListView lv_smstask ;
	 private static TextView tv_showtaskinfo ;
	 static int pressplateid ;
	 static boolean isfirstlongclick = true;
	 

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managersmstask);
		
		sqldb = new DBHelper(ManagertaskActivity.this, "smstask.db", null);
		
		mInflater = LayoutInflater.from(ManagertaskActivity.this);
		
	    lv_smstask = (ListView)findViewById(R.id.lv_smstask);
	    tv_showtaskinfo = (TextView)findViewById(R.id.tv_showtaskinfo);

	    update_dataset();
	
	m_datasetadpter = new platelistadapter();
	
	lv_smstask.setAdapter(m_datasetadpter);
	
	lv_smstask.setOnItemClickListener(new OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			//点击后在标题上显示点击了第几行
            Log.e("autophone","你点击了第"+position +"id:"+id);
            
           if (isfirstlongclick)
		   {
        	   isfirstlongclick = !isfirstlongclick;
        	   Toast.makeText(ManagertaskActivity.this, "长按修改模板名字", Toast.LENGTH_LONG).show();    
		   }   
            
           tv_showtaskinfo.setText(taskinfo.get(position));
           
		}
		
	});
	
	lv_smstask.setOnItemLongClickListener(new OnItemLongClickListener()
	{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
		{
			pressplateid = taskid.get(position);
			Log.e("autophone","你长按了第"+position+"模板名"+taskname.get(position));
			show_modtasknameDialog(taskname.get(position));
		
			return false;
		}
		
	});
	
}
	
	class platelistadapter extends BaseAdapter
	{
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			 ViewHolder holder;
	            //观察convertView随ListView滚动情况
	     
	            if (convertView == null) {
	                     convertView = mInflater.inflate(R.layout.showsmstask, parent, false);

	                     holder = new ViewHolder();
	                    /*得到各个控件的对象*/
	                    holder.taskname = (TextView) convertView.findViewById(R.id.tv_listsmstask);
	                    holder.delete_task = (Button) convertView.findViewById(R.id.btn_deltask);
	                    holder.start_task = (Button) convertView.findViewById(R.id.btn_starttask);
	                    holder.start_task.setTag(taskid.get(position));
	                    holder.delete_task.setTag(taskid.get(position));
	                    Log.e("autophone", ""+position+"--"+taskid.get(position));
	                    
	                   
	                    holder.delete_task.setOnClickListener(new OnClickListener()
						{
							
							@Override
							public void onClick(View v)
							{
								Log.v("MyListViewBase", "你点击了删除按钮" + v.getTag()); 
								sqldb.delrec_smstask((Integer)v.getTag());
								 update_dataset();
							     m_datasetadpter.notifyDataSetChanged();
							   //  lv_contentplate.setAdapter(m_datasetadpter);
							}
						});
	                    
	                    holder.start_task.setOnClickListener(new OnClickListener()
						{
							
							@Override
							public void onClick(View v)
							{
								Intent open_starttaskactivity = new Intent();
								open_starttaskactivity.setClass(ManagertaskActivity.this, StartSMStaskActivity.class);
		         				
								Bundle bundle = new Bundle();
		         				bundle.putInt("taskid", (int)v.getTag());
		         				open_starttaskactivity.putExtras(bundle);
		         				
								startActivity(open_starttaskactivity);
								
								finish();
							}
						});
	                    
	                    convertView.setTag(holder);//绑定ViewHolder对象
	                    Log.v("autophone", "getView " + position + " " + convertView);
	          }
	          else{
	                    holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
	                    Log.e("autophone", "会执行");
	                  }
	            /*设置TextView显示的内容，即我们存放在动态数组中的数据*/
	            holder.taskname.setText((String)getItem(position));
	            
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
			return taskname.get(position);
		}
		
		@Override
		public int getCount()
		{			
			//Log.e("autophone", "getCount "+m_Cursor.getCount());
			return taskname.size();
		}
		
	   class ViewHolder{
		    public TextView taskname;
		    public Button   delete_task;
		    public Button	start_task;
		    }
	};
	
	public void update_dataset()
	{
		taskname = new ArrayList<>();		
		taskid = new ArrayList<>();
		taskinfo = new ArrayList<>();
		
	    m_Cursor =  sqldb.query_smstask(false);
	    Log.e("autophone","m_Cursor.getCount"+ m_Cursor.getCount());
	    
		while (m_Cursor.moveToNext())
		{
			taskname.add(m_Cursor.getString(2));	
			taskid.add(Integer.valueOf(m_Cursor.getInt(9)));
			taskinfo.add("时间:"+m_Cursor.getString(1)+"   发送数:"+Integer.valueOf(m_Cursor.getInt(7)));
		}
	}
    

	public void show_modtasknameDialog(String platename){         
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        LayoutInflater inflater = getLayoutInflater();  
        final View layout = inflater.inflate(R.layout.modtasknamedialog, null);//获取自定义布局  
        builder.setView(layout);  
    //    builder.setIcon(R.drawable.ic_launcher);//设置标题图标  
        builder.setTitle("编辑短信模板名称");//设置标题内容  
        //builder.setMessage("");//显示自定义布局内容  
        final EditText edt_modtaskname = (EditText)layout.findViewById(R.id.edt_modtaskname);
        edt_modtaskname.setText(platename);  
            
        //确认按钮  
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
              
            @Override  
            public void onClick(DialogInterface arg0, int arg1) {  
                
            	String modtaskname = edt_modtaskname.getText().toString();

				sqldb.update_smstaskname(pressplateid, modtaskname);

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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.managertask, menu);
		return true;
	}

}
