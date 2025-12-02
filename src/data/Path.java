package data;

public class Path {
    public static final String RES_DIR = "res";
    public static final String USERS_TXT_PATH = RES_DIR + "/users.txt";
    public static final String PUBLIC_DIR = RES_DIR + "/public";
    public static final String PUBLIC_VOCAS_DIR = PUBLIC_DIR + "/vocas";

    public static String getUserDirPath(String username) {
        return RES_DIR + "/" + username;
    }

    public static String getUserVocasDirPath(String username) {
        return getUserDirPath(username) + "/vocas";
    }

    public static String getUserNotesDirPath(String username) {
        return getUserDirPath(username) + "/notes";
    }

    public static String getUserFavoriteVocaFilePath(String username) {
        return getUserVocasDirPath(username) + "/_favorite.txt";
    }
}

