package com.ccaong.xshare.ui.group.create;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.ccaong.xshare.App;
import com.ccaong.xshare.R;
import com.ccaong.xshare.base.BaseFragment;
import com.ccaong.xshare.databinding.GroupFragmentCreateBinding;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

/**
 * @author : devel
 * @date : 2020/4/13 14:25
 * @desc :
 */
public class CreateGroupFragment extends BaseFragment<GroupFragmentCreateBinding,CreateGroupViewModel> {

    private static final int REQUEST_CODE_WRITE_SETTINGS = 7879;

    @Override
    protected int getLayoutResId() {
        return R.layout.group_fragment_create;
    }

    @Override
    protected void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(CreateGroupViewModel.class);
    }

    @Override
    protected void bindViewModel() {

    }

    @Override
    protected void init() {

        boolean permission = ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;

        if (permission) {
            AndPermission.with(this)
                    .runtime().permission(Permission.READ_PHONE_STATE)
                    .onGranted(p -> mViewModel.init())
                    .onDenied(p -> Toast.makeText(getContext(), "23333", Toast.LENGTH_SHORT).show())
                    .start();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_SETTINGS}, REQUEST_CODE_WRITE_SETTINGS);
            }
        }
        initWave();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Settings.System.canWrite方法检测授权结果
                if (Settings.System.canWrite(getContext())) {
                    AndPermission.with(this)
                            .runtime().permission(Permission.READ_PHONE_STATE)
                            .onGranted(p -> mViewModel.init())
                            .onDenied(p -> Toast.makeText(getContext(), "23333", Toast.LENGTH_SHORT).show())
                            .start();
                } else {
                    Toast.makeText(getContext(), "23333", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initWave() {
        mDataBinding.wave.start();
        mDataBinding.wave.setImageRadius(100);
    }

    @Override
    protected void initDataChange() {
        super.initDataChange();
        mViewModel.getApStatus().observe(this, s -> {
            if ("1".equals(s)) {
                // TODO: 2020/4/14 连接成功，跳转下一个界面
                mViewModel.finishFragment();
                Toast.makeText(getContext(), "连接成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.finishFragment();
    }
}
