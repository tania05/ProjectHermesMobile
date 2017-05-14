package ca.projecthermes.projecthermes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Iterator;

import ca.projecthermes.projecthermes.util.BundleHelper;
import ca.projecthermes.projecthermes.util.ErrorCodeHelper;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("hermes", "Broadcast received of action " + action + " with extras " + BundleHelper.describeContents(intent.getExtras()));

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d("hermes", "Wifi P2P enabled");
            } else {
                Log.d("hermes", "Wifi P2P disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Iterator<WifiP2pDevice> devices = peers.getDeviceList().iterator();

                        if (!devices.hasNext()) {
                            Log.d("hermes", "There is no nearby peers");
                            return;
                        }

                        final WifiP2pDevice device = devices.next();
                        Log.d("hermes", "First device named " + device.deviceName + " has status " + ErrorCodeHelper.findPossibleConstantsForInt(device.status, WifiP2pDevice.class));
                        if (device.status == WifiP2pDevice.AVAILABLE) {
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;

                            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d("hermes", "Connected to peer " + device.deviceName);
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Log.d("hermes", "Could not connect to peer for reason " + reason);
                                }
                            });
                        }

                    }
                });
            }
        }
    }
}
