package com.dayu.autosms.c;

import java.io.File;

import com.dayu.autosms.SmsTask;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper
{
	
	private static final String DB_NAME = "smstask.db";  
    private static final int DB_VERSION = 1;    
    private static final String CREATE_INFO = "create table if not exists smstask("  
            + "tasktime DATETEXT(19,19),"
    		+ "Taskname VARCHAR2(50),"
    		+ "Taskfilepath VARCHAR2(500), Taskfilename VARCHAR2(50), "
            + "Tasksuccess INT , Taskfail INT, "
    		+ "Tasktotal INT, Taskcontentplate INT"
    		+ ")";  
    
   

	public DBHelper(Context context, String name, CursorFactory factory)
	{
		super(context, name, null, DB_VERSION);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// TODO Auto-generated method stub
		
		String sql = CREATE_INFO;
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
		
	    sql = CREATE_INFO;
        db.execSQL(sql);
        
        Log.e("database","数据库更新成功");
	}
	
	public void delrec_smstask()
	{
		String sql ="delete from smstask";
		SQLiteDatabase db = getReadableDatabase();
    	db.execSQL(sql);
	}
	
	public void delrec_null()
	{
		String sql ="delete from stock_sort where buy_times = 0";
		SQLiteDatabase db = getReadableDatabase();
    	db.execSQL(sql);
	}

    public Cursor query_count()
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	Cursor c = db.rawQuery("select count(*) as tol from smstask", null);
    	return c;
    }
    
    
   
   
    
    public Cursor query_Sum_fenhong()
    {
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	return db.rawQuery("select sum(fenhong_mon) as jiaoyicishu from stock_sort where iscompleted ='true'", null);
    }
    
    public Cursor query_yongjin(boolean asc)
    {
    	String order;
    	order = asc ? "asc" : "desc";
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	return db.rawQuery("select zqdm,zqmc,yongjin_tol+yinhuashui_tol as tol from stock_sort where iscompleted ='true' order by tol "+ order +" limit 100", null);
    }
    
    public Cursor query_fenhong(boolean asc)
    {
    	String order;
    	order = asc ? "asc" : "desc";
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	return db.rawQuery("select zqdm,zqmc,fenhong_mon from stock_sort where iscompleted ='true' order by fenhong_mon "+ order +" limit 100", null);
    }
    
    public Cursor query_jiaoyicishu(boolean asc)
    {
    	String order;
    	order = asc ? "asc" : "desc";
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	return db.rawQuery("select zqdm,zqmc,buy_times+sell_times as tol from stock_sort where iscompleted ='true'  order by tol "+ order +" limit 100", null);
    }
    
    public Cursor query_yinlipercent(boolean asc)
    {
    	String order;
    	order = asc ? "asc" : "desc";
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	return db.rawQuery("select zqdm,zqmc,zonglirun,buy_tolmon,zonglirun/buy_tolmon*100 as tol from stock_sort where"
    			          + " iscompleted ='true'  order by tol "+ order +" limit 100", null);
    }
    
    public Cursor query_yinlipercent_all(boolean asc)
    {
    	String order;
    	order = asc ? "asc" : "desc";
    	SQLiteDatabase db = getReadableDatabase();
    	Log.e("database","query访问成功");  
    	return db.rawQuery("select zqdm,zqmc,zonglirun,buy_tolmon,fenhong_mon,yongjin_tol,(zonglirun+fenhong_mon-yongjin_tol)/buy_tolmon*100 as tol "
    			          + "from stock_sort where"
    			          + " iscompleted ='true'  order by tol "+ order +" limit 100", null);
    }
    
    public void insert_stockrec(SmsTask s)
    {
    	String sql ="insert into smstask(tasktime,Taskname,Taskfilepath,"
    			+ "Taskfilename,Tasksuccess,Taskfail,Tasktotal,Taskcontentplate,"
    			+")"
    			+ "values ('"+ s.getTasktime()+ "','"+ s.getTaskname()
    			+ "',"+ s.getTaskfilepath() + "," + s.getTaskfilename()
    			+ ","+ s.getTasksuccess() +"," + s.getTaskfail()
    			+ ","+ s.getTasktotal() +"," + s.getTaskcontentplate()
    		    + "')";
    	SQLiteDatabase db = getReadableDatabase();
    	db.execSQL(sql);
    	
      //  Log.e("database","数据库insert成功");
    }
 
	@Override
	public void onOpen(SQLiteDatabase db)
	{
		// TODO Auto-generated method stub
		super.onOpen(db);
		Log.e("database","数据库open成功");
	}

    
    
}
