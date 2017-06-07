package ca.projecthermes.projecthermes.networking;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.Null;

public interface INetworkDevice {
    IObservable<INetworkDevice> getStatusChangeObservable();

    IObservable<INetworkDevice> getSignalLossObservable();

    void deviceStatusUpdate(WifiP2pDevice updatedDevice);

    void onSignalLoss();

    IObservable<WifiP2pInfo> connect();

    IObservable<WifiP2pInfo> requestNetworkInfo();

    String getName();
    String getDeviceAddress();
    boolean getIsGroupOwner();
    int getStatus();
}