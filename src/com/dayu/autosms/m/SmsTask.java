package com.dayu.autosms.m;


public class SmsTask
{
	String TaskStarttime;
	String TaskEndtime;
	String Taskname;
	String Taskfilepath;
	String Taskfilename;
	String platename;
	String platecontent;
	
	int Taskid = 0;
	int Tasksuccess = 0;
	int Taskfail = 0;
	int Tasktotal = 0;
	int Taskcontentplate;  //¶ÌÐÅÄ£°å
	public String getTaskStarttime()
	{
		return TaskStarttime;
	}
	public void setTaskStarttime(String tasktime)
	{
		TaskStarttime = tasktime;
	}
	public String getTaskEndtime()
	{
		return TaskStarttime;
	}
	public void setTaskEndtime(String tasktime)
	{
		TaskStarttime = tasktime;
	}
	public String getTaskname()
	{
		return Taskname;
	}
	public void setTaskname(String taskname)
	{
		Taskname = taskname;
	}
	public String getTaskfilepath()
	{
		return Taskfilepath;
	}
	public void setTaskfilepath(String taskfilepath)
	{
		Taskfilepath = taskfilepath;
	}
	public String getTaskfilename()
	{
		return Taskfilename;
	}
	public void setTaskfilename(String taskfilename)
	{
		Taskfilename = taskfilename;
	}
	public int getTasksuccess()
	{
		return Tasksuccess;
	}
	public void setTasksuccess(int tasksuccess)
	{
		Tasksuccess = tasksuccess;
	}
	public int getTaskfail()
	{
		return Taskfail;
	}
	public void setTaskfail(int taskfail)
	{
		Taskfail = taskfail;
	}
	public int getTasktotal()
	{
		return Tasktotal;
	}
	public void setTasktotal(int tasktotal)
	{
		Tasktotal = tasktotal;
	}
	public int getTaskcontentplate()
	{
		return Taskcontentplate;
	}
	public void setTaskcontentplate(int taskcontentplate)
	{
		Taskcontentplate = taskcontentplate;
	}
	public String getPlatename()
	{
		return platename;
	}
	public void setPlatename(String platename)
	{
		this.platename = platename;
	}
	public String getPlatecontent()
	{
		return platecontent;
	}
	public void setPlatecontent(String platecontent)
	{
		this.platecontent = platecontent;
	}
	public int getTaskid()
	{
		return Taskid;
	}
	public void setTaskid(int taskid)
	{
		Taskid = taskid;
	}
	
	
	
}
