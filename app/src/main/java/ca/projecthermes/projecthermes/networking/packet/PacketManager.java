package ca.projecthermes.projecthermes.networking.packet;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import ca.projecthermes.projecthermes.IHermesLogger;
import ca.projecthermes.projecthermes.networking.payload.IPayload;
import ca.projecthermes.projecthermes.util.IObservable;
import ca.projecthermes.projecthermes.util.IObservableListener;
import ca.projecthermes.projecthermes.util.Null;
import ca.projecthermes.projecthermes.util.Source;


//TODO: We are creating more threads than are necessary. That should probably be sorted out.
public class PacketManager implements Runnable, IPacketManager {

    private final InputStream _inputStream;
    private final OutputStream _outputStream;
    private final IHermesLogger _logger;
    private final IPacketSerializer _packetSerializer;

    private final Source<Null> _disconnectSource;
    private final Source<IPayload> _packetReceiveSource;

    private final Object _threadControlLock = new Object();

    private boolean _running;
    private boolean _isOpen;
    private Thread _readerThread;

    private static final Charset CHARSET = Charset.forName("UTF-16");

    public PacketManager(
            @NotNull IHermesLogger logger,
            @NotNull InputStream inputStream,
            @NotNull OutputStream outputStream,
            @NotNull IPacketSerializer packetDeserializer
    ) {
        _logger = logger;
        _inputStream = inputStream;
        _outputStream = outputStream;
        _packetSerializer = packetDeserializer;

        _disconnectSource = new Source<>();
        _packetReceiveSource = new Source<>();
    }

    @Override
    public IObservable<Null> getDisconnectObservable() {
        return _disconnectSource;
    }

    @Override
    public IObservable<IPayload> getPacketReceiveObservable() {
        return _packetReceiveSource;
    }

    @Override
    public void run() {
        synchronized (_threadControlLock) {
            if (_running) {
                throw new RuntimeException("Tried to start the same PacketManager instance multiple times.");
            }
            _running = true;
            _isOpen = true;
            _readerThread = startReaderLoop();
        }
    }

    private void sendMessageAsync(final byte[] message) throws IOException {
        synchronized (_outputStream) {
            _outputStream.write(message);
            _outputStream.flush();
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
        int val = ((source[off] & 0xFF) << 24) | ((source[off+1] & 0xFF) << 16) | ((source[off+2] & 0xFF) << 8) | (source[off+3] & 0xFF);
        return val;
    }

    private void sendMessageAsync(String message) throws IOException {
        _logger.i("Sending string message: " + message);
        byte[] messageBytes = message.getBytes(CHARSET);

        byte[] outputBytes = new byte[messageBytes.length + 4];
        write(outputBytes, 0, messageBytes.length);
        write(outputBytes, 4, messageBytes);

        sendMessageAsync(outputBytes);
    }

    private void sendMessageAsync(IPayload payload) throws IOException {
        sendMessageAsync(_packetSerializer.serialize(payload));
    }

    @Override
    public Source<Null> sendMessage(final IPayload payload) {
        final Source<Null> source = new Source<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendMessageAsync(payload);
                    source.update(null);
                } catch (IOException e) {
                    source.error(e);
                }
            }
        }).start();

        source.subscribe(new IObservableListener<Null>() {
            @Override
            public void update(Null arg) {}

            @Override
            public void error(Exception e) {
                disconnect();
            }
        });

        return source;
    }

    private byte[] waitForBytes(InputStream stream, int length) throws IOException {
        int readBytes = 0;
        byte[] ret = new byte[length];

        while (readBytes < length) {
            int result = stream.read(ret, readBytes, length - readBytes);
            if (result == -1) {
                return null;
            }
            readBytes += result;
        }

        return ret;
    }

    private void onPacketReceived(IPayload payload) {
        _logger.i("Received payload: " + payload);
        _packetReceiveSource.update(payload);
    }

    private Thread startReaderLoop() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (_inputStream) {
                    try {
                        while (true) {
                            byte[] messageLengthBytes = waitForBytes(_inputStream, 4);
                            if (messageLengthBytes == null) {
                                break;
                            }
                            int messageLength = readInt(messageLengthBytes, 0);

                            byte[] messageBytes = waitForBytes(_inputStream, messageLength);
                            if (messageBytes == null) {
                                break;
                            }

                            IPayload payload = _packetSerializer.deserialize(new String(messageBytes, CHARSET));
                            onPacketReceived(payload);
                        }

                        if (Thread.interrupted()) {
                            _logger.i("Aborting read loop, disconnect process started.");
                        } else {
                            _logger.i("Aborting read loop, socket is disconnected");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                disconnect();
            }
        });
        t.start();
        return t;
    }

    @Override
    public void disconnect() {
        synchronized (_threadControlLock) {
            if (!_isOpen) return;

            _logger.i("Disconnect procedure invoked. Closing all sockets.");
            _readerThread.interrupt();
        }

        synchronized (_inputStream) {
            try {
                _inputStream.close();
            } catch (IOException e) {
                _logger.e("Exception while ensuring input stream closed:");
                _logger.e(e.toString());
            }
        }

        synchronized (_outputStream) {
            try {
                _outputStream.close();
            } catch (IOException e) {
                _logger.e("Exception while ensuring output stream closed:");
                _logger.e(e.toString());
            }
        }

        _disconnectSource.update(null);
    }
}
