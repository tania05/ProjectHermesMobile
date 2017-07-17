package ca.projecthermes.projecthermes.networking;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import ca.projecthermes.projecthermes.exceptions.IntValueException;
import ca.projecthermes.projecthermes.util.ErrorCodeHelper;
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

    private Timer _timer;

    // Updated through the "deviceUpdateObservable"
    private WifiP2pDevice _wifiP2pDevice;

    // Updated on device connection
    private WifiP2pInfo _wifiP2pInfo;

    public NetworkDevice(
        @NotNull WifiP2pDevice wifiP2pDevice,
        @NotNull WifiP2pManager wifiP2pManager,
        @NotNull WifiP2pManager.Channel channel,
        @NotNull IFactory<WifiP2pConfig> configFactory
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
            if (!Util.equal(_wifiP2pDevice.deviceAddress, updatedDevice.deviceAddress))
                throw new AssertionError();


            int oldStatus = _wifiP2pDevice.status;
            int newStatus = updatedDevice.status;

            _wifiP2pDevice = updatedDevice;

            if (oldStatus != newStatus) {
                _statusChangeSource.update(this);

                if (_timer != null) {
                    _timer.cancel();
                }
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
    public void connect() {
        WifiP2pConfig config = _configFactory.create();

        synchronized (this) {
            config.deviceAddress = _wifiP2pDevice.deviceAddress;
            config.groupOwnerIntent = (int) Math.floor(Math.random() * 16);
            _wifiP2pManager.connect(_channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    //ignored, yay!
                }

                @Override
                public void onFailure(int reason) {
                    Log.e("NetworkDevice", "Failed to initiate connection to device " + _wifiP2pDevice.deviceName);
                }
            });
        }
    }

    @Override
    public void disconnect() {
        _wifiP2pManager.removeGroup(_channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("NetworkDevice", "Manual disconnect success.");
            }

            @Override
            public void onFailure(int reason) {
                Log.e("NetworkDevice", "Failed to disconnect from device... ");
            }
        });
    }

    @Override
    public void cancelConnect() {
        _wifiP2pManager.cancelConnect(_channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("NetworkDevice", "Canceled the connection.");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("NetworkDevice", "Failed to cancel connection.");
            }
        });
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
    public int getStatus() {
        synchronized (this) {
            return _wifiP2pDevice.status;
        }
    }

    @Override
    public void scheduleDisconnect(int time) {
        if (_timer == null) {
            _timer = new Timer();
            _timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    disconnect();
                }
            }, time);
        }
    }
}
