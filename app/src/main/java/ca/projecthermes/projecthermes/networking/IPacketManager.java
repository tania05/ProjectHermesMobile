package ca.projecthermes.projecthermes.networking;

import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.Null;

/**
 * Created by brand_000 on 2017-05-20.
 */

interface IPacketManager {
    IObservable<Null> getDisconnectObservable();

    IObservable<IPayload> getPacketReceiveObservable();

    IObservable<Object> sendMessage(IPayload payload);
}
