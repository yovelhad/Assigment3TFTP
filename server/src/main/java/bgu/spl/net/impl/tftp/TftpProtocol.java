package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private final LoggedUsers loggedUsers;
    private int connectionId;
    private String username;
    private Connections<byte[]> connections;
    private boolean shouldTerminate = false;
    ClientFileMonitor clientMonitor;
    String filesFolderPath;

    public TftpProtocol(LoggedUsers loggedUsers, String filesFolderPath) {
        this.loggedUsers = loggedUsers;
        this.filesFolderPath = filesFolderPath;
    }

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.shouldTerminate=false;
        clientMonitor = new ClientFileMonitor();
    }

    @Override
    public void process(byte[] message) {
        System.out.println(message);
        if(message.length<2){
            return;
        }
        short opcode = (short) (((short) message[0]) << 8 | (short) (message[1]));
        System.out.println("opcode: " + opcode);
        byte[] meatOfMessage = Arrays.copyOfRange(message, 2, message.length);
        if(opcode!=7  && username == null) {
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
                break;
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
                handleDisc();
        }
    }

    private void handleDisc() {
        byte[] ACK = {0,4,0,0};
        acknowledgment(ACK);
        loggedUsers.logout(username);
        connections.disconnect(connectionId);
        shouldTerminate = true;
    }


    private void broadcastFileAddedDeleted(byte[] message) {
        // iterate over all logged in users and send the message
        for (Map.Entry<String, Integer> entry : loggedUsers.getLoggedUsers().entrySet()) {
            connections.send(entry.getValue(), message);
        }
    }

    private void deleteFileRequest(byte[] message) {
        String fileName = new String(message, StandardCharsets.UTF_8);
        Path filePathToDelete = Paths.get(filesFolderPath + fileName);
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
        if (username != null) {
            byte[] ERROR = {0,5,0,7};
            error(ERROR);
        } else{
            String ret = new String(message, StandardCharsets.UTF_8);
            boolean success = loggedUsers.login(ret, connectionId);
            if (!success) {
                byte[] ERROR = {0,5,0,7};
                error(ERROR);
            } else {
                username = ret;
                byte[] ACK = {0,4,0,0};
                acknowledgment(ACK);
            }
        }
    }

    private void directoryListingRequest() {
        // Get list of filenames in the directory
        File directory = new File(filesFolderPath);
        String[] filenames = directory.list();
        String directoryListing = "";
        if (filenames != null) {
            for (String filename : filenames) {
                directoryListing = directoryListing + filename + '0';
            }
        }
        byte[] directoryListingInBytes = directoryListing.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // construct and send the directory listing packet
        // 2 bytes - opcode: 3
        // 2 bytes - packet size (length of directoryListingInBytes)
        // 2 bytes - block number: 1
        // n bytes - directory listing

        byte[] opcode = new byte[] {0, 6}; // 2 bytes - opcode: 6 (for example)
        byte[] packetSize = new byte[] {(byte) (directoryListingInBytes.length >> 8), (byte) (directoryListingInBytes.length)}; // 2 bytes - packet size
        byte[] blockNumber = new byte[] {0, 1}; // 2 bytes - block number: 1

        // Use ByteBuffer to concatenate the byte arrays
        ByteBuffer buffer = ByteBuffer.allocate(opcode.length + packetSize.length + blockNumber.length + directoryListingInBytes.length);
        buffer.put(opcode);
        buffer.put(packetSize);
        buffer.put(blockNumber);
        buffer.put(directoryListingInBytes);

        connections.send(connectionId,buffer.array());
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
            errorMessageInBytes = (errorMessage).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ERROR = concatenateByteArrays(message,errorMessageInBytes);

        } else if (errorCode==7) {
            errorMessage ="User already logged in– Login username already connected.";
            errorMessageInBytes = (errorMessage).getBytes(java.nio.charset.StandardCharsets.UTF_8);
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
        File file = new File(filesFolderPath + fileName);
        if (file.exists()) {
            // File already exists, send error packet
            byte[] ERROR = {0,5,0,5};
            error(ERROR);
            return;
        }
        clientMonitor.setNewUploadFile(file);

        // Acknowledge WRQ with block number 0
        byte[] ACK = {0,4,0,0};
        acknowledgment(ACK);
    }


    private void readRequest(byte[] message) {
        String fileName = new String(message, StandardCharsets.UTF_8);
        // Check if file exists
        File file = new File(filesFolderPath + fileName);
        if (!file.exists()) {
            // Send error packet if file not found
            byte[] ACK = {0,4,0,1};
            acknowledgment(ACK);
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
        if (dataPacket == null) {
            return;
        }
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

    public static byte[] concatenateByteArrays(byte[]... arrays) {
        // Calculate the length of the result array
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        // Create the result array
        byte[] result = new byte[totalLength];

        // Copy the contents of each array into the result array
        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

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
