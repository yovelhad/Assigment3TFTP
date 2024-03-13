package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public void setNewUploadFile(String filePathName){
        blockNumber = 0;
        try {
            uploadFile = new FileOutputStream(filePathName, true);
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
        byte[] dataOpCode = new byte[6];
        byte[] nextPacket = new byte[512];
        try {
            downloadFile.skip(downloadMonitor*512);
            int size = downloadFile.read(nextPacket);
            incrementDownloadMonitor();
            if(size ==-1){
                finishedDownloading=true;
            }
            return TftpProtocol.concatenateByteArrays(dataOpCode, nextPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
