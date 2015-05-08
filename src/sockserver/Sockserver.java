/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Administrator
 */
public class Sockserver
{
    public static final String pStr = "F:\\";
    public static Path PATH = Paths.get(pStr);
 
    private static void pTest()
    {
        Path path = Paths.get("F:\\pron\\010\\010/");  // create
        System.out.println(path.toString());
        
        path = path.getParent();   // remove last elem
        System.out.println(path.toString());
        
        path = Paths.get(path.toString(), "hello");  // add elem
        System.out.println(path.toString());
    }
    
    private static void startServer() throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(80);
        
        while (true)
        {
            Socket sock = serverSocket.accept();
            new Client (sock);
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        //pTest();
        startServer();
    }
}
