package com.ccaong.xshare.ui.share;

import android.widget.Toast;

import com.ccaong.xshare.R;
import com.ccaong.xshare.base.BaseFragment;
import com.ccaong.xshare.databinding.FragmentShareBinding;
import com.ccaong.xshare.view.MapView;

/**
 * @author devel
 */
public class ShareFragment extends BaseFragment<FragmentShareBinding, ShareViewModel> {

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_share;
    }

    @Override
    protected void initViewModel() {

    }

    @Override
    protected void bindViewModel() {

    }

    @Override
    protected void init() {
        mDataBinding.mapView.setOnViewItemClickListener(name -> showPrivate());
    }

    private void showPrivate() {
        mDataBinding.mapView.setMapResId(R.raw.chinahigh34);
    }
}