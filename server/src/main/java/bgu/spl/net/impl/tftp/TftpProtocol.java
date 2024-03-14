package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.io.*;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.IllegalBlockSizeException;

class holder{
    static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
    static List<String> activeUsers = new ArrayList<>();

    static Integer clientCounter = 0 ;

}
public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private int connectionId;
    private Connections<byte[]> connections;
    private boolean shouldTerminate = false;
    ClientFileMonitor clientMonitor;
    Path filesFolder;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId + holder.clientCounter;
        holder.clientCounter++;
        this.connections = connections;
        this.shouldTerminate=false;
        holder.ids_login.put(connectionId, false);
        Path filesFolder = Paths.get("Files");
        Set<Integer> ids_loginKeySets =  holder.ids_login.keySet();
        clientMonitor = new ClientFileMonitor();
        // TODO implement this
    }

    @Override
    public void process(byte[] message) {
        System.out.println("alksmdaskdlamsd");
        if(message.length<2){
            return;
        }
        short opcode = (short) (((short) message[0]) << 8 | (short) (message[1]));
        byte[] meatOfMessage = Arrays.copyOfRange(message, 2, message.length);
        if(opcode!=7  && !holder.ids_login.get(connectionId)){
            byte[] ERROR = {0,5,0,6};
            error(ERROR);
            return;
        }
        switch (opcode){
            case 1:
                readRequest(meatOfMessage);
                break;
            case 2:
                writeRequest(meatOfMessage);
                break;
            case 3:
                receiveDataPacket(meatOfMessage);
            case 4:
                receiveACK(meatOfMessage);
                break;
            case 6:
                directoryListingRequest();
                break;
            case 7:
                loginRequest(meatOfMessage);
                break;
            case 8:
                deleteFileRequest(meatOfMessage);
                break;
            case 10:
                shouldTerminate = true;
        }
    }


    private void broadcastFileAddedDeleted(byte[] message) {
        for(Integer id : holder.ids_login.keySet()){
            if(holder.ids_login.get(id)) {
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
        if(holder.ids_login.get(connectionId)){
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

    private void directoryListingRequest() {
        // Get list of filenames in the directory
        File directory = new File("Files");
        String[] filenames = directory.list();
        String directoryListing = "";
        if (filenames != null) {
            for (String filename : filenames) {
                directoryListing = directoryListing + filename + '0';
            }
        }
        byte[] directoryListingInBytes = directoryListing.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        connections.send(connectionId,directoryListingInBytes);
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


    private void writeRequest(byte[] message) { //checking if the file the client wishes to upload already exists
        String fileName = new String(message, StandardCharsets.UTF_8);
        File file = new File("Flies/" + fileName);
        if (file.exists()) {
            // File already exists, send error packet
            byte[] ERROR = {0,5,0,5};
            error(ERROR);
            return;
        }
        clientMonitor.setNewUploadFile("Flies/" + fileName);

        // Acknowledge WRQ with block number 0
        byte[] ACK = {0,4,0,0};
        acknowledgment(ACK);
    }


    private void readRequest(byte[] message) {
        String fileName = new String(message, StandardCharsets.UTF_8);
        String filePath = System.getProperty("user.dir") + "/server/Flies/";
        // Check if file exists
        File file = new File(filePath + fileName);
        if (!file.exists()) {
            // Send error packet if file not found
            byte[] ERROR = {0,5,0,1};
            error(ERROR);
            return;
        }
        // If file exists, send file data to client
        try {
            setAndSendFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void receiveACK(byte[] message) {
        short blockNumber = (short) (((short) message[0]) << 8 | (short) (message[1]));
        int currentBlockNumber = clientMonitor.getDownloadMonitor()-1;
        sendFile();
    }

    private void setAndSendFile(File file) throws IOException {
        FileInputStream fileToInputStream = new FileInputStream(file);
        clientMonitor.setDownloadFile(fileToInputStream);
        sendFile();

    }
    private void sendFile(){
        byte[] dataPacket = clientMonitor.getNextPacket();
        connections.send(connectionId, dataPacket);
    }


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
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

    private void receiveDataPacket(byte[] message) {
        short sizeOfPacket = (short) (((short) message[0]) << 8 | (short) (message[1]));    
        byte[] onlyData = Arrays.copyOfRange(message, 4, message.length);
        clientMonitor.setUploadFile(onlyData, sizeOfPacket);
        short blockNumber = clientMonitor.getBlockNumber();
        byte[] blockNumberBytes = new byte[]{(byte) (blockNumber>>8),(byte)(blockNumber&0xff)};
        byte[] ACK = new byte[]{0,4,blockNumberBytes[0],blockNumberBytes[1]};
        acknowledgment(ACK);
    }





    
}
