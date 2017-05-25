package com.dayu.autosms.m;

public class SmsBase
{
	String sms_sendphone;
	String sms_sendtext;
	public SmsBase(String sms_sendphone, String sms_sendtext)
	{
		super();
		this.sms_sendphone = sms_sendphone;
		this.sms_sendtext = sms_sendtext;
	}
	public String getSms_sendphone()
	{
		return sms_sendphone;
	}
	public void setSms_sendphone(String sms_sendphone)
	{
		this.sms_sendphone = sms_sendphone;
	}
	public String getSms_sendtext()
	{
		return sms_sendtext;
	}
	public void setSms_sendtext(String sms_sendtext)
	{
		this.sms_sendtext = sms_sendtext;
	}

	

}
