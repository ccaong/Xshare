package com.ccaong.xshare.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ccaong.xshare.R;
import com.ccaong.xshare.base.viewmodel.BaseViewModel;
import com.ccaong.xshare.databinding.ActivityBaseBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


/**
 * Activity的基类
 *
 * @param <DB> data binding
 * @param <VM> view model
 * @author devel
 */
public abstract class BaseActivity<DB extends ViewDataBinding, VM extends BaseViewModel>
        extends AppCompatActivity {

    protected DB mDataBinding;

    protected VM mViewModel;

    private ActivityBaseBinding mActivityBaseBinding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleIntent(getIntent());

        if (isNoActionBar()) {
            setNoActionBar();
        }

        mActivityBaseBinding = DataBindingUtil.setContentView(this, R.layout.activity_base);
        mDataBinding = DataBindingUtil.inflate(getLayoutInflater(), getLayoutResId(),
                mActivityBaseBinding.flContentContainer, true);

        initViewModel();
        bindViewModel();

        mDataBinding.setLifecycleOwner(this);

        init();

        // ViewModel订阅生命周期事件
        if (mViewModel != null) {
            getLifecycle().addObserver(mViewModel);
        }
    }

    /**
     * 设置沉浸式状态栏
     */
    private void setNoActionBar() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }


    private void removeLoadView() {
        int childCount = mActivityBaseBinding.flContentContainer.getChildCount();
        if (childCount > 1) {
            mActivityBaseBinding.flContentContainer.removeViews(1, childCount - 1);
        }
    }


    /**
     * 处理参数
     *
     * @param intent 参数容器
     */
    protected void handleIntent(Intent intent) {

    }

    /**
     * 是否为沉浸模式
     *
     * @return true表示支持，false表示不支持
     */
    protected boolean isNoActionBar() {
        return false;
    }


    /**
     * 是否支持页面加载。默认不支持
     *
     * @return true表示支持，false表示不支持
     */
    protected boolean isSupportLoad() {
        return false;
    }

    /**
     * 获取当前页面的布局资源ID
     *
     * @return 布局资源ID
     */
    protected abstract int getLayoutResId();

    /**
     * 初始化ViewModel
     */
    protected abstract void initViewModel();

    /**
     * 绑定ViewModel
     */
    protected abstract void bindViewModel();

    /**
     * 初始化
     */
    protected abstract void init();


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            Log.e("记录操作", "dispatchTouchEvent");
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
