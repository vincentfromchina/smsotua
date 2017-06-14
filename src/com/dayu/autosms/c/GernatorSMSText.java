package com.dayu.autosms.c;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class GernatorSMSText
{
	final static String TAG = "autosms";
	//{|n|} {|@|}  {| |} {|A|} {|a|}  {|d|} {|t|}
		final static String spc[] = {"^","|","`","&","*","%","$","#","@"};
		final static String little_a[] = {"a","b","c","d","e","f","g","h",
				"i","j","k","l","m","n","o","p","q","r","s","t","u","v"
				,"w","x","y","z"};
		final static String big_a[] = {"A","B","C","D","E","F","G","H",
				"I","J","K","L","M","N","O","P","Q","R","S","T","U","V"
				,"W","X","Y","Z"};
		final static String num[] = {"0","1","2","3","4","5","6","7","8","9"};
		final static String blank[] = {" "," "," "};
		
		 static SimpleDateFormat nowdate;
		 static SimpleDateFormat nowtime;
		 static Date date;
		 
	static public  String sqliteEscape(String keyWord){  
		        keyWord = keyWord.replace("/", "//");  
		        keyWord = keyWord.replace("'", "''");  
		        keyWord = keyWord.replace("[", "/[");  
		        keyWord = keyWord.replace("]", "/]");  
		        keyWord = keyWord.replace("%", "/%");  
		        keyWord = keyWord.replace("&","/&");  
		        keyWord = keyWord.replace("_", "/_");  
		        keyWord = keyWord.replace("(", "/(");  
		        keyWord = keyWord.replace(")", "/)");  
		        return keyWord;  
		    }  
		 
	static public String getSMSresult(String orgStr)
    {
    	int t = -1;
		
		while ((t=orgStr.indexOf("{|@|}"))>=0)
		{
			//Log.e(TAG, String.valueOf(t));
			orgStr = replacestr(orgStr,"{|@|}",t);
		}
		
		while ((t=orgStr.indexOf("{|A|}"))>=0)
		{
			//Log.e(TAG, String.valueOf(t));
			orgStr = replacestr(orgStr,"{|A|}",t);
		}
		
		while ((t=orgStr.indexOf("{|a|}"))>=0)
		{
			//Log.e(TAG, String.valueOf(t));
			orgStr = replacestr(orgStr,"{|a|}",t);
		}
		
		while ((t=orgStr.indexOf("{| |}"))>=0)
		{
			//Log.e(TAG, String.valueOf(t));
			orgStr = replacestr(orgStr,"{| |}",t);
		}
		
		while ((t=orgStr.indexOf("{|n|}"))>=0)
		{
			//Log.e(TAG, String.valueOf(t));
			orgStr = replacestr(orgStr,"{|n|}",t);
		}
		
		while ((t=orgStr.indexOf("{|d|}"))>=0)
		{
			//Log.e(TAG, String.valueOf(t));
			orgStr = replacestr(orgStr,"{|d|}",t);
		}
		
		while ((t=orgStr.indexOf("{|t|}"))>=0)
		{
			//Log.e(TAG, String.valueOf(t));
			orgStr = replacestr(orgStr,"{|t|}",t);
		}
		return orgStr;
    }
    
    static String greanater(String p)
    {
    	switch (p)
		{
		case "{|@|}":
			Random r=new Random();
			int i=r.nextInt(spc.length-0)+0;
			p = spc[i];
			break;
		case "{|A|}":
			 r=new Random();
			 i=r.nextInt(big_a.length-0)+0;
			p = big_a[i];
			break;
		case "{|a|}":
			 r=new Random();
			 i=r.nextInt(little_a.length-0)+0;
			p = little_a[i];
			break;
		case "{|n|}":
			 r=new Random();
			 i=r.nextInt(num.length-0)+0;
			p = num[i];
			break;
		case "{| |}":
			 r=new Random();
			 i=r.nextInt(blank.length-0)+0;
			p = blank[i];
			break;
		case "{|d|}":
			 
			 nowdate = new SimpleDateFormat("yyyyƒÍ M‘¬d»’");
			 long UTCtime = System.currentTimeMillis();
				date = new Date(UTCtime);
				p = nowdate.format(date);
			break;
		case "{|t|}":
			 
			nowtime = new SimpleDateFormat("HH:mm");
			UTCtime = System.currentTimeMillis();
			date = new Date(UTCtime);
			p = nowtime.format(date);	     
			break;
		default:
			break;
		}
		return p;
		
		
    }
	
    static String replacestr(String s,String rep,int pos)
    {
    	String s_1 = s.substring(0, pos);
    //	Log.e(TAG, s_1);
    	String s_2 = s.substring(pos+5, s.length());
    //	Log.e(TAG, s_2);
    	
    	switch (rep)
		{
		case "{|@|}":
			s = s_1 + greanater("{|@|}") + s_2;
			break; 
		case "{|A|}":
			s = s_1 + greanater("{|A|}") + s_2;
			break; 
		case "{|a|}":
			s = s_1 + greanater("{|a|}") + s_2;
			break; 
		case "{|n|}":
			s = s_1 + greanater("{|n|}") + s_2;
			break; 
		case "{| |}":
			s = s_1 + greanater("{| |}") + s_2;
			break; 
		case "{|d|}":
			s = s_1 + greanater("{|d|}") + s_2;
			break;
		case "{|t|}":
			s = s_1 + greanater("{|t|}") + s_2;
			break;
		default:
			break;
		}

		return s;
    }
}
