package com.ccaong.xshare.ui.group.create;

import android.content.Context;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

import com.ccaong.xshare.App;
import com.ccaong.xshare.base.BaseTransfer;
import com.ccaong.xshare.base.common.Constant;
import com.ccaong.xshare.base.viewmodel.BaseViewModel;
import com.ccaong.xshare.manager.ApMgr;
import com.ccaong.xshare.manager.ThreadManager;
import com.ccaong.xshare.manager.WifiMgr;
import com.ccaong.xshare.receiver.WifiAPBroadcastReceiver;
import com.ccaong.xshare.util.LogUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.ccaong.xshare.util.StringUtil.isStringEmpty;

/**
 * @author : devel
 * @date : 2020/4/13 14:26
 * @desc :
 */
public class CreateGroupViewModel extends BaseViewModel{

    private WifiAPBroadcastReceiver wifiAPBroadcastReceiver;
    private MutableLiveData<String> mApStatus;

    public CreateGroupViewModel() {
        mApStatus = new MutableLiveData<>();
    }

    public LiveData<String> getApStatus() {
        return mApStatus;
    }

    /**
     * 初始化界面
     * 开启热点
     */
    public void init() {
        LogUtils.e("WaitingJoinAc", "init");
        WifiMgr.getInstance().closeWifi();
        //获取手机的唯一编码
        TelephonyManager tm = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        //获取手机的设备id
        String ssid = (isStringEmpty(android.os.Build.DEVICE) ? Constant.DEFAULT_SSID : android.os.Build.DEVICE);
        //将设备id和手机的唯一编码拼接成一个唯一的id
        String uniqueId = ssid + deviceId.substring(10, 14);

        //关闭热点和wifi
        WifiMgr.getInstance().disableWifi();
        if (ApMgr.isApOn()) {
            ApMgr.disableAp();
        }
        initWifiReceiver();
        register();

        ApMgr.configApState(uniqueId);
    }

    /**
     * 注册Wifi状态监听
     */
    private void register() {
        //监听状态改变
        IntentFilter filter = new IntentFilter(WifiAPBroadcastReceiver.ACTION_WIFI_AP_STATE_CHANGED);
        App.getContext().registerReceiver(wifiAPBroadcastReceiver, filter);
    }

    /**
     * 监听wifi(热点)状态
     */
    private void initWifiReceiver() {
        wifiAPBroadcastReceiver = new WifiAPBroadcastReceiver() {
            @Override
            public void onWifiApEnabled() {
                ThreadManager.getThreadPool().execute(() -> {
                    try {
                        LogUtils.e("WaitingJoinAc", "热点开启成功");
                        startWaitingJoinServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.e("WaitingJoinAc", "Socket创建异常" + e.getMessage());
                    }
                });
            }
        };
    }

    /**
     * 代表UDP协议的Socket,用来接收发送数据
     */
    private DatagramSocket mDatagramSocket;

    /**
     * 开启 文件接收方 通信服务 (必须在子线程执行)
     *
     * @throws Exception Socket异常
     */
    private void startWaitingJoinServer() throws Exception {

        //网络连接上，获取自身IP地址
        int count = 0;
        String localAddress = WifiMgr.getInstance().getHotspotLocalIpAddress();
        while (localAddress.equals(Constant.DEFAULT_UNKOWN_IP) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(1000);
            localAddress = WifiMgr.getInstance().getHotspotLocalIpAddress();
            count++;
        }

        mDatagramSocket = new DatagramSocket(Constant.DEFAULT_SERVER_COM_PORT);
        byte[] receiveData = new byte[1024];
        byte[] sendData = null;
        while (true) {
            LogUtils.e("WaitingJoinAc", "等待接收消息");
            //1.接收 文件发送方的消息
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            mDatagramSocket.receive(receivePacket);
            String msg = new String(receivePacket.getData()).trim();

            //获取客户端的ip
            InetAddress inetAddress = receivePacket.getAddress();
            //获取客户端的端口号
            int port = receivePacket.getPort();
            LogUtils.e("WaitingJoinAc", "收到的消息>>>" + msg);

            //判断接收到的消息
            if (msg.startsWith(Constant.MSG_CREATE_GROUP_INIT)) {
                LogUtils.e("WaitingJoinAc", "接收到正确的消息");
                LogUtils.e("WaitingJoinAc", "IP地址：" + inetAddress);
                LogUtils.e("WaitingJoinAc", "信息：" + msg);

                LogUtils.e("WaitingJoinAc", "准备发送消息");
                //将所有的客户端和服务端的信息发送给所有的客户端
                sendData = (Constant.MSG_JOIN_GROUP_NIT).getBytes(BaseTransfer.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, port);
                //这句话执行完毕后    加入方跳转
                mDatagramSocket.send(sendPacket);
                LogUtils.e("WaitingJoinAc", "消息发送完毕");

                mApStatus.postValue("1");
            }
        }
    }

    /**
     * 退出当前界面
     */
    public void finishFragment() {
        unRegister();
        ThreadManager.getThreadPool().execute(this::closeSocket);
    }

    /**
     * 关闭UDP Socket 流
     */
    private void closeSocket() {
        LogUtils.e("WaitingJoinAc", "关闭Socket");
        if (mDatagramSocket != null) {
            mDatagramSocket.disconnect();
            mDatagramSocket.close();
            mDatagramSocket = null;
        }
    }

    /**
     * 解除注册的广播
     */
    private void unRegister() {
        LogUtils.e("WaitingJoinAc", "广播解除注册");
        try {
            if (wifiAPBroadcastReceiver != null) {
                LogUtils.e("WaitingJoinAc", "广播解除注册成功");
                App.getContext().unregisterReceiver(wifiAPBroadcastReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
