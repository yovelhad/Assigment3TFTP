package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, BlockingConnectionHandler<T>> clientsMap; //holds each connected client
    public ConnectionsImpl(){
        clientsMap = new ConcurrentHashMap<Integer, BlockingConnectionHandler<T>>();
    }

    @Override
    public void connect(int connectionId, BlockingConnectionHandler<T> handler) {
        clientsMap.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        BlockingConnectionHandler<T> currentClient = clientsMap.get(connectionId);
        if(currentClient!=null) {
            currentClient.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId) {
        clientsMap.remove(connectionId);

    }

}
