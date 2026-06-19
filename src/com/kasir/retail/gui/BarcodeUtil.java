package com.kasir.retail.gui;

import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Vector;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

/**
 * Utility class for decoding barcodes from {@link BufferedImage} using ZXing.
 */
public class BarcodeUtil {
    /**
     * Decode a barcode from the given image.
     * @param img BufferedImage containing a barcode.
     * @return decoded text, or null if not found.
     */
    public static String decode(BufferedImage img) {
        if (img == null) return null;
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(img);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Vector<BarcodeFormat> formats = new Vector<>();
            formats.add(BarcodeFormat.EAN_13);
            formats.add(BarcodeFormat.EAN_8);
            formats.add(BarcodeFormat.UPC_A);
            formats.add(BarcodeFormat.UPC_E);
            formats.add(BarcodeFormat.CODE_39);
            formats.add(BarcodeFormat.CODE_93);
            formats.add(BarcodeFormat.CODE_128);
            formats.add(BarcodeFormat.QR_CODE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
            Result result = reader.decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
