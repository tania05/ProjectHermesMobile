package ca.projecthermes.projecthermes.networking.Responder;

import java.util.ArrayList;
import java.util.Arrays;

import ca.projecthermes.projecthermes.IHermesLogger;
import ca.projecthermes.projecthermes.networking.IPacketManager;
import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.networking.payload.Message;
import ca.projecthermes.projecthermes.networking.payload.TransmissionRequest;
import ca.projecthermes.projecthermes.util.IMessageStore;
import ca.projecthermes.projecthermes.util.IObservableListener;

public class TransmissionRequestResponder implements Runnable {
    private final IHermesLogger _logger;
    private final IPacketManager _packetManager;
    private final IMessageStore _messageStore;

    private boolean _running = false;

    public TransmissionRequestResponder(
        IHermesLogger logger,
        IPacketManager packetManager,
        IMessageStore messageStore
    ) {
        _logger = logger;
        _packetManager = packetManager;
        _messageStore = messageStore;

        subscribeToObservables();
    }

    @Override
    public void run() {
        synchronized (this) {
            if (_running) {
                throw new RuntimeException("TransmissionRequestResponder already running");
            }
            _running = true;
            offerMessages();
        }
    }

    private void subscribeToObservables() {
        _packetManager.getPacketReceiveObservable().subscribe(new IObservableListener<IPayload>() {
            @Override
            public void update(IPayload arg) {
                if (arg instanceof TransmissionRequest) {
                    onTransmissionRequest((TransmissionRequest) arg);
                } else if (arg instanceof Message) {
                    onMessageReceived((Message) arg);
                }
            }

            @Override
            public void error(Exception e) {

            }
        });
    }

    private void offerMessages() {
        ArrayList<byte[]> identifiers = _messageStore.getStoredMessageIdentifiers();
        if (identifiers == null || identifiers.size() == 0) {
            // No need to offer messages, we have none to send.
            return;
        }

        TransmissionRequest offer = new TransmissionRequest(false, identifiers);

        _logger.d("Offering messages: " + offer);
        _packetManager.sendMessage(offer);
    }

    private void onTransmissionRequest(TransmissionRequest request) {
        _logger.d("Received transmission request: " + request);
        if (request.isRequesting) {
            for (byte[] requestedIdentifier : request.messageIdentifiers) {
                Message message = _messageStore.getMessageForIdentifier(requestedIdentifier);
                if (message != null) {
                    _logger.d("Sending message " + message);
                    _packetManager.sendMessage(message);
                } else {
                    _logger.e("Could not find message with identifier " + Arrays.toString(requestedIdentifier) + " in database." );
                }
            }
        } else {
            ArrayList<byte[]> requestingIdentifiers = new ArrayList<>();
            for (byte[] offeringIdentifier : request.messageIdentifiers) {
                Message message = _messageStore.getMessageForIdentifier(offeringIdentifier);
                if (message == null) {
                    requestingIdentifiers.add(offeringIdentifier);
                }
            }

            TransmissionRequest requestResponse = new TransmissionRequest(true, requestingIdentifiers);
            _logger.d("Requesting messages " + requestResponse);
            _packetManager.sendMessage(requestResponse);
        }
    }

    private void onMessageReceived(Message message) {
        _logger.d("Received message: " + message);

        //TODO we could potentially be trying to store the same message twice, the store should handle this.
        _messageStore.storeMessage(message);
    }
}
