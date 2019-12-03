package reversi.server;
/**
 * Represents the server side of the Reversi game.
 * @author: Mike Cao
 * @date: 11/4/18
 * @assignment: Lab 6 ReversiClient.java
 */

import reversi.Reversi;
import reversi.ReversiException;
import reversi.ReversiProtocol;
import reversi.Reversi.Move;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.security.InvalidParameterException;

public class ReversiServer implements ReversiProtocol {

    /** The reversi board of size DIM x DIM. */
    private static Reversi board = null;
    private static ServerSocket serverSocket;
    private static Socket socket1;
    private static Socket socket2;
    /** Used for sending a message to the first client. */
    private static PrintWriter out1;
    /** Used for receiving an input from the first client. */
    private static BufferedReader in1;
    /** Used for sending a message to the second client. */
    private static PrintWriter out2;
    /** Used for receiving an input from the second client. */
    private static BufferedReader in2;
    /** Declares an input variable for the methods. */
    private static String input;
    /** Variable to hold the winner */
    private static Reversi.Move winner;
    /** Constructor */
    private static ReversiServer server;
    /** port number */
    private static int port;
    /** Dimensions of the board */
    private static int DIM;

    /**
     * Creates a new board of size DIM x DIM.
     * @param DIM
     */
    public ReversiServer(int DIM) {
        board = new Reversi(DIM);
    }

    /**
     * Initializes the board in both clients. Establishes the connection from the server to both clients. Prints the board once to both clients.
     * @param clientNum
     * @param port
     * @param DIM
     */
    public void init(int clientNum, int port, int DIM) {
        try {
            if (clientNum == 1) {
                System.out.println("Waiting for player one...");
                socket1 = serverSocket.accept();
                out1 = new PrintWriter(socket1.getOutputStream(), true);
                in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
                out1.print(CONNECT + " " + DIM + "\n");
                out1.print(board.toString());
                out1.flush();
            } else {
                System.out.println("Waiting for player two...");
                socket2 = serverSocket.accept();
                out2 = new PrintWriter(socket2.getOutputStream(), true);
                in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
                out2.print(CONNECT + " " + DIM + "\n");
                out2.print(board.toString());
                out2.flush();
            }
            System.out.println("Player " + clientNum + " connected!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes a response from the clients that specifies the row and column of the cell that they want to put their piece in. Prints the board to both clients afterwards.
     * @param response
     */
    public void move(String response) {
        int row;
        int col;

        String[] splitResponse = response.split(" ");
        row = Integer.parseInt(splitResponse[1]);
        col = Integer.parseInt(splitResponse[2]);

        try {
            board.makeMove(row, col);
        } catch (ReversiException e) {
            e.printStackTrace();
        }

        out1.println(MOVE_MADE + " " + row + " " + col);
        out2.println(MOVE_MADE + " " + row + " " + col);

        out1.print(board.toString());
        out2.print(board.toString());

        out1.flush();
        out2.flush();
    }

    /**
     * Messages the clients whether or not they won, tied, or lost.
     */
    public void messageWinner() {
        winner = board.getWinner();
        if (winner == Move.PLAYER_ONE) {
            out1.println(GAME_WON);
            out2.println(GAME_LOST);
        } else if (winner == Move.PLAYER_TWO) {
            out1.println(GAME_LOST);
            out2.println(GAME_WON);
        } else {
            out1.println(GAME_TIED);
            out2.println(GAME_TIED);
        }
    }

    /**
     * Creates the new server socket and calls the appropriate methods by initializing the boards, messaging the clients
     * to make a move, and moves according to the response. Repeats until a winner is found, in which case the
     * messageWinner is called and the sockets close.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        try {
            DIM = Integer.parseInt(args[0]);
            port = Integer.parseInt(args[1]);
            server = new ReversiServer(DIM);
            serverSocket = new ServerSocket(port);
        } catch (InvalidParameterException e) {
            System.err.println("Usage: java ReversiServer DIM port");
            System.exit(1);
        }
        server.init(1, port, DIM);
        server.init(2, port, DIM);
        System.out.println("Starting game!");

        try {
            while (!board.gameOver()) {
                out1.println(MAKE_MOVE);
                input = in1.readLine();
                server.move(input);

                out2.println(MAKE_MOVE);
                input = in2.readLine();
                server.move(input);
            }
            server.messageWinner();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to ReversiClient");
            System.exit(1);
        } finally {
            serverSocket.close();
            socket1.close();
            socket2.close();
        }

    }
}