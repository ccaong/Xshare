package com.ccaong.xshare.ui.home;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ccaong.xshare.R;
import com.ccaong.xshare.base.BaseFragment;
import com.ccaong.xshare.databinding.FragmentHomeBinding;

import androidx.lifecycle.ViewModelProvider;

/**
 * @author devel
 */
public class HomeFragment extends BaseFragment<FragmentHomeBinding, HomeViewModel> {


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    protected void bindViewModel() {

    }

    @Override
    protected void init() {
        startAnimation();
    }

    /**
     * 按钮动画
     */
    private void startAnimation() {
        Animation animation;
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.btn_share);
        animation.setDuration(500);
        animation.setRepeatCount(1);
        mDataBinding.circleBigImageView.startAnimation(animation);
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.btn_share);
        animation.setDuration(450);
        animation.setRepeatCount(1);
        mDataBinding.circleMidImageView.startAnimation(animation);
    }

}