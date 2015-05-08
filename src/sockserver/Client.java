package sockserver;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import javax.imageio.ImageIO;
import static sockserver.Sockserver.pStr;
import static sockserver.Sockserver.PATH;

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

    private BufferedImage resizeImage(BufferedImage originalImage)
    {
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(100, 100, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 100, 100, null);
        g.dispose();
        return resizedImage;
    }

    private byte[] reduceImg(File path) throws Exception
    {
        BufferedImage image = ImageIO.read(path);
        BufferedImage i2 = resizeImage(image);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(i2, "jpg", os);
        return os.toByteArray();
    }

//    private byte[] reduceImg(File path) throws Exception
//    {
//        BufferedImage image = ImageIO.read(path);
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName("jpeg").next();
//
//        ImageWriteParam param = writer.getDefaultWriteParam();
//        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//        param.setCompressionQuality(0.2f); // Change this, float between 0.0 and 1.0
//
//        writer.setOutput(ImageIO.createImageOutputStream(os));
//        writer.write(null, new IIOImage(image, null, null), param);
//        writer.dispose();
//        return os.toByteArray();
//    }
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
        File f = new File(Sockserver.PATH.toString());
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        File[] fils = f.listFiles();

        if (fils == null)
        {
            return "noone";
        }

        ArrayList<String> dirs = new ArrayList<>();

        sb2.append("<a href=\"").append("BACK*").append("\">");
        sb2.append("*BACK*").append("</a>").append("<hr>\r\n");

        for (int n = 0; n < fils.length; n++)
        {
            String name = fils[n].getName();
            if (fils[n].isDirectory())
            {
                dirs.add(name);
            }
            else if (name.endsWith(".jpg"))
            {
                sb.append("<a href=\"");
                sb.append("*IMG*");
                sb.append(URLEncoder.encode(name));
                sb.append("\" target=\"_blank\"><img src=\"");
                sb.append(name);
                sb.append("\"></a>\r\n");
            }
        }
        for (String dir : dirs)
        {
            sb2.append("<a href=\"").append("LINK*").append(URLEncoder.encode(dir)).append("\">");
            sb2.append(dir).append("</a>").append("&nbsp;|&nbsp;\r\n");
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
    private void sendJpegSmall(OutputStream out, String fname)
    {
        File f = new File(Sockserver.PATH.toString() + "/" + fname);
        PrintWriter w = new PrintWriter(out);
        try
        {
            byte[] b = reduceImg(f);
            imgHead (w, b.length);
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
        File f = new File(Sockserver.PATH.toString() + "/" + fname);
        PrintWriter w = new PrintWriter(out);
        try
        {
            InputStream input = new FileInputStream(f);
            byte[] b = new byte[(int) f.length()];
            input.read(b);
            imgHead (w, b.length);
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
    private void imagePage(PrintWriter out)
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
            if (si == null)
            {
                return;
            }
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
                    if (fname.startsWith("BACK*"))
                    {
                        Path ph = Sockserver.PATH.getParent();
                        if (ph != null)
                        {
                            Sockserver.PATH = ph;
                        }
                        imagePage(out);
                    }
                    else if (fname.startsWith("*IMG*"))
                    {
                        sendJpegOriginal(m_sock.getOutputStream(), fname.substring(5));
                    }
                    else if (fname.startsWith("LINK*"))
                    {
                        Sockserver.PATH = Paths.get(Sockserver.PATH.toString(), fname.substring(5));
                        imagePage(out);
                    }
                    else if (fname.startsWith("reset"))
                    {
                        PATH = Paths.get(pStr);
                        imagePage(out);
                    }
                    else
                    {
                        sendJpegSmall(m_sock.getOutputStream(), fname);
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
