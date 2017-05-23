package com.dayu.autosms;

import java.util.LinkedList;

import android.R.bool;
import android.util.Log;

public class SmsTaskQuery
{
	static LinkedList<SmsBase> m_sendlist = new LinkedList<>();
	static int m_sendlist_count = 0;
	static int tasktotal_count = 0;
	static int addlist_num = 5;
	static boolean exit = false;
	
	
	
	
	public SmsTaskQuery()
	{
		Log.e("autophone","create query "+ String.valueOf(m_sendlist.size()));
	}

	synchronized public static int insert_sendlist(SmsBase smsBase)
	{
		m_sendlist.addLast(smsBase);
		m_sendlist_count = m_sendlist.size();
		Log.e("autophone","insert "+ String.valueOf(m_sendlist.size()));
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
			Log.e("autophone","poll "+ String.valueOf(m_sendlist.size()));
			return t_SmsBase;		
		}
		
		return null;
	}
	
	
}
