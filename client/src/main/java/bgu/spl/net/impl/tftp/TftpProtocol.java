package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;

import java.util.Arrays;

public class TftpProtocol implements MessagingProtocol<byte[]> {
    private boolean RRQ;
    private boolean DIRQ;
    String filesFolderPath;

    public TftpProtocol(String filesFolderPath){
        this.filesFolderPath = filesFolderPath;
        RRQ = false;
        DIRQ = false;
    }
    @Override
    public byte[] process(byte[] message) {
        if(message.length<2){
            return null;
        }
        short opcode = (short) (((short) message[0]) << 8 | (short) (message[1]));
        byte[] meatOfMessage = Arrays.copyOfRange(message, 2, message.length);
        switch (opcode){
            case 3: //data
                if(RRQ){

                }

            case 4://ack

            case 5://error

            case 9: //bcast




        }
        return null;

    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
