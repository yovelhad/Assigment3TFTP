package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler {

    private Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private final MessageEncoderDecoder<byte[]> encdec;
    private final MessagingProtocol<byte[]> protocol;

    public ConnectionHandler(String ip, int port) throws IOException {
        this.sock = new Socket(ip, port);
        this.in = new BufferedInputStream(sock.getInputStream());
        this.out = new BufferedOutputStream(sock.getOutputStream());
        this.encdec = new TftpEncoderDecoder();
        this.protocol = new TftpProtocol(System.getProperty("user.dir"));
        System.out.println("Connected to the server!");
    }

    public void send(byte[] bytes) throws IOException {
        byte[] encodedBytes = encdec.encode(bytes);
        out.write(encodedBytes);
        out.flush();
    }

    public byte[] receive() throws IOException {
        int read;
        byte[] nextMessage = null;
        while ((read = in.read()) >= 0) {
            nextMessage = encdec.decodeNextByte((byte) read);
            if (nextMessage != null) {
                protocol.process(nextMessage);
            }
        }
        return nextMessage;
    }
}
