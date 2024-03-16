package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.Server;

public class TftpServer{

    public static void main(String[] args) {
        LoggedUsers loggedUsers = new LoggedUsers();
        String filesFolderPath = System.getProperty("user.dir") + "/server/Flies/";

        Server.threadPerClient(7777,
                ()-> new TftpProtocol(loggedUsers, filesFolderPath),
                TftpEncoderDecoder::new
        ).serve();

    }
}
