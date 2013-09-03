package com.castill.ncuewifi.authentication;

import java.io.IOException;
import java.util.Date;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.castill.ncuewifi.authentication.R;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AuthService extends IntentService {

	private Handler handler = new Handler();
	
	public static final int LO_SUCCESS = 0x0C8;
	public static final int LOGIN_FAILED = 0x001;
	public static final int LO_EXCEPTION = 0x010;
	public static final int LO_ALREADYLOGIN = 0x110;
	
	public AuthService(){
		super("AuthService");
	}
	
	public AuthService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		boolean bool_flag = intent.getBooleanExtra(GlobalValue.BOOL_FLAG, false);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String user = "";
		String password = "";
		boolean widget_call = intent.getBooleanExtra(GlobalValue.WIDGET_CALL, false);

		
		if(widget_call){
			user = settings.getString(GlobalValue.USER, "");
			password = settings.getString(GlobalValue.PASSWORD, "");
			
			if(user.equals("") || password.equals("")) {
				Intent it = new Intent(getApplicationContext(), MainActivity.class);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(it);
				toastMessage(getResources().getString(R.string.information_not_enter));
				return;
			} else if(!isWifiEnable()){
				Intent it = new Intent(getBaseContext(), OpenWifiActivity.class);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				toastMessage(getResources().getString(R.string.em_wifi_enable));
				getApplication().startActivity(it);
				return;
			} 
			else 
			
			if(!isConnected()){
				toastMessage(getResources().getString(R.string.em_wifi_connect));
				return;
			}
			
			try{
				Document doc = Jsoup.connect("http://www.google.com").get();
				Elements element = doc.select("title");
				String title = element.get(0).html().toString();
				if(title.equalsIgnoreCase("Google")){
					bool_flag = false;
				} else {
					bool_flag = true;
				}
			} catch (Exception e){
				bool_flag = true;
			}
		}	
		
		int status = -1;
		String resUrl = "";
		
		if(bool_flag){
			try {
				try {
					toastMessage(getResources().getString(R.string.login_start));
				} catch (NotFoundException notFound) {
					
				}
				if(!widget_call){
					user = intent.getStringExtra(GlobalValue.USER).toString();
					password = intent.getStringExtra(GlobalValue.PASSWORD).toString();
				}
				
				Response res = Jsoup.connect("http://120.107.207.254/auth/index.html/u")
							   .data("user", user, "password", password, "cmd", "authenticate")
							   .timeout(0)
							   .followRedirects(true)
							   .method(Method.POST)
							   .execute();
				resUrl = res.url().toString();
				status = res.statusCode();
			} catch (IOException alreadyLogin) {
				status = LOGIN_FAILED;
			} catch (Exception exception) {
				status = LO_EXCEPTION;
			}
		} else {
			try {
				toastMessage(getResources().getString(R.string.logout_start));
				status =  Jsoup.connect("http://120.107.207.254/cgi-bin/login?cmd=logout").execute().statusCode();
			} catch (Exception exception) {
				status = LO_EXCEPTION;
			}
		}
		
		SharedPreferences.Editor PE = settings.edit();
		
		switch(status){
		case LO_SUCCESS:
			
			if(bool_flag) {
				
				if(resUrl.equals(GlobalValue.LOGIN_SUCCESS)){
					toastMessage(getResources().getString(R.string.login_success));
					PE.putLong(GlobalValue.LOGIN_TIME, new Date().getTime());
					PE.commit();
				} else if(resUrl.equals(GlobalValue.LOGIN_ERROR_INFORMATION)) {
					toastMessage(getResources().getString(R.string.login_failed));
					return;
				} else if(resUrl.equals(GlobalValue.LOGIN_NO_INFORMATION)) {
					Intent it = new Intent(getApplicationContext(), MainActivity.class);
					it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplication().startActivity(it);
					toastMessage(getResources().getString(R.string.information_not_enter));
					return;
				} else if(resUrl.equals(GlobalValue.ONLY_ONE_USER)) {
					toastMessage(getResources().getString(R.string.login_only_one_user));
					return;
				} else {
					toastMessage(getResources().getString(R.string.lo_exception));
					return;
				}
			} else {
				toastMessage(getResources().getString(R.string.logout_success));
				PE.putLong(GlobalValue.LOGOUT_TIME, new Date().getTime());
				PE.commit();
			}
			
			if(settings.getBoolean(GlobalValue.ISHAVEWIDGET, false)) {
				AppWidgetManager manager = AppWidgetManager.getInstance(this);
				int[] ids = manager.getAppWidgetIds(new ComponentName(getApplicationContext(), AuthWidgetProvider.class));
				RemoteViews views = null;
				
		        Intent it = new Intent(getApplicationContext(), AuthService.class);
		        it.putExtra(GlobalValue.WIDGET_CALL, true);
		        
				if(bool_flag) {
					views = new RemoteViews(getApplication().getPackageName(), R.layout.auth_widget_logout_layout);
				} else {
					views = new RemoteViews(getApplication().getPackageName(), R.layout.auth_widget_login_layout);
				}
				
				PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, it, PendingIntent.FLAG_CANCEL_CURRENT);
				views.setOnClickPendingIntent(R.id.imagebutton_widget_button, pendingIntent);
				for(int i = 0 ; i < ids.length ; i++) {
					manager.updateAppWidget(ids[i], views);
				}
			}
			break;
		case LO_EXCEPTION:
			toastMessage(getResources().getString(R.string.lo_exception));
			break;
		case LOGIN_FAILED:
			toastMessage(getResources().getString(R.string.already_login));
			break;
		}
		stopSelf();
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
    
    private void toastMessage(final String msg){
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
		});
    }
}
