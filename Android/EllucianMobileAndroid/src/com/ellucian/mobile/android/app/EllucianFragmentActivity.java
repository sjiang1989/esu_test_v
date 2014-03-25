package com.ellucian.mobile.android.app;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.client.MobileClient;
import com.ellucian.mobile.android.client.services.AuthenticateUserIntentService;
import com.ellucian.mobile.android.client.services.ConfigurationUpdateService;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public abstract class EllucianFragmentActivity extends FragmentActivity implements DrawerLayoutActivity {
	public String moduleId;
	public String moduleName;
	public String requestUrl;
	private GoogleAnalytics gaInstance;
	private Tracker gaTracker1;
	private Tracker gaTracker2;
	private MainAuthenticationReceiver mainAuthenticationReceiver;
	private ConfigurationUpdateReceiver configReceiver;
	private SendToSelectionReceiver resetReceiver;
	private OutdatedReceiver outdatedReceiver;
	private UnauthenticatedUserReceiver unauthenticatedUserReceiver;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        configureGoogleAnalytics();
        
        Intent incomingIntent = getIntent();
        if (incomingIntent.hasExtra(Extra.MODULE_ID)) {
        	moduleId = incomingIntent.getStringExtra(Extra.MODULE_ID);
        	Log.d("Activity", "Activity moduleId set to: " + moduleId);
        } 
               
        if (incomingIntent.hasExtra(Extra.MODULE_NAME)) {
        	moduleName = incomingIntent.getStringExtra(Extra.MODULE_NAME);
        	Log.d("Activity", "Activity moduleId set to: " + moduleId);
        } 
        
        if (incomingIntent.hasExtra(Extra.REQUEST_URL))	{
        	requestUrl = incomingIntent.getStringExtra(Extra.REQUEST_URL);
        	Log.d("Activity", "Activity requestUrl set to: " + requestUrl);
        } else {
        	requestUrl = "";
        }
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getActionBar();
		setProgressBarIndeterminateVisibility(false);
        
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		
		lbm.unregisterReceiver(mainAuthenticationReceiver);
		lbm.unregisterReceiver(configReceiver);
		lbm.unregisterReceiver(resetReceiver);
		lbm.unregisterReceiver(outdatedReceiver);
		lbm.unregisterReceiver(unauthenticatedUserReceiver);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		String tag = getClass().getName();
		
		SharedPreferences preferences = getSharedPreferences(Utils.CONFIGURATION, MODE_PRIVATE);
		String configUrl = preferences.getString(Utils.CONFIGURATION_URL, null);
		
		long lastUpdate = preferences.getLong(Utils.CONFIGURATION_LAST_UPDATE, 0);
		Log.d(tag, "last update time: " + lastUpdate);
		
		if(lastUpdate != 0 && (lastUpdate + EllucianApplication.MILLISECONDS_PER_DAY) < System.currentTimeMillis()) {
			Log.d(tag, "24 hours past since last update, updating configuration");
			Intent intent = new Intent(this, ConfigurationUpdateService.class);
			intent.putExtra(Extra.CONFIG_URL, configUrl);			
			intent.putExtra(ConfigurationUpdateService.REFRESH, true);	
			startService(intent);
		}
		
		//notifications
		if (getEllucianApp().isUserAuthenticated()) {
			if (System.currentTimeMillis() > getEllucianApp().getLastNotificationsCheck() + EllucianApplication.DEFAULT_NOTIFICATIONS_REFRESH) {
				Log.d("MainActivity.onStart", "startingNotifications");
				getEllucianApp().startNotifications();
			}
		}
		
		// call registerWithGcmIfNeeded often - it checks criteria to see if it needs to register or re-register
		getEllucianApp().registerWithGcmIfNeeded();

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		
		configReceiver = new ConfigurationUpdateReceiver(this);
		lbm.registerReceiver(configReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_SUCCESS));
		
		resetReceiver = new SendToSelectionReceiver(this);
		lbm.registerReceiver(resetReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_SEND_TO_SELECTION));
		
		outdatedReceiver = new OutdatedReceiver(this);
		lbm.registerReceiver(outdatedReceiver, new IntentFilter(ConfigurationUpdateService.ACTION_OUTDATED));
		
		mainAuthenticationReceiver = new MainAuthenticationReceiver(this);
		lbm.registerReceiver(mainAuthenticationReceiver, new IntentFilter(AuthenticateUserIntentService.ACTION_UPDATE_MAIN));
		
		unauthenticatedUserReceiver = new UnauthenticatedUserReceiver(this);
		lbm.registerReceiver(unauthenticatedUserReceiver, new IntentFilter(MobileClient.ACTION_UNAUTHENTICATED_USER));
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		configureActionBar();
	}
	
    @Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		configureActionBar();
	}

	public void setContentView(int layoutResId) {
    	super.setContentView(layoutResId);
    	configureActionBar();
    }
    
    public void configureActionBar() {
    	ActionBar bar = getActionBar();
	    
	    int primaryColor = Utils.getPrimaryColor(this);
	    int headerTextColor = Utils.getHeaderTextColor(this);
	    // set 2 background drawables on the ActionBar element
	    Resources r = getResources();
    	Drawable[] layers = new Drawable[2];
    	layers[0] = new ColorDrawable(primaryColor);
    	layers[1] = r.getDrawable(R.drawable.ab_stacked_divider);
    	LayerDrawable layerDrawable = new LayerDrawable(layers);
    	bar.setBackgroundDrawable(layerDrawable);
    	bar.setSplitBackgroundDrawable(new ColorDrawable(primaryColor));
    	bar.setStackedBackgroundDrawable(new ColorDrawable(primaryColor));
    	
    	int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
    	TextView title = (TextView)findViewById(titleId);
    	if(title != null) {
    		title.setTextColor(headerTextColor);
    	}
    	if(Utils.hasDefaultMenuIcon(this)) {
    		bar.setIcon(getResources().getDrawable(R.drawable.default_home_icon));
    	} else {
    		Drawable menuIcon = Utils.getMenuIcon(this);
    		if (menuIcon != null) {
    			bar.setIcon(menuIcon);
    		} else {
    			bar.setIcon(getResources().getDrawable(R.drawable.default_home_icon));
    		}
    	}
    }

    public EllucianApplication getEllucianApp() {
        return (EllucianApplication )this.getApplication();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        getEllucianApp().touch();
    }
    
    /**
     * Create the tracker objects for Google Analytics
     */
	private void configureGoogleAnalytics() {
		gaInstance = GoogleAnalytics.getInstance(this);
        gaInstance.getLogger().setLogLevel(LogLevel.VERBOSE); 
        String trackerId1 = Utils.getStringFromPreferences(this, Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER1, null);
        String trackerId2 = Utils.getStringFromPreferences(this, Utils.GOOGLE_ANALYTICS, Utils.GOOGLE_ANALYTICS_TRACKER2, null);
        if(trackerId1 != null) {
			gaTracker1 = gaInstance.getTracker(trackerId1);
			UncaughtExceptionHandler handler = new ExceptionReporter(
					gaTracker1, GAServiceManager.getInstance(),
					Thread.getDefaultUncaughtExceptionHandler(), this);
			Thread.setDefaultUncaughtExceptionHandler(handler);
        }
        if(trackerId2 != null) {
        	gaTracker2 = gaInstance.getTracker(trackerId2);
        }
	}
    
    /**
     * Send event to google analytics
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEvent(String category, String action, String label, Long value, String moduleName) {
    	sendEventToTracker1(category, action, label, value, moduleName);
    	sendEventToTracker2(category, action, label, value, moduleName);
    }
    
    /**
     * Send event to google analytics
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker1(String category, String action, String label, Long value, String moduleName) {
    	if(gaTracker1 != null) {
    		MapBuilder mb = MapBuilder.createEvent(category, action, label, value);
    		String configurationName = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		if(moduleName != null) mb.set(Fields.customDimension(2), moduleName);
    		gaTracker1.send(mb.build());
    	}
    }
    
    
    /**
     * Send event to google analytics
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker2(String category, String action, String label, Long value, String moduleName) {
    	if(gaTracker2 != null) {
    		MapBuilder mb = MapBuilder.createEvent(category, action, label, value);
    		String configurationName = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		if(moduleName != null) mb.set(Fields.customDimension(2), moduleName);
    		gaTracker2.send(mb.build());
    	}
    }
    
    /**
     * Send view to google analytics
     * @param appScreen
     */
    public void sendView(String appScreen, String moduleName) {
    	sendViewToTracker1(appScreen, moduleName);
    	sendViewToTracker2(appScreen, moduleName);
    }
    
    /**
     * Send view to google analytics for just tracker 1
     * @param appScreen
     */
    public void sendViewToTracker1(String appScreen, String moduleName) {
    	if(gaTracker1 != null) {
    		MapBuilder mb = MapBuilder.createAppView().set(Fields.SCREEN_NAME, appScreen);
    		String configurationName = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		if(moduleName != null) mb.set(Fields.customDimension(2), moduleName);
			gaTracker1.send(mb.set(Fields.SCREEN_NAME, appScreen).build());
    	}
    }
    
    /**
     * Send view to google analytics for just tracker 2
     * @param appScreen
     */
    public void sendViewToTracker2(String appScreen, String moduleName) {
    	if(gaTracker2 != null) {
    		MapBuilder mb = MapBuilder.createAppView().set(Fields.SCREEN_NAME, appScreen);
    		String configurationName = Utils.getStringFromPreferences(this, Utils.CONFIGURATION, Utils.CONFIGURATION_NAME, null);
    		mb.set(Fields.customDimension(1), configurationName);
    		if(moduleName != null) mb.set(Fields.customDimension(2), moduleName);
			gaTracker2.send(mb.build());
    	}
    }
    
	/**
	 * Called to process touch screen events. At the very least your
	 * implementation must call superDispatchTouchEvent(MotionEvent) to do the
	 * standard touch screen processing. Overriding to capture EditText
	 * objects. If the user touches outside the EditText, dismiss the keyboard
	 * 
	 * @param event	The touch screen event.
	 * @return boolean Return true if this event was consumed.
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		View v = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (v instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			Log.d("Activity",
					"Touch event " + event.getRawX() + "," + event.getRawY()
							+ " " + x + "," + y + " rect " + w.getLeft() + ","
							+ w.getTop() + "," + w.getRight() + ","
							+ w.getBottom() + " coords " + scrcoords[0] + ","
							+ scrcoords[1]);
			if (event.getAction() == MotionEvent.ACTION_UP
					&& (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w
							.getBottom())) {

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
						.getWindowToken(), 0);
			}
		}
		return ret;
	}
}

