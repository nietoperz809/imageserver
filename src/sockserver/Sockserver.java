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
    public static String PATH = "F:\\pron\\010\\010/";
    
    public static void main(String[] args) throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(80);
        
        while (true)
        {
            Socket sock = serverSocket.accept();
            new Client (sock);
        }
    }
}
