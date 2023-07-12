package com.albresky.splayer.Utils;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ActivityManager {
    private List<Activity> allActivities = new ArrayList<>();


    private static WeakReference<Activity> activityWeakReference;


    private static Object activityUpdateLock = new Object();


    public void addActivity(Activity activity) {
        if (activity != null) {
            allActivities.add(activity);
        }
    }


    public void removeActivity(Activity activity) {
        if (activity != null) {
            allActivities.remove(activity);
        }
    }


    public void finishAll() {
        for (Activity activity : allActivities) {
            activity.finish();
        }

    }

    public static Activity getCurrentActivity() {
        Activity currentActivity = null;
        synchronized (activityUpdateLock) {
            if (activityWeakReference != null) {
                currentActivity = activityWeakReference.get();
            }
        }
        return currentActivity;
    }


    public static void setCurrentActivity(Activity activity) {
        synchronized (activityUpdateLock) {
            activityWeakReference = new WeakReference<Activity>(activity);
        }

    }
}
