package ca.projecthermes.projecthermes.networking;

import java.util.ArrayList;
import java.util.Arrays;

import ca.projecthermes.projecthermes.networking.payload.Heartbeat;
import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.networking.payload.Message;
import ca.projecthermes.projecthermes.networking.payload.TransmissionRequest;

public class Packet<T extends IPayload> implements IPacket<T> {
    private int _packetType;
    private Class<T> _payloadClazz;

    public Packet(int packetType, Class<T> payloadClazz) {
        _packetType = packetType;
        _payloadClazz = payloadClazz;
    }

    @Override
    public int getPacketType() { return _packetType; }
    @Override
    public Class<T> getPayloadClazz() { return _payloadClazz; }


    // Ugh... lets remove this static here.
    // TODO
    public static ArrayList<IPacket> PACKET_TYPES = new ArrayList<>(Arrays.asList(new IPacket[] {
            new Packet<>(0, Heartbeat.class),
            new Packet<>(1, TransmissionRequest.class),
            new Packet<>(2, Message.class)
    }));
}
