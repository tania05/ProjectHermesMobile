package ca.projecthermes.projecthermes.networking;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

import ca.projecthermes.projecthermes.networking.payload.Heartbeat;

public class Connection implements Runnable {

    private Socket _socket;
    private boolean _isServer;

    private InputStream _in;
    private ReentrantLock _inLock;

    private OutputStream _out;
    private ReentrantLock _outLock;

    private long _lastHeartbeat;

    private static final int HEARTBEAT_TIME = 2000; //30 seconds
    private static final int HEARTBEAT_SERVER_PENALTY = 10000; // Server will try and wait for client to send the heartbeat.
    private static final Charset CHARSET = Charset.forName("UTF-16");

    public Connection(Socket socket, boolean isServer) {
        _socket = socket;
        _isServer = isServer;

        _inLock = new ReentrantLock();
        _outLock = new ReentrantLock();
    }

    @Override
    public void run() {
        Log.d("hermes", "Connection thread running");

        _inLock.lock();
        _outLock.lock();
        try {
            _in = _socket.getInputStream();
            _out = _socket.getOutputStream();

            _lastHeartbeat = System.currentTimeMillis();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            _inLock.unlock();
            _outLock.unlock();
        }

        Thread readerThread = null;
        try {
            Log.d("hermes", "Starting reader connection loop");
            readerThread = startReaderLoop();


            Log.d("hermes", "Begin waiting for heartbeat expiration");
            // Begin Listen Loop
            heartbeatLoop();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            Log.i("hermes", "closing socket...");
            try {
                _socket.close();
            } catch (IOException innerException) {
                innerException.printStackTrace();
            }
            if (readerThread != null) {
                try {
                    readerThread.join();
                } catch (InterruptedException innerException) {
                    innerException.printStackTrace();
                }
            }
            Log.i("hermes", "Closed socket");
        }

    }

    private void sendMessageAsync(final byte[] message) throws IOException {
        _outLock.lock();
        try {
            Log.w("hermes", "Sending message... " + message.length);
            _out.write(message);
            _out.flush();
        } finally {
            _outLock.unlock();
        }
    }


    private void write(byte[] dest, int off, int val) {
        dest[off] = (byte)((val >> 24) & 0xFF);
        dest[off+1] = (byte)((val >> 16) & 0xFF);
        dest[off+2] = (byte)((val >> 8) & 0xFF);
        dest[off+3] = (byte)(val & 0xFF);
    }

    private void write(byte[] dest, int off, byte[] source) {
        System.arraycopy(source, 0, dest, off, source.length);
    }

    private int readInt(byte[] source, int off) {
        return (source[off] << 24) | (source[off+1] << 16) | (source[off+2] << 8) | source[off+3];
    }

    private void sendMessageAsync(String message) throws IOException {
        Log.w("hermes", "Sending message" + message);
        byte[] messageBytes = message.getBytes(CHARSET);

        byte[] outputBytes = new byte[messageBytes.length + 4];
        write(outputBytes, 0, messageBytes.length);
        write(outputBytes, 4, messageBytes);

        sendMessageAsync(outputBytes);
    }

    private void sendHeartbeat() throws IOException {
        sendMessageAsync(Packet.serialize(new Heartbeat(false)));
    }

    private byte[] waitForBytes(InputStream stream, int length) throws IOException {
        int readBytes = 0;
        byte[] ret = new byte[length];

        while (readBytes < length) {
            int result = stream.read(ret, readBytes, length - readBytes);
            if (result == -1) {
                return null;
            }
            readBytes += length;
        }

        return ret;
    }

    private void onPacketReceived(Object payload) throws IOException {
        _lastHeartbeat = System.currentTimeMillis();

        Log.i("hermes", "Received payload: " + payload);

        if (payload instanceof Heartbeat) {
            Heartbeat heartbeat = (Heartbeat) payload;
            Log.d("hermes", "have hearbeat, is reply? " + heartbeat.isReply);
            if (!heartbeat.isReply) {
                sendMessageAsync(Packet.serialize(new Heartbeat(true)));
            }
        }
    }

    private Thread startReaderLoop() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                _inLock.lock();
                try {
                    while (true) {
                        Log.d("hermes", "Waiting for message ...");
                        byte[] messageLengthBytes = waitForBytes(_in, 4);
                        if (messageLengthBytes == null) {
                            break;
                        }
                        int messageLength = readInt(messageLengthBytes, 0);
                        Log.d("hermes", "Reader got message length " + messageLength);

                        byte[] messageBytes= waitForBytes(_in, messageLength);
                        if (messageBytes == null) {
                            break;
                        }

                        Object payload = Packet.deserialize(new String(messageBytes, CHARSET));
                        onPacketReceived(payload);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    _inLock.unlock();
                }

                //TODO begin disconnect procedure. We have been disconnected from the other device for some reason.
                Log.e("hermes", "NYI: disconnect procedure");
            }
        });
        t.start();
        return t;
    }

    private void heartbeatLoop() throws InterruptedException, IOException {
        while (!_socket.isClosed()) {
            long timeTillNextHeartbeat = _lastHeartbeat + HEARTBEAT_TIME - System.currentTimeMillis();
            if (_isServer) {
                timeTillNextHeartbeat += HEARTBEAT_SERVER_PENALTY;
            }
            if (timeTillNextHeartbeat <= 0) {
                _lastHeartbeat = System.currentTimeMillis();
                sendHeartbeat();
            } else {
                Log.d("hermes", "Heartbeat sleep for " + timeTillNextHeartbeat);
                Thread.sleep(timeTillNextHeartbeat);
            }
        }
    }
}
