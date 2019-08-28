import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;


public class DefiniteFile {

    private File file;

    private String text;


    DefiniteFile(File file, String text) {
        this.file = file;
        this.text = text;
    }

    DefiniteFile(File file) {
        this.file = file;
    }

    public boolean manage() {
        boolean result = false;
        try(BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(file)))) {
            int symbol;
            int textStart = 0;
            while ((symbol = in.read())!= -1) {
                if ((char) symbol == text.charAt(textStart)) {
                    textStart++;
                    if (textStart == text.length()) {
                        result = true;
                        break;
                    }
                } else {
                    textStart = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }









}
