package ca.projecthermes.projecthermes.networking;

import android.net.wifi.p2p.WifiP2pDevice;

public interface INetworkDeviceFactory {
    INetworkDevice createFromWifiP2pDevice(WifiP2pDevice device);
}
