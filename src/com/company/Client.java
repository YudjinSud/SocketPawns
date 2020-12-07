package com.company;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    public static void main(String []args) throws IOException{
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

        ServerConnection serverConn = new ServerConnection(socket);

        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter( socket.getOutputStream(), true);

        new Thread(serverConn).start();

        while(true) {
            System.out.println(">");
            String command = keyboard.readLine();
            if(command.equals("quit")) break;
            out.println(command);
        }
        socket.close();
        System.exit(0);

    }
}

class ServerConnection implements Runnable{

    private Socket server;
    private BufferedReader in;
    private PrintWriter out;

    public ServerConnection(Socket s) throws IOException {
        server = s;
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        out = new PrintWriter(server.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            while(true) {
                String serverResponse = in.readLine();
                Move move = new Gson().fromJson(serverResponse, Move.class);
                System.out.println("What client parsed from server  :" + move.move);
                if(serverResponse == null) break;
                System.out.println("[SERVER] recieved " + serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
