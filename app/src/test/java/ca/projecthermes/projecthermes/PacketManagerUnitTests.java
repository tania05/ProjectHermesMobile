package ca.projecthermes.projecthermes;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.concurrent.FutureTask;

import ca.projecthermes.projecthermes.mock.networking.payload.TestPayload;
import ca.projecthermes.projecthermes.networking.IPacketSerializer;
import ca.projecthermes.projecthermes.networking.PacketManager;
import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.util.IObservableListener;
import ca.projecthermes.projecthermes.util.Null;
import ca.projecthermes.projecthermes.util.NullLogger;
import ca.projecthermes.projecthermes.util.SystemLogger;

import static org.junit.Assert.*;

public class PacketManagerUnitTests {

    private ByteArrayInputStream getInputStream() {
        return new ByteArrayInputStream(new byte[] {
                0x00, 0x00, 0x00, 0x02,
                0x00, 0x24    // 2 bytes, "$"
        });
    }

    private ByteArrayOutputStream getOutputStream() {
        return new ByteArrayOutputStream();
    }

    @Test
    public void CanReadAPacket() throws Exception {
        ByteArrayInputStream input = getInputStream();
        ByteArrayOutputStream output = getOutputStream();
        final Object waiter = new Object();
        final IPayload[] payload = new IPayload[1];
        final boolean[] disconnectCalled = new boolean[] { false };

        PacketManager pm = new PacketManager(new NullLogger(), input, output, new IPacketSerializer() {
            @Override
            public IPayload deserialize(String json) {
                if (json.equals("$")) {
                    return new TestPayload(24);
                }
                throw new RuntimeException("Deserializing incorrect string");
            }

            @Override
            public String serialize(Object payload) {
                throw new RuntimeException("Not expected to be called.");
            }
        });

        pm.getPacketReceiveObservable().subscribe(new IObservableListener<IPayload>() {
            @Override
            public void update(IPayload arg) {
                synchronized (waiter) {
                    payload[0] = arg;
                    waiter.notify();
                }
            }

            @Override
            public void error(Exception e) {
                throw new RuntimeException("Not expected to be called.");
            }
        });

        pm.getDisconnectObservable().subscribe(new IObservableListener<Null>() {
            @Override
            public void update(Null arg) {
                synchronized (waiter) {
                    disconnectCalled[0] = true;
                    waiter.notify();
                }
            }

            @Override
            public void error(Exception e) {
                throw new RuntimeException("Not expected to be called.");
            }
        });

        new Thread(pm).start();

        synchronized (waiter) {
            //TODO, better async testing, this will fail on spurious wakeup.
            waiter.wait(5000);
            assertTrue(payload[0] instanceof TestPayload);
            assertEquals(((TestPayload)payload[0]).val, 24);

            waiter.wait(5000);
            assertTrue(disconnectCalled[0]);

        }

    }
}
