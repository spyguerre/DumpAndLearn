package dal.data.OCR;

import dal.graphic.Languages;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.io.File;

public class OCR {
    public static String read(Languages lang) {
        Tesseract tesseract = new Tesseract();
        try {
            // Set the path to tessdata (change this to your local path)
            tesseract.setDatapath("./tessdata/");
            tesseract.setLanguage(Languages.getTessCode(lang));

            // Provide the image file
            File imageFile = new File("./images/7.png");

            // Perform OCR
            String text = tesseract.doOCR(imageFile);
            System.out.println("Recognized Text: \n" + text);

            return text;
        } catch (TesseractException e) {
            System.err.println("Error: " + e.getMessage());
            return "";
        }
    }
}
