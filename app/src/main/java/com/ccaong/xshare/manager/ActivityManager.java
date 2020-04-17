package com.ccaong.xshare.manager;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * @author : devel
 * @date : 2020/2/26 14:30
 * @desc : Activity管理类
 */
public class ActivityManager {
    private static ActivityManager sInstance = new ActivityManager();
    /**
     * 若引用当前Activity
     */
    private WeakReference<Activity> sCurrentActivityWeakRef;


    private ActivityManager() {

    }

    public static ActivityManager getInstance() {
        return sInstance;
    }

    /**
     * 获取当前的Activity
     *
     * @return
     */
    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }

    /**
     * 保存Activity
     *
     * @param activity
     */
    public void setCurrentActivity(Activity activity) {
        sCurrentActivityWeakRef = new WeakReference<Activity>(activity);
    }
}
