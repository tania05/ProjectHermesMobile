package ca.projecthermes.projecthermes.networking.packet;

import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.Null;
import ca.projecthermes.projecthermes.util.Source;

public interface IPacketManager extends Runnable {
    IObservable<Null> getDisconnectObservable();

    IObservable<IPayload> getPacketReceiveObservable();

    IObservable<Null> sendMessage(IPayload payload);

    void disconnect();
}
