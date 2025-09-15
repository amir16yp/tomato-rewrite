package tomato.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static BufferedImage loadQOI(String pathFromJar) {
        try {
            // Get the resource stream
            InputStream resourceStream = Utils.class.getResourceAsStream(pathFromJar);
            if (resourceStream == null) {
                throw new RuntimeException("RESOURCE NOT FOUND " + pathFromJar);
            }

            // Decode the image
            BufferedImage image = new QOIDecoder(toByteArray(resourceStream)).getImage();
            return image;
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}
