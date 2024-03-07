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
        if(nextByte == '0'){
            byte[] ret = new byte[bytes.size()];
            for( int i = 0; i<bytes.size(); i++){
                ret[i] = bytes.get(i);
            }
            bytes.clear();
            return ret;
        }
        bytes.add(nextByte);
        return null;
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