package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.util.LinkedList;
import java.util.List;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder
    private List<Byte> bytes = new LinkedList<Byte>();


    @Override
    //messages are just filenames in UTF8
    public byte[] decodeNextByte(byte nextByte) {
        // TODO: implement this
        if(bytes.size()>=2) {
            short opcode = (short) ((short) bytes.get(0) & 0xFF << 8 | (short) bytes.get(1) & 0xFF);
            switch (opcode) {

                case 1:
                case 2:
                case 5:
                case 7:
                case 8:
                case 9:
                    if (nextByte == 0) {
                        return listToArray();
                    }
                    break;

                case 3:
                    if(bytes.size()>4) {
                        short packetSize = (short) ((short) bytes.get(2) & 0xFF << 8 | (short) bytes.get(3) & 0xFF);//only used for data packets
                        if (bytes.size() == packetSize + 6) {
                            return listToArray();
                        }
                    }
                    break;

                case 4:
                    if (bytes.size() == 4) {
                        return listToArray();
                    }
                    break;

                case 6:
                case 10:
                    return listToArray();

            }
        }
        bytes.add(nextByte);
        return null;
    }
    public byte[] listToArray(){
        byte[] ret = new byte[bytes.size()];
        for( int i = 0; i<bytes.size(); i++){
            ret[i] = bytes.get(i);
        }
        bytes.clear();
        return ret;
    }

    @Override
    public byte[] encode(byte[] message) {
        byte[] ret = new byte[message.length+1];
        for(int i = 0; i<ret.length; i++){
            ret[i]=message[i];
        }
        ret[ret.length-1] = 0;
        return ret;
        //TODO: implement this
    }
}