package tomato.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class QOIDecoder {
    private static final byte[] QOI_MAGIC = new byte[]{'q', 'o', 'i', 'f'};
    private static final int QOI_OP_RGB = 0xFE;
    private static final int QOI_OP_RGBA = 0xFF;
    private static final int QOI_OP_INDEX = 0x00;
    private static final int QOI_OP_DIFF = 0x40;
    private static final int QOI_OP_LUMA = 0x80;
    private static final int QOI_OP_RUN = 0xC0;
    private static final int QOI_MASK_2 = 0xC0;
    private static final byte[] QOI_PADDING = new byte[]{0, 0, 0, 0, 0, 0, 0, 1};

    private final int width;
    private final int height;
    private final byte channels;
    private final byte colorspace;
    private final BufferedImage image;
    private final int[] pixels;
    private final int[][] index = new int[64][4];
    private int run = 0;
    private int currentPixel = 0;

    public QOIDecoder(byte[] data) throws Exception {
        if (data == null || data.length < 14) {
            throw new Exception("Invalid QOI data: too short");
        }

        // Verify magic bytes
        for (int i = 0; i < QOI_MAGIC.length; i++) {
            if (data[i] != QOI_MAGIC[i]) {
                throw new Exception("Invalid QOI magic bytes");
            }
        }

        // Read header
        width = readInt32(data, 4);
        height = readInt32(data, 8);
        channels = data[12];
        colorspace = data[13];

        if (width <= 0 || height <= 0 || (channels != 3 && channels != 4)) {
            throw new Exception("Invalid QOI header values");
        }

        // Create BufferedImage
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        decode(data, 14);
    }

    private void decode(byte[] data, int startPos) throws Exception {
        int[] previousPixel = new int[]{0, 0, 0, 255};
        int pos = startPos;

        while (currentPixel < width * height && pos < data.length - QOI_PADDING.length) {
            if (run > 0) {
                run--;
                writePixel(previousPixel);
                continue;
            }

            int b1 = data[pos++] & 0xFF;
            if (b1 == QOI_OP_RGB) {
                previousPixel[0] = data[pos++] & 0xFF;
                previousPixel[1] = data[pos++] & 0xFF;
                previousPixel[2] = data[pos++] & 0xFF;
                writePixel(previousPixel);
            } else if (b1 == QOI_OP_RGBA) {
                previousPixel[0] = data[pos++] & 0xFF;
                previousPixel[1] = data[pos++] & 0xFF;
                previousPixel[2] = data[pos++] & 0xFF;
                previousPixel[3] = data[pos++] & 0xFF;
                writePixel(previousPixel);
            } else {
                int op = b1 & QOI_MASK_2;
                if (op == QOI_OP_INDEX) {
                    int indexPos = b1;
                    System.arraycopy(index[indexPos], 0, previousPixel, 0, 4);
                    writePixel(previousPixel);
                } else if (op == QOI_OP_DIFF) {
                    previousPixel[0] += ((b1 >> 4) & 0x03) - 2;
                    previousPixel[1] += ((b1 >> 2) & 0x03) - 2;
                    previousPixel[2] += (b1 & 0x03) - 2;
                    normalizeChannels(previousPixel);
                    writePixel(previousPixel);
                } else if (op == QOI_OP_LUMA) {
                    int b2 = data[pos++] & 0xFF;
                    int vg = (b1 & 0x3F) - 32;
                    int vr = vg + ((b2 >> 4) & 0x0F) - 8;
                    int vb = vg + (b2 & 0x0F) - 8;
                    previousPixel[0] += vr;
                    previousPixel[1] += vg;
                    previousPixel[2] += vb;
                    normalizeChannels(previousPixel);
                    writePixel(previousPixel);
                } else if (op == QOI_OP_RUN) {
                    run = (b1 & 0x3F);
                    writePixel(previousPixel);
                }
            }
        }

        // Verify padding
        for (int i = 0; i < QOI_PADDING.length; i++) {
            if (data[data.length - QOI_PADDING.length + i] != QOI_PADDING[i]) {
                throw new Exception("Invalid QOI padding");
            }
        }
    }

    private void writePixel(int[] pixel) {
        pixels[currentPixel] = (pixel[3] << 24) | (pixel[0] << 16) | (pixel[1] << 8) | pixel[2];

        // Update index
        int indexPosition = (pixel[0] * 3 + pixel[1] * 5 + pixel[2] * 7 + pixel[3] * 11) % 64;
        System.arraycopy(pixel, 0, index[indexPosition], 0, 4);

        currentPixel++;
    }

    private void normalizeChannels(int[] pixel) {
        for (int i = 0; i < 3; i++) {
            pixel[i] &= 0xFF;
        }
    }

    private int readInt32(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) |
                ((data[offset + 1] & 0xFF) << 16) |
                ((data[offset + 2] & 0xFF) << 8) |
                (data[offset + 3] & 0xFF);
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte getChannels() {
        return channels;
    }

    public byte getColorspace() {
        return colorspace;
    }

}