package ca.projecthermes.projecthermes.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import ca.projecthermes.projecthermes.HermesLogger;
import ca.projecthermes.projecthermes.WiFiDirectBroadcastReceiver;
import ca.projecthermes.projecthermes.data.HermesDbHelper;
import ca.projecthermes.projecthermes.networking.INetworkDevice;
import ca.projecthermes.projecthermes.networking.INetworkDeviceFactory;
import ca.projecthermes.projecthermes.networking.NetworkDevice;
import ca.projecthermes.projecthermes.networking.NetworkManager;
import ca.projecthermes.projecthermes.util.DefaultFactory;

public class WiFiPeerDiscoverService extends IntentService {
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;

    public WiFiPeerDiscoverService() {
        super("WiFiPeerDiscoverService");

        wifiP2pManager = null;
        channel = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        WiFiDirectBroadcastReceiver receiver = new WiFiDirectBroadcastReceiver(
                new NetworkManager(wifiP2pManager, channel, new INetworkDeviceFactory() {
                    @Override
                    public INetworkDevice createFromWifiP2pDevice(WifiP2pDevice device) {
                        return new NetworkDevice(device, wifiP2pManager, channel, new DefaultFactory<>(WifiP2pConfig.class));
                    }
                }, new HermesLogger("NetworkManager")),
                new HermesLogger("WifiDirectBroadcastReceiver")
        );

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        filter.addAction(HermesDbHelper.MESSAGE_ADDED_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        // TODO: Note, the service (by default) runs on the UI thread.
        Log.i("hermes", "Service running");

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("hermes", "discover peers runs successfully");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("hermes", "discover peers failed");
            }
        });

        // Will kill on low memory conditions, but will restart when memory
        // but will restart when the resources are available again.
        return START_STICKY;

    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
