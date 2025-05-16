package com.fhj.dns.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.fhj.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class WifiUtil {

    public static WifiUtil INSTANCE;

    static {
        INSTANCE = new WifiUtil();
    }

    public void findNetWorkForInterNet(Context context, boolean isCell,NetWorkCallbackAsync.AvailableNetworkListener listener) {

        //获取ConnectivityManager服务
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        var transPort = isCell?NetworkCapabilities.TRANSPORT_CELLULAR:NetworkCapabilities.TRANSPORT_WIFI;
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(transPort)
                .build();

        List<Integer> capabilities = new ArrayList<>();
        capabilities.add(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        List<Integer> transportTypes = new ArrayList<>();
        transportTypes.add(transPort);

        ConnectivityManager.NetworkCallback callback = getDefaultCallback(listener, capabilities, transportTypes, connectivityManager);

        //请求获取匹配networkRequest的最佳网络
        //如果没有找到networkRequest的要求的网络，还会自行去寻找合适的网络进行返回,，如果获取到的网络必须满足某项需求
        //需要再onAvailable对network进行判断
        connectivityManager.requestNetwork(networkRequest, callback);

    }

    /**
     * NetWork 的 capability 是否与指定的Capability 是否匹配  只能匹配单个
     *
     * @param connectivityManager
     * @param netWork
     * @param capability
     * @return
     */
    public boolean networkIsMatch(ConnectivityManager connectivityManager, Network netWork, int capability) {
        return connectivityManager.getNetworkCapabilities(netWork).hasCapability(capability);
    }

    /**
     * NetWork 的 capability 是否与指定的Capability 是否匹配 匹配多个
     *
     * @param connectivityManager
     * @param netWork
     * @param capabilities
     * @return
     */
    public boolean networkIsMatchList(ConnectivityManager connectivityManager, Network netWork, List<Integer> capabilities) {
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(netWork);
        boolean result = true;
        for (int a : capabilities) {
            result = result && networkCapabilities.hasCapability(a);
        }
        return result;
    }

    public boolean networkIsMatchCapabilitiesAndTransportList(ConnectivityManager connectivityManager, NetworkCapabilities allCapabilities, List<Integer> capabilities, List<Integer> tansPorts) {
        NetworkCapabilities networkCapabilities = allCapabilities;
        boolean result = true;

        if (capabilities != null) {
            for (int a : capabilities) {
                result = result && networkCapabilities.hasCapability(a);
            }
        }

        if (tansPorts != null && result) {
            for (int a : tansPorts) {
                result = result && networkCapabilities.hasTransport(a);
            }
        }

        return result;
    }

    /**
     * 获取默认的异步NetworkCallback实现
     *
     * @param listener
     * @param capabilities
     * @param connectivityManager
     * @return
     */
    private ConnectivityManager.NetworkCallback getDefaultCallback(NetWorkCallbackAsync.AvailableNetworkListener listener, List<Integer> capabilities, List<Integer> transPorts, ConnectivityManager connectivityManager) {
        ConnectivityManager.NetworkCallback callback = new NetWorkCallbackAsync(listener, new NetWorkCallbackAsync.Match() {
            @Override
            public boolean match(NetworkCapabilities capabilities1) {
                return networkIsMatchCapabilitiesAndTransportList(connectivityManager, capabilities1, capabilities, transPorts);
            }
        });
        return callback;
    }

    public String intIp2Ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 检查wifi打开
     *
     *     需要权限
     *     <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     *     <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
     *     <uses-permission android:name="android.permission.WRITE_SETTINGS" />
     *
     * @param context 同于获取wifi服务
     */
    public static boolean checkWifiOpened(Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        var enable = wifiManager.isWifiEnabled();
        Logger.INSTANCE.log("wifi " + (enable ? "开启" : "关闭"));
        return enable;
    }


    /**
     * 主线程调用
     *
     * @param consumer
     */
    public void currentWifiName(Context context, com.example.wifidemo1.Function.MyConsumer<List<LinkAddress>> consumer) {
        if (!checkWifiOpened(context)) {
            consumer.accept(null);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build();
        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (connectivityManager.getNetworkCapabilities(network).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        WifiInfo wifiInfo = (WifiInfo) connectivityManager.getNetworkCapabilities(network).getTransportInfo();
                        consumer.accept(connectivityManager.getLinkProperties(network).getLinkAddresses());
                    }
                } else {
                    consumer.accept(null);
                }
                connectivityManager.unregisterNetworkCallback(this);
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
//                consumer.accept(null);
//                connectivityManager.unregisterNetworkCallback(this);
            }

            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
            }

            @Override
            public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                super.onBlockedStatusChanged(network, blocked);
            }

        };
        connectivityManager.requestNetwork(request, callback);
    }
}
