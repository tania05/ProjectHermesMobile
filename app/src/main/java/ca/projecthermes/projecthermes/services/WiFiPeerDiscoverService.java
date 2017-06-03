package ca.projecthermes.projecthermes.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WiFiPeerDiscoverService extends IntentService {
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;

    public WiFiPeerDiscoverService() {
        super("WiFiPeerDiscoverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("hermes", "Service running");

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

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
    }
}
