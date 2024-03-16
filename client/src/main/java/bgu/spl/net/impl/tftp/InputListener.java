package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class InputListener<T> implements Runnable, Closeable {

    private final Socket sock;
    private final MessageEncoderDecoder<T> encdec;
    private final MessagingProtocol<T> protocol;

    public InputListener(Socket sock, MessageEncoderDecoder<T> encdec, MessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = encdec;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            BufferedInputStream in = new BufferedInputStream(sock.getInputStream());

            while (!protocol.shouldTerminate() && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        // TODO: implement me
    }
}
