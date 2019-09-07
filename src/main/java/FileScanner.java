import java.io.*;


public class FileScanner {

    private File file;

    private String text;

    FileScanner(File file, String text) {
        this.file = file;
        this.text = text;
    }

    public boolean search() {
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
