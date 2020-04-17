package com.ccaong.xshare.ui.group.join;

import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.ccaong.xshare.BR;
import com.ccaong.xshare.R;
import com.ccaong.xshare.base.BaseFragment;
import com.ccaong.xshare.databinding.GroupFragmentJoinBinding;
import com.ccaong.xshare.ui.adapter.CommonAdapter;

import java.lang.ref.WeakReference;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * @author : devel
 * @date : 2020/4/14 13:37
 * @desc :
 */
public class JoinGroupFragment extends BaseFragment<GroupFragmentJoinBinding, JoinGroupViewModel> {

    private static final int MSG_SCAN_WIFI = 0X99;

    private CommonAdapter<ScanResult> commonAdapter;
    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected int getLayoutResId() {
        return R.layout.group_fragment_join;
    }

    @Override
    protected void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(JoinGroupViewModel.class);
    }

    @Override
    protected void bindViewModel() {

    }

    @Override
    protected void init() {
        mDataBinding.scanView.startScan();
        initRecyclerView();

        mViewModel.init();
        scanWifi();
    }

    private void scanWifi() {
        mViewModel.scanWifi();
        mHandler.sendEmptyMessageDelayed(MSG_SCAN_WIFI, 1000);
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        commonAdapter = new CommonAdapter<ScanResult>(R.layout.group_item_wifi_info, BR.wifiInfo) {
            @Override
            public void addListener(View root, ScanResult itemData, int position) {
                super.addListener(root, itemData, position);
                root.findViewById(R.id.item).setOnClickListener(v -> {
                    //连接wifi
                    mHandler.removeMessages(MSG_SCAN_WIFI);
                    mViewModel.connectWiFi(itemData);
                });
            }
        };
        mDataBinding.recyclerView.setAdapter(commonAdapter);
        mDataBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected void initDataChange() {
        super.initDataChange();
        mViewModel.getScanList().observe(this, scanResults -> {
            if (commonAdapter != null) {
                commonAdapter.onItemDataChanged(scanResults);
            }
        });

        mViewModel.getConnectSuccess().observe(this, s -> {
            if ("1".equals(s)) {
                Toast.makeText(getContext(), "连接成功", Toast.LENGTH_SHORT).show();
            }
            if ("-1".equals(s)) {
                Toast.makeText(getContext(), "连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDataBinding.scanView.stopScan();
        mHandler.removeCallbacksAndMessages(null);
    }

    private static class MyHandler extends Handler {

        private WeakReference<JoinGroupFragment> reference;

        MyHandler(JoinGroupFragment context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SCAN_WIFI) {
                JoinGroupFragment fragment = reference.get();
                if (fragment != null) {
                    fragment.scanWifi();
                }
            }
        }
    }
}
