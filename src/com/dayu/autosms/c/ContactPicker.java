package com.dayu.autosms.c;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dayu.autosms.AddsmstaskActivity;
import com.dayu.autosms.AutoSMSActivity;
import com.dayu.autosms.R;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ContactPicker extends Dialog {

	final static String TAG = "autosms";

	private final int SELECTED_COLOR = 0xffadbcb6;

	private ListView mContactsLv;

	private TextView mTitle;
	private ContactLvAdapter mContactLvAdapter;
	private PickContactEvent mEvent;
	SimpleAdapter simpleAdapter;
	private Cursor contacts;
	List<String[]> contact = new ArrayList<String[]>();
	//List<String> contactphone = new ArrayList<String>();

	public ContactPicker(Context context, PickContactEvent event) {
		super(context);
		mEvent = event;
		
		contacts = readAllContacts(context);
		init(context);
		
	}


	public interface PickContactEvent {
		public void onPickEvent(List<String[]> contact);
	}

	private void init(Context context) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_select_contact);
		mTitle = (TextView) findViewById(R.id.title_tv);
		mTitle.setText("请选择联系人:");
		
		mContactsLv = (ListView) findViewById(R.id.contact_lv);
		findViewById(R.id.ok_btn).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						setResult();
						ContactPicker.this.dismiss();
					}
				});

		findViewById(R.id.cancel_btn).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						ContactPicker.this.dismiss();
					}
				});

		
		mContactLvAdapter = new ContactLvAdapter();
		setListAdapterData(context);
		mContactsLv.setAdapter(mContactLvAdapter);
		
		
		mContactsLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				 ViewHolder t_vh1 =(ViewHolder) view.getTag();
				 if (t_vh1 != null)
				{
					 String[] tmp = contact.get(position);
					 if (t_vh1.mCheckBox.isChecked())
					{
						 t_vh1.mCheckBox.setChecked(false);
						 tmp[2] = "0";
					}else {
						t_vh1.mCheckBox.setChecked(true);
						tmp[2] = "1";
					}
					 
				}else
				{
					if (AutoSMSActivity.isdebug) Log.e(TAG, "ViewHolder is null");
				}
			}
		});
		
	}

	private void setResult() {
		
		    if (contact != null)
			{
		    	mEvent.onPickEvent(contact);
			}
			ContactPicker.this.dismiss();
	}

	

	private void setListAdapterData(Context mContext) {
		
				
		  int contactIdIndex = 0;
	        int nameIndex = 0;
	        
	        if(contacts.getCount() > 0) {
	            contactIdIndex = contacts.getColumnIndex(ContactsContract.Contacts._ID);
	            nameIndex = contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
	        }
	        while(contacts.moveToNext()) {
	            String contact_Id = contacts.getString(contactIdIndex);
	            String contact_name = contacts.getString(nameIndex);
	            if (AutoSMSActivity.isdebug) Log.e(TAG, contact_Id);
	            if (AutoSMSActivity.isdebug) Log.e(TAG, contact_name);
	            String[] tmp = new String[3];
	            tmp[0] = contact_name;
	            tmp[2] = "0";
	        //    contact.add(contact_name);
	            /*
	             * 查找该联系人的phone信息
	             */
	            Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
	                    null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contact_Id, null, null);
	            int phoneIndex = 0;
	            if(phones.getCount() > 0) {
	                phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
	            }
	            while(phones.moveToNext()) {
	                String phoneNumber = phones.getString(phoneIndex);
	                if (AutoSMSActivity.isdebug) Log.e(TAG, phoneNumber);
	                //contactphone.add(phoneNumber);
	                tmp[1] = phoneNumber;
	            }
	            
	            contact.add(tmp);
	            
	            if (phones!=null)
				{
	            	phones.close();
				}
	            
	            
	        }
	        
	        if (contacts!=null)
			{
	        	contacts.close();
			}
	        
	        
		/*
		Collections.sort(contactname, new Comparator<String>() {
			@Override
			public int compare(String s1,String s2) {
				
				// sort by the ASCII code:a-z 0-9
				return -(s2.toLowerCase().compareTo(s1.toLowerCase()));
			}
		});
         */
		
		mContactLvAdapter.setList(contact);
		mContactLvAdapter.setSelectedIndex(-1);
		
	   	
	}

	

	private class ContactLvAdapter extends BaseAdapter {

		private int mSelectedIndex = -1;
		public List<String[]> list;

		public void setList(List<String[]> l) {
			list = l;
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
		}

		public String getContactname(int position) {
			if (position >= getCount() || position < 0) {
				return null;
			}
			return contact.get(position)[0];
		}
		
		public String getContactphone(int position) {
			if (position >= getCount() || position < 0) {
				return null;
			}
			return contact.get(position)[1];
		}
		
		public boolean getischecked(int position) {
			if (position >= getCount() || position < 0) {
				return false;
			}
			 
			 boolean ischeck = false;
			 String[] tmp = contact.get(position);
			 if (tmp[2].equals("1"))
			{
				 ischeck = true;
			}
			 
			return ischeck;
		}

		@Override
		public long getItemId(int position) {
			if (AutoSMSActivity.isdebug) Log.e(TAG, "ha from this" + position);
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.listview_showcontact, null);
				holder.mContactname = (TextView) convertView.findViewById(R.id.tv_listcontact);
				holder.mContactphone = (TextView) convertView.findViewById(R.id.tv_listphone);
				holder.mContactname.setTextColor(0xff000000);
				holder.mContactphone.setTextColor(0x7d6366e3);
				
				holder.mCheckBox = (CheckBox)convertView.findViewById(R.id.chb_selcontact);
				
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
		
			holder.mContactname.setText( getContactname(position));
			holder.mContactphone.setText(getContactphone(position));
			holder.mCheckBox.setTag(position);
			holder.mCheckBox.setChecked(getischecked(position));
			
			if (AutoSMSActivity.isdebug) Log.e(TAG, "now position: "+position);

			if (position == mSelectedIndex) {
				convertView.setBackgroundColor(SELECTED_COLOR);
			} else {
				convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			return convertView;
		}

		public void setSelectedIndex(int index) {
			mSelectedIndex = index;
			notifyDataSetChanged();
		}

		public int getSelectedIndex() {
			return mSelectedIndex;
		}

		

		@Override
		public Object getItem(int position)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		
	}
	
	class ViewHolder {
		public TextView mContactphone;
		public TextView mContactname;
		public CheckBox mCheckBox;
	}
	
	/*
     * 读取联系人的信息
     */
    public Cursor readAllContacts(Context mContext) {
        return mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, 
                 null, null, null, null);
    }
}