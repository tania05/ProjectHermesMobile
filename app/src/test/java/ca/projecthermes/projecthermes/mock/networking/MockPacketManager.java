package ca.projecthermes.projecthermes.mock.networking;

import ca.projecthermes.projecthermes.networking.packet.IPacketManager;
import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.Null;
import ca.projecthermes.projecthermes.util.Source;

public class MockPacketManager implements IPacketManager {
    public final Source<Null> disconnectSource = new Source<>();
    public final Source<IPayload> packetReceiveSource = new Source<>();
    public final Source<Object> sendMessageSource = new Source<>();
    public final Source<Null> onSuccessfulSendSource = new Source<>();

    @Override
    public IObservable<Null> getDisconnectObservable() {
        return disconnectSource;
    }

    @Override
    public IObservable<IPayload> getPacketReceiveObservable() {
        return packetReceiveSource;
    }

    @Override
    public Source<Null> sendMessage(IPayload payload) {
        sendMessageSource.update(payload);
        return onSuccessfulSendSource;
    }

    @Override
    public void disconnect() {
        disconnectSource.update(null);
    }

    @Override
    public void run() {

    }
}
