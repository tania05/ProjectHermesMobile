package ca.projecthermes.projecthermes.networking;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ca.projecthermes.projecthermes.IHermesLogger;
import ca.projecthermes.projecthermes.util.CollectionUtil;
import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.Source;

public class NetworkManager implements INetworkManager {
    private final WifiP2pManager _wifiP2pManager;
    private final WifiP2pManager.Channel _channel;
    private final INetworkDeviceFactory _networkDeviceFactory;
    private final IHermesLogger _logger;

    private final HashMap<String, INetworkDevice> _deviceMap;
    private final Source<INetworkDevice> _newDeviceSource;

    public NetworkManager(
        @NotNull WifiP2pManager wifiP2pManager,
        @NotNull WifiP2pManager.Channel channel,
        @NotNull INetworkDeviceFactory networkDeviceFactory,
        @NotNull IHermesLogger logger
    ) {
        _wifiP2pManager = wifiP2pManager;
        _channel = channel;
        _networkDeviceFactory = networkDeviceFactory;
        _logger = logger;

        _deviceMap = new HashMap<>();
        _newDeviceSource = new Source<>();
    }

    @Override
    public IObservable<INetworkDevice> getNewDeviceFoundObservable() { return _newDeviceSource; }

    @Override
    public void updatePeers() {
        // Found a new device
        // - Add to internal device list
        // - send device found event
        _wifiP2pManager.requestPeers(_channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                onPeerListUpdate(peers);
            }
        });
    }

    private void onPeerListUpdate(WifiP2pDeviceList devices) {
        synchronized (this) {
            _logger.d("Updating peer list");
            for (WifiP2pDevice device : devices.getDeviceList()) {
                _logger.d("Looking at device: " + device.deviceName);
                updateOrAddDevice(device);
            }

            Collection<String> devicesInRange = CollectionUtil.map(devices.getDeviceList(), new CollectionUtil.IMapCallback<WifiP2pDevice, String>() {
                @Override
                public String convert(WifiP2pDevice val) {
                    return val.deviceAddress;
                }
            });

            Set<String> outOfRangeKeys = new HashSet<>(_deviceMap.keySet());
            outOfRangeKeys.removeAll(devicesInRange);

            if (outOfRangeKeys.size() > 0) {
                for (String key : outOfRangeKeys) {
                    _logger.d("Found a device that has lost signal");
                    _deviceMap.remove(key).onSignalLoss();
                }
            }
        }
    }

    private void updateOrAddDevice(WifiP2pDevice device) {
        String deviceAddress = device.deviceAddress;

        synchronized (this) {
            if (_deviceMap.containsKey(deviceAddress)) {
                _deviceMap.get(deviceAddress).deviceStatusUpdate(device);
            } else {
                INetworkDevice networkDevice = _networkDeviceFactory.createFromWifiP2pDevice(device);
                _deviceMap.put(device.deviceAddress, networkDevice);
                _newDeviceSource.update(networkDevice);
            }
        }
    }
}
