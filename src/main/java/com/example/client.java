package com.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

public class client {
    public static void main(String[] args) {
        String hostname = "0.0.0.0";
        int port = 8080;

        new Thread(() -> {

            try (Socket socket = new Socket(hostname, port)) {

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String time = reader.readLine();

                System.out.println(time);

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
                            |File Received              |
                            `````````````````````````````
                        """;
                writer.println(tob64(reply));

            } catch (UnknownHostException ex) {

                System.out.println("Server not found: " + ex.getMessage());

            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
                System.out.println("""
                    Please make sure server is online &
                    All files are in place.
                                """);
            }
        }).start();


    }

    public static void downloader(String message) throws IOException {
        //Make file
        File file = new File(message);
        var fileOutputStream = new FileOutputStream(file);

        var fileBytes = Files.readAllBytes(file.toPath());

        var base64File = Base64.getEncoder().encodeToString(fileBytes);

        System.out.println("File written: "+base64File);
    }

    public static String tob64(String s){
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
}
