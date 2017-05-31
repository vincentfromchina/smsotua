package com.dayu.autosms.c;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Getnowtime
{
	 SimpleDateFormat nowdate;
	 SimpleDateFormat nowtime;
	 Date date;
	 

	public Getnowtime()
	{
		super();
	}
	
	public String getnowtime(String timeformat)
	{
		nowdate = new SimpleDateFormat(timeformat);
	    long UTCtime = System.currentTimeMillis();
		date = new Date(UTCtime);
		String tmp_time = nowdate.format(date);
		return tmp_time;
	}

}
