package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;

import java.io.*;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private final Connections<T> connections;
    private int connectionId;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, Connections<T> connections, int connectionId) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void run() {
        try (InputStream in = sock.getInputStream();
             OutputStream out = sock.getOutputStream()) {


            connections.connect(connectionId, this);
            protocol.start(connectionId, connections);

            while (!Thread.currentThread().isInterrupted() && !protocol.shouldTerminate()) {

                byte nextByte = in.readNBytes(1)[0];
                T msg = encdec.decodeNextByte(nextByte);
                if (msg != null) {
                    protocol.process(msg);
                }

            }

        } catch (IOException ex) {
        }

        connections.disconnect(connectionId);

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        //IMPLEMENT IF NEEDED
    }
}
