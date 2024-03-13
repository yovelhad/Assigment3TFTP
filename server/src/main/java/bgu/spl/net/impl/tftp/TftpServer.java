package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Set;

public class TftpServer{

    public static void main(String[] args) throws IOException {
        Server.threadPerClient(7777,
                ()-> new TftpProtocol(),
                TftpEncoderDecoder::new
        ).serve();

    }

}
