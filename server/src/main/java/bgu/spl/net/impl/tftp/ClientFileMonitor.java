package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ClientFileMonitor {

    private int downloadMonitor;
    private FileOutputStream uploadFile;
    private FileInputStream downloadFile;
    public boolean finishedUpload;
    public boolean finishedDownloading;
    public short blockNumber;



    public ClientFileMonitor(){
        blockNumber = -1;
        finishedUpload=false;
        finishedDownloading=false;
        downloadMonitor = 0;
    }
    
    public int getDownloadMonitor(){
        return downloadMonitor;
    }
    
    public void incrementDownloadMonitor(){
        downloadMonitor++;
    }
    
    public void setUploadFile(byte[] nextPacket, int packetSize){
        try {
            uploadFile.write(nextPacket);
            blockNumber++;
            if(packetSize<512){
                finishedUploading();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    public void setNewUploadFile(File file){
        blockNumber = 0;
        try {
            uploadFile = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setDownloadFile(FileInputStream file){
        this.downloadFile = file;
    }

    public FileInputStream getDownloadFile(){
        return downloadFile;
    }
    
    public void finishedUploading(){
        try {
            uploadFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finishedUpload=true;
    }

    public short getBlockNumber(){
        return blockNumber;
    }

    public byte[] getNextPacket(){
        byte[] dataOpCode = {0, 3};
        byte[] nextPacket = new byte[512];
        try {
            int size = downloadFile.read(nextPacket);
            if (size == -1){
                downloadMonitor = 0;
                downloadFile.close();
                return null;
            }
            if(size < 512){
                nextPacket = Arrays.copyOf(nextPacket, size);
                finishedDownloading=true;
            }
            byte[] sizeBytes = new byte[] {(byte) (size>>8),(byte)(size&0xff)};
            byte[] blockNumberBytes = new byte[] {(byte) ((downloadMonitor+1)>>8),(byte)((downloadMonitor+1)&0xff)};
            incrementDownloadMonitor();
            return TftpProtocol.concatenateByteArrays(dataOpCode, sizeBytes, blockNumberBytes, nextPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
