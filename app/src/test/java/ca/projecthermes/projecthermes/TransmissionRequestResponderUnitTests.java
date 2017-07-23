package ca.projecthermes.projecthermes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import ca.projecthermes.projecthermes.mock.networking.MockPacketManager;
import ca.projecthermes.projecthermes.networking.responder.TransmissionRequestResponder;
import ca.projecthermes.projecthermes.networking.payload.Message;
import ca.projecthermes.projecthermes.networking.payload.TransmissionRequest;
import ca.projecthermes.projecthermes.data.IMessageStore;
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
                            new byte[] { testIdentifier },
                            new byte[] { 0x00 },
                            new byte[] { 0x00 },
                            new byte[] { 0x01, 0x02, 0x03 },
                            new byte[] { 0x01, 0x02, 0x03 },
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

        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store, new Source<byte[]>());


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

        assertEquals(0, numRequested[0]);
    }

    @Test
    public void WillRequestAMessageItDoesNotHave() throws Exception {
        final int[] numRequested = new int[] { -1 };

        MockPacketManager pm = new MockPacketManager();
        IMessageStore store = getStore((byte) 0, null);

        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store, new Source<byte[]>());


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

        assertEquals(1, numRequested[0]);
    }

    @Test
    public void WillOfferMessagesOnRun() throws Exception {
        final TransmissionRequest[] requested = new TransmissionRequest[] { null };

        MockPacketManager pm = new MockPacketManager();
        IMessageStore store = getStore((byte) 0, null);

        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store, new Source<byte[]>());

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

        assertEquals(false, requested[0].isRequesting);
        assertEquals(1, requested[0].messageIdentifiers.size());
        assertArrayEquals(store.getStoredMessageIdentifiers().get(0), requested[0].messageIdentifiers.get(0));
    }

    @Test
    public void WillGiveAMessageIfAsked() throws Exception {
        final Message[] messageSent = new Message[] { null };

        MockPacketManager pm = new MockPacketManager();
        IMessageStore store = getStore((byte) 0, null);

        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store, new Source<byte[]>());


        pm.sendMessageSource.subscribe(new IObservableListener<Object>() {
            @Override
            public void update(Object arg) {
                if (arg instanceof Message) {
                    messageSent[0] = (Message) arg;
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
        pm.packetReceiveSource.update(new TransmissionRequest(true, receivedIdentifierList));

        assertArrayEquals(store.getStoredMessageIdentifiers().get(0), messageSent[0].identifier) ;
        assertArrayEquals(store.getMessageForIdentifier(store.getStoredMessageIdentifiers().get(0)).body, messageSent[0].body);
        assertArrayEquals(store.getMessageForIdentifier(store.getStoredMessageIdentifiers().get(0)).verifier, messageSent[0].verifier);
    }

    @Test
    public void WillGiveMultipleMessagesIfAsked() throws Exception {
        final ArrayList<Message> messageSent = new ArrayList<>();

        MockPacketManager pm = new MockPacketManager();
        IMessageStore store =  new IMessageStore() {

            @Override
            public ArrayList<byte[]> getStoredMessageIdentifiers() {
                ArrayList<byte[]> stored = new ArrayList<>();
                stored.add(new byte[] { 0 });
                stored.add(new byte[] { 1 });
                return stored;
            }

            @Override
            public Message getMessageForIdentifier(byte[] identifier) {
                if (Arrays.equals(identifier, new byte[] { 0 })) {
                    return new Message(
                            new byte[] { 0 }, // identifier
                            new byte[] { 0 }, // verifier
                            new byte[] { 0 },
                            new byte[] { 0, 0, 0 }, // body
                            new byte[] { 0, 0, 0 }, // body
                            new byte[] { 0, 0, 0 } // body
                    );

                } else if (Arrays.equals(identifier, new byte[] { 1 })) {
                    return new Message(
                            new byte[] { 1 },
                            new byte[] { 0 },
                            new byte[] { 0 },
                            new byte[] { 1, 1, 1 },
                            new byte[] { 1, 1, 1 },
                            new byte[] { 1, 1, 1 }
                    );
                } else {
                    return null;
                }
            }

            @Override
            public void storeMessage(Message m) {
            }
        };

        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store, new Source<byte[]>());


        pm.sendMessageSource.subscribe(new IObservableListener<Object>() {
            @Override
            public void update(Object arg) {
                if (arg instanceof Message) {
                    messageSent.add((Message) arg);
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
        receivedIdentifierList.add(new byte[] { 1 });
        pm.packetReceiveSource.update(new TransmissionRequest(true, receivedIdentifierList));

        assertEquals(2, messageSent.size());
        boolean identifierZeroExists = false;
        boolean identifierOneExists = false;

        for (int i = 0; i < messageSent.size(); i++) {
            Message m = messageSent.get(i);
            if (Arrays.equals(m.identifier, new byte[] { 0 })) {
                identifierZeroExists = true;
            } else if (Arrays.equals(m.identifier, new byte[] { 1 })) {
                identifierOneExists = true;
            }
        }

        assertTrue(identifierZeroExists);
        assertTrue(identifierOneExists);

    }

    @Test
    public void WillStoreAMessageItDoesNotHave() throws Exception {
        final boolean[] wasStored = new boolean[] { false };

        Source<Message> storeSource = new Source<>();
        IMessageStore store = getStore((byte) 0, storeSource);
        final Message sentMessage = new Message(
                new byte[] { 0x01 },
                new byte[] { 0x00 },
                new byte[] { 0x00 },
                new byte[] { 0x00, 0x01, 0x02, 0x03 },
                new byte[] { 0x00, 0x01, 0x02, 0x03 },
                new byte[] { 0x00, 0x01, 0x02, 0x03 }
        );

        storeSource.subscribe(new IObservableListener<Message>() {
            @Override
            public void update(Message arg) {
                if (arg.equals(sentMessage)) {
                    wasStored[0] = true;
                }
            }

            @Override
            public void error(Exception e) {

            }
        });

        MockPacketManager pm = new MockPacketManager();
        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store, new Source<byte[]>());

        responder.run();

        pm.packetReceiveSource.update(sentMessage);
        assertTrue(wasStored[0]);
    }

    public void WillSendAMessageWhenOneIsAdded() {
        final Message[] sent = new Message[] { null };

        MockPacketManager pm = new MockPacketManager();
        IMessageStore store = getStore((byte) 0, null);


        Source<byte[]> messageAddedSource = new Source<>();
        TransmissionRequestResponder responder = new TransmissionRequestResponder(new NullLogger(), pm, store, messageAddedSource);

        pm.sendMessageSource.subscribe(new IObservableListener<Object>() {
            @Override
            public void update(Object arg) {
                if (arg instanceof Message) {
                    sent[0] = (Message) arg;
                }
            }

            @Override
            public void error(Exception e) {
                throw new RuntimeException("NETBC");

            }
        });

        responder.run();

        byte[] identifier = store.getStoredMessageIdentifiers().get(0);
        Message expected = store.getMessageForIdentifier(identifier);

        messageAddedSource.update(identifier);


        assertEquals(expected.identifier, sent[0].identifier);
        assertEquals(expected.body, sent[0].body);
        assertEquals(expected.verifier, sent[0].verifier);
    }
}
