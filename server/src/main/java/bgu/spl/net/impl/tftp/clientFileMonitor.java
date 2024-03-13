package bgu.spl.net.impl.tftp;

public class clientFileMonitor {
    final int clientId;
    public int downloadMonitor;
    public int uploadMonitor;
    public clientFileMonitor(int clientId){
        this.clientId = clientId;
        downloadMonitor = 0;
        uploadMonitor = 0;
    }
    public int getDownloadMonitor(){
        return downloadMonitor;
    }
    public int getUploadMonitor(){
        return uploadMonitor;
    }
    public int getClientId(){
        return clientId;
    }
    public void incrementDownloadMonitor(){
        downloadMonitor++;
    }
    public void incrementUploadMonitor(){
        uploadMonitor++;
    }
}
