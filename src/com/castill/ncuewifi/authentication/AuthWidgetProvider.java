package com.castill.ncuewifi.authentication;

import com.castill.ncuewifi.authentication.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class AuthWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		
		int i;
        for(i = 0; i < appWidgetIds.length; i++) {
        	int appWidgetId = appWidgetIds[i];
	        Intent intent = new Intent(context, AuthService.class);
	        intent.putExtra(GlobalValue.WIDGET_CALL, true);
	        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	        RemoteViews views = null;
	        views = new RemoteViews(context.getPackageName(), R.layout.auth_widget_login_layout);
	        views.setOnClickPendingIntent(R.id.imagebutton_widget_button, pendingIntent);
	        appWidgetManager.updateAppWidget(appWidgetId, views);
		}
        
        context.startService(new Intent(context, WidgetUpdateService.class));
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor PE = settings.edit();
    	PE.putBoolean(GlobalValue.ISHAVEWIDGET, false);
    	PE.commit();
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor PE = settings.edit();
    	PE.putBoolean(GlobalValue.ISHAVEWIDGET, true);
    	PE.commit();
	}	
}
