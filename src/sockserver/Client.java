


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    private final String PATH = "F:/feet/";
    
    public Client(Socket s)
    {
        m_sock = s;
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
    
    protected void html (PrintWriter out, String txt)
    {
        txt = "<html>"+txt+"</html>";
        sendHeader (out, txt, "text/html");
        out.print (txt);
    }
    
    protected String listFiles()
    {
        File f = new File (PATH);
        StringBuilder sb = new StringBuilder();
        File[] fils = f.listFiles();
        
        if (fils == null)
            return "noone";
        
        for (int n=0; n<fils.length; n++)
        {
            String name = fils[n].getName();
            if (name.endsWith(".jpg") == false)
                continue;
            sb.append ("<img src=\"");
            sb.append (name);
            sb.append ("\" alt=\"Smiley face\" height=\"256\" width=\"256\">\r\n");
        }
        return sb.toString();
    }
    
    protected void image (OutputStream out, String fname)
    {
        File f = new File (PATH+fname);
        PrintWriter w = new PrintWriter(out);
        w.println ("HTTP/1.1 200 OK");
        w.println ("Content-Length: "+f.length());
        w.println ("Content-Type: image/jpeg");
        w.println ();
        w.flush();
        try
        {
            InputStream input = new FileInputStream(f);
            byte[] b = new byte[(int)f.length()];
            input.read(b);
            out.write(b);
            out.flush();
        }
        catch (Exception ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void imagePage (PrintWriter out)
    {
        String txt = "<html>\r\n"+listFiles()+"</html>";
        sendHeader (out, txt, "text/html");
        out.print (txt);
    }
    
    private String[] getInput (BufferedReader in) throws IOException
    {
        String[] out =  in.readLine().split (" ");
        out[1] = java.net.URLDecoder.decode(out[1], "UTF-8");
        return out;
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
            String[] si = getInput(in);
            System.out.println(si[1]);
            switch (si[1])
            {
                case "/":
                html (out, "hello wÖrldx");
                break;
                
                case "/img":
                imagePage (out);
                break;
                
                default:
                String fname = si[1].substring(1);
                image (m_sock.getOutputStream(), fname);
                //image (new PrintWriter(System.out), fname);
                break;
                    
                case "/stop":
                System.exit(0);
                break;
            }
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
