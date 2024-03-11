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

public class TftpServer implements Connections<byte[]> {

    HashMap<Integer, ConnectionHandler> clientsMap;
    DatagramSocket socket;
    int port;
    public TftpServer(int port){
        clientsMap = new HashMap<>();
        this.port = port;
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void connect(int connectionId, ConnectionHandler<byte[]> handler) {
        clientsMap.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, byte[] msg) {
        ConnectionHandler currentClient = clientsMap.get(connectionId);
        if(currentClient!=null) {
            currentClient.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId) {

    }

    //TODO: Implement this
    public void send(byte[] msg){
        Set<Integer> keys = clientsMap.keySet();
        for(Integer ID: keys){
            ConnectionHandler currentClient = clientsMap.get(ID);
            currentClient.send(msg);
        }
    }

    @Override
    public void setSoTimeout(int connectionId, int timeOut) {

    }

    @Override
    public void receive(int connectionId, DatagramPacket datagramPacket) {

    }
    public void start(){
        while(true){

        }

    }
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
