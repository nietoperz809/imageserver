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
import java.util.ArrayList;
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

    public Client(Socket s)
    {
        m_sock = s;
        m_thread = new Thread(this);
        m_thread.start();
    }

    private void sendHeader(PrintWriter out, String txt, String type)
    {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Length: " + txt.getBytes().length);
        out.println("Content-Type: " + type + "; charset=utf-8");
        out.println();
    }

    protected void html(PrintWriter out, String txt)
    {
        txt = "<html>" + txt + "</html>";
        sendHeader(out, txt, "text/html");
        out.print(txt);
    }

    protected String listFiles()
    {
        File f = new File(Sockserver.PATH);
        StringBuilder sb = new StringBuilder();
        File[] fils = f.listFiles();

        if (fils == null)
        {
            return "noone";
        }

        sb.append("<a href=\"").append("BACK*").append("\">");
        sb.append("*BACK*").append("</a>").append("</br>\r\n");

        for (int n = 0; n < fils.length; n++)
        {
            String name = fils[n].getName();
            if (fils[n].isDirectory())
            {
                sb.append("<a href=\"").append("LINK*").append(name).append("\">");
                sb.append(name).append("</a>").append("</br>\r\n");
            }
            else if (name.endsWith(".jpg"))
            {
                sb.append("<img src=\"");
                sb.append(name);
                sb.append("\" alt=\"Smiley face\" height=\"256\" width=\"256\">\r\n");
            }
        }
        return sb.toString();
    }

    /**
     * Send JPEG to output stream
     *
     * @param out output stream
     * @param fname file name
     */
    private void sendJpeg(OutputStream out, String fname)
    {
        File f = new File(Sockserver.PATH + fname);
        System.out.println("send: " + Sockserver.PATH + fname);
        PrintWriter w = new PrintWriter(out);
        w.println("HTTP/1.1 200 OK");
        w.println("Content-Length: " + f.length());
        w.println("Content-Type: image/jpeg");
        w.println();
        w.flush();
        try
        {
            InputStream input = new FileInputStream(f);
            byte[] b = new byte[(int) f.length()];
            input.read(b);
            out.write(b);
            out.flush();
        }
        catch (Exception ex)
        {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * send image page
     *
     * @param out Print Writer
     */
    protected void imagePage(PrintWriter out)
    {
        String txt = "<html>\r\n" + listFiles() + "</html>";
        sendHeader(out, txt, "text/html");
        out.print(txt);
    }

    private String[] getInput(BufferedReader in)
    {
        try
        {
            String[] out = in.readLine().split(" ");
            out[1] = java.net.URLDecoder.decode(out[1], "UTF-8");
            return out;
        }
        catch (Exception ex)
        {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void naked(PrintWriter out, String txt)
    {
        sendHeader(out, txt, "text/text");
        out.print(txt);
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
                    html(out, "hello wÖrldx");
                    break;

                case "/img":
                    imagePage(out);
                    break;

                default:
                    String fname = si[1].substring(1);
                    if (fname.startsWith("LINK*"))
                    {
                        Sockserver.PATH = Sockserver.PATH + fname.substring(5) + "/";
                        System.out.println(Sockserver.PATH);
                        imagePage(out);
                    }
                    else
                    {
                        sendJpeg(m_sock.getOutputStream(), fname);
                        //image (new PrintWriter(System.out), fname);
                    }
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
