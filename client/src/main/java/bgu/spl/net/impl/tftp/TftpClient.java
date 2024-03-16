package bgu.spl.net.impl.tftp;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TftpClient {

    public static void main(String[] args) throws IOException {
        String ip = args.length == 3 ? args[1] : "127.0.0.1";
        int port = args.length == 3 ? Integer.parseInt(args[2]) : 7777;

        // all the connection to the server is done in the ConnectionHandler, so we open the socket in it
        ConnectionHandler connectionHandler = new ConnectionHandler(ip, port);

        Thread listeningThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Optional<byte[]> receivedData;
                try {
                    receivedData = Optional.ofNullable(connectionHandler.receive());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                receivedData.ifPresent(data -> {
                    System.out.println("Received: " + Arrays.toString(data));
                });
            }
        });

        Thread keyboardThread = new Thread(() -> {
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

        listeningThread.start();
        keyboardThread.start();

    }

    public static void handleCommand(String command, String[] args, ConnectionHandler connectionHandler) throws IOException {
        byte[] opcode;
        byte[] message;
        switch (command) {
            case "LOGRQ":
                if (args.length != 1 || args[0].isEmpty()) {
                    throw new IllegalArgumentException("Invalid command");
                }
                opcode = new byte[]{0x0, 0x7};
                byte[] usernameBytes = args[0].getBytes();
                message = new byte[opcode.length + usernameBytes.length + 1];
                System.arraycopy(opcode, 0, message, 0, opcode.length);
                System.arraycopy(usernameBytes, 0, message, opcode.length, usernameBytes.length);
                connectionHandler.send(message);
                break;

            case "DELRQ":
                if(args.length!=1 || args[0].isEmpty()){
                    throw new IllegalArgumentException("Invalid command");
                }
                opcode = new byte[]{0x0, 0x8};
                byte[] fileToDelete = args[0].getBytes();
                message = new byte[opcode.length + fileToDelete.length+1];
                System.arraycopy(opcode,0,message,0,opcode.length);
                System.arraycopy(fileToDelete, 0, message, opcode.length, fileToDelete.length);
                connectionHandler.send(message);
                break;

            case "RRQ":
                if(args.length!=1 || args[0].isEmpty()){
                    throw new IllegalArgumentException("Invalid command");
                }
                opcode = new byte[]{0x0, 0x1};
                byte[] fileToDownload = args[0].getBytes();
                message = new byte[opcode.length + fileToDownload.length+1];
                System.arraycopy(opcode,0,message,0,opcode.length);
                System.arraycopy(fileToDownload, 0, message, opcode.length, fileToDownload.length);
                connectionHandler.send(message);
                break;

            case "WRQ":
                if(args.length!=1 || args[0].isEmpty()){
                    throw new IllegalArgumentException("Invalid command");
                }
                opcode = new byte[]{0x0, 0x2};
                byte[] fileToUpload = args[0].getBytes();
                message = new byte[opcode.length + fileToUpload.length+1];
                System.arraycopy(opcode,0,message,0,opcode.length);
                System.arraycopy(fileToUpload, 0, message, opcode.length, fileToUpload.length);
                connectionHandler.send(message);
                break;

            case "DIRQ":
                if(args.length!=0){
                    throw new IllegalArgumentException("Invalid command");
                }
                opcode = new byte[]{0x0,0x6};
                connectionHandler.send(opcode);
                break;

            case "DISC":
                if(args.length!=0){
                    throw new IllegalArgumentException("Invalid command");
                }
                opcode = new byte[]{0x0,0x10};
                connectionHandler.send(opcode);
                break;

            default:
                throw new IllegalArgumentException("Invalid command");
        }
    }
}