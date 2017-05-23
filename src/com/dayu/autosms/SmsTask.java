package com.dayu.autosms;

import android.R.integer;

public class SmsTask
{
	String Tasktime;
	String Taskname;
	String Taskfilepath;
	String Taskfilename;
	
	int Tasksuccess;
	int Taskfail;
	int Tasktotal;
	int Taskcontentplate;  //¶ÌÐÅÄ£°å
	public String getTasktime()
	{
		return Tasktime;
	}
	public void setTasktime(String tasktime)
	{
		Tasktime = tasktime;
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
	
	
	
}
