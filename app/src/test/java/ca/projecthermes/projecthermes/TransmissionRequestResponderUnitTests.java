package ca.projecthermes.projecthermes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import ca.projecthermes.projecthermes.mock.networking.MockPacketManager;
import ca.projecthermes.projecthermes.networking.Responder.TransmissionRequestResponder;
import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.networking.payload.Message;
import ca.projecthermes.projecthermes.networking.payload.TransmissionRequest;
import ca.projecthermes.projecthermes.util.IMessageStore;
import ca.projecthermes.projecthermes.util.IObservableListener;
import ca.projecthermes.projecthermes.util.NullLogger;
import ca.projecthermes.projecthermes.util.Source;

import static org.junit.Assert.*;

public class TransmissionRequestResponderUnitTests {
    private IMessageStore getStore(final byte testIdentifier, final Source<Message> storeSource) {
        return new IMessageStore() {

            @Override
            public ArrayList<byte[]> getStoredMessageIdentifiers() {
                ArrayList<byte[]> b = new ArrayList<>();
                b.add(new byte[] { testIdentifier });

                return b;
            }

            @Override
            public Message getMessageForIdentifier(byte[] identifier) {
                if (Arrays.equals(identifier, new byte[]{ testIdentifier })) {
                    return new Message(
                            new byte[] { 0x01 },
                            new byte[] { 0x00 },
                            new byte[] { 0x01, 0x02, 0x03 }
                    );
                }
                return null;
            }

            @Override
            public void storeMessage(Message m) {
                if (storeSource != null) {
                    storeSource.update(m);
                }
            }
        };
    }

    @Test
    public void WillNotRequestAMessageItHas() throws Exception {
        final int[] numRequested = new int[] { -1 };

        MockPacketManager pm = new MockPacketManager();
        IMessageStore store = getStore((byte) 0, null);

        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store);


        pm.sendMessageSource.subscribe(new IObservableListener<Object>() {
            @Override
            public void update(Object arg) {
                if (arg instanceof TransmissionRequest) {
                    TransmissionRequest request = (TransmissionRequest) arg;
                    if (request.isRequesting) {
                        numRequested[0] = request.messageIdentifiers.size();
                    }
                }
            }

            @Override
            public void error(Exception e) {
                throw new RuntimeException("NETBC");
            }
        });

        responder.run();

        ArrayList<byte[]> receivedIdentifierList = new ArrayList<>();
        receivedIdentifierList.add(new byte[] { 0 });
        pm.packetReceiveSource.update(new TransmissionRequest(false, receivedIdentifierList));

        assertEquals(numRequested[0], 0);
    }

    @Test
    public void WillRequestAMessageItDoesNotHave() throws Exception {
        final int[] numRequested = new int[] { -1 };

        MockPacketManager pm = new MockPacketManager();
        IMessageStore store = getStore((byte) 0, null);

        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store);


        pm.sendMessageSource.subscribe(new IObservableListener<Object>() {
            @Override
            public void update(Object arg) {
                if (arg instanceof TransmissionRequest) {
                    TransmissionRequest request = (TransmissionRequest) arg;
                    if (request.isRequesting) {
                        numRequested[0] = request.messageIdentifiers.size();
                    }
                }
            }

            @Override
            public void error(Exception e) {
                throw new RuntimeException("NETBC");
            }
        });

        responder.run();

        ArrayList<byte[]> receivedIdentifierList = new ArrayList<>();
        receivedIdentifierList.add(new byte[] { 1 });
        pm.packetReceiveSource.update(new TransmissionRequest(false, receivedIdentifierList));

        assertEquals(numRequested[0], 1);
    }

    @Test
    public void WillOfferMessagesOnRun() throws Exception {
        final TransmissionRequest[] requested = new TransmissionRequest[] { null };

        MockPacketManager pm = new MockPacketManager();
        IMessageStore store = getStore((byte) 0, null);

        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store);

        pm.sendMessageSource.subscribe(new IObservableListener<Object>() {
            @Override
            public void update(Object arg) {
                if (arg instanceof TransmissionRequest) {
                    TransmissionRequest request = (TransmissionRequest) arg;
                    requested[0] = request;
                }
            }

            @Override
            public void error(Exception e) {
                throw new RuntimeException("NETBC");

            }
        });

        responder.run();

        assertEquals(requested[0].isRequesting, false);
        assertEquals(requested[0].messageIdentifiers.size(), 1);
        assertArrayEquals(requested[0].messageIdentifiers.get(0), store.getStoredMessageIdentifiers().get(0));

    }
}
