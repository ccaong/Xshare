package com.ccaong.xshare.ui.group.join;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.ccaong.xshare.App;
import com.ccaong.xshare.base.BaseTransfer;
import com.ccaong.xshare.base.common.Constant;
import com.ccaong.xshare.base.viewmodel.BaseViewModel;
import com.ccaong.xshare.manager.ThreadManager;
import com.ccaong.xshare.manager.WifiMgr;
import com.ccaong.xshare.util.LogUtils;
import com.ccaong.xshare.util.NetUtils;
import com.ccaong.xshare.util.StringUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * @author : devel
 * @date : 2020/4/14 13:39
 * @desc :
 */
public class JoinGroupViewModel extends BaseViewModel {

    private ThreadManager.ThreadPool threadPool;

    /**
     * 关闭的socket的子线程
     */
    private CloseSocketRunnable closeSocketRunnable = new CloseSocketRunnable();

    /**
     * 附近的wifi列表
     */
    private List<ScanResult> mScanList;

    private MutableLiveData<List<ScanResult>> mScanResultList;
    private MutableLiveData<String> mConnectSuccess;

    public JoinGroupViewModel() {
        threadPool = ThreadManager.getThreadPool();
        mScanList = new ArrayList<>();
        mScanResultList = new MutableLiveData<>();
        mConnectSuccess = new MutableLiveData<>();
    }

    /**
     * 扫描到的符合条件的wifi
     *
     * @return
     */
    public LiveData<List<ScanResult>> getScanList() {
        return mScanResultList;
    }

    /**
     * 获取到连接状态
     *
     * @return
     */
    public LiveData<String> getConnectSuccess() {
        return mConnectSuccess;
    }


    public void init() {
        //开启wifi
        if (!WifiMgr.getInstance().isWifiEnable()) {
            //wifi未打开的情况
            WifiMgr.getInstance().openWifi();
        }
        threadPool.execute(closeSocketRunnable);
    }

    /**
     * 扫描wifi信息
     */
    public void scanWifi() {
        WifiMgr.getInstance().startScan();
        //获取当前所有扫描到的wifi
        mScanList = WifiMgr.getInstance().getScanResultList();
        //获取没有密码的热点
        filterWifiList(mScanList);
    }

    /**
     * 筛选符合条件的WiFi列表
     */
    private void filterWifiList(List<ScanResult> scanResultList) {

        String NO_PASSWORD = "[ESS]";
        String NO_PASSWORD_WPS = "[WPS][ESS]";

        if (scanResultList == null || scanResultList.size() == 0) {
            mScanResultList.postValue(scanResultList);
            return;
        }
        List<ScanResult> resultList = new ArrayList<>();
        for (ScanResult scanResult : scanResultList) {
            boolean hasPwd = scanResult.capabilities != null && scanResult.capabilities.equals(NO_PASSWORD)
                    || scanResult.capabilities != null && scanResult.capabilities.equals(NO_PASSWORD_WPS);
            if (hasPwd) {
                resultList.add(scanResult);
            }
        }
        mScanResultList.postValue(resultList);
    }


    /**
     * 连接Wifi
     */
    public void connectWiFi(ScanResult wifiInfo) {

        WifiMgr.getInstance().openWifi();

        //加入到指定的wifi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //系统版本大于6.0，需要额外的方法加入到指定的wifi
            wifiConnect(wifiInfo.SSID);
        } else {
            //直接加入指定的wifi
            WifiMgr.getInstance().addNetwork(WifiMgr.createWifiCfg(wifiInfo.SSID, null, WifiMgr.WIFICIPHER_NOPASS));
        }
        //在线程池中开启创建发送消息到群组创建方的服务
        threadPool.execute(createSendMsgToServerRunnable(WifiMgr.getInstance().getIpAddressFromHotspot()));
    }


    /**
     * android 6.0连接到指定的wifi
     *
     * @param ssid
     */
    private void wifiConnect(String ssid) {
        // 连接到外网
        WifiConfiguration mWifiConfiguration;
        WifiManager mWifiManager = (WifiManager) App.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (mWifiManager == null) {
            // TODO: 2020/4/16 WiFi 连接失败
            mConnectSuccess.postValue("-1");
            return;
        }
        //检测指定SSID的WifiConfiguration 是否存在
        WifiConfiguration tempConfig = isExists(ssid, mWifiManager);
        if (tempConfig == null) {
            //创建一个新的WifiConfiguration ，CreateWifiInfo()需要自己实现
            mWifiConfiguration = WifiMgr.createWifiCfg(ssid, null, WifiMgr.WIFICIPHER_NOPASS);
            int wcgID = mWifiManager.addNetwork(mWifiConfiguration);
            boolean connectSuccess = mWifiManager.enableNetwork(wcgID, true);
        } else {
            //发现指定WiFi，并且这个WiFi以前连接成功过
            mWifiConfiguration = tempConfig;
            boolean connectSuccess = mWifiManager.enableNetwork(mWifiConfiguration.networkId, true);
        }
    }

    /**
     * 判断曾经连接过得WiFi中是否存在指定SSID的WifiConfiguration
     *
     * @param SSID
     * @return WifiConfiguration
     */
    private WifiConfiguration isExists(String SSID, WifiManager mWifiManager) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 创建发送UDP消息到 创建group 的服务线程
     *
     * @param serverIP
     */
    private Runnable createSendMsgToServerRunnable(final String serverIP) {

        return () -> {
            try {
                closeSocket();
                LogUtils.e("ScanWiFiAc", "step2");
                startFileSenderServer(serverIP, Constant.DEFAULT_SERVER_COM_PORT);
            } catch (Exception e) {
                LogUtils.e("ScanWiFiAc", "出现异常" + e.toString());
                mConnectSuccess.postValue("-1");
                e.printStackTrace();
            }
        };
    }


    /**
     * 开启 文件发送方 通信服务 (必须在子线程执行)
     *
     * @param targetIpAddr
     * @param serverPort
     * @throws Exception
     */
    private DatagramSocket mDatagramSocket;

    private void startFileSenderServer(String targetIpAddress, int serverPort) throws Exception {
        // 确保Wifi连接上之后获取得到IP地址
        LogUtils.e("ScanWiFiAc", "进入startFileSenderServer");
        int count = 0;
        while (targetIpAddress.equals(Constant.DEFAULT_UNKOWN_IP) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(1000);
            targetIpAddress = WifiMgr.getInstance().getIpAddressFromHotspot();
            count++;
        }

        // 即使获取到连接的热点wifi的IP地址也是无法连接网络
        count = 0;
        while (!NetUtils.pingIpAddress(targetIpAddress) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(500);
            count++;
        }
        /*
         * TODO java.net.BindException: bind failed: EADDRINUSE (Address already in use)
         * because 传输界面返回没有正常结束或者关闭
         */
        LogUtils.e("ScanWiFiAc", "new DatagramSocket");
        mDatagramSocket = new DatagramSocket(serverPort);
        LogUtils.e("ScanWiFiAc", "new DatagramSocket成功");
        byte[] receiveData = new byte[1024];
        byte[] sendData = null;
        InetAddress ipAddress = InetAddress.getByName(targetIpAddress);

        //获取手机的唯一编码
//        TelephonyManager tm = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
//        String DEVICE_ID = tm.getDeviceId();
        //获取手机的设备id
        String ssid = (StringUtil.isStringEmpty(android.os.Build.DEVICE) ? Constant.DEFAULT_SSID : android.os.Build.DEVICE);
        //将设备id和手机的唯一编码拼接成一个唯一的id
//        String uniqueId = ssid + DEVICE_ID.substring(10, 14);

        String myIp = WifiMgr.getInstance().getCurrentIpAddress();


        //将信息发送给服务端
        sendData = (Constant.MSG_CREATE_GROUP_INIT + "" + myIp).getBytes(BaseTransfer.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, serverPort);
        mDatagramSocket.send(sendPacket);
        LogUtils.e("ScanWiFiAc", "step3");

        boolean flag = true;
        //2.接收服务端的反馈
        while (flag) {
            LogUtils.e("ScanWiFiAc", "等待接收消息");
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            mDatagramSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), BaseTransfer.UTF_8).trim();
            LogUtils.e("ScanWiFiAc", "收到的消息" + response);
            if (response.startsWith(Constant.MSG_JOIN_GROUP_NIT)) {

                LogUtils.e("ScanWiFiAc", "接收到正确的消息");

                //接收字符串，转换字符串，转换成ip，添加到列表中
//                parseIplist(response);

                //接收到服务端的反馈，发送广播，通知客户端进入到shareActivity中
                //关闭socket
                threadPool.execute(closeSocketRunnable);
                flag = false;
                //连接成功
                mConnectSuccess.postValue("1");
            }
        }
    }


    /**
     * 在子线程中关闭socket
     */
    class CloseSocketRunnable implements Runnable {
        @Override
        public void run() {
            closeSocket();
        }
    }

    /**
     * 关闭UDP Socket 流
     */
    private void closeSocket() {
        if (mDatagramSocket != null) {
            mDatagramSocket.disconnect();
            mDatagramSocket.close();
            mDatagramSocket = null;
            Log.e("mDatagramSocket", "mDatagramSocket已经被关闭");
        }
    }
}
