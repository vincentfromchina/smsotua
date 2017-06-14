package com.dayu.autosms.c;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.dayu.autosms.R;
import com.dayu.autosms.R.drawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Showresult extends View
{
	final static String TAG = "autosms";
	 private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG  
             | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG  
             | Canvas.CLIP_TO_LAYER_SAVE_FLAG;  
	private Paint mPaint ;
	private Bitmap bp, bitmap2;
	
	 InputStream is;
	private String sum_zonglirun,sum_buytimes,sum_selltimes,
	sum_zongjine,sum_completed,sum_fenhong,max_zonglirun,max_lirunpercent,
	max_cjrq,min_cjrq;
	private TextPaint tp;
	 Options opts;
	int x=100,y=100;
	
	String content = "你好 welcome";
	
	private int Screenwidth;
	private int Screenheight;
	
	public Showresult(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mPaint = new Paint();
	    tp = new TextPaint();
		tp.setColor(Color.BLACK);
		tp.setShadowLayer(3, 1, 1, Color.WHITE);
		tp.setTextSize(30);
		 opts = new Options();
		 opts.inInputShareable = true;
		 opts.inPreferredConfig = Config.RGB_565;
		 
		 bp =  BitmapFactory.decodeResource(getResources(), R.drawable.xin2335_2, opts);
	}	
		 
		
	
	 private int getStringWidth(String str) {
		  return (int) mPaint.measureText(str);
		 }

	 private int getStringHeight(String str) {
		  FontMetrics fr = mPaint.getFontMetrics();
		  return (int) Math.ceil(fr.descent - fr.top) + 2;  //ceil() 函数向上舍入为最接近的整数。

		 }
	 
	 public String formatdate2str(String rq)
	 {
		 SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		 String cjrq ="";
		 java.util.Date d1;
		 try
		{   
			 d1 = df.parse(rq);
			 SimpleDateFormat df1 = new SimpleDateFormat("yyyy年MM月dd日");
			 cjrq = df1.format(d1);
		} catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 return cjrq;
	 }
	 
	 public void set_contentchange(String v_content)
	 {
		 content = v_content;
	 }
	 
	 public void set_screenwidth(int v_screenwidth)
	 {
		 Screenwidth = v_screenwidth;
	 }
	 
	 public void set_screenheight(int v_screenheight)
	 {
		 Screenheight = v_screenheight;
	 }
	 
	 

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas)
	{
		 
		super.onDraw(canvas);
		
		PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
		canvas.setDrawFilter(pfd);
		
		int mapwidth = canvas.getWidth(),mapheight = canvas.getHeight();
		
		int numline = 15;
		
		int includecharwidth = Math.round(mapwidth*300/Screenwidth);
		
		float onetext = includecharwidth/numline;
		
		 Rect rf = new Rect(0, 0, mapwidth, mapheight);
		 
		/* is = getResources().openRawResource(R.drawable.cgcjd);
		 	 
		 x = x<800 ? x+20: 100;
		
		 y = y<800 ? y+20: 100;
		 
		 bitmap2 = Bitmap.createBitmap(BitmapFactory.decodeStream(is, null, opts),0,0,x,y);
		*/ 
		 
	//	 canvas.drawBitmap(bp, null, rf, mPaint);
		 
		 	 
		 StringBuilder sbBuilder = new StringBuilder();
		 sbBuilder.append(content);
		 
		 tp.setTextSize(onetext);
		 tp.setAntiAlias(true);
		 
		 StaticLayout layout = new StaticLayout(sbBuilder.toString(),tp,includecharwidth,Alignment.ALIGN_NORMAL,1.3F,0.0F,true);

        
		 canvas.translate(mapwidth*120/Screenwidth,mapheight*150/Screenheight);

		layout.draw(canvas);
		invalidate();
		
		/*
		 try
		{
			Class contentsetact = Class.forName("com.dayu.autosms.ContentSetActivity");
			
			contentsetact.getMethod("getcontent", null ).invoke(contentsetact, null);
		
			//	Method[] mm = contentsetact.getMethods();
		//	mm.toString();
			
			//Log.e("TAG",contentsetact.getPackage().toString());
		} catch (ClassNotFoundException e)
		{
			Log.e("TAG", "ClassNotFoundException");
			e.printStackTrace();
		} catch (NoSuchMethodException e)
		{
			Log.e("TAG", "NoSuchMethodException");
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			Log.e("TAG", "IllegalAccessException");
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			Log.e("TAG", "IllegalArgumentException");
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			Log.e("TAG", "InvocationTargetException");
			e.printStackTrace();
		}
		 */
		 
	//	Log.e("gushiriji","mapwidth"+mapwidth+",mapheight"+mapheight+",includecharwidth"+includecharwidth+",onetext"+onetext ) ;
	//	Log.e("gushiriji", ""+canvas.isHardwareAccelerated());
		
	}
	
}
