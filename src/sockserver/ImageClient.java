/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockserver;

import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Administrator
 */
public class ImageClient extends Client
{
    @Override
    protected void html (PrintWriter out, String txt)
    {
        out.println ("doof");
    }
    
    public ImageClient (Socket s)
    {
        super(s);
    }
}
