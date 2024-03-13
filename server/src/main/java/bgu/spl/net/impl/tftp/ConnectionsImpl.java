package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.util.HashMap;

public class ConnectionsImpl<T> implements Connections<T> {
    HashMap<Integer, BlockingConnectionHandler<T>> clientsMap; //holds each connected client
    public ConnectionsImpl(){
        clientsMap = new HashMap<Integer, BlockingConnectionHandler<T>>();
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        clientsMap.put(connectionId, (BlockingConnectionHandler<T>) handler);
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


    public void setSoTimeout(int connectionId, int timeOut) {

    }


//    public byte[] receive(int connectionId) {
//        BlockingConnectionHandler<T> currentClient = clientsMap.get(connectionId);
//        if(currentClient!=null){
//            try {
//                InputStream inputStream = currentClient.getSock().getInputStream();
//                byte[] buffer = new byte[512];
//                int byteRead = inputStream.read(buffer);
//                if(byteRead==0 || byteRead==-1){
//                    return null;
//                }
//                else{
//                    return buffer;
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//
//    }
}
