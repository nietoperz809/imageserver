/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class Client implements Runnable
{
    Socket m_sock;
    Thread m_thread;
    BufferedReader m_in;
    PrintWriter m_out;
    
    public Client(Socket s)
    {
        m_sock = s;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
            PrintWriter out = new PrintWriter(m_sock.getOutputStream(), true);)
        {
            m_in = in;
            m_out = out;
        }
        catch (Exception ex)
        {
            
        }
        m_thread = new Thread(this);
        m_thread.start();
    }

    private void sendHeader (PrintWriter out, String txt, String type)
    {
        out.println ("HTTP/1.1 200 OK");
        out.println ("Content-Length: "+txt.getBytes().length);
        out.println ("Content-Type: "+type+"; charset=utf-8");
        out.println ();
    }
    
    private void html (PrintWriter out, String txt)
    {
        txt = "<html>"+txt+"</html>";
        sendHeader (out, txt, "text/html");
        out.print (txt);
    }
    
    private void naked (PrintWriter out, String txt)
    {
        sendHeader (out, txt, "text/text");
        out.print (txt);
    }
    
    private void perform() throws Exception
    {
        try (BufferedReader in
                = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
                PrintWriter out
                = new PrintWriter(m_sock.getOutputStream(), true);)
        {
            System.out.println(in.readLine());
            html (out, "hello wÖrldx");
        }
    }

    @Override
    public void run()
    {
        try
        {
            perform();
            m_sock.close();
        }
        catch (Exception ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
