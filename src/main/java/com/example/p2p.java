package com.example;

//Imports
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

public class p2p {
    //main
    public static void main(String[] args) throws InterruptedException {

        System.out.println("""
                To send files type: S
                To receive type: R
                """);

        try(Scanner inp = new Scanner(System.in)){
            String choice = inp.next();

            if (choice.contains("S".toLowerCase()))
                new Thread(() -> new Server().accept()).start();

            if (choice.contains("R".toLowerCase()))
                new Thread(() -> {
                    try {
                        new Client().connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            new Client().reconnect();
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }).start();
        };


    }

    //Server Side
    //Sends file
    static class Server{

        void accept(){
            int port = 8080;

            try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"))) {

                System.out.println("Server is listening on port " + port);

                while (true) {
                    try {
                        //Main setup
                        Socket socket = serverSocket.accept();

                        System.out.println("New client connected");

                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);

                        System.out.println("Enter File path: ");

                        Scanner scn = new Scanner(System.in);
                        String filePath = scn.next();

                        sender(filePath);

                        //Set up Reader
                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    }
                }

            } catch (Exception ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    //Client side
    //Receives File
    static class Client {

        void connect() throws InterruptedException {
            String hostname = "0.0.0.0";
            int port = 8080;

            try (Socket socket = new Socket(hostname, port)) {

                System.out.println("Starting client");

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String time = reader.readLine();

                System.out.println(time);

                //Send reply
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                String reply =
                        """
                            ____________________________
                            |Time received.             |
                            |Decoded encoded time       |
                            `````````````````````````````
                        """;
                writer.println(Base64.getEncoder().encodeToString(reply.getBytes(StandardCharsets.UTF_8)));

            } catch (Exception ex) {

                System.out.println("Error: " + ex.getMessage());
                reconnect();
            }
        }

        void reconnect() throws InterruptedException {
            System.out.println("""
                        Could not connect to a server.
                        Trying again in 5s.
                    """);
            Thread.sleep(5000);
            connect();
        }

    }

    public static void sender(String message) throws IOException {
        //Make file
        File file = new File(message);

        var fileBytes = Files.readAllBytes(file.toPath());

        var base64File = Base64.getEncoder().encodeToString(fileBytes);


        System.out.println("File sent: "+base64File);
        System.out.println("File type: "+Files.probeContentType(file.toPath()));
    }

    public static String tob64(String s){
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    static String converter (String s) {
        return new String(Base64.getDecoder().decode(s));
    }

}
