package sockserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
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

//    protected void html(PrintWriter out, String txt)
//    {
//        txt = "<html>" + txt + "</html>";
//        sendHeader(out, txt, "text/html");
//        out.print(txt);
//    }
    private String listFiles(String path)
    {
        File f = new File(path);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        File[] fils = f.listFiles();

        if (fils == null)
        {
            return "--- noone";
        }

        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<String> dirnames = new ArrayList<>();

        Path pp = Paths.get(path).getParent();
        if (pp != null)
        {
            sb2.append("<a href=\"").append(URLEncoder.encode(pp.toString())).append("\">");
            sb2.append("*BACK*").append("</a>").append("<hr>\r\n");
        }
        for (int n = 0; n < fils.length; n++)
        {
            String name = fils[n].getName();
            Path p = Paths.get(path, name);
            if (fils[n].isDirectory())
            {
                dirs.add(p.toString());
                dirnames.add(name);
            }
            else if (name.toLowerCase().endsWith(".jpg"))
            {
                sb.append("<a href=\"");
                sb.append("*IMG*");
                sb.append(URLEncoder.encode(p.toString()));
                sb.append("\" target=\"_blank\"><img src=\"");
                sb.append(URLEncoder.encode(p.toString()));
                sb.append("\"></a>\r\n");
            }
        }
        for (int n = 0; n < dirs.size(); n++)
        {
            sb2.append("<a href=\"").append(URLEncoder.encode(dirs.get(n))).append("\">");
            sb2.append(dirnames.get(n)).append("</a>").append("&nbsp;|&nbsp;\r\n");
        }
        sb2.append("<hr>");
        sb2.append(sb);
        return sb2.toString();
    }

    private void imgHead(PrintWriter w, int len)
    {
        w.println("HTTP/1.1 200 OK");
        w.println("Content-Length: " + len);
        w.println("Content-Type: image/jpeg");
        w.println("Cache-Control: max-age=31536000, public");
        //w.println("Expires: 06 Apr 2020 19:25:30 GMT");
        w.println("Connection: close");
        w.println();
    }

    /**
     * Send JPEG to output stream
     *
     * @param out output stream
     * @param fname file name
     */
    private void sendJpegSmall(OutputStream out, String path)
    {
        File f = new File(path);
        PrintWriter w = new PrintWriter(out);
        try
        {
            byte[] b = ImageTools.reduceImg(f, 0.2f);
            imgHead(w, b.length);
            out.write(b);
            w.flush();
            out.flush();
        }
        catch (Exception ex)
        {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendJpegOriginal(OutputStream out, String fname)
    {
        File f = new File(fname);
        PrintWriter w = new PrintWriter(out);
        try
        {
            InputStream input = new FileInputStream(f);
            byte[] b = new byte[(int) f.length()];
            input.read(b);
            imgHead(w, b.length);
            out.write(b);
            w.flush();
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
    private void imagePage(PrintWriter out, String path)
    {
        String txt = "<html>\r\n" + listFiles(path) + "</html>";
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
            if (si == null)
            {
                return;
            }
            String path = si[1].substring(1);
            System.out.println(path);
            if (path.toLowerCase().endsWith(".jpg"))
            {
                if (path.startsWith("*IMG*"))
                    sendJpegOriginal(m_sock.getOutputStream(), path.substring(5));
                else
                    sendJpegSmall(m_sock.getOutputStream(), path);
            }
            else
            {
                if (path.isEmpty())
                {
                    path = "F:\\";
                }
                imagePage(out, path);
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
