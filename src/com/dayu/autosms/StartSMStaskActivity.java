package com.dayu.autosms;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class StartSMStaskActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_smstask);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start_smstask, menu);
		return true;
	}

}
