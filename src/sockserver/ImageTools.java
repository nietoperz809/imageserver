/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockserver;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

/**
 *
 * @author Administrator
 */
public class ImageTools
{
//    private byte[] reduceImg(File path) throws Exception
//    {
//        BufferedImage image = ImageIO.read(path);
//        BufferedImage i2 = resizeImage(image);
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        ImageIO.write(i2, "jpg", os);
//        return os.toByteArray();
//    }

    /**
     * Creates Image copy of new size
     *
     * @param originalImage Input image
     * @param b With
     * @param h Height
     * @return new Image
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int b, int h)
    {
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(b, h, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, b, h, null);
        g.dispose();
        return resizedImage;
    }
    
    /**
     * Reduces image quality 
     * @param path Path of jpeg file
     * @param qual Quality 0.0 ... 1.0
     * @return byte array of jpeg data
     * @throws Exception 
     */
    public static byte[] reduceImg(File path, float qual) throws Exception
    {
        BufferedImage image = ImageIO.read(path);
        image = resizeImage(image, 100, 100);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName("jpeg").next();

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(qual); // Change this, float between 0.0 and 1.0

        writer.setOutput(ImageIO.createImageOutputStream(os));
        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();
        return os.toByteArray();
    }
}
