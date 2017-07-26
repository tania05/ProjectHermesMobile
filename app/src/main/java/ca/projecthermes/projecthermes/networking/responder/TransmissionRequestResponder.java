package ca.projecthermes.projecthermes.networking.responder;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import ca.projecthermes.projecthermes.Ethereum.Ethereum;
import ca.projecthermes.projecthermes.IHermesLogger;
import ca.projecthermes.projecthermes.networking.packet.IPacketManager;
import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.networking.payload.Message;
import ca.projecthermes.projecthermes.networking.payload.TransmissionRequest;
import ca.projecthermes.projecthermes.data.IMessageStore;
import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.IObservableListener;
import ca.projecthermes.projecthermes.util.Util;

public class TransmissionRequestResponder implements Runnable {
    private final IHermesLogger _logger;
    private final IPacketManager _packetManager;
    private final IMessageStore _messageStore;
    private final IObservable<byte[]> _messageAddedObservable;

    private boolean _running = false;

    public TransmissionRequestResponder(
            @NotNull IHermesLogger logger,
            @NotNull IPacketManager packetManager,
            @NotNull IMessageStore messageStore,
            @NotNull IObservable<byte[]> messageAddedObservable
            ) {
        _logger = logger;
        _packetManager = packetManager;
        _messageStore = messageStore;
        _messageAddedObservable = messageAddedObservable;

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

        _messageAddedObservable.subscribe(new IObservableListener<byte[]>() {
            @Override
            public void update(byte[] arg) {
                ArrayList<byte[]> identifiers = new ArrayList<>();
                identifiers.add(arg);

                // Offer the message.
                TransmissionRequest messageOffer = new TransmissionRequest(false, identifiers);
                _packetManager.sendMessage(messageOffer);
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
                    _logger.d("I do not have message identifier " + Util.bytesToHex(offeringIdentifier));
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
        Log.e("FFFF", "Received message: " + Util.bytesToHex(message.identifier));
        _messageStore.storeMessage(message);
    }
}
