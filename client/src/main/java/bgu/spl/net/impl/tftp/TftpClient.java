package bgu.spl.net.impl.tftp;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TftpClient {
    //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    public static void main(String[] args) throws IOException {
        String ip = args.length == 3 ? args[1] : "127.0.0.1";
        int port = args.length == 3 ? Integer.parseInt(args[2]) : 7777;

        // all the connection to the server is done in the ConnectionHandler, so we open the socket in it
        ConnectionHandler connectionHandler = new ConnectionHandler(ip, port);

        // did you learn about this one?
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Thread for listening to the server
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Optional<byte[]> receivedData;
                try {
                    receivedData = Optional.ofNullable(connectionHandler.receive());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // I think all of the processing should be done in the protocol, so we should call the protocol's process method here?
                receivedData.ifPresent(data -> {
                    System.out.println("Received: " + Arrays.toString(data));
                    // Call the listen function here
                });
            }
        });

        // Thread for reading the standard input
        executor.submit(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String[] split = input.split(" ");
                String command = split[0];
                String[] cmdArgs = Arrays.copyOfRange(split, 1, split.length);
                try {
                    handleCommand(command, cmdArgs, connectionHandler);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        executor.shutdown();

    }

    public static void handleCommand(String command, String[] args, ConnectionHandler connectionHandler) throws IOException {
        switch (command) {
            case "LOGRQ":
                if (args.length != 1 || args[0].isEmpty()) {
                    throw new IllegalArgumentException("Invalid command");
                }
                byte[] opcode = new byte[]{0x0, 0x7};
                byte[] usernameBytes = args[0].getBytes();
                byte[] message = new byte[opcode.length + usernameBytes.length + 1];
                System.arraycopy(opcode, 0, message, 0, opcode.length);
                System.arraycopy(usernameBytes, 0, message, opcode.length, usernameBytes.length);
                connectionHandler.send(message);
                // todo: we need to wait for the response from the server
                break;
            case "RRQ":
                // Handle RRQ command
                // ... rest of the RRQ handling code ...
                break;
            case "WRQ":
                // Handle WRQ command
                // ... rest of the WRQ handling code ...
                break;
            case "DELRQ":
                // Handle DELRQ command
                // ... rest of the DELRQ handling code ...
                break;
            case "DIRQ":
                // Handle DIRQ command
                // ... rest of the DIRQ handling code ...
                break;
            case "DISC":
                // Handle DISC command
                // ... rest of the DISC handling code ...
                break;
            default:
                throw new IllegalArgumentException("Invalid command");
        }
    }
}
