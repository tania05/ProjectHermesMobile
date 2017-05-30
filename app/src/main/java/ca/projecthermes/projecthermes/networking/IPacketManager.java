package ca.projecthermes.projecthermes.networking;

import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.Null;
import ca.projecthermes.projecthermes.util.Source;

public interface IPacketManager {
    IObservable<Null> getDisconnectObservable();

    IObservable<IPayload> getPacketReceiveObservable();

    Source<Null> sendMessage(IPayload payload);
}
