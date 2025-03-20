package dal.data.ocr;

import dal.data.Languages;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;

public class OCR {
    public static String read(BufferedImage image, Languages lang) {
        Tesseract tesseract = new Tesseract();
        try {
            // Set the path to tessdata (change this to your local path)
            tesseract.setDatapath("./tessdata/");
            tesseract.setLanguage(Languages.getTessCode(lang));

            // Perform OCR while keeping only letters, numbers, common punctuation, and whitespace
            return tesseract.doOCR(image).replaceAll("[^\\p{L}\\p{N}.,;:'\"!?()\\[\\]\\-\\s]", "");
        } catch (TesseractException e) {
            System.err.println("Error: " + e.getMessage());
            return "";
        }
    }
}
