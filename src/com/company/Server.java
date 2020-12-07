package com.company;

import com.google.gson.Gson;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 8080;
    private static int playerNo = 1;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws IOException{

        System.out.println();

        File moves = new File("moves.txt");
        if(moves.exists()) {
            moves.delete();
        }
        System.out.println("[SERVER] Waiting for players...");
	    ServerSocket server = new ServerSocket((PORT));
        while(true) {
            Socket connection = server.accept();
            ClientHandler clientThread = new ClientHandler(connection, clients, playerNo);
            clients.add(clientThread);
            JOptionPane.showMessageDialog(null, "[Server] Player " + playerNo++ + " connected.");
            pool.execute(clientThread);
        }
    }
}


class Move {
    String move;
    String color;
}



class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<ClientHandler> clients;
    private int id;
    private String color;
    private int moveCount = 0;
    private int points = 0;
    Board board = new Board();

    public boolean checkGameStatus() {
        if(points == 8) {
            if(id == 1) {
                System.out.println("White win!");
            }else {
                System.out.printf("Black win!\n");
            }
            System.out.println("All pawns of player are destroyed !\n");
            System.exit(0);
            return true;
        }
        return false;
    }


    private void makeMove(String cmd) {
        String move = cmd;
        try {
            board.performMove(move, color, true);
            checkWinning(move);
            writeToCoords(cmd);
            System.out.println(board);
            moveCount++;
        } catch (IOException e) {
            System.out.println("You can't do this move!");
            e.printStackTrace();
        }
    }


    private void checkWinning(String cmd) {
        int[] moveArray = Board.parseInput(cmd);
        if(moveArray[1] != moveArray[3]) {
            System.out.println("Eating happened\n");
            points++;
        }
        else if(moveArray[2] == 7) {
            System.out.println("White win!\n");
            System.exit(0);
        }
        else if(moveArray[2] == 0) {
            System.out.println("Black win!\n");
            System.exit(0);
        }
    }


    public void writeToCoords(String move) {
        try {
            FileWriter fw = new FileWriter("moves.txt", true);
            fw.write(move + "," + color + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawBoard(){
        String line;
        board = new Board();
        try (BufferedReader br = new BufferedReader(new FileReader("moves.txt"))) {
            while ((line = br.readLine()) != null) {
                String[] line_content = line.split(",");
                String move = line_content[0];
                String color = line_content[1];
                System.out.println(move + "-" + color);
                board.performMove(move, color, true);
            }
            System.out.println(board);
        } catch (IOException e) {
            if(moveCount > 1) {
                e.printStackTrace();
            }
        }
    }


    public ClientHandler(Socket socketForClient, ArrayList<ClientHandler> clients, int id) throws IOException {
        this.socket = socketForClient;
        this.clients = clients;
        this.id = id;
        this.color = id == 1 ? "white" : "black";
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out =new PrintWriter(socket.getOutputStream(), true);
    }


    private void sayToAll(String msg) {
        for(ClientHandler player : clients) {
            player.out.println(msg);
        }
    }

    @Override
    public void run() {
        try {
            while (!checkGameStatus()) {
                System.out.println("Player ID :: " + id);
                String msg = in.readLine();
                System.out.println("[SERVER] recieved :" + msg);
                drawBoard();
                makeMove(msg);
                Move mv = new Move();
                mv.move = msg;
                mv.color = color;
                String json = new Gson().toJson(mv);
                sayToAll(json);
                System.out.println(json);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

