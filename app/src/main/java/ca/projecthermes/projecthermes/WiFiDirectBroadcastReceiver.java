package ca.projecthermes.projecthermes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import ca.projecthermes.projecthermes.data.HermesDbHelper;
import ca.projecthermes.projecthermes.exceptions.InvokerFailException;
import ca.projecthermes.projecthermes.networking.INetworkDevice;
import ca.projecthermes.projecthermes.networking.INetworkManager;
import ca.projecthermes.projecthermes.networking.packet.IPacketManager;
import ca.projecthermes.projecthermes.networking.responder.HeartbeatResponder;
import ca.projecthermes.projecthermes.networking.packet.Packet;
import ca.projecthermes.projecthermes.networking.packet.PacketManager;
import ca.projecthermes.projecthermes.networking.packet.PacketSerializer;
import ca.projecthermes.projecthermes.networking.responder.TransmissionRequestResponder;
import ca.projecthermes.projecthermes.util.BundleHelper;
import ca.projecthermes.projecthermes.util.ErrorCodeHelper;
import ca.projecthermes.projecthermes.util.IObservableListener;
import ca.projecthermes.projecthermes.util.Source;
import ca.projecthermes.projecthermes.util.TimeManager;
import ca.projecthermes.projecthermes.util.Util;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final int PORT = 2150;

    private final INetworkManager _networkManager;
    private final IHermesLogger _logger;

    private final Object _serverRunningLock = new Object();
    private boolean _serverRunning = false;

    private final Source<byte[]> _messageAddedSource = new Source<>();

    private Context context;

    public WiFiDirectBroadcastReceiver(
            @NotNull INetworkManager networkManager,
            @NotNull IHermesLogger logger
    ) {
        _networkManager = networkManager;
        _logger = logger;

        registerObservables();
    }

    private void registerObservables() {
        _networkManager.getNewDeviceFoundObservable().subscribe(new IObservableListener<INetworkDevice>() {
            @Override
            public void update(INetworkDevice arg) {
                onNewDeviceFound(arg);
            }

            @Override
            public void error(Exception e) {
                // Purposely empty, will not be called
                _logger.wtf("Unexpected error event on newDeviceObservable: " + e.toString());
            }
        });
    }

    private void onNewDeviceFound(final INetworkDevice device) {
        _logger.d("Found a new device " + device.getName());
        IObservableListener<INetworkDevice> listener = getDeviceUpdateInterface();
        device.getStatusChangeObservable().subscribe(listener);

        listener.update(device);
    }

    private IObservableListener<INetworkDevice> getDeviceUpdateInterface() {
        final boolean[] attemptedConnection = new boolean[] { false };
        return new IObservableListener<INetworkDevice>() {

            @Override
            public void update(final INetworkDevice device) {

                _logger.d("Device " + device.getName() + " now has status " + ErrorCodeHelper.findPossibleConstantsForInt(device.getStatus(), WifiP2pDevice.class));

                int deviceStatus = device.getStatus();
                if (deviceStatus == WifiP2pDevice.AVAILABLE) {
                    _logger.d("Device " + device.getName() + " is available, attempting connection...");
                    attemptedConnection[0] = false;
                    device.connect();
                }
                if (attemptedConnection[0]) return;

                if (deviceStatus == WifiP2pDevice.CONNECTED) {
                    attemptedConnection[0] = true;
                    device.requestNetworkInfo().subscribe(new IObservableListener<WifiP2pInfo>() {
                        @Override
                        public void update(WifiP2pInfo arg) {
                            onDeviceConnect(device, arg);
                        }

                        @Override
                        public void error(Exception e) {
                            _logger.wtf("Unexpected error event on requestNetworkInfo: " + e.toString());

                        }
                    });
                }
            }

            @Override
            public void error(Exception e) {
                _logger.wtf("Unexpected error event on deviceUpdateObservable: " + e.toString());
            }
        };
    }

    private void onDeviceConnect(final INetworkDevice device, final WifiP2pInfo info) {
        _logger.d("Connected to " + device.getName());
        try {
            if (info.isGroupOwner) {
                ensureServerRunning();
            } else {
                _logger.d("Attempting to open client connection...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Socket[] socket = new Socket[] { null };

                        //Temporary block
                        try {
                            Util.sleepRetryInvoker(5000, 30, new Util.IInvokerCallback() {
                                @Override
                                public void call() throws Exception {
                                    _logger.d("Attempting to open client socket");
                                    try {
                                        socket[0] = getClientSocketForWifiP2pInfo(info);
                                    } catch (Exception e) {
                                        _logger.d("Failed to open client socket: " + e.toString());
                                        throw e;
                                    }
                                }
                            });

                            //blocks
                            onSocketConnectRunnable(socket[0], false).run();
                        } catch (InvokerFailException e) {
                            _logger.e("Failed to open client socket 30 times, aborting.");
                            device.disconnect();
                        }

                    }
                }).start();
            }
        } catch (Exception e) {
            _logger.wtf("onDeviceConnect: " + e.toString());
        }
    }

    private Runnable onSocketConnectRunnable(final Socket socket, final boolean isServer) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    IPacketManager packetManager = createPacketManager(socket);
                    Collection<Runnable> responders;
                    if (isServer) {
                        responders = createServerRunnables(packetManager);
                    } else {
                        responders = createClientRunnables(packetManager);
                    }
                    for (Runnable responder : responders) {
                        new Thread(responder).start();
                    }

                    // blocks
                    packetManager.run();
                } catch (IOException e) {
                    _logger.wtf("onSocketConnectRunnable: " + e.toString());
                }
            }
        };
    }


    private void serverLoop(ServerSocket serverSocket) throws IOException {
        //noinspection InfiniteLoopStatement
        while (true) {
            _logger.d("Waiting for connection...");
            final Socket socket = serverSocket.accept();
            _logger.d("Connected to new client");
            new Thread(onSocketConnectRunnable(socket, true)).start();
        }
    }

    private void ensureServerRunning() throws IOException {
        _logger.d("Ensuring that server is running.");
        synchronized (_serverRunningLock) {
            if (!_serverRunning) {
                _serverRunning = true;


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //blocks
                        try {
                            ServerSocket serverSocket = new ServerSocket(PORT);
                            _logger.d("Server running on port : " + PORT);

                            serverLoop(serverSocket);
                        } catch (IOException e) {
                            _logger.wtf("Server IOException: " + e.toString());
                        }
                        _logger.wtf("Server closed...");
                    }
                }).start();
            }
        }
    }

    private Socket getClientSocketForWifiP2pInfo(WifiP2pInfo info) throws IOException {
        Socket socket = new Socket();
        socket.bind(null);

        _logger.d("Attempting to open socket to " + info.groupOwnerAddress + "forced to 192.168.49.1 on port " + PORT);

        socket.connect(new InetSocketAddress("192.168.49.1", PORT), 10000);
        return socket;
    }

    @NonNull
    private IPacketManager createPacketManager(@NotNull Socket socket) throws IOException {
        return new PacketManager(new HermesLogger("PacketManager"), socket.getInputStream(), socket.getOutputStream(), new PacketSerializer(new HermesLogger("PacketSerializer"), Packet.PACKET_TYPES));
    }

    @NonNull
    private Collection<Runnable> createServerRunnables(@NotNull IPacketManager packetManager) {
        return createCommonRunnables(packetManager);
    }

    @NonNull
    private Collection<Runnable> createClientRunnables(@NotNull IPacketManager packetManager) {
        return createCommonRunnables(packetManager);
    }

    @NonNull
    private Collection<Runnable> createCommonRunnables(@NotNull IPacketManager packetManager) {
        return Arrays.asList(
                new HeartbeatResponder(new HermesLogger("HeartbeatResponder"), packetManager, new TimeManager(), 60000),
                new TransmissionRequestResponder(
                        new HermesLogger("TransmissionRequestResponder"),
                        packetManager,
                        new HermesDbHelper(context),
                        _messageAddedSource
                )
        );
    }

    private void onPeerUpdate() {
        _networkManager.updatePeers();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
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
            onPeerUpdate();
        } else if (HermesDbHelper.MESSAGE_ADDED_ACTION.equals(action)) {
            byte[] identifier = intent.getByteArrayExtra(HermesDbHelper.EXTRA_MESSAGE_IDENTIFIER);
            _messageAddedSource.update(identifier);
        }
    }
}
