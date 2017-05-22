package ca.projecthermes.projecthermes.networking.Responder;

import org.jetbrains.annotations.NotNull;

import ca.projecthermes.projecthermes.IHermesLogger;
import ca.projecthermes.projecthermes.networking.IPacketManager;
import ca.projecthermes.projecthermes.networking.payload.Heartbeat;
import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.util.IObservableListener;
import ca.projecthermes.projecthermes.util.ITimeManager;
import ca.projecthermes.projecthermes.util.Null;


// This class may not be necessary, it could be that this is handled automatically
// already...
//
// TODO: Not disconnecting when missing too many heartbeats.
public class HeartbeatResponder implements Runnable {
    private final IHermesLogger _logger;
    private final IPacketManager _packetManager;
    private final ITimeManager _timeManager;
    private final long _timeBetweenHeartbeats;

    private boolean _running = false;
    private Thread _runningThread;
    private long _lastHeartbeat;
    private int _missedHeartbeats;


    public HeartbeatResponder(
            @NotNull IHermesLogger logger,
            @NotNull IPacketManager packetManager,
            @NotNull ITimeManager timeManager,
            long timeBetweenHeartbeats
    ) {
        _logger = logger;
        _packetManager = packetManager;
        _timeManager = timeManager;
        _timeBetweenHeartbeats = timeBetweenHeartbeats;

        registerObservables();
    }

    @Override
    public void run() {
        synchronized (this) {
            if (_running) {
                throw new RuntimeException("HeartbeatResponder is already running.");
            }
            _running = true;

            _lastHeartbeat = _timeManager.getTime();
            _missedHeartbeats = 0;
            try {
                listenForHeartbeats();
            } catch (InterruptedException ignored) {}
        }
    }

    private void registerObservables() {
        IObservableListener<IPayload> receiveListener = new IObservableListener<IPayload>() {
            @Override
            public void update(IPayload arg) {
                synchronized (this) {
                    _lastHeartbeat = _timeManager.getTime();
                    _missedHeartbeats = 0;
                }
                if (arg instanceof Heartbeat) {
                    onHeartbeatReceived((Heartbeat) arg);
                }
            }

            @Override
            public void error(Exception e) {

            }
        };

        IObservableListener<Null> disconnectListener = new IObservableListener<Null>() {
            @Override
            public void update(Null arg) {
                synchronized (this) {
                    _runningThread.interrupt();
                }
            }

            @Override
            public void error(Exception e) {
            }
        };

        _packetManager.getPacketReceiveObservable().subscribe(receiveListener);
        _packetManager.getDisconnectObservable().subscribe(disconnectListener);
    }


    private void listenForHeartbeats() throws InterruptedException {
        // This ends on thread interruption.
        //noinspection InfiniteLoopStatement
        while (true) {
            long currentTime = _timeManager.getTime();
            boolean shouldSleep = false;
            long timeUntilNextHeartbeat = 0;
            synchronized (this) {
                timeUntilNextHeartbeat = (_lastHeartbeat + _timeBetweenHeartbeats) - currentTime;
                if (timeUntilNextHeartbeat < 0) {
                    _missedHeartbeats++;
                    _lastHeartbeat = currentTime;
                    sendHeartbeat(false);
                } else {
                    shouldSleep = true;
                }
            }

            if (shouldSleep) {
                Thread.sleep(timeUntilNextHeartbeat);
            }
        }
    }
    private void onHeartbeatReceived(Heartbeat heartbeat) {
        _logger.d("Received a heartbeat, [isReply] : " + heartbeat.isReply);
        if (!heartbeat.isReply) {
            sendHeartbeat(true);
        }
    }

    private void sendHeartbeat(boolean isReply) {
        _logger.d("Sending a heartbeat, [isReply] : " + isReply);
        _packetManager.sendMessage(new Heartbeat(isReply));
    }
}
