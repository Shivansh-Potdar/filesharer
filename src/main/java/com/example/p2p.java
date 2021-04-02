package com.example;

//Imports
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
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
               new Server().accept();

            if (choice.contains("R".toLowerCase()))
                new Client().connect();
        };


    }

    //Server Side
    //Sends file
    static class Server{

        void accept(){
            int port = 8080;

            try (ServerSocket serverSocket = new ServerSocket(port, 1, InetAddress.getByName("0.0.0.0"))) {

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

                        writer.println(sender(filePath));

                        //Set up Reader
                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        System.out.println(converter(reader.readLine()));

                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                        reload();
                    }
                }

            } catch (Exception ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
                reload();
            }
        }

        void reload(){
            accept();
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

                String fileR = reader.readLine();

                System.out.println(fileR);

                download(fileR);

                //Send reply
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                String reply =
                        """
                            ____________________________
                            |File received.             |
                            |Decoded encoded file       |
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

        void download(String s) throws FileNotFoundException {
            var fileBytes = Base64.getDecoder().decode(s.getBytes(StandardCharsets.UTF_8));

            try (var stream = new FileOutputStream("src\\main\\resources\\output\\final")) {
                stream.write(fileBytes);
                System.out.println("File saved");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String sender(String message) throws IOException {
        //Make file
        File file = new File(message);

        var fileBytes = Files.readAllBytes(file.toPath());

        var base64File = Base64.getEncoder().encodeToString(fileBytes);


        System.out.println("File sent: "+base64File);
        System.out.println("File type: "+Files.probeContentType(file.toPath()));

        return base64File;
    }

    static String converter (String s) {
        return new String(Base64.getDecoder().decode(s));
    }

}
