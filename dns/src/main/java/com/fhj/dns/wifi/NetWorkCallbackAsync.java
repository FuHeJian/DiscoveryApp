package com.fhj.dns.wifi;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public class NetWorkCallbackAsync extends ConnectivityManager.NetworkCallback {

    public AvailableNetworkListener mListener;

    private List<Integer> mCapabilities = Collections.emptyList();

    private Match mMatch;

    private boolean isUpdate = false;
    public NetWorkCallbackAsync(@NonNull AvailableNetworkListener listener, @NonNull Match match) {
        mListener = listener;
/*        if (capabilities != null && capabilities.size() > 0) {
            mCapabilities = capabilities;
        }*/
        mMatch = match;
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
    }


    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        mListener.onLost(network,this);
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        boolean matched = true;

        matched = matched && mMatch.match(networkCapabilities);

        if(mListener == null) return;
        if(isUpdate)return;
        if(matched){
            mListener.onUpdate(network);
        }else {
            mListener.onNotMatchNeedNetWork(network);
        }
        isUpdate = true;
/*        if ( mListener.mNetworkIsEmpty && matched) {
            mListener.onSuccess(network,NetWorkCallbackAsync.this);
            mListener.mNetworkIsEmpty = false;
            mListener.mNetWorkIsMatched = true;
        } else {
            if (!matched) {
                mListener.onNotMatchNeedNetWork(network);
            } else {
                mListener.onUpdate(network);
            }
        }*/
    }

    /**
     * 用于ConnectivityManager.NetworkCallback中onAvailable的NetWork状态监听
     */
    static public abstract class AvailableNetworkListener {

        boolean mNetworkIsEmpty = true;

        boolean mNetWorkIsMatched;

        /**
         * 获取到满足的网络，只会调用一次
         *
         * @param network 首次满足条件的NetWork
         */
        public void onSuccess(Network network, ConnectivityManager.NetworkCallback callback){

        }

        /**
         * 网络更新，匹配到了更好的网络，会多次调用
         *
         * @param network 更新后的NetWork
         */
        public void onUpdate(Network network){

        };

        /**
         * 找到的网络与要求的不匹配
         *
         * @param network 不匹配的网络，但是目前最好的
         */
        public void onNotMatchNeedNetWork(Network network){

        };

        public void onLost(Network network,ConnectivityManager.NetworkCallback callback){

        }

    }

    public interface Match {
        public boolean match(NetworkCapabilities allCapabilities);
    }

}
