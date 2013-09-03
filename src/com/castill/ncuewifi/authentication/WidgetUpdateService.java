package com.castill.ncuewifi.authentication;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;

public class WidgetUpdateService extends IntentService {
	
	public WidgetUpdateService() {
		super("WidgetUpdateService");
	}
	
	public WidgetUpdateService(String name) {
		super("WidgetUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean isLogin;
		if(!isWifiEnable() || !isConnected()) {
			isLogin = false;
		} else {
			try{
				Document doc = Jsoup.connect("http://www.google.com").get();
				Elements element = doc.select("title");
				String title = element.get(0).html().toString();
				if(title.equalsIgnoreCase("Google")){
					isLogin = true;
				} else {
					isLogin = false;
				}
			} catch (Exception e){
				isLogin = false;
			}
		}

		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		int[] ids = manager.getAppWidgetIds(new ComponentName(getApplicationContext(), AuthWidgetProvider.class));
		RemoteViews views = null;
		Intent it = new Intent(getApplicationContext(), AuthService.class);
        it.putExtra(GlobalValue.WIDGET_CALL, true);
        
		if(isLogin) {
			views = new RemoteViews(getApplication().getPackageName(), R.layout.auth_widget_logout_layout);
		} else {
			views = new RemoteViews(getApplication().getPackageName(), R.layout.auth_widget_login_layout);
		}
		
		PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, it, PendingIntent.FLAG_CANCEL_CURRENT);
		views.setOnClickPendingIntent(R.id.imagebutton_widget_button, pendingIntent);
		
		for(int i =0 ; i < ids.length ; i++) {
			manager.updateAppWidget(ids[i], views);
		}
	}
	
	private boolean isWifiEnable(){
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    	return manager.isWifiEnabled();
    }
    
    private boolean isConnected(){
    	ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo info = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    	return info.isConnected() && manager.getConnectionInfo().getSSID().equalsIgnoreCase("NCUE");
    }
}
