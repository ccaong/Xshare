package com.ccaong.xshare.base.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;

/**
 * @author : devel
 * @date : 2020/4/13 16:14
 * @desc :
 */
public class BaseAndroidViewModel extends BaseViewModel {

    @SuppressLint("StaticFieldLeak")
    private Application mApplication;

    public BaseAndroidViewModel(@NonNull Application application) {
        mApplication = application;
    }

    /**
     * Return the application.
     */
    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    @NonNull
    public <T extends Application> T getApplication() {
        return (T) mApplication;
    }
}
