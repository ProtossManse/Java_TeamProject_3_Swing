package screen;

import model.User;
import data.Path;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class FavoriteDialog extends VocManageDialog {

    public FavoriteDialog(JFrame parent, User currentUser) {
        super(parent, "즐겨찾기", currentUser);

        // 즐겨찾기에서는 파일 열기/생성 버튼 숨김
        this.fileBtn.setVisible(false);
        this.newFileBtn.setVisible(false);

        // 즐겨찾기 파일(_favorites.txt)을 바로 로드
        loadFavoriteFile();
    }

    private void loadFavoriteFile() {
        // 경로 설정
        this.currentFile = new File(Path.getUserFavoriteVocaFilePath(currentUser.getName()));

        // 파일이 없으면 생성
        if (!this.currentFile.exists()) {
            try {
                this.currentFile.getParentFile().mkdirs(); // 상위 폴더 확보
                this.currentFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "즐겨찾기 파일을 생성할 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 테이블 갱신
        refreshTable();
    }

    @Override
    protected void setInitialDir() {
        // VocManageDialog 생성자에서 호출되지만,
        // FavoriteDialog는 loadFavoriteFile()에서 currentFile을 직접 지정하므로
        // 여기서는 상위 폴더 정도만 잡아두거나 비워둬도 무방합니다.
        super.initialDirectory = new File("res/" + currentUser.getName() + "/vocas");
    }
}