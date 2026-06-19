package lk.goldvault.backend.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class QrCodeUtil {

    private static final int QR_SIZE = 300;

    /**
     * Generates a QR code for the given content and returns it as a base64-encoded PNG string.
     * The frontend can render this directly as: <img src="data:image/png;base64,{value}">
     */
    public String generateQrBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);

            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }
}