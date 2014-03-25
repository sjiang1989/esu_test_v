package com.ellucian.mobile.android.app;

import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.util.ConfigurationProperties;

import android.app.Fragment;

public class EllucianFragment extends Fragment {
	
	public EllucianActivity getEllucianActivity() {
		return (EllucianActivity)getActivity();
	}
	
	public ConfigurationProperties getConfigurationProperties() {
        return getEllucianActivity().getConfigurationProperties();
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
    	 getEllucianActivity().sendEvent(category, action, label, value, moduleName);
    }
    
    /**
     * Send event to google analytics for just tracker 1
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker1(String category, String action, String label, Long value, String moduleName) {
    	 getEllucianActivity().sendEventToTracker1(category, action, label, value, moduleName);
    }
    
    /**
     * Send event to google analytics for just tracker 2
     * @param category
     * @param action
     * @param label
     * @param value
     * @param moduleName
     */
    public void sendEventToTracker2(String category, String action, String label, Long value, String moduleName) {
    	 getEllucianActivity().sendEventToTracker2(category, action, label, value, moduleName);
    }

    /**
     * Send view to google analytics
     * @param appScreen
     * @param moduleName
     */
    public void sendView(String appScreen, String moduleName) {
    	getEllucianActivity().sendView(appScreen, moduleName);
    }
    
    /**
     * Send view to google analytics for just tracker 1
     * @param appScreen
     * @param moduleName
     */
    public void sendViewToTracker1(String appScreen, String moduleName) {
    	getEllucianActivity().sendViewToTracker1(appScreen, moduleName);
    }
    
    /**
     * Send view to google analytics for just tracker 2
     * @param appScreen
     * @param moduleName
     */
    public void sendViewToTracker2(String appScreen, String moduleName) {
    	getEllucianActivity().sendViewToTracker2(appScreen, moduleName);
    }
}