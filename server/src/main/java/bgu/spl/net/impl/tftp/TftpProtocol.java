package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class holder{
    static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
    static List<String> activeUsers = new ArrayList<>();

}
public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private int connectionId;
    private Connections<byte[]> connections;
    private boolean shouldTerminate = false;
    Path filesFolder;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.shouldTerminate=false;
        holder.ids_login.put(connectionId, false);
        Path filesFolder = Paths.get("Files");
        // TODO implement this
    }

    @Override
    public void process(byte[] message) {

        short opcode = (short) (((short) message[0]) << 8 | (short) (message[1]));
        byte[] meatOfMessage = Arrays.copyOfRange(message, 2, message.length-1);

        shouldTerminate = opcode==10;
        switch (opcode){
            case 1:
                readRequest(meatOfMessage);
                break;
            case 2:
                writeRequest(meatOfMessage);
                break;
            case 6:
                directoryListingRequest(meatOfMessage);
                break;
            case 7:
                loginRequest(meatOfMessage);
                break;
            case 8:
                deleteFileRequest(meatOfMessage);
                break;
            case 10:
                shouldTerminate();

        }
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }

    private void broadcastFileAddedDeleted(byte[] message) {
        for(Integer id : holder.ids_login.keySet()){
            if(holder.ids_login.get(id) == true) {
                connections.send(id,message);
            }
        }
    }

    private void deleteFileRequest(byte[] message) {
        String fileName = new String(message, StandardCharsets.UTF_8);
        Path filePathToDelete = filesFolder.resolve(fileName);
        if(Files.exists(filePathToDelete)){
            try{
                Files.delete(filePathToDelete);
                byte[] ACK = {0,4,0,0};
                acknowledgment(ACK);
                byte[] BCAST = concatenateByteArrays(new byte[] {0,9,0,0},message);
                broadcastFileAddedDeleted(BCAST);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        else{
            byte[] ERROR = {0,5,0,1};
            error(ERROR);
        }



    }

    private void loginRequest(byte[] message) {
        if(holder.ids_login.get(connectionId) == true){
            byte[] ERROR = {0,5,0,7};
            error(ERROR);
        }
        else{
            String ret = new String(message, StandardCharsets.UTF_8);
            holder.ids_login.put(connectionId,true);
            holder.activeUsers.add(ret);
            byte[] ACK = {0,4,0,0};
            acknowledgment(ACK);
        }

    }

    private void directoryListingRequest(byte[] message) {

    }

    private void error(byte[] message) {
        short errorCode = (short) (((short) message[2]) << 8 | (short) (message[3]));
        String errorMessage ="";
        byte[] errorMessageInBytes;
        byte[] ERROR;
        if(errorCode==1){
            errorMessage ="File not found– RRQ DELRQ of non-existing file.";
            errorMessageInBytes = (errorMessage + '0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        } else if (errorCode==2) {
            errorMessage ="Access violation– File cannot be written, read or deleted.";
            errorMessageInBytes = (errorMessage + '0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        } else if (errorCode==3) {
            errorMessage ="Disk full or allocation exceeded– No room in disk.";
            errorMessageInBytes = (errorMessage + '0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        } else if (errorCode==4) {
            errorMessage =" Illegal TFTP operation– Unknown Opcode.";
            errorMessageInBytes = (errorMessage + '0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        } else if (errorCode==5) {
            errorMessage ="File already exists– File name exists on WRQ.";
            errorMessageInBytes = (errorMessage + '0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        } else if (errorCode==6) {
            errorMessage =" User not logged in– Any opcode received before Login completes.";
            errorMessageInBytes = (errorMessage + '0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        } else if (errorCode==7) {
            errorMessage ="User already logged in– Login username already connected.";
            errorMessageInBytes = (errorMessage + '0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        }
        else{
            errorMessage ="Not defined, see error message (if any).";
            errorMessageInBytes = (errorMessage + '0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        }

        connections.send(connectionId,ERROR);

    }

    private void acknowledgment(byte[] message) {
        connections.send(connectionId,message);
    }

    private void dataPacket(byte[] message) {


    }

    private void writeRequest(byte[] message) {

    }

    private void readRequest(byte[] message) {
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
    }
    public static byte[] concatenateByteArrays(byte[] array1, byte[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;

        // Create a new array with the combined length
        byte[] result = new byte[length1 + length2];

        // Copy the contents of the first array
        System.arraycopy(array1, 0, result, 0, length1);

        // Copy the contents of the second array
        System.arraycopy(array2, 0, result, length1, length2);

        return result;
    }





    
}
