/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockserver;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Administrator
 */
public class Sockserver
{
    public static void main(String[] args) throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(80);
//        Socket clientSocket = serverSocket.accept();
//        PrintWriter out
//                = new PrintWriter(clientSocket.getOutputStream(), true);
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(clientSocket.getInputStream()));
        
        while (true)
        {
            Socket sock = serverSocket.accept();
            new ImageClient (sock);
        }
    }
}
