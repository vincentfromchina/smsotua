package com.dayu.autosms;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.dayu.autosms.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class WebActivity extends Activity
{
	private WebView v_help;
	private ProgressBar bar ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		
     	v_help = (WebView)findViewById(R.id.webhelp);
     	
     	bar = (ProgressBar)findViewById(R.id.myProgressBar);
     	v_help.loadUrl(getIntent().getStringExtra("urls"));

     	WebSettings settings = v_help.getSettings();
		v_help.requestFocus();
		v_help.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		settings.setJavaScriptEnabled(true);
		
		v_help.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				// TODO Auto-generated method stub
				// 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				view.loadUrl(url);
				return true;
			}
		});
		
		v_help.setWebChromeClient(new WebChromeClient() {

	          @Override
	          public void onProgressChanged(WebView view, int newProgress) {
	              if (newProgress == 100) {
	                  bar.setVisibility(View.INVISIBLE);
	              } else {
	                  if (View.INVISIBLE == bar.getVisibility()) {
	                      bar.setVisibility(View.VISIBLE);
	                  }
	                  bar.setProgress(newProgress);
	              }
	              super.onProgressChanged(view, newProgress);
	          }
	         
	      });
		
		Button btn_back = (Button)findViewById(R.id.btn_fanhuishouye);
		btn_back.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				/*Intent web = new Intent();
				web.setClass(WebActivity.this, TraceActivity.class);
				
				startActivity(web);*/
				finish();
			}
		});
		
		ArrayList<HashMap<String, Object>> mdata = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map1 = new HashMap<String, Object>();
		map1.put("type", "image");
		map1.put("value", "https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo/bd_logo1_31bdc765.png");
		mdata.add(map1);
		HashMap<String, Object> map2 = new HashMap<String, Object>();
		map2.put("type", "text");
		map2.put("txt", "<html><body>一、【协议的范围】<br>1.1【协议适用主体范围】<br><a href='gaoji.html'>完成高级设置</a></body></html>");
		mdata.add(map2);
		HashMap<String, Object> map3 = new HashMap<String, Object>();
		map3.put("type", "image");
		map3.put("value", "https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=1463927064,3086155938&fm=80&w=179&h=119&img.JPEG");
		mdata.add(map3);
		
	//	CustomTextView ctView = (CustomTextView)findViewById(R.id.CustomTextView);
	//	ctView.setText(mdata);
		
	}
	
	
	
	
	
	
    public boolean onKeyDown(int keyCoder,KeyEvent event){  
        if(v_help.canGoBack() && keyCoder == KeyEvent.KEYCODE_BACK){  
        	v_help.goBack();   //goBack()表示返回webView的上一页面  

                return true;  
         }  
         return false;  
      }  
}
