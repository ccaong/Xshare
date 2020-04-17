package com.ccaong.xshare.manager;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.ccaong.xshare.App;

import java.lang.reflect.Method;

import static android.content.Context.WIFI_SERVICE;


/**
 * @author devel
 */
public class ApMgr {

    /**
     * 判断热点是否开启
     *
     * @return
     */
    public static boolean isApOn() {
        WifiManager wifimanager = (WifiManager) App.getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * 关闭热点
     */
    public static void disableAp() {
        WifiManager wifimanager = (WifiManager) App.getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, null, false);
        } catch (Throwable ignored) {

        }
    }

    public static boolean configApState() {
        WifiManager wifimanager = (WifiManager) App.getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // if WiFi is on, turn it off
            if (isApOn()) {
                wifimanager.setWifiEnabled(false);
                // if ap is on and then disable ap
                disableAp();
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 开启热点
     *
     * @param apName 热点名称
     * @return
     */
    public static boolean configApState(String apName) {
        WifiManager wifimanager = (WifiManager) App.getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            wificonfiguration = new WifiConfiguration();
            wificonfiguration.SSID = apName;
            // if WiFi is on, turn it off
            if (isApOn()) {
                wifimanager.setWifiEnabled(false);
                // if ap is on and then disable ap
                disableAp();
            }
            assert wifimanager != null;
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
