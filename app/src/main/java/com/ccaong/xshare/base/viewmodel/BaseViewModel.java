package com.ccaong.xshare.base.viewmodel;


import android.content.res.Resources;

import com.ccaong.xshare.App;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel的基类
 *
 * @author devel
 */
public abstract class BaseViewModel extends ViewModel implements DefaultLifecycleObserver {

    public Resources resources;

    public Resources getResources() {
        if (resources == null) {
            resources = App.getContext().getResources();
        }
        return resources;
    }
}
