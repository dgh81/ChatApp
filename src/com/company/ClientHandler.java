package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    // ClientHandler kører i 1. tråd på "serveren"
    public ClientHandler(Socket socket) {
        // opsætning af klient
        try {
            this.socket = socket;
            // streams er bytes, writers/readers er chars.
            // writer = vi sender
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // reader = klient sender
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // lyt efter "ny linje" i konsollen og sæt til clientUsername
            this.clientUsername = bufferedReader.readLine();
            // tilføj ny klient til arrayliste
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        } catch (IOException e) {
            closeClientHandler(socket, bufferedReader, bufferedWriter);
        }
    }

    // run kører i 2. tråd i ClientHandler
    @Override
    public void run() {
        // Her lytter vi efter beskeder. At lytte er en "blocking operation",
        // dvs. vi kan ikke fortsætte med at chatte før nogen skriver... Derfor kører vi lytningen i sin egen tråd.
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                // her stopper koden indtil der er en linje i bufferen (hvis man ikke bruger Threads/Runnable)
                messageFromClient = bufferedReader.readLine();
                // kald på broadcast og medsend besked
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeClientHandler(socket, bufferedReader, bufferedWriter);
                // while loop breaker/stopper hvis klienten forlader programmet
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                // modtag chat fra alle undtagen os selv
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    // tilføj Enter tryk til besked
                    clientHandler.bufferedWriter.newLine();
                    // bufferen skrives ikke før den er fuld. tvinges til at blive sendt med flush
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeClientHandler(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler () {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }
    public void closeClientHandler(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        Client.closeClient(socket, bufferedReader, bufferedWriter);
    }
}

    /*public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                //InputStreamReader lukkes automatisk når wrapperen bufferedReader lukkes
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                //OutputStreamWriter lukkes automatisk når wrapperen bufferedWriter lukkes
                bufferedWriter.close();
            }
            if (socket != null) {
                //socket.GetOutputStream lukkes automatisk når wrapperen socket lukkes
                //socket.GetInputStream lukkes automatisk når wrapperen socket lukkes
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/