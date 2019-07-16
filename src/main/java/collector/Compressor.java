package collector;

import lombok.Cleanup;
import utils.CheckDate;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

class Compressor {

    static File Compress(File imageFile) {
        File compressedImageFile = imageFile;
        try {
            compressedImageFile = new File(
                    imageFile.getParentFile().getAbsolutePath() + "\\" +
                            CheckDate.getTimeForArt() + "compressed.jpg");
            @Cleanup OutputStream os = new FileOutputStream(compressedImageFile);
            @Cleanup InputStream is = new FileInputStream(imageFile);
            @Cleanup ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No writers found");
            }
            ImageWriter writer = writers.next();
            // quality
            float quality = 0.6f;
            try {
                // create a BufferedImage as the result of decoding the supplied InputStream
                BufferedImage image = ImageIO.read(is);
                // resize
                int width = image.getWidth();
                int height = image.getHeight();
                double ratio = (double) height / (double) width;
                Image tmp = image.getScaledInstance(300, (int) (300 * ratio),
                        Image.SCALE_SMOOTH);
                image = new BufferedImage(300, (int) (300 * ratio),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                g2d.drawImage(tmp, 0, 0, null);
                g2d.dispose();
                // get all image writers for JPG format
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                // compress to a given quality
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
                // write out
                writer.write(null,
                        new IIOImage(image, null, null), param);
            } finally {
                // close all streams
                writer.dispose();
            }
        } catch (Exception e) {
            compressedImageFile.delete();
            compressedImageFile = null;
        }
        return compressedImageFile;
    }

}
