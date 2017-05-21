package ca.projecthermes.projecthermes.networking;

import ca.projecthermes.projecthermes.networking.payload.IPayload;

public interface IPacket<T extends IPayload> {
    int getPacketType();

    Class<T> getPayloadClazz();
}
