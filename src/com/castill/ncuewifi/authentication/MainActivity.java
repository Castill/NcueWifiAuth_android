package com.castill.ncuewifi.authentication;

import com.castill.ncuewifi.authentication.R;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

public class MainActivity extends SherlockActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button login = (Button) findViewById(R.id.button_login);
        Button logout = (Button) findViewById(R.id.button_logout);
        
        MyOnClickListener myClick = new MyOnClickListener();
        
        login.setOnClickListener(myClick);
        logout.setOnClickListener(myClick);

    	EditText user = (EditText) findViewById(R.id.editText_account);
    	EditText pass = (EditText) findViewById(R.id.editText_password);
    	CheckBox remember_info = (CheckBox) findViewById(R.id.checkBox_record_infomation);
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	remember_info.setChecked(settings.getBoolean(GlobalValue.REMEMBER, false));
    	user.setText(settings.getString(GlobalValue.USER, ""));
    	pass.setText(settings.getString(GlobalValue.PASSWORD, ""));
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}



	@Override
	protected void onPause() {
		super.onPause();
    	EditText user = (EditText) findViewById(R.id.editText_account);
    	EditText pass = (EditText) findViewById(R.id.editText_password);
    	
    	CheckBox remember_info = (CheckBox) findViewById(R.id.checkBox_record_infomation);
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	SharedPreferences.Editor PE = settings.edit();
    	if(remember_info.isChecked()){
	        PE.putString(GlobalValue.USER, user.getText().toString().trim());
	        PE.putString(GlobalValue.PASSWORD, pass.getText().toString().trim());
    	} else {
	        PE.putString(GlobalValue.USER, "");
	        PE.putString(GlobalValue.PASSWORD, "");
    	}
        PE.putBoolean(GlobalValue.REMEMBER, remember_info.isChecked());
        PE.commit();
	}

	private boolean isWifiEnable(){
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    	return manager.isWifiEnabled();
    }
    
    private boolean isConnected(){
    	ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo info = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    	//return info.isConnected() && manager.getConnectionInfo().getSSID().equalsIgnoreCase(GlobalValue.NCUE_SSID);
        return info.isConnected();
    }
    
    private void openSetting(){
		Dialog dialog;
		AlertDialog.Builder builder;
    	builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.open_wifi));
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setPositiveButton(getResources().getString(R.string.open_wifi_confirm), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		});
		builder.setNegativeButton(getResources().getString(R.string.open_wifi_cancel), null);
		
		dialog = builder.create();
		dialog.show();
    }
    
    class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			EditText user = (EditText) findViewById(R.id.editText_account);
			EditText password = (EditText) findViewById(R.id.editText_password);
			String userText = user.getText().toString().trim();
			String passText = password.getText().toString().trim();
			if(userText.equals("")){
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.em_user), Toast.LENGTH_SHORT).show();
				return;
			} else if(passText.equals("")) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.em_password), Toast.LENGTH_SHORT).show();
				return;
			} else if(!isWifiEnable()){
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.em_wifi_enable), Toast.LENGTH_SHORT).show();
				openSetting();
				return;
			} 
			
			else if(!isConnected()){
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.em_wifi_connect), Toast.LENGTH_SHORT).show();
				return;
			}
			
			Intent it = new Intent(MainActivity.this, AuthService.class);
			if(v.getId() == R.id.button_login){
				it.putExtra(GlobalValue.USER, userText);
				it.putExtra(GlobalValue.PASSWORD, passText);
				it.putExtra(GlobalValue.BOOL_FLAG, true);
				startService(it);
			} else if(v.getId() == R.id.button_logout) {
				it.putExtra(GlobalValue.BOOL_FLAG, false);
				startService(it);
			}
		}
    }
}
