package ca.projecthermes.projecthermes.networking;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import ca.projecthermes.projecthermes.exceptions.IntValueException;
import ca.projecthermes.projecthermes.util.IFactory;
import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.Source;
import ca.projecthermes.projecthermes.util.Util;

public class NetworkDevice implements INetworkDevice {

    private final WifiP2pManager  _wifiP2pManager;
    private final WifiP2pManager.Channel _channel;
    private final IFactory<WifiP2pConfig> _configFactory;

    private final Source<INetworkDevice> _statusChangeSource;
    private final Source<INetworkDevice> _signalLossSource;

    // Updated through the "deviceUpdateObservable"
    private WifiP2pDevice _wifiP2pDevice;

    // Updated on device connection
    private WifiP2pInfo _wifiP2pInfo;

    public NetworkDevice(
        @NotNull WifiP2pDevice wifiP2pDevice,
        @NotNull WifiP2pManager wifiP2pManager,
        @NotNull WifiP2pManager.Channel channel,
        IFactory<WifiP2pConfig> configFactory
    ) {
        _wifiP2pDevice = wifiP2pDevice;

        _wifiP2pManager = wifiP2pManager;
        _channel = channel;
        _configFactory = configFactory;

        _statusChangeSource = new Source<>();
        _signalLossSource = new Source<>();
    }

    @Override
    public IObservable<INetworkDevice> getStatusChangeObservable() { return _statusChangeSource; }
    @Override
    public IObservable<INetworkDevice> getSignalLossObservable() { return _signalLossSource; }

    @Override
    public void deviceStatusUpdate(WifiP2pDevice updatedDevice) {
        synchronized (this) {
            Log.d("Device", "update device " + updatedDevice.deviceName);
            if (!Util.equal(_wifiP2pDevice.deviceAddress, updatedDevice.deviceAddress))
                throw new AssertionError();


            int oldStatus = _wifiP2pDevice.status;
            int newStatus = updatedDevice.status;

            Log.d("Device", "Old status " + oldStatus + " new status " + newStatus);
            _wifiP2pDevice = updatedDevice;

            if (oldStatus != newStatus) {
                _statusChangeSource.update(this);
            }
        }
    }

    @Override
    public void onSignalLoss() {
        synchronized (this) {
            _signalLossSource.update(this);
        }
    }

    @Override
    public IObservable<WifiP2pInfo> connect() {
        WifiP2pConfig config = _configFactory.create();
        final Source<WifiP2pInfo> connectListener = new Source<>();


        synchronized (this) {
            config.deviceAddress = _wifiP2pDevice.deviceAddress;
            _wifiP2pManager.connect(_channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    _wifiP2pManager.requestConnectionInfo(_channel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            connectListener.update(info);
                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    connectListener.error(new IntValueException(reason));
                }
            });
        }

        return connectListener;
    }

    @Override
    public IObservable<WifiP2pInfo> requestNetworkInfo() {
        final Source<WifiP2pInfo> source = new Source<>();
        _wifiP2pManager.requestConnectionInfo(_channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                source.update(info);
            }
        });

        return source;
    }

    @Override
    public String getName() {
        synchronized (this) {
            return _wifiP2pDevice.deviceName;
        }
    }

    @Override
    public String getDeviceAddress() {
        synchronized (this) {
            return _wifiP2pDevice.deviceAddress;
        }
    }
    @Override
    public boolean getIsGroupOwner() {
        synchronized (this) {
            return _wifiP2pDevice.isGroupOwner();
        }
    }

    @Override
    public int getStatus() {
        synchronized (this) {
            return _wifiP2pDevice.status;
        }
    }
}
