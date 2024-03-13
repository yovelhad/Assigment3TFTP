package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Set;

public class TftpServer{

    public static void main(String[] args) throws IOException {
        // 1. Get the address (host:port) of the server
        String serverIP = args[0];  // 132.4.5.6
        int serverPort = Integer.parseInt(args[1]); // 7000
        Socket socket = new Socket(serverIP, serverPort);
        String msg = "Hello World";
        for (byte b : msg.getBytes()){
            socket.getOutputStream().write(b);
        socket.getOutputStream().write('0');  // indicate end of message (for the case of TCP)
        socket.close();
        }

    }

}
