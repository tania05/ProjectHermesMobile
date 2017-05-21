package ca.projecthermes.projecthermes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

import ca.projecthermes.projecthermes.networking.HeartbeatResponder;
import ca.projecthermes.projecthermes.networking.Packet;
import ca.projecthermes.projecthermes.networking.PacketManager;
import ca.projecthermes.projecthermes.networking.PacketSerializer;
import ca.projecthermes.projecthermes.util.BundleHelper;
import ca.projecthermes.projecthermes.util.ErrorCodeHelper;
import ca.projecthermes.projecthermes.util.TimeManager;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

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
                        } else if (device.status == WifiP2pDevice.CONNECTED) {
                            manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                                @Override
                                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                    Log.d("hermes", "Got connection info");
                                    final WifiP2pInfo finfo = info;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (!finfo.isGroupOwner) {
                                                    Log.d("hermes", "I am client");
                                                    Socket socket = new Socket();
                                                    socket.bind(null);
                                                    int port = 2150;
                                                    int timeout = 5000;

                                                    try {
                                                        //Hacky fix for a short time.
                                                        Log.d("hermes", "hacky sleep");
                                                        Thread.sleep(5000);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }

                                                    Log.d("hermes", "sleep over");
                                                    socket.connect(new InetSocketAddress(finfo.groupOwnerAddress, port), timeout);
                                                    PacketManager packetManager = new PacketManager(
                                                            new HermesLogger("packetManager"),
                                                            socket.getInputStream(),
                                                            socket.getOutputStream(),
                                                            new PacketSerializer(new HermesLogger("packetSerializer"), Packet.PACKET_TYPES)
                                                    );
                                                    new Thread(new HeartbeatResponder(new HermesLogger("heartbeatResponder"), packetManager, new TimeManager(), 7500)).start();
                                                    new Thread(packetManager).start();
                                                } else {
                                                    Log.d("hermes", "I am server");
                                                    ServerSocket server = new ServerSocket(2150);
                                                    Log.d("hermes", "Waiting for packetManager...");
                                                    Socket client = server.accept();
                                                    Log.d("hermes", "Got packetManager");

                                                    PacketManager packetManager = new PacketManager(
                                                            new HermesLogger("packetManager"),
                                                            client.getInputStream(),
                                                            client.getOutputStream(),
                                                            new PacketSerializer(new HermesLogger("packetSerializer"), Packet.PACKET_TYPES)
                                                    );
                                                    new Thread(new HeartbeatResponder(new HermesLogger("heartbeatResponder"), packetManager, new TimeManager(), 10000)).start();
                                                    new Thread(packetManager).start();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                }
                            });
                        }
                    }
                });
            }
        }
    }
}
