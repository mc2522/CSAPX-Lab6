package reversi.client;
/**
 * Represents the client side of the Reversi game.
 * @author: Mike Cao
 * @date: 11/4/18
 * @assignment: Lab 6 ReversiClient.java
 */

//import java.net.InetAddress;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.UnknownHostException;

import static reversi.ReversiProtocol.*;


public class ReversiClient {

    /** Socket for establishing the connection. */
    private static Socket socket;
    /** Used for receiving the input from the server. */
    private static BufferedReader in;
    /** Used for sending a message to the server. */
    private static PrintWriter out;
    /** Used for receiving the input from the console. */
    private static BufferedReader stdIn;

    /**
     * Prints the board everytime the server prints the board to the server.
     * @param DIMs
     */
    public void printBoard(String DIMs) {
         int line = 0;
         int DIM = Integer.parseInt(DIMs.substring(DIMs.length() - 1));
         try {
             while (line <= DIM) {
                 System.out.println(in.readLine());
                 line++;
             }
         } catch(Exception e) {
             e.printStackTrace();
         }
    }

    /**
     * Prints the message of the result if the server sends prints the results to the clients.
     * @param message
     */
    public void winner(String message) {
        switch (message) {
            case GAME_WON:
                System.out.println("You won! Yay!");
                break;
            case GAME_LOST:
                System.out.println("You lost! Boo!");
                break;
            case GAME_TIED:
                System.out.println("You tied! Meh!");
                break;
        }
    }

    /**
     * Prompts the user to enter their move if the server sends a message demanding the move. Gets the input from the
     * console and sends that to the server. Calls winner() if the message received is a result.
     * @return boolean on whether or not the game is finished.
     */
    public boolean move() {
        try {
            String[] secondInput, splitFirstInput;
            String firstInput = in.readLine();
            if (firstInput.equals(GAME_LOST) || firstInput.equals(GAME_WON) || firstInput.equals(GAME_TIED)) {
                winner(firstInput);
                return true;
            } else if (firstInput.equals(MAKE_MOVE)) {
                System.out.print("YOUR TURN! Enter row column: ");
                out.println(MOVE + " " + stdIn.readLine());
                secondInput = in.readLine().split(" ");
                System.out.println("A move has been made in row " + secondInput[1] + " column " + secondInput[2]);
                return false;
            } else {
                splitFirstInput = firstInput.split(" ");
                System.out.println("A move has been made in row " + splitFirstInput[1] + " column " + splitFirstInput[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates a new socket with the hostname and the port.
     * Prints the board and prompts the user for their move and repeats until someone wins.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ReversiClient client = new ReversiClient();
        //InetAddress host = InetAddress.getLocalHost(); ==> host.getHostName()
        String hostname;
        int port;

        try {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
            socket = new Socket(hostname, port);
        } catch(Exception e) {
            System.err.println("Usage: java ReversiClient hostname port");
            System.exit(1);
        }

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            {
                String DIM = in.readLine();
                boolean isRunning = true;
                boolean finished;
                while(isRunning) {
                    client.printBoard(DIM);
                    finished = client.move();
                    if(finished == true)
                        isRunning = false;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host name");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to ReversiServer");
            System.exit(1);
        } finally {
            socket.close();
        }
    }
}
