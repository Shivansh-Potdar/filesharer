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
    public static void main(String[] args) {
        new Thread(() -> new Server().accept()).start();

        new Thread(()-> new Client().connect()).start();
    }

    //Server Side
    static class Server{
        void check(){

        }

        void accept(){
            int port = 8080;

            try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"))) {

                System.out.println("Server is listening on port " + port);

                boolean active = true;

                while (active) {
                    try {
                        //Main setup
                        Socket socket = serverSocket.accept();

                        System.out.println("New client connected");

                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);

                        writer.println(new Date().toString());

                        //Set up Reader
                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                        //Send echo
                        String echoText = socket.getInetAddress().getHostName()+":\n"+converter(reader.readLine());
                        System.out.println(echoText);

                        //Exit code
                        if(reader.readLine() != null){
                            String exitVal = reader.readLine();

                            if (exitVal.contentEquals("exit"))
                                active = false;
                        }

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
    static class Client {

        void connect(){
            String hostname = "0.0.0.0";
            int port = 8080;

            try (Socket socket = new Socket(hostname, port)) {

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String time = reader.readLine();

                System.out.println(time);

                String encodedTime = Base64.getEncoder().encodeToString(time.getBytes(StandardCharsets.UTF_8));

                System.out.println("Enter File path: ");

                Scanner scn = new Scanner(System.in);
                String filePath = scn.next();

                downloader(filePath);

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

            } catch (UnknownHostException ex) {

                System.out.println("Server not found: " + ex.getMessage());

            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
                System.out.println("""
                    Please make sure server is online &
                    All files are in place.
                                """);
            }
        }

        void send(){

        }
    }

    public static void downloader(String message) throws IOException {
        //Make file
        File file = new File(message);

        var fileBytes = Files.readAllBytes(file.toPath());

        var base64File = Base64.getEncoder().encodeToString(fileBytes);

        System.out.println("File written: "+base64File);
    }

    public static String tob64(String s){
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    static String converter (String s) {
        return new String(Base64.getDecoder().decode(s));
    }

}
