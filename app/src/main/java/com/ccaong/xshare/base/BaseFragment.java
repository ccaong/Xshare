package com.ccaong.xshare.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ccaong.xshare.R;
import com.ccaong.xshare.base.viewmodel.BaseViewModel;
import com.ccaong.xshare.databinding.FragmentBaseBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;


/**
 * Fragment的基类
 *
 * @param <DB> data binding
 * @param <VM> view model
 * @author devel
 */
public abstract class BaseFragment<DB extends ViewDataBinding, VM extends BaseViewModel>
        extends Fragment {

    protected DB mDataBinding;

    protected VM mViewModel;

    private FragmentBaseBinding mFragmentBaseBinding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            handleArguments(args);
        }

        initViewModel();

        initDataChange();

        // ViewModel订阅生命周期事件
        if (mViewModel != null) {
            getLifecycle().addObserver(mViewModel);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentBaseBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_base, container, false);
        mDataBinding = DataBindingUtil.inflate(inflater, getLayoutResId(),
                mFragmentBaseBinding.flContentContainer, true);

        bindViewModel();
        mDataBinding.setLifecycleOwner(this);

        init();

        return mFragmentBaseBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // ViewModel订阅生命周期事件
        if (mViewModel != null) {
            getLifecycle().removeObserver(mViewModel);
        }
    }


    /**
     * 处理参数
     *
     * @param args 参数容器
     */
    protected void handleArguments(Bundle args) {

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
     * 监听数据变化
     */
    protected void initDataChange() {

    }

    /**
     * 绑定ViewModel
     */
    protected abstract void bindViewModel();

    /**
     * 初始化
     */
    protected abstract void init();

}
