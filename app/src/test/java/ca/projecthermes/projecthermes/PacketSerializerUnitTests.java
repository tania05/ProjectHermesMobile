package ca.projecthermes.projecthermes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import ca.projecthermes.projecthermes.mock.networking.payload.TestPayload;
import ca.projecthermes.projecthermes.networking.IPacket;
import ca.projecthermes.projecthermes.networking.Packet;
import ca.projecthermes.projecthermes.networking.PacketSerializer;
import ca.projecthermes.projecthermes.util.NullLogger;

import static org.junit.Assert.*;

public class PacketSerializerUnitTests {
    private ArrayList<IPacket> getPacketList() {
        return new ArrayList<>(Arrays.asList(new IPacket[] {
                new Packet<>(100, TestPayload.class)
        }));
    }

    @Test
    public void CanRoundTrip() {
        TestPayload payload = new TestPayload((int)(Math.random() * 10000));
        PacketSerializer serializer = new PacketSerializer(new NullLogger(), getPacketList());

        assertEquals(payload, serializer.deserialize(serializer.serialize(payload)));
    }
}
