//Package
package com.example;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

class Main {

     public static void main(String[] args) {

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

     static String converter (String s) {
        return new String(Base64.getDecoder().decode(s));
     }

}
