package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.net.DatagramSocket;
import java.util.HashMap;

public class TftpServer implements Connections<T> {
    HashMap<Integer, ConnectionHandler> clientsMap;
    DatagramSocket socket;
    public TftpServer(){
        clientsMap = new HashMap<>();
        int port = Integer.parseInt(args[0]);
        socket = new DatagramSocket(port);
    }
    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        clientsMap.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler currentClient = clientsMap.get(connectionId);
        socket.send(T);

        return false;
    }

    @Override
    public void disconnect(int connectionId) {

    }

    //TODO: Implement this
    public void send(T msg){

    }

}
