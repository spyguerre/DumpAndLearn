package dal.data.translation;

import dal.graphic.general.SettingsController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class Translator {
    public static String translate(String text) {
        return translate(text, true);
    }

    public static String translate(String text, boolean isForeignText) {
        // Python command to perform the translation using deep_translator package, depending on which side the user typed.
        String command;
        String nativeCode = SettingsController.getNativeCode();
        String foreignCode = SettingsController.getForeignCode();
        text = text.replace("'", "\\'").replace("\"", "\\\"").trim();  // Remove " and '
        text = text.replaceAll("[^\\x00-\\x7F]", ""); // Keep only ASCII characters
        if (!isForeignText) {
            System.out.println("Translating " + text + " from " + nativeCode + " to " + foreignCode + "...");
            command = "python -c \"import sys; sys.stdout.reconfigure(encoding='utf-8'); "
                    + "from deep_translator import GoogleTranslator; print(GoogleTranslator(source='"
                    + nativeCode + "', target='" + foreignCode
                    + "').translate('" + text + "'))\"";
        } else {
            System.out.println("Translating " + text + " from " + foreignCode + " to " + nativeCode + "...");
            command = "python -c \"import sys; sys.stdout.reconfigure(encoding='utf-8'); "
                    + "from deep_translator import GoogleTranslator; print(GoogleTranslator(source='"
                    + foreignCode + "', target='" + nativeCode
                    + "').translate('" + text + "'))\"";
        }

        try {
            // Run the Python command
            Process process = Runtime.getRuntime().exec(command);

            // Capture and print the python stderr for debugging.
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }

            // Capture the output using UTF-8 encoding (I hate accents and japanese characters).
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            line = reader.readLine();
            if (line != null) {
                System.out.println("Translation received: " + line);
                return line;
            } else {
                System.out.println("Couldn't get a translation.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
