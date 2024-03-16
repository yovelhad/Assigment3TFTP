package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {
    private final ConcurrentHashMap<Integer, ConnectionHandler<T>> clientsMap; //holds each connected client
    public ConnectionsImpl(){
        clientsMap = new ConcurrentHashMap<>();
    }

    @Override
    public void connect(int connectionId, BlockingConnectionHandler<T> handler) {
        clientsMap.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> currentClient = clientsMap.get(connectionId);
        if(currentClient!=null) {
            currentClient.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId) {
        ConnectionHandler<T> currentClient = clientsMap.remove(connectionId);
        if(currentClient!=null) {
            try {
                currentClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
