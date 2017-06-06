package com.dayu.autosms.c;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.dayu.autosms.m.SmsTask;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper
{
	
	private static final String DB_NAME = "smstask.db";  
    private static final int DB_VERSION = 7;    
    private static final String CREATE_smstask = "create table if not exists smstask("  
            + "taskStarttime DATETEXT(19,19),"
            + "taskEndtime DATETEXT(19,19),"
    		+ "taskname VARCHAR2(50),"
    		+ "taskfilepath VARCHAR2(500), taskfilename VARCHAR2(50), "
            + "tasksuccess INT , Taskfail INT, "
    		+ "tasktotal INT, taskcontentplate INT,"
            + "taskid INTEGER PRIMARY KEY AUTOINCREMENT"
    		+ ")";  
    private static final String CREATE_contentplate = "create table if not exists contentplate("  
            + "plateid INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "platename VARCHAR2(50),"
    		+ "platecontent VARCHAR2(1000),"
    		+ "plateaddtime DATETEXT(19, 19)"
    		+ ")";
    
    private static final String CREATE_config = "CREATE TABLE if not exists config ("
    		+"sign VARCHAR2(50)," 
    		+"sendinteval INT DEFAULT 1)";
    
    private static final String update_config = "insert into config(sendinteval)"
    		+"values(1)";
   

	public DBHelper(Context context, String dbname, CursorFactory factory)
	{
		super(context, dbname, null, DB_VERSION);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// TODO Auto-generated method stub
		
		String sql = CREATE_smstask;
        db.execSQL(sql);
        
        sql = CREATE_contentplate;
        db.execSQL(sql);
        
        Log.e("database","数据库创建成功");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
		String sql = "";
	  /* sql = "CREATE TABLE [condition] ([periodid] INT,  [wendu_l] INT,  [wendu_h] INT,  [sun_l] INT,  [sun_h] int,  [water] VARCHAR2(1000),  [pzid] INT,  [waterpersecond] INT)";
        db.execSQL(sql);
      		
		/* sql = "alter table condition add [suntime] VARCHAR2(500)";
		db.execSQL(sql);
		
	  
		 sql = "update condition set suntime='06:00:00|20:00:00'";
		 db.execSQL(sql); */
		 
				 
		String pre ="drop table if exists smstask";
		db.execSQL(pre);
		
		pre ="drop table if exists contentplate";
		db.execSQL(pre);
		
		pre ="drop table if exists config";
		db.execSQL(pre);
		
		sql = CREATE_smstask;
        db.execSQL(sql);
        
        sql = CREATE_contentplate;
        db.execSQL(sql);
        
        sql = CREATE_config;
        db.execSQL(sql);
        
        sql = update_config;
        db.execSQL(sql);
        
        Log.e("database","数据库更新成功");
	}
	
	public void delrec_smstask(Integer taskid)
	{
		String sql ="delete from smstask where taskid=" +taskid;
		SQLiteDatabase db = getReadableDatabase();
    	db.execSQL(sql);
	}
	
	public void delrec_smscontentplate(Integer plateid)
	{
		String sql ="delete from contentplate where plateid =" +plateid;
		SQLiteDatabase db = getReadableDatabase();
    	db.execSQL(sql);
    	Log.e("database","delete成功"); 
	}

    public Cursor query_count()
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	Cursor c = db.rawQuery("select count(*) as tol from smstask", null);
    	return c;
    }
    
    public void insert_smstask(SmsTask s)
    {
    	String sql ="insert into smstask(taskStarttime,taskname,taskfilepath,"
    			+ "taskfilename,tasksuccess,taskfail,tasktotal,taskcontentplate,"
    			+"taskEndtime)"
    			+ "values ('"+ s.getTaskStarttime()+ "','"+ s.getTaskname()
    			+ "','"+ s.getTaskfilepath() + "','" + s.getTaskfilename()
    			+ "',"+ s.getTasksuccess() +"," + s.getTaskfail()
    			+ ","+ s.getTasktotal() +"," + s.getTaskcontentplate()
    		    +",'" + s.getTaskEndtime()+"')";
    	SQLiteDatabase db = getReadableDatabase();
    	db.execSQL(sql);
    	
        Log.e("database","数据库insert成功");
    }
    
    public void insert_smscontentplate(String platename,String contentplate,String opttime)
    {
    	String sql ="insert into contentplate(platename,platecontent,plateaddtime)"
    			+ "values ('"+ platename+ "','"+ contentplate+ "','"+ opttime
    			+"')";
    	SQLiteDatabase db = getReadableDatabase();
    	db.execSQL(sql);
    	
        Log.e("database","数据库insert成功");
    }
   
    public void update_smstask(SmsTask s)
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","update_smstask成功");  
        db.execSQL("update smstask set taskEndtime='"+s.getTaskEndtime()+ "',tasksuccess=" +s.getTasksuccess()+ ",Taskfail="+ s.getTaskfail()+",tasktotal="+s.getTasktotal()+ " where taskid="+s.getTaskid());
    }
    
    public void update_contentplate(int plateid,String contentplate)
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","update contentplate成功");  
        db.execSQL("update contentplate set platecontent='"+contentplate+"' where plateid="+plateid);
    }
    
    public void update_contentplatename(int plateid,String contentplatename)
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","update contentplate set platename='"+contentplatename+"' where plateid="+plateid);  
        db.execSQL("update contentplate set platename='"+contentplatename+"' where plateid="+plateid);
    }
    
    public void update_smstaskname(int taskid,String taskname)
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","update smstask set taskname='"+taskname+"' where taskid="+taskid);  
        db.execSQL("update smstask set taskname='"+taskname+"' where taskid="+taskid);
    }
    
    public void update_inteval(int inteval)
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","update config set sendinteval="+inteval);  
        db.execSQL("update config set sendinteval="+inteval);
    }
    
    
    public Cursor query_smscontentplate()
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	return db.rawQuery("select * from contentplate order by plateaddtime desc", null);
    }
    
    public Cursor query_smstask(boolean asc)
    {
    	String order;
    	order = asc ? "asc" : "desc";
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	return db.rawQuery("select * from smstask order by taskEndtime "+ order, null);
    }
    
    public Cursor get_sendtask(int taskid)
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","select * from smstask where taskid=" + taskid);  
    	return db.rawQuery("select * from smstask,contentplate where taskid=" + taskid +" and smstask.taskcontentplate=contentplate.plateid",null);
    			         
    }
    
    public Cursor get_config()
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","select * from config");  
    	return db.rawQuery("select * from config",null);
    			         
    }
    
 
	@Override
	public void onOpen(SQLiteDatabase db)
	{
		// TODO Auto-generated method stub
		super.onOpen(db);
		Log.e("database","数据库open成功");
	}

	public static void copyDataBaseToSD(String dbfilepath){  
	     if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {  
	            return ;  
	         }  
	     File dbFile = new File( dbfilepath);  
	     File file  = new File(Environment.getExternalStorageDirectory(), "copyof_smstask.db");  
	       
	     FileChannel inChannel = null,outChannel = null;  
	       
	     try {  
	        file.createNewFile();  
	        inChannel = new FileInputStream(dbFile).getChannel();  
	        outChannel = new FileOutputStream(file).getChannel();  
	        inChannel.transferTo(0, inChannel.size(), outChannel);  
	    } catch (Exception e) {  
	        Log.e("database", "copy dataBase to SD error.");  
	        e.printStackTrace();  
	    }finally{  
	        try {  
	            if (inChannel != null) {  
	                inChannel.close();  
	                inChannel = null;  
	            }  
	            if(outChannel != null){  
	                outChannel.close();  
	                outChannel = null;  
	            }  
	        } catch (IOException e) {  
	            Log.e("database", "file close error.");  
	            e.printStackTrace();  
	        }  
	    }  
	}  
    
}
