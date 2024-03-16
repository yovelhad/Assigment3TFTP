package bgu.spl.net.impl.tftp;

import java.io.*;
import java.net.Socket;

public class TftpClient {
    //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    public static void main(String[] args) throws IOException {
        // 2 threads:
        // 1. listener - conenct to the server's socket
        // 2. keyboard listener

        if (args.length == 0) {
            args = new String[]{"localhost"};
        }

        //BufferedWriter automatically using UTF-8 encoding
        try (Socket sock = new Socket(args[0], 7777);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {
            String filesFolderPath = System.getProperty("user.dir");


            final InputListener<byte[]> inputListener = new InputListener<>(sock, new TftpEncoderDecoder(), new TftpProtocol(filesFolderPath));

             execute(inputListener);


            // while loop - receive input from keyboard

            // work with encdec to process the inputted messages

            System.out.println("sending message to server");
            out.write(args[1]);
            out.newLine();
            out.flush();

        }
    }

    private static void execute(InputListener<byte[]> handler) {
        new Thread(handler).start();
    }
}
