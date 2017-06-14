package com.dayu.autosms.m;

import java.util.LinkedList;

import com.dayu.autosms.AutoSMSActivity;

import android.R.bool;
import android.util.Log;

public class SmsTaskQuery
{
	final static String TAG = "autosms";
	static LinkedList<SmsBase> m_sendlist ;
	static int m_sendlist_count = 0;
	static int tasktotal_count = 0;
	static int addlist_num = 5;
	static boolean exit = false;
	
		
	public SmsTaskQuery()
	{
		if (AutoSMSActivity.isdebug) Log.e(TAG,"create sms send query "+ String.valueOf(m_sendlist.size()));
	}
	
	synchronized public static void init_sendlist()
	{
		m_sendlist = new LinkedList<>();
		m_sendlist.clear();
		m_sendlist_count = 0;
		tasktotal_count = 0;
		if (AutoSMSActivity.isdebug) Log.e(TAG,"init query success");
	}

	synchronized public static int insert_sendlist(SmsBase smsBase)
	{
		m_sendlist.addLast(smsBase);
		m_sendlist_count = m_sendlist.size();
		if (AutoSMSActivity.isdebug) Log.e(TAG,"insert "+ String.valueOf(m_sendlist.size()));
		return m_sendlist_count;
	}
	
	synchronized public static int query_sendlist_count()
	{
		m_sendlist_count = m_sendlist.size();
		return m_sendlist_count;
	}
	
	synchronized public static SmsBase poll_sendlist()
	{
		SmsBase t_SmsBase = null;
		if ((t_SmsBase = m_sendlist.pollFirst())!=null)
		{
			if (AutoSMSActivity.isdebug) Log.e(TAG,"poll "+ String.valueOf(m_sendlist.size()));
			return t_SmsBase;		
		}
		
		return null;
	}
	
	
}
