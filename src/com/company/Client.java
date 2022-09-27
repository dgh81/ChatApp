package com.company;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            closeClient(socket, bufferedReader, bufferedWriter);
        }
    }

    // vi sender - bruger Writer - loop
    public void sendMessage() {
        try {
            // bruges til "SERVER: username has entered the chat!" - køres kun 1 gang
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // bruges til resterende chats - loopes
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                //System.out.print(username + ": ");
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeClient(socket, bufferedReader, bufferedWriter);
        }
    }

    // vi modtager - bruger Reader - tråd / loop
    public void listenForMessage() {
        // Køres i ny tråd, så vi kan lytte sideløbende med at sende. Runnable erstattet med lamda
        new Thread(() -> {
            String msgFromGroupChat;
            while(socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    System.out.println(msgFromGroupChat);
                } catch (IOException e) {
                    closeClient(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public static void closeClient(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the groupchat: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }

}
