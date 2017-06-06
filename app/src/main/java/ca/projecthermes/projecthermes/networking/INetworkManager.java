package ca.projecthermes.projecthermes.networking;

import ca.projecthermes.projecthermes.util.IObservable;

public interface INetworkManager {
    IObservable<INetworkDevice> getNewDeviceFoundObservable();

    void updatePeers();
}
