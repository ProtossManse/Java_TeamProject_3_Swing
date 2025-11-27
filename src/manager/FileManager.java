package manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FileManager {

    public static String read(String path) {
        String data = "";
        File file = new File(path);

        checkFile(file);

        try (Scanner scanner = new Scanner(file);) {
            scanner.useDelimiter("\\Z");
            if (scanner.hasNext()) {
                data = scanner.next();
            }
        } catch (
                FileNotFoundException ignored) {
        } catch (NoSuchElementException e) {
            return data;
        }

        return data;
    }

    private static void checkFile(File file) {
        if (!file.exists()) {
            File parentDir = file.getParentFile();
            while (parentDir == null) {
                parentDir.mkdirs();
                parentDir = parentDir.getParentFile();
            }
        }

        try {
            file.createNewFile();
        } catch (IOException ignored) {
        }
    }

    public static boolean write(String path, String data) {
        File file = new File(path);


        try (FileOutputStream fos = new FileOutputStream(file);) {
            fos.write(data.getBytes());
        } catch (IOException e) {
            return false;
        }

        return true;

    }
}
