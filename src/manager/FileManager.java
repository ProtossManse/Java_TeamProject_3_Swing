package manager;

import data.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


/**
 * 파일 입출력 관련 기능은 모두 여기서 수행합니다.
 * 따로 파일 입출력 관련 로직 구현하지 마시고, FileManager 통해서 진행해주세요!
 */
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
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean write(String path, String data) {
        return write(path, data, false);
    }

    public static boolean write(String path, String data, boolean append) {
        File file = new File(path);

        checkFile(file); // Ensure file and its parent directories exist before writing.

        try (FileOutputStream fos = new FileOutputStream(file, append);) {
            fos.write(data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return false;
        }

        return true;

    }

    public static void createUserDirectories(String username) {
        new File(Path.getUserDirPath(username)).mkdirs();
        new File(Path.getUserVocasDirPath(username)).mkdirs();
        new File(Path.getUserNotesDirPath(username)).mkdirs();

        try {
            new File(Path.getUserFavoriteVocaFilePath(username)).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
